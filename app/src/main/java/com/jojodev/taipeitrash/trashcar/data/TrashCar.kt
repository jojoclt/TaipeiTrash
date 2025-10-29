package com.jojodev.taipeitrash.trashcar.data

import androidx.compose.runtime.Stable
import com.google.android.gms.maps.model.LatLng

@Stable
data class TrashCar(
    val id: Int,
    val importDate: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val district: String,
    val timeArrive: String,
    val timeLeave: String
)
fun TrashCar?.toLatLng() = this?.let {
    LatLng(it.latitude, it.longitude)
} ?: LatLng(0.0, 0.0)