package com.konovus.myfiles.ui.detailsScreen

import android.app.usage.StorageStatsManager
import android.graphics.ColorFilter
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.konovus.myfiles.MainActivity
import com.konovus.myfiles.R
import com.konovus.myfiles.TAG
import com.konovus.myfiles.databinding.CategoryProgressItemBinding
import com.konovus.myfiles.databinding.DetailsFragmentBinding
import com.konovus.myfiles.entities.MyFile
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.log
import kotlin.math.roundToInt

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class DetailsFragment : Fragment(R.layout.details_fragment) {

    private var _binding: DetailsFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DetailsFragmentBinding.bind(view)

        MainActivity.tabLayout.visibility = View.GONE

        initLayout()

    }

    private fun initLayout() {
        val storageStatsManager =
            requireActivity().getSystemService(AppCompatActivity.STORAGE_STATS_SERVICE) as StorageStatsManager
        val n1 = storageStatsManager.getFreeBytes(StorageManager.UUID_DEFAULT)
        val n2 = storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
        val totalSpace = android.text.format.Formatter.formatFileSize(requireContext(), n2)
        val totalUsed = getTotalUsed(n1, n2)
        binding.totalUsedCounter.text = if (totalUsed.length == 4) "${totalUsed}0" else totalUsed
        binding.totalStorageTv.text = "Total storage $totalSpace"
        binding.progressGbUsed.max = totalSpace.substring(0,2).toInt()
        binding.progressGbUsed.progress = totalUsed.toDouble().toInt()
        viewModel.data.observe(viewLifecycleOwner) {

            binding.categoriesProgressWrapper.removeAllViews()
            val categoriesNames = listOf(
                "Images", "Videos", "Audio", "Documents", "Archive files",
                "Installation packages"
            )
            val myDataFields = listOf<List<MyFile>>(
                it.images, it.videos, it.audio, it.documents,
                it.archives, it.apks
            )
            val colors = listOf(R.color.teal_200, R.color.purple_700, R.color.orange, R.color.yellow, R.color.pink, R.color.purple_200)

            myDataFields.forEachIndexed { i, folder ->

                val categoryItem = layoutInflater.inflate(R.layout.category_progress_item, null)
                val linearBinding = CategoryProgressItemBinding.bind(categoryItem)
                var counter = 0L
                folder.forEach { file -> counter += file.size }
                val categorySpace =
                    android.text.format.Formatter.formatFileSize(requireContext(), counter)
                linearBinding.counter.text = categorySpace
                linearBinding.categoryName.text = categoriesNames[i]
                linearBinding.progressIndicator.max = totalSpace.split(".")[0].toInt()
                val currentProgress = if (categorySpace.split(" ")[1] == "MB")
                    (categorySpace.split(" ")[0].toDouble() / 1000).toInt()
                else categorySpace.split(" ")[0].toDouble().toInt()
                linearBinding.progressIndicator.progress = currentProgress
                linearBinding.progressIndicator.progressDrawable.colorFilter = BlendModeColorFilterCompat
                    .createBlendModeColorFilterCompat(ContextCompat.getColor(requireContext(), colors[i]), BlendModeCompat.SRC_ATOP)
                binding.categoriesProgressWrapper.addView(categoryItem)
            }
        }
    }

    private fun getTotalUsed(n1: Long, n2: Long): String {
        val result: Double = (n2 - n1).toDouble() / 1000 / 1000 / 1000
        return ((result * 100.0).roundToInt() / 100.0).toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}