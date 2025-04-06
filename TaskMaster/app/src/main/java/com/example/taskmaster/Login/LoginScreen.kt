package com.example.taskmaster.Login // Asegúrate que este sea tu paquete

// --- IMPORTACIONES NECESARIAS ---
import androidx.compose.foundation.Image // Para mostrar el logo
import androidx.compose.foundation.layout.*
import androidx.compose.material3.* // Import base de Material 3
import androidx.compose.runtime.* // Para remember, collectAsState, LaunchedEffect, etc.
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource // Para cargar la imagen del logo
import androidx.compose.ui.text.input.PasswordVisualTransformation // Para ocultar contraseña
import androidx.compose.ui.tooling.preview.Preview // Para la previsualización
import androidx.compose.ui.unit.dp // Para especificar tamaños y paddings
import androidx.lifecycle.viewmodel.compose.viewModel // Para obtener el ViewModel
import com.example.taskmaster.R // Importa la clase R de tu proyecto (para R.drawable...)
import com.example.taskmaster.ui.theme.TaskMasterTheme // Importa tu tema
// --- FIN IMPORTACIONES ---


@OptIn(ExperimentalMaterial3Api::class) // Necesario para Scaffold y otros componentes M3
@Composable
fun LoginScreen(
    // Callback que se ejecuta en MainActivity cuando el login es exitoso
    onLoginSuccess: () -> Unit,
    // Callback que se ejecuta en MainActivity para ir a la pantalla de registro
    onNavigateToRegister: () -> Unit,
    // Obtiene (o crea) la instancia del ViewModel asociada
    loginViewModel: LoginViewModel = viewModel()
) {
    // --- Observación del Estado del ViewModel ---
    val email by loginViewModel.email.collectAsState()
    val password by loginViewModel.password.collectAsState()
    val isLoading by loginViewModel.isLoading.collectAsState()
    val loginState by loginViewModel.loginState.collectAsState() // Estado del resultado (Idle, Success, Error)

    // Estado para controlar el Snackbar (mensajes temporales)
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Efecto Secundario: Reacciona a cambios en loginState ---
    // Se ejecuta cada vez que loginState cambia
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginViewModel.LoginResult.Success -> {
                // Si el ViewModel indica éxito, llama al callback onLoginSuccess
                // que (en MainActivity) cambiará la pantalla al Menú.
                onLoginSuccess()
                // Resetea el estado en el ViewModel para evitar re-ejecutar esto si hay recomposición
                loginViewModel.resetLoginState()
            }
            is LoginViewModel.LoginResult.Error -> {
                // Si el ViewModel indica error, muestra el mensaje en el Snackbar
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                // Resetea el estado en el ViewModel
                loginViewModel.resetLoginState()
            }
            is LoginViewModel.LoginResult.Idle -> {
                // No hacer nada en el estado inicial o reseteado
            }
        }
    } // Fin LaunchedEffect

    // --- UI Principal ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) } // Define dónde mostrar SnackBar
    ) { paddingValues -> // Padding aplicado por el Scaffold (ej. si hubiera TopBar)
        Column(
            modifier = Modifier
                .fillMaxSize() // Ocupa toda la pantalla
                .padding(paddingValues) // Aplica padding del Scaffold
                .padding(horizontal = 24.dp), // Padding lateral adicional
            horizontalAlignment = Alignment.CenterHorizontally, // Centra elementos horizontalmente
            verticalArrangement = Arrangement.Center // Centra el contenido verticalmente
        ) {

            // --- Logo ---
            Image(
                // **IMPORTANTE:** Asegúrate que 'app_logo' exista en res/drawable
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo de TaskMaster", // Accesibilidad
                modifier = Modifier.size(150.dp) // Tamaño del logo
            )
            Spacer(modifier = Modifier.height(24.dp)) // Espacio bajo el logo
            // --- Fin Logo ---


            // --- Título ---
            Text(
                text = "TaskMaster",
                style = MaterialTheme.typography.headlineLarge, // Estilo grande
                color = MaterialTheme.colorScheme.primary // Color del tema
            )
            Spacer(modifier = Modifier.height(32.dp)) // Espacio bajo el título
            // --- Fin Título ---


            // --- Campo Email ---
            OutlinedTextField(
                value = email, // Vinculado al estado del ViewModel
                onValueChange = loginViewModel::onEmailChange, // Notifica cambios al ViewModel
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(), // Ocupa todo el ancho
                singleLine = true, // Campo de una línea
                enabled = !isLoading // Deshabilitado mientras carga
            )
            Spacer(modifier = Modifier.height(16.dp)) // Espacio entre campos
            // --- Fin Campo Email ---


            // --- Campo Contraseña ---
            OutlinedTextField(
                value = password, // Vinculado al estado del ViewModel
                onValueChange = loginViewModel::onPasswordChange, // Notifica cambios al ViewModel
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(), // Oculta la contraseña
                singleLine = true,
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(32.dp)) // Espacio antes del botón
            // --- Fin Campo Contraseña ---


            // --- Botón Iniciar Sesión ---
            Button(
                onClick = { loginViewModel.onLoginClick() }, // Llama a la función de login en ViewModel
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading // Deshabilitado mientras carga
            ) {
                // Muestra indicador de progreso o texto del botón
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary, // Color sobre el fondo del botón
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar Sesión")
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Espacio bajo el botón
            // --- Fin Botón Iniciar Sesión ---


            // --- Botón para ir a Registro ---
            TextButton(
                onClick = {
                    // Solo navega si no está cargando
                    if (!isLoading) {
                        onNavigateToRegister() // Llama al callback para ir a Registro
                    }
                }
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }
            // --- Fin Botón Registro ---

        } // Fin Column
    } // Fin Scaffold
}

// --- Previsualización (No cambia) ---
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TaskMasterTheme {
        LoginScreen(
            onLoginSuccess = { println("Preview: Login Success!") },
            onNavigateToRegister = { println("Preview: Navigate to Register!") }
        )
    }
}