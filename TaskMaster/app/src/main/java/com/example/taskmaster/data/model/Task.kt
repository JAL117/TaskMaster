package com.example.taskmaster.data.model // O donde tengas tus modelos

import java.time.LocalDateTime // Asegúrate que usa LocalDateTime

// Asegúrate que Priority está definida (puede ser en otro archivo o aquí)
enum class Priority(val displayText: String) {
    HIGH("Alta"),
    MEDIUM("Media"),
    LOW("Baja")
}

// ---- ¡VERIFICA QUE TU DATA CLASS SE VEA ASÍ! ----
data class Task(
    val id: String?, // Puede ser null si aún no tiene ID de la BD
    val title: String,
    val bodyText: String?, // Texto opcional
    val imageUrl: String?, // Opcional
    val audioUrl: String?, // Opcional
    val dueDateTime: LocalDateTime?, // Usa LocalDateTime y es opcional
    val tag: String?, // <-- ¿Tienes este parámetro llamado 'tag' y de tipo String??
    val priority: Priority, // Tipo Enum Priority
    val isCompleted: Boolean = false // Valor por defecto
)
// ---- FIN VERIFICACIÓN ----