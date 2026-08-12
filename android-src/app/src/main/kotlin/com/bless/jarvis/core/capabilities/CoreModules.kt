package com.bless.jarvis.core.capabilities

object CoreModules {
    fun registerDefaults() {
        CapabilityRegistry.register(AndroidCoreModule)
        CapabilityRegistry.register(VoiceModule)
        CapabilityRegistry.register(ShizukuModule)
        CapabilityRegistry.register(CloudModule)
    }
}

private object AndroidCoreModule : JarvisModule {
    override val id = "android-core"
    override val name = "Android Core"
    override val version = "1.0"
    override fun capabilities() = listOf(
        Capability("open_app", "Open Applications", "Launch installed applications", CapabilityStatus.ACTIVE),
        Capability("battery", "Battery", "Read battery state", CapabilityStatus.ACTIVE),
        Capability("device_info", "Device Information", "Read basic device information", CapabilityStatus.ACTIVE),
        Capability("screenshot", "Screenshot", "Capture the device screen", CapabilityStatus.REQUIRES_PERMISSION)
    )
}

private object VoiceModule : JarvisModule {
    override val id = "voice"
    override val name = "Voice"
    override val version = "1.0"
    override fun capabilities() = listOf(
        Capability("voice_input", "Voice Input", "Speech recognition", CapabilityStatus.ACTIVE, "Local Android", "1.0", listOf("RECORD_AUDIO")),
        Capability("voice_output", "Voice Output", "Spoken responses", CapabilityStatus.ACTIVE)
    )
}

private object ShizukuModule : JarvisModule {
    override val id = "android-control"
    override val name = "Android Control"
    override val version = "1.0"
    override fun capabilities() = listOf(
        Capability("shizuku", "Privileged Android Control", "Extended device control through Shizuku", CapabilityStatus.REQUIRES_PERMISSION, "Shizuku", "1.0")
    )
}

private object CloudModule : JarvisModule {
    override val id = "cloud-core"
    override val name = "Cloud Brain"
    override val version = "1.0"
    override fun capabilities() = listOf(
        Capability("ai_chat", "AI Chat", "Cloud JARVIS intelligence", CapabilityStatus.ACTIVE, "Cloud"),
        Capability("memory", "Memory", "Server-side conversation memory", CapabilityStatus.ACTIVE, "Cloud"),
        Capability("web_search", "Web Search", "Cloud-assisted web research", CapabilityStatus.ACTIVE, "Cloud")
    )
}
