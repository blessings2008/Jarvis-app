package com.bless.jarvis.network

data class ChatRequest(
    val message: String,
    val sessionId: String
)

data class JarvisAction(
    val id: String?,
    val name: String,
    val parameters: Map<String, Any>?,
    val requires_confirmation: Boolean?,
    val requires_verification: Boolean?
)

data class ChatResponse(
    val request_id: String?,
    val type: String?,
    val reply: String?,
    val actions: List<JarvisAction>?,
    val requires_confirmation: Boolean?,
    val sources: List<Source>?
)

data class Source(
    val title: String?,
    val url: String?
)

data class ActionResultRequest(
    val sessionId: String,
    val action: String,
    val actionId: String?,
    val status: String,
    val details: String?
)

data class ActionResultResponse(
    val status: String?
)
