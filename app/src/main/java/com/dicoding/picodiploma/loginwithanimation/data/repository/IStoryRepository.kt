package com.dicoding.picodiploma.loginwithanimation.data.repository

import androidx.paging.PagingData
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.data.response.FileUploadResponse
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface IStoryRepository {
    fun getStories(token: String): Flow<PagingData<ListStoryItem>>
    suspend fun uploadStory(token: String, imageMultipart: MultipartBody.Part, description: RequestBody): Result<FileUploadResponse>
}