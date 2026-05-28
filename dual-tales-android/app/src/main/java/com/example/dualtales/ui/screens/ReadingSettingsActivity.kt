package com.example.dualtales.ui.screens

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dualtales.MainActivity
import com.example.dualtales.databinding.ActivityReadingSettingsBinding
import com.example.dualtales.network.RetrofitClient
import kotlinx.coroutines.launch
import android.content.Intent
import com.example.dualtales.R

class ReadingSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadingSettingsBinding
    private lateinit var prefs: SharedPreferences
    private var storyId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("reading_settings", MODE_PRIVATE)
        storyId = intent.getLongExtra("story_id", 0L)

        setupToolbar()
        loadSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener { finish() }
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

        binding.llChangeTitle.setOnClickListener {
            if (storyId == 0L) {
                Toast.makeText(this, "동화 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showChangeTitleDialog()
        }

        binding.llDeleteStory.setOnClickListener {
            if (storyId == 0L) {
                Toast.makeText(this, "동화 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showDeleteConfirmDialog()
        }
    }

    private fun showChangeTitleDialog() {
        val editText = EditText(this).apply {
            hint = "새 제목을 입력해주세요"
            setPadding(48, 32, 48, 32)
            typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.pretendard_medium)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("제목 변경")
            .setView(editText)
            .setPositiveButton("변경") { _, _ ->
                val newTitle = editText.text.toString().trim()
                if (newTitle.isEmpty()) {
                    Toast.makeText(this, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                changeTitle(newTitle)
            }
            .setNegativeButton("취소", null)
            .show()

        // 제목, 버튼 폰트 변경
        val pretendard = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.pretendard_bold)
        val pretendardMedium = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.pretendard_medium)
        dialog.findViewById<android.widget.TextView>(androidx.appcompat.R.id.alertTitle)?.typeface = pretendard
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.typeface = pretendardMedium
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.typeface = pretendardMedium
    }

    private fun showDeleteConfirmDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("동화 삭제")
            .setMessage("정말 삭제하시겠어요?\n삭제된 동화는 복구할 수 없어요.")
            .setPositiveButton("삭제") { _, _ ->
                deleteStory()
            }
            .setNegativeButton("취소", null)
            .show()

        val pretendard = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.pretendard_bold)
        val pretendardMedium = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.pretendard_medium)
        dialog.findViewById<android.widget.TextView>(androidx.appcompat.R.id.alertTitle)?.typeface = pretendard
        dialog.findViewById<android.widget.TextView>(android.R.id.message)?.typeface = pretendardMedium
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.typeface = pretendardMedium
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.typeface = pretendardMedium
    }

    private fun changeTitle(newTitle: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.updateTitle(storyId, newTitle)
                if (response.isSuccessful) {
                    Toast.makeText(this@ReadingSettingsActivity, "제목이 변경되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ReadingSettingsActivity, "제목 변경에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ReadingSettingsActivity, "서버 연결에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteStory() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.deleteStory(storyId)
                if (response.isSuccessful) {
                    Toast.makeText(this@ReadingSettingsActivity, "동화가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@ReadingSettingsActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("tab", "bookshelf")
                    })
                    finish()
                } else {
                    Toast.makeText(this@ReadingSettingsActivity, "삭제에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ReadingSettingsActivity, "서버 연결에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
