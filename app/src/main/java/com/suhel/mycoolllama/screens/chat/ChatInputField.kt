package com.suhel.mycoolllama.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.suhel.mycoolllama.R

@Composable
fun ChatInputField(
    generating: Boolean,
    onSendClick: (String) -> Unit,
    onStopClick: () -> Unit,
    onClearKVCacheClick: () -> Unit,
    onResetSamplerClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(8.dp)
    ) {
        var prompt by remember { mutableStateOf("") }

        TextField(
            value = prompt,
            enabled = !generating,
            onValueChange = { prompt = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = { Text("Prompt here") },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences
            ),
            colors = TextFieldDefaults.colors(
                // Remove all background colors
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,

                // Remove all indicator lines (the underline)
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),

            shape = RoundedCornerShape(8.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = onClearKVCacheClick,
                enabled = !generating
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clean),
                    contentDescription = "Clear KV cache"
                )
            }
            IconButton(
                onClick = onResetSamplerClick,
                enabled = !generating
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_reset_sampler),
                    contentDescription = "Reset sampler"
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    if (generating) {
                        onStopClick.invoke()
                    } else {
                        val currentPrompt = prompt
                        prompt = ""
                        onSendClick.invoke(currentPrompt)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(if (generating) R.drawable.ic_stop else R.drawable.ic_send),
                    contentDescription = "Send prompt"
                )
            }
        }
    }
}
