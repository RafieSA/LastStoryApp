package com.dicoding.picodiploma.loginwithanimation.data.pref

import kotlinx.coroutines.flow.Flow

interface IUserPreference {
    fun getSession(): Flow<UserModel>
}