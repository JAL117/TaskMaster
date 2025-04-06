package com.example.taskmaster.AddEditTask // Asegúrate que este sea tu paquete

// --- IMPORTACIONES COMPLETAS Y NECESARIAS ---
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.data.model.Priority
import com.example.taskmaster.data.model.Task // Asegúrate que Task usa LocalDateTime y tiene isCompleted
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.lang.Exception // Importado
import java.time.LocalDate
import java.time.LocalDateTime // Importado
import java.time.LocalTime // Importado
import java.util.UUID
// --- FIN IMPORTACIONES ---


// sealed class AudioState { ... } // <-- ELIMINA LA DEFINICIÓN DE AQUÍ FUERA

@RequiresApi(Build.VERSION_CODES.S) // Ajusta si es necesario
class AddEditTaskViewModel : ViewModel() {

    // --- ** DEFINICIÓN DE SaveResult DENTRO ** ---
    sealed class SaveResult {
        object Idle : SaveResult()
        data class Success(val message: String? = null) : SaveResult()
        data class Error(val message: String) : SaveResult()
    }

    // --- ** DEFINICIÓN DE AudioState MOVIDA AQUÍ DENTRO ** ---
    sealed class AudioState {
        object Idle : AudioState()
        object Recording : AudioState()
        object Recorded : AudioState()
        object Playing : AudioState()
        data class Error(val message: String) : AudioState()
    }
    // --- ** FIN AudioState ** ---


    // --- Estados Internos ---
    private val _title = MutableStateFlow("")
    private val _bodyText = MutableStateFlow("")
    private val _dueDateTime = MutableStateFlow<LocalDateTime?>(null) // Usa LocalDateTime
    private val _tag = MutableStateFlow("")
    private val _priority = MutableStateFlow(Priority.MEDIUM)
    private val _isLoading = MutableStateFlow(false)
    private val _saveResult = MutableStateFlow<SaveResult>(SaveResult.Idle) // Usa SaveResult interno
    private val _imageUri = MutableStateFlow<Uri?>(null)
    private val _audioFile = MutableStateFlow<File?>(null)
    // ---- ESTA LÍNEA AHORA DEBERÍA FUNCIONAR ----
    private val _audioState = MutableStateFlow<AudioState>(AudioState.Idle) // Usa AudioState interno

    // --- Estados Expuestos ---
    val title: StateFlow<String> = _title.asStateFlow()
    val bodyText: StateFlow<String> = _bodyText.asStateFlow()
    val dueDateTime: StateFlow<LocalDateTime?> = _dueDateTime.asStateFlow()
    val tag: StateFlow<String> = _tag.asStateFlow()
    val priority: StateFlow<Priority> = _priority.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val saveResult: StateFlow<SaveResult> = _saveResult.asStateFlow() // Expone SaveResult interno
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()
    val audioFile: StateFlow<File?> = _audioFile.asStateFlow()
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow() // Expone AudioState interno

    // --- MediaRecorder y MediaPlayer ---
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    // --- Manejadores de Cambios ---
    fun onTitleChange(newTitle: String) { _title.value = newTitle }
    fun onBodyChange(newBody: String) { _bodyText.value = newBody }
    fun onTagChange(newTag: String) { _tag.value = newTag }
    fun onPriorityChange(newPriority: Priority) { _priority.value = newPriority }
    fun onImageSelected(uri: Uri?) { _imageUri.value = uri }
    fun clearImage() { _imageUri.value = null }

    // --- Funciones para Fecha y Hora ---
    fun onDateSelected(newDate: LocalDate) {
        val currentTime = _dueDateTime.value?.toLocalTime() ?: LocalTime.NOON
        _dueDateTime.value = LocalDateTime.of(newDate, currentTime)
    }
    fun onTimeSelected(newTime: LocalTime) {
        val currentDate = _dueDateTime.value?.toLocalDate() ?: LocalDate.now()
        _dueDateTime.value = LocalDateTime.of(currentDate, newTime)
    }
    fun clearDateTime() { _dueDateTime.value = null }


