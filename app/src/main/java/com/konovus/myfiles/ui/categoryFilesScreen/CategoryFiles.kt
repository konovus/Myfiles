package com.konovus.myfiles.ui.categoryFilesScreen

import android.R.attr
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.konovus.myfiles.*
import com.konovus.myfiles.databinding.CategoryFilesFragmentBinding
import com.konovus.myfiles.entities.MyData
import com.konovus.myfiles.entities.MyFile
import com.konovus.myfiles.entities.MyFolder
import com.konovus.myfiles.ui.dialogScreen.SafetyDialogFragment
import com.konovus.myfiles.ui.searchScreen.SearchAdapter
import com.konovus.myfiles.util.ToolbarState
import com.konovus.myfiles.util.onQueryTextChanged
import kotlinx.coroutines.*
import java.io.File
import java.util.function.Predicate


@RequiresApi(Build.VERSION_CODES.Q)
class CategoryFiles : Fragment(R.layout.category_files_fragment),
    CategoryFilesAdapter.OnItemClickListener {

    private val args by navArgs<CategoryFilesArgs>()
    private var _binding: CategoryFilesFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by viewModels()

    private lateinit var adapter: CategoryFilesAdapter
    private lateinit var folder: MyFolder
    private var files: MutableList<MyFile> = mutableListOf()
    private lateinit var actionBar: ActionBar
    private lateinit var toolbar: Toolbar
    private val selectedMap: MutableMap<Int, MyFile> by lazy { mutableMapOf() }
    private lateinit var title: String
    companion object {
        val albumCoverList = mutableMapOf<String, ByteArray>()
        const val DELETE_REQUEST_CODE = 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = CategoryFilesFragmentBinding.bind(view)
        
        MainActivity.tabLayout.visibility = View.GONE
        folder = args.folder

        adapter = CategoryFilesAdapter(this@CategoryFiles,
            viewModel.stateManager.selectedItems, viewModel.stateManager.isMultiSelectEnabled)
        toolbar = requireActivity().findViewById(R.id.my_toolbar)
        actionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)
        title = toolbar.title.toString()

        binding.apply {

            recyclerView.adapter = adapter
            adapter.submitList(folder.files)

            lifecycleScope.launch(Dispatchers.IO) {
                val mmr = MediaMetadataRetriever()
                folder.files.forEach { file ->

                    if (file.name.endsWith("mp3")) {
                        mmr.setDataSource(file.uri)
                        val data = mmr.embeddedPicture
                        if (data != null && !albumCoverList.containsKey(file.name))
                            albumCoverList[file.name] = data
                    }
                }
            }
        }

        setupMultiSelection()
        setHasOptionsMenu(true)
    }
    
    private fun setupMultiSelection() {
        viewModel.stateManager.toolbarState.observe(viewLifecycleOwner) { state ->
            when(state) {
                ToolbarState.NormalViewState -> {
                    setNormalToolbar()
                }
                ToolbarState.MultiSelectionState -> {
                    setMultiSelectionToolbar()
                }
            }
        }
    }
    
    private fun setNormalToolbar() {
        Log.i(TAG, "setNormalToolbar: SET")
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener {
            MainActivity.activityInstance.onBackPressed()
        }
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.category_files_appbar_menu)
        toolbar.title = title
        viewModel.stateManager.clearSelectedItems()
        viewModel.stateManager.selectedItems.removeObservers(viewLifecycleOwner)
    }
    
    private fun setMultiSelectionToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.appbar_multiselect_menu)
        toolbar.setNavigationOnClickListener {
            viewModel.stateManager.setToolbarState(ToolbarState.NormalViewState)
            setNormalToolbar()
        }
        viewModel.stateManager.selectedItems.observe(viewLifecycleOwner) {
                if (it == null)
                    toolbar.title = "0 Selected"
                else toolbar.title = "${it.size} Selected"
        }
    }

    


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.sort_by_name -> {
                files = folder.files.sortedBy { it.name }.toMutableList()
                adapter.submitList(files)
                binding.wrapper.postDelayed(
                    { binding.recyclerView.layoutManager?.scrollToPosition(0) },
                    50
                )
                true
            }
            R.id.sort_by_date -> {
                files = folder.files.sortedBy { it.date }.toMutableList()
                adapter.submitList(files)
                binding.wrapper.postDelayed(
                    { binding.recyclerView.layoutManager?.scrollToPosition(0) },
                    50
                )
                true
            }
            R.id.sort_by_size -> {
                files = folder.files.sortedBy { it.size }.toMutableList()
                adapter.submitList(files)
                binding.wrapper.postDelayed(
                    { binding.recyclerView.layoutManager?.scrollToPosition(0) },
                    50
                )
                true
            }
            R.id.order -> {
                if (files.isEmpty())
                    files = folder.files
                files = files.reversed().toMutableList()
                adapter.submitList(files)
                binding.wrapper.postDelayed(
                    { binding.recyclerView.layoutManager?.scrollToPosition(0) },
                    100
                )
                true
            }
            R.id.select_all -> {
                if (viewModel.stateManager.selectedItems.value?.size == folder.files.size) {
                    selectedMap.clear()
                    viewModel.stateManager.clearSelectedItems()
                    val selectAllIcon = toolbar.menu.findItem(R.id.select_all)
                    selectAllIcon.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_select_all_24, context?.theme)
                } else {
                    folder.files.forEachIndexed { i, file -> selectedMap[i] = file }
                    viewModel.stateManager.addAllToSelectedItems(folder.files.toMutableList())
                    val selectAllIcon = toolbar.menu.findItem(R.id.select_all)
                    selectAllIcon.icon = ResourcesCompat.getDrawable(resources, R.drawable.all_checked, context?.theme)
                }
                true
            }
            R.id.delete -> {
                SafetyDialogFragment().newInstance(selectedMap.entries.size).show(childFragmentManager, "safety")
                SafetyDialogFragment.deletionTrigger.observe(viewLifecycleOwner) {
                    if (it!!) {
                        val removedFiles = mutableListOf<File>()
                        val sortedSelectedMap = selectedMap.entries.sortedBy { entry -> entry.key }
                        sortedSelectedMap.forEach { entry ->
                            val file = File(entry.value.uri)
                            if (!file.isDirectory || file.listFiles()?.isEmpty() == true) {
                                if (files.size > 0) files.removeAt(entry.key)
                                folder.files.removeAt(entry.key - removedFiles.size)
                                adapter.notifyItemRemoved(entry.key - removedFiles.size)
                                removedFiles.add(file)
                                viewModel.stateManager.addOrRemoveItemsFromSelectedList(entry.value)
                                file.delete()
                            } else Toast.makeText(requireContext(), "Cannot delete a non-empty Folder!", Toast.LENGTH_SHORT).show()
                        }
                        SafetyDialogFragment.deletionTrigger.postValue(false)
                        removedFiles.forEach { f ->  selectedMap.values.removeIf{file -> file.name == f.name} }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.category_files_appbar_menu, menu)

        var queryTextChangedJob: Job? = null
        val searchItem = menu.findItem(R.id.search)

        val searchView = searchItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        val data = args.folder.files


        searchView.onQueryTextChanged(searchView) { query ->
            queryTextChangedJob?.cancel()
            SearchAdapter.counterInt = 1
            val searchList = mutableListOf<MyFile>()


            queryTextChangedJob = lifecycleScope.launch {
                delay(500)
                if (query.isNotEmpty()) {
                    data.forEach {
                        if (it.name.lowercase().contains(query))
                            searchList.add(it)
                    }
                    adapter.submitList(searchList)
                } else adapter.submitList(data)
            }
        }
    }

    override fun onItemClick(file: MyFile, position: Int) {
        if (viewModel.stateManager.isMultiSelectEnabled.value!!)
            onCheckBoxClick(file, position)
        else openClickedFile(file, position)
    }

    private fun openClickedFile(file: MyFile, position: Int) {
        if (file.name.endsWith("mp3")) {
            val intent = Intent()
            val audio = File(file.uri)
            intent.action = Intent.ACTION_VIEW
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            intent.setDataAndType(
                FileProvider.getUriForFile(requireContext(),
                    BuildConfig.APPLICATION_ID + ".provider",audio), "audio/*")
            startActivity(intent)
        } else if (file.name.endsWith("mp4")) {
            val action = CategoryFilesDirections.actionCategoryFilesToVideoPlayerFragment(file.uri)
            findNavController().navigate(action)
        } else if (file.name.endsWith("jpg") || file.name.endsWith("jpeg")) {
            val action = CategoryFilesDirections.actionCategoryFilesToPagerFragment(MyData(images = folder.files), position)
            findNavController().navigate(action)
        } else if (arrayListOf("pdf", "epub", "doc", "docx", "txt", "djvu", "pdb", "fb2", "prc", "mobi")
                .contains(file.name.split(".").last()))
            openFileWithIntent(file.uri)
    }

    private fun openFileWithIntent(path: String) {
        val file = File(path)
        val map = MimeTypeMap.getSingleton()
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)
        var type = map.getMimeTypeFromExtension(ext)
        if (path.endsWith("docx"))
            type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        Log.i(TAG, "openFileWithIntent: $type")
        if (file.exists() && type == null) type = "*/*"
        Log.i(TAG, "openFileWithIntent: $type")
        val intent = Intent(Intent.ACTION_VIEW)

        val uri = FileProvider.getUriForFile(requireContext(), "com.konovus.myfiles.provider", file)
        intent.setDataAndType(uri, type)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION )
        startActivity(intent)
    }

    override fun onLongItemClick(file: MyFile, position: Int) {
        viewModel.stateManager.setToolbarState(ToolbarState.MultiSelectionState)
        viewModel.stateManager.addOrRemoveItemsFromSelectedList(file)
        selectedMap[position] = file
    }

    override fun onCheckBoxClick(file: MyFile, position: Int) {

        if (selectedMap.containsKey(position))
            selectedMap.remove(position)
        else selectedMap[position] = file
        viewModel.stateManager.addOrRemoveItemsFromSelectedList(file)
        val selectAllIcon = toolbar.menu.findItem(R.id.select_all)
        if (viewModel.stateManager.selectedItems.value?.size != folder.files.size)
            selectAllIcon.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_select_all_24, context?.theme)
         else
            selectAllIcon.icon = ResourcesCompat.getDrawable(resources, R.drawable.all_checked, context?.theme)


    }

}