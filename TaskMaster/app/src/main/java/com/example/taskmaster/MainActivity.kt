package com.example.taskmaster // Asegúrate que este sea tu paquete raíz

// --- IMPORTACIONES COMPLETAS Y NECESARIAS ---
import android.os.Build // Necesario para @RequiresApi
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi // Necesario por AddEditTaskScreen y TaskListScreen (indirectamente por TaskItem)
import androidx.compose.foundation.layout.Arrangement // Usado en el placeholder eliminado, podría quitarse si no se usa más
import androidx.compose.foundation.layout.Column      // Usado en el placeholder eliminado
import androidx.compose.foundation.layout.Spacer       // Usado en el placeholder eliminado
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height      // Usado en el placeholder eliminado
import androidx.compose.foundation.layout.padding     // Usado en el placeholder eliminado
import androidx.compose.material3.Button        // Usado en el placeholder eliminado
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text          // Usado en el placeholder eliminado
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment    // Usado en el placeholder eliminado
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp         // Usado en el placeholder eliminado
import com.example.taskmaster.AddEditTask.AddEditTaskScreen // Importa AddEditTaskScreen REAL
import com.example.taskmaster.Login.LoginScreen             // Importa LoginScreen REAL
import com.example.taskmaster.Menu.MenuScreen               // Importa MenuScreen REAL
import com.example.taskmaster.Register.RegisterScreen         // Importa RegisterScreen REAL
import com.example.taskmaster.TaskList.TaskListScreen       // <-- IMPORTA TaskListScreen REAL
import com.example.taskmaster.ui.theme.TaskMasterTheme        // Importa tu tema
// --- FIN IMPORTACIONES ---


// ===========================================================
// 1. Definición de las Pantallas Posibles (Sealed Class)
// ===========================================================
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object TaskList : Screen("task_list")
    data class AddEditTask(val taskId: String? = null) : Screen("add_edit_task") // Para añadir o editar
    object Menu : Screen("menu")
}

// ===========================================================
// 2. Clase Principal de la Actividad
// ===========================================================
class MainActivity : ComponentActivity() {
    // Es necesario aquí si tu minSdk < 26 y usas java.time en alguna pantalla llamada directamente
    // @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskMasterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Llama al Composable que maneja la navegación
                    TaskMasterApp()
                }
            }
        }
    }
}

// ===========================================================
// 3. Composable Principal que Gestiona la Navegación Manual
// ===========================================================
// Añade la anotación aquí porque llama a Composables que la requieren (AddEditTaskScreen)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskMasterApp() {
    // Estado que controla qué pantalla se muestra
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    // Log para depurar qué pantalla se está mostrando
    Log.d("TaskMasterApp", "Recomposing. Current Screen: ${currentScreen::class.simpleName}")

    // El 'when' decide qué Composable mostrar según el estado actual
    when (val screen = currentScreen) {

        // ---- Caso: Pantalla de Login ----
        is Screen.Login -> {
            Log.d("TaskMasterApp", "Displaying LoginScreen")
            LoginScreen(
                onLoginSuccess = { currentScreen = Screen.Menu }, // Va al Menú
                onNavigateToRegister = { currentScreen = Screen.Register } // Va a Registro
            )
        }

        // ---- Caso: Pantalla de Registro ----
        is Screen.Register -> {
            Log.d("TaskMasterApp", "Displaying RegisterScreen")
            RegisterScreen(
                onRegisterSuccess = { currentScreen = Screen.Menu }, // Va al Menú
                onNavigateBackToLogin = { currentScreen = Screen.Login } // Vuelve a Login
            )
        }

        // ---- Caso: Pantalla de Menú ----
        is Screen.Menu -> {
            Log.d("TaskMasterApp", "Displaying MenuScreen")
            MenuScreen(
                onNavigateToTaskList = { currentScreen = Screen.TaskList }, // Va a Lista REAL
                onNavigateToAddTask = { currentScreen = Screen.AddEditTask(taskId = null) }, // Va a Añadir Tarea REAL
                onLogout = {
                    // Aquí limpiarías sesión
                    currentScreen = Screen.Login // Vuelve a Login
                }
            )
        }

        // ---- Caso: Pantalla Añadir/Editar Tarea (USA LA REAL) ----
        is Screen.AddEditTask -> {
            Log.d("TaskMasterApp", "Displaying AddEditTaskScreen for task: ${screen.taskId}")
            AddEditTaskScreen(
                // taskId = screen.taskId, // Descomenta para edición
                onTaskSaved = {
                    Log.d("TaskMasterApp", "Task Saved -> Navigate to TaskList")
                    currentScreen = Screen.TaskList // Vuelve a la lista REAL
                },
                onNavigateBack = {
                    Log.d("TaskMasterApp", "AddEditTask -> Navigate Back")
                    currentScreen = Screen.Menu // Vuelve al Menú (o a TaskList si prefieres)
                }
            )
        }

        // ---- Caso: Pantalla Lista de Tareas (USA LA REAL) ----
        is Screen.TaskList -> {
            Log.d("TaskMasterApp", "Displaying TaskListScreen")
            // *** CAMBIO CLAVE: Llama a TaskListScreen real ***
            TaskListScreen(
                onNavigateToAddTask = {
                    Log.d("TaskMasterApp", "TaskList -> Navigate to AddTask")
                    currentScreen = Screen.AddEditTask(taskId = null) // Va a Añadir Tarea REAL
                },
                onNavigateToEditTask = { taskId ->
                    Log.d("TaskMasterApp", "TaskList -> Navigate to EditTask $taskId")
                    currentScreen = Screen.AddEditTask(taskId = taskId) // Va a Editar Tarea REAL (pasando ID)
                },
                onNavigateToMenu = {
                    Log.d("TaskMasterApp", "TaskList -> Navigate to Menu")
                    currentScreen = Screen.Menu // Vuelve al Menú
                },
                onLogout = {
                    Log.d("TaskMasterApp", "TaskList Logout -> Navigate to Login")
                    // Limpiar sesión
                    currentScreen = Screen.Login // Vuelve a Login
                }
            )
        }
    } // Fin when
} // Fin TaskMasterApp

// ===========================================================
// 4. PLACEHOLDERS (YA NO QUEDAN PANTALLAS POR IMPLEMENTAR EN ESTE FLUJO)
// ===========================================================

// --- ELIMINADO EL TaskListScreenPlaceholder ---
// @Composable
// fun TaskListScreenPlaceholder(...) { ... }

// --- ELIMINADO EL AddEditTaskScreenPlaceholder ---
// @Composable
// fun AddEditTaskScreenPlaceholder(...) { ... }