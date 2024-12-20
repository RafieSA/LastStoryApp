package com.dicoding.picodiploma.loginwithanimation.view.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.repository.StoryRepository
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
import kotlinx.coroutines.launch

class MapsViewModel(
    private val storyRepository: StoryRepository,
    private val userPreference: UserPreference
) : ViewModel() {
    private val _stories = MutableLiveData<Result<List<ListStoryItem>>>()
    val stories: LiveData<Result<List<ListStoryItem>>> = _stories

    init {
        viewModelScope.launch {
            userPreference.getSession().collect {
                getStoriesWithLocation(it.token)
            }
        }
    }

    private fun getStoriesWithLocation(token: String) = viewModelScope.launch {
        _stories.value = storyRepository.getStoriesWithLocation(token)
    }
}