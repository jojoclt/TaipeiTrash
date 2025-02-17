package com.jojodev.taipeitrash.trashcar.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trashcar")
data class TrashCarEntity(
    @PrimaryKey
    val id: Int,
    val importDate: String,
    val address: String,
    val longitude: Double,
    val latitude: Double,
    val district: String,
    val timeArrive: String,
    val timeLeave: String,
)