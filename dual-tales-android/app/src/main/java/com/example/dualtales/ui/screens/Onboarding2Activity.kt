package com.example.dualtales.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dualtales.MainActivity
import com.example.dualtales.R
import com.example.dualtales.databinding.ActivityOnboarding2Binding
import com.google.android.material.bottomsheet.BottomSheetDialog

class Onboarding2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboarding2Binding
    private var selectedLang1: String? = null
    private var selectedLang2: String? = null

    private val languages = listOf("한국어", "영어", "베트남어", "프랑스어")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboarding2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.llLang1Selector.setOnClickListener {
            showLanguageBottomSheet(1)
        }

        binding.llLang2Selector.setOnClickListener {
            showLanguageBottomSheet(2)
        }

        binding.btnComplete.setOnClickListener {
            if (selectedLang1 != null) {
                getSharedPreferences("dualtales_prefs", MODE_PRIVATE).edit()
                    .putString("language1", selectedLang1)
                    .putString("language2", selectedLang2)
                    .apply()
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        }
    }

    private fun showLanguageBottomSheet(slot: Int) {
        val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetStyle)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_language, null)
        bottomSheet.setContentView(view)

        val container = view.findViewById<LinearLayout>(R.id.ll_language_list)

        languages.forEach { lang ->
            val isDisabled = (slot == 1 && lang == selectedLang2) ||
                             (slot == 2 && lang == selectedLang1)
            val isSelected = (slot == 1 && lang == selectedLang1) ||
                             (slot == 2 && lang == selectedLang2)

            val item = layoutInflater.inflate(R.layout.item_language_option, container, false)
            val tvLang = item.findViewById<TextView>(R.id.tv_language_name)
            val ivCheck = item.findViewById<ImageView>(R.id.iv_check)

            tvLang.text = lang
            tvLang.setTextColor(
                when {
                    isDisabled -> android.graphics.Color.parseColor("#CCCCCC")
                    isSelected -> android.graphics.Color.parseColor("#C383E1")
                    else       -> android.graphics.Color.parseColor("#1A1A1A")
                }
            )
            ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE

            if (!isDisabled) {
                item.setOnClickListener {
                    if (slot == 1) {
                        selectedLang1 = lang
                        binding.tvLang1Selected.text = lang
                        binding.tvLang1Selected.setTextColor(
                            android.graphics.Color.parseColor("#1A1A1A")
                        )
                    } else {
                        selectedLang2 = lang
                        binding.tvLang2Selected.text = lang
                        binding.tvLang2Selected.setTextColor(
                            android.graphics.Color.parseColor("#1A1A1A")
                        )
                    }
                    updateButtonState()
                    bottomSheet.dismiss()
                }
            }
            container.addView(item)
        }
        bottomSheet.show()
    }

    private fun updateButtonState() {
        if (selectedLang1 != null) {
            binding.btnComplete.setBackgroundResource(R.drawable.bg_button_purple)
        } else {
            binding.btnComplete.setBackgroundResource(R.drawable.bg_button_disabled)
        }
    }
}
