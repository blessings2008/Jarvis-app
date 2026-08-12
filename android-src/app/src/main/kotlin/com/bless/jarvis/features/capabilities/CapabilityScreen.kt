package com.bless.jarvis.features.capabilities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bless.jarvis.core.capabilities.CapabilityCenter
import com.bless.jarvis.core.capabilities.CapabilityInfo
import com.bless.jarvis.core.capabilities.CapabilityStatus

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
    CapabilityStatus.NOT_WIRED -> "⚠ NOT WIRED"
    CapabilityStatus.REQUIRES_SKILL -> "○ REQUIRES SKILL"
    CapabilityStatus.REQUIRES_PERMISSION -> "🔐 PERMISSION"
    CapabilityStatus.OFFLINE -> "○ OFFLINE"
}
