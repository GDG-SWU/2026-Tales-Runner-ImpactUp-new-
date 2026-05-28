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
            android.util.Log.d("ADAPTER", "id=${item.id}, coverImageUrl=${item.coverImageUrl}")
            if (!item.coverImageUrl.isNullOrBlank()) {
                com.bumptech.glide.Glide.with(binding.root.context)
                    .load(item.coverImageUrl)
                    .placeholder(R.drawable.book_cover_1)
                    .error(R.drawable.book_cover_1)
                    .into(binding.ivBookCover)
            } else {
                binding.ivBookCover.setImageResource(R.drawable.book_cover_1)
            }
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBookCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
}
