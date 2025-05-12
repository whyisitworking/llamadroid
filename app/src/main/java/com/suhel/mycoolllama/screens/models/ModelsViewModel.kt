package com.suhel.mycoolllama.screens.models

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suhel.mycoolllama.data.ModelsRepository
import com.suhel.mycoolllama.features.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ModelsViewModel @Inject constructor(
    private val router: Router,
    private val modelsRepository: ModelsRepository
) : ViewModel() {
    val state = modelsRepository.state
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(10_000),
            initialValue = ModelsRepository.State()
        )

    fun addModel(uri: Uri) {
        modelsRepository.addModel(uri)
    }

    fun deleteModel(model: ModelsRepository.Model) {
        modelsRepository.deleteModel(model)
    }
}
