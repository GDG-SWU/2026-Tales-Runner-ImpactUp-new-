package com.example.dualtales.ui.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dualtales.R
import com.example.dualtales.databinding.ActivityCreateStep4Binding
import com.example.dualtales.network.RetrofitClient
import com.example.dualtales.network.dto.StoryAnswerRequest
import kotlinx.coroutines.launch

class CreateStep4ViewModel : ViewModel() {

    sealed class AnswerState {
        object Loading : AnswerState()
        object Success : AnswerState()
        data class Error(val message: String) : AnswerState()
    }

    val answerState = MutableLiveData<AnswerState>()

    fun sendFinalAnswer(draftId: Long, answer: String) {
        viewModelScope.launch {
            answerState.value = AnswerState.Loading
            try {
                val response = RetrofitClient.api.proceedDraft(
                    draftId, StoryAnswerRequest(answer)
                )
                if (response.isSuccessful) {
                    answerState.value = AnswerState.Success
                } else {
                    answerState.value = AnswerState.Error("서버 오류: ${response.code()}")
                }
            } catch (e: Exception) {
                answerState.value = AnswerState.Error("서버 연결에 실패했습니다")
            }
        }
    }
}

class CreateStep4Activity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateStep4Binding
    private val viewModel: CreateStep4ViewModel by viewModels()

    private var draftId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityCreateStep4Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.llToolbar) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, statusBarHeight, view.paddingRight, view.paddingBottom)
            insets
        }

        draftId = intent.getLongExtra("draft_id", 0L)
        val questionKo = intent.getStringExtra("question_ko") ?: ""
        val questionForeign = intent.getStringExtra("question_foreign") ?: ""

        binding.tvQuestionText.text = questionKo
        // 외국어 질문은 추후 별도 뷰 추가 시 연결

        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.answerState.observe(this) { state ->
            when (state) {
                is CreateStep4ViewModel.AnswerState.Loading -> {
                    binding.btnNext.isEnabled = false
                    binding.btnNext.text = "전송 중..."
                }
                is CreateStep4ViewModel.AnswerState.Success -> {
                    // 모든 답변 완료 → 로딩 화면으로 이동 (AI가 동화 생성 중)
                    val intent = Intent(this, LoadingActivity::class.java).apply {
                        putExtra("draft_id", draftId)
                    }
                    startActivity(intent)
                    finish()
                }
                is CreateStep4ViewModel.AnswerState.Error -> {
                    binding.btnNext.isEnabled = true
                    binding.btnNext.text = "다음"
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnVoice.setOnClickListener {
            @Suppress("DEPRECATION")
            startActivityForResult(
                Intent(this, VoiceInputActivity::class.java).apply {
                    putExtra("step_number", 4)
                    putExtra("question_label", "질문 3")
                    putExtra("question_text", binding.tvQuestionText.text.toString())
                    putExtra("question_bg_res", R.drawable.bg_question_purple)
                },
                REQUEST_VOICE_INPUT
            )
        }

        binding.btnNext.setOnClickListener {
            val answer = binding.etAnswer.text.toString().trim()
            if (answer.isEmpty()) {
                Toast.makeText(this, "답변을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // TODO: 백엔드 연결 후 viewModel.sendFinalAnswer()로 교체
            val intent = Intent(this, LoadingActivity::class.java).apply {
                putExtra("draft_id", draftId)
            }
            startActivity(intent)
            finish()
        }

        binding.tvTempSave.setOnClickListener {
            // TODO: 임시저장 추후 구현
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
