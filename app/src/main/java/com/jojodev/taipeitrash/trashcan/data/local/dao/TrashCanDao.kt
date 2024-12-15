package com.jojodev.taipeitrash.trashcan.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jojodev.taipeitrash.trashcan.data.local.entities.TrashCanEntity

@Dao
interface TrashCanDao {
    @Upsert
    suspend fun updateTrash(trashCan: List<TrashCanEntity>)

    @Query("DELETE FROM trashcan")
    suspend fun deleteTrash()

    @Query("SELECT * FROM trashcan")
    fun getTrashCan(): List<TrashCanEntity>


}