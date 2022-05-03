package com.konovus.myfiles.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "MyDataTable")
data class MyData(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val documents: MutableList<MyFile> = mutableListOf(),
    val downloads: MutableList<MyFile> = mutableListOf(),
    val recent: MutableList<MyFile> = mutableListOf(),
    val images: MutableList<MyFile> = mutableListOf(),
    val videos: MutableList<MyFile> = mutableListOf(),
    val audio: MutableList<MyFile> = mutableListOf(),
    val apks: MutableList<MyFile> = mutableListOf(),
    val archives: MutableList<MyFile> = mutableListOf(),
    val largeFiles: MutableList<MyFile> = mutableListOf(),
) : Parcelable {
    fun flatten() = listOf(documents, downloads, recent, images, videos, audio, apks, archives, largeFiles).flatten()
}