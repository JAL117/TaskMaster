package com.example.taskmaster.Register // Asegúrate que este sea tu paquete

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskmaster.ui.theme.TaskMasterTheme // Ajusta si tu tema está en otro lugar
// Necesitarás esta importación si usas KeyboardOptions
import androidx.compose.foundation.text.KeyboardOptions


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    // Callbacks para comunicar hacia afuera (a MainActivity)
    onRegisterSuccess: () -> Unit,
    onNavigateBackToLogin: () -> Unit,
    // Instancia del ViewModel
    registerViewModel: RegisterViewModel = viewModel()
) {
    // Observa el estado del ViewModel
    val name by registerViewModel.name.collectAsState()
    val email by registerViewModel.email.collectAsState()
    val password by registerViewModel.password.collectAsState()
    val confirmPassword by registerViewModel.confirmPassword.collectAsState()
    val isLoading by registerViewModel.isLoading.collectAsState()
    val registerState by registerViewModel.registerState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Efecto para reaccionar a cambios en registerState
    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is RegisterViewModel.RegisterResult.Success -> {
                // Llama al callback para notificar éxito y que MainActivity cambie la pantalla
                onRegisterSuccess()
                registerViewModel.resetRegisterState() // Resetea estado en ViewModel
            }
            is RegisterViewModel.RegisterResult.Error -> {
                // Muestra error en Snackbar
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                registerViewModel.resetRegisterState() // Resetea estado en ViewModel
            }
            is RegisterViewModel.RegisterResult.Idle -> { /* No hacer nada */ }
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
            // Permite scroll si el contenido es muy largo (útil en pantallas pequeñas)
            // .verticalScroll(rememberScrollState())
            verticalArrangement = Arrangement.Center // O Arrangement.Top si prefieres empezar desde arriba
        ) {
            Text(
                text = "Registro de Usuario",
                style = MaterialTheme.typography.headlineMedium // Un poco más pequeño que el login
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = registerViewModel::onNameChange,
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = registerViewModel::onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = registerViewModel::onPasswordChange,
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = registerViewModel::onConfirmPasswordChange,
                label = { Text("Confirmar Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading,
                // Opcional: Marcar error si no coinciden mientras escribe
                isError = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword
            )
            // Opcional: Mostrar texto de ayuda si las contraseñas no coinciden
            // if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
            //    Text("Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            //}


            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { registerViewModel.onRegisterClick() },
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
                    Text("Registrarse")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para volver a Login
            TextButton(
                onClick = {
                    if (!isLoading) { // Evita navegar si está en medio de un registro
                        onNavigateBackToLogin()
                    }
                }
            ) {
                Text("¿Ya tienes cuenta? Inicia Sesión")
            }
        }
    }
}


// --- Previsualización ---
@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    TaskMasterTheme {
        RegisterScreen(
            onRegisterSuccess = { println("Preview: Register Success!") },
            onNavigateBackToLogin = { println("Preview: Navigate Back to Login!") }
        )
    }
}

