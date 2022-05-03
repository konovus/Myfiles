package com.konovus.myfiles.entities

import android.os.Parcelable
import androidx.room.PrimaryKey
import com.konovus.myfiles.util.sizeInMb
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class MyFile(
    val name: String = "",
    val size: Long = 1,
    @PrimaryKey
    val date: Long = 1,
    val uri: String = "",
    val isFolder: Boolean = false
) : Parcelable {

    fun size(): String {
        val size = this.size / 1024
        if (size < 1024)
            return "$size KB"
        else return "${size / 1024} MB"
    }

    fun date(): String {
        val date = Date(this.date)
        val format = SimpleDateFormat("MMM dd, yyyy")
        return format.format(date)
    }
}
