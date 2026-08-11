package com.bless.jarvis.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bless.jarvis.execution.ActionExecutor
import com.bless.jarvis.network.ActionResultRequest
import com.bless.jarvis.network.ChatRequest
import com.bless.jarvis.network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionId = "android-${UUID.randomUUID()}"

    val messages = mutableStateListOf<ChatMessage>()
    val inputText = mutableStateOf("")
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    init {
        messages.add(ChatMessage("Online. Standing by, Bless.", isUser = false))
    }

    fun onInputChange(newText: String) {
        inputText.value = newText
    }

    fun sendMessage() {
        val text = inputText.value.trim()
        if (text.isEmpty() || isLoading.value) return

        messages.add(ChatMessage(text, isUser = true))
        inputText.value = ""
        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.sendMessage(
                    ChatRequest(message = text, sessionId = sessionId)
                )

                val reply = response.reply ?: "I didn't get a proper response, Bless."
                messages.add(ChatMessage(reply, isUser = false))

                response.actions?.forEach { action ->
                    if (action.requires_confirmation == true) {
                        return@forEach
                    }
                    executeAndReport(action.name, action.parameters, action.id)
                }
            } catch (e: Exception) {
                errorMessage.value = "Connection error: ${e.message}"
                messages.add(ChatMessage("Sorry, I couldn't reach the server. (${e.message})", isUser = false))
            } finally {
                isLoading.value = false
            }
        }
    }

    private suspend fun executeAndReport(actionName: String, parameters: Map<String, Any>?, actionId: String?) {
        val result = ActionExecutor.execute(getApplication(), actionName, parameters)
        messages.add(ChatMessage("[$actionName -> ${result.status}: ${result.details}]", isUser = false))

        try {
            RetrofitClient.api.reportActionResult(
                ActionResultRequest(
                    sessionId = sessionId,
                    action = actionName,
                    actionId = actionId,
                    status = result.status,
                    details = result.details
                )
            )
        } catch (e: Exception) {
            // Non-fatal — the action already ran either way.
        }
    }
}
