#include <android/log.h>
#include <jni.h>
#include <sys/sysconf.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#include "llama.h"
#include "common.h"
#include "log-ext.h"

#include "llama-completion.h"

extern "C"
JNIEXPORT void JNICALL
Java_com_suhel_llamacpp_LlamaBridge_libraryLoad(JNIEnv *env __unused,
                                                jobject thiz __unused) {
    llama_backend_init();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_suhel_llamacpp_LlamaBridge_libraryUnload(JNIEnv *env __unused,
                                                  jobject thiz __unused) {
    llama_backend_free();
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_suhel_llamacpp_LlamaBridge_modelLoad(JNIEnv *env,
                                              jobject thiz __unused,
                                              jstring model_path) {
    auto model_path_str = env->GetStringUTFChars(model_path, nullptr);
    auto model = llama_model_load_from_file(model_path_str, llama_model_default_params());
    LOGi("Model loaded %s", model_path_str);
    env->ReleaseStringUTFChars(model_path, model_path_str);

    return (jlong) model;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_suhel_llamacpp_LlamaBridge_modelUnload(JNIEnv *env __unused,
                                                jobject thiz __unused,
                                                jlong model_ptr) {
    auto model = (llama_model *) model_ptr;

    if (model) {
        llama_model_free(model);
        LOGi("Model freed");
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_suhel_llamacpp_LlamaBridge_completionCreate(JNIEnv *env __unused,
                                                     jobject thiz __unused,
                                                     jlong model_ptr,
                                                     jint max_tokens) {
    auto model = (llama_model *) model_ptr;
    llama_completion *completion;
    auto result = llama_completion_init(&completion, model, max_tokens);

    if (result != LLAMA_OK) {
        LOGi("Completion init failed %d", result);
    }

    return (jlong) completion;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_suhel_llamacpp_LlamaBridge_completionFeedPrompt(JNIEnv *env,
                                                         jobject thiz __unused,
                                                         jlong completion_ptr,
                                                         jstring prompt) {
    auto completion = (llama_completion *) completion_ptr;
    auto prompt_str = env->GetStringUTFChars(prompt, nullptr);
    llama_completion_feed_prompt(completion, prompt_str);
    env->ReleaseStringUTFChars(prompt, prompt_str);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_suhel_llamacpp_LlamaBridge_completionGenerateToken(JNIEnv *env,
                                                            jobject thiz  __unused,
                                                            jlong completion_ptr) {
    auto completion = (llama_completion *) completion_ptr;
    auto next_token = llama_completion_generate_next_token(completion);

    if (next_token.has_value()) {
        return env->NewStringUTF(next_token->c_str());
    }

    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_suhel_llamacpp_LlamaBridge_completionClearCache(JNIEnv *env __unused,
                                                         jobject thiz __unused,
                                                         jlong completion_ptr) {
    auto completion = (llama_completion *) completion_ptr;
    llama_completion_clear_kv_cache(completion);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_suhel_llamacpp_LlamaBridge_completionResetSampler(JNIEnv *env __unused,
                                                           jobject thiz __unused,
                                                           jlong completion_ptr) {
    auto completion = (llama_completion *) completion_ptr;
    llama_completion_reset_sampler(completion);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_suhel_llamacpp_LlamaBridge_completionDestroy(JNIEnv *env __unused,
                                                      jobject thiz __unused,
                                                      jlong completion_ptr) {
    auto completion = (llama_completion *) completion_ptr;

    if (completion) {
        llama_completion_free(completion);
        LOGi("Completion freed");
    }
}
