package com.example.dualtales.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dualtales.R
import com.example.dualtales.data.BookItem
import com.example.dualtales.databinding.FragmentSearchBinding
import com.example.dualtales.ui.adapters.BookCardAdapter

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dummyBooks = listOf(
            BookItem(id = "1", title = "모모와 노란 공의 풍덩!", coverImageUrl = "book_cover_1", language = "JA"),
            BookItem(id = "2", title = "동화책 2", coverImageUrl = "book_cover_2", language = "EN"),
            BookItem(id = "3", title = "동화책 3", coverImageUrl = "book_cover_3", language = "EN"),
            BookItem(id = "4", title = "동화책 4", coverImageUrl = "book_cover_4", language = "FR"),
            BookItem(id = "5", title = "동화책 5", coverImageUrl = "book_cover_5", language = "EN"),
            BookItem(id = "6", title = "동화책 6", coverImageUrl = "book_cover_6", language = "KO")
        )

        val adapter = BookCardAdapter(dummyBooks) { bookItem ->
            if (bookItem.id == "1") {
                startActivity(Intent(requireContext(), ReadingActivity::class.java).apply {
                    putExtra("story_id", -1L)
                    putExtra("book_title", bookItem.title)
                    putExtra("use_dummy", true)
                })
            } else {
                Toast.makeText(requireContext(), "준비 중입니다", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvSearchResults.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvSearchResults.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
