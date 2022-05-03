package com.konovus.myfiles.ui.storageExplorerScreen

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.webkit.MimeTypeMap
import android.widget.HorizontalScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.allViews
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.konovus.myfiles.BuildConfig
import com.konovus.myfiles.MainActivity
import com.konovus.myfiles.R
import com.konovus.myfiles.TAG
import com.konovus.myfiles.databinding.StorageExplorerFragmentBinding
import com.konovus.myfiles.databinding.StorageExplorerItemBinding
import com.konovus.myfiles.entities.MyData
import com.konovus.myfiles.entities.MyFile
import com.konovus.myfiles.ui.categoryFilesScreen.CategoryFilesAdapter
import com.konovus.myfiles.ui.categoryFilesScreen.CategoryFilesDirections
import com.konovus.myfiles.ui.dialogScreen.SafetyDialogFragment
import com.konovus.myfiles.ui.mainScreen.MainFragmentDirections
import com.konovus.myfiles.ui.searchScreen.SearchAdapter
import com.konovus.myfiles.util.ToolbarState
import com.konovus.myfiles.util.onQueryTextChanged
import com.konovus.myfiles.util.removeFromTo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.Duration

@RequiresApi(Build.VERSION_CODES.R)
class StorageExplorerFragment : Fragment(R.layout.storage_explorer_fragment),
    CategoryFilesAdapter.OnItemClickListener {

    private var _binding: StorageExplorerFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StorageExplorerViewModel by viewModels()
    private val files = mutableListOf<MyFile>()
    private val selectedMap: MutableMap<Int, MyFile> by lazy { mutableMapOf() }
    private lateinit var toolbar: Toolbar
    private lateinit var title: String
    private val adapter by lazy { CategoryFilesAdapter(
        this,
        viewModel.stateManager.selectedItems,
        viewModel.stateManager.isMultiSelectEnabled
    ) }
    companion object{
        private var currentDir: MyFile = MyFile()
        private val navNameItems = mutableMapOf<MyFile, Int>()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = StorageExplorerFragmentBinding.bind(view)

//        here i am making the tabLayout visible, because after watching a video the tabLayout is hidden
        MainActivity.tabLayout.visibility = View.VISIBLE

        toolbar = requireActivity().findViewById(R.id.my_toolbar)
        toolbar.inflateMenu(R.menu.main_appbar_menu)
        title = toolbar.title.toString()
//      i check if it's night mode than i have to change the navigation panel's color
        if (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_NO)
            binding.nestedScroll.background = ContextCompat.getDrawable(requireContext(), R.color.lighter_gray)

        binding.recyclerView.adapter = adapter
//        here i'm restoring the state of the navigation, so when the user goes back to the explorer fragment, he's gonna
//        get to the same folder where he left
        if (currentDir.uri.isNotEmpty()) {
            setupDataForRecyclerView(pathName = currentDir.uri)
            navNameItems.keys.forEach {
                setupNavigationItem(it)
            }
            navNameItems.entries.removeIf { it.value > binding.explorerPathWrapper.size}
            changeColorsToNavItems()
        } else {
            setupDataForRecyclerView(pathName =  "${Environment.getStorageDirectory()}/emulated/0" )
            val storage = File("${Environment.getStorageDirectory()}/emulated/0")
            setupNavigationItem(MyFile(storage.name, storage.length(), storage.lastModified(), storage.absolutePath, storage.isDirectory))
        }

        setupMultiSelection()
        handleBackButtonPress()
        setHasOptionsMenu(true)
    }

    private fun setupMultiSelection() {
        viewModel.stateManager.toolbarState.observe(viewLifecycleOwner) { state ->
            when(state) {
                ToolbarState.NormalViewState -> {
                    Log.i(TAG, "setupMultiSelection: catch from ToolbarState")
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

        toolbar.navigationIcon = null
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.appbar_menu)
        viewModel.stateManager.clearSelectedItems()
        toolbar.title = title
        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setMultiSelectionToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.appbar_multiselect_menu)
        toolbar.setNavigationOnClickListener {
            viewModel.stateManager.setToolbarState(ToolbarState.NormalViewState)
        }
        viewModel.stateManager.selectedItems.observe(viewLifecycleOwner) {
                if (it != null)
                    toolbar.title = "${it.size} Selected"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                val action = StorageExplorerFragmentDirections.actionStorageExplorerFragmentToSearchFragment(MyData(documents = files))
                findNavController().navigate(action)
                true
            }
            R.id.select_all -> {
                if (viewModel.stateManager.selectedItems.value?.size == files.size) {
                    selectedMap.clear()
                    viewModel.stateManager.clearSelectedItems()
                    toolbar.title = "0 Selected"
                    val selectAllIcon = toolbar.menu.findItem(R.id.select_all)
                    selectAllIcon.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_select_all_24, context?.theme)
                } else {
                    files.forEachIndexed { i, file -> selectedMap[i] = file }
                    viewModel.stateManager.addAllToSelectedItems(files.toMutableList())
                    val selectAllIcon = toolbar.menu.findItem(R.id.select_all)
                    selectAllIcon.icon = ResourcesCompat.getDrawable(resources, R.drawable.all_checked, context?.theme)
                }
                true
            }
            R.id.delete -> {
                adapter.submitList(files)
                SafetyDialogFragment().newInstance(selectedMap.entries.size).show(childFragmentManager, "safety")
                SafetyDialogFragment.deletionTrigger.observe(viewLifecycleOwner) {
                    if (it!!) {
                        val removedFiles = mutableListOf<File>()
                        val sortedSelectedMap = selectedMap.entries.sortedBy { entry -> entry.key }
                        sortedSelectedMap.forEach { entry ->
                            val file = File(entry.value.uri)
                            if (!file.isDirectory || file.listFiles()?.isEmpty() == true) {
                                files.removeAt(entry.key - removedFiles.size)
                                adapter.notifyItemRemoved(entry.key - removedFiles.size)
                                removedFiles.add(file)
                                viewModel.stateManager.addOrRemoveItemsFromSelectedList(entry.value)
//                                file.delete()
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
        inflater.inflate(R.menu.appbar_menu, menu)

    }

    private fun handleBackButtonPress() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // if we are in the root directory, on back press we just switch to the other tab
                if (navNameItems.keys.size == 1)
                    MainActivity.tabLayout.getTabAt(0)?.select()
                // if we are in a subdirectory then we just go up the directory tree first
                else {
                    navNameItems.remove(navNameItems.keys.last())
                    binding.explorerPathWrapper.removeViewAt(binding.explorerPathWrapper.childCount - 1)
                    changeColorsToNavItems()
                    setupDataForRecyclerView(navNameItems.keys.last().uri)
                }
            }
        })
    }

    private fun setupDataForRecyclerView(pathName: String) {
        Log.i(TAG, "setupDataForRecyclerView + $pathName ")
        files.clear()
        val root = File(pathName)
        root.listFiles()?.forEach { files.add(MyFile(it.name, it.length(), it.lastModified(), it.absolutePath, it.isDirectory)) }
//        i'm using file.toList() to create a new list , so that the differ in the adapter can do the diffing
        adapter.submitList(files.toList())
        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(file: MyFile, position: Int) {
        if (viewModel.stateManager.isMultiSelectEnabled.value!!)
            onCheckBoxClick(file, position)
        else {
            if (file.isFolder) {
                currentDir = file
                setupDataForRecyclerView(file.uri)
                setupNavigationItem(file)
                changeColorsToNavItems()
                binding.nestedScroll.post { binding.nestedScroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT) }
            } else openClickedFile(file, position)
        }
    }

    override fun onLongItemClick(file: MyFile, position: Int) {
        viewModel.stateManager.setToolbarState(ToolbarState.MultiSelectionState)
        viewModel.stateManager.addOrRemoveItemsFromSelectedList(file)
        selectedMap[position] = file
    }

    override fun onCheckBoxClick(file: MyFile, position: Int) {
        Log.i(TAG, "onCheckBoxClick: $position : ${file.name}")
        if (selectedMap.containsKey(position))
            selectedMap.remove(position)
        else selectedMap[position] = file
        viewModel.stateManager.addOrRemoveItemsFromSelectedList(file)
        val selectAllIcon = toolbar.menu.findItem(R.id.select_all)
        if (viewModel.stateManager.selectedItems.value?.size != files.size)
            selectAllIcon.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_select_all_24, context?.theme)
        else
            selectAllIcon.icon = ResourcesCompat.getDrawable(resources, R.drawable.all_checked, context?.theme)

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
            val action = StorageExplorerFragmentDirections.actionStorageExplorerFragmentToVideoPlayerFragment(file.uri)
            findNavController().navigate(action)
        } else if (file.name.endsWith("jpg") || file.name.endsWith("jpeg")) {
            val action = StorageExplorerFragmentDirections.actionStorageExplorerFragmentToPagerFragment(MyData(images = files), position)
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

    private fun setupNavigationItem(file: MyFile) {

        val navigationItem = layoutInflater.inflate(R.layout.storage_explorer_item, null)
        val navBinding = StorageExplorerItemBinding.bind(navigationItem)
        navBinding.folderNavName.text = if (file.name == "0") "Storage" else file.name
        binding.explorerPathWrapper.addView(navigationItem)
        navNameItems[file] = binding.explorerPathWrapper.size
        navBinding.folderNavName.setOnClickListener {

            currentDir = file
            binding.explorerPathWrapper.removeViews(navNameItems[file]!!, binding.explorerPathWrapper.size - navNameItems[file]!!)
            changeColorsToNavItems()
            setupDataForRecyclerView(file.uri)
            navNameItems.entries.removeIf { entry -> entry.value > binding.explorerPathWrapper.size}
            it.postDelayed({ binding.recyclerView.layoutManager?.scrollToPosition(0) }, 100)
        }
    }

    private fun changeColorsToNavItems(){
        var mediumColor = ContextCompat.getColor(requireContext(), R.color.medium_gray)
        var lightColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
        if (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_NO) {
                mediumColor = ContextCompat.getColor(requireContext(), R.color.gray)
                lightColor = ContextCompat.getColor(requireContext(), R.color.medium_gray)
        }
        val listOfTextViews: MutableList<TextView> = binding.explorerPathWrapper.children.map {
            (it as ViewGroup).children
        }.flatten().filterIsInstance<TextView>().toMutableList()

        listOfTextViews.forEach { view ->
            view.setTextColor(mediumColor)
        }

        listOfTextViews.last().setTextColor(lightColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

