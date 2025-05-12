package com.suhel.mycoolllama.data

import com.suhel.mycoolllama.data.ModelsRepository.Model
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

object GlobalState {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val loadModelTrigger = MutableSharedFlow<Model>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val currentModel = loadModelTrigger.stateIn(
        coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    fun loadModel(model: Model) {
        loadModelTrigger.tryEmit(model)
    }
}