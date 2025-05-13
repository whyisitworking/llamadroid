@file:OptIn(ExperimentalCoroutinesApi::class)

package com.suhel.mycoolllama.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.pow

class ModelsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val importModelTrigger = MutableSharedFlow<Uri>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val modelReloadTrigger = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val modelsDirectory: File by lazy {
        File(context.filesDir, MODELS_DIRECTORY).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    val state = combine<List<Model>, ImportStatus, _>(
        modelReloadTrigger.onStart { emit(Unit) }.map { loadModels() },
        importModelTrigger.flatMapLatest { importModel(it) }.onStart { emit(ImportStatus()) }
    ) { models, importStatus ->
        State(
            availableModels = models,
            importStatus = importStatus
        )
    }

    fun addModel(uri: Uri): Boolean = importModelTrigger.tryEmit(uri)

    fun deleteModel(model: Model): Boolean = File(model.filePath).delete().also {
        modelReloadTrigger.tryEmit(Unit)
    }

    private suspend fun loadModels(): List<Model> = withContext(Dispatchers.IO) {
        if (modelsDirectory.exists()) {
            val files = modelsDirectory.listFiles() ?: emptyArray()

            // Filter for GGUF files and convert to Model objects
            files.filter { it.isFile && it.name.lowercase().endsWith(MODEL_EXTENSION) }
                .map { file ->
                    Model(
                        filePath = file.absolutePath,
                        name = file.name,
                        sizeBytes = file.length(),
                        dateImported = file.lastModified()
                    )
                }
                .sortedByDescending { it.dateImported }
        } else emptyList()
    }

    private fun importModel(uri: Uri): Flow<ImportStatus> = flow {
        emit(ImportStatus(busy = true))

        // Get file details from content resolver
        val fileName = getFileNameFromUri(context, uri)
            ?: return@flow emit(ImportStatus(error = ImportStatus.Error.NO_FILE_NAME))

        // Verify this is a GGUF file
        if (!fileName.lowercase().endsWith(MODEL_EXTENSION)) {
            return@flow emit(ImportStatus(error = ImportStatus.Error.NOT_GGUF))
        }

        // Check if model with same name already exists
        val destFile = File(modelsDirectory, fileName)
        if (destFile.exists()) {
            return@flow emit(ImportStatus(error = ImportStatus.Error.FILE_EXISTS))
        }

        // Copy the file with progress tracking
        val fileSize = getFileSizeFromUri(context, uri) ?: 0L

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    if (fileSize > 0) {
                        val progress = (totalBytesRead.toDouble() / fileSize).toFloat()
                        emit(ImportStatus(busy = true, progress = progress))
                    }
                }
            }
        } ?: return@flow emit(ImportStatus(error = ImportStatus.Error.IO_ERROR))

        emit(
            ImportStatus()
        )

        modelReloadTrigger.tryEmit(Unit)
    }.flowOn(Dispatchers.IO)

    private fun getFileNameFromUri(context: Context, uri: Uri): String? =
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                cursor.takeIf { it.moveToFirst() }
                    ?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    ?.takeIf { it != -1 }
                    ?.let { cursor.getString(it) }
            } ?: uri.lastPathSegment

    private fun getFileSizeFromUri(context: Context, uri: Uri): Long? =
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                cursor.takeIf { it.moveToFirst() }
                    ?.getColumnIndex(OpenableColumns.SIZE)
                    ?.takeIf { it != -1 }
                    ?.let(cursor::getLong)
            }

    data class State(
        val availableModels: List<Model> = emptyList(),
        val importStatus: ImportStatus = ImportStatus()
    )

    data class Model(
        val filePath: String,
        val name: String,
        val sizeBytes: Long,
        val dateImported: Long
    ) {
        val formattedSize: String
            get() {
                if (sizeBytes <= 0) return "0 B"
                val units = arrayOf("B", "KB", "MB", "GB", "TB")
                val digitGroups = (log10(sizeBytes.toDouble()) / log10(1024.0)).toInt()
                return "%.1f %s".format(sizeBytes / 1024.0.pow(digitGroups), units[digitGroups])
            }

        val formattedDateImported: String
            get() = dateFormatter.format(Date(dateImported))

        companion object {
            private val dateFormatter by lazy {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            }
        }
    }

    data class ImportStatus(
        val busy: Boolean = false,
        val progress: Float = 0.0f,
        val error: Error? = null
    ) {
        enum class Error {
            NO_FILE_NAME,
            NOT_GGUF,
            FILE_EXISTS,
            IO_ERROR
        }
    }

    companion object {
        private const val TAG = "ModelManager"
        private const val MODELS_DIRECTORY = "models"
        private const val MODEL_EXTENSION = ".gguf"
    }
}
