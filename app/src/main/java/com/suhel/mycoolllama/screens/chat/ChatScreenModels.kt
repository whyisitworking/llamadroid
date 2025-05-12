package com.suhel.mycoolllama.screens.chat

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.suhel.mycoolllama.screens.chat.ChatScreenState.Message
import java.util.UUID

@Stable
data class ChatScreenState(
    val loadingModel: Boolean = false,
    val generating: Boolean = false,
    val messages: List<Message> = emptyList(),
    val incomingMessage: String? = null
) {
    @Immutable
    data class Message(
        val id: UUID = UUID.randomUUID(),
        val text: String,
        val isOutgoing: Boolean
    )
}

@Stable
data class ChatCompletionEvent(
    val loadingModel: Boolean = false,
    val generating: Boolean = false,
    val newMessage: Message? = null,
    val generatedOutput: String? = null
)
