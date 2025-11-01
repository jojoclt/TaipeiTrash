package com.jojodev.taipeitrash.trashcar.data

import androidx.compose.runtime.Stable
import com.google.android.gms.maps.model.LatLng
import com.jojodev.taipeitrash.core.model.TrashModel

@Stable
data class TrashCar(
    override val id: Int,
    override val importDate: String,
    override val address: String,
    override val latitude: Double,
    override val longitude: Double,
    override val district: String,
    val timeArrive: String,
    val timeLeave: String
) : TrashModel

fun TrashCar?.toLatLng() = this?.let {
    LatLng(it.latitude, it.longitude)
} ?: LatLng(0.0, 0.0)