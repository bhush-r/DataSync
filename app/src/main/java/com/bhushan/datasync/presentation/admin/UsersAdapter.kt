package com.bhushan.datasync.presentation.admin

import android.view.LayoutInflater import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bhushan.datasync.R
import com.bhushan.datasync.databinding.ItemUserBinding
import com.bhushan.datasync.domain.model.Role
import com.bhushan.datasync.domain.model.User

class UsersAdapter : ListAdapter<User, UsersAdapter.UserViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            val name = user.name.ifBlank { "User" }
            binding.tvUserName.text = name
            binding.tvUserEmail.text = user.email
            binding.tvUserInitial.text = name.take(1).uppercase()
            binding.tvUserRole.text = user.role.name

            val context = binding.root.context
            val isAdmin = user.role == Role.ADMIN

            if (isAdmin) {
                binding.tvUserRole.setTextColor(context.getColor(R.color.admin_badge))
                binding.tvUserRole.backgroundTintList = context.getColorStateList(R.color.admin_badge_container)
            } else {
                binding.tvUserRole.setTextColor(context.getColor(R.color.user_badge))
                binding.tvUserRole.backgroundTintList = context.getColorStateList(R.color.user_badge_container)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.uid == newItem.uid
        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }
}