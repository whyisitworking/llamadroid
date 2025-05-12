package com.suhel.llamacpp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class LlamaModel(private val modelPtr: Long) : AutoCloseable {
    override fun close() {
        LlamaBridge.modelUnload(modelPtr)
    }

    fun createCompletionFlow(): Flow<LlamaCompletion> = callbackFlow {
        val completion = LlamaCompletion(
            LlamaBridge.completionCreate(modelPtr, DEFAULT_CONTEXT_LEN),
            DEFAULT_CONTEXT_LEN
        )

        send(completion)

        awaitClose {
            completion.close()
        }
    }

    companion object {
        private const val DEFAULT_CONTEXT_LEN = 2048

        fun loadModelFlow(modelPath: String): Flow<LlamaModel> = callbackFlow {
            val model = withContext(Dispatchers.IO) {
                LlamaModel(LlamaBridge.modelLoad(modelPath))
            }

            send(model)

            awaitClose {
                model.close()
            }
        }
    }
}
