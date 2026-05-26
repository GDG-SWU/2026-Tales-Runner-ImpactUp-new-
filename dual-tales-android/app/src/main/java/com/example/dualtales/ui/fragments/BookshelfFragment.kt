package com.example.dualtales.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dualtales.R
import com.example.dualtales.databinding.FragmentBookshelfBinding
import com.example.dualtales.network.UserManager
import com.example.dualtales.ui.screens.ReadingActivity

class BookshelfFragment : Fragment() {

    private var _binding: FragmentBookshelfBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookshelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nickname = UserManager.getNickname()
        if (nickname.isNotEmpty()) {
            binding.tvGreeting.text = "${nickname} 님 안녕하세요.\n오늘은 어떤 동화를 읽어볼까요?"
        }

        val coverImages = listOf(
            R.drawable.book_cover_1, R.drawable.book_cover_2,
            R.drawable.book_cover_3, R.drawable.book_cover_4,
            R.drawable.book_cover_5, R.drawable.book_cover_6
        )
        val titles = listOf(
            "모모와 노란 공의 풍덩!", "동화책 2",
            "동화책 3", "동화책 4",
            "동화책 5", "동화책 6"
        )

        binding.llBookGrid.removeAllViews()

        for (i in coverImages.indices step 2) {
            val rowView = layoutInflater.inflate(R.layout.item_book_row, binding.llBookGrid, false)

            val iv1 = rowView.findViewById<android.widget.ImageView>(R.id.iv_book1)
            val iv2 = rowView.findViewById<android.widget.ImageView>(R.id.iv_book2)
            val card1 = rowView.findViewById<androidx.cardview.widget.CardView>(R.id.card_book1)
            val card2 = rowView.findViewById<androidx.cardview.widget.CardView>(R.id.card_book2)

            iv1.setImageResource(coverImages[i])
            card1.setOnClickListener {
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

            if (i + 1 < coverImages.size) {
                iv2.setImageResource(coverImages[i + 1])
                card2.setOnClickListener {
                    Toast.makeText(requireContext(), "준비 중입니다", Toast.LENGTH_SHORT).show()
                }
            }

            binding.llBookGrid.addView(rowView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
