package com.bless.jarvis.features.capabilities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bless.jarvis.core.capabilities.CapabilityCenter
import com.bless.jarvis.core.capabilities.CapabilityInfo
import com.bless.jarvis.core.capabilities.CapabilityStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapabilityScreen() {
    var selected by remember { mutableStateOf<CapabilityInfo?>(null) }
    val items = CapabilityCenter.all()

    Scaffold(topBar = { TopAppBar(title = { Text("Capabilities") }) }) { padding ->
        if (selected == null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items) { capability ->
                    CapabilityCard(capability) { selected = capability }
                }
            }
        } else {
            CapabilityDetail(selected!!, Modifier.padding(padding)) { selected = null }
        }
    }
}

@Composable
private fun CapabilityCard(capability: CapabilityInfo, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(capability.name, style = MaterialTheme.typography.titleMedium)
                Text(capability.description, style = MaterialTheme.typography.bodyMedium)
            }
            Text(statusLabel(capability.status), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun CapabilityDetail(capability: CapabilityInfo, modifier: Modifier, onBack: () -> Unit) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("‹ Back", modifier = Modifier.clickable(onClick = onBack), style = MaterialTheme.typography.labelLarge)
        Text(capability.name, style = MaterialTheme.typography.headlineSmall)
        Text(capability.description, style = MaterialTheme.typography.bodyLarge)
        Text("Status: ${statusLabel(capability.status)}")
        Text("Type: ${capability.type}")
        Text("Version: ${capability.version}")
        Text("Used: ${capability.usageCount} times")
        capability.successRate?.let { Text("Success rate: $it%") }
        if (capability.permissions.isNotEmpty()) Text("Permissions: ${capability.permissions.joinToString()}")
    }
}

private fun statusLabel(status: CapabilityStatus): String = when (status) {
    CapabilityStatus.ACTIVE -> "● ACTIVE"
    CapabilityStatus.AVAILABLE -> "● AVAILABLE"
    CapabilityStatus.NOT_WIRED -> "⚠ NOT WIRED"
    CapabilityStatus.REQUIRES_SKILL -> "○ REQUIRES SKILL"
    CapabilityStatus.REQUIRES_PERMISSION -> "🔐 PERMISSION"
    CapabilityStatus.OFFLINE -> "○ OFFLINE"
}
