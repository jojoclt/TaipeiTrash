package com.jojodev.taipeitrash.trashcar.data.mapper

import com.jojodev.taipeitrash.core.model.DayOfWeek
import com.jojodev.taipeitrash.trashcar.data.TrashCar
import com.jojodev.taipeitrash.trashcar.data.local.entities.TrashCarEntity

fun TrashCarEntity.asExternalModel() =
    TrashCar(
        id = id,
        importDate = importDate,
        address = address,
        longitude = longitude,
        latitude = latitude,
        district = district,
        timeArrive = timeArrive,
        timeLeave = timeLeave,
        trashDays = DayOfWeek.parseFromString(trashDay),
        recycleDays = DayOfWeek.parseFromString(recycleDay)
    )