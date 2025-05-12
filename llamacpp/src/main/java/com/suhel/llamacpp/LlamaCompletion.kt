package com.suhel.llamacpp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class LlamaCompletion(
    private val completionPtr: Long,
    private val maxTokens: Int
) : AutoCloseable {
    companion object {
        private const val TAG = "LlamaCompletion"
    }

    override fun close() {
        LlamaBridge.completionDestroy(completionPtr)
    }

    suspend fun feedPrompt(prompt: String) = withContext(Dispatchers.IO) {
        LlamaBridge.completionFeedPrompt(completionPtr, prompt)
    }

    fun createGenerationFlow(): Flow<PromptGenerateOutput> = callbackFlow {
        send(PromptGenerateOutput(generating = true))

        val result = buildString {
            repeat(maxTokens) {
                if(!isActive) return@buildString

                val token =
                    LlamaBridge.completionGenerateToken(completionPtr) ?: return@buildString

                if (this.isNotBlank() || token.isNotBlank()) {
                    append(token)
                    send(PromptGenerateOutput(text = toString(), generating = true))
                }
            }
        }

        send(PromptGenerateOutput(text = result.trim()))
        this.close()
    }.flowOn(Dispatchers.IO)

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        LlamaBridge.completionClearCache(completionPtr)
    }

    suspend fun resetSampler() = withContext(Dispatchers.IO) {
        LlamaBridge.completionResetSampler(completionPtr)
    }

    data class PromptGenerateOutput(
        val text: String? = null,
        val generating: Boolean = false
    )
}
