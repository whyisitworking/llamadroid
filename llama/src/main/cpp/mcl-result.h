//
// Created by Suhel Chakraborty on 11/05/25.
//

#ifndef MY_COOL_MCL_MCL_RESULT_H
#define MY_COOL_MCL_MCL_RESULT_H

enum mcl_result {
    MCL_OK = 0,
    MCL_EXISTS = -1,
    MCL_INVALID_ARG = -2,
    MCL_DECODE_ERROR = -3,
    MCL_CTX_CREATE_ERROR = -4,
    MCL_MODEL_LOAD_ERROR = -5,
};

#endif //MY_COOL_MCL_MCL_RESULT_H
