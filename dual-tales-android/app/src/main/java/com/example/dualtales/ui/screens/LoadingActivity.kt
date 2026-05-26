package com.example.dualtales.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.dualtales.R
import com.example.dualtales.databinding.ActivityLoadingBinding

class LoadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupVideo()
        setupButton()
        startFakeLoading() // TODO: AI 연동 시 실제 API 응답으로 교체
    }

    private fun setupVideo() {
        val videoUri = Uri.parse(
            "android.resource://${packageName}/${R.raw.loading_character}"
        )
        binding.vvLoading.setVideoURI(videoUri)
        binding.vvLoading.setOnPreparedListener { mp ->
            mp.isLooping = true
            binding.vvLoading.start()
        }
    }

    private fun setupButton() {
        // 초기 비활성화 상태
        binding.btnRead.isEnabled = false
        binding.btnRead.setBackgroundResource(R.drawable.bg_button_gray)

        binding.btnRead.setOnClickListener {
            // 더미 데이터로 ReadingActivity 이동
            val intent = Intent(this, ReadingActivity::class.java).apply {
                putExtra("story_id", -1L) // 더미용
                putExtra("book_title", "모모와 노란 공의 풍덩!")
                putExtra("use_dummy", true)
            }
            startActivity(intent)
        }
    }

    private fun startFakeLoading() {
        // TODO: AI/백엔드 연동 시 이 부분을 실제 API 완료 콜백으로 교체
        // 현재는 3초 후 완료 상태로 전환 (테스트용)
        binding.root.postDelayed({
            onLoadingComplete()
        }, 3000L)
    }

    private fun onLoadingComplete() {
        // 로딩 상태 전부 숨기기
        binding.vvLoading.stopPlayback()
        binding.vvLoading.visibility = View.GONE
        binding.tvTitle.visibility = View.GONE
        binding.tvSubtitle.visibility = View.GONE

        // 완료 뷰(아이콘 + 텍스트) 표시
        binding.llComplete.visibility = View.VISIBLE

        // 버튼 활성화
        binding.btnRead.isEnabled = true
        binding.btnRead.backgroundTintList = null
        binding.btnRead.setBackgroundResource(R.drawable.bg_button_purple)

        // 페이드인 애니메이션
        binding.llComplete.alpha = 0f
        binding.llComplete.animate()
            .alpha(1f)
            .setDuration(400)
            .start()

        binding.btnRead.alpha = 0f
        binding.btnRead.animate()
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(200)
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.vvLoading.stopPlayback()
    }
}
