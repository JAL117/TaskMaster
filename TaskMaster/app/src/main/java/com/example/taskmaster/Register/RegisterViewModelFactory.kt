package com.example.taskmaster.Register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.moviles.ui.register.ui.RegisterViewModel
import com.example.taskmaster.Register.RegisterModel

class RegisterViewModelFactory(private val registerModel: RegisterModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(registerModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}