package com.bless.jarvis.features.activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ActivityScreen() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("ACTIVITY", style = MaterialTheme.typography.headlineMedium) }
        item { Text("Today", style = MaterialTheme.typography.titleMedium) }
        item { ActivityRow("🎙", "Voice command", "Ready for activity history") }
        item { ActivityRow("📱", "Device action", "Action events will appear here") }
        item { ActivityRow("🧠", "AI request", "Conversation events are tracked") }
    }
}

@Composable
private fun ActivityRow(icon: String, title: String, detail: String) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("$icon  $title", style = MaterialTheme.typography.titleMedium)
            Text(detail, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
