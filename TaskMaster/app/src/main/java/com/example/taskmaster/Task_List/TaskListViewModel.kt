package com.example.taskmaster.Task_List
// --- IMPORTACIONES COMPLETAS ---
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.data.model.Priority // Importa tus modelos
import com.example.taskmaster.data.model.Task // Asegúrate que Task usa dueDateTime, imageUrl, audioUrl
import kotlinx.coroutines.delay // Para simular carga/eliminación
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception // Import para Exception genérico
import java.time.LocalDate // Todavía útil para crear fechas base
import java.time.LocalDateTime // <-- Importado
import java.time.LocalTime // <-- Importado
// --- FIN IMPORTACIONES ---


// Estado para la UI de la lista (Sin cambios)
data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TaskListViewModel : ViewModel() {

    // --- Estado Interno y Expuesto (Sin cambios) ---
    private val _uiState = MutableStateFlow(TaskListUiState(isLoading = true))
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    // --- Eventos para Navegación (Sin cambios) ---
    private val _navigateToEditTask = MutableSharedFlow<String>()
    val navigateToEditTask: SharedFlow<String> = _navigateToEditTask.asSharedFlow()

    // Lista simulada en memoria (Sin cambios)
    private val simulatedTasks = mutableListOf<Task>()

    init {
        // Carga las tareas iniciales (Sin cambios)
        loadTasks()
        // Añadir algunas tareas de ejemplo (Actualizado)
        addExampleTasks()
    }

    // --- ** FUNCIÓN addExampleTasks CORREGIDA Y ACTUALIZADA ** ---
    private fun addExampleTasks() {
        viewModelScope.launch {
            // Solo añade ejemplos si la lista está vacía (evita duplicados)
            if (simulatedTasks.isNotEmpty()) return@launch

            delay(100) // Pequeña demora

            val now = LocalDateTime.now().withSecond(0).withNano(0)

            val examples = listOf(
                Task(
                    id = "1", title = "Comprar Leche", bodyText = "Ir al supermercado",
                    dueDateTime = now.plusDays(1).with(LocalTime.of(9, 0)),
                    tag = "Compras", priority = Priority.HIGH,
                    // --- CORREGIDO: Añadir parámetros faltantes ---
                    imageUrl = null,
                    audioUrl = null
                ),
                Task(
                    id = "2", title = "Estudiar Compose", bodyText = "Revisar LazyColumn",
                    dueDateTime = now.plusDays(3).with(LocalTime.NOON),
                    tag = "Estudio", priority = Priority.MEDIUM,
                    // --- CORREGIDO ---
                    imageUrl = null,
                    audioUrl = null
                ),
                Task(
                    id = "3", title = "Llamar a Mamá", bodyText = null,
                    dueDateTime = null,
                    tag = null, priority = Priority.LOW,
                    // --- CORREGIDO ---
                    imageUrl = null,
                    audioUrl = null
                ),
                Task(
                    id = "4", title = "Tarea Larga Scroll", bodyText = "Descripción larga...",
                    dueDateTime = now.plusWeeks(1).with(LocalTime.of(17, 30)),
                    tag = "Prueba", priority = Priority.MEDIUM,
                    // --- CORREGIDO ---
                    imageUrl = null,
                    audioUrl = null
                )
            )

            simulatedTasks.addAll(examples)
            // Actualiza el estado (Sin cambios aquí)
            _uiState.update { it.copy(tasks = simulatedTasks.toList()) }
        }
    }
    // --- ** FIN addExampleTasks CORREGIDA ** ---


    // --- Carga de Tareas (Simulada) - Lógica sin cambios ---
    fun loadTasks() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                delay(1000)
                // Asegura que isLoading se ponga a false después de cargar o si ya hay datos
                _uiState.update { it.copy(tasks = simulatedTasks.toList(), isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar tareas: ${e.message}") }
            }
        }
    }

    // --- Eliminación de Tarea (Simulada) - Lógica sin cambios ---
    fun deleteTask(taskId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                delay(500)
                simulatedTasks.removeAll { it.id == taskId }
                _uiState.update { it.copy(tasks = simulatedTasks.toList(), isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al eliminar tarea: ${e.message}") }
            }
        }
    }

    // --- Navegación (Sin cambios) ---
    fun onEditTaskClicked(taskId: String) {
        viewModelScope.launch { _navigateToEditTask.emit(taskId) }
    }

    // --- Resetear Mensaje de Error (Sin cambios) ---
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

} // Fin TaskListViewModel