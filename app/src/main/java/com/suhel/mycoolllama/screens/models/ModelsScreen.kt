package com.suhel.mycoolllama.screens.models

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.suhel.mycoolllama.data.RouterParams
import com.suhel.mycoolllama.data.ModelsRepository.Model
import com.suhel.mycoolllama.ui.theme.ScreenScaffold
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsScreen(
    onNavChat: () -> Unit,
    viewModel: ModelsViewModel = hiltViewModel()
) {
    ScreenScaffold("Models") {
        val state by viewModel.state.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val openFileChooser = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            if (uri != null) viewModel.addModel(uri)
        }

        if (state.importStatus.busy) {
            ModelLoadingSubScreen { state.importStatus.progress }
        } else {
            ExistingModelsSubScreen(
                models = state.availableModels,
                modifier = Modifier.weight(1f),
                onClick = { model ->
                    RouterParams.loadModel(model)
                    onNavChat()
                },
                onDeleteClick = { model ->
                    viewModel.deleteModel(model)
                },
            )

            Button(
                content = {
                    Text("Import Model")
                },
                onClick = {
                    coroutineScope.launch {
                        openFileChooser.launch("*/*")
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp, bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun ModelLoadingSubScreen(progress: () -> Float) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text("Loading model")
        CircularProgressIndicator(progress)
    }
}

@Composable
private fun ExistingModelsSubScreen(
    models: List<Model>,
    modifier: Modifier = Modifier,
    onClick: (Model) -> Unit,
    onDeleteClick: (Model) -> Unit
) {
    Box(modifier = modifier.fillMaxWidth()) {
        if (models.isEmpty()) {
            Text(
                text = "No models available",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    models,
                    key = { model -> model.name }
                ) { model ->
                    ModelItem(model, onClick, onDeleteClick)
                }
            }
        }
    }
}

@Composable
private fun ModelItem(
    model: Model,
    onClick: (Model) -> Unit,
    onDeleteClick: (Model) -> Unit
) {
    Card(onClick = { onClick(model) }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = model.name,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.formattedSize,
                        fontSize = 14.sp
                    )
                    Text(
                        text = model.formattedDateImported,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onDeleteClick(model) }) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                }
            }
        }
    }
}
