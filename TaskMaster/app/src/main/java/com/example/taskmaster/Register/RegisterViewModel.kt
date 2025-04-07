package com.example.moviles.ui.register.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.Register.RegisterModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class RegisterViewModel(private val registerModel: RegisterModel) : ViewModel() {

    // Campos de texto
    var name by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set

    // Errores de validación
    var errorName by mutableStateOf<String?>(null)
        private set
    var errorEmail by mutableStateOf<String?>(null)
        private set
    var errorPassword by mutableStateOf<String?>(null)
        private set
    var errorConfirmPassword by mutableStateOf<String?>(null)
        private set

    // Estado de navegación
    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin

    // Loading state
    var loading by mutableStateOf(false)
        private set

    // Show error state
    var showError by mutableStateOf(false)
        private set

    // Message error
    var messageError by mutableStateOf("")
        private set
    // Actualizadores de los campos de texto
    fun onNameChanged(value: String) {
        name = value
        errorName = null // Resetea el error al cambiar el valor
    }
    fun onEmailChanged(value: String) {
        email = value
        errorEmail = null // Resetea el error al cambiar el valor
    }
    fun onPasswordChanged(value: String) {
        password = value
        errorPassword = null // Resetea el error al cambiar el valor
    }
    fun onConfirmPasswordChanged(value: String) {
        confirmPassword = value
        errorConfirmPassword = null // Resetea el error al cambiar el valor
    }

    // Lógica del botón de registro
    fun onRegisterButtonClicked() {
        viewModelScope.launch {
            if (validateFields()) {
                // Simulación de registro exitoso (reemplazar con tu lógica real)
                loading = true
                val result = registerModel.registerUser(name, email, password)
                loading = false
                when{
                    result.isSuccess -> {
                        _navigateToLogin.value = true
                    }
                    result.isFailure -> {
                        showError = true
                        messageError = result.exceptionOrNull()?.message ?: "Error desconocido"
                        delay(3000)
                        showError = false
                    }
                }

            }
        }
    }

    // Validaciones
    private fun validateFields(): Boolean {
        var isValid = true

        if (name.isBlank()){
            errorName = "El nombre no puede estar vacío"
            isValid = false
        }
        if (email.isBlank()) {
            errorEmail = "El email no puede estar vacío"
            isValid = false
        } else if (!isValidEmail(email)) {
            errorEmail = "Formato de email incorrecto"
            isValid = false
        }

        if (password.isBlank()) {
            errorPassword = "La contraseña no puede estar vacía"
            isValid = false
        } else if (password.length < 6) {
            errorPassword = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        if (confirmPassword.isBlank()) {
            errorConfirmPassword = "Debes confirmar la contraseña"
            isValid = false
        } else if (password != confirmPassword) {
            errorConfirmPassword = "Las contraseñas no coinciden"
            isValid = false
        }

        return isValid
    }
    // Validacion de email con patron
    private fun isValidEmail(email: String): Boolean {
        val EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return EMAIL_PATTERN.matcher(email).matches()
    }
}