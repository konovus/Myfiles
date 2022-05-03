package com.konovus.myfiles.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MyFolder (
    val files: MutableList<MyFile>
) : Parcelable