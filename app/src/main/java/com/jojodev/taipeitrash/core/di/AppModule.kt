package com.jojodev.taipeitrash.core.di

import android.content.Context
import androidx.room.Room
import com.jojodev.taipeitrash.core.db.TrashDatabase
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
    fun provideTrashDatabase(@ApplicationContext applicationContext: Context) =
        Room.databaseBuilder(
            applicationContext,
            TrashDatabase::class.java,
            "trash.db"
        ).build()

    @Provides
    @Singleton
    fun provideTrashCanDao(database: TrashDatabase) = database.trashCanDao()

    @Provides
    @Singleton
    fun provideTrashCarDao(database: TrashDatabase) = database.trashCarDao()
}