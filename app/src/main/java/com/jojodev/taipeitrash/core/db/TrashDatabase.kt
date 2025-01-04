package com.jojodev.taipeitrash.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jojodev.taipeitrash.trashcan.data.local.dao.TrashCanDao
import com.jojodev.taipeitrash.trashcan.data.local.entities.TrashCanEntity
import com.jojodev.taipeitrash.trashcar.data.local.entities.TrashCarEntity
import com.jojodev.taipeitrash.trashcar.data.local.dao.TrashCarDao

@Database(
    entities = [TrashCanEntity::class, TrashCarEntity::class],
    version = 1
)
abstract class TrashDatabase: RoomDatabase() {
    abstract fun trashCanDao(): TrashCanDao
    abstract fun trashCarDao(): TrashCarDao
}