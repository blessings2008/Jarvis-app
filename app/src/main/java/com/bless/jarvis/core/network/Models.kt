package com.bless.jarvis.core.network

data class ChatRequest(val sessionId: String, val message: String, val approvalToken: String? = null)
data class ToolCall(val name: String = "", val arguments: Map<String, Any?> = emptyMap())
data class ChatResponse(val ok: Boolean = false, val message: String = "", val decision: String = "respond", val confidence: Double = .5, val tool_calls: List<ToolCall> = emptyList(), val observations: List<Map<String, Any?>> = emptyList())
data class Capability(val name: String, val description: String, val parameters: Map<String, Any?> = emptyMap(), val risk: String = "low", val source: String = "device", val version: Int = 1, val enabled: Boolean = true)
data class CapabilityRequest(val sessionId: String, val capabilities: List<Capability>)
data class WorldRequest(val sessionId: String, val observation: Map<String, Any?>)
data class HealthResponse(val ok: Boolean = false, val brain: String? = null, val database: String? = null)