package com.example.taskmaster.Register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviles.ui.register.ui.RegisterViewModel
import com.example.taskmaster.Register.RegisterViewModelFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import com.example.taskmaster.APIService.ApiService
import com.example.taskmaster.R
import com.example.taskmaster.Register.RegisterModel

@Composable
fun RegisterScreen(
    navigateToLogin: () -> Unit
) {
    //Instancia de retrofit
    val apiService = Retrofit.Builder()
        .baseUrl("http://192.168.252.42:3000")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    //Instancia del modelo
    val registerModel = RegisterModel(apiService)

    // Obtiene la instancia del viewModel utilizando la factory
    val registerViewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(registerModel)
    )

    val navigateToLoginState by registerViewModel.navigateToLogin.collectAsState()
    if (navigateToLoginState) {
        LaunchedEffect(Unit) {
            navigateToLogin()
        }
    }

    val loading = registerViewModel.loading
    val showMessageError = registerViewModel.showError
    val messageError = registerViewModel.messageError

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)  // Aumenta el padding para más espacio alrededor
            .verticalScroll(rememberScrollState()), // Agrega scroll para pantallas pequeñas
        verticalArrangement = Arrangement.spacedBy(16.dp), // Espacio entre elementos
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Crear cuenta", // Título más llamativo
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp)) // Espacio antes de los campos

        InputField(
            value = registerViewModel.name,
            onValueChange = { registerViewModel.onNameChanged(it) },
            label = "Nombre",
            errorMessage = registerViewModel.errorName
        )

        InputField(
            value = registerViewModel.email,
            onValueChange = { registerViewModel.onEmailChanged(it) },
            label = "Email",
            keyboardType = KeyboardType.Email,
            errorMessage = registerViewModel.errorEmail
        )

        PasswordField(
            value = registerViewModel.password,
            onValueChange = { registerViewModel.onPasswordChanged(it) },
            label = "Contraseña",
            errorMessage = registerViewModel.errorPassword
        )

        PasswordField(
            value = registerViewModel.confirmPassword,
            onValueChange = { registerViewModel.onConfirmPasswordChanged(it) },
            label = "Confirmar contraseña",
            errorMessage = registerViewModel.errorConfirmPassword,
            imeAction = ImeAction.Done // Cierra el teclado después de este campo
        )

        Spacer(modifier = Modifier.height(24.dp))

        RegisterButton(
            onClick = { registerViewModel.onRegisterButtonClicked() },
            loading = loading
        )

        if (showMessageError) {
            ErrorMessage(message = messageError)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),

            singleLine = true,
            shape = RoundedCornerShape(12.dp), // Bordes más redondeados
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = if (passwordVisible) painterResource(id = R.drawable.ic_visibility_off) else painterResource(id = R.drawable.ic_visibility),
                        contentDescription = description
                    )
                }
            },
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun RegisterButton(
    onClick: () -> Unit,
    loading: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
        enabled = !loading,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text(
                text = "Registrarse",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 8.dp)
    )
}