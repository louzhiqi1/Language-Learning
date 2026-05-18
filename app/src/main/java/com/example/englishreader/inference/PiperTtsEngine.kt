package com.example.englishreader.inference

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class PiperTtsEngine(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false

    val isLoaded: Boolean get() = ready

    fun load() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(1.0f)
                ready = true
            }
        }
    }

    fun setLanguage(locale: Locale) {
        tts?.language = locale
    }

    fun setSpeed(speed: Float) {
        tts?.setSpeechRate(speed)
    }

    suspend fun speak(text: String): Unit = suspendCancellableCoroutine { cont ->
        if (!ready) {
            cont.resume(Unit)
            return@suspendCancellableCoroutine
        }
        val utteranceId = "tts_${System.currentTimeMillis()}"
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) {
                if (id == utteranceId && cont.isActive) cont.resume(Unit)
            }
            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId && cont.isActive) cont.resume(Unit)
            }
        })
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun speakAsync(text: String) {
        if (!ready) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_async")
    }

    fun stop() {
        tts?.stop()
    }

    fun unload() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
