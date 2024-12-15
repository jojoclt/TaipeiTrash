package com.jojodev.taipeitrash.trashcan.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trashcan")
data class TrashCanEntity(
    @PrimaryKey
    val id: Int,
    val importDate: String,
    val remark: String,
    val address: String,
    val longitude: String,
    val latitude: String,
    val district: String
)