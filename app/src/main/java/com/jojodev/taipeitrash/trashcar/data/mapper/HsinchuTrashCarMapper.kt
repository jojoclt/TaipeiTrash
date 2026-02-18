package com.jojodev.taipeitrash.trashcar.data.mapper

import com.jojodev.taipeitrash.trashcar.data.local.entities.TrashCarEntity
import com.jojodev.taipeitrash.trashcar.data.network.models.HsinchuPointData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun HsinchuPointData.asEntity(
    currentDate: String = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
): TrashCarEntity {
    val times = time.split("~")
    val timeArrive = times.getOrNull(0)?.trim() ?: time
    val timeLeave = times.getOrNull(1)?.trim() ?: time

    return TrashCarEntity(
        id = pointId.toIntOrNull() ?: 0,
        importDate = currentDate,
        address = pointName,
        longitude = longitude.toDouble(),
        latitude = latitude.toDouble(),
        district = district,
        timeArrive = timeArrive,
        timeLeave = timeLeave,
        trashDay = trashDay,
        recycleDay = recycleDay,
        isFixed = taskType == "10"
    )
}