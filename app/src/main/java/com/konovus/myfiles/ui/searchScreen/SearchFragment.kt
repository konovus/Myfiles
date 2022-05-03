package com.konovus.myfiles.ui.searchScreen

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.konovus.myfiles.MainActivity
import com.konovus.myfiles.R
import com.konovus.myfiles.TAG
import com.konovus.myfiles.databinding.MainFragmentBinding
import com.konovus.myfiles.databinding.SearchFragmentBinding
import com.konovus.myfiles.entities.MyFile
import com.konovus.myfiles.entities.MyFolder
import com.konovus.myfiles.util.onActionExpanded
import com.konovus.myfiles.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.app.SearchManager
import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import android.app.Activity
import android.inputmethodservice.InputMethodService

import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.getSystemService


class SearchFragment : Fragment(R.layout.search_fragment), SearchAdapter.OnItemClickListener {

    private var _binding: SearchFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchView: SearchView
    private val args by navArgs<SearchFragmentArgs>()
    private val viewModel by viewModels<SearchViewModel>()
    private val adapter = SearchAdapter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = SearchFragmentBinding.bind(view)

        MainActivity.tabLayout.visibility = View.GONE

        binding.apply {
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapter

        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.appbar_menu, menu)

        var queryTextChangedJob: Job? = null
        val searchItem = menu.findItem(R.id.search)

        searchView = searchItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchItem.expandActionView()

        val data = args.data.flatten()

        searchItem.onActionExpanded {
            MainActivity.activityInstance.onBackPressed()
//            val action = SearchFragmentDirections.actionSearchFragmentToMainFragment()
//            findNavController().navigate(action)
        }

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
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        MainActivity.tabLayout.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onItemClick(file: MyFile) {

    }
}
