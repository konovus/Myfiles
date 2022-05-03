package com.konovus.myfiles.entities

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class Converters  {

    private val moshi: Moshi =  Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

//    @TypeConverter
//    fun fromEpisodeUrls(data: EpisodeUrls): String {
//        return moshi.adapter(EpisodeUrls::class.java).toJson(data)
//    }
//
//    @TypeConverter
//    fun toEpisodeUrls(json: String): EpisodeUrls {
//        return moshi.adapter(EpisodeUrls::class.java).fromJson(json)!!
//    }

    @TypeConverter
    fun fromStringList(data: List<MyFile>): String{
        val type = Types.newParameterizedType(List::class.java, MyFile::class.java)
        val adapter = moshi.adapter<List<MyFile>>(type)
        return adapter.toJson(data)
    }
    @TypeConverter
    fun toStringList(json: String): List<MyFile> {
        val type = Types.newParameterizedType(List::class.java, MyFile::class.java)
        val adapter = moshi.adapter<List<MyFile>>(type)
        return adapter.fromJson(json)!!
    }

    @TypeConverter
    fun fromMyData(data: MyData): String {
        return moshi.adapter(MyData::class.java).toJson(data)
    }

    @TypeConverter
    fun toMyData(json: String): MyData {
        return moshi.adapter(MyData::class.java).fromJson(json)!!
    }

    @TypeConverter
    fun fromMyFile(file: MyFile): String {
        return moshi.adapter(MyFile::class.java).toJson(file)
    }

    @TypeConverter
    fun toMyFile(json: String): MyFile {
        return moshi.adapter(MyFile::class.java).fromJson(json)!!
    }




}
