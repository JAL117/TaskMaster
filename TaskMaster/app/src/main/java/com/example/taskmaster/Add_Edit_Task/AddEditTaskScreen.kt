package com.example.taskmaster.AddEditTask // Asegúrate que este sea tu paquete

// --- IMPORTACIONES COMPLETAS Y VERIFICADAS ---
import android.Manifest // Permiso
import android.app.DatePickerDialog // Diálogo de Fecha del Sistema
import android.app.TimePickerDialog // Diálogo de Hora del Sistema
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.DatePicker // Para listener de DatePickerDialog
import android.widget.TimePicker // Para listener de TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable // Para hacer elementos clickables
import androidx.compose.foundation.interaction.MutableInteractionSource // Para TextField readOnly
import androidx.compose.foundation.layout.* // Layouts básicos
import androidx.compose.foundation.rememberScrollState // Para scroll
import androidx.compose.foundation.selection.selectable // Para RadioButton
import androidx.compose.foundation.selection.selectableGroup // Para RadioButton
import androidx.compose.foundation.verticalScroll // Para scroll
import androidx.compose.foundation.text.KeyboardOptions // Opciones de teclado
import androidx.compose.material3.* // Componentes Material 3
import androidx.compose.runtime.* // remember, collectAsState, LaunchedEffect, etc.
import androidx.compose.runtime.saveable.rememberSaveable // Para estado simple persistente
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Para obtener el Context
import androidx.compose.ui.semantics.Role // Para accesibilidad
import androidx.compose.ui.text.input.KeyboardType // Tipos de teclado
import androidx.compose.ui.tooling.preview.Preview // Para Previews
import androidx.compose.ui.unit.dp // Para unidades de dimensión
import androidx.lifecycle.viewmodel.compose.viewModel // Para obtener ViewModel
import coil.compose.AsyncImage // Para cargar imágenes desde URI
import com.example.taskmaster.AddEditTask.AddEditTaskViewModel.AudioState
import com.example.taskmaster.R // Clase R del proyecto
import com.example.taskmaster.data.model.Priority // Modelo Priority
import com.example.taskmaster.ui.theme.TaskMasterTheme // Tema de la app
import com.google.accompanist.permissions.* // Para manejo de permisos
import java.time.Instant // Requerido por DatePicker M3 (aunque no lo usemos ahora)
import java.time.LocalDate
import java.time.LocalDateTime // Usado para estado de fecha/hora
import java.time.LocalTime // Usado para estado de fecha/hora
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
    val dueDateTime by viewModel.dueDateTime.collectAsState() // Observa LocalDateTime
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
    val scrollState = rememberScrollState()
    // Formateadores separados para claridad
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT) }
    val timeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? -> viewModel.onImageSelected(uri) }
    )
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    // --- Lógica para DatePickerDialog y TimePickerDialog del SISTEMA ---
    // Usa la fecha/hora del ViewModel o la actual como punto de partida
    val currentDateTime = dueDateTime ?: LocalDateTime.now()

    val datePickerDialog = remember(context, currentDateTime) { // Recrea si cambia el contexto o la fecha base
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth -> // Listener cuando se selecciona una fecha
                viewModel.onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            currentDateTime.year,
            currentDateTime.monthValue - 1, // Mes en DatePicker es 0-11
            currentDateTime.dayOfMonth
        ).apply {
            // Opcional: Evita seleccionar fechas pasadas
            datePicker.minDate = System.currentTimeMillis() - 86400000L // Permite desde ayer
        }
    }

    val timePickerDialog = remember(context, currentDateTime) { // Recrea si cambia el contexto o la hora base
        TimePickerDialog(
            context,
            { _: TimePicker, hourOfDay: Int, minute: Int -> // Listener cuando se selecciona una hora
                viewModel.onTimeSelected(LocalTime.of(hourOfDay, minute))
            },
            currentDateTime.hour, // Hora inicial
            currentDateTime.minute, // Minuto inicial
            false // false para formato AM/PM, true para 24 horas (ajusta según prefieras)
        )
    }

    // --- Efectos Secundarios ---
    // Reacciona al resultado del guardado
    LaunchedEffect(saveResult) {
        when (val result = saveResult) {
            is AddEditTaskViewModel.SaveResult.Success -> onTaskSaved()
            is AddEditTaskViewModel.SaveResult.Error -> {
                snackbarHostState.showSnackbar(result.message, duration = SnackbarDuration.Short)
                viewModel.resetSaveState()
            }
            is AddEditTaskViewModel.SaveResult.Idle -> {}
        }
    }
    // Reacciona a errores de audio
    LaunchedEffect(audioState) {
        if (audioState is AudioState.Error) {
            snackbarHostState.showSnackbar((audioState as AudioState.Error).message, duration = SnackbarDuration.Short)
            // viewModel.resetAudioErrorState() // Opcional
        }
    }


    // --- UI Principal con Scaffold ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Añadir Nueva Tarea") },
                navigationIcon = { TextButton(onClick = onNavigateBack) { Text("Atrás") } } // Sin icono
            )
        }
    ) { paddingValues ->
        Column( // Columna principal scrollable
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp).verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Título y Cuerpo ---
            OutlinedTextField( value = title, onValueChange = viewModel::onTitleChange, label = { Text("Título *") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isLoading )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField( value = bodyText, onValueChange = viewModel::onBodyChange, label = { Text("Descripción (Opcional)") }, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), enabled = !isLoading, singleLine = false )
            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN DE ADJUNTOS (Sin Iconos) ---
            Text("Adjuntos:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            // Imagen
            Row( modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween ) {
                Text("Imagen:", style = MaterialTheme.typography.bodyLarge)
                Button( onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, enabled = !isLoading && imageUri == null ) { Text("Añadir Imagen") }
            }
            if (imageUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage( model = imageUri, contentDescription = "Vista previa de imagen", modifier = Modifier.fillMaxWidth().height(150.dp) )
                Spacer(modifier = Modifier.height(4.dp))
                TextButton( onClick = { viewModel.clearImage() }, modifier = Modifier.align(Alignment.End) ) { Text("Quitar Imagen") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Audio
            Row( modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp) ) {
                Text("Audio:", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                when (audioState) { // Botones de texto según estado
                    AudioState.Idle -> {
                        if (audioFile == null) { Button(onClick = { if (audioPermissionState.status.isGranted) viewModel.startRecording(context) else { permissionRequested = true; audioPermissionState.launchPermissionRequest() } }, enabled = !isLoading) { Text("Grabar Audio") } }
                        else { TextButton(onClick = { viewModel.playAudio() }, enabled = !isLoading) { Text("Reproducir") }; TextButton(onClick = { viewModel.deleteAudio() }, enabled = !isLoading) { Text("Eliminar") } }
                    }
                    AudioState.Recording -> { Button(onClick = { viewModel.stopRecording() }, enabled = !isLoading, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) { Text("Detener Grabación") } }
                    AudioState.Recorded -> { TextButton(onClick = { viewModel.playAudio() }, enabled = !isLoading) { Text("Reproducir") }; TextButton(onClick = { viewModel.deleteAudio() }, enabled = !isLoading) { Text("Eliminar") } }
                    AudioState.Playing -> { TextButton(onClick = { viewModel.stopPlayback() }, enabled = !isLoading) { Text("Detener") } }
                    is AudioState.Error -> { Text("Error Audio", color = MaterialTheme.colorScheme.error); if (audioFile != null) { TextButton(onClick = { viewModel.deleteAudio() }, enabled = !isLoading) { Text("Eliminar") } } }
                }
            }
            // Mensaje de permiso
            if (permissionRequested && !audioPermissionState.status.isGranted) {
                Spacer(modifier = Modifier.height(8.dp))
                Text( text = if (audioPermissionState.status.shouldShowRationale) "Se necesita permiso para grabar audio." else "Permiso denegado. Habilítalo en ajustes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error )
            }
            Spacer(modifier = Modifier.height(24.dp))
            // --- FIN SECCIÓN ADJUNTOS ---


            // --- **FECHA Y HORA (CON DIÁLOGOS DEL SISTEMA Y BOX CLICKABLE)** ---
            Text("Vencimiento (Opcional):", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Row( modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp) ) {
                // Campo Clickable para Fecha
                Box(modifier = Modifier.weight(1f)) { // Ocupa mitad del espacio
                    OutlinedTextField(
                        value = dueDateTime?.format(dateFormatter) ?: "Fecha", // Muestra fecha formateada
                        onValueChange = {}, readOnly = true, enabled = !isLoading,
                        label = { /* Sin label aquí */ },
                        modifier = Modifier.fillMaxWidth(),
                        interactionSource = remember { MutableInteractionSource() } // Evita foco
                    )
                    // Capa invisible para capturar click y mostrar DatePicker
                    Spacer(modifier = Modifier.matchParentSize().clickable(enabled = !isLoading) { datePickerDialog.show() })
                }
                // Campo Clickable para Hora
                Box(modifier = Modifier.weight(1f)) { // Ocupa la otra mitad
                    OutlinedTextField(
                        value = dueDateTime?.format(timeFormatter) ?: "Hora", // Muestra hora formateada
                        onValueChange = {}, readOnly = true, enabled = !isLoading && dueDateTime != null, // Solo si hay fecha
                        label = { /* Sin label aquí */ },
                        modifier = Modifier.fillMaxWidth(),
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    // Capa invisible para capturar click y mostrar TimePicker
                    Spacer(modifier = Modifier.matchParentSize().clickable(enabled = !isLoading && dueDateTime != null) { timePickerDialog.show() })
                }
                // Botón Limpiar (solo si hay fecha/hora)
                if (dueDateTime != null) {
                    TextButton(onClick = { viewModel.clearDateTime() }, enabled = !isLoading) { Text("Limpiar") }
                } else {
                    // Spacer para mantener alineación si no hay botón Limpiar
                    Spacer(modifier = Modifier.width(64.dp)) // Ancho aprox de TextButton
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // --- **FIN FECHA Y HORA** ---


            // --- Etiqueta ---
            OutlinedTextField( value = tag, onValueChange = viewModel::onTagChange, label = { Text("Etiqueta (Opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !isLoading )
            Spacer(modifier = Modifier.height(24.dp))

            // --- Prioridad ---
            Text("Prioridad:", style = MaterialTheme.typography.labelLarge)
            Row( modifier = Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.SpaceEvenly ) {
                Priority.entries.forEach { priority -> // O .values()
                    Row( modifier = Modifier.selectable(selected = (priority == selectedPriority), onClick = { viewModel.onPriorityChange(priority) }, role = Role.RadioButton, enabled = !isLoading).padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically ) {
                        RadioButton(selected = (priority == selectedPriority), onClick = null, enabled = !isLoading)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = priority.displayText)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // --- Botones de Acción ---
            Row( modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween ) {
                OutlinedButton( onClick = onNavigateBack, enabled = !isLoading, modifier = Modifier.weight(1f).padding(end = 8.dp) ) { Text("Cancelar") }
                Button( onClick = { viewModel.onSaveClick() }, enabled = !isLoading, modifier = Modifier.weight(1f).padding(start = 8.dp) ) {
                    if (isLoading) { CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp) } else { Text("Guardar Tarea") }
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Espacio final

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