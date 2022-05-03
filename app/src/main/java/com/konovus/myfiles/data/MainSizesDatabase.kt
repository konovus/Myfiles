package com.konovus.myfiles.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.konovus.myfiles.entities.Converters
import com.konovus.myfiles.entities.MainSizes
import com.konovus.myfiles.entities.MyData

@Database(entities = [MainSizes::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MainSizesDatabase : RoomDatabase(){

    abstract fun mainSizesDao(): MainSizesDao

}