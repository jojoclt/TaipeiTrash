package com.jojodev.taipeitrash.trashcar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jojodev.taipeitrash.trashcar.data.local.entities.TrashCarEntity

@Dao
interface TrashCarDao {
    @Upsert
    suspend fun updateTrashCar(trashCan: List<TrashCarEntity>)

    @Query("DELETE FROM trashcar")
    suspend fun deleteTrashCar()

    @Query("SELECT * FROM trashcar")
    fun getTrashCar(): List<TrashCarEntity>


}