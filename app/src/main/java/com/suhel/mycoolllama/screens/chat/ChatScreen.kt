package com.suhel.mycoolllama.screens.chat

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.colintheshots.twain.MarkdownText
import com.suhel.mycoolllama.R
import com.suhel.mycoolllama.ui.theme.ScreenScaffold
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    onNavBack: () -> Unit,
    viewModel: ChatScreenViewModel = hiltViewModel()
) {
    ScreenScaffold(
        title = "Chat",
        onNavBack = onNavBack
    ) {
        val state by viewModel.state.collectAsState()
        if (state.loadingModel) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (state.messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No conversation yet")
                }
            } else {
                ChatMessages(state.messages, state.incomingMessage, modifier = Modifier.weight(1f))
            }

            if (!state.generating) {
                ChatTextField(
                    onSendClick = {
                        viewModel.generate(it)
                    },
                    onClearKVCacheClick = {
                        viewModel.clearKVCache()
                    },
                    onResetSamplerClick = {
                        viewModel.resetSampler()
                    }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.ChatMessages(
    messages: List<ChatMessage>,
    incomingMessage: String?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(messages, key = { message -> message.id }) { message ->
            ChatMessage(message)
        }

        incomingMessage?.let { incoming ->
            item(key = "incoming") {
                ChatMessageIncoming(message = incoming, generating = true)
            }
        }
    }
}

@Composable
private fun ChatTextField(
    onSendClick: (String) -> Unit,
    onClearKVCacheClick: () -> Unit,
    onResetSamplerClick: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onClearKVCacheClick) {
                Text("Clear KV cache")
            }
            Button(onClick = onResetSamplerClick) {
                Text("Reset sampler")
            }
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = prompt,
            shape = RoundedCornerShape(50),
            onValueChange = { prompt = it },
            placeholder = { Text("Enter a prompt") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSendClick.invoke(prompt)
                    prompt = ""
                }
            )
        )
    }
}

@Composable
private fun ChatMessage(message: ChatMessage) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (message.isOutgoing) {
            ChatMessageOutgoing(message.text)
        } else {
            ChatMessageIncoming(message.text)
        }
    }
}

@Composable
private fun ColumnScope.ChatMessageIncoming(message: String, generating: Boolean = false) {
    Column(
        modifier = Modifier
            .align(Alignment.Start)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        MarkdownText(
            markdown = message,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )

        if (generating) {
            Box(modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            val clipboard = LocalClipboard.current
            val coroutineScope = rememberCoroutineScope()

            IconButton(
                onClick = {
                    coroutineScope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(ClipData.newPlainText("Prompt output", message))
                        )
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_content_copy),
                    contentDescription = "Copy"
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.ChatMessageOutgoing(message: String) {
    Column(
        modifier = Modifier
            .align(Alignment.End)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp)
        )
    }
}
