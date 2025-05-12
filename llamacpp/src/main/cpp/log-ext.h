//
// Created by Suhel Chakraborty on 12/05/25.
//

#ifndef MY_COOL_LLAMA_LOG_EXT_H
#define MY_COOL_LLAMA_LOG_EXT_H

#define TAG "MCLNative"
#define LOGi(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGe(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#endif //MY_COOL_LLAMA_LOG_EXT_H
