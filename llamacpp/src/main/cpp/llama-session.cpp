//
// Created by Suhel Chakraborty on 11/05/25.
//

#include "llama-session.h"
#include "llama-result.h"

struct llama_session {
    llama_model *model;
    std::string model_path;
};

llama_result llama_session_create(llama_session **self_ptr, const char *model_path) {
    if (!self_ptr) {
        return LLAMA_INVALID_ARG;
    }

    if (*self_ptr) {
        llama_session_free(self_ptr);
    }

    auto model = llama_model_load_from_file(model_path,
                                            llama_model_default_params());

    *self_ptr = new llama_session {
            .model = model,
            .model_path = std::string(model_path),
    };

    return LLAMA_OK;
}

llama_model *llama_session_get_model(llama_session *self) {
    return self->model;
}

void llama_session_free(llama_session **self_ptr) {
    if(self_ptr && *self_ptr) {
        auto self = *self_ptr;

        if(self->model) {
            llama_model_free(self->model);
        }

        delete *self_ptr;
    }
}
