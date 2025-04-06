package com.example.taskmaster.AddEditTask // Asegúrate que este sea tu paquete

// --- IMPORTACIONES (Asegúrate de tener todas estas) ---
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi // Importante si usas APIs que lo requieren
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.data.model.Priority
import com.example.taskmaster.data.model.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.time.LocalDate // Asegúrate que tu proyecto soporta java.time
import java.util.UUID
// --- FIN IMPORTACIONES ---


// Estados para la grabación/reproducción de audio (Puede estar fuera o dentro)
sealed class AudioState {
    object Idle : AudioState()
    object Recording : AudioState()
    object Recorded : AudioState()
    object Playing : AudioState()
    data class Error(val message: String) : AudioState()
}

// Anotación opcional si usas MediaRecorder de forma específica
@RequiresApi(Build.VERSION_CODES.S) // O ajusta según tu lógica de versiones
class AddEditTaskViewModel : ViewModel() {

    // --- ** INICIO: DEFINICIÓN DE SaveResult DENTRO DEL VIEWMODEL ** ---
    sealed class SaveResult {
        object Idle : SaveResult()
        data class Success(val message: String? = null) : SaveResult()
        data class Error(val message: String) : SaveResult()
    }
    // --- ** FIN: DEFINICIÓN DE SaveResult ** ---


    // --- Estados existentes ---
    private val _title = MutableStateFlow("")
    private val _bodyText = MutableStateFlow("")
    private val _dueDate = MutableStateFlow<LocalDate?>(null)
    private val _tag = MutableStateFlow("")
    private val _priority = MutableStateFlow(Priority.MEDIUM)
    private val _isLoading = MutableStateFlow(false)
    // ---- CORREGIDO: Asegura que el tipo y valor inicial usan la definición interna ----
    private val _saveResult = MutableStateFlow<SaveResult>(SaveResult.Idle)


    // --- Estados para Adjuntos ---
    private val _imageUri = MutableStateFlow<Uri?>(null)
    private val _audioFile = MutableStateFlow<File?>(null)
    private val _audioState = MutableStateFlow<AudioState>(AudioState.Idle)

    // --- Estados Expuestos ---
    val title: StateFlow<String> = _title.asStateFlow()
    val bodyText: StateFlow<String> = _bodyText.asStateFlow()
    val dueDate: StateFlow<LocalDate?> = _dueDate.asStateFlow()
    val tag: StateFlow<String> = _tag.asStateFlow()
    val priority: StateFlow<Priority> = _priority.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val saveResult: StateFlow<SaveResult> = _saveResult.asStateFlow() // Usa SaveResult (definido arriba)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()
    val audioFile: StateFlow<File?> = _audioFile.asStateFlow()
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow() // Usa AudioState (definido arriba)

    // --- MediaRecorder y MediaPlayer ---
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    // --- Manejadores de Cambios ---
    fun onTitleChange(newTitle: String) { _title.value = newTitle }
    fun onBodyChange(newBody: String) { _bodyText.value = newBody }
    fun onDateChange(newDate: LocalDate?) { _dueDate.value = newDate }
    fun onTagChange(newTag: String) { _tag.value = newTag }
    fun onPriorityChange(newPriority: Priority) { _priority.value = newPriority }
    fun onImageSelected(uri: Uri?) { _imageUri.value = uri }
    fun clearImage() { _imageUri.value = null }


    // --- Lógica de Audio (Sin cambios respecto a tu código anterior) ---
    fun startRecording(context: Context) {
        if (_audioState.value !is AudioState.Idle) return
        val outputFile = File(context.cacheDir, "audio_${UUID.randomUUID()}.3gp")
        Log.d("AudioRecord", "Output file: ${outputFile.absolutePath}")
        recorder = MediaRecorder(context).apply { // Asumiendo API >= S para simplicidad aquí
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile.absolutePath)
            try {
                prepare()
                start()
                _audioFile.value = outputFile
                _audioState.value = AudioState.Recording
                Log.d("AudioRecord", "Recording started")
            } catch (e: Exception) { // Captura genérica por simplicidad
                Log.e("AudioRecord", "startRecording failed", e)
                _audioState.value = AudioState.Error("Error al iniciar grabación: ${e.message}")
                releaseRecorder()
                outputFile.delete()
                _audioFile.value = null
            }
        }
    }

    fun stopRecording() {
        if (_audioState.value is AudioState.Recording) {
            try {
                recorder?.apply { stop(); reset() }
                releaseRecorder()
                _audioState.value = AudioState.Recorded
                Log.d("AudioRecord", "Recording stopped")
            } catch (e: Exception) { // Captura genérica
                Log.e("AudioRecord", "stopRecording failed", e)
                _audioState.value = AudioState.Error("Error al detener grabación: ${e.message}")
                releaseRecorder()
                _audioFile.value?.delete()
                _audioFile.value = null
            }
        }
    }

    fun playAudio() {
        val fileToPlay = _audioFile.value ?: return
        if (_audioState.value != AudioState.Recorded && _audioState.value != AudioState.Idle) return

        player = MediaPlayer().apply {
            try {
                setDataSource(fileToPlay.absolutePath)
                prepareAsync()
                setOnPreparedListener { start(); _audioState.value = AudioState.Playing; Log.d("AudioRecord", "Playback started") }
                setOnCompletionListener { _audioState.value = AudioState.Recorded; releasePlayer(); Log.d("AudioRecord", "Playback completed") }
                setOnErrorListener { _, _, _ -> _audioState.value = AudioState.Error("Error de reproducción"); releasePlayer(); true }
            } catch (e: Exception) { // Captura genérica
                Log.e("AudioRecord", "playAudio failed", e)
                _audioState.value = AudioState.Error("Error al preparar audio: ${e.message}")
                releasePlayer()
            }
        }
    }

    fun stopPlayback() {
        if (_audioState.value is AudioState.Playing) {
            player?.apply {
                try { if (isPlaying) stop() }
                catch (e: IllegalStateException) { Log.e("AudioRecord", "MediaPlayer stop failed", e) }
                finally { releasePlayer(); _audioState.value = AudioState.Recorded; Log.d("AudioRecord", "Playback stopped manually") }
            }
        }
    }

    fun deleteAudio() {
        stopPlayback()
        stopRecording() // Por si acaso
        _audioFile.value?.delete()
        _audioFile.value = null
        _audioState.value = AudioState.Idle
        Log.d("AudioRecord", "Audio file deleted")
    }

    // --- Limpieza de Recursos ---
    private fun releaseRecorder() { /* ... (igual que antes) ... */
        try { recorder?.release() } catch (_: Exception) {} finally { recorder = null }
    }
    private fun releasePlayer() { /* ... (igual que antes) ... */
        try { player?.release() } catch (_: Exception) {} finally { player = null }
    }
    override fun onCleared() { /* ... (igual que antes, llama a release...) ... */
        super.onCleared()
        releaseRecorder()
        releasePlayer()
    }


    // --- Lógica de Guardado (ACTUALIZADA Y CORREGIDA) ---
    fun onSaveClick() {
        if (_isLoading.value) return
        if (title.value.isBlank()) {
            // ---- CORREGIDO: Usa SaveResult definido dentro ----
            _saveResult.value = SaveResult.Error("El título no puede estar vacío.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            // ---- CORREGIDO: Usa SaveResult definido dentro ----
            _saveResult.value = SaveResult.Idle // Resetea al iniciar

            try {
                delay(1500) // Simula la espera

                val newTask = Task(
                    title = title.value.trim(),
                    bodyText = bodyText.value.takeIf { it.isNotBlank() },
                    imageUrl = imageUri.value?.toString(),
                    audioUrl = audioFile.value?.toUri()?.toString(),
                    dueDate = dueDate.value,
                    tag = tag.value.takeIf { it.isNotBlank() },
                    priority = priority.value
                )
                Log.d("AddEditTaskVM", "Simulando guardado de: $newTask")

                // Aquí iría la llamada real al repositorio
                // val repositoryResult = repository.saveTask(newTask, imageUri.value, audioFile.value)
                // if (repositoryResult.isSuccess) { ... }

                // ---- CORREGIDO: Usa SaveResult definido dentro ----
                _saveResult.value = SaveResult.Success("¡Tarea guardada!")

            } catch (e: Exception) {
                Log.e("AddEditTaskVM", "Error simulando guardado", e)
                // ---- CORREGIDO: Usa SaveResult definido dentro ----
                _saveResult.value = SaveResult.Error("Error al guardar la tarea: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Función de Reseteo (CORREGIDA) ---
    fun resetSaveState() {
        // ---- CORREGIDO: Usa SaveResult definido dentro ----
        _saveResult.value = SaveResult.Idle
    }

} // <-- LLAVE DE CIERRE FINAL DE LA CLASE AddEditTaskViewModel