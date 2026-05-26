package com.example.dualtales.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dualtales.data.BookItem
import com.example.dualtales.databinding.ItemBookFeaturedBinding

class FeaturedBookAdapter(
    private val items: List<BookItem>,
    private val onContinueClick: (BookItem) -> Unit
) : RecyclerView.Adapter<FeaturedBookAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBookFeaturedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookItem) {
            binding.tvPageBadge.text = "${item.currentPage} / ${item.totalPages}"

            if (!item.coverImageUrl.isNullOrEmpty()) {
                // TODO: Glide/Coil로 실제 이미지 로딩
                // Glide.with(binding.root).load(item.coverImageUrl).into(binding.ivBookCover)
            } else {
                binding.ivBookCover.setBackgroundColor(
                    android.graphics.Color.parseColor("#E0E0E0")
                )
            }

            binding.btnContinueReading.setOnClickListener {
                onContinueClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemBookFeaturedBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size
}
