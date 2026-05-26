package com.example.dualtales.ui.screens

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dualtales.MainActivity
import com.example.dualtales.R
import com.example.dualtales.databinding.ActivityLoginBinding
import com.example.dualtales.network.TokenManager
import com.example.dualtales.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTitle()
        setupTextWatchers()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupTitle() {
        val fullText = "듀얼 테일즈에 오신 걸 환영합니다"
        val spannable = SpannableString(fullText)
        val purple = ContextCompat.getColor(this, R.color.main_purple)
        // "듀얼 테일즈" = 인덱스 0~6 (공백 포함 6글자)
        spannable.setSpan(ForegroundColorSpan(purple), 0, 6, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvTitle.text = spannable
    }

    private fun setupTextWatchers() {
        val watcher = { liveData: androidx.lifecycle.MutableLiveData<String> ->
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    liveData.value = s?.toString().orEmpty()
                }
            }
        }
        binding.etEmail.addTextChangedListener(watcher(viewModel.email))
        binding.etPassword.addTextChangedListener(watcher(viewModel.password))
    }

    private fun observeViewModel() {
        viewModel.isLoginEnabled.observe(this) { enabled ->
            binding.btnLogin.isEnabled = enabled
            binding.btnLogin.backgroundTintList = if (enabled) {
                null
            } else {
                ColorStateList.valueOf(Color.parseColor("#C2C2C2"))
            }
        }

        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = "로그인 중..."
                }
                is LoginViewModel.LoginState.Success -> {
                    startActivity(Intent(this, Onboarding1Activity::class.java))
                    finish()
                }
                is LoginViewModel.LoginState.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "로그인"
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            viewModel.login()
        }

        binding.llSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.llFindAccount.setOnClickListener {
            Toast.makeText(this, "준비 중입니다", Toast.LENGTH_SHORT).show()
        }
    }
}
