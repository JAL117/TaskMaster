package com.example.taskmaster.Task_List

// --- IMPORTACIONES COMPLETAS Y NECESARIAS ---
import android.os.Build // Necesario para @RequiresApi
import androidx.annotation.RequiresApi // Necesario por LocalDateTime y formato
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.taskmaster.data.model.Priority // Importa tu Enum Priority
import com.example.taskmaster.data.model.Task // Importa tu modelo Task actualizado
import com.example.taskmaster.ui.theme.TaskMasterTheme
// import java.time.LocalDate // Ya no es necesario aquí si Task usa LocalDateTime
import java.time.LocalDateTime // Importado
import java.time.LocalTime // Necesario para la Preview
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


// --- Función para obtener Color por Prioridad (Sin cambios) ---
@Composable
fun getPriorityColor(priority: Priority): Color {
    val highPriorityColor = Color(0xFFF44336) // Rojo
    val mediumPriorityColor = Color(0xFFFFEB3B) // Amarillo
    val lowPriorityColor = Color(0xFF4CAF50)  // Verde
    return when (priority) {
        Priority.HIGH -> highPriorityColor
        Priority.MEDIUM -> mediumPriorityColor
        Priority.LOW -> lowPriorityColor
    }
}


// --- Composable Principal del Item de Tarea (Usa dueDateTime) ---
@RequiresApi(Build.VERSION_CODES.O) // Necesario por LocalDateTime y formateadores
@Composable
fun TaskItem(
    task: Task, // Espera el Task actualizado con dueDateTime, imageUrl, audioUrl
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Formateador para Fecha y Hora
    val dateTimeFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT) }

    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de Prioridad
            Box( modifier = Modifier.width(6.dp).heightIn(min = 50.dp).background(getPriorityColor(task.priority), MaterialTheme.shapes.small) )
            Spacer(modifier = Modifier.width(12.dp))

            // Columna para Textos
            Column( modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp) ) {
                // Título
                Text( text = task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis )
                // Fecha y Hora de Vencimiento (si existe)
                task.dueDateTime?.let {
                    Text( text = "Vence: ${it.format(dateTimeFormatter)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant )
                }
                // Etiqueta (si existe)
                task.tag?.let { Text( text = "Etiqueta: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant ) }
            } // Fin Columna Textos

            Spacer(modifier = Modifier.width(8.dp))

            // Columna para Botones de Acción (Sin iconos)
            Column(horizontalAlignment = Alignment.End) {
                TextButton( onClick = { task.id?.let { onEditClick(it) } }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp) ) { Text("Editar") }
                TextButton( onClick = { task.id?.let { onDeleteClick(it) } }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp) ) { Text("Eliminar") }
            } // Fin Columna Botones
        } // Fin Row principal
    } // Fin Card
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun TaskItemPreview() {
    TaskMasterTheme {
        Column { // Muestra varios items en la preview
            TaskItem(
                task = Task(
                    id = "prev1", title = "Tarea Urgente con Hora", bodyText = "Desc.",
                    dueDateTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0), // Usa dueDateTime
                    tag = "Importante", priority = Priority.HIGH,
                    // --- CORREGIDO: Añadir valores (null) para los nuevos campos ---
                    imageUrl = null,
                    audioUrl = null
                    // dueDate = LocalDate.now().plusDays(1) // <-- ELIMINADO
                ),
                onEditClick = {}, onDeleteClick = {}
            )
            TaskItem(
                task = Task(
                    id = "prev2", title = "Tarea Normal", bodyText = "Hacer algo",
                    dueDateTime = LocalDateTime.now().plusWeeks(1).with(LocalTime.NOON), // Usa dueDateTime
                    tag = "Casa", priority = Priority.MEDIUM,
                    // --- CORREGIDO ---
                    imageUrl = null,
                    audioUrl = null
                    // dueDate = LocalDate.now().plusDays(1) // <-- ELIMINADO
                ),
                onEditClick = {}, onDeleteClick = {}
            )
            TaskItem(
                task = Task(
                    id = "prev3", title = "Tarea sin fecha/hora", bodyText = null,
                    dueDateTime = null, // Usa dueDateTime (es null aquí)
                    tag = null, priority = Priority.LOW,
                    // --- CORREGIDO ---
                    imageUrl = null,
                    audioUrl = null
                    // dueDate = LocalDate.now().plusDays(1) // <-- ELIMINADO
                ),
                onEditClick = {}, onDeleteClick = {}
            )
        }
    }
}