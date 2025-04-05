package com.example.taskmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.taskmaster.Login.LoginScreen
import com.example.taskmaster.Register.RegisterScreen // <-- IMPORTA LA NUEVA PANTALLA
// Importa tus otras pantallas y placeholders
// import com.example.taskmaster.TaskList.TaskListScreenPlaceholder
// import com.example.taskmaster.AddEditTask.AddEditTaskScreenPlaceholder
// import com.example.taskmaster.Menu.MenuScreenPlaceholder
import com.example.taskmaster.ui.theme.TaskMasterTheme

// Sealed class Screen (igual que antes)
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object TaskList : Screen("task_list")
    data class AddEditTask(val taskId: String? = null) : Screen("add_edit_task")
    object Menu : Screen("menu")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskMasterTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TaskMasterApp()
                }
            }
        }
    }
}

@Composable
fun TaskMasterApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    when (val screen = currentScreen) {
        is Screen.Login -> LoginScreen(
            onLoginSuccess = { currentScreen = Screen.TaskList },
            // Aquí está la conexión clave:
            onNavigateToRegister = { currentScreen = Screen.Register } // Cambia el estado a Register
        )
        is Screen.Register -> RegisterScreen( // <-- USA TU RegisterScreen REAL
            onRegisterSuccess = {
                // Ir a la lista de tareas después del registro exitoso
                currentScreen = Screen.TaskList
            },
            onNavigateBackToLogin = {
                // Volver a la pantalla de login
                currentScreen = Screen.Login
            }
        )
        is Screen.TaskList -> TaskListScreenPlaceholder( // Reemplaza con tu TaskListScreen real
            onNavigateToAddTask = { currentScreen = Screen.AddEditTask(taskId = null) },
            onNavigateToEditTask = { taskId -> currentScreen = Screen.AddEditTask(taskId = taskId) },
            onNavigateToMenu = { currentScreen = Screen.Menu },
            onLogout = { currentScreen = Screen.Login }
        )
        is Screen.AddEditTask -> AddEditTaskScreenPlaceholder( // Reemplaza con tu AddEditTaskScreen real
            taskId = screen.taskId,
            onTaskSaved = { currentScreen = Screen.TaskList },
            onNavigateBack = { currentScreen = Screen.TaskList }
        )
        is Screen.Menu -> MenuScreenPlaceholder( // Reemplaza con tu MenuScreen real
            onNavigateBack = { currentScreen = Screen.TaskList }
        )
    }
}

// Mantén los placeholders para las otras pantallas por ahora si no las has creado
// ... (Placeholders de TaskList, AddEdit, Menu como los tenías) ...

// --- PLACEHOLDERS --- (Excepto Register, que ya no es placeholder)

@Composable
fun TaskListScreenPlaceholder(
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (String) -> Unit,
    onNavigateToMenu: () -> Unit,
    onLogout: () -> Unit
) { /* ... código del placeholder ... */ }

@Composable
fun AddEditTaskScreenPlaceholder(taskId: String?, onTaskSaved: () -> Unit, onNavigateBack: () -> Unit) { /* ... código del placeholder ... */ }

@Composable
fun MenuScreenPlaceholder(onNavigateBack: () -> Unit) { /* ... código del placeholder ... */ }