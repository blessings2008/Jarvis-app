package com.bless.jarvis.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var isListening by remember { mutableStateOf(false) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    val textToSpeech = remember {
        TextToSpeech(context, null)
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer?.destroy()
            speechRecognizer = startListening(
                context = context,
                onResult = { text ->
                    viewModel.onInputChange(text)
                    isListening = false
                },
                onListeningChanged = { listening -> isListening = listening }
            )
        } else {
            isListening = false
        }
    }

    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)

            val lastMessage = viewModel.messages.last()
            if (!lastMessage.isUser && viewModel.messages.size > 1) {
                textToSpeech.language = Locale.getDefault()
                textToSpeech.speak(
                    lastMessage.text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "jarvis-${viewModel.messages.size}"
                )
            }
        }
    }

    fun toggleListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            speechRecognizer?.destroy()
            speechRecognizer = startListening(
                context = context,
                onResult = { text ->
                    viewModel.onInputChange(text)
                    isListening = false
                },
                onListeningChanged = { listening -> isListening = listening }
            )
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
                items(viewModel.messages) { message ->
                    MessageBubble(message)
                }
                if (viewModel.isLoading.value) {
                    item {
                        Row(modifier = Modifier.padding(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("JARVIS is thinking...")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.inputText.value,
                    onValueChange = viewModel::onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(if (isListening) "Listening..." else "Type a command...") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(onClick = { toggleListening() }) {
                    Text(if (isListening) "■" else "🎙")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = { viewModel.sendMessage() },
                    enabled = viewModel.inputText.value.isNotBlank() && !viewModel.isLoading.value
                ) {
                    Text("Send")
                }
            }
        }
    }
}

private fun startListening(
    context: android.content.Context,
    onResult: (String) -> Unit,
    onListeningChanged: (Boolean) -> Unit
): SpeechRecognizer {
    val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    recognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: android.os.Bundle?) { onListeningChanged(true) }
        override fun onBeginningOfSpeech() { onListeningChanged(true) }
        override fun onEndOfSpeech() { onListeningChanged(false) }
        override fun onError(error: Int) { onListeningChanged(false) }
        override fun onResults(results: android.os.Bundle?) {
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (!text.isNullOrBlank()) onResult(text)
            onListeningChanged(false)
        }
        override fun onPartialResults(partialResults: android.os.Bundle?) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
    })

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
    }
    recognizer.startListening(intent)
    return recognizer
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
