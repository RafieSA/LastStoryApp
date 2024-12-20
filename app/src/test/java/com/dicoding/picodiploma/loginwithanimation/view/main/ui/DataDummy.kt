package com.dicoding.picodiploma.loginwithanimation.view.main.ui

import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem

object DataDummy {
    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                photoUrl = "photoUrl + $i",
                createdAt = "createdAt + $i",
                name = "name + $i",
                description = "description + $i",
                lon = i.toDouble(),
                id = i.toString(),
                lat = i.toDouble()
            )
            items.add(story)
        }
        return items
    }
}