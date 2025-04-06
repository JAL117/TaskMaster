package com.example.taskmaster.Login // Asegúrate que el paquete sea el correcto

// --- IMPORTACIONES ---
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay // Aún necesaria si quieres revertir fácilmente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// --- FIN IMPORTACIONES ---

class LoginViewModel : ViewModel() {

    // --- Estados Internos (Privados y Mutables) ---
    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false) // Mantenemos isLoading por si quieres mostrarlo brevemente
    // Estado para comunicar el resultado del login a la UI (éxito, error, idle)
    private val _loginState = MutableStateFlow<LoginResult>(LoginResult.Idle)

    // --- Estados Expuestos (Públicos e Inmutables - StateFlow) ---
    val email: StateFlow<String> = _email.asStateFlow()
    val password: StateFlow<String> = _password.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val loginState: StateFlow<LoginResult> = _loginState.asStateFlow()

    // --- Manejadores de Eventos de la UI ---
    // Estas funciones son llamadas por la UI (LoginScreen)

    fun onEmailChange(newEmail: String) {
        // Actualiza el valor del StateFlow del email
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        // Actualiza el valor del StateFlow de la contraseña
        _password.value = newPassword
    }

    // --- FUNCIÓN onLoginClick MODIFICADA TEMPORALMENTE ---
    fun onLoginClick() {
        // Evita iniciar otro login si ya hay uno en progreso (opcional mantenerlo)
        if (_isLoading.value) {
            return
        }

        // --- ** MODIFICACIÓN TEMPORAL: FORZAR ÉXITO ** ---
        // Opcional: Mostrar brevemente el loading para feedback visual
        // viewModelScope.launch {
        //     _isLoading.value = true
        //     delay(200) // Pequeña demora opcional
        _loginState.value = LoginResult.Success("Navegación directa (temporal)")
        //     _isLoading.value = false // Ocultar loading después de establecer Success
        // }
        // --- ** FIN MODIFICACIÓN TEMPORAL ** ---


        /* --- CÓDIGO ORIGINAL COMENTADO (para referencia futura) ---

        // Validación básica (puedes añadir validación de formato de email, etc.)
        if (email.value.isBlank() || password.value.isBlank()) {
             _loginState.value = LoginResult.Error("Por favor, ingresa email y contraseña.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = LoginResult.Idle // Resetea cualquier estado de error/éxito previo

            // --- SIMULACIÓN DE LLAMADA AL MODELO (Repositorio/API) ---
            try {
                delay(1500) // Simula el tiempo de espera de una llamada de red
                if (email.value.trim().equals("test@test.com", ignoreCase = true) && password.value == "1234") {
                    // Simula un login exitoso
                    _loginState.value = LoginResult.Success("¡Login exitoso!")
                } else {
                    // Simula credenciales inválidas
                    _loginState.value = LoginResult.Error("Credenciales inválidas.")
                }
            } catch (e: Exception) {
                 // Simula un error inesperado durante la llamada
                _loginState.value = LoginResult.Error("Ocurrió un error: ${e.message}")
            } finally {
                // Este bloque se ejecuta siempre, haya éxito o error
                _isLoading.value = false // Oculta el indicador de carga
            }
            // --- FIN DE LA SIMULACIÓN ---
        }
        --- FIN CÓDIGO ORIGINAL COMENTADO --- */

    } // Fin onLoginClick

    // Función para que la UI pueda resetear el estado de 'loginState' después de consumirlo
    // (ej. después de mostrar el Snackbar o navegar)
    fun resetLoginState() {
        _loginState.value = LoginResult.Idle
    }

    // --- Clase Sellada para el Resultado del Login ---
    // Define los posibles estados que el proceso de login puede tener
    sealed class LoginResult {
        object Idle : LoginResult() // Estado inicial o neutro
        data class Success(val message: String? = null) : LoginResult() // Éxito, opcionalmente con mensaje/datos
        data class Error(val message: String) : LoginResult() // Error, siempre con mensaje
    }

} // Fin clase LoginViewModel