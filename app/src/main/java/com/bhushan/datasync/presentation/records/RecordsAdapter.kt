package com.bhushan.datasync.presentation.records

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bhushan.datasync.databinding.ItemRecordBinding
import com.bhushan.datasync.domain.model.RecordItem
import com.bhushan.datasync.utils.DateUtils

class RecordsAdapter(
    private val onEditClick: (RecordItem) -> Unit,
    private val onDeleteClick: (RecordItem) -> Unit
) : ListAdapter<RecordItem, RecordsAdapter.RecordViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecordViewHolder(private val binding: ItemRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecordItem) {
            binding.tvRecordTitle.text = item.title
            binding.tvRecordDescription.text = item.description
            binding.tvRecordDate.text = DateUtils.formatShort(item.updatedAt)
            binding.btnEdit.setOnClickListener { onEditClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<RecordItem>() {
        override fun areItemsTheSame(oldItem: RecordItem, newItem: RecordItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: RecordItem, newItem: RecordItem) = oldItem == newItem
    }
}
