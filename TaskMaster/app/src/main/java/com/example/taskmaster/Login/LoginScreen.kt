package com.example.taskmaster.Login // Asegúrate que este sea tu paquete

import androidx.compose.runtime.* // Para @Composable, remember, collectAsState, LaunchedEffect, etc.
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskmaster.ui.theme.TaskMasterTheme // Ajusta si tu tema está en otro lugar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    // Ya no recibe NavController. En su lugar, recibe lambdas
    // para comunicar eventos hacia afuera (a quien lo llame, ej: MainActivity).
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    // Instancia del ViewModel
    loginViewModel: LoginViewModel = viewModel()
) {
    // Observa el estado del ViewModel
    val email by loginViewModel.email.collectAsState()
    val password by loginViewModel.password.collectAsState()
    val isLoading by loginViewModel.isLoading.collectAsState()
    val loginState by loginViewModel.loginState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Efecto para reaccionar a cambios en loginState (éxito o error)
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginViewModel.LoginResult.Success -> {
                // Llama al callback para notificar el éxito
                onLoginSuccess()
                loginViewModel.resetLoginState() // Resetea estado en ViewModel
            }
            is LoginViewModel.LoginResult.Error -> {
                // Muestra el error en el Snackbar
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                loginViewModel.resetLoginState() // Resetea estado en ViewModel
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
            Text(
                text = "TaskMaster",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(40.dp))

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

            // Llama al callback para ir a Registro
            TextButton(
                onClick = {
                    if (!isLoading) {
                        onNavigateToRegister()
                    }
                }
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }
            // Podrías añadir aquí "Olvidaste contraseña" con otro callback si es necesario
        }
    }
}

// --- Previsualización ---
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TaskMasterTheme {
        // Proporcionamos lambdas vacías para la previsualización
        LoginScreen(
            onLoginSuccess = { println("Preview: Login Success!") },
            onNavigateToRegister = { println("Preview: Navigate to Register!") }
        )
    }
}