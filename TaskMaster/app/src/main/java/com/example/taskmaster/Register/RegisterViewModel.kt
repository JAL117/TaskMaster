package com.example.taskmaster.Register // Asegúrate que este sea tu paquete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay // Para simular espera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    // --- Estados Internos (Privados y Mutables) ---
    private val _name = MutableStateFlow("")
    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _confirmPassword = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    // Estado para comunicar el resultado del registro a la UI (éxito, error, idle)
    private val _registerState = MutableStateFlow<RegisterResult>(RegisterResult.Idle)

    // --- Estados Expuestos (Públicos e Inmutables - StateFlow) ---
    val name: StateFlow<String> = _name.asStateFlow()
    val email: StateFlow<String> = _email.asStateFlow()
    val password: StateFlow<String> = _password.asStateFlow()
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val registerState: StateFlow<RegisterResult> = _registerState.asStateFlow()

    // --- Manejadores de Eventos de la UI ---
    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        // Podrías añadir validación de formato aquí si quieres feedback inmediato
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    fun onRegisterClick() {
        if (_isLoading.value) {
            return // Evita múltiples clics si ya está cargando
        }

        // --- Validaciones ---
        if (name.value.isBlank() || email.value.isBlank() || password.value.isBlank() || confirmPassword.value.isBlank()) {
            _registerState.value = RegisterResult.Error("Todos los campos son obligatorios.")
            return
        }
        // Podrías añadir validación de formato de email aquí (ej. con Regex)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
            _registerState.value = RegisterResult.Error("Formato de email inválido.")
            return
        }
        if (password.value.length < 6) { // Ejemplo: Mínimo 6 caracteres
            _registerState.value = RegisterResult.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }
        if (password.value != confirmPassword.value) {
            _registerState.value = RegisterResult.Error("Las contraseñas no coinciden.")
            return
        }
        // --- Fin Validaciones ---


        viewModelScope.launch {
            _isLoading.value = true
            _registerState.value = RegisterResult.Idle // Resetea estado

            // --- SIMULACIÓN DE LLAMADA AL MODELO (Repositorio/API) ---
            try {
                delay(2000) // Simula tiempo de red

                // Simula un error si el email ya existe (ejemplo)
                if (email.value.trim().equals("test@test.com", ignoreCase = true)) {
                    _registerState.value = RegisterResult.Error("Este email ya está registrado.")
                } else {
                    // Simula registro exitoso
                    _registerState.value = RegisterResult.Success("¡Registro exitoso!")
                }

            } catch (e: Exception) {
                // Simula error inesperado
                _registerState.value = RegisterResult.Error("Ocurrió un error: ${e.message}")
            } finally {
                _isLoading.value = false // Oculta indicador de carga
            }
            // --- FIN SIMULACIÓN ---
        }
    }

    // Para resetear el estado después de que la UI lo consuma
    fun resetRegisterState() {
        _registerState.value = RegisterResult.Idle
    }

    // --- Clase Sellada para el Resultado del Registro ---
    sealed class RegisterResult {
        object Idle : RegisterResult()
        data class Success(val message: String? = null) : RegisterResult()
        data class Error(val message: String) : RegisterResult()
    }
}