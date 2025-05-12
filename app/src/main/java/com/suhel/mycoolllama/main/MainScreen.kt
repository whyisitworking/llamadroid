package com.suhel.mycoolllama.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.suhel.mycoolllama.screens.chat.ChatScreen
import com.suhel.mycoolllama.screens.models.ModelsScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navGraph = remember(navController) {
        navController.createGraph(startDestination = "models") {
            composable("models") {
                ModelsScreen(
                    onNavChat = {
                        navController.navigate("chat")
                    }
                )
            }
            composable("chat") {
                ChatScreen(
                    onNavBack = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }

    NavHost(
        navController = navController,
        graph = navGraph
    )
}
