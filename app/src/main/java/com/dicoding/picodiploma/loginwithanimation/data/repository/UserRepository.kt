package com.dicoding.picodiploma.loginwithanimation.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiService
import com.dicoding.picodiploma.loginwithanimation.data.response.LoginResponse
import com.dicoding.picodiploma.loginwithanimation.data.response.RegisterResponse
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException

class UserRepository private constructor(
    private val apiService: ApiService
) {
    fun register(name: String, email: String, pass: String): LiveData<Result<RegisterResponse>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)
        try {
            val response = apiService.register(name, email, pass).execute().body()
            if (response != null && response.error == false) {
                emit(Result.Success(response))
            } else {
                emit(Result.Error(response?.message ?: "Register failed"))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            emit(Result.Error(errorBody.toString()))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun login(email: String, pass: String): LiveData<Result<LoginResponse>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, pass).execute()
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null && !loginResponse.error && loginResponse.loginResult != null) {
                    emit(Result.Success(loginResponse))
                } else {
                    emit(Result.Error(loginResponse?.message ?: "Login failed"))
                }
            } else {
                emit(Result.Error("Login failed"))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            emit(Result.Error(errorBody.toString()))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService)
            }.also { instance = it }
    }
}