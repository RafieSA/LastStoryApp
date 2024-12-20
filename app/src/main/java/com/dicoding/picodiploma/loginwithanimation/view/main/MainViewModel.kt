package com.dicoding.picodiploma.loginwithanimation.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.data.pref.IUserPreference
import com.dicoding.picodiploma.loginwithanimation.data.repository.IStoryRepository
import com.dicoding.picodiploma.loginwithanimation.data.response.FileUploadResponse
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MainViewModel(
    private val storyRepository: IStoryRepository,
    private val userPreference: IUserPreference
) : ViewModel() {

    private val _storyList = MutableStateFlow<PagingData<ListStoryItem>>(PagingData.empty())
    val storyList: StateFlow<PagingData<ListStoryItem>> = _storyList.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PagingData.empty()
    )

    private val _uploadResult = MutableLiveData<Result<FileUploadResponse>>()
    val uploadResult: LiveData<Result<FileUploadResponse>> = _uploadResult

    init {
        getStories()
    }

    fun getStories() {
        viewModelScope.launch {
            val token = userPreference.getSession().first().token
            storyRepository.getStories(token).cachedIn(viewModelScope).collect { pagingData ->
                _storyList.value = pagingData
            }
        }
    }

    fun uploadStory(token: String, imageMultipart: MultipartBody.Part, description: RequestBody) {
        viewModelScope.launch {
            _uploadResult.value = Result.Loading
            try {
                val response = storyRepository.uploadStory("Bearer $token", imageMultipart, description)
                _uploadResult.value = response
            } catch (e: Exception) {
                _uploadResult.value = Result.Error(e.message.toString())
            }
        }
    }
}