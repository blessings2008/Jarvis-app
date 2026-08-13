package com.bless.jarvis.features.platform

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bless.jarvis.core.navigation.JarvisDestination

private data class PlatformCard(val destination: JarvisDestination, val icon: String, val title: String, val description: String)

private val cards = listOf(
    PlatformCard(JarvisDestination.CAPABILITIES, "🧩", "Capabilities", "Everything JARVIS can do."),
    PlatformCard(JarvisDestination.CONVERSATIONS, "💬", "Conversations", "Separate chats and contexts."),
    PlatformCard(JarvisDestination.MEMORY, "🧠", "Memory", "Stored facts, preferences and projects."),
    PlatformCard(JarvisDestination.AUTOMATIONS, "⚡", "Automations", "Rules that let JARVIS act proactively."),
    PlatformCard(JarvisDestination.EVOLUTION, "🧬", "Evolution", "Capability gaps and human-approved suggestions."),
    PlatformCard(JarvisDestination.SKILLS, "🛠️", "Skills", "Installable groups of capabilities."),
    PlatformCard(JarvisDestination.DEVICES, "📱", "Devices", "Connected phones, PCs and future devices."),
    PlatformCard(JarvisDestination.VISION, "📷", "Vision", "Camera and image intelligence."),
    PlatformCard(JarvisDestination.VOICE, "🎙️", "Voice", "Voice assistant and speech controls."),
    PlatformCard(JarvisDestination.NOTIFICATIONS, "🔔", "Notifications", "JARVIS alerts and suggestions."),
    PlatformCard(JarvisDestination.ANALYTICS, "📊", "Analytics", "Usage, reliability and performance."),
    PlatformCard(JarvisDestination.SECURITY, "🔐", "Security", "Permissions and sensitive-action protection."),
    PlatformCard(JarvisDestination.DEVELOPER, "🧪", "Developer", "Diagnostics and internal tools.")
)

@Composable
fun PlatformIndex(onOpen: (JarvisDestination) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("JARVIS PLATFORM", style = MaterialTheme.typography.headlineSmall) }
        item { Text("Every feature is a module. Open one to manage it.", style = MaterialTheme.typography.bodyMedium) }
        items(cards) { card ->
            ElevatedCard(modifier = Modifier.fillMaxWidth().clickable { onOpen(card.destination) }) {
                Row(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(card.icon, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(card.title, style = MaterialTheme.typography.titleMedium)
                        Text(card.description, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("›", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}

@Composable
fun ModuleScreen(destination: JarvisDestination) {
    val descriptions = mapOf(
        JarvisDestination.CONVERSATIONS to "Manage independent conversations, contexts and sessions.",
        JarvisDestination.MEMORY to "Control what JARVIS remembers and forgets.",
        JarvisDestination.AUTOMATIONS to "Create WHEN → IF → DO rules for proactive behavior.",
        JarvisDestination.EVOLUTION to "Review capability gaps and approve future skills.",
        JarvisDestination.SKILLS to "Installed and available skill packages.",
        JarvisDestination.DEVICES to "Connected devices and their live status.",
        JarvisDestination.VISION to "Camera, OCR and image-analysis capabilities.",
        JarvisDestination.VOICE to "Voice assistant, wake word and speech settings.",
        JarvisDestination.NOTIFICATIONS to "JARVIS alerts, failures and suggestions.",
        JarvisDestination.ANALYTICS to "Requests, actions, success rates and performance.",
        JarvisDestination.SECURITY to "App lock, confirmations, biometrics and trusted devices.",
        JarvisDestination.DEVELOPER to "Diagnostics, registry inspection, action testing and logs."
    )
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text(destination.label.uppercase(), style = MaterialTheme.typography.headlineSmall) }
        item { Text(descriptions[destination] ?: "JARVIS module", style = MaterialTheme.typography.bodyLarge) }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("MODULE READY", style = MaterialTheme.typography.titleMedium)
                    Text("This module is registered in the JARVIS platform architecture. Its capabilities can be added independently without coupling the UI to the executor.")
                }
            }
        }
    }
}
