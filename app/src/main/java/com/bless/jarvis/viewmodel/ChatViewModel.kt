package com.bless.jarvis.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bless.jarvis.core.network.Capability
import com.bless.jarvis.core.network.CapabilityRequest
import com.bless.jarvis.core.network.ChatRequest
import com.bless.jarvis.core.network.RetrofitClient
import com.bless.jarvis.core.network.ToolCall
import com.bless.jarvis.core.network.WorldRequest
import com.bless.jarvis.execution.ActionExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class UiMessage(val text: String, val fromJarvis: Boolean)

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("jarvis", Context.MODE_PRIVATE)
    val sessionId: String = prefs.getString("session_id", null) ?: UUID.randomUUID().toString().also {
        prefs.edit().putString("session_id", it).apply()
    }
    private val executor = ActionExecutor(app)

    private val _messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val messages: StateFlow<List<UiMessage>> = _messages
    private val _state = MutableStateFlow("CONNECTING")
    val state: StateFlow<String> = _state
    private val _online = MutableStateFlow(false)
    val online: StateFlow<Boolean> = _online
    private val _activity = MutableStateFlow("Connecting to JARVIS")
    val activity: StateFlow<String> = _activity

    init { boot() }

    private fun boot() = viewModelScope.launch {
        _state.value = "CONNECTING"
        _activity.value = "Connecting to the cognitive core"
        try {
            val health = RetrofitClient.api.health()
            if (!health.ok || health.database != "connected") throw IllegalStateException("Brain/database unavailable")
            register()
            report()
            _online.value = true
            _state.value = "READY"
            _activity.value = "Waiting for you"
        } catch (_: Exception) {
            _online.value = false
            _state.value = "OFFLINE"
            _activity.value = "Connection failed"
        }
    }

    private suspend fun register() {
        RetrofitClient.api.register(CapabilityRequest(sessionId, listOf(
            Capability("open_app", "Launch an installed Android app", mapOf("package" to "string"), "medium"),
            Capability("launch_url", "Open a URL", mapOf("url" to "string"), "low"),
            Capability("device_info", "Read device information", risk = "low"),
            Capability("get_battery", "Read battery level", risk = "low"),
            Capability("get_volume", "Read media volume", risk = "low"),
            Capability("set_volume", "Set media volume", mapOf("volume" to "number"), "medium"),
            Capability("open_settings", "Open Android settings", risk = "low")
        )))
    }

    private suspend fun report(observation: Map<String, Any?> = mapOf("environment" to mapOf("platform" to "android", "online" to true))) {
        RetrofitClient.api.world(WorldRequest(sessionId, observation))
    }

    private fun actionMessage(call: ToolCall, result: Map<String, Any?>): String {
        val completed = result["completed"] == true
        if (!completed) return "I couldn't complete ${call.name}: ${result["error"] ?: "the Android body reported a failure."}"
        return when (call.name) {
            "get_battery" -> "Battery is at ${result["percent"] ?: "unknown"}% ."
            "get_volume" -> "Media volume is ${result["music"] ?: "unknown"}/${result["max"] ?: "unknown"}."
            "set_volume" -> "Media volume set to ${result["volume"] ?: "the requested level"}."
            "open_app" -> "Done. The requested app was opened."
            "launch_url" -> "Done. I opened the requested link."
            "device_info" -> "Device: ${result["manufacturer"] ?: "unknown"} ${result["model"] ?: ""}, Android ${result["android"] ?: "unknown"}."
            "open_settings" -> "Done. Android Settings is open."
            else -> "Done."
        }
    }

    fun send(text: String) {
        if (text.isBlank()) return
        val clean = text.trim()
        _messages.value = _messages.value + UiMessage(clean, false)
        _state.value = "THINKING"
        _activity.value = "JARVIS is thinking"
        viewModelScope.launch {
            try {
                if (!_online.value) {
                    boot()
                    return@launch
                }
                val response = RetrofitClient.api.chat(ChatRequest(sessionId, clean))
                _online.value = true
                var executedMessage: String? = null
                for (call in response.tool_calls) {
                    _state.value = "ACTING"
                    _activity.value = "Executing ${call.name}"
                    val result = try {
                        executor.execute(call)
                    } catch (e: Exception) {
                        mapOf("completed" to false, "error" to (e.message ?: "Action failed"))
                    }
                    executedMessage = actionMessage(call, result)
                    try { report(mapOf("environment" to mapOf("last_action" to call.name, "result" to result))) } catch (_: Exception) { }
                }
                val message = response.message.ifBlank {
                    executedMessage ?: if (response.tool_calls.isNotEmpty()) "Action completed." else "I'm here."
                }
                _messages.value = _messages.value + UiMessage(message, true)
                _state.value = if (response.decision == "learn") "LEARNING" else "READY"
                _activity.value = "Waiting for you"
            } catch (e: Exception) {
                _state.value = "READY"
                _activity.value = "Brain request failed"
                val detail = e.message?.takeIf { it.isNotBlank() } ?: "Unknown error"
                _messages.value = _messages.value + UiMessage("JARVIS couldn't complete that request: $detail", true)
                if (e is java.io.IOException) {
                    _online.value = false
                    _state.value = "OFFLINE"
                }
            }
        }
    }
}
