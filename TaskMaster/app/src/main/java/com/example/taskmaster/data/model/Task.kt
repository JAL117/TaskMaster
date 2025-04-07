package com.example.taskmaster.data.model

import java.time.LocalDateTime

enum class Priority(val displayText: String) {
    HIGH("Alta"),
    MEDIUM("Media"),
    LOW("Baja")
}

data class Task(
    val id: String?,
    val title: String,
    val bodyText: String?,
    val imageUrl: String?,
    val audioUrl: String?,
    val dueDateTime: LocalDateTime?,
    val tag: String?,
    val priority: Priority,
    val isCompleted: Boolean = false
)