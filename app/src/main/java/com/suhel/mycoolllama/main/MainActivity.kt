package com.suhel.mycoolllama.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.suhel.llamacpp.LlamaBridge
import com.suhel.mycoolllama.ui.theme.MyCoolLlamaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyCoolLlamaTheme {
                MainScreen()
            }
        }

        LlamaBridge.libraryLoad()
    }

    override fun onDestroy() {
        LlamaBridge.libraryUnload()
        super.onDestroy()
    }
}
