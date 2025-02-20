package com.jojodev.taipeitrash.trashcar.data

import com.google.android.gms.maps.model.LatLng
import com.jojodev.taipeitrash.trashcar.data.local.entities.TrashCarEntity
import com.jojodev.taipeitrash.trashcar.data.network.models.NetworkTrashCar

data class TrashCar(
    val id: Int,
    val importDate: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val district: String,
    val timeArrive: String,
    val timeLeave: String
) {
    fun toLatLng() = LatLng(latitude, longitude)
}

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