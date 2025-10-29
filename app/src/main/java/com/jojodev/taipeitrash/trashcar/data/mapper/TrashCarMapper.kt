package com.jojodev.taipeitrash.trashcar.data.mapper

import com.jojodev.taipeitrash.trashcar.data.TrashCar
import com.jojodev.taipeitrash.trashcar.data.local.entities.TrashCarEntity
import com.jojodev.taipeitrash.trashcar.data.network.models.NetworkTrashCar

fun NetworkTrashCar.asEntity() =
    TrashCarEntity(
        id = id,
        importDate = importdate.date,
        address = address,
        longitude = longitude.toDouble(),
        latitude = latitude.toDouble(),
        district = district,
        timeArrive = timeArrive,
        timeLeave = timeLeave
    )

fun TrashCarEntity.asExternalModel() =
    TrashCar(
        id = id,
        importDate = importDate,
        address = address,
        longitude = longitude,
        latitude = latitude,
        district = district,
        timeArrive = timeArrive,
        timeLeave = timeLeave
    )