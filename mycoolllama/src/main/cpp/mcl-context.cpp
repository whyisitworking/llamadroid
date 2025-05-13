//
// Created by Suhel Chakraborty on 11/05/25.
//

#include "mcl-context.h"

#include <unistd.h>

#include "llama.h"
#include "common.h"

#include "mcl-result.h"
#include "mcl-log-ext.h"

struct mcl_context {
    llama_model *model;
    llama_context *ctx;
    llama_batch batch;
    llama_sampler *sampler;
    int max_tokens;
    int n_threads;
    int token_pos;
    std::string utf_cache;
};

static bool is_valid_utf8(const char *string) {
    if (!string) {
        return true;
    }

    const auto *bytes = (const unsigned char *) string;
    int num;

    while (*bytes != 0x00) {
        if ((*bytes & 0x80) == 0x00) {
            // U+0000 to U+007F
            num = 1;
        } else if ((*bytes & 0xE0) == 0xC0) {
            // U+0080 to U+07FF
            num = 2;
        } else if ((*bytes & 0xF0) == 0xE0) {
            // U+0800 to U+FFFF
            num = 3;
        } else if ((*bytes & 0xF8) == 0xF0) {
            // U+10000 to U+10FFFF
            num = 4;
        } else {
            return false;
        }

        bytes += 1;
        for (int i = 1; i < num; ++i) {
            if ((*bytes & 0xC0) != 0x80) {
                return false;
            }
            bytes += 1;
        }
    }

    return true;
}

mcl_result mcl_context_init(mcl_context **self_ptr,
                            llama_model *model,
                            int max_tokens) {
    if (!self_ptr) {
        return MCL_INVALID_ARG;
    }

    int n_threads = (int) sysconf(_SC_NPROCESSORS_ONLN);

    auto ctx_params = llama_context_default_params();
    ctx_params.n_ctx = (uint32_t) max_tokens;
    ctx_params.n_threads = n_threads;
    ctx_params.n_threads_batch = n_threads;
    auto ctx = llama_init_from_model(model, ctx_params);

    if (!ctx) {
        return MCL_CTX_CREATE_ERROR;
    }

    *self_ptr = new mcl_context{
            .model = model,
            .ctx = ctx,
            .batch = llama_batch_init(1, 0, 1),
            .sampler = llama_sampler_init_greedy(),
            .max_tokens = max_tokens,
            .n_threads = n_threads
    };

    return MCL_OK;
}

mcl_result mcl_context_feed_prompt(mcl_context *self, const char *prompt) {
    auto tokens = common_tokenize(self->ctx, prompt, true, true);
    auto batch = llama_batch_init((int32_t) tokens.size(), 0, 1);

    for (size_t i = 0; i < tokens.size(); i++) {
        common_batch_add(batch, tokens[i], (llama_pos) i, {0}, false);
    }
    auto token_count = batch.n_tokens;
    batch.logits[token_count - 1] = true;

    auto decode_result = llama_decode(self->ctx, batch);
    llama_batch_free(batch);

    if (decode_result != 0) {
        return MCL_DECODE_ERROR;
    }

    self->token_pos = token_count;
    self->utf_cache.clear();

    return MCL_OK;
}

std::optional<std::string> mcl_context_generate_next_token(mcl_context *self) {
    llama_token new_token = llama_sampler_sample(self->sampler, self->ctx, -1);
    if (llama_vocab_is_eog(llama_model_get_vocab(self->model), new_token)) {
        return std::nullopt;
    }

    common_batch_clear(self->batch);
    common_batch_add(self->batch, new_token, self->token_pos, {0}, true);

    self->token_pos++;

    auto decode_result = llama_decode(self->ctx, self->batch);
    if (decode_result != 0) {
        return std::nullopt;
    }

    std::string token_chars = common_token_to_piece(self->ctx, new_token, true);
    self->utf_cache += token_chars;

    if (is_valid_utf8(self->utf_cache.c_str())) {
        auto valid_token_str = self->utf_cache;
        self->utf_cache.clear();
        return valid_token_str;
    } else {
        return "";
    }
}

void mcl_context_clear_kv_cache(mcl_context *self) {
    if (self && self->ctx) {
        llama_kv_self_clear(self->ctx);
    }
}

void mcl_context_reset_sampler(mcl_context *self) {
    if (self && self->sampler) {
        llama_sampler_reset(self->sampler);
    }
}

void mcl_context_free(mcl_context *self) {
    if (!self) {
        return;
    }

    if (self->ctx) {
        llama_free(self->ctx);
        LOGi("Context freed");
    }

    if (self->sampler) {
        llama_sampler_free(self->sampler);
        LOGi("Sampler freed");
    }

    llama_batch_free(self->batch);
    LOGi("Batch freed");
}
