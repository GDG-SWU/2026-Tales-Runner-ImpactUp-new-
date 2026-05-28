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
import androidx.viewpager2.widget.ViewPager2
import com.example.dualtales.databinding.FragmentBookshelfBinding
import com.example.dualtales.network.RetrofitClient
import com.example.dualtales.network.dto.StoryResponseDto
import kotlinx.coroutines.launch
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

        val nickname = com.example.dualtales.network.UserManager.getNickname()
        if (nickname.isNotEmpty()) {
            binding.tvGreeting.text = "${nickname} 님 안녕하세요.\n오늘은 어떤 동화를 읽어볼까요?"
        }

        observeViewModel()
        viewModel.loadMyStories()
    }

    private fun observeViewModel() {
        viewModel.storiesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BookshelfViewModel.StoriesState.Loading -> {
                    android.util.Log.d("BOOKSHELF", "loading...")
                }
                is BookshelfViewModel.StoriesState.Success -> {
                    android.util.Log.d("BOOKSHELF", "stories size = ${state.stories.size}")
                    setupFeaturedPager(state.stories)
                    updateBookList(state.stories)
                }
                is BookshelfViewModel.StoriesState.Error -> {
                    android.util.Log.d("BOOKSHELF", "error = ${state.message}")
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupFeaturedPager(stories: List<StoryResponseDto>) {
        val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

            inner class FeaturedViewHolder(val binding: com.example.dualtales.databinding.ItemBookFeaturedBinding) :
                androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
                return FeaturedViewHolder(
                    com.example.dualtales.databinding.ItemBookFeaturedBinding.inflate(
                        android.view.LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
                val story = stories[position]
                val vh = holder as FeaturedViewHolder
                if (!story.coverImageUrl.isNullOrBlank()) {
                    com.bumptech.glide.Glide.with(vh.binding.root.context)
                        .load(story.coverImageUrl)
                        .into(vh.binding.ivBookCover)
                } else {
                    vh.binding.ivBookCover.setImageResource(R.drawable.book_cover_1)
                }
                vh.binding.tvPageBadge.text = "1 / ${story.page_count}"
                vh.binding.btnContinueReading.setOnClickListener {
                    startActivity(Intent(requireContext(), ReadingActivity::class.java).apply {
                        putExtra("story_id", story.id)
                        putExtra("book_title", story.title)
                    })
                }
            }

            override fun getItemCount() = stories.size
        }

        binding.viewPagerFeatured.apply {
            this.adapter = adapter
            offscreenPageLimit = 1
            clipToPadding = false
            clipChildren = false
            setPageTransformer(ScalePageTransformer())
        }
    }

    private fun updateBookList(stories: List<StoryResponseDto>) {
        binding.llBookGrid.removeAllViews()

        if (stories.isEmpty()) {
            // 빈 상태 처리 — 필요 시 empty view 표시
            return
        }

        val pairs = stories.chunked(2)
        pairs.forEach { pair ->
            val rowView = layoutInflater.inflate(R.layout.item_book_row, binding.llBookGrid, false)
            val iv1 = rowView.findViewById<android.widget.ImageView>(R.id.iv_book1)
            val iv2 = rowView.findViewById<android.widget.ImageView>(R.id.iv_book2)
            val card1 = rowView.findViewById<androidx.cardview.widget.CardView>(R.id.card_book1)
            val card2 = rowView.findViewById<androidx.cardview.widget.CardView>(R.id.card_book2)

            val story1 = pair[0]
            if (!story1.coverImageUrl.isNullOrBlank()) {
                com.bumptech.glide.Glide.with(this).load(story1.coverImageUrl).into(iv1)
            } else {
                iv1.setImageResource(R.drawable.book_cover_1)
            }
            card1.setOnClickListener {
                startActivity(Intent(requireContext(), ReadingActivity::class.java).apply {
                    putExtra("story_id", story1.id)
                    putExtra("book_title", story1.title)
                })
            }

            if (pair.size > 1) {
                val story2 = pair[1]
                if (!story2.coverImageUrl.isNullOrBlank()) {
                    com.bumptech.glide.Glide.with(this).load(story2.coverImageUrl).into(iv2)
                } else {
                    iv2.setImageResource(R.drawable.book_cover_2)
                }
                card2.setOnClickListener {
                    startActivity(Intent(requireContext(), ReadingActivity::class.java).apply {
                        putExtra("story_id", story2.id)
                        putExtra("book_title", story2.title)
                    })
                }
            } else {
                card2.visibility = android.view.View.INVISIBLE
            }

            binding.llBookGrid.addView(rowView)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMyStories()
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
