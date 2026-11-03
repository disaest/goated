package com.example.goated.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goated.data.AuthRepository
import com.example.goated.data.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Форма входа и регистрации. После успеха сессия меняется, и навигация показывает профиль. */
class AuthViewModel : ViewModel() {

    data class UiState(
        val isSignUp: Boolean = false,
        val email: String = "",
        val login: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun onEmailChange(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onLoginChange(v: String) = _state.update { it.copy(login = v, error = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onConfirmChange(v: String) = _state.update { it.copy(confirmPassword = v, error = null) }

    fun toggleMode() = _state.update {
        it.copy(isSignUp = !it.isSignUp, error = null, password = "", confirmPassword = "")
    }

    fun submit() {
        val s = _state.value
        if (s.loading) return
        val validationError = validate(s)
        if (validationError != null) {
            _state.update { it.copy(error = validationError) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                if (s.isSignUp) AuthRepository.signUp(s.email, s.login, s.password)
                else AuthRepository.signIn(s.login, s.password)
                _state.value = UiState()
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUserMessage()) }
            }
        }
    }

    private fun validate(s: UiState): String? {
        val login = s.login.trim()
        if (s.isSignUp) {
            if (!EMAIL_REGEX.matches(s.email.trim())) return "Введите корректный email"
            if (!USERNAME_REGEX.matches(login)) return "Логин: 3–20 символов, латиница, цифры и _"
            if (s.password.length < 6) return "Пароль должен быть не короче 6 символов"
            if (s.password != s.confirmPassword) return "Пароли не совпадают"
        } else {
            if (login.isEmpty()) return "Введите логин или email"
            if (s.password.isEmpty()) return "Введите пароль"
        }
        return null
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        val USERNAME_REGEX = Regex("^[A-Za-z0-9_]{3,20}$")
    }
}
