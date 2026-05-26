package com.example.dualtales.ui.screens

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dualtales.databinding.ActivityReadingSettingsBinding

class ReadingSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadingSettingsBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("reading_settings", MODE_PRIVATE)

        setupToolbar()
        loadSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun loadSettings() {
        binding.swSlideMode.isChecked = prefs.getBoolean("slide_mode", true)
        binding.swDarkMode.isChecked = prefs.getBoolean("dark_mode", false)
    }

    private fun setupListeners() {
        binding.llFontSize.setOnClickListener {
            // TODO: 글자 크기 선택 다이얼로그
        }

        binding.swSlideMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("slide_mode", isChecked).apply()
        }

        binding.swDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
        }
    }
}
