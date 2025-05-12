//
// Created by Suhel Chakraborty on 11/05/25.
//

#ifndef MY_COOL_LLAMA_LLAMA_CPP_EXT_H
#define MY_COOL_LLAMA_LLAMA_CPP_EXT_H

#include <memory>
#include "llama.h"

struct llama_batch_deleter {
    void operator()(llama_batch *adapter) { llama_batch_free(*adapter); }
};

typedef std::unique_ptr<llama_batch, llama_batch_deleter> llama_batch_ptr;

#endif //MY_COOL_LLAMA_LLAMA_CPP_EXT_H
