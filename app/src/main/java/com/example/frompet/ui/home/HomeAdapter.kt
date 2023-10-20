package com.example.frompet.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.frompet.databinding.ItemHomeBinding
import com.example.frompet.data.model.UserModel


class HomeAdapter : ListAdapter<UserModel, HomeAdapter.HomeViewHolder>(
    object : DiffUtil.ItemCallback<UserModel>() {
        override fun areItemsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = ItemHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class HomeViewHolder(
        private val binding: ItemHomeBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserModel) = with(binding) {
            ivPetImage.load(item.petProfile)
            tvNamePet.text = item.petName
            tvTypePet.text = item.petType


        }
    }
}