package com.konovus.myfiles.ui.categoryFilesScreen

import androidx.lifecycle.ViewModel
import com.konovus.myfiles.util.MainStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor() :ViewModel() {

    private val _stateManager = MainStateManager()
    val stateManager: MainStateManager
        get() = _stateManager
}