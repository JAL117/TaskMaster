package com.example.taskmaster.Login // Asegúrate que este sea tu paquete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay // Para simular espera
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    // --- Estados Internos (Privados y Mutables) ---
    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Idle)

    // --- Estados Expuestos (Públicos e Inmutables - StateFlow) ---
    val email: StateFlow<String> = _email.asStateFlow()
    val password: StateFlow<String> = _password.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val loginState: StateFlow<LoginResult> = _loginState.asStateFlow()

    // --- Manejadores de Eventos de la UI ---
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onLoginClick() {
        if (_isLoading.value) {
            return
        }
        if (email.value.isBlank() || password.value.isBlank()) {
            _loginState.value = LoginResult.Error("Por favor, ingresa email y contraseña.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = LoginResult.Idle // Resetear estado

            // --- SIMULACIÓN DE LLAMADA AL MODELO ---
            try {
                delay(2000) // Simula espera de red
                if (email.value.trim() == "test@test.com" && password.value == "1234") {
                    _loginState.value = LoginResult.Success("¡Login exitoso!")
                } else {
                    _loginState.value = LoginResult.Error("Credenciales inválidas.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginResult.Error("Ocurrió un error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
            // --- FIN SIMULACIÓN ---
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginResult.Idle
    }

    // --- Clase Sellada para el Resultado del Login ---
    sealed class LoginResult {
        object Idle : LoginResult()
        data class Success(val message: String? = null) : LoginResult() // Puede llevar datos opcionales
        data class Error(val message: String) : LoginResult()
    }
}