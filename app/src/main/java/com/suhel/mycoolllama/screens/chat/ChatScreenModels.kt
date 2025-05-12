package com.suhel.mycoolllama.screens.chat

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import java.util.UUID

@Stable
data class ChatScreenState(
    val loadingModel: Boolean = false,
    val generating: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val incomingMessage: String? = null
)

@Immutable
data class ChatMessage(
    val id: UUID = UUID.randomUUID(),
    val text: String,
    val isOutgoing: Boolean
)
