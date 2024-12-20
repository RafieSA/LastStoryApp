package com.dicoding.picodiploma.loginwithanimation.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.repository.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.response.LoginResponse
import kotlinx.coroutines.launch

class LoginViewModel(private val pref: UserPreference, private val repository: UserRepository) : ViewModel() {
    fun login(email: String, pass: String): LiveData<Result<LoginResponse>> = repository.login(email, pass)

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            if (user.email.isNotEmpty() && user.name.isNotEmpty() && user.token.isNotEmpty()) {
                pref.saveSession(user)
            }
        }
    }

}