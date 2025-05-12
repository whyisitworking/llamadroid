//
// Created by Suhel Chakraborty on 11/05/25.
//

#ifndef MY_COOL_LLAMA_LLAMA_RESULT_H
#define MY_COOL_LLAMA_LLAMA_RESULT_H

enum llama_result {
    LLAMA_OK = 0,
    LLAMA_EXISTS = -1,
    LLAMA_INVALID_ARG = -2,
    LLAMA_DECODE_ERROR = -3,
    LLAMA_CTX_CREATE_ERROR = -4,
    LLAMA_MODEL_LOAD_ERROR = -5,
};

#endif //MY_COOL_LLAMA_LLAMA_RESULT_H
