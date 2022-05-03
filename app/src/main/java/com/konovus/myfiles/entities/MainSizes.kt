package com.konovus.myfiles.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "main_sizes")
data class MainSizes(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val documents: Int = 0,
    val downloads: Int = 0,
    val recent: Int = 0,
    val images: Int = 0,
    val videos: Int = 0,
    val audio: Int = 0,
    val apks: Int = 0,
    val archives: Int = 0,
    val largeFiles: Int = 0
) : Parcelable