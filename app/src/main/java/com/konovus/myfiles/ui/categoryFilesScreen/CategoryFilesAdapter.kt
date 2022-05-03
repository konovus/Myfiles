package com.konovus.myfiles.ui.categoryFilesScreen

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.konovus.myfiles.R
import com.konovus.myfiles.TAG
import com.konovus.myfiles.databinding.FileItemBinding
import com.konovus.myfiles.entities.MyFile
import com.konovus.myfiles.ui.categoryFilesScreen.CategoryFiles.Companion.albumCoverList
import java.io.File


@RequiresApi(Build.VERSION_CODES.Q)
class CategoryFilesAdapter(
    private val listener: OnItemClickListener,
    private val selectedItems: MutableLiveData<MutableList<MyFile>?>,
    private val isMultiSelectEnabled: LiveData<Boolean>
) :
    ListAdapter<MyFile, CategoryFilesAdapter.CategoryFilesViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryFilesViewHolder {
        val binding = FileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryFilesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryFilesViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class CategoryFilesViewHolder(private val binding: FileItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    getItem(pos)?.let {
                        listener.onItemClick(it, pos)
                    }
                }
            }
            itemView.setOnLongClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    getItem(pos)?.let {
                        listener.onLongItemClick(it, pos)
                    }
                }
                true
            }
            binding.checkbox.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    getItem(pos)?.let {
                        listener.onCheckBoxClick(it, pos)
                    }
                }
            }
        }

        fun bind(file: MyFile) {
            binding.apply {
                name.text = file.name
                size.text = file.size()
                date.text = file.date()
//                uri.text = file.uri
                checkbox.isChecked = false

                if (file.name.endsWith("jpg") || file.name.endsWith("jpeg") || file.name.endsWith("mp4"))
                    Glide.with(itemView.context).load(Uri.fromFile(File(file.uri)))
                        .into(fileLogo)
                else if (file.name.endsWith("mp3")) {
                    if (albumCoverList.containsKey(file.name)) {
                        val bitmap = BitmapFactory.decodeByteArray(
                            albumCoverList[file.name],
                            0,
                            albumCoverList[file.name]!!.size
                        )
                        Glide.with(itemView.context).asBitmap().load(bitmap)
                            .into(fileLogo)
                    } else fileLogo.setImageResource(R.drawable.ic_baseline_audio_file_24)
                } else if (file.isFolder)
                    fileLogo.setImageResource(R.drawable.ic_baseline_folder_24)
                else if (file.name.endsWith("apk")) {
                    val pm: PackageManager = itemView.context.packageManager
                    val pi = pm.getPackageArchiveInfo(file.uri, 0)
                    if (pi != null) {
                        pi.applicationInfo.sourceDir = file.uri
                        pi.applicationInfo.publicSourceDir = file.uri

                        val icon = pi.applicationInfo.loadIcon(pm)
                        Glide.with(itemView.context).asDrawable().load(icon).into(fileLogo)
                    } else fileLogo.setImageResource(R.drawable.ic_large_files)

                } else fileLogo.setImageResource(R.drawable.ic_large_files)


                selectedItems.observe(itemView.context as LifecycleOwner) {
                    it?.let {
                        if (it.size  > 0 )
                            checkbox.visibility = View.VISIBLE
                        checkbox.isChecked = it.contains(file)
                    }
                    if (it == null)
                        checkbox.isChecked = false
                }

                isMultiSelectEnabled.observe(itemView.context as LifecycleOwner) {
                    checkbox.isVisible = it
                }

            }
        }
    }



    interface OnItemClickListener {
        fun onItemClick(file: MyFile, position: Int)
        fun onLongItemClick(file: MyFile, position: Int)
        fun onCheckBoxClick(file: MyFile, position: Int)
    }

    class DiffCallback : DiffUtil.ItemCallback<MyFile>() {
        override fun areItemsTheSame(oldItem: MyFile, newItem: MyFile) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: MyFile, newItem: MyFile) =
            oldItem == newItem
    }

}