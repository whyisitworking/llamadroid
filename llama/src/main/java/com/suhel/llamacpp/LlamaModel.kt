package com.suhel.llamacpp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class LlamaModel(private val modelPtr: Long) : AutoCloseable {
    fun newContext(maxTokenCount: Int = DEFAULT_CONTEXT_LEN): Flow<LlamaContext> = flow {
        val contextPtr = withContext(Dispatchers.IO) {
            LlamaBridge.contextCreate(modelPtr, maxTokenCount)
        }
        val context = LlamaContext(contextPtr, maxTokenCount)

        emit(context)
        awaitCancellation()

        withContext(Dispatchers.IO) {
            context.close()
        }
    }

    override fun close() {
        LlamaBridge.modelUnload(modelPtr)
    }

    companion object {
        private const val DEFAULT_CONTEXT_LEN = 2048

        fun load(modelPath: String): Flow<LlamaModel> = flow {
            val model = withContext(Dispatchers.IO) {
                LlamaModel(LlamaBridge.modelLoad(modelPath))
            }

            emit(model)
            awaitCancellation()

            withContext(Dispatchers.IO) {
                model.close()
            }
        }
    }
}
