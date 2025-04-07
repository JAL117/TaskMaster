package com.example.taskmaster.Login // Asegúrate que este sea tu paquete


import androidx.compose.foundation.Image // Para mostrar el logo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.* // Import base de Material 3
import androidx.compose.runtime.* // Para remember, collectAsState, LaunchedEffect, etc.
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // Para cargar la imagen del logo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation // Para ocultar contraseña
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview // Para la previsualización
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Para obtener el ViewModel
import com.example.taskmaster.R
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavHostController = rememberNavController(),
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(context)
    )

    val scope = rememberCoroutineScope()
    val navigateToHome by viewModel.navigateToHome.collectAsState(initial = false)

    LaunchedEffect(navigateToHome) {
        if (navigateToHome) {
            navController.navigate("home_screen") {
                popUpTo("login_screen") { inclusive = true }
            }
            onLoginSuccess() // Llama a la lambda cuando la navegación es exitosa
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Añade un padding general
        ) {
            Login(Modifier.align(Alignment.Center), viewModel, navController, onLoginSuccess = onLoginSuccess) // Pasa la lambda
        }
    }
}

@Composable
fun Login(modifier: Modifier, viewModel: LoginViewModel, navController: NavHostController, onLoginSuccess: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth() // Permite que la columna ocupe el ancho completo disponible
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp) // Aumenta el espacio entre elementos
    ) {
        HeaderImage(Modifier.size(120.dp)) // Tamaño fijo para la imagen
        Text(
            text = "Bienvenido", // Título más llamativo
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary // Color del tema
        )
        Text(
            text = "Inicia sesión para continuar", // Subtítulo
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) // Color atenuado
        )
        EmailField(viewModel)
        PasswordField(viewModel)
        LoginButton(viewModel, onLoginSuccess = onLoginSuccess) // Pasa la lambda
        NoAccountButton(navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailField(viewModel: LoginViewModel) {
    val errorEmail = viewModel.errorEmail
    OutlinedTextField(
        value = viewModel.email,
        onValueChange = { viewModel.onEmailChanged(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = "Correo electrónico") }, // Label más amigable
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        maxLines = 1,
        isError = errorEmail != null,
        supportingText = {
            if (errorEmail != null) {
                Text(text = errorEmail, color = MaterialTheme.colorScheme.error)
            }
        },
        shape = RoundedCornerShape(8.dp) // Bordes más suaves
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordField(viewModel: LoginViewModel) {
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = viewModel.password,
        onValueChange = { viewModel.onPasswordChanged(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = "Contraseña") }, // Label más amigable
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        maxLines = 1,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter = if (passwordVisible) painterResource(id = R.drawable.ic_visibility_off) else painterResource(
                        id = R.drawable.ic_visibility
                    ),
                    contentDescription = description,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Icono más sutil
                )
            }
        },
        shape = RoundedCornerShape(8.dp) // Bordes más suaves
    )
}

@Composable
fun LoginButton(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    val scope = rememberCoroutineScope()
    Button(
        onClick = {
            scope.launch {
                viewModel.onLoginButtonClicked()
                onLoginSuccess() // Llama a la lambda
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // Altura ligeramente mayor
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)), // Sombra sutil
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = "Iniciar sesión",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp // Tamaño de fuente más grande
        )
    }
}

@Composable
fun NoAccountButton(navController: NavHostController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = { navController.navigate("register_screen") }) {
            Text(text = "¿No tienes cuenta? Regístrate")
        }
    }
}

@Composable
fun HeaderImage(modifier: Modifier) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "Header",
        modifier = modifier,
    )
}