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
import com.example.dualtales.databinding.ActivityCreateStep2Binding
import com.example.dualtales.network.RetrofitClient
import com.example.dualtales.network.dto.StoryAnswerRequest
import com.example.dualtales.network.dto.StoryDraftResponseDto
import kotlinx.coroutines.launch

class CreateStep2ViewModel : ViewModel() {

    sealed class AnswerState {
        object Loading : AnswerState()
        data class Success(val response: StoryDraftResponseDto) : AnswerState()
        data class Error(val message: String) : AnswerState()
    }

    val answerState = MutableLiveData<AnswerState>()

    fun sendAnswer(draftId: Long, answer: String) {
        viewModelScope.launch {
            answerState.value = AnswerState.Loading
            try {
                val response = RetrofitClient.api.proceedDraft(
                    draftId, StoryAnswerRequest(answer)
                )
                if (response.isSuccessful && response.body() != null) {
                    answerState.value = AnswerState.Success(response.body()!!)
                } else {
                    answerState.value = AnswerState.Error("서버 오류: ${response.code()}")
                }
            } catch (e: Exception) {
                answerState.value = AnswerState.Error("서버 연결에 실패했습니다")
            }
        }
    }
}

class CreateStep2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateStep2Binding
    private val viewModel: CreateStep2ViewModel by viewModels()

    private var draftId = 0L
    private var langCode = ""
    private var targetAge = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityCreateStep2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.llToolbar) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, statusBarHeight, view.paddingRight, view.paddingBottom)
            insets
        }

        // Step1에서 넘어온 데이터 받기
        draftId = intent.getLongExtra("draft_id", 0L)
        langCode = intent.getStringExtra("lang_code") ?: ""
        targetAge = intent.getIntExtra("target_age", 4)
        val questionKo = intent.getStringExtra("question_ko") ?: ""
        val questionForeign = intent.getStringExtra("question_foreign") ?: ""

        // 서버에서 받은 질문 표시
        binding.tvQuestionText.text = questionKo
        // 외국어 질문은 추후 별도 뷰 추가 시 연결

        val currentStep = intent.getIntExtra("current_step", 1)
        binding.tvQuestionLabel.text = "질문 ${currentStep}"

        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.answerState.observe(this) { state ->
            when (state) {
                is CreateStep2ViewModel.AnswerState.Loading -> {
                    binding.btnNext.isEnabled = false
                    binding.btnNext.text = "전송 중..."
                }
                is CreateStep2ViewModel.AnswerState.Success -> {
                    val next = state.response
                    binding.btnNext.isEnabled = true
                    binding.btnNext.text = "다음"
                    binding.etAnswer.setText("")

                    if (next.isFinal) {
                        val intent = Intent(this, LoadingActivity::class.java).apply {
                            putExtra("draft_id", draftId)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        binding.tvQuestionText.text = next.question_ko
                        binding.tvQuestionLabel.text = "질문 ${next.currentStep}"
                        binding.etAnswer.setText("")
                    }
                }
                is CreateStep2ViewModel.AnswerState.Error -> {
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
                    putExtra("step_number", 2)
                    putExtra("question_label", binding.tvQuestionLabel.text.toString())
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
            viewModel.sendAnswer(draftId, answer)
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
