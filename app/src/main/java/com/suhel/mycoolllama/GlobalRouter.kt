package com.suhel.mycoolllama

import com.suhel.mycoolllama.screens.models.ModelsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

object GlobalRouter {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val loadModelTrigger = MutableSharedFlow<ModelsRepository.Model>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val currentModel = loadModelTrigger.stateIn(
        coroutineScope,
        started = SharingStarted.Companion.Eagerly,
        initialValue = null
    )

    fun loadModel(model: ModelsRepository.Model) {
        loadModelTrigger.tryEmit(model)
    }
}