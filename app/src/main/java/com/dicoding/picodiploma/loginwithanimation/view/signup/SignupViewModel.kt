package com.dicoding.picodiploma.loginwithanimation.view.signup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.data.repository.UserRepository
import kotlinx.coroutines.launch

class SignupViewModel(private val repository: UserRepository) : ViewModel() {
    private val _registerResult = MutableLiveData<Result<String>>()
    val registerResult = _registerResult

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val result = repository.register(name, email, password)
                result.observeForever { registerResponseResult ->
                    if (registerResponseResult is Result.Success) {
                        _registerResult.value = Result.Success(registerResponseResult.data.message ?: "")
                    } else if (registerResponseResult is Result.Error) {
                        _registerResult.value = Result.Error(registerResponseResult.error)
                    }
                }
            } catch (e: Exception) {
                _registerResult.value = Result.Error("Registration failed: ${e.message}")
            }
        }
    }
}