package com.example.dualtales.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dualtales.databinding.FragmentProfileBinding
import com.example.dualtales.network.RetrofitClient
import com.example.dualtales.network.UserManager
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    val madeCount = MutableLiveData<Int>(0)

    fun loadMadeCount() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getMyStories()
                if (response.isSuccessful && response.body() != null) {
                    madeCount.value = response.body()!!.size
                }
            } catch (e: Exception) {
                // 실패 시 0 유지
            }
        }
    }
}

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

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

        binding.tvNickname.text = UserManager.getNickname().ifEmpty { "닉네임 없음" }
        binding.tvEmail.text = UserManager.getEmail().ifEmpty { "이메일 없음" }
        binding.tvMadeBooks.text = "만든 동화 책 0권"
        binding.tvReadBooks.text = "읽은 책 0권"

        viewModel.madeCount.observe(viewLifecycleOwner) { count ->
            binding.tvMadeBooks.text = "만든 동화 책 ${count}권"
        }

        viewModel.loadMadeCount()
        setupClickListeners()
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
