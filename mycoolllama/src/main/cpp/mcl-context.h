//
// Created by Suhel Chakraborty on 11/05/25.
//

#ifndef MY_COOL_mcl_mcl_context_H
#define MY_COOL_mcl_mcl_context_H

#include <string>

#include "llama.h"
#include "common.h"

#include "mcl-result.h"

struct mcl_context;

mcl_result mcl_context_init(mcl_context **context_ptr,
                            llama_model *model,
                            int max_tokens);

mcl_result mcl_context_feed_prompt(mcl_context *self, const char *prompt);

std::optional<std::string> mcl_context_generate_next_token(mcl_context *self);

void mcl_context_clear_kv_cache(mcl_context *context);

void mcl_context_reset_sampler(mcl_context *context);

void mcl_context_free(mcl_context *context_ptr);

#endif //MY_COOL_mcl_mcl_context_H
