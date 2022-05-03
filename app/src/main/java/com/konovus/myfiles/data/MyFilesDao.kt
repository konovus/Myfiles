package com.konovus.myfiles.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.konovus.myfiles.entities.MyData
import kotlinx.coroutines.flow.Flow

@Dao
interface MyFilesDao {

    @Query("select * from mydatatable")
    fun getData(): Flow<MyData>

    @Query("select * from mydatatable")
    fun getDataLiveData(): LiveData<MyData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(myData: MyData)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(myData: MyData)

    @Delete
    suspend fun delete(myData: MyData)
}