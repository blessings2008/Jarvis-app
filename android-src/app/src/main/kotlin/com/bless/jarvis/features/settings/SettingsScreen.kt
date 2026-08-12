package com.bless.jarvis.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bless.jarvis.core.capabilities.CapabilityRegistry

@Composable
fun SettingsScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("SETTINGS", style = MaterialTheme.typography.headlineMedium) }
        item { SettingSection("JARVIS", listOf("Personality", "Response style", "Behavior")) }
        item { SettingSection("VOICE", listOf("Voice", "Wake word", "Speech settings")) }
        item { SettingSection("AI", listOf("Model", "Server", "Web search")) }
        item { SettingSection("MEMORY", listOf("Memory settings", "Retention", "Privacy")) }
        item { SettingSection("CAPABILITIES", listOf("Skills", "Permissions", "Installed modules")) }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("MODULE REGISTRY", style = MaterialTheme.typography.titleMedium)
                    Text("${CapabilityRegistry.modules().size} modules registered")
                    Text("${CapabilityRegistry.all().size} capabilities available")
                }
            }
        }
        item { SettingSection("SECURITY", listOf("App lock", "Sensitive actions", "Biometric")) }
        item { SettingSection("APPEARANCE", listOf("Theme", "Interface", "Animations")) }
        item { SettingSection("DEVELOPER", listOf("Diagnostics", "Capability registry", "Action tester", "Logs")) }
        item { SettingSection("ABOUT", listOf("Version", "System diagnostics")) }
    }
}

@Composable
private fun SettingSection(title: String, entries: List<String>) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            entries.forEach { Text(it, Modifier.padding(vertical = 9.dp)) }
        }
    }
}
