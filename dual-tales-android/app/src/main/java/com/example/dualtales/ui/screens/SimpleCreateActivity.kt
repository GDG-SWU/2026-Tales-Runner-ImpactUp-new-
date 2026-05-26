package com.example.dualtales.ui.screens

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dualtales.R
import com.example.dualtales.databinding.ActivitySimpleCreateBinding

class SimpleCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySimpleCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기
        binding.llToolbar.findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        // 말로 내용 입력하기
        binding.btnVoice.setOnClickListener {
            @Suppress("DEPRECATION")
            startActivityForResult(
                Intent(this, VoiceInputActivity::class.java).apply {
                    putExtra("step_number", 1)
                    putExtra("question_label", "동화 내용 질문")
                    putExtra("question_text", "내 동화는 어떤 이야기인가요?")
                    putExtra("question_bg_res", R.drawable.bg_question_purple)
                },
                REQUEST_VOICE_INPUT
            )
        }

        // 임시저장
        binding.tvTempSave.setOnClickListener {
            Toast.makeText(this, "임시저장 준비 중입니다", Toast.LENGTH_SHORT).show()
        }

        // 동화 완성하기 버튼
        binding.btnNext.setOnClickListener {
            val answer = binding.etAnswer.text.toString().trim()
            if (answer.isEmpty()) {
                Toast.makeText(this, "내용을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, LoadingActivity::class.java))
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VOICE_INPUT && resultCode == RESULT_OK) {
            val voiceResult = data?.getStringExtra("voice_result") ?: ""
            if (voiceResult.isNotEmpty()) {
                binding.etAnswer.setText(voiceResult)
            }
        }
    }

    companion object {
        const val REQUEST_VOICE_INPUT = 100
    }
}
