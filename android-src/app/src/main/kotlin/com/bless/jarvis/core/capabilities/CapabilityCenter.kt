package com.bless.jarvis.core.capabilities

import androidx.compose.runtime.Immutable

@Immutable
data class CapabilityInfo(
    val id: String,
    val name: String,
    val description: String,
    val type: String,
    val status: CapabilityStatus,
    val version: String = "1.0",
    val permissions: List<String> = emptyList(),
    val usageCount: Int = 0,
    val successRate: Int? = null
)

object CapabilityCenter {
    private val capabilities = listOf(
        CapabilityInfo("ai_chat", "AI Chat", "Talk to the JARVIS cloud brain.", "Cloud", CapabilityStatus.ACTIVE),
        CapabilityInfo("open_app", "Open Applications", "Launch supported Android applications.", "Local Android", CapabilityStatus.ACTIVE),
        CapabilityInfo("voice", "Voice Assistant", "Speech input and spoken responses.", "Local Android", CapabilityStatus.ACTIVE, permissions = listOf("Microphone")),
        CapabilityInfo("shizuku", "Device Control", "Privileged Android operations through Shizuku.", "Integration", CapabilityStatus.REQUIRES_PERMISSION, permissions = listOf("Shizuku")),
        CapabilityInfo("battery", "Battery", "Read device battery status.", "Local Android", CapabilityStatus.ACTIVE),
        CapabilityInfo("volume", "Volume Control", "Control device audio volume.", "Local Android", CapabilityStatus.NOT_WIRED),
        CapabilityInfo("vision", "Vision", "Analyze images and the camera.", "Skill", CapabilityStatus.OFFLINE),
        CapabilityInfo("web_search", "Web Search", "Search the web through the cloud brain.", "Cloud", CapabilityStatus.ACTIVE)
    )

    fun all(): List<CapabilityInfo> = capabilities
    fun find(id: String): CapabilityInfo? = capabilities.firstOrNull { it.id == id }
}
