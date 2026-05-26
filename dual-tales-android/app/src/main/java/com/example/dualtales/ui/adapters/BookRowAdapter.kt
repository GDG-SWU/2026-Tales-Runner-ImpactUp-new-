package com.example.dualtales.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dualtales.data.BookItem
import com.example.dualtales.databinding.ItemBookRowBinding

class BookRowAdapter(
    private val books: List<BookItem>,
    private val onBookClick: (BookItem) -> Unit
) : RecyclerView.Adapter<BookRowAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBookRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val book1 = books[position * 2]
            val book2Index = position * 2 + 1

            // 책 1 설정
            binding.ivBook1.setBackgroundColor(
                android.graphics.Color.parseColor("#E0E0E0")
            )
            binding.cardBook1.setOnClickListener { onBookClick(book1) }

            // 책 2 설정 (홀수 개일 때 마지막 카드 숨김)
            if (book2Index < books.size) {
                val book2 = books[book2Index]
                binding.cardBook2.visibility = View.VISIBLE
                binding.ivBook2.setBackgroundColor(
                    android.graphics.Color.parseColor("#E0E0E0")
                )
                binding.cardBook2.setOnClickListener { onBookClick(book2) }
            } else {
                binding.cardBook2.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    // 행 개수 = 책 개수를 2로 나누어 올림
    override fun getItemCount() = (books.size + 1) / 2
}
