package com.konovus.myfiles.ui.storageExplorerScreen

import androidx.lifecycle.ViewModel
import com.konovus.myfiles.util.MainStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StorageExplorerViewModel @Inject constructor() : ViewModel() {

    private val _stateManager = MainStateManager()
    val stateManager: MainStateManager
        get() = _stateManager
}