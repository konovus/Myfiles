package com.konovus.myfiles.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.konovus.myfiles.entities.Converters
import com.konovus.myfiles.entities.MyData

@Database(entities = [MyData::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MyFilesDatabase : RoomDatabase(){

    abstract fun myFilesDao(): MyFilesDao

}