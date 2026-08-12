package com.bless.jarvis.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.bless.jarvis.core.capabilities.CoreModules
import com.bless.jarvis.core.navigation.JarvisDestination
import com.bless.jarvis.features.activity.ActivityScreen
import com.bless.jarvis.features.dashboard.DashboardScreen
import com.bless.jarvis.features.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisApp() {
    LaunchedEffect(Unit) { CoreModules.registerDefaults() }
    var destination by rememberSaveable { mutableStateOf(JarvisDestination.HOME) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (destination == JarvisDestination.CHAT) "J.A.R.V.I.S." else destination.label) },
                actions = {
                    IconButton(onClick = { destination = JarvisDestination.SETTINGS }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = destination == JarvisDestination.HOME,
                    onClick = { destination = JarvisDestination.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = destination == JarvisDestination.CHAT,
                    onClick = { destination = JarvisDestination.CHAT },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "JARVIS") },
                    label = { Text("JARVIS") }
                )
                NavigationBarItem(
                    selected = destination == JarvisDestination.ACTIVITY,
                    onClick = { destination = JarvisDestination.ACTIVITY },
                    icon = { Icon(Icons.Default.History, contentDescription = "Activity") },
                    label = { Text("Activity") }
                )
                NavigationBarItem(
                    selected = destination == JarvisDestination.SETTINGS,
                    onClick = { destination = JarvisDestination.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { paddingValues ->
        when (destination) {
            JarvisDestination.HOME -> DashboardScreen(
                onOpenChat = { destination = JarvisDestination.CHAT },
                onOpenSettings = { destination = JarvisDestination.SETTINGS }
            )
            JarvisDestination.CHAT -> ChatScreen()
            JarvisDestination.ACTIVITY -> ActivityScreen()
            JarvisDestination.SETTINGS -> SettingsScreen()
        }
    }
}
