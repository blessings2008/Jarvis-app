package com.bless.jarvis.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bless.jarvis.core.network.Capability
import com.bless.jarvis.core.network.CapabilityRequest
import com.bless.jarvis.core.network.ChatRequest
import com.bless.jarvis.core.network.RetrofitClient
import com.bless.jarvis.core.network.WorldRequest
import com.bless.jarvis.execution.ActionExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class UiMessage(val text: String, val fromJarvis: Boolean)

class ChatViewModel(app: Application) : AndroidViewModel(app) {
    val sessionId: String = UUID.randomUUID().toString()
    private val executor = ActionExecutor(app)

    private val _messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val messages: StateFlow<List<UiMessage>> = _messages

    private val _state = MutableStateFlow("READY")
    val state: StateFlow<String> = _state

    private val _online = MutableStateFlow(false)
    val online: StateFlow<Boolean> = _online

    private val _activity = MutableStateFlow("Waiting for you")
    val activity: StateFlow<String> = _activity

    init {
        viewModelScope.launch {
            try {
                RetrofitClient.api.health()
                _online.value = true
                register()
                report()
            } catch (_: Exception) {
                _online.value = false
            }
        }
    }

    private suspend fun register() {
        RetrofitClient.api.register(
            CapabilityRequest(
                sessionId,
                listOf(
                    Capability("open_app", "Launch an installed Android app", mapOf("package" to "string"), "medium"),
                    Capability("launch_url", "Open a URL", mapOf("url" to "string"), "low"),
                    Capability("device_info", "Read device information", risk = "low"),
                    Capability("get_battery", "Read battery level", risk = "low"),
                    Capability("get_volume", "Read media volume", risk = "low"),
                    Capability("set_volume", "Set media volume", mapOf("volume" to "number"), "medium"),
                    Capability("open_settings", "Open Android settings", risk = "low")
                )
            )
        )
    }

    private suspend fun report() {
        RetrofitClient.api.world(
            WorldRequest(
                sessionId,
                mapOf("environment" to mapOf("platform" to "android", "online" to true))
            )
        )
    }

    fun send(text: String) {
        if (text.isBlank()) return
        _messages.value = _messages.value + UiMessage(text, false)
        _state.value = "THINKING"
        _activity.value = "Understanding your request"

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.chat(ChatRequest(sessionId, text))

                for (call in response.tool_calls) {
                    _state.value = "ACTING"
                    _activity.value = "Executing ${call.name}"
                    val result = executor.execute(call)
                    RetrofitClient.api.world(
                        WorldRequest(
                            sessionId,
                            mapOf(
                                "environment" to mapOf(
                                    "last_action" to call.name,
                                    "result" to result
                                )
                            )
                        )
                    )
                }

                _messages.value = _messages.value + UiMessage(
                    response.message.ifBlank { "Done." },
                    true
                )
                _state.value = if (response.decision == "learn") "LEARNING" else "READY"
                _activity.value = "Waiting for you"
            } catch (_: Exception) {
                _online.value = false
                _state.value = "OFFLINE"
                _activity.value = "Connection lost"
                _messages.value = _messages.value + UiMessage(
                    "I can't reach my brain right now. Check your connection and try again.",
                    true
                )
            }
        }
    }
}
