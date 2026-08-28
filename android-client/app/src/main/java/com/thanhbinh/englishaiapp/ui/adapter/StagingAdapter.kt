package com.thanhbinh.englishaiapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thanhbinh.englishaiapp.data.model.StagingVocabulary
import com.thanhbinh.englishaiapp.databinding.ItemStagingVocabularyBinding

class StagingAdapter(
    private val onEditClick: (position: Int, item: StagingVocabulary) -> Unit,
    private val onDeleteClick: (position: Int, item: StagingVocabulary) -> Unit
) : RecyclerView.Adapter<StagingAdapter.StagingViewHolder>() {

    private val items = mutableListOf<StagingVocabulary>()

    fun getItems(): List<StagingVocabulary> = items

    fun setItems(newItems: List<StagingVocabulary>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: StagingVocabulary) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    fun addAll(newItems: List<StagingVocabulary>) {
        val startPos = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(startPos, newItems.size)
    }

    fun updateItem(position: Int, updatedItem: StagingVocabulary) {
        if (position in items.indices) {
            items[position] = updatedItem
            notifyItemChanged(position)
        }
    }

    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size - position)
        }
    }

    fun clearAll() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StagingViewHolder {
        val binding = ItemStagingVocabularyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StagingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StagingViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class StagingViewHolder(private val binding: ItemStagingVocabularyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StagingVocabulary, position: Int) {
            binding.txtEntryBadge.text = "MỤC ${position + 1}"
            binding.txtTerm.text = item.term
            binding.txtMeaning.text = item.meaning

            if (item.example.isNotBlank()) {
                binding.txtExample.text = item.example
                binding.txtExample.visibility = View.VISIBLE
            } else {
                binding.txtExample.visibility = View.GONE
            }

            binding.btnEdit.setOnClickListener {
                val currentPos = adapterPosition
                if (currentPos != RecyclerView.NO_POSITION && currentPos in items.indices) {
                    onEditClick(currentPos, items[currentPos])
                }
            }

            binding.btnDelete.setOnClickListener {
                val currentPos = adapterPosition
                if (currentPos != RecyclerView.NO_POSITION && currentPos in items.indices) {
                    onDeleteClick(currentPos, items[currentPos])
                }
            }
        }
    }
}
