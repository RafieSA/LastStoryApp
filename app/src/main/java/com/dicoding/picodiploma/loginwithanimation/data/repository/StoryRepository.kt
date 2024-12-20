package com.dicoding.picodiploma.loginwithanimation.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiService
import com.dicoding.picodiploma.loginwithanimation.data.response.FileUploadResponse
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.response.LoginResponse
import com.dicoding.picodiploma.loginwithanimation.view.addstory.StoryPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class StoryRepository private constructor(
    private val apiService: ApiService
) : IStoryRepository {

    fun login(email: String, pass: String): LiveData<Result<LoginResponse>> = liveData(Dispatchers.IO) {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, pass).execute()
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null && !loginResponse.error) {
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

    override fun getStories(token: String): Flow<PagingData<ListStoryItem>> {
        Log.d("StoryRepository", "Token: $token")
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40
            ),
            pagingSourceFactory = { StoryPagingSource(apiService, token) }
        ).flow
    }

    suspend fun getStoriesWithLocation(token: String): Result<List<ListStoryItem>> {
        return try {
            val storyResponse = apiService.getStoriesWithLocation("Bearer $token", "1")
            if (!storyResponse.error!!) {
                Result.Success(storyResponse.listStory)
            } else {
                Result.Error(storyResponse.message ?: "Unknown error")
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    override suspend fun uploadStory(token: String, imageMultipart: MultipartBody.Part, description: RequestBody): Result<FileUploadResponse> {
        return try {
            val response = apiService.uploadStory(token, imageMultipart, description)
            if (!response.error) {
                Result.Success(response)
            } else {
                Result.Error(response.message)
            }
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(
            apiService: ApiService
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService)
            }.also { instance = it }
    }
}