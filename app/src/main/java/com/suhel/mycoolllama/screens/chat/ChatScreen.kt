package com.suhel.mycoolllama.screens.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.suhel.mycoolllama.ui.theme.ScreenScaffold

@Composable
fun ChatScreen(
    onNavBack: () -> Unit,
    viewModel: ChatScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    ScreenScaffold(
        title = state.modelName ?: "Chat",
        onNavBack = onNavBack
    ) {
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

            ChatInputField(
                generating = state.generating,
                onSendClick = {
                    viewModel.generate(it)
                },
                onStopClick = {
                    viewModel.stopGeneration()
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
