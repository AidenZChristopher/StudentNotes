package com.example.studentnotes.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentnotes.data.entity.Folder
import com.example.studentnotes.databinding.ItemFolderBinding

class FolderAdapter(
    private val listener: OnFolderClickListener
) : ListAdapter<Folder, FolderAdapter.FolderViewHolder>(DiffCallback()) {

    interface OnFolderClickListener {
        fun onFolderClick(folder: Folder)
        fun onFolderDelete(folder: Folder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FolderViewHolder(private val binding: ItemFolderBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onFolderClick(getItem(adapterPosition))
                }
            }
            binding.deleteFolderBtn.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onFolderDelete(getItem(adapterPosition))
                }
            }
        }

        fun bind(folder: Folder) {
            binding.folderName.text = folder.name
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Folder>() {
        override fun areItemsTheSame(oldItem: Folder, newItem: Folder) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Folder, newItem: Folder) = oldItem == newItem
    }
}
