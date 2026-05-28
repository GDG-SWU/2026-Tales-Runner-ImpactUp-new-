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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dualtales.data.BookItem
import com.example.dualtales.databinding.FragmentSearchBinding
import com.example.dualtales.network.RetrofitClient
import com.example.dualtales.network.dto.StoryResponseDto
import com.example.dualtales.ui.adapters.BookCardAdapter
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    sealed class FeedState {
        object Loading : FeedState()
        data class Success(val stories: List<StoryResponseDto>) : FeedState()
        data class Error(val message: String) : FeedState()
    }
    val feedState = MutableLiveData<FeedState>()

    fun loadFeed(langCode: String = "") {
        viewModelScope.launch {
            feedState.value = FeedState.Loading
            try {
                val response = if (langCode.isEmpty()) {
                    RetrofitClient.api.getFeed("EN")
                } else {
                    RetrofitClient.api.getFeed(langCode)
                }
                if (response.isSuccessful && response.body() != null) {
                    feedState.value = FeedState.Success(response.body()!!)
                } else {
                    feedState.value = FeedState.Error("피드를 불러오지 못했습니다")
                }
            } catch (e: Exception) {
                feedState.value = FeedState.Error("서버 연결에 실패했습니다")
            }
        }
    }
}

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private var selectedLangCode = "EN"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvSearchResults.layoutManager = GridLayoutManager(requireContext(), 3)

        setupLangFilter()
        observeViewModel()
        viewModel.loadFeed(selectedLangCode)
    }

    private fun setupLangFilter() {
        binding.btnLangEnglish.setOnClickListener {
            selectedLangCode = "EN"
            viewModel.loadFeed(selectedLangCode)
        }
        binding.btnLangFrench.setOnClickListener {
            selectedLangCode = "FR"
            viewModel.loadFeed(selectedLangCode)
        }
        binding.btnLangKorean.setOnClickListener {
            selectedLangCode = "KO"
            viewModel.loadFeed(selectedLangCode)
        }
    }

    private fun observeViewModel() {
        viewModel.feedState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchViewModel.FeedState.Loading -> {}
                is SearchViewModel.FeedState.Success -> updateFeedList(state.stories)
                is SearchViewModel.FeedState.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFeedList(stories: List<StoryResponseDto>) {
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
        val adapter = BookCardAdapter(bookItems) { bookItem ->
            startActivity(Intent(requireContext(), ReadingActivity::class.java).apply {
                putExtra("story_id", bookItem.id.toLong())
                putExtra("book_title", bookItem.title)
            })
        }
        binding.rvSearchResults.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
