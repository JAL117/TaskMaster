package com.example.taskmaster.Task_List

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Import para LazyColumn
import androidx.compose.material.icons.Icons // Si decides añadir icono al FAB
import androidx.compose.material.icons.filled.Add // Ejemplo
import androidx.compose.material3.*
import androidx.compose.runtime.* // collectAsState, LaunchedEffect, remember, etc.
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskmaster.ui.theme.TaskMasterTheme
import kotlinx.coroutines.flow.collectLatest // Para observar SharedFlow

@OptIn(ExperimentalMaterial3Api::class) // Para Scaffold, FAB, etc.
@Composable
fun TaskListScreen(
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (taskId: String) -> Unit,
    onNavigateToMenu: () -> Unit, // Callback para volver al menú
    onLogout: () -> Unit, // Callback para cerrar sesión
    viewModel: TaskListViewModel = viewModel()
) {
    // Observa el estado completo de la UI
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para controlar el diálogo de confirmación de eliminación
    var showDeleteDialog by remember { mutableStateOf(false) }
    var taskToDeleteId by remember { mutableStateOf<String?>(null) }

    // Observa eventos de navegación desde el ViewModel
    LaunchedEffect(Unit) { // Se ejecuta una vez al entrar en la pantalla
        viewModel.navigateToEditTask.collectLatest { taskId ->
            onNavigateToEditTask(taskId) // Llama al callback de MainActivity
        }
    }

    // Muestra errores en el Snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            viewModel.clearErrorMessage() // Limpia el mensaje después de mostrarlo
        }
    }

    // --- Diálogo de Confirmación de Eliminación ---
    if (showDeleteDialog && taskToDeleteId != null) {
        AlertDialog(
            onDismissRequest = {
                // Cierra el diálogo si se toca fuera o botón atrás
                showDeleteDialog = false
                taskToDeleteId = null
            },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar esta tarea?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskToDeleteId?.let { viewModel.deleteTask(it) } // Llama a eliminar en ViewModel
                        showDeleteDialog = false
                        taskToDeleteId = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error) // Rojo
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    taskToDeleteId = null
                }) { Text("Cancelar") }
            }
        )
    } // Fin AlertDialog


    // --- UI Principal con Scaffold ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas") },
                // Puedes añadir acciones aquí si quieres (ej. volver al menú)
                actions = {
                    /* IconButton(onClick = onNavigateToMenu) {
                         Icon(Icons.Filled.Menu, contentDescription = "Menú")
                     }*/
                    // O un botón de Logout directo si no hay menú intermedio
                    TextButton(onClick = onLogout) { Text("Salir") }
                }
            )
        },
        floatingActionButton = { // Botón flotante para añadir tareas
            FloatingActionButton(onClick = onNavigateToAddTask) {
                // Icon(Icons.Filled.Add, contentDescription = "Añadir Tarea") // Si añades icono
                Text("+") // Alternativa simple sin icono
            }
        }
    ) { paddingValues -> // Padding aplicado por Scaffold

        Box( // Usamos Box para centrar el indicador de carga o mensajes
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplica padding del Scaffold
        ) {
            // Muestra indicador de carga si está cargando
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // Muestra mensaje si la lista está vacía y no está cargando
            else if (uiState.tasks.isEmpty()) {
                Text(
                    text = "¡No tienes tareas pendientes! Añade una nueva.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            // Muestra la lista si hay tareas y no está cargando
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp) // Espacio arriba/abajo de la lista
                ) {
                    // Itera sobre la lista de tareas del estado
                    items(
                        items = uiState.tasks,
                        key = { task -> task.id ?: task.hashCode() } // Usa ID como key estable
                    ) { task ->
                        TaskItem(
                            task = task,
                            onEditClick = { taskId ->
                                viewModel.onEditTaskClicked(taskId) // Notifica al ViewModel
                            },
                            onDeleteClick = { taskId ->
                                // Abre el diálogo de confirmación
                                taskToDeleteId = taskId
                                showDeleteDialog = true
                            },
                            modifier = Modifier.padding(bottom = 4.dp) // Espacio extra bajo cada item si se desea
                        )
                    }
                } // Fin LazyColumn
            } // Fin else (mostrar lista)
        } // Fin Box
    } // Fin Scaffold
}


// --- Previsualización de la Pantalla Completa ---
@Preview(showBackground = true)
@Composable
fun TaskListScreenPreview() {
    TaskMasterTheme {
        TaskListScreen(
            onNavigateToAddTask = {},
            onNavigateToEditTask = {},
            onNavigateToMenu = {},
            onLogout = {}
            // No podemos previsualizar fácilmente el ViewModel aquí sin Hilt o Factories
        )
    }
}