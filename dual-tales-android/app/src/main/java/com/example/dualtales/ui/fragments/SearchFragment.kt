package com.example.dualtales.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dualtales.R
import com.example.dualtales.data.BookItem
import com.example.dualtales.databinding.FragmentSearchBinding
import com.example.dualtales.ui.adapters.BookCardAdapter
import com.example.dualtales.ui.screens.ReadingActivity

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var bookAdapter: BookCardAdapter

    // 원본 데이터 (수정하지 않음)
    private val allBooks = mutableListOf<BookItem>()

    // 어댑터에 전달하는 필터링된 목록
    private val filteredBooks = mutableListOf<BookItem>()

    // 다중 선택 가능한 언어 필터 상태
    private val selectedLanguages = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDummyData()
        setupRecyclerView()
        setupLanguageButtons()
        setupSearch()
    }

    private fun loadDummyData() {
        // TODO: 백엔드 API 호출로 교체
        allBooks.addAll(
            listOf(
                BookItem("1", "파란 신발의 모험", null, "한국어", 2, 14),
                BookItem("2", "The Star and the Sea", null, "영어", 5, 20),
                BookItem("3", "Le Petit Renard", null, "프랑스어", 1, 12),
                BookItem("4", "숲속의 작은 음악가", null, "한국어", 3, 16),
                BookItem("5", "A Wish for Wings", null, "영어", 0, 18),
                BookItem("6", "La Lune et les Étoiles", null, "프랑스어", 2, 10)
            )
        )
        filteredBooks.addAll(allBooks)
    }

    private fun setupRecyclerView() {
        bookAdapter = BookCardAdapter(filteredBooks) { book ->
            val intent = Intent(requireContext(), ReadingActivity::class.java).apply {
                putExtra("book_id", book.id)
                putExtra("book_title", book.title)
                putExtra("current_page", book.currentPage)
                putExtra("total_pages", book.totalPages)
            }
            startActivity(intent)
        }

        binding.rvSearchResults.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = bookAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    // 아이템 간격 8dp: 각 면에 4dp씩 적용
                    val spacing = (4 * resources.displayMetrics.density).toInt()
                    outRect.set(spacing, spacing, spacing, spacing)
                }
            })
        }
    }

    private fun setupLanguageButtons() {
        binding.btnLangEnglish.setOnClickListener {
            toggleLanguageFilter("영어", binding.btnLangEnglish)
        }
        binding.btnLangFrench.setOnClickListener {
            toggleLanguageFilter("프랑스어", binding.btnLangFrench)
        }
        binding.btnLangKorean.setOnClickListener {
            toggleLanguageFilter("한국어", binding.btnLangKorean)
        }
    }

    private fun toggleLanguageFilter(language: String, button: Button) {
        if (language in selectedLanguages) {
            selectedLanguages.remove(language)
            button.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_language_chip
            )
            button.setTextColor(Color.parseColor("#1A1A1A"))
        } else {
            selectedLanguages.add(language)
            button.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_language_chip_selected
            )
            button.setTextColor(Color.parseColor("#FFFFFF"))
        }
        applyFilter()
    }

    private fun setupSearch() {
        // 실시간 필터링
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { applyFilter() }
        })

        // 검색 아이콘 클릭 시에도 필터 적용
        binding.ivSearchIcon.setOnClickListener { applyFilter() }
    }

    /**
     * 언어 필터(다중 선택) AND 텍스트 검색을 동시에 적용한다.
     * - 선택된 언어 없음 → 언어 조건 무시
     * - 검색어 없음 → 텍스트 조건 무시
     */
    private fun applyFilter() {
        val query = binding.etSearch.text.toString().trim()
        filteredBooks.clear()
        filteredBooks.addAll(
            allBooks.filter { book ->
                val matchesLanguage = selectedLanguages.isEmpty() ||
                        book.language in selectedLanguages
                val matchesQuery = query.isEmpty() ||
                        book.title.contains(query, ignoreCase = true) ||
                        book.language.contains(query, ignoreCase = true)
                matchesLanguage && matchesQuery
            }
        )
        bookAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
