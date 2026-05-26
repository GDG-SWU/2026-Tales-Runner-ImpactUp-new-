package com.example.dualtales.ui.screens

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dualtales.R
import com.example.dualtales.databinding.ActivitySignupBinding
import com.example.dualtales.network.RetrofitClient
import com.example.dualtales.network.dto.UserRequestDto
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private var isEmailVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Material3 테마의 강제 tint 제거
        binding.btnEmailCheck.backgroundTintList = null
        binding.btnSignupComplete.backgroundTintList = null

        setupFocusListeners()
        setupEmailCheck()
        setupTextWatchers()
        setupClickListeners()
    }

    private fun setupFocusListeners() {
        listOf(binding.etNickname, binding.etEmail, binding.etPassword).forEach { et ->
            et.setOnFocusChangeListener { _, hasFocus ->
                et.background = ContextCompat.getDrawable(
                    this,
                    if (hasFocus) R.drawable.bg_input_focused else R.drawable.bg_input_field
                )
            }
        }

        binding.etPasswordConfirm.setOnFocusChangeListener { _, hasFocus ->
            val pw = binding.etPassword.text.toString()
            val pwConfirm = binding.etPasswordConfirm.text.toString()
            val isError = pwConfirm.isNotEmpty() && pw != pwConfirm
            when {
                isError -> { /* 오류 상태 유지 */ }
                hasFocus -> binding.etPasswordConfirm.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_input_focused)
                else -> binding.etPasswordConfirm.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_input_field)
            }
        }
    }

    private fun setupEmailCheck() {
        binding.btnEmailCheck.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                isEmailVerified = true
                binding.tvEmailStatus.apply {
                    text = "✓ 사용가능"
                    setTextColor(Color.parseColor("#C383E1"))
                    visibility = View.VISIBLE
                }
            } else {
                isEmailVerified = false
                binding.tvEmailStatus.apply {
                    text = "✗ 사용 불가"
                    setTextColor(Color.parseColor("#FF5252"))
                    visibility = View.VISIBLE
                }
            }
            updateSignupButton()
        }
    }

    private fun setupTextWatchers() {
        binding.etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) = updateSignupButton()
        })

        // 이메일 변경 시 인증 상태 초기화
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (isEmailVerified) {
                    isEmailVerified = false
                    binding.tvEmailStatus.visibility = View.GONE
                }
                updateSignupButton()
            }
        })

        // 비밀번호 변경 시 확인 필드 재검증
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                validatePasswordConfirm()
                updateSignupButton()
            }
        })

        // 비밀번호확인 실시간 검사
        binding.etPasswordConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                validatePasswordConfirm()
                updateSignupButton()
            }
        })
    }

    private fun validatePasswordConfirm() {
        val pw = binding.etPassword.text.toString()
        val pwConfirm = binding.etPasswordConfirm.text.toString()
        if (pwConfirm.isNotEmpty() && pw != pwConfirm) {
            binding.etPasswordConfirm.background =
                ContextCompat.getDrawable(this, R.drawable.bg_input_error)
            binding.tvPasswordError.visibility = View.VISIBLE
        } else {
            binding.etPasswordConfirm.background =
                ContextCompat.getDrawable(this, R.drawable.bg_input_focused)
            binding.tvPasswordError.visibility = View.GONE
        }
    }

    private fun isFormValid(): Boolean {
        val pw = binding.etPassword.text.toString()
        return binding.etNickname.text.toString().isNotEmpty()
                && isEmailVerified
                && pw.isNotEmpty()
                && pw == binding.etPasswordConfirm.text.toString()
    }

    private fun updateSignupButton() {
        binding.btnSignupComplete.background = ContextCompat.getDrawable(
            this,
            if (isFormValid()) R.drawable.bg_button_purple_signup
            else R.drawable.bg_button_gray_disabled
        )
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnSignupComplete.setOnClickListener {
            if (isFormValid()) {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString()
                val nickname = binding.etNickname.text.toString().trim()

                binding.btnSignupComplete.isEnabled = false

                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.api.signUp(
                            UserRequestDto(email, password, nickname)
                        )
                        if (response.isSuccessful) {
                            Toast.makeText(this@SignUpActivity, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else if (response.code() == 400) {
                            Toast.makeText(this@SignUpActivity, "이미 사용중인 이메일입니다", Toast.LENGTH_SHORT).show()
                            binding.btnSignupComplete.isEnabled = true
                        } else {
                            Toast.makeText(this@SignUpActivity, "회원가입에 실패했습니다", Toast.LENGTH_SHORT).show()
                            binding.btnSignupComplete.isEnabled = true
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@SignUpActivity, "서버 연결에 실패했습니다", Toast.LENGTH_SHORT).show()
                        binding.btnSignupComplete.isEnabled = true
                    }
                }
            }
        }
    }
}
