package com.bankingsystem.mobile.ui.register

import com.bankingsystem.mobile.data.storage.TokenManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bankingsystem.mobile.data.config.RetrofitClient
import com.bankingsystem.mobile.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val message: String) : RegisterState()
    data class Error(val error: String) : RegisterState()
}

class RegisterViewModel(
    context: Context,
    private val repository: UserRepository = UserRepository(
        apiService = RetrofitClient.apiService,
        tokenManager = TokenManager(context)
    )
) : ViewModel() {

    private val _usernameAvailable = MutableStateFlow<Boolean?>(null)
    val usernameAvailable: StateFlow<Boolean?> = _usernameAvailable

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private var checkUsernameJob: Job? = null

    fun checkUsernameAvailability(username: String) {
        checkUsernameJob?.cancel()
        _usernameAvailable.value = null
        if (username.length < 3) {
            _usernameAvailable.value = false
            return
        }
        checkUsernameJob = viewModelScope.launch {
            delay(500) // debounce
            val available = repository.checkUsernameAvailability(username)
            _usernameAvailable.value = available
        }
    }

    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val success = repository.registerUser(username, email, password)
            if (success) {
                _registerState.value = RegisterState.Success("Registration successful")
            } else {
                _registerState.value = RegisterState.Error("Registration failed, please try again")
            }
        }
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }

}
