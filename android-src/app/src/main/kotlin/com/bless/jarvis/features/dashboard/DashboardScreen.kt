package com.bless.jarvis.features.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bless.jarvis.core.capabilities.CapabilityRegistry
import com.bless.jarvis.core.capabilities.CapabilityStatus

@Composable
fun DashboardScreen(onOpenChat: () -> Unit, onOpenSettings: () -> Unit) {
    val capabilities = CapabilityRegistry.all()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("J.A.R.V.I.S.", style = MaterialTheme.typography.headlineMedium)
            Text("● SYSTEM ONLINE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("JARVIS CORE", style = MaterialTheme.typography.titleLarge)
                    Text("Brain    ONLINE")
                    Text("Memory  ONLINE")
                    Text("Device  CONNECTED")
                }
            }
        }
        item { Text("QUICK ACTIONS", style = MaterialTheme.typography.titleMedium) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onOpenChat, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Mic, null); Spacer(Modifier.width(6.dp)); Text("Voice") }
                OutlinedButton(onClick = onOpenSettings, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Settings, null); Spacer(Modifier.width(6.dp)); Text("Settings") }
            }
        }
        item { Text("CAPABILITIES", style = MaterialTheme.typography.titleMedium) }
        items(capabilities) { capability ->
            ListItem(
                headlineContent = { Text(capability.name) },
                supportingContent = { Text(capability.description) },
                trailingContent = { Text(statusLabel(capability.status)) }
            )
        }
    }
}

private fun statusLabel(status: CapabilityStatus) = when (status) {
    CapabilityStatus.ACTIVE -> "● READY"
    CapabilityStatus.AVAILABLE -> "AVAILABLE"
    CapabilityStatus.REQUIRES_SKILL -> "SKILL"
    CapabilityStatus.REQUIRES_PERMISSION -> "PERMISSION"
    CapabilityStatus.OFFLINE -> "OFFLINE"
    CapabilityStatus.NOT_WIRED -> "NOT WIRED"
}
