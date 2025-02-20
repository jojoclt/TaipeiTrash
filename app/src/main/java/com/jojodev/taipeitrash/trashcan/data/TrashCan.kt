package com.jojodev.taipeitrash.trashcan.data

import com.google.android.gms.maps.model.LatLng
import com.jojodev.taipeitrash.trashcan.data.local.entities.TrashCanEntity
import com.jojodev.taipeitrash.trashcan.data.network.models.NetworkTrashCan

data class TrashCan(
    val id: Int,
    val importDate: String,
    val remark: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val district: String
) {
    fun toLatLng() = LatLng(latitude, longitude)
}
fun NetworkTrashCan.asEntity() =
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