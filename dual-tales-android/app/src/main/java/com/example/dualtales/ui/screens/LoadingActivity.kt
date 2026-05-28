package com.example.dualtales.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dualtales.MainActivity
import com.example.dualtales.R
import com.example.dualtales.databinding.ActivityLoadingBinding
import com.example.dualtales.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadingBinding
    private var draftId = 0L
    private var isPolling = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        draftId = intent.getLongExtra("draft_id", 0L)

        setupVideo()
        setupButton()
        startPolling()
    }

    private fun setupVideo() {
        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.loading_character}")
        binding.vvLoading.setVideoURI(videoUri)
        binding.vvLoading.setOnPreparedListener { mp ->
            mp.isLooping = true
            binding.vvLoading.start()
        }
    }

    private fun setupButton() {
        binding.btnRead.isEnabled = false
        binding.btnRead.setBackgroundResource(R.drawable.bg_button_gray)
    }

    private fun startPolling() {
        lifecycleScope.launch {
            var attempts = 0
            val maxAttempts = 20  // 최대 20번 (5초 간격 × 20 = 최대 100초 대기)

            while (isPolling && attempts < maxAttempts) {
                android.util.Log.d("LOADING", "polling start, draftId=$draftId")
                delay(5000L)  // 5초 대기
                attempts++
                android.util.Log.d("LOADING", "polling attempt $attempts")

                try {
                    val response = RetrofitClient.api.getMyStories()
                    android.util.Log.d("LOADING", "code=${response.code()}, size=${response.body()?.size}")

                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        val latestStory = response.body()!!.first()
                        android.util.Log.d("LOADING", "storyId=${latestStory.id}, title=${latestStory.title}")
                        isPolling = false
                        onLoadingComplete(latestStory.id, latestStory.title)
                        return@launch
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LOADING", "polling error: ${e.message}")
                }
            }

            // 최대 시도 초과 시
            if (isPolling) {
                Toast.makeText(this@LoadingActivity, "동화 생성에 시간이 걸리고 있어요. 책장에서 확인해주세요!", Toast.LENGTH_LONG).show()
                onLoadingComplete(0L, "")
            }
        }
    }

    private fun onLoadingComplete(storyId: Long, title: String) {
        binding.vvLoading.stopPlayback()
        binding.vvLoading.visibility = View.GONE
        binding.tvTitle.visibility = View.GONE
        binding.tvSubtitle.visibility = View.GONE
        binding.llComplete.visibility = View.VISIBLE
        binding.btnRead.isEnabled = true
        binding.btnRead.backgroundTintList = null
        binding.btnRead.setBackgroundResource(R.drawable.bg_button_purple)

        binding.llComplete.alpha = 0f
        binding.llComplete.animate().alpha(1f).setDuration(400).start()
        binding.btnRead.alpha = 0f
        binding.btnRead.animate().alpha(1f).setDuration(400).setStartDelay(200).start()

        binding.btnRead.setOnClickListener {
            if (storyId != 0L) {
                startActivity(Intent(this, ReadingActivity::class.java).apply {
                    putExtra("story_id", storyId)
                    putExtra("book_title", title)
                })
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isPolling = false
        binding.vvLoading.stopPlayback()
    }
}
