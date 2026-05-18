package com.example.englishreader.inference

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LlamaInference(private val context: Context) {
    private var modelPtr: Long = 0L
    private var contextPtr: Long = 0L

    companion object {
        private var nativeLoaded = false
        fun ensureNativeLoaded() {
            if (!nativeLoaded) {
                try {
                    System.loadLibrary("omp")
                    System.loadLibrary("ggml-base")
                    System.loadLibrary("ggml")
                    System.loadLibrary("llama")
                    System.loadLibrary("llama-android")
                    nativeLoaded = true
                } catch (_: UnsatisfiedLinkError) { }
            }
        }
    }

    private external fun nativeInit(libDir: String)
    private external fun nativeLoadModel(path: String, nGpuLayers: Int): Long
    private external fun nativeCreateContext(modelPtr: Long, ctxSize: Int): Long
    private external fun nativeGenerate(ctxPtr: Long, prompt: String, maxTokens: Int): String
    private external fun nativeFree(modelPtr: Long, ctxPtr: Long)

    val isLoaded: Boolean get() = modelPtr != 0L

    suspend fun load(modelFileName: String, nGpuLayers: Int = 1) = withContext(Dispatchers.IO) {
        ensureNativeLoaded()
        nativeInit(context.applicationInfo.nativeLibraryDir)
        val appPath = File(context.filesDir, "models/$modelFileName")
        val tmpPath = File("/data/local/tmp/$modelFileName")
        val modelPath = when {
            appPath.exists() -> appPath.absolutePath
            tmpPath.exists() -> tmpPath.absolutePath
            else -> appPath.absolutePath
        }
        modelPtr = nativeLoadModel(modelPath, nGpuLayers)
        contextPtr = nativeCreateContext(modelPtr, 4096)
    }

    suspend fun generate(prompt: String, maxTokens: Int = 1024): String = withContext(Dispatchers.IO) {
        require(isLoaded) { "Model not loaded" }
        nativeGenerate(contextPtr, prompt, maxTokens)
    }

    fun unload() {
        if (modelPtr != 0L) {
            nativeFree(modelPtr, contextPtr)
            modelPtr = 0L
            contextPtr = 0L
        }
    }
}
