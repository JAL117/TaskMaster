package com.example.taskmaster.data.model // O donde tengas tus modelos

import java.time.LocalDate // Usaremos LocalDate para la fecha

// Define una enum para Prioridad (más claro que Ints)
enum class Priority(val displayText: String) {
    HIGH("Alta"),
    MEDIUM("Media"),
    LOW("Baja")
}


data class Task(
    val id: String? = null,
    val title: String,
    val bodyText: String?,
    val imageUrl: String? = null, // <-- NUEVO: URI de la imagen como String
    val audioUrl: String? = null, // <-- NUEVO: URI del audio como String
    val dueDate: LocalDate?,
    val tag: String?,
    val priority: Priority,
    val isCompleted: Boolean = false
)