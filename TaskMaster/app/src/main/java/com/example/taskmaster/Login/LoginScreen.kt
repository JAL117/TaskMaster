package com.example.taskmaster.Login // Asegúrate que este sea tu paquete

// --- Añade estas importaciones ---
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.taskmaster.R // <-- Importa la clase R de tu proyecto

// Tus otras importaciones...
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskmaster.ui.theme.TaskMasterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    val email by loginViewModel.email.collectAsState()
    val password by loginViewModel.password.collectAsState()
    val isLoading by loginViewModel.isLoading.collectAsState()
    val loginState by loginViewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(loginState) {
        // ... (tu lógica de LaunchedEffect sigue igual)
        when (val state = loginState) {
            is LoginViewModel.LoginResult.Success -> {
                onLoginSuccess()
                loginViewModel.resetLoginState()
            }
            is LoginViewModel.LoginResult.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                loginViewModel.resetLoginState()
            }
            is LoginViewModel.LoginResult.Idle -> { /* No hacer nada */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // --- INICIO: Añadir Logo ---
            Image(
                // Reemplaza 'app_logo' con el nombre EXACTO de tu archivo en res/drawable
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo de TaskMaster", // Texto para accesibilidad
                modifier = Modifier
                    .size(150.dp) // Ajusta el tamaño según necesites
                // .padding(bottom = 16.dp) // Espacio opcional debajo del logo
            )

            // Añade un espacio entre el logo y el título si lo deseas
            Spacer(modifier = Modifier.height(24.dp))
            // --- FIN: Añadir Logo ---


            Text( // El título que ya tenías
                text = "TaskMaster",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Ajusta este Spacer si añadiste padding al logo o quieres más/menos espacio
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = loginViewModel::onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = loginViewModel::onPasswordChange,
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { loginViewModel.onLoginClick() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar Sesión")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    if (!isLoading) {
                        onNavigateToRegister()
                    }
                }
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}

// --- Previsualización ---
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