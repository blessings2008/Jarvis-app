package com.bless.jarvis.core.skills

import androidx.compose.runtime.Immutable

@Immutable
data class SkillManifest(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val capabilities: List<String>,
    val permissions: List<String> = emptyList(),
    val dependencies: List<String> = emptyList(),
    val source: String = "JARVIS Repository"
)

enum class SkillState { INSTALLED, AVAILABLE, DISABLED, UPDATE_AVAILABLE }

@Immutable
data class SkillEntry(
    val manifest: SkillManifest,
    val state: SkillState
)
