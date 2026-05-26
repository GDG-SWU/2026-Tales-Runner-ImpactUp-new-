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
        val useDummy = intent.getBooleanExtra("use_dummy", false)

        setupToolbar(bookTitle)
        setupNavButtons()
        setupTextCardFlip()

        if (useDummy) {
            loadDummyStory()
        } else {
            observeViewModel()
            if (storyId != 0L) {
                viewModel.loadStory(storyId)
            }
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
        if (!page.image_url.isNullOrEmpty()) {
            Glide.with(this)
                .load(page.image_url)
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
            startActivity(Intent(this, ReadingSettingsActivity::class.java))
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

    private fun loadDummyStory() {
        val dummyPages = listOf(
            Triple(
                "무지개 공원에 사는 강아지 친구 모모는 아주 특별했어요. /n" +
                        "모모는 세상에서 제일 빨리 달릴 수 있었죠!",
                "虹の公園に住む子犬の友達モモは、とても特別でした。/n" +
                        "モモは世界で一番速く走ることができたのです！",
                R.drawable.page_1
            ),
            Triple(
                "어느 날, 모모는 신나게 공을 뻥! 차 올렸어요./n " +
                        "노란 공은 데구르르 굴러가 연못에 풍덩! 빠져버렸어요!",
                "ある日、モモは楽しそうにボールをポーン！と蹴り上げました。/n" +
                        "黄色いボールはコロコロ転がって、池にポチャン！と落ちてしまったのです！",
                R.drawable.page_2
            ),
            Triple(
                "모모는 너무 놀라서 엉엉 울 것 같았어요./n " +
                        "연못은 너무 깊어서 모모 혼자서는 꺼낼 수 없었죠.",
                "モモはとても驚いて、しくしく泣きそうでした。/n" +
                        "池はとても深くて、モモ一人ではどうやっても取り出すことができませんでした。",
                R.drawable.page_3
            ),
            Triple(
                "바로 그때, 연못에서 꽥꽥! 소리가 들렸어요./n " +
                        "귀여운 오리 친구들이 헤엄쳐 다가왔죠.",
                "ちょうどその時、池からガーガー！という声が聞こえました。/n" +
                        "かわいいアヒルの友達が泳いでやってきました。",
                R.drawable.page_4
            ),
            Triple(
                "오리 친구들은 작은 부리로 노란 공을 톡톡! 밀었어요./n " +
                        "드디어 노란 공이 다시 뿅! 나타났어요!",
                "アヒルの友達は、小さなクチバシで黄色いボールをツンツン！と押しました。/n" +
                        "ついに黄色いボールが再び現れました！",
                R.drawable.page_5
            ),
            Triple(
                "모모는 정말 기뻐서 꼬리를 살랑살랑 흔들었어요./n " +
                        "무지개 공원에는 즐거운 웃음소리가 가득했어요!",
                "モモは本当に嬉しくて、しっぽをフリフリ振りました。/n" +
                        "虹の公園には、楽しい笑い声がいっぱいでした！",
                R.drawable.page_6
            )
        )

        totalPages = dummyPages.size
        currentPage = 1

        // 더미 페이지 업데이트 함수
        fun showPage(page: Int) {
            val (ko, foreign, imgRes) = dummyPages[page - 1]
            binding.tvStoryText.text = ko
            binding.tvStoryTextTranslated.text = foreign
            binding.ivIllustration.setImageResource(imgRes)
            binding.llTextCardFront.visibility = android.view.View.VISIBLE
            binding.llTextCardBack.visibility = android.view.View.GONE
            isShowingTranslation = false
            updatePageBadge()
        }

        showPage(currentPage)

        // 페이지 버튼 재설정
        binding.btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                showPage(currentPage)
            }
        }
        binding.btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                showPage(currentPage)
            }
        }

        setupGuideOverlay()
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
