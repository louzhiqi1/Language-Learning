package com.example.englishreader.inference

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MnnImageGenerator(private val context: Context) {

    private val jni = MnnDiffusionJni()
    private var loaded = false

    fun getModelDir(): File = File(context.filesDir, "models/sd-mnn")

    fun isModelAvailable(): Boolean {
        val dir = getModelDir()
        return dir.exists() && File(dir, "unet.mnn").exists() && File(dir, "tokenizer.mtok").exists()
    }

    suspend fun generate(prompt: String, outputPath: String, steps: Int = 5): Boolean =
        withContext(Dispatchers.IO) {
            if (!isModelAvailable()) return@withContext false

            try {
                if (!loaded) {
                    val modelDir = getModelDir().absolutePath
                    Log.i("MnnImage", "Loading model from: $modelDir")
                    loaded = jni.load(modelDir, 0) // 0 = MNN_FORWARD_CPU
                    if (!loaded) {
                        Log.e("MnnImage", "Failed to load diffusion model")
                        return@withContext false
                    }
                }

                val styledPrompt = "children's book illustration, cartoon style, colorful, simple, cute, $prompt"
                val seed = (1..9999).random()
                Log.i("MnnImage", "Generating: steps=$steps, seed=$seed")
                val success = jni.generate(styledPrompt, outputPath, steps, seed)
                Log.i("MnnImage", "Result: $success, exists=${File(outputPath).exists()}")
                success
            } catch (e: Exception) {
                Log.e("MnnImage", "Error: ${e.message}", e)
                false
            }
        }
}
