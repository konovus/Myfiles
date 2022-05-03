package com.konovus.myfiles.di


import android.app.Application
import androidx.room.Room
import com.konovus.myfiles.data.MainSizesDao
import com.konovus.myfiles.data.MainSizesDatabase
import com.konovus.myfiles.data.MyFilesDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =  Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @Provides
    @Singleton
    fun provideDatabase(app: Application): MyFilesDatabase =
        Room.databaseBuilder(app, MyFilesDatabase::class.java, "myFiles_db")
            .fallbackToDestructiveMigration()
            .build()
    @Provides
    @Singleton
    fun provideMainSizesDatabase(app: Application): MainSizesDatabase =
        Room.databaseBuilder(app, MainSizesDatabase::class.java, "main_sizes_db")
            .fallbackToDestructiveMigration()
            .build()


    @Provides
    fun provideMyFilesDao(db: MyFilesDatabase) = db.myFilesDao()
    @Provides
    fun provideMainSizesDao(db: MainSizesDatabase) = db.mainSizesDao()
}