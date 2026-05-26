package com.example.dualtales.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dualtales.data.BookItem
import com.example.dualtales.databinding.ItemBookSmallBinding

class SmallBookAdapter(
    private val items: List<BookItem>,
    private val onItemClick: (BookItem) -> Unit
) : RecyclerView.Adapter<SmallBookAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemBookSmallBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookItem) {
            val context = binding.root.context

            val resId1 = context.resources.getIdentifier(
                item.coverImageUrl, "drawable", context.packageName
            )
            if (resId1 != 0) {
                binding.ivBook1.setImageResource(resId1)
            }
            binding.cardBook1.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemBookSmallBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size
}
