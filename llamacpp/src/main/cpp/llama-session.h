//
// Created by Suhel Chakraborty on 11/05/25.
//

#ifndef MY_COOL_LLAMA_LLAMA_SESSION_H
#define MY_COOL_LLAMA_LLAMA_SESSION_H

#include <string>
#include "llama-result.h"
#include "llama-completion.h"

struct llama_session;

llama_result llama_session_create(llama_session **self_ptr, const char *model_path);
llama_model *llama_session_get_model(llama_session *self);
void llama_session_free(llama_session **self_ptr);

#endif //MY_COOL_LLAMA_LLAMA_SESSION_H