    // --- Lógica de Audio (Usa AudioState interno) ---
    fun startRecording(context: Context) {
        if (_audioState.value !is AudioState.Idle) return // Correcto
        val outputFile = File(context.cacheDir, "audio_${UUID.randomUUID()}.3gp")
        recorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC); setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); setOutputFile(outputFile.absolutePath)
            try { prepare(); start(); _audioFile.value = outputFile; _audioState.value = AudioState.Recording; Log.d("AudioRecord", "Rec started") } // Correcto
            catch (e: Exception) { Log.e("AudioRecord", "startRec fail", e); _audioState.value = AudioState.Error("Err start rec: ${e.message}"); releaseRecorder(); outputFile.delete(); _audioFile.value = null } // Correcto
        }
    }
    fun stopRecording() {
        if (_audioState.value is AudioState.Recording) { // Correcto
            try { recorder?.apply { stop(); reset() }; releaseRecorder(); _audioState.value = AudioState.Recorded; Log.d("AudioRecord", "Rec stopped") } // Correcto
            catch (e: Exception) { Log.e("AudioRecord", "stopRec fail", e); _audioState.value = AudioState.Error("Err stop rec: ${e.message}"); releaseRecorder(); _audioFile.value?.delete(); _audioFile.value = null } // Correcto
        }
    }
    fun playAudio() {
        val fileToPlay = _audioFile.value ?: return
        if (_audioState.value != AudioState.Recorded && _audioState.value != AudioState.Idle) return // Correcto
        player = MediaPlayer().apply {
            try {
                setDataSource(fileToPlay.absolutePath); prepareAsync()
                setOnPreparedListener { start(); _audioState.value = AudioState.Playing; Log.d("AudioRecord", "Play started") } // Correcto
                setOnCompletionListener { _audioState.value = AudioState.Recorded; releasePlayer(); Log.d("AudioRecord", "Play completed") } // Correcto
                setOnErrorListener { _, _, _ -> _audioState.value = AudioState.Error("Err playback"); releasePlayer(); true } // Correcto
            } catch (e: Exception) { Log.e("AudioRecord", "playAudio fail", e); _audioState.value = AudioState.Error("Err prepare audio: ${e.message}"); releasePlayer() } // Correcto
        }
    }
    fun stopPlayback() {
        if (_audioState.value is AudioState.Playing) { // Correcto
            player?.apply {
                try { if (isPlaying) stop() } catch (e: IllegalStateException) { Log.e("AudioRecord", "stopPlay fail", e) }
                finally { releasePlayer(); _audioState.value = AudioState.Recorded; Log.d("AudioRecord", "Play stopped manual") } // Correcto
            }
        }
    }
    fun deleteAudio() {
        stopPlayback(); stopRecording(); _audioFile.value?.delete(); _audioFile.value = null; _audioState.value = AudioState.Idle; Log.d("AudioRecord", "Audio deleted") // Correcto
    }


    // --- Limpieza de Recursos ---
    private fun releaseRecorder() { try { recorder?.release() } catch (_: Exception) {} finally { recorder = null } }
    private fun releasePlayer() { try { player?.release() } catch (_: Exception) {} finally { player = null } }
    override fun onCleared() { super.onCleared(); releaseRecorder(); releasePlayer() }


    // --- Lógica de Guardado ---
    fun onSaveClick() {
        if (_isLoading.value) return
        if (title.value.isBlank()) {
            _saveResult.value = SaveResult.Error("El título no puede estar vacío.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _saveResult.value = SaveResult.Idle // Correcto
            try {
                delay(1500)
                val newTask = Task( id = null, title = title.value.trim(), bodyText = bodyText.value.takeIf { it.isNotBlank() }, imageUrl = imageUri.value?.toString(), audioUrl = audioFile.value?.toUri()?.toString(), dueDateTime = dueDateTime.value, tag = tag.value.takeIf { it.isNotBlank() }, priority = priority.value, isCompleted = false )
                Log.d("AddEditTaskVM", "Simulando guardado de: $newTask")
                // Repo call...
                _saveResult.value = SaveResult.Success("¡Tarea guardada!") // Correcto
            } catch (e: Exception) {
                Log.e("AddEditTaskVM", "Error simulando guardado", e)
                _saveResult.value = SaveResult.Error("Error al guardar la tarea: ${e.message}") // Correcto
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Función de Reseteo ---
    fun resetSaveState() {
        _saveResult.value = SaveResult.Idle // Correcto
    }

} // <-- LLAVE DE CIERRE FINAL DE LA CLASE AddEditTaskViewModel