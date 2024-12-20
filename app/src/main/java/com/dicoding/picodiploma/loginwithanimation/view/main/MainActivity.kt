package com.dicoding.picodiploma.loginwithanimation.view.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMainBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.addstory.AddStoryActivity
import com.dicoding.picodiploma.loginwithanimation.view.maps.MapsActivity
import com.dicoding.picodiploma.loginwithanimation.view.welcome.WelcomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var userPreference: UserPreference
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var listStoryAdapter: ListStoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(dataStore)
        listStoryAdapter = ListStoryAdapter()

        binding.rvStories.layoutManager = LinearLayoutManager(this)
        binding.rvStories.adapter = listStoryAdapter

        setupViewModel()

        binding.fabMaps.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
        binding.fabLogout.setOnClickListener {
            lifecycleScope.launch {
                userPreference.logout()
            }
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            launcherIntentAddStory.launch(intent)
        }
    }

    private val launcherIntentAddStory = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            lifecycleScope.launch {
                mainViewModel.storyList.collectLatest { pagingData ->
                    listStoryAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun setupViewModel() {
        lifecycleScope.launch {
            userPreference.getSession().collect { user ->
                if (!user.isLogin) {
                    startActivity(Intent(this@MainActivity, WelcomeActivity::class.java))
                    finish()
                } else {
                    lifecycleScope.launch {
                        mainViewModel.storyList.collectLatest { pagingData ->
                            listStoryAdapter.submitData(pagingData)
                        }
                    }
                }
            }
        }
    }
}