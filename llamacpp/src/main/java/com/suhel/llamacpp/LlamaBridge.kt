package com.suhel.llamacpp

object LlamaBridge {
    init {
        System.loadLibrary("llama-adapter")
    }

    external fun libraryLoad()
    external fun libraryUnload()

    external fun modelLoad(modelPath: String): Long
    external fun modelUnload(modelPtr: Long)

    external fun completionCreate(modelPtr: Long, maxTokens: Int): Long
    external fun completionFeedPrompt(completionPtr: Long, prompt: String)
    external fun completionGenerateToken(completionPtr: Long): String?
    external fun completionClearCache(completionPtr: Long)
    external fun completionResetSampler(completionPtr: Long)
    external fun completionDestroy(completionPtr: Long)
}
