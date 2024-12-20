package com.dicoding.picodiploma.loginwithanimation.view.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val story = intent.getParcelableExtra("story", ListStoryItem::class.java)

        binding.tvDetailName.text = story?.name
        binding.tvDetailDescription.text = story?.description
        Glide.with(this)
            .load(story?.photoUrl)
            .into(binding.ivDetailPhoto)
    }
}