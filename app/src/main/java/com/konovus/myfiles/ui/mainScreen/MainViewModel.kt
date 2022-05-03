package com.konovus.myfiles.ui.mainScreen

import android.util.Log
import androidx.lifecycle.*
import com.konovus.myfiles.TAG
import com.konovus.myfiles.data.MainSizesDao
import com.konovus.myfiles.data.MyFilesDao
import com.konovus.myfiles.entities.MainSizes
import com.konovus.myfiles.entities.MyData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dao: MyFilesDao,
    private val mainSizesDao: MainSizesDao
    ) : ViewModel() {

    private val _data = dao.getDataLiveData().distinctUntilChanged()
    val data = _data

    private val _mainSizes = mainSizesDao.getMainSizes()
    val mainSizes = _mainSizes

    fun updateMainSizes(data: MyData){
        data.apply {
            viewModelScope.launch {
                mainSizesDao.insertMainSizes(MainSizes(1, documents.size, downloads.size, recent.size, images.size,
                videos.size, audio.size, apks.size, archives.size, largeFiles.size))
            }
        }
    }
    fun updateData(data: MyData) {
        viewModelScope.launch {
            Log.i(TAG, "updateData: Data updated")
            dao.insert(data)
        }
    }
}