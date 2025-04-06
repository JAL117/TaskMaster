package com.example.taskmaster.Menu // Asegúrate que este sea tu paquete

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.taskmaster.ui.theme.TaskMasterTheme

@Composable
fun MenuScreen(
    // Callbacks para notificar a MainActivity qué acción tomar
    onNavigateToTaskList: () -> Unit,
    onNavigateToAddTask: () -> Unit,
    // Podrías añadir más opciones aquí (ej: onNavigateToSettings)
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp), // Añade padding vertical también
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Centra las opciones verticalmente
    ) {
        Text(
            text = "Menú Principal",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 48.dp) // Más espacio bajo el título
        )

        // Botón para ir a la Lista de Tareas
        Button(
            onClick = onNavigateToTaskList,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp) // Espacio vertical entre botones
        ) {
            Text("Ver Mis Tareas")
        }

        // Botón para ir a Añadir Tarea
        Button(
            onClick = onNavigateToAddTask,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Añadir Nueva Tarea")
        }

        // Puedes añadir más botones aquí para otras secciones
        // Button(onClick = { /* onNavigateToSettings() */ }, ...) { Text("Configuración") }

        Spacer(modifier = Modifier.height(32.dp)) // Espacio antes del botón de salir

        // Botón para Cerrar Sesión
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            // Cambia los colores para que parezca una acción diferente (opcional)
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Cerrar Sesión")
        }
    }
}

// --- Previsualización ---
@Preview(showBackground = true, device = "id:pixel_6") // Puedes probar en diferentes dispositivos
@Composable
fun MenuScreenPreview() {
    TaskMasterTheme {
        MenuScreen(
            onNavigateToTaskList = { println("Preview: Nav to Task List") },
            onNavigateToAddTask = { println("Preview: Nav to Add Task") },
            onLogout = { println("Preview: Logout") }
        )
    }
}