package com.example.englishreader

import android.app.Application
import android.content.Context
import com.example.englishreader.data.db.AppDatabase
import com.example.englishreader.data.db.entity.WordEntity
import com.example.englishreader.domain.model.WordStatus
import com.example.englishreader.inference.PiperTtsEngine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EnglishReaderApp : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var ttsEngine: PiperTtsEngine
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        ttsEngine = PiperTtsEngine(this)
        initializeIfFirstRun()
    }

    private fun initializeIfFirstRun() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("initialized", false)) return

        CoroutineScope(Dispatchers.IO).launch {
            val json = resources.openRawResource(R.raw.initial_vocabulary)
                .bufferedReader().readText()
            val type = object : TypeToken<List<VocabEntry>>() {}.type
            val entries: List<VocabEntry> = Gson().fromJson(json, type)

            val words = entries.map { entry ->
                WordEntity(
                    word = entry.word,
                    meaning = entry.meaning,
                    status = WordStatus.MASTERED
                )
            }
            database.wordDao().insertAll(words)
            prefs.edit().putBoolean("initialized", true).apply()
        }
    }

    private data class VocabEntry(val word: String, val meaning: String)
}
