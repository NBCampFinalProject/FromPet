package com.example.frompet.ui.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.frompet.R

import com.example.frompet.databinding.ItemChatlistBinding
import com.example.frompet.data.model.UserModel

class ChatListAdapter(private val context: Context, private val onUserClick: (UserModel) -> Unit) :
    ListAdapter<UserModel, ChatListAdapter.ChatListViewHoler>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHoler {
        val binding = ItemChatlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatListViewHoler(binding)
    }

    override fun onBindViewHolder(holder: ChatListViewHoler, position: Int) {
        val user = getItem(position)
        holder.bindItems(user)
    }

    inner class ChatListViewHoler(private val binding: ItemChatlistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItems(user: UserModel) {
            binding.apply {
                tvUserName.text = user.petName
                tvUserType.text = user.petType
                user.petProfile?.let {
                    profileArea.load(it){
                        error(R.drawable.kakaotalk_20230825_222509794_01)

                    }
                }
                root.setOnClickListener {
                    onUserClick(user)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UserModel>() {
        override fun areItemsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem == newItem
        }
    }
}



