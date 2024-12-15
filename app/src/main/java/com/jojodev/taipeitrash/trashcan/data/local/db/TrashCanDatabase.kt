package com.jojodev.taipeitrash.trashcan.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jojodev.taipeitrash.trashcan.data.local.dao.TrashCanDao
import com.jojodev.taipeitrash.trashcan.data.local.entities.TrashCanEntity

@Database(
    entities = [TrashCanEntity::class],
    version = 1
)
abstract class TrashCanDatabase: RoomDatabase() {
    abstract val dao: TrashCanDao
}