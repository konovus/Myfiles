package com.konovus.myfiles.util

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.konovus.myfiles.TAG
import com.konovus.myfiles.entities.MyFile

class MainStateManager {

    private val _toolbarState: MutableLiveData<ToolbarState> = MutableLiveData(ToolbarState.NormalViewState)
    val toolbarState: LiveData<ToolbarState>
        get() = _toolbarState

    private val _selectedItems = MutableLiveData<MutableList<MyFile>?>()
    val selectedItems: MutableLiveData<MutableList<MyFile>?>
        get() = _selectedItems

    private val _isMultiSelectEnabled = MutableLiveData(false)
    val isMultiSelectEnabled: LiveData<Boolean>
        get() = _isMultiSelectEnabled

    private val _deletionTrigger = MutableLiveData(false)
    val deletionTrigger: LiveData<Boolean>
        get() = _deletionTrigger

    fun addOrRemoveItemsFromSelectedList(file: MyFile) {
        var list = _selectedItems.value
        if (list == null)
            list = ArrayList()

        if (list.contains(file))
            list.remove(file)
        else list.add(file)
        _selectedItems.postValue(list)
    }

    fun setDeletionTrigger(value: Boolean) {
        _deletionTrigger.postValue(value)
        Log.i(TAG, "setDeletionTrigger: ${_deletionTrigger.value}")
    }

    fun setToolbarState(state: ToolbarState) =
        _toolbarState.postValue(state)

    fun addAllToSelectedItems(files: MutableList<MyFile>) =
        _selectedItems.postValue(files)

    fun clearSelectedItems() {
        _selectedItems.postValue(null)
        Log.i(TAG, "clearSelectedItems: ${_selectedItems.value?.size}")
    }

    init {
        toolbarState.observeForever {
            Log.i(TAG, "observe ToolbarState: $it")
            if (it == ToolbarState.MultiSelectionState)
                _isMultiSelectEnabled.postValue(true)
            else _isMultiSelectEnabled.postValue(false)
        }
    }

}