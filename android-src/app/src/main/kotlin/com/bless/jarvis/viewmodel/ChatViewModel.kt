package com.bless.jarvis.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bless.jarvis.execution.ActionExecutor
import com.bless.jarvis.execution.LocalCommandRouter
import com.bless.jarvis.network.ActionResultRequest
import com.bless.jarvis.network.ChatRequest
import com.bless.jarvis.network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val speak: Boolean = !isUser
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionId = "android-${UUID.randomUUID()}"

    val messages = mutableStateListOf<ChatMessage>()
    val inputText = mutableStateOf("")
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    init { messages.add(ChatMessage("Online. Standing by, Bless.", false, false)) }

    fun onInputChange(newText: String) { inputText.value = newText }

    fun sendMessage(message: String = inputText.value) {
        val text = message.trim()
        if (text.isEmpty() || isLoading.value) return
        messages.add(ChatMessage(text, true, false))
        inputText.value = ""
        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val local = LocalCommandRouter.match(text)
                if (local != null) {
                    executeAndReport(local.action, local.parameters, null, true)
                    return@launch
                }

                val response = RetrofitClient.api.sendMessage(ChatRequest(text, sessionId))
                messages.add(ChatMessage(response.reply ?: "I didn't get a proper response, Bless.", false, true))
                response.actions?.forEach { action ->
                    if (action.requires_confirmation != true) executeAndReport(action.name, action.parameters, action.id, false)
                }
            } catch (e: Exception) {
                errorMessage.value = "Connection error: ${e.message}"
                messages.add(ChatMessage("I couldn't reach the server. Please check your connection.", false, true))
            } finally { isLoading.value = false }
        }
    }

    private suspend fun executeAndReport(actionName: String, parameters: Map<String, Any>?, actionId: String?, local: Boolean) {
        val result = ActionExecutor.execute(getApplication(), actionName, parameters)
        val prefix = if (local) "[Local action" else "[$actionName"
        messages.add(ChatMessage("$prefix → ${result.status}: ${result.details}]", false, false))
        try {
            RetrofitClient.api.reportActionResult(ActionResultRequest(sessionId, actionName, actionId, result.status, result.details))
        } catch (_: Exception) { }
    }
}
