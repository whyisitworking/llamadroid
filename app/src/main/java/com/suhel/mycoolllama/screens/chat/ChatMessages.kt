package com.suhel.mycoolllama.screens.chat

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.colintheshots.twain.MarkdownText
import com.suhel.mycoolllama.R
import kotlinx.coroutines.launch

@Composable
fun ColumnScope.ChatMessages(
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
