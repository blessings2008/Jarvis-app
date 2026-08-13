package com.bless.jarvis.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bless.jarvis.viewmodel.ChatViewModel
import com.bless.jarvis.voice.VoiceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var voiceMode by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var partialText by remember { mutableStateOf("") }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var lastSpokenIndex by remember { mutableIntStateOf(-1) }

    val voiceManager = remember(context) {
        VoiceManager(
            context = context,
            onPartialText = { partialText = it },
            onFinalText = { text ->
                partialText = ""
                voiceError = null
                viewModel.sendMessage(text)
            },
            onListeningChanged = { isListening = it },
            onError = { voiceError = it }
        )
    }

    DisposableEffect(voiceManager) { onDispose { voiceManager.destroy() } }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) { voiceMode = true; voiceManager.startConversation() }
        else { voiceMode = false; voiceError = "Microphone permission is required for voice mode." }
    }

    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.lastIndex)
            val index = viewModel.messages.lastIndex
            val message = viewModel.messages[index]
            if (index != lastSpokenIndex && !message.isUser && message.speak) {
                lastSpokenIndex = index
                voiceManager.speak(message.text, voiceMode)
            }
        }
    }

    fun toggleVoice() {
        voiceError = null
        if (voiceMode) {
            voiceMode = false
            partialText = ""
            voiceManager.stopConversation()
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            voiceMode = true
            voiceManager.startConversation()
        } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("J.A.R.V.I.S.", fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(if (voiceMode) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape))
                        Spacer(Modifier.width(5.dp))
                        Text(if (voiceMode) "VOICE MODE" else "ONLINE", style = MaterialTheme.typography.labelSmall)
                    }
                }
            })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
            ) {
                items(viewModel.messages) { message -> MessageBubble(message) }
                if (viewModel.isLoading.value) item {
                    Surface(shape = RoundedCornerShape(18.dp), tonalElevation = 2.dp) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(9.dp))
                            Text("JARVIS is thinking…")
                        }
                    }
                }
            }

            if (isListening || partialText.isNotBlank()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    VoicePulse()
                    Spacer(Modifier.width(10.dp))
                    Text(partialText.ifBlank { "Listening…" }, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (voiceError != null) Text(voiceError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 18.dp, vertical = 3.dp))

            Surface(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), tonalElevation = 5.dp, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = viewModel.inputText.value,
                        onValueChange = viewModel::onInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(if (voiceMode) "Voice mode active…" else "Ask JARVIS anything") },
                        maxLines = 3,
                        shape = RoundedCornerShape(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(onClick = { toggleVoice() }, modifier = Modifier.size(52.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (voiceMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer)) {
                        Icon(if (voiceMode) Icons.Default.Stop else Icons.Default.Mic, if (voiceMode) "Stop voice mode" else "Start voice mode")
                    }
                    Spacer(Modifier.width(6.dp))
                    FilledIconButton(onClick = { viewModel.sendMessage() }, enabled = viewModel.inputText.value.isNotBlank() && !viewModel.isLoading.value, modifier = Modifier.size(52.dp)) {
                        Icon(Icons.Default.Send, "Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun VoicePulse() {
    val transition = rememberInfiniteTransition(label = "voice")
    val scale by transition.animateFloat(1f, 1.45f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "scale")
    Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(22.dp).scale(scale).alpha(0.22f).background(MaterialTheme.colorScheme.primary, CircleShape))
        Box(Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
    }
}

@Composable
private fun MessageBubble(message: com.bless.jarvis.viewmodel.ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (message.isUser) RoundedCornerShape(20.dp, 20.dp, 5.dp, 20.dp) else RoundedCornerShape(20.dp, 20.dp, 20.dp, 5.dp)
    val brush = if (message.isUser) Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer)) else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface))
    Box(Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(color = Color.Transparent, shape = shape, modifier = Modifier.widthIn(max = 330.dp)) {
            Box(Modifier.background(brush, shape).padding(horizontal = 15.dp, vertical = 11.dp)) { Text(message.text, style = MaterialTheme.typography.bodyLarge) }
        }
    }
}
