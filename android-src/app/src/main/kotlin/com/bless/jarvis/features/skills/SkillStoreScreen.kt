package com.bless.jarvis.features.skills

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bless.jarvis.core.skills.SkillManager
import com.bless.jarvis.core.skills.SkillManifest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillStoreScreen() {
    val context = LocalContext.current
    var installed by remember { mutableStateOf(SkillManager.installed(context).map { it.id }.toSet()) }
    var selected by remember { mutableStateOf<SkillManifest?>(null) }
    LaunchedEffect(Unit) { installed = SkillManager.installed(context).map { it.id }.toSet() }

    Scaffold(topBar = { TopAppBar(title = { Text("Skills") }) }) { padding ->
        if (selected == null) {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { Text("INSTALLED", style = MaterialTheme.typography.titleMedium) }
                items(SkillManager.installed(context)) { SkillCard(it, true) { selected = it } }
                item { Text("AVAILABLE", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp)) }
                items(SkillManager.catalog().filterNot { installed.contains(it.id) }) { SkillCard(it, false) { selected = it } }
            }
        } else {
            SkillDetail(selected!!, installed.contains(selected!!.id), Modifier.padding(padding)) {
                installed = SkillManager.installed(context).map { it.id }.toSet(); selected = null
            }
        }
    }
}

@Composable private fun SkillCard(skill: SkillManifest, isInstalled: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.weight(1f)) { Text(skill.name, style = MaterialTheme.typography.titleMedium); Text(skill.description, style = MaterialTheme.typography.bodyMedium); Text("v${skill.version} • ${skill.capabilities.size} capabilities") }
            Text(if (isInstalled) "✓ ACTIVE" else "INSTALL", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable private fun SkillDetail(skill: SkillManifest, installed: Boolean, modifier: Modifier, onDone: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("‹ Back", Modifier.clickable(onClick = onDone))
        Text(skill.name, style = MaterialTheme.typography.headlineSmall)
        Text(skill.description)
        Text("Version: ${skill.version}")
        Text("Developer: ${skill.author}")
        Text("CAPABILITIES", style = MaterialTheme.typography.titleMedium)
        skill.capabilities.forEach { Text("• $it") }
        if (skill.permissions.isNotEmpty()) Text("Permissions: ${skill.permissions.joinToString()}")
        Button(onClick = {
            if (installed) SkillManager.uninstall(context, skill.id).onSuccess { status = "✓ Skill removed" }.onFailure { status = it.message ?: "Failed" }
            else SkillManager.install(context, skill).onSuccess { status = "✓ Skill installed" }.onFailure { status = it.message ?: "Failed" }
        }) { Text(if (installed) "UNINSTALL" else "INSTALL") }
        if (status.isNotEmpty()) Text(status)
    }
}
