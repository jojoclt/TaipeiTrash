package com.jojodev.taipeitrash.trashcan.data.mapper

import com.jojodev.taipeitrash.trashcan.data.TrashCan
import com.jojodev.taipeitrash.trashcan.data.local.entities.TrashCanEntity
import com.jojodev.taipeitrash.trashcan.data.network.models.TaipeiTrashCans

fun TaipeiTrashCans.asEntity() =
    TrashCanEntity(
        id = _id,
        importDate = _importdate.date,
        remark = remark,
        address = address,
        longitude = longitude.toDouble(),
        latitude = latitude.toDouble(),
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