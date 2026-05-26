package com.example.dualtales.ui.screens

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
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
import com.example.dualtales.databinding.ActivityCreateStep1Binding
import com.example.dualtales.network.RetrofitClient
import com.example.dualtales.network.dto.StoryDraftCreateRequest
import com.example.dualtales.network.dto.StoryDraftResponseDto
import kotlinx.coroutines.launch

class CreateStep1ViewModel : ViewModel() {

    sealed class DraftState {
        object Loading : DraftState()
        data class Success(val response: StoryDraftResponseDto) : DraftState()
        data class Error(val message: String) : DraftState()
    }

    val draftState = MutableLiveData<DraftState>()

    fun startDraft(langCode: String, age: Int) {
        viewModelScope.launch {
            draftState.value = DraftState.Loading
            try {
                val response = RetrofitClient.api.startDraft(
                    StoryDraftCreateRequest(langCode, age)
                )
                if (response.isSuccessful && response.body() != null) {
                    draftState.value = DraftState.Success(response.body()!!)
                } else {
                    draftState.value = DraftState.Error("서버 오류: ${response.code()}")
                }
            } catch (e: Exception) {
                draftState.value = DraftState.Error("서버 연결에 실패했습니다")
            }
        }
    }
}

class CreateStep1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateStep1Binding
    private val viewModel: CreateStep1ViewModel by viewModels()

    private var selectedAge = 4           // 기본값: 3~5세 → 4
    private var selectedLangCode = "EN"   // 기본값: 영어

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityCreateStep1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.llToolbar) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, statusBarHeight, view.paddingRight, view.paddingBottom)
            insets
        }

        binding.etLanguage.isFocusable = false
        binding.etLanguage.isClickable = true
        binding.etLanguage.setOnClickListener { showLanguageDropdown(it) }
        binding.llSearch.setOnClickListener { showLanguageDropdown(it) }

        updateAgeSelection("3~5세")
        observeViewModel()
        setupClickListeners()
    }

    private fun updateAgeSelection(selected: String) {
        listOf(binding.btnAge35, binding.btnAge68, binding.btnAgeOther).forEach { btn ->
            btn.backgroundTintList = null
            btn.setBackgroundResource(R.drawable.bg_age_unselected)
            btn.setTextColor(Color.parseColor("#2F2F2F"))
        }

        val selectedBtn = when (selected) {
            "3~5세" -> { selectedAge = 4; binding.btnAge35 }
            "6~8세" -> { selectedAge = 7; binding.btnAge68 }
            else    -> { selectedAge = 10; binding.btnAgeOther }
        }
        selectedBtn.backgroundTintList = null
        selectedBtn.setBackgroundResource(R.drawable.bg_age_selected)
        selectedBtn.setTextColor(Color.WHITE)
    }

    private fun observeViewModel() {
        viewModel.draftState.observe(this) { state ->
            when (state) {
                is CreateStep1ViewModel.DraftState.Loading -> {
                    binding.btnNext.isEnabled = false
                    binding.btnNext.text = "생성 중..."
                }
                is CreateStep1ViewModel.DraftState.Success -> {
                    val draft = state.response
                    val intent = Intent(this, CreateStep2Activity::class.java).apply {
                        putExtra("draft_id", draft.draftId)
                        putExtra("question_ko", draft.question_ko)
                        putExtra("question_foreign", draft.question_foreign)
                        putExtra("current_step", draft.currentStep)
                        putExtra("is_final", draft.isFinal)
                        putExtra("lang_code", selectedLangCode)
                        putExtra("target_age", selectedAge)
                    }
                    startActivity(intent)
                    binding.btnNext.isEnabled = true
                    binding.btnNext.text = "다음"
                }
                is CreateStep1ViewModel.DraftState.Error -> {
                    binding.btnNext.isEnabled = true
                    binding.btnNext.text = "다음"
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnAge35.setOnClickListener { updateAgeSelection("3~5세") }
        binding.btnAge68.setOnClickListener { updateAgeSelection("6~8세") }
        binding.btnAgeOther.setOnClickListener { updateAgeSelection("그외") }

        binding.btnNext.setOnClickListener {
            // TODO: 백엔드 연결 후 viewModel.startDraft()로 교체
            // 더미 데이터로 Step2로 이동
            val dummyQuestions = listOf(
                Pair("안녕! 네가 제일 좋아하는 친구는 누구야?", "ねえ、一番好きな友達は誰かな？"),
                Pair("모모는 다른 강아지들이랑 뭐가 다를까?", "モモは他のワンちゃんと何が違うかな？"),
                Pair("우리 모모는 무엇을 할 때 제일 신나고 재미있어 할까?", "ももちゃんは、どんなことをしている時が一番ワクワクして楽しいかな？")
            )
            val intent = Intent(this, CreateStep2Activity::class.java).apply {
                putExtra("draft_id", 1L)
                putExtra("question_ko", dummyQuestions[0].first)
                putExtra("question_foreign", dummyQuestions[0].second)
                putExtra("current_step", 1)
                putExtra("is_final", false)
                putExtra("lang_code", selectedLangCode)
                putExtra("target_age", selectedAge)
            }
            startActivity(intent)
        }

        binding.tvTempSave.setOnClickListener {
            // TODO: 임시저장 추후 구현
        }
    }

    private fun showLanguageDropdown(anchorView: View) {
        val languages = listOf(
            Pair("영어", "EN"),
            Pair("일본어", "JA"),
            Pair("프랑스어", "FR")
        )

        val listPopupWindow = ListPopupWindow(this)
        listPopupWindow.anchorView = anchorView
        listPopupWindow.width = anchorView.width
        listPopupWindow.isModal = true

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            languages.map { it.first }
        )
        listPopupWindow.setAdapter(adapter)

        listPopupWindow.setOnItemClickListener { _, _, position, _ ->
            selectedLangCode = languages[position].second
            binding.etLanguage.setText(languages[position].first)
            listPopupWindow.dismiss()
        }

        listPopupWindow.show()
    }
}
