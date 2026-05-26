package com.example.dualtales.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dualtales.R
import com.example.dualtales.databinding.ActivityVoiceInputBinding

class VoiceInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceInputBinding
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isListening = false
    private var currentLanguageIndex = 0
    private lateinit var availableLanguages: List<String>

    private val languageCodeMap = mapOf(
        "한국어"  to "ko-KR",
        "영어"    to "en-US",
        "베트남어" to "vi-VN",
        "프랑스어" to "fr-FR"
    )

    companion object {
        const val REQUEST_VOICE_INPUT = 100
        private const val REQUEST_RECORD_PERMISSION = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent extra 수신
        val stepNumber    = intent.getIntExtra("step_number", 1)
        val questionLabel = intent.getStringExtra("question_label") ?: "질문 1"
        val questionText  = intent.getStringExtra("question_text") ?: ""
        val questionBgRes = intent.getIntExtra("question_bg_res", R.drawable.bg_question_purple)

        // SharedPreferences에서 선택된 언어 로드
        val prefs = getSharedPreferences("dualtales_prefs", MODE_PRIVATE)
        val lang1 = prefs.getString("language1", "한국어") ?: "한국어"
        val lang2 = prefs.getString("language2", null)
        availableLanguages = if (lang2 != null) listOf(lang1, lang2) else listOf(lang1)

        binding.tvCurrentLanguage.text = availableLanguages[0]

        // 스텝 인디케이터 구성
        setupStepIndicator(stepNumber)

        // 질문 카드 설정
        binding.llQuestion.setBackgroundResource(questionBgRes)
        binding.tvQuestionLabel.text = questionLabel
        binding.tvQuestionText.text  = questionText

        setupSpeechRecognizer()
        setupClickListeners()
        checkAudioPermission()
    }

    private fun setupStepIndicator(stepNumber: Int) {
        binding.llIndicator.removeAllViews()
        val density = resources.displayMetrics.density
        val dp10  = (10 * density).toInt()
        val dp17  = (17 * density).toInt()
        val dp10m = (10 * density).toInt()
        val dp3   = ( 3 * density).toInt()

        for (i in 1..stepNumber) {
            val isActive = (i == stepNumber)
            val size = if (isActive) dp17 else dp10
            val params = LinearLayout.LayoutParams(size, size)
            if (i > 1) params.marginStart = dp10m
            if (!isActive) params.topMargin = dp3
            val iv = ImageView(this).apply {
                layoutParams = params
                setImageResource(
                    if (isActive) R.drawable.ic_step_active else R.drawable.ic_step_inactive
                )
            }
            binding.llIndicator.addView(iv)
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                updateVoiceState()
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                updateVoiceState()
            }

            override fun onError(error: Int) {
                isListening = false
                updateVoiceState()
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH       -> "음성을 인식하지 못했어요"
                    SpeechRecognizer.ERROR_NETWORK        -> "네트워크 오류가 발생했어요"
                    SpeechRecognizer.ERROR_AUDIO          -> "마이크 오류가 발생했어요"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성이 감지되지 않았어요"
                    else                                  -> "오류가 발생했어요"
                }
                Toast.makeText(this@VoiceInputActivity, errorMsg, Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val current = binding.tvVoiceText.text.toString()
                    val newText = if (current.isEmpty()) {
                        matches[0]
                    } else {
                        "$current ${matches[0]}"
                    }
                    binding.tvVoiceText.text = newText
                    binding.tvVoiceText.setTextColor(Color.parseColor("#151515"))
                }
                isListening = false
                updateVoiceState()
                binding.btnVoiceComplete.visibility = View.VISIBLE
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!partial.isNullOrEmpty()) {
                    binding.tvVoiceText.text = partial[0]
                    binding.tvVoiceText.setTextColor(Color.parseColor("#C383E1"))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun buildRecognizerIntent(): Intent {
        val languageCode = languageCodeMap[availableLanguages[currentLanguageIndex]] ?: "ko-KR"
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }

    private fun startListening() {
        binding.tvVoiceText.text = ""
        binding.btnVoiceComplete.visibility = View.GONE
        speechRecognizer.startListening(buildRecognizerIntent())
        isListening = true
        updateVoiceState()
    }

    private fun stopListening() {
        speechRecognizer.stopListening()
        isListening = false
        updateVoiceState()
    }

    private fun updateVoiceState() {
        if (isListening) {
            binding.tvVoiceText.setTextColor(Color.parseColor("#C383E1"))
            binding.tvVoiceStatus.visibility = View.VISIBLE
            binding.tvVoiceStatus.text =
                "${availableLanguages[currentLanguageIndex]}로 듣는 중"
            binding.btnVoiceComplete.visibility = View.GONE
            binding.btnMicToggle.setImageResource(R.drawable.img_voice_listening)
            binding.btnMicToggle.setBackgroundResource(R.drawable.bg_mic_button)
            binding.btnMicToggle.setPadding(24, 24, 24, 24)
        } else {
            binding.tvVoiceStatus.visibility = View.GONE
            binding.btnMicToggle.setImageResource(R.drawable.img_voice_done)
            binding.btnMicToggle.setBackgroundResource(0)
            binding.btnMicToggle.setPadding(0, 0, 0, 0)
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnMicToggle.setOnClickListener {
            if (isListening) stopListening() else startListening()
        }

        binding.btnLanguageSwitch.setOnClickListener {
            currentLanguageIndex = (currentLanguageIndex + 1) % availableLanguages.size
            binding.tvCurrentLanguage.text = availableLanguages[currentLanguageIndex]
            if (isListening) {
                speechRecognizer.stopListening()
                speechRecognizer.startListening(buildRecognizerIntent())
                binding.tvVoiceStatus.text =
                    "${availableLanguages[currentLanguageIndex]}로 듣는 중"
            }
        }

        binding.btnVoiceComplete.setOnClickListener {
            val resultText = binding.tvVoiceText.text.toString()
            val resultIntent = Intent().apply {
                putExtra("voice_result", resultText)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_PERMISSION
            )
        } else {
            startListening()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startListening()
        } else {
            Toast.makeText(this, "마이크 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
