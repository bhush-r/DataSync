package com.bhushan.datasync.presentation.calllogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bhushan.datasync.R
import com.bhushan.datasync.databinding.ItemCallLogBinding
import com.bhushan.datasync.domain.model.CallLogItem
import com.bhushan.datasync.domain.model.CallType
import com.bhushan.datasync.utils.DateUtils

class CallLogsAdapter : ListAdapter<CallLogItem, CallLogsAdapter.CallLogViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallLogViewHolder {
        val binding = ItemCallLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CallLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CallLogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CallLogViewHolder(private val binding: ItemCallLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CallLogItem) {
            binding.tvCallName.text = item.name
            binding.tvCallNumber.text = item.phoneNumber
            binding.tvCallDuration.text = DateUtils.formatDuration(item.durationSeconds)
            binding.tvCallDate.text = DateUtils.formatShort(item.timestamp)

            val (label, colorRes) = when (item.callType) {
                CallType.INCOMING -> R.string.call_type_incoming to R.color.call_incoming
                CallType.OUTGOING -> R.string.call_type_outgoing to R.color.call_outgoing
                CallType.MISSED -> R.string.call_type_missed to R.color.call_missed
                CallType.UNKNOWN -> R.string.call_type_unknown to R.color.text_secondary
            }
            binding.tvCallType.text = binding.root.context.getString(label)
            binding.tvCallType.setTextColor(binding.root.context.getColor(colorRes))
            binding.callTypeIndicator.setBackgroundColor(binding.root.context.getColor(colorRes))
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<CallLogItem>() {
        override fun areItemsTheSame(oldItem: CallLogItem, newItem: CallLogItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CallLogItem, newItem: CallLogItem) =
            oldItem == newItem
    }
}
