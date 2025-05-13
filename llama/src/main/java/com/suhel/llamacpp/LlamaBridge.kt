package com.suhel.llamacpp

object LlamaBridge {
    init {
        System.loadLibrary("mcl-jni")
    }

    external fun libraryLoad()
    external fun libraryUnload()

    external fun modelLoad(modelPath: String): Long
    external fun modelUnload(modelPtr: Long)

    external fun contextCreate(modelPtr: Long, maxTokens: Int): Long
    external fun contextFeedPrompt(completionPtr: Long, prompt: String)
    external fun contextGenerateToken(completionPtr: Long): String?
    external fun contextClearKVCache(completionPtr: Long)
    external fun contextResetSampler(completionPtr: Long)
    external fun contextDestroy(completionPtr: Long)
}
