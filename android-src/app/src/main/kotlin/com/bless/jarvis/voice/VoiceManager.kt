package com.bless.jarvis.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceManager(
    context: Context,
    private val onPartialText: (String) -> Unit,
    private val onFinalText: (String) -> Unit,
    private val onListeningChanged: (Boolean) -> Unit,
    private val onError: (String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        tts = TextToSpeech(appContext, this)
        if (SpeechRecognizer.isRecognitionAvailable(appContext)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
            recognizer?.setRecognitionListener(this)
        }
    }

    fun startListening() {
        val speechRecognizer = recognizer ?: run {
            onError("Speech recognition isn't available on this device.")
            return
        }
        tts?.stop()
        onListeningChanged(true)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500L)
        }
        try {
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            onListeningChanged(false)
            onError("Couldn't start voice recognition: ${e.message ?: "unknown error"}")
        }
    }

    fun stopListening() {
        recognizer?.stopListening()
        onListeningChanged(false)
    }

    fun speak(text: String) {
        if (!ttsReady || text.isBlank()) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis-response")
    }

    fun stopSpeaking() { tts?.stop() }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) {
            tts?.language = Locale.getDefault()
            tts?.setSpeechRate(0.95f)
            tts?.setPitch(0.95f)
        }
    }

    override fun onResults(results: Bundle?) {
        onListeningChanged(false)
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
        if (!text.isNullOrBlank()) onFinalText(text)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
        if (!text.isNullOrBlank()) onPartialText(text)
    }

    override fun onError(error: Int) {
        onListeningChanged(false)
        when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> onError("I didn't catch that. Try again.")
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> onError("I didn't hear anything.")
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> onError("Voice recognition needs an internet connection.")
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> onError("Microphone permission is required for voice mode.")
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> onError("Voice recognition is busy. Try again.")
            SpeechRecognizer.ERROR_CLIENT -> Unit
            else -> onError("Voice recognition failed. Try again.")
        }
    }

    override fun onReadyForSpeech(params: Bundle?) { onListeningChanged(true) }
    override fun onBeginningOfSpeech() { onListeningChanged(true) }
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() { onListeningChanged(false) }
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
