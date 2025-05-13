package com.suhel.llamacpp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LlamaContext(
    private val contextPtr: Long,
    private val maxTokens: Int
) : AutoCloseable {
    companion object {
        private const val TAG = "LlamaContext"
    }

    suspend fun feedPrompt(prompt: String) = withContext(Dispatchers.IO) {
        LlamaBridge.contextFeedPrompt(contextPtr, prompt)
    }

    suspend fun clearKVCache() = withContext(Dispatchers.IO) {
        LlamaBridge.contextClearKVCache(contextPtr)
    }

    suspend fun resetSampler() = withContext(Dispatchers.IO) {
        LlamaBridge.contextResetSampler(contextPtr)
    }

    fun createGenerationFlow(): Flow<PromptGenerateOutput> = callbackFlow {
        send(PromptGenerateOutput(generating = true))

        val result = buildString {
            repeat(maxTokens) {
                val token = LlamaBridge.contextGenerateToken(contextPtr) ?: return@buildString
                append(token)
                send(PromptGenerateOutput(text = toString(), generating = true))
            }
        }

        send(PromptGenerateOutput(text = result.trim()))
        this.close()
    }.flowOn(Dispatchers.IO)

    override fun close() {
        LlamaBridge.contextDestroy(contextPtr)
    }

    data class PromptGenerateOutput(
        val text: String? = null,
        val generating: Boolean = false
    )
}
