package com.example.data.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class ConversationLanguage(val displayName: String, val tag: String) {
    AUTO("Auto (EN / বাংলা)", "auto"),
    BANGLA("বাংলা (Bangla)", "bn-BD"),
    ENGLISH("English (UK/US)", "en-US")
}

class SpeechManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _speechRmsLevel = MutableStateFlow(0f)
    val speechRmsLevel: StateFlow<Float> = _speechRmsLevel.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(ConversationLanguage.AUTO)
    val selectedLanguage: StateFlow<ConversationLanguage> = _selectedLanguage.asStateFlow()

    private var onSpeechDoneCallback: (() -> Unit)? = null

    init {
        initTts()
    }

    fun setLanguage(language: ConversationLanguage) {
        _selectedLanguage.value = language
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                tts?.let { engine ->
                    // Default to English locale with crisp JARVIS pitch and rate
                    val result = engine.setLanguage(Locale.UK)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.setLanguage(Locale.US)
                    }
                    engine.setPitch(0.92f)
                    engine.setSpeechRate(1.02f)

                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            val callback = onSpeechDoneCallback
                            onSpeechDoneCallback = null
                            callback?.invoke()
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            val callback = onSpeechDoneCallback
                            onSpeechDoneCallback = null
                            callback?.invoke()
                        }
                    })
                }
            }
        }
    }

    /**
     * Speaks the text using appropriate voice modulation and language engine (Bangla or English).
     * Automatically identifies Bengali characters and switches TTS engine locale.
     */
    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isTtsInitialized || tts == null) {
            initTts()
        }

        onSpeechDoneCallback = onDone
        _isSpeaking.value = true

        tts?.let { engine ->
            val hasBengali = text.any { it in '\u0980'..'\u09FF' }
            if (hasBengali || _selectedLanguage.value == ConversationLanguage.BANGLA) {
                // Configure Bengali TTS
                val bnLocale = Locale("bn", "BD")
                val res = engine.setLanguage(bnLocale)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val fallbackBn = Locale("bn", "IN")
                    val resIn = engine.setLanguage(fallbackBn)
                    if (resIn == TextToSpeech.LANG_MISSING_DATA || resIn == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.setLanguage(Locale("bn"))
                    }
                }
                engine.setPitch(1.0f)
                engine.setSpeechRate(0.98f)
            } else {
                // Configure British JARVIS English TTS
                val res = engine.setLanguage(Locale.UK)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.US)
                }
                engine.setPitch(0.92f)
                engine.setSpeechRate(1.02f)
            }

            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "JARVIS_SPEECH_${System.currentTimeMillis()}")
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "JARVIS_SPEECH")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        onSpeechDoneCallback = null
    }

    fun startListening(
        language: ConversationLanguage = _selectedLanguage.value,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Voice recognition service not available on this device.")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        _speechRmsLevel.value = (rmsdB / 10f).coerceIn(0f, 1f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _speechRmsLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _speechRmsLevel.value = 0f
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            else -> "Recognition paused."
                        }
                        onError(msg)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _speechRmsLevel.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenText = matches?.firstOrNull() ?: ""
                        if (spokenText.isNotBlank()) {
                            onResult(spokenText)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

                when (language) {
                    ConversationLanguage.BANGLA -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                        putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-IN", "bn"))
                    }
                    ConversationLanguage.ENGLISH -> {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
                        putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-GB", "en"))
                    }
                    ConversationLanguage.AUTO -> {
                        // Support bilingual recognition
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                        putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-US", "en-GB", "bn-IN"))
                    }
                }
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            onError("Microphone initialization error: ${e.message}")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
        _speechRmsLevel.value = 0f
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
        onSpeechDoneCallback = null
    }
}
