package com.example.dualtales.ui.screens

import android.content.Intent
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.example.dualtales.MainActivity
import com.example.dualtales.R
import com.example.dualtales.databinding.ActivityReadingBinding
import com.example.dualtales.network.RetrofitClient
import com.example.dualtales.network.dto.StoryDetailResponseDto
import kotlinx.coroutines.launch

class ReadingViewModel : ViewModel() {

    sealed class StoryState {
        object Loading : StoryState()
        data class Success(val story: StoryDetailResponseDto) : StoryState()
        data class Error(val message: String) : StoryState()
    }

    val storyState = MutableLiveData<StoryState>()

    fun loadStory(storyId: Long) {
        viewModelScope.launch {
            storyState.value = StoryState.Loading
            try {
                val response = RetrofitClient.api.getStoryDetail(storyId)
                if (response.isSuccessful && response.body() != null) {
                    storyState.value = StoryState.Success(response.body()!!)
                } else {
                    storyState.value = StoryState.Error("동화를 불러오지 못했습니다")
                }
            } catch (e: Exception) {
                storyState.value = StoryState.Error("서버 연결에 실패했습니다")
            }
        }
    }
}

class ReadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadingBinding
    private val viewModel: ReadingViewModel by viewModels()

    private var currentPage = 1
    private var totalPages = 0
    private var isShowingTranslation = false
    private var storyId = 0L
    private var storyData: StoryDetailResponseDto? = null

    companion object {
        private const val PREF_NAME = "dualtales_prefs"
        private const val KEY_GUIDE_SHOWN = "reading_guide_shown"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storyId = intent.getLongExtra("story_id", 0L)
        val bookTitle = intent.getStringExtra("book_title") ?: "동화책"

        setupToolbar(bookTitle)
        setupNavButtons()
        setupTextCardFlip()

        observeViewModel()
        if (storyId != 0L) {
            viewModel.loadStory(storyId)
        }
    }

    private fun observeViewModel() {
        viewModel.storyState.observe(this) { state ->
            when (state) {
                is ReadingViewModel.StoryState.Loading -> {
                    binding.tvPageNumber.text = "로딩 중..."
                }
                is ReadingViewModel.StoryState.Success -> {
                    storyData = state.story
                    totalPages = state.story.contents.size
                    currentPage = 1
                    updatePage()
                    setupGuideOverlay()
                }
                is ReadingViewModel.StoryState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updatePage() {
        val contents = storyData?.contents ?: return
        if (contents.isEmpty()) return

        val page = contents.getOrNull(currentPage - 1) ?: return

        // 한국어 텍스트
        binding.tvStoryText.text = page.content_ko

        // 외국어 텍스트 (카드 뒷면)
        binding.tvStoryTextTranslated.text = page.content_foreign

        // 이미지 로딩
        android.util.Log.d("READING", "page=${currentPage}, image_url=${page.image_url}")
        android.util.Log.d("READING", "content_ko=${page.content_ko?.take(20)}")

        if (!page.image_url.isNullOrEmpty()) {
            Glide.with(this)
                .load(page.image_url)
                .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(e: com.bumptech.glide.load.engine.GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>, isFirstResource: Boolean): Boolean {
                        android.util.Log.e("READING", "Glide load failed: ${e?.message}")
                        return false
                    }
                    override fun onResourceReady(resource: android.graphics.drawable.Drawable, model: Any, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
                        android.util.Log.d("READING", "Glide load success!")
                        return false
                    }
                })
                .into(binding.ivIllustration)
        }

        // 언어 전환 초기화 (앞면으로)
        binding.llTextCardFront.visibility = View.VISIBLE
        binding.llTextCardBack.visibility = View.GONE
        isShowingTranslation = false

        updatePageBadge()
    }

    private fun setupToolbar(bookTitle: String) {
        binding.tvBookTitle.text = bookTitle

        binding.tvExit.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("tab", "bookshelf")
            }
            startActivity(intent)
            finish()
        }
        binding.tvSettings.setOnClickListener {
            startActivity(Intent(this, ReadingSettingsActivity::class.java).apply {
                putExtra("story_id", storyId)
            })
        }
    }

    private fun setupNavButtons() {
        binding.btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                updatePage()
            }
        }
        binding.btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                updatePage()
            }
        }
    }

    private fun updatePageBadge() {
        binding.tvPageNumber.text = "$currentPage / $totalPages"
    }

    private fun setupGuideOverlay() {
        val isFirstVisit = !getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .getBoolean(KEY_GUIDE_SHOWN, false)

        if (!isFirstVisit) return

        binding.ivIllustration.post {
            val rootLoc = IntArray(2)
            binding.root.getLocationOnScreen(rootLoc)

            val imageLoc = IntArray(2)
            binding.ivIllustration.getLocationOnScreen(imageLoc)

            val left = (imageLoc[0] - rootLoc[0]).toFloat()
            val top = (imageLoc[1] - rootLoc[1]).toFloat()
            val right = left + binding.ivIllustration.width
            val bottom = top + binding.ivIllustration.height

            binding.spotlightOverlay.spotlightRect = RectF(left, top, right, bottom)
            binding.spotlightOverlay.cornerRadius = 30 * resources.displayMetrics.density

            binding.guideOverlayContainer.visibility = View.VISIBLE
            binding.guideOverlayContainer.setOnClickListener { dismissGuide() }
        }
    }

    private fun dismissGuide() {
        binding.guideOverlayContainer.visibility = View.GONE
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            .edit().putBoolean(KEY_GUIDE_SHOWN, true).apply()
    }

    private fun setupTextCardFlip() {
        binding.flTextCardContainer.setOnClickListener {
            flipCard()
        }
    }

    private fun flipCard() {
        val frontView = binding.llTextCardFront
        val backView = binding.llTextCardBack
        val duration = 250L

        if (!isShowingTranslation) {
            frontView.animate()
                .alpha(0f)
                .setDuration(duration)
                .withEndAction {
                    frontView.visibility = View.GONE
                    frontView.alpha = 1f
                    backView.alpha = 0f
                    backView.visibility = View.VISIBLE
                    backView.animate().alpha(1f).setDuration(duration).start()
                }.start()
            isShowingTranslation = true
        } else {
            backView.animate()
                .alpha(0f)
                .setDuration(duration)
                .withEndAction {
                    backView.visibility = View.GONE
                    backView.alpha = 1f
                    frontView.alpha = 0f
                    frontView.visibility = View.VISIBLE
                    frontView.animate().alpha(1f).setDuration(duration).start()
                }.start()
            isShowingTranslation = false
        }
    }
}
