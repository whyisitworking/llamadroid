@file:OptIn(ExperimentalCoroutinesApi::class)

package com.suhel.mycoolllama.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suhel.llamacpp.LlamaModel
import com.suhel.mycoolllama.data.GlobalState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChatScreenViewModel @Inject constructor() : ViewModel() {
    companion object {
        private const val TAG = "ChatScreenViewModel"
    }

    private val generateCompletionTrigger = MutableSharedFlow<String?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val completion = GlobalState.currentModel
        .filterNotNull()
        .flatMapLatest { LlamaModel.loadModelFlow(it.filePath) }
        .flatMapLatest { it.createCompletionFlow() }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = null
        )

    val state = combine(
        completion.onStart { emit(null) },
        generateCompletionTrigger.onStart { emit(null) }
    ) { completion, prompt -> completion to prompt }
        .distinctUntilChanged()
        .flatMapLatest { (completion, prompt) ->
            when {
                completion == null -> flowOf(ChatCompletionEvent(loadingModel = true))

                prompt != null -> flow {
                    emit(
                        ChatCompletionEvent(
                            generating = true,
                            newMessage = ChatScreenState.Message(
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
                                newMessage = output.text?.takeIf { !output.generating }?.let {
                                    ChatScreenState.Message(
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
        }
        .distinctUntilChanged()
        .scan(ChatScreenState()) { prevState, event ->
            prevState.copy(
                loadingModel = event.loadingModel,
                generating = event.generating,
                messages = event.newMessage?.let { prevState.messages + it } ?: prevState.messages,
                incomingMessage = event.generatedOutput
            )
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = ChatScreenState(loadingModel = true)
        )

    fun addPrompt(prompt: String) {
        generateCompletionTrigger.tryEmit(prompt)
    }
}
