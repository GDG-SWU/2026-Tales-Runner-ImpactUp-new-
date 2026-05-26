package com.example.dualtales.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dualtales.databinding.FragmentProfileBinding
import com.example.dualtales.network.UserManager
import com.example.dualtales.ui.screens.Onboarding2Activity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateProfile(
            nickname = UserManager.getNickname().ifEmpty { "닉네임 없음" },
            email = UserManager.getEmail().ifEmpty { "이메일 없음" },
            madeCount = 0,  // TODO: 내 동화 목록 API 연결 후 업데이트
            readCount = 0
        )

        setupClickListeners()
    }

    /**
     * 프로필 정보를 업데이트합니다.
     * 추후 실제 API 데이터 연동 시 이 함수를 호출합니다.
     */
    fun updateProfile(nickname: String, email: String, madeCount: Int, readCount: Int) {
        binding.tvNickname.text = nickname
        binding.tvEmail.text = email
        binding.tvMadeBooks.text = "만든 동화 책 ${madeCount}권"
        binding.tvReadBooks.text = "읽은 책 ${readCount}권"
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "준비 중입니다", Toast.LENGTH_SHORT).show()
        }

        binding.llLanguageSetting.setOnClickListener {
            startActivity(Intent(requireContext(), Onboarding2Activity::class.java))
        }

        binding.llAppSettings.setOnClickListener {
            Toast.makeText(requireContext(), "준비 중입니다", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
