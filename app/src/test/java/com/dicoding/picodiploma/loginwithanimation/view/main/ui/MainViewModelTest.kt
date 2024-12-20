package com.dicoding.picodiploma.loginwithanimation.view.main.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.picodiploma.loginwithanimation.data.pref.IUserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.repository.IStoryRepository
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.view.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var userPreference: IUserPreference

    private lateinit var mainViewModel: MainViewModel
    private lateinit var fakeStoryRepository: IStoryRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        fakeStoryRepository = FakeStoryRepository()
        Mockito.`when`(userPreference.getSession()).thenReturn(
            flowOf(UserModel("dummy_token", "name", "dummy_token", true))
        )
        mainViewModel = MainViewModel(fakeStoryRepository, userPreference)
    }

    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        mainViewModel.getStories()
        val actualStory = mainViewModel.storyList.value
        val differ = AsyncPagingDataDiffer(
            diffCallback = DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)
        advanceUntilIdle()
        Assert.assertNotNull(differ.snapshot())
        Assert.assertTrue(differ.snapshot().isNotEmpty())
        Assert.assertEquals(101, differ.snapshot().size)
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        fakeStoryRepository = FakeStoryRepository()
        mainViewModel = MainViewModel(fakeStoryRepository, userPreference)
        mainViewModel.getStories()
        val actualStory = mainViewModel.storyList.value
        val differ = AsyncPagingDataDiffer(
            diffCallback = DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)
        advanceUntilIdle()
        Assert.assertNotNull(differ.snapshot())
        Assert.assertTrue(differ.snapshot().isEmpty())
    }

    companion object {
        val DIFF_CALLBACK = object : androidx.recyclerview.widget.DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }

        val noopListUpdateCallback = object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {}
            override fun onRemoved(position: Int, count: Int) {}
            override fun onMoved(fromPosition: Int, toPosition: Int) {}
            override fun onChanged(position: Int, count: Int, payload: Any?) {}
        }
    }
}