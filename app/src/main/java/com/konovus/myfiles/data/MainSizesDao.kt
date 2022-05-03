package com.konovus.myfiles.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.konovus.myfiles.entities.MainSizes

@Dao
interface MainSizesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMainSizes(mainSizes: MainSizes)

    @Query("select * from main_sizes")
    fun getMainSizes() : LiveData<MainSizes>
}