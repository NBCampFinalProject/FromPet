package com.pet.frompet.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.pet.frompet.databinding.ItemHomeBinding
import com.pet.frompet.data.model.User


class HomeAdapter(homeFragment: HomeFragment) : ListAdapter<User, HomeAdapter.HomeViewHolder>(
    object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
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
        fun bind(item: User) = with(binding) {
            ivPetImage.load(item.petProfile)
            tvAgePet.text = item.petAge.toString()
            tvNamePet.text = item.petName
            tvTypePet.text = item.petType

        }
    }
}
