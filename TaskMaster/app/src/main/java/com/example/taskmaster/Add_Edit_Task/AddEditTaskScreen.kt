package com.example.taskmaster.AddEditTask // Asegúrate que sea tu paquete

// --- IMPORTACIONES COMPLETAS Y CORREGIDAS ---
import android.Manifest
// import android.app.DatePickerDialog // ELIMINADO
import android.net.Uri
import android.os.Build
import android.util.Log
// import android.widget.DatePicker // ELIMINADO
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
// import androidx.compose.foundation.Image // No se usa
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource // Necesario para el TextField readOnly
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.* // Import base M3
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.res.painterResource // No se usa
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage // Para cargar imágenes
import com.example.taskmaster.R
import com.example.taskmaster.data.model.Priority
import com.example.taskmaster.ui.theme.TaskMasterTheme
import com.google.accompanist.permissions.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
// --- FIN IMPORTACIONES ---


@RequiresApi(Build.VERSION_CODES.O) // Requerido por java.time si minSdk < 26
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class) // APIs experimentales
@Composable
fun AddEditTaskScreen(
    onTaskSaved: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AddEditTaskViewModel = viewModel()
) {
    // --- Observación del Estado del ViewModel ---
    val title by viewModel.title.collectAsState()
    val bodyText by viewModel.bodyText.collectAsState()
    val dueDate by viewModel.dueDate.collectAsState()
    val tag by viewModel.tag.collectAsState()
    val selectedPriority by viewModel.priority.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val audioFile by viewModel.audioFile.collectAsState()
    val audioState by viewModel.audioState.collectAsState()

    // --- Hooks y Variables de UI ---
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scrollState = rememberScrollState() // Para scroll vertical
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    // Lanzador para seleccionar imágenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? -> viewModel.onImageSelected(uri) }
    )
    // Estado y controlador de permiso de audio
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var permissionRequested by rememberSaveable { mutableStateOf(false) } // Para mensaje post-denegación

    // --- Estados y Lógica para DatePickerDialog de Compose ---
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        selectableDates = object : SelectableDates { // Opcional: Restringir fechas
            val nowMillis = Instant.now().toEpochMilli()
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= nowMillis - 86400000L // Desde ayer (por zonas horarias)
            override fun isSelectableYear(year: Int): Boolean =
                year >= LocalDate.now().year
        }
    )

    // --- Efectos Secundarios ---
    // Reacciona al resultado del guardado (éxito/error)
    LaunchedEffect(saveResult) {
        when (val result = saveResult) {
            is AddEditTaskViewModel.SaveResult.Success -> { onTaskSaved() }
            is AddEditTaskViewModel.SaveResult.Error -> {
                snackbarHostState.showSnackbar(result.message, duration = SnackbarDuration.Short)
                viewModel.resetSaveState()
            }
            is AddEditTaskViewModel.SaveResult.Idle -> {}
        }
    }
    // Reacciona a errores del estado de audio
    LaunchedEffect(audioState) {
        if (audioState is AudioState.Error) {
            snackbarHostState.showSnackbar((audioState as AudioState.Error).message, duration = SnackbarDuration.Short)
            // Considera resetear el estado de error en el ViewModel si es necesario
            // viewModel.resetAudioErrorState()
        }
    }

    // --- Diálogo de Calendario de Compose (se muestra condicionalmente) ---
    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.onDateChange(selectedDate)
                        }
                        showDatePickerDialog = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState) // El calendario real
        }
    }

    // --- UI Principal con Scaffold ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Añadir Nueva Tarea") }, // O "Editar Tarea"
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) { Text("Atrás") } // Sin icono
                }
            )
        }
    ) { paddingValues ->
        // Columna principal scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding del Scaffold
                .padding(horizontal = 16.dp) // Padding lateral
                .verticalScroll(scrollState) // Habilita scroll
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Espacio superior

            // --- Título ---
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Título *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- Cuerpo/Descripción ---
            OutlinedTextField(
                value = bodyText,
                onValueChange = viewModel::onBodyChange,
                label = { Text("Descripción (Opcional)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), // Altura mínima
                enabled = !isLoading,
                singleLine = false // Permite múltiples líneas
            )
            Spacer(modifier = Modifier.height(24.dp)) // Más espacio antes de adjuntos

            // --- SECCIÓN DE ADJUNTOS ---
            Text("Adjuntos:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // --- Manejo de Imagen (Sin Iconos) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Imagen:", style = MaterialTheme.typography.bodyLarge)
                Button(
                    onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    enabled = !isLoading && imageUri == null // Habilitado solo si NO hay imagen
                ) {
                    Text("Añadir Imagen") // Solo Texto
                }
            }
            // Muestra la imagen seleccionada y botón para quitarla
            if (imageUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Vista previa de imagen",
                    modifier = Modifier.fillMaxWidth().height(150.dp) // Tamaño de preview
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { viewModel.clearImage() },
                    modifier = Modifier.align(Alignment.End) // Alinear a la derecha
                ) {
                    Text("Quitar Imagen")
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Espacio entre Imagen y Audio

            // --- Manejo de Audio (Sin Iconos) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Espacio entre botones
            ) {
                Text("Audio:", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f)) // El texto ocupa el espacio sobrante
                // Botones cambian según el estado del audio
                when (audioState) {
                    AudioState.Idle -> {
                        if (audioFile == null) { // No hay audio
                            Button(onClick = { // Botón Grabar
                                if (audioPermissionState.status.isGranted) { viewModel.startRecording(context) }
                                else { permissionRequested = true; audioPermissionState.launchPermissionRequest() }
                            }, enabled = !isLoading) {
                                Text("Grabar Audio")
                            }
                        } else { // Hay archivo pero está idle (quizás por error previo)
                            TextButton(onClick = { viewModel.playAudio() }, enabled = !isLoading) { Text("Reproducir") }
                            TextButton(onClick = { viewModel.deleteAudio() }, enabled = !isLoading) { Text("Eliminar") }
                        }
                    }
                    AudioState.Recording -> { // Grabando...
                        Button(onClick = { viewModel.stopRecording() }, enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Text("Detener Grabación")
                        }
                        // Podrías añadir un Text("(Grabando...)") aquí si quieres
                    }
                    AudioState.Recorded -> { // Grabación lista
                        TextButton(onClick = { viewModel.playAudio() }, enabled = !isLoading) { Text("Reproducir") }
                        TextButton(onClick = { viewModel.deleteAudio() }, enabled = !isLoading) { Text("Eliminar") }
                    }
                    AudioState.Playing -> { // Reproduciendo...
                        TextButton(onClick = { viewModel.stopPlayback() }, enabled = !isLoading) { Text("Detener") }
                        // Podrías añadir un Text("(Reproduciendo...)") aquí si quieres
                    }
                    is AudioState.Error -> { // Error
                        Text("Error Audio", color = MaterialTheme.colorScheme.error)
                        if (audioFile != null) { TextButton(onClick = { viewModel.deleteAudio() }, enabled = !isLoading) { Text("Eliminar") } }
                    }
                }
            } // Fin Row Audio

            // Muestra mensaje sobre permiso de audio si fue denegado
            if (permissionRequested && !audioPermissionState.status.isGranted) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (audioPermissionState.status.shouldShowRationale) "Se necesita permiso para grabar audio." else "Permiso denegado. Habilítalo en ajustes.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error
                )
            }
            // --- FIN SECCIÓN ADJUNTOS ---

            Spacer(modifier = Modifier.height(24.dp)) // Espacio antes de Fecha

            // --- Fecha (Corregido con Box para click) ---
            Text("Fecha de Vencimiento (Opcional):", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Box { // Contenedor para manejar el click
                OutlinedTextField(
                    value = dueDate?.format(dateFormatter) ?: "Seleccionar fecha",
                    onValueChange = {}, // No editable
                    label = { /* Etiqueta movida arriba */ },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = !isLoading,
                    interactionSource = remember { MutableInteractionSource() } // Evita foco visual
                )
                // Capa invisible para capturar el click
                Spacer(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(enabled = !isLoading) { showDatePickerDialog = true } // Muestra el diálogo
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // --- FIN FECHA CORREGIDA ---

            // --- Etiqueta ---
            OutlinedTextField(
                value = tag,
                onValueChange = viewModel::onTagChange,
                label = { Text("Etiqueta (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- Prioridad ---
            Text("Prioridad:", style = MaterialTheme.typography.labelLarge)
            Row( modifier = Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.SpaceEvenly ) {
                // Usa .entries si tu Kotlin es >= 1.9, sino .values()
                Priority.entries.forEach { priority ->
                    Row(
                        modifier = Modifier.selectable(selected = (priority == selectedPriority), onClick = { viewModel.onPriorityChange(priority) }, role = Role.RadioButton, enabled = !isLoading).padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (priority == selectedPriority), onClick = null, enabled = !isLoading)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = priority.displayText)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // --- Botones de Acción ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onNavigateBack, // Callback para volver
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) { Text("Cancelar") }
                Button(
                    onClick = { viewModel.onSaveClick() }, // Llama a guardar en ViewModel
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    // Muestra indicador de carga o texto
                    if (isLoading) { CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp) }
                    else { Text("Guardar Tarea") }
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Espacio al final

        } // Fin Column principal
    } // Fin Scaffold
}

// --- Previsualización ---
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun AddEditTaskScreenPreview() {
    TaskMasterTheme {
        AddEditTaskScreen(
            onTaskSaved = { println("Preview: Task Saved") },
            onNavigateBack = { println("Preview: Navigate Back") }
        )
    }
}