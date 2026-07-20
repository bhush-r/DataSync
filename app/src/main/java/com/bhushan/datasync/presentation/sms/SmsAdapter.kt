package com.bhushan.datasync.presentation.sms

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bhushan.datasync.databinding.ItemSmsBinding
import com.bhushan.datasync.domain.model.SmsItem
import com.bhushan.datasync.utils.DateUtils

class SmsAdapter : ListAdapter<SmsItem, SmsAdapter.SmsViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val binding = ItemSmsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SmsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SmsViewHolder(private val binding: ItemSmsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SmsItem) {
            binding.tvSmsSender.text = item.sender
            binding.tvSmsPreview.text = item.preview
            binding.tvSmsDate.text = DateUtils.formatShort(item.timestamp)
            binding.tvSmsInitial.text = item.sender.take(1).uppercase().ifEmpty { "?" }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<SmsItem>() {
        override fun areItemsTheSame(oldItem: SmsItem, newItem: SmsItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SmsItem, newItem: SmsItem) = oldItem == newItem
    }
}
