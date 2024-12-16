package com.jojodev.taipeitrash.trashcan.data

import com.jojodev.taipeitrash.trashcan.data.local.entities.TrashCanEntity
import com.jojodev.taipeitrash.trashcan.data.network.models.NetworkTrashCan

data class TrashCan(
    val id: Int,
    val importDate: String,
    val remark: String,
    val address: String,
    val latitude: String,
    val longitude: String,
    val district: String
)
fun NetworkTrashCan.asEntity() =
    TrashCanEntity(
        id = _id,
        importDate = _importdate.date,
        remark = remark,
        address = address,
        longitude = longitude,
        latitude = latitude,
        district = district
    )

fun TrashCanEntity.asExternalModel() =
    TrashCan(
        id = id,
        importDate = importDate,
        remark = remark,
        address = address,
        longitude = longitude,
        latitude = latitude,
        district = district
    )