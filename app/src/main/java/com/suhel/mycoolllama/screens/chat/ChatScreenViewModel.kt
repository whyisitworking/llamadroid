@file:OptIn(ExperimentalCoroutinesApi::class)

package com.suhel.mycoolllama.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suhel.llamacpp.LlamaContext
import com.suhel.llamacpp.LlamaModel
import com.suhel.mycoolllama.data.RouterParams
import com.suhel.mycoolllama.extensions.cacheIn
import com.suhel.mycoolllama.extensions.emitNullOnStart
import com.suhel.mycoolllama.extensions.trigger
import com.suhel.mycoolllama.extensions.triggerFlow
import com.suhel.mycoolllama.extensions.unitTriggerFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatScreenViewModel @Inject constructor() : ViewModel() {
    companion object {
        private const val TAG = "ChatScreenViewModel"
    }

    private val generateCompletionTrigger = triggerFlow<String>()
    private val clearKVCacheTrigger = unitTriggerFlow()
    private val resetSamplerTrigger = unitTriggerFlow()

    private val completion = RouterParams.currentModel
        .filterNotNull()
        .flatMapLatest { LlamaModel.load(it.filePath) }
        .flatMapLatest { model -> model.newContext() }
        .cacheIn(viewModelScope)

    init {
        completion
            .filterNotNull()
            .combine(clearKVCacheTrigger) { completion, _ ->
                withContext(Dispatchers.IO) {
                    completion.clearKVCache()
                }
            }
            .launchIn(viewModelScope)

        completion
            .filterNotNull()
            .combine(resetSamplerTrigger) { completion, _ ->
                withContext(Dispatchers.IO) {
                    completion.resetSampler()
                }
            }
            .launchIn(viewModelScope)
    }

    val state = combine(
        completion,
        generateCompletionTrigger.emitNullOnStart()
    ) { completion, prompt -> completion to prompt }
        .distinctUntilChanged()
        .flatMapLatest { (completion, prompt) -> chatCompletionFlow(completion, prompt) }
        .distinctUntilChanged()
        .scan(ChatScreenState()) { prevState, event ->
            prevState.copy(
                loadingModel = event.loadingModel,
                generating = event.generating,
                messages = event.newMessage?.let { prevState.messages + it } ?: prevState.messages,
                incomingMessage = event.generatedOutput
            )
        }
        .cacheIn(viewModelScope, ChatScreenState(loadingModel = true))

    data class ChatCompletionEvent(
        val loadingModel: Boolean = false,
        val generating: Boolean = false,
        val newMessage: ChatMessage? = null,
        val generatedOutput: String? = null
    )

    private fun chatCompletionFlow(
        completion: LlamaContext?,
        prompt: String?
    ): Flow<ChatCompletionEvent> = when {
        completion == null -> flowOf(
            ChatCompletionEvent(loadingModel = true)
        )

        prompt != null -> flow {
            emit(
                ChatCompletionEvent(
                    generating = true,
                    newMessage = ChatMessage(
                        text = prompt,
                        isOutgoing = true
                    )
                )
            )
            completion.feedPrompt(prompt)
            completion.createGenerationFlow().collect { output ->
                emit(
                    ChatCompletionEvent(
                        generating = output.generating,
                        generatedOutput = output.text.takeIf { output.generating },
                        newMessage = output.text.takeIf { !output.generating }?.let {
                            ChatMessage(
                                text = it,
                                isOutgoing = false
                            )
                        }
                    )
                )
            }
        }

        else -> flowOf(ChatCompletionEvent())
    }

    fun generate(prompt: String) {
        generateCompletionTrigger.trigger(prompt)
    }

    fun clearKVCache() {
        clearKVCacheTrigger.trigger()
    }

    fun resetSampler() {
        resetSamplerTrigger.trigger()
    }
}
