#include <android/log.h>
#include <jni.h>
#include <sys/sysconf.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#include "llama.h"
#include "common.h"

#include "mcl-log-ext.h"
#include "mcl-context.h"

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
    LOGv("Model loaded %s", model_path_str);
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
        LOGv("Model freed");
    }
}


extern "C"
JNIEXPORT jlong JNICALL
Java_com_suhel_llamacpp_LlamaBridge_contextCreate(JNIEnv *env __unused,
                                                  jobject thiz __unused,
                                                  jlong model_ptr,
                                                  jint max_tokens) {
    auto model = (llama_model *) model_ptr;
    mcl_context *context;
    auto result = mcl_context_init(&context, model, max_tokens);

    if (result != MCL_OK) {
        LOGi("context init failed %d", result);
    }

    return (jlong) context;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_suhel_llamacpp_LlamaBridge_contextFeedPrompt(JNIEnv *env,
                                                      jobject thiz __unused,
                                                      jlong context_ptr,
                                                      jstring prompt) {
    auto context = (mcl_context *) context_ptr;
    auto prompt_str = env->GetStringUTFChars(prompt, nullptr);
    mcl_context_feed_prompt(context, prompt_str);
    env->ReleaseStringUTFChars(prompt, prompt_str);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_suhel_llamacpp_LlamaBridge_contextGenerateToken(JNIEnv *env,
                                                         jobject thiz  __unused,
                                                         jlong context_ptr) {
    auto context = (mcl_context *) context_ptr;
    auto next_token = mcl_context_generate_next_token(context);

    if (next_token.has_value()) {
        return env->NewStringUTF(next_token->c_str());
    }

    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_suhel_llamacpp_LlamaBridge_contextClearKVCache(JNIEnv *env __unused,
                                                        jobject thiz __unused,
                                                        jlong context_ptr) {
    auto context = (mcl_context *) context_ptr;
    mcl_context_clear_kv_cache(context);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_suhel_llamacpp_LlamaBridge_contextResetSampler(JNIEnv *env __unused,
                                                        jobject thiz __unused,
                                                        jlong context_ptr) {
    auto context = (mcl_context *) context_ptr;
    mcl_context_reset_sampler(context);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_suhel_llamacpp_LlamaBridge_contextDestroy(JNIEnv *env __unused,
                                                   jobject thiz __unused,
                                                   jlong context_ptr) {
    auto context = (mcl_context *) context_ptr;

    if (context) {
        mcl_context_free(context);
        LOGi("context freed");
    }
}
