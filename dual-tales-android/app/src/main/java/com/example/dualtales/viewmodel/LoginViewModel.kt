package com.example.dualtales.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dualtales.network.RetrofitClient
import com.example.dualtales.network.dto.LoginRequestDto
import com.example.dualtales.network.TokenManager
import com.example.dualtales.network.UserManager
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    val email = MutableLiveData("")
    val password = MutableLiveData("")

    val isLoginEnabled = androidx.lifecycle.MediatorLiveData<Boolean>().apply {
        addSource(email) { value = isValid() }
        addSource(password) { value = isValid() }
    }

    private fun isValid(): Boolean {
        val e = email.value.orEmpty()
        val p = password.value.orEmpty()
        return e.isNotBlank() && p.isNotBlank()
    }

    sealed class LoginState {
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    val loginState = MutableLiveData<LoginState>()

    fun login() {
        val e = email.value.orEmpty()
        val p = password.value.orEmpty()

        viewModelScope.launch {
            loginState.value = LoginState.Loading
            try {
                val response = RetrofitClient.api.login(LoginRequestDto(e, p))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    val token = body.accessToken
                    if (!token.isNullOrEmpty()) {
                        TokenManager.saveToken(token)
                    }
                    // 유저 정보 저장 추가
                    UserManager.saveUser(body.id, body.email, body.nickname)
                    loginState.value = LoginState.Success
                } else {
                    loginState.value = LoginState.Error("이메일 또는 비밀번호를 확인해주세요")
                }
            } catch (ex: Exception) {
                loginState.value = LoginState.Error("서버 연결에 실패했습니다")
            }
        }
    }
}
