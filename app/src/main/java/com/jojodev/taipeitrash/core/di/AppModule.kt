package com.jojodev.taipeitrash.core.di

import android.content.Context
import androidx.room.Room
import com.jojodev.taipeitrash.trashcan.data.local.db.TrashCanDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideTrashCanDatabase(@ApplicationContext applicationContext: Context) =
        Room.databaseBuilder(
            applicationContext,
            TrashCanDatabase::class.java,
            "trash.db"
        ).build()

    @Provides
    @Singleton
    fun provideTrashCanDao(database: TrashCanDatabase) = database.dao
}