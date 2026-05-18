package com.example.englishreader.inference

import android.util.Log

class MnnDiffusionJni {
    companion object {
        init {
            System.loadLibrary("diffusion-android")
        }
    }

    external fun load(modelPath: String, backendType: Int): Boolean
    external fun generate(prompt: String, outputPath: String, steps: Int, seed: Int): Boolean
    external fun unload()
}
