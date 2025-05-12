//
// Created by Suhel Chakraborty on 11/05/25.
//

#ifndef MY_COOL_LLAMA_LLAMA_COMPLETION_H
#define MY_COOL_LLAMA_LLAMA_COMPLETION_H

#include <stdexcept>
#include <unistd.h>
#include <string>

#include "llama.h"
#include "llama-result.h"
#include "ext/llama-cpp-ext.h"
#include "ext/log-ext.h"

#include "common.h"

struct llama_completion;

llama_result llama_completion_init(llama_completion **completion_ptr,
                                   llama_model *model,
                                   int max_tokens);

llama_result llama_completion_feed_prompt(llama_completion *self, const char *prompt);

std::optional<std::string> llama_completion_generate_next_token(llama_completion *self);

void llama_completion_clear_kv_cache(llama_completion *completion);

void llama_completion_reset_sampler(llama_completion *completion);

void llama_completion_free(llama_completion *completion_ptr);

#endif //MY_COOL_LLAMA_LLAMA_COMPLETION_H
