//
// Created by Suhel Chakraborty on 11/05/25.
//

#ifndef MY_COOL_LLAMA_LOG_EXT_H
#define MY_COOL_LLAMA_LOG_EXT_H

#include <android/log.h>

#define TAG "mycoolllama"
#define LOGi(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGv(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGe(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#endif //MY_COOL_LLAMA_LOG_EXT_H
