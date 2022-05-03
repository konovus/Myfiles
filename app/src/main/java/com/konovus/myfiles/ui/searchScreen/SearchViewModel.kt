package com.konovus.myfiles.ui.searchScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.konovus.myfiles.entities.MyData
import com.konovus.myfiles.entities.MyFile
import com.konovus.myfiles.entities.MyFolder
import dagger.hilt.android.lifecycle.HiltViewModel

class SearchViewModel : ViewModel() {

    val data: MutableLiveData<MyFolder> by lazy { MutableLiveData<MyFolder>() }

}