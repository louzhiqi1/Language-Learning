#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "llama.h"
#include "ggml-backend.h"

#define TAG "LlamaJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

static llama_sampler * create_sampler() {
    auto * smpl = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(smpl, llama_sampler_init_penalties(64, 1.1f, 0.0f, 0.0f));
    llama_sampler_chain_add(smpl, llama_sampler_init_temp(0.8f));
    llama_sampler_chain_add(smpl, llama_sampler_init_top_p(0.95f, 1));
    llama_sampler_chain_add(smpl, llama_sampler_init_dist(0));
    return smpl;
}

static void batch_set_token(struct llama_batch & batch, int32_t idx, llama_token token, llama_pos pos, bool logits) {
    batch.token[idx] = token;
    batch.pos[idx] = pos;
    batch.n_seq_id[idx] = 1;
    batch.seq_id[idx][0] = 0;
    batch.logits[idx] = logits ? 1 : 0;
    batch.n_tokens = idx + 1;
}

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_englishreader_inference_LlamaInference_nativeInit(
    JNIEnv *env, jobject, jstring libDir) {

    const char *dir = env->GetStringUTFChars(libDir, nullptr);
    LOGI("Loading backends from: %s", dir);

    std::string base(dir);
    env->ReleaseStringUTFChars(libDir, dir);

    std::string cpu_path = base + "/libggml-cpu.so";

    auto cpu = ggml_backend_load(cpu_path.c_str());
    LOGI("CPU backend: %s", cpu ? "loaded" : "failed");
}

JNIEXPORT jlong JNICALL
Java_com_example_englishreader_inference_LlamaInference_nativeLoadModel(
    JNIEnv *env, jobject, jstring path, jint nGpuLayers) {

    const char *model_path = env->GetStringUTFChars(path, nullptr);
    auto params = llama_model_default_params();
    params.n_gpu_layers = nGpuLayers;
    LOGI("Loading model: %s", model_path);
    auto *model = llama_model_load_from_file(model_path, params);
    env->ReleaseStringUTFChars(path, model_path);
    if (!model) {
        LOGI("Failed to load model");
        return 0;
    }
    LOGI("Model loaded successfully");
    return reinterpret_cast<jlong>(model);
}

JNIEXPORT jlong JNICALL
Java_com_example_englishreader_inference_LlamaInference_nativeCreateContext(
    JNIEnv *, jobject, jlong modelPtr, jint ctxSize) {

    auto *model = reinterpret_cast<llama_model *>(modelPtr);
    auto params = llama_context_default_params();
    params.n_ctx = ctxSize;
    params.n_batch = 512;
    params.n_threads = 6;
    params.n_threads_batch = 6;
    auto *ctx = llama_init_from_model(model, params);
    return reinterpret_cast<jlong>(ctx);
}

JNIEXPORT jstring JNICALL
Java_com_example_englishreader_inference_LlamaInference_nativeGenerate(
    JNIEnv *env, jobject, jlong ctxPtr, jstring prompt, jint maxTokens) {

    auto *ctx = reinterpret_cast<llama_context *>(ctxPtr);
    const llama_model *model = llama_get_model(ctx);
    const llama_vocab *vocab = llama_model_get_vocab(model);
    const char *prompt_str = env->GetStringUTFChars(prompt, nullptr);
    int prompt_len = strlen(prompt_str);

    // Tokenize
    int n_prompt_max = prompt_len + 128;
    std::vector<llama_token> tokens(n_prompt_max);
    int n_tokens = llama_tokenize(vocab, prompt_str, prompt_len,
                                   tokens.data(), n_prompt_max, true, true);
    env->ReleaseStringUTFChars(prompt, prompt_str);

    if (n_tokens < 0) {
        LOGI("Tokenization failed");
        return env->NewStringUTF("");
    }
    tokens.resize(n_tokens);
    LOGI("Prompt tokens: %d", n_tokens);

    // Clear KV cache
    llama_memory_clear(llama_get_memory(ctx), true);

    // Decode prompt using llama_batch_get_one for simplicity
    int batch_size = 512;
    for (int i = 0; i < n_tokens; i += batch_size) {
        int n_eval = std::min(batch_size, n_tokens - i);
        struct llama_batch batch = llama_batch_get_one(tokens.data() + i, n_eval);
        if (llama_decode(ctx, batch) != 0) {
            LOGI("Decode failed at token %d", i);
            return env->NewStringUTF("");
        }
    }

    // Generate
    std::string result;
    auto *sampler = create_sampler();

    struct llama_batch batch = llama_batch_init(1, 0, 1);

    // Find <|im_end|> token for stop condition
    llama_token im_end_token = -1;
    {
        const char *im_end_str = "<|im_end|>";
        llama_token tmp[8];
        int n = llama_tokenize(vocab, im_end_str, strlen(im_end_str), tmp, 8, false, true);
        if (n == 1) im_end_token = tmp[0];
    }

    for (int i = 0; i < maxTokens; i++) {
        llama_token new_token = llama_sampler_sample(sampler, ctx, -1);

        if (llama_vocab_is_eog(vocab, new_token)) {
            LOGI("EOS at step %d", i);
            break;
        }
        if (new_token == im_end_token) {
            LOGI("im_end at step %d", i);
            break;
        }

        char buf[256];
        int n = llama_token_to_piece(vocab, new_token, buf, sizeof(buf), 0, true);
        if (n > 0) result.append(buf, n);

        if (i > 0 && i % 100 == 0) {
            LOGI("Generated %d tokens so far...", i);
        }

        // Prepare next token
        batch_set_token(batch, 0, new_token, n_tokens + i, true);
        if (llama_decode(ctx, batch) != 0) {
            LOGI("Decode failed during generation at step %d", i);
            break;
        }
    }

    llama_sampler_free(sampler);
    llama_batch_free(batch);

    LOGI("Generated %d chars", (int)result.size());
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT void JNICALL
Java_com_example_englishreader_inference_LlamaInference_nativeFree(
    JNIEnv *, jobject, jlong modelPtr, jlong ctxPtr) {

    auto *ctx = reinterpret_cast<llama_context *>(ctxPtr);
    auto *model = reinterpret_cast<llama_model *>(modelPtr);
    if (ctx) llama_free(ctx);
    if (model) llama_model_free(model);
}

} // extern "C"
