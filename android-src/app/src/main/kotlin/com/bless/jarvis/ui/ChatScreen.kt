package com.bless.jarvis.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bless.jarvis.viewmodel.ChatMessage
import com.bless.jarvis.viewmodel.ChatViewModel
import com.bless.jarvis.voice.VoiceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var isListening by remember { mutableStateOf(false) }
    var liveTranscript by remember { mutableStateOf("") }
    var voiceError by remember { mutableStateOf<String?>(null) }

    val voiceManager = remember(context) {
        VoiceManager(
            context = context,
            onPartialText = { liveTranscript = it },
            onFinalText = { text ->
                liveTranscript = ""
                voiceError = null
                viewModel.sendMessage(text)
            },
            onListeningChanged = { listening ->
                isListening = listening
                if (!listening) liveTranscript = ""
            },
            onError = { message ->
                isListening = false
                liveTranscript = ""
                voiceError = message
            }
        )
    }

    DisposableEffect(voiceManager) {
        onDispose { voiceManager.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceError = null
            voiceManager.startListening()
        } else {
            voiceError = "Microphone permission is required for voice mode."
        }
    }

    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
            val last = viewModel.messages.lastIndex
            val message = viewModel.messages[last]
            if (last > 0 && !message.isUser && !message.text.startsWith("[")) {
                voiceManager.speak(message.text)
            }
        }
    }

    fun toggleListening() {
        if (isListening) {
            voiceManager.stopListening()
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            voiceError = null
            voiceManager.startListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("J.A.R.V.I.S.", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(viewModel.messages) { message -> MessageBubble(message) }

                if (isListening) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("● Listening", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(liveTranscript.ifBlank { "Speak now..." })
                            }
                        }
                    }
                }

                if (viewModel.isLoading.value) {
                    item {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("JARVIS is thinking...")
                        }
                    }
                }
            }

            if (voiceError != null) {
                Text(
                    text = voiceError!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.inputText.value,
                    onValueChange = viewModel::onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a command...") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilledIconButton(
                    onClick = { toggleListening() },
                    enabled = !viewModel.isLoading.value
                ) {
                    Text(if (isListening) "■" else "🎙")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = { viewModel.sendMessage() },
                    enabled = viewModel.inputText.value.isNotBlank() && !viewModel.isLoading.value
                ) { Text("Send") }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (message.isUser)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = bubbleColor,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
