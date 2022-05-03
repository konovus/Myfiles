package com.konovus.myfiles.ui.mainScreen

import android.Manifest
import android.Manifest.permission.*
import android.app.usage.StorageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager.UUID_DEFAULT
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.konovus.myfiles.BuildConfig
import com.konovus.myfiles.MainActivity
import com.konovus.myfiles.R
import com.konovus.myfiles.TAG
import com.konovus.myfiles.databinding.MainFragmentBinding
import com.konovus.myfiles.entities.MainSizes
import com.konovus.myfiles.entities.MyData
import com.konovus.myfiles.entities.MyFile
import com.konovus.myfiles.entities.MyFolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@RequiresApi(Build.VERSION_CODES.R)
@AndroidEntryPoint
class MainFragment : Fragment(R.layout.main_fragment) {

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()
    companion object{
        private var data = MyData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = MainFragmentBinding.bind(view)

        MainActivity.tabLayout.visibility = View.VISIBLE

        if ( checkPermission( arrayOf( READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)))
            init()

        initClickListeners(data)
        setHasOptionsMenu(true)
    }

    private fun init() {
        initDataObserver()
        setMainUiCategoriesText()
        initSearchAllData()
        initAvailableData()
    }

    private fun initDataObserver() {
        viewModel.data.observe(viewLifecycleOwner){
            it?.let {
                data = it
                initClickListeners(it)
            }
        }
    }

