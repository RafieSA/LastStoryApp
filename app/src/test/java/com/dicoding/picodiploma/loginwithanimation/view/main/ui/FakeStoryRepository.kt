package com.dicoding.picodiploma.loginwithanimation.view.main.ui

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.data.repository.IStoryRepository
import com.dicoding.picodiploma.loginwithanimation.data.response.FileUploadResponse
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class FakeStoryRepository : IStoryRepository {
    private val dummyStories = DataDummy.generateDummyStoryResponse()

    override fun getStories(token: String): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 2 * 20
            ),
            pagingSourceFactory = {
                FakeStoryPagingSource(dummyStories)
            }
        ).flow
    }

    override suspend fun uploadStory(token: String, imageMultipart: MultipartBody.Part, description: RequestBody): Result<FileUploadResponse> {
        return Result.Success(FileUploadResponse(false, "Story uploaded successfully"))
    }
}

class FakeStoryPagingSource(private val dummyStories: List<ListStoryItem>) :
    PagingSource<Int, ListStoryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val start = position * params.loadSize
            val end = minOf((start + params.loadSize), dummyStories.size)

            LoadResult.Page(
                data = dummyStories.subList(start, end),
                prevKey = if (position == 0) null else position - 1,
                nextKey = if (end >= dummyStories.size) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 0
    }
}