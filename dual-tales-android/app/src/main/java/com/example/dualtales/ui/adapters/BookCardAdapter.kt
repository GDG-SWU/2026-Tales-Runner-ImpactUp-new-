package com.example.dualtales.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dualtales.data.BookItem
import com.example.dualtales.R
import com.example.dualtales.databinding.ItemBookCardBinding

class BookCardAdapter(
    private val items: List<BookItem>,
    private val onItemClick: (BookItem) -> Unit
) : RecyclerView.Adapter<BookCardAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBookCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookItem) {
            when (item.id) {
                "1" -> binding.ivBookCover.setImageResource(R.drawable.book_cover_1)
                "2" -> binding.ivBookCover.setImageResource(R.drawable.book_cover_2)
                "3" -> binding.ivBookCover.setImageResource(R.drawable.book_cover_3)
                "4" -> binding.ivBookCover.setImageResource(R.drawable.book_cover_4)
                "5" -> binding.ivBookCover.setImageResource(R.drawable.book_cover_5)
                "6" -> binding.ivBookCover.setImageResource(R.drawable.book_cover_6)
            }
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBookCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
}
