package com.jojodev.taipeitrash.trashcar.data.mapper

import com.jojodev.taipeitrash.trashcar.data.local.entities.TrashCarEntity
import com.jojodev.taipeitrash.trashcar.data.network.models.TaipeiTrashCars

fun TaipeiTrashCars.asEntity() =
    TrashCarEntity(
        id = id,
        importDate = importdate.date,
        address = address,
        longitude = longitude.toDouble(),
        latitude = latitude.toDouble(),
        district = district,
        timeArrive = timeArrive,
        timeLeave = timeLeave,
        trashDay = "1,2,4,5,6",  // Taipei doesn't have per-item schedules
        recycleDay = "1,2,4,5,6"
    )