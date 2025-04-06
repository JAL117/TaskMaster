package com.example.taskmaster // Asegúrate que este sea tu paquete raíz

import android.os.Bundle
import android.util.Log // Import necesario para los Logs de depuración
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column // Import necesario para Placeholders
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding // Import necesario para Placeholders
import androidx.compose.material3.Button // Import necesario para Placeholders
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text // Import necesario para Placeholders
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp // Import necesario para Placeholders
import com.example.taskmaster.Login.LoginScreen // Importa tu pantalla de Login
import com.example.taskmaster.Register.RegisterScreen // Importa tu pantalla de Registro
import com.example.taskmaster.Menu.MenuScreen // <-- IMPORTA LA PANTALLA DE MENÚ
// Importa tus otras pantallas cuando las tengas
// import com.example.taskmaster.TaskList.TaskListScreen
// import com.example.taskmaster.AddEditTask.AddEditTaskScreen
import com.example.taskmaster.ui.theme.TaskMasterTheme // Importa tu tema

// ===========================================================
// 1. Definición de las Pantallas Posibles (Sealed Class)
// ===========================================================
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object TaskList : Screen("task_list")
    data class AddEditTask(val taskId: String? = null) : Screen("add_edit_task")
    object Menu : Screen("menu") // Asegúrate que está definido
}

// ===========================================================
// 2. Clase Principal de la Actividad
// ===========================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskMasterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskMasterApp()
                }
            }
        }
    }
}

// ===========================================================
// 3. Composable Principal que Gestiona la Navegación Manual
// ===========================================================
@Composable
fun TaskMasterApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    Log.d("TaskMasterApp", "Recomposing. Current Screen: ${currentScreen::class.simpleName}")

    when (val screen = currentScreen) {

        // ---- Caso: Pantalla de Login ----
        is Screen.Login -> {
            Log.d("TaskMasterApp", "Displaying LoginScreen")
            LoginScreen(
                // **CAMBIO:** Va al Menú después del login
                onLoginSuccess = {
                    Log.d("TaskMasterApp", "Login Success -> Navigate to Menu")
                    currentScreen = Screen.Menu
                },
                onNavigateToRegister = {
                    Log.d("TaskMasterApp", "Login -> Navigate to Register")
                    currentScreen = Screen.Register
                }
            )
        }

        // ---- Caso: Pantalla de Registro ----
        is Screen.Register -> {
            Log.d("TaskMasterApp", "Displaying RegisterScreen")
            RegisterScreen(
                // **CAMBIO:** Va al Menú después del registro
                onRegisterSuccess = {
                    Log.d("TaskMasterApp", "Register Success -> Navigate to Menu")
                    currentScreen = Screen.Menu
                },
                onNavigateBackToLogin = {
                    Log.d("TaskMasterApp", "Register -> Navigate back to Login")
                    currentScreen = Screen.Login
                }
            )
        }

        // ---- Caso: Pantalla de Menú ----
        is Screen.Menu -> {
            Log.d("TaskMasterApp", "Displaying MenuScreen")
            // **CAMBIO:** Llama a MenuScreen real
            MenuScreen(
                onNavigateToTaskList = {
                    Log.d("TaskMasterApp", "Menu -> Navigate to TaskList")
                    currentScreen = Screen.TaskList
                },
                onNavigateToAddTask = {
                    Log.d("TaskMasterApp", "Menu -> Navigate to AddTask")
                    currentScreen = Screen.AddEditTask(taskId = null)
                },
                onLogout = {
                    Log.d("TaskMasterApp", "Menu -> Logout -> Navigate to Login")
                    // Aquí limpiarías datos de sesión si los tuvieras
                    currentScreen = Screen.Login
                }
            )
        }

        // ---- Caso: Pantalla Lista de Tareas (Placeholder) ----
        is Screen.TaskList -> {
            Log.d("TaskMasterApp", "Displaying TaskListScreen (Placeholder)")
            TaskListScreenPlaceholder(
                onNavigateToAddTask = { currentScreen = Screen.AddEditTask(taskId = null) },
                onNavigateToEditTask = { taskId -> currentScreen = Screen.AddEditTask(taskId = taskId) },
                // Podrías necesitar un botón para volver al menú desde aquí, o no.
                onNavigateToMenu = { currentScreen = Screen.Menu },
                onLogout = {
                    Log.d("TaskMasterApp", "TaskList Logout -> Navigate to Login")
                    // Limpiar sesión
                    currentScreen = Screen.Login
                }
            )
        }

        // ---- Caso: Pantalla Añadir/Editar Tarea (Placeholder) ----
        is Screen.AddEditTask -> {
            Log.d("TaskMasterApp", "Displaying AddEditTaskScreen (Placeholder) for task: ${screen.taskId}")
            AddEditTaskScreenPlaceholder(
                taskId = screen.taskId,
                onTaskSaved = { currentScreen = Screen.TaskList },
                onNavigateBack = { currentScreen = Screen.TaskList } // Vuelve a la lista al cancelar/guardar
            )
        }
    }
}

// ===========================================================
// 4. PLACEHOLDERS para las pantallas aún no implementadas
//    (TaskList, AddEditTask)
// ===========================================================

@Composable
fun TaskListScreenPlaceholder(
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (String) -> Unit,
    onNavigateToMenu: () -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Task List Screen Placeholder")
        Button(onClick = onNavigateToAddTask) { Text("Add New Task") }
        Button(onClick = { onNavigateToEditTask("task123") }) { Text("Edit Task 'task123'") }
        Button(onClick = onNavigateToMenu) { Text("Go back to Menu") } // Botón para volver al menú
        Button(onClick = onLogout) { Text("Logout") }
    }
}

@Composable
fun AddEditTaskScreenPlaceholder(taskId: String?, onTaskSaved: () -> Unit, onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(if (taskId == null) "Add New Task Placeholder" else "Edit Task $taskId Placeholder")
        Button(onClick = onTaskSaved) { Text("Save Task") }
        Button(onClick = onNavigateBack) { Text("Cancel / Back to Task List") }
    }
}

// Ya no se necesita el MenuScreenPlaceholder