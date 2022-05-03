package com.konovus.myfiles.ui.detailsScreen

import androidx.lifecycle.ViewModel
import com.konovus.myfiles.data.MyFilesDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    val dao: MyFilesDao
) : ViewModel(){


    private val _data = dao.getDataLiveData()
    val data = _data


}