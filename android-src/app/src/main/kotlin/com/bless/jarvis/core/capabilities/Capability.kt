package com.bless.jarvis.core.capabilities

enum class CapabilityStatus { ACTIVE, AVAILABLE, REQUIRES_SKILL, REQUIRES_PERMISSION, OFFLINE, NOT_WIRED }

data class Capability(
    val id: String,
    val name: String,
    val description: String,
    val status: CapabilityStatus,
    val type: String = "Local Android",
    val version: String = "1.0",
    val permissions: List<String> = emptyList()
)

interface JarvisModule {
    val id: String
    val name: String
    val version: String
    fun capabilities(): List<Capability>
}

object CapabilityRegistry {
    private val modules = mutableListOf<JarvisModule>()

    fun register(module: JarvisModule) {
        modules.removeAll { it.id == module.id }
        modules += module
    }

    fun modules(): List<JarvisModule> = modules.toList()

    fun all(): List<Capability> = modules.flatMap { it.capabilities() }

    fun find(id: String): Capability? = all().firstOrNull { it.id == id }
}