    private fun initSearchAllData() {
        data = MyData()
        val root = File("${Environment.getStorageDirectory()}/emulated/0")

        val extList = listOf(
            arrayListOf("pdf", "epub", "doc", "docx", "txt", "djvu", "pdb", "fb2", "prc", "mobi"),
            arrayListOf("mp3"), arrayListOf("jpg", "jpeg"), arrayListOf("mp4"), arrayListOf("apk"),
            arrayListOf("rar", "zip")
        )

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

            val myData = searchDirectory(root, data, extList)
            viewModel.updateData(myData)
            viewModel.updateMainSizes(myData)
        }
    }


    private fun setMainUiCategoriesText() {
        viewModel.mainSizes.observe(viewLifecycleOwner){
            val mainSizes = it ?: MainSizes()
            mainSizes.apply {
                binding.apply {
                    documentsCounter.text = documents.toString()
                    downloadsCounter.text = downloads.toString()
                    recentCounter.text = recent.toString()
                    imagesCounter.text = images.toString()
                    videosCounter.text = videos.toString()
                    audiosCounter.text = audio.toString()
                    apksCounter.text = apks.toString()
                    archivesCounter.text = archives.toString()
                    largeFilesCounter.text = largeFiles.toString()
                }
            }
        }
    }

    private fun searchDirectory(
        directory: File,
        data: MyData,
        extList: List<ArrayList<String>>
    ) : MyData {

            data.apply {
                directory.listFiles().forEach {
                    if (it.isFile) {
                        if (it.length() > 10 * 1_048_576)
                            largeFiles.add(MyFile(it.name, it.length(), it.lastModified(), it.absolutePath))
                        if (it.lastModified() > System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000 && !it.absolutePath.contains("cache"))
                            recent.add(MyFile(it.name, it.length(), it.lastModified(), it.absolutePath))
                        when {
                            extList[0].any { ext -> it.name.endsWith(".$ext") } ->
                                documents.add(MyFile(it.name, it.length(), it.lastModified(), it.absolutePath))
                            extList[1].any { ext -> it.name.endsWith(".$ext") && it.length() > 50 * 1024} ->
                                audio.add(MyFile(it.name, it.length(), it.lastModified(), it.absolutePath)
                            )
                            extList[2].any { ext -> it.name.endsWith(".$ext") && it.length() > 524_288} ->
                                images.add(MyFile(it.name, it.length(), it.lastModified(), it.absolutePath)
                            )
                            extList[3].any { ext -> it.name.endsWith(".$ext") } && it.length() > 3 * 1_048_576->
                                videos.add(MyFile(it.name, it.length(), it.lastModified(), it.absolutePath)
                            )
                            extList[4].any { ext -> it.name.endsWith(".$ext") } ->
                                apks.add(MyFile(it.name, it.length(), it.lastModified(), it.absolutePath)
                            )
                            extList[5].any { ext -> it.name.endsWith(".$ext") } ->
                                archives.add(MyFile(it.name, it.length(), it.lastModified(), it.absolutePath)
                            )
                        }
                    } else {
                        if (it.isDirectory && it.name.equals("Download"))
                            it.listFiles().forEach { file ->
                                downloads.add(
                                    MyFile(file.name, file.length(), file.lastModified(), file.absolutePath))
                            }
                        searchDirectory(it, data, extList)
                    }
                }
            }
        return data
    }

    private fun initAvailableData() {
        binding.apply {
            val storageStatsManager =
                requireActivity().getSystemService(AppCompatActivity.STORAGE_STATS_SERVICE) as StorageStatsManager
            viewLifecycleOwner.lifecycleScope.launch {
                val n1 = storageStatsManager.getFreeBytes(UUID_DEFAULT)
                val n2 = storageStatsManager.getTotalBytes(UUID_DEFAULT)
                val available = android.text.format.Formatter.formatFileSize(requireContext(), n1)
                val totalSpace = android.text.format.Formatter.formatFileSize(requireContext(), n2)
                availableStorageTv.text = "Available $available / $totalSpace "
            }

        }
    }


    private fun checkPermission(permissions: Array<String>): Boolean {
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.i(TAG, "checkPermission: Already granted")
                } else {
                    Log.i(TAG, "checkPermission: Not granted")

                }
            }
        var result = 0
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {

                // Requesting the permission
               requestPermissionLauncher.launch(permission)
            } else {
                Log.i(TAG, "Permission already granted")
                result++
            }
        }
        return result == permissions.size
    }


    // region Boilerplate methods

    private fun initClickListeners(data: MyData) {
        binding.details.setOnClickListener {
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToDetailsFragment()
            )
        }
        data.apply {
            binding.bgDocuments.setOnClickListener {
                findNavController().navigate(
                    getActionToDestination(
                        documents, it.tag.toString()
                    )
                )
            }
            binding.bgDownloads.setOnClickListener {
                findNavController().navigate(
                    getActionToDestination(
                        downloads, it.tag.toString()
                    )
                )
            }
            binding.bgRecent.setOnClickListener {
                findNavController().navigate(
                    getActionToDestination(
                        recent, it.tag.toString()
                    )
                )
            }
            binding.bgImages.setOnClickListener {
                findNavController().navigate(
                    getActionToDestination(
                        images, it.tag.toString()
                    )
                )
            }
            binding.bgVideos.setOnClickListener {
                findNavController().navigate(
                    getActionToDestination(
                        videos, it.tag.toString()
                    )
                )
            }
            binding.bgAudios.setOnClickListener {
                findNavController().navigate(
                    getActionToDestination(
                        audio, it.tag.toString()
                    )
                )
            }
            binding.bgApks.setOnClickListener {
                findNavController().navigate(
                    getActionToDestination(
                        apks, it.tag.toString()
                    )
                )
            }
            binding.bgArchives.setOnClickListener {
                findNavController().navigate(
                    getActionToDestination(
                        archives, it.tag.toString()
                    )
                )
            }
            binding.bgLargeFiles.setOnClickListener {
                findNavController().navigate(
                    getActionToDestination(largeFiles, it.tag.toString())
                )
            }
        }
    }

    private fun getActionToDestination(list: MutableList<MyFile>, title: String): MainFragmentDirections.ActionMainFragmentToCategoryFiles {
        val formattedTitle = title.replace("_", " ").replaceFirstChar(Char::titlecase)
        return MainFragmentDirections.actionMainFragmentToCategoryFiles(MyFolder(list), formattedTitle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                val action = MainFragmentDirections.actionMainFragmentToSearchFragment(data)
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.appbar_menu, menu)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

}