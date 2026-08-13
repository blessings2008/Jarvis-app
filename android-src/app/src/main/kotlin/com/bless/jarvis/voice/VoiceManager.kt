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
    private var continuous = false
    private var restarting = false

    init {
        tts = TextToSpeech(appContext, this)
        if (SpeechRecognizer.isRecognitionAvailable(appContext)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
            recognizer?.setRecognitionListener(this)
        }
    }

    fun startConversation() { continuous = true; startListening() }
    fun stopConversation() { continuous = false; restarting = false; recognizer?.cancel(); onListeningChanged(false) }

    fun startListening() {
        val speechRecognizer = recognizer ?: run { onError("Speech recognition isn't available on this device."); return }
        tts?.stop()
        restarting = false
        onListeningChanged(true)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 900L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 300L)
        }
        try { speechRecognizer.startListening(intent) } catch (e: Exception) {
            onListeningChanged(false); onError("Couldn't start voice recognition.")
        }
    }

    fun stopListening() { continuous = false; recognizer?.stopListening(); onListeningChanged(false) }

    fun speak(text: String, resumeListening: Boolean = continuous) {
        if (!ttsReady || text.isBlank()) return
        continuous = resumeListening
        recognizer?.cancel()
        onListeningChanged(false)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis-response")
    }

    fun stopSpeaking() { tts?.stop() }

    fun destroy() {
        continuous = false
        recognizer?.destroy(); recognizer = null
        tts?.stop(); tts?.shutdown(); tts = null
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (!ttsReady) return
        val engine = tts ?: return
        val locale = Locale.US
        engine.language = locale
        engine.setSpeechRate(0.92f)
        engine.setPitch(0.90f)
        val bestVoice = engine.voices
            ?.filter { it.locale.language == locale.language }
            ?.maxByOrNull { it.quality * 10 - it.latency }
        if (bestVoice != null) engine.voice = bestVoice
    }

    override fun onResults(results: Bundle?) {
        onListeningChanged(false)
        restarting = false
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim()
        if (!text.isNullOrBlank()) onFinalText(text)
        else if (continuous) startListening()
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim()
        if (!text.isNullOrBlank()) onPartialText(text)
    }

    override fun onError(error: Int) {
        onListeningChanged(false)
        restarting = false
        if ((error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) && continuous) {
            startListening()
            return
        }
        if (error != SpeechRecognizer.ERROR_CLIENT) onError("Voice recognition failed. Try again.")
        if (continuous && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) startListening()
    }

    override fun onReadyForSpeech(params: Bundle?) { onListeningChanged(true) }
    override fun onBeginningOfSpeech() { onListeningChanged(true) }
    override fun onEndOfSpeech() { onListeningChanged(false) }
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
