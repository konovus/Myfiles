package com.konovus.myfiles.ui.searchScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.konovus.myfiles.databinding.FileItemBinding
import com.konovus.myfiles.databinding.SearchFragmentBinding
import com.konovus.myfiles.entities.MyFile
import com.konovus.myfiles.ui.categoryFilesScreen.CategoryFilesAdapter

class SearchAdapter(private val listener: OnItemClickListener) : ListAdapter<MyFile, SearchAdapter.SearchAdapterViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchAdapterViewHolder {
        val binding = FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchAdapter.SearchAdapterViewHolder, position: Int) {
        getItem(position)?.let{ holder.bind(it) }
    }

    inner class SearchAdapterViewHolder(private val binding: FileItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {

        }

        fun bind(file: MyFile){
            binding.apply {
                name.text = file.name
                size.text = file.size()
                date.text = file.date()
//                uri.text = file.uri
            }
        }
    }


    interface OnItemClickListener {
        fun onItemClick(file: MyFile)
    }

    class DiffCallback : DiffUtil.ItemCallback<MyFile>() {
        override fun areItemsTheSame(oldItem: MyFile, newItem: MyFile) =
            oldItem.date == newItem.date

        override fun areContentsTheSame(oldItem: MyFile, newItem: MyFile) =
            oldItem == newItem
    }

    companion object{
        var counterInt = 1
    }
}