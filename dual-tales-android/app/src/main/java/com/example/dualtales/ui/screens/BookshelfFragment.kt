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
import com.example.dualtales.databinding.FragmentBookshelfBinding
import com.example.dualtales.network.RetrofitClient
import com.example.dualtales.network.dto.StoryResponseDto
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.dualtales.ui.adapters.BookCardAdapter
import com.example.dualtales.databinding.ItemBookFeaturedBinding
import kotlinx.coroutines.launch
import com.example.dualtales.data.BookItem
import com.example.dualtales.R

class BookshelfViewModel : ViewModel() {

    sealed class StoriesState {
        object Loading : StoriesState()
        data class Success(val stories: List<StoryResponseDto>) : StoriesState()
        data class Error(val message: String) : StoriesState()
    }

    val storiesState = MutableLiveData<StoriesState>()

    fun loadMyStories() {
        viewModelScope.launch {
            storiesState.value = StoriesState.Loading
            try {
                val response = RetrofitClient.api.getMyStories()
                if (response.isSuccessful && response.body() != null) {
                    storiesState.value = StoriesState.Success(response.body()!!)
                } else {
                    storiesState.value = StoriesState.Error("동화 목록을 불러오지 못했습니다")
                }
            } catch (e: Exception) {
                storiesState.value = StoriesState.Error("서버 연결에 실패했습니다")
            }
        }
    }
}

class BookshelfFragment : Fragment() {

    private var _binding: FragmentBookshelfBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookshelfViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookshelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 닉네임 표시
        val nickname = com.example.dualtales.network.UserManager.getNickname()
        if (nickname.isNotEmpty()) {
            binding.tvGreeting.text = "${nickname} 님 안녕하세요.\n오늘은 어떤 동화를 읽어볼까요?"
        }

        setupFeaturedPager()
        observeViewModel()
        loadDummyBooks()
    }

    private fun setupFeaturedPager() {
        val coverImages = listOf(
            R.drawable.book_cover_1,
            R.drawable.book_cover_2,
            R.drawable.book_cover_3
        )
        binding.viewPagerFeatured.apply {
            adapter = FeaturedAdapter(coverImages)
            offscreenPageLimit = 1
            clipToPadding = false
            clipChildren = false
            setPageTransformer(ScalePageTransformer())
        }
    }

    inner class FeaturedAdapter(
        private val coverImages: List<Int>
    ) : RecyclerView.Adapter<FeaturedAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemBookFeaturedBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(resId: Int) {
                binding.ivBookCover.setImageResource(resId)
                binding.ivBookCover.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                binding.tvPageBadge.text = "4 / 16"
                binding.btnContinueReading.setOnClickListener {
                    startActivity(Intent(requireContext(), ReadingActivity::class.java).apply {
                        putExtra("story_id", -1L)
                        putExtra("use_dummy", true)
                    })
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(ItemBookFeaturedBinding.inflate(layoutInflater, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(coverImages[position])

        override fun getItemCount() = coverImages.size
    }

    private fun observeViewModel() {
        viewModel.storiesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BookshelfViewModel.StoriesState.Loading -> {
                    // 로딩 중 처리 (필요 시 ProgressBar 표시)
                }
                is BookshelfViewModel.StoriesState.Success -> {
                    updateBookList(state.stories)
                }
                is BookshelfViewModel.StoriesState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateBookList(stories: List<StoryResponseDto>) {
        val bookItems = stories.map { story ->
            BookItem(
                id = story.id.toString(),
                title = story.title,
                coverImageUrl = story.coverImageUrl ?: "",
                language = story.targetLangCode,
                totalPages = story.page_count,
                createdAt = story.createdAt
            )
        }

        binding.llBookGrid.removeAllViews()
        bookItems.forEach { bookItem ->
            val itemView = layoutInflater.inflate(
                com.example.dualtales.R.layout.item_book_card,
                binding.llBookGrid,
                false
            )
            itemView.setOnClickListener {
                val intent = Intent(requireContext(), ReadingActivity::class.java).apply {
                    putExtra("story_id", bookItem.id.toLong())
                    putExtra("book_title", bookItem.title)
                }
                startActivity(intent)
            }
            binding.llBookGrid.addView(itemView)
        }
    }

    private fun loadDummyBooks() {
        // 1. R.drawable 리소스 ID 방식으로 6개 이미지 준비 (가장 안전함)
        val coverImages = listOf(
            R.drawable.book_cover_1,
            R.drawable.book_cover_2,
            R.drawable.book_cover_3,
            R.drawable.book_cover_4,
            R.drawable.book_cover_5,
            R.drawable.book_cover_6
        )

        val titles = listOf(
            "모모와 노란 공의 풍덩!", "동화책 2",
            "동화책 3", "동화책 4",
            "동화책 5", "동화책 6"
        )

        binding.llBookGrid.removeAllViews()

        // 2. 2개씩 한 쌍으로 총 3줄 만들기
        for (i in coverImages.indices step 2) {

            // attachToRoot = false 설정으로 방금 XML에 넣은 marginBottom 49dp를 살립니다.
            val rowView = layoutInflater.inflate(
                R.layout.item_book_row,
                binding.llBookGrid,
                false
            )

            val card1 = rowView.findViewById<androidx.cardview.widget.CardView>(R.id.card_book1)
            val card2 = rowView.findViewById<androidx.cardview.widget.CardView>(R.id.card_book2)
            val iv1 = rowView.findViewById<android.widget.ImageView>(R.id.iv_book1)
            val iv2 = rowView.findViewById<android.widget.ImageView>(R.id.iv_book2)

            // 첫 번째 책 세팅
            iv1?.setImageResource(coverImages[i])
            iv1?.setBackgroundColor(android.graphics.Color.TRANSPARENT) // 회색 배경 가리기

            card1?.setOnClickListener {
                if (i == 0) {
                    startActivity(Intent(requireContext(), ReadingActivity::class.java).apply {
                        putExtra("story_id", -1L)
                        putExtra("book_title", titles[i])
                        putExtra("use_dummy", true)
                    })
                } else {
                    Toast.makeText(requireContext(), "준비 중입니다", Toast.LENGTH_SHORT).show()
                }
            }

            // 두 번째 책 세팅
            if (i + 1 < coverImages.size) {
                iv2?.setImageResource(coverImages[i + 1])
                iv2?.setBackgroundColor(android.graphics.Color.TRANSPARENT) // 회색 배경 가리기

                card2?.setOnClickListener {
                    Toast.makeText(requireContext(), "준비 중입니다", Toast.LENGTH_SHORT).show()
                }
            } else {
                card2?.visibility = View.INVISIBLE
            }

            binding.llBookGrid.addView(rowView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ScalePageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        val scaleFactor = 0.85f
        when {
            position < -1 -> page.alpha = 0f
            position <= 1 -> {
                val scale = maxOf(scaleFactor, 1 - Math.abs(position) * (1 - scaleFactor))
                page.scaleX = scale
                page.scaleY = scale
                page.alpha = maxOf(0.7f, 1 - Math.abs(position) * 0.3f)
            }
            else -> page.alpha = 0f
        }
    }
}
