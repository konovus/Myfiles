package com.konovus.myfiles

import androidx.lifecycle.ViewModel
import com.konovus.myfiles.util.MainStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {

    private val _stateManager = MainStateManager()
    val stateManager: MainStateManager
        get() = _stateManager

    fun setupOnBackPressed(activity: MainActivity) {
        activity.onBackPressed()
    }
}