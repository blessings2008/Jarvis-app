package com.bless.jarvis.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.bless.jarvis.core.capabilities.CoreModules
import com.bless.jarvis.core.navigation.JarvisDestination
import com.bless.jarvis.features.activity.ActivityScreen
import com.bless.jarvis.features.capabilities.CapabilityScreen
import com.bless.jarvis.features.dashboard.DashboardScreen
import com.bless.jarvis.features.platform.ModuleScreen
import com.bless.jarvis.features.platform.PlatformIndex
import com.bless.jarvis.features.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisApp() {
    LaunchedEffect(Unit) { CoreModules.registerDefaults() }
    var destination by rememberSaveable { mutableStateOf(JarvisDestination.HOME) }
    var showPlatform by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (destination == JarvisDestination.CHAT) "J.A.R.V.I.S." else destination.label) },
                actions = {
                    IconButton(onClick = { showPlatform = true }) { Icon(Icons.Default.Apps, "Modules") }
                    IconButton(onClick = { destination = JarvisDestination.SETTINGS; showPlatform = false }) { Icon(Icons.Default.Settings, "Settings") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(destination == JarvisDestination.HOME, { destination = JarvisDestination.HOME }, { Icon(Icons.Default.Home, "Home") }, label = { Text("Home") })
                NavigationBarItem(destination == JarvisDestination.CHAT, { destination = JarvisDestination.CHAT }, { Icon(Icons.Default.AutoAwesome, "JARVIS") }, label = { Text("JARVIS") })
                NavigationBarItem(destination == JarvisDestination.ACTIVITY, { destination = JarvisDestination.ACTIVITY }, { Icon(Icons.Default.History, "Activity") }, label = { Text("Activity") })
                NavigationBarItem(destination == JarvisDestination.SETTINGS, { destination = JarvisDestination.SETTINGS }, { Icon(Icons.Default.Settings, "Settings") }, label = { Text("Settings") })
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (showPlatform) {
                PlatformIndex { destination = it; showPlatform = false }
            } else {
                when (destination) {
                    JarvisDestination.HOME -> DashboardScreen({ destination = JarvisDestination.CHAT }, { destination = JarvisDestination.SETTINGS })
                    JarvisDestination.CHAT -> ChatScreen()
                    JarvisDestination.ACTIVITY -> ActivityScreen()
                    JarvisDestination.SETTINGS -> SettingsScreen()
                    JarvisDestination.CAPABILITIES -> CapabilityScreen()
                    else -> ModuleScreen(destination)
                }
            }
        }
    }
}
