#include <jni.h>
#include <string>
#include <android/log.h>
#include "diffusion/diffusion.hpp"

#define TAG "DiffusionJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

using namespace MNN::DIFFUSION;

static Diffusion* sDiffusion = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_example_englishreader_inference_MnnDiffusionJni_load(
    JNIEnv* env, jobject, jstring modelPath, jint backendType) {

    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    LOGI("Loading diffusion model from: %s, backend: %d", path, backendType);

    if (sDiffusion) {
        delete sDiffusion;
        sDiffusion = nullptr;
    }

    sDiffusion = Diffusion::createDiffusion(
        std::string(path),
        STABLE_DIFFUSION_1_5,
        (MNNForwardType)backendType,
        0  // memory_mode=0 (memory saving)
    );
    env->ReleaseStringUTFChars(modelPath, path);

    if (!sDiffusion) {
        LOGE("Failed to create diffusion instance");
        return JNI_FALSE;
    }

    bool ok = sDiffusion->load();
    if (!ok) {
        LOGE("Failed to load diffusion model");
        delete sDiffusion;
        sDiffusion = nullptr;
        return JNI_FALSE;
    }

    LOGI("Diffusion model loaded successfully");
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_com_example_englishreader_inference_MnnDiffusionJni_generate(
    JNIEnv* env, jobject, jstring prompt, jstring outputPath, jint steps, jint seed) {

    if (!sDiffusion) {
        LOGE("Diffusion not loaded");
        return JNI_FALSE;
    }

    const char* promptStr = env->GetStringUTFChars(prompt, nullptr);
    const char* outStr = env->GetStringUTFChars(outputPath, nullptr);

    LOGI("Generating: prompt='%s', output='%s', steps=%d, seed=%d", promptStr, outStr, steps, seed);

    bool ok = sDiffusion->run(
        std::string(promptStr),
        std::string(outStr),
        steps,
        seed,
        [](int progress) {
            LOGI("Progress: %d%%", progress);
        }
    );

    env->ReleaseStringUTFChars(prompt, promptStr);
    env->ReleaseStringUTFChars(outputPath, outStr);

    LOGI("Generation %s", ok ? "succeeded" : "failed");
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_example_englishreader_inference_MnnDiffusionJni_unload(JNIEnv*, jobject) {
    if (sDiffusion) {
        delete sDiffusion;
        sDiffusion = nullptr;
        LOGI("Diffusion unloaded");
    }
}

}
