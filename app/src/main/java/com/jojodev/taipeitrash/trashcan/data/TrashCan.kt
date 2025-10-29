package com.jojodev.taipeitrash.trashcan.data

import androidx.compose.runtime.Stable
import com.google.android.gms.maps.model.LatLng

@Stable
data class TrashCan(
    val id: Int,
    val importDate: String,
    val remark: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val district: String
)
fun TrashCan?.toLatLng() = this?.let {
    LatLng(it.latitude, it.longitude)
} ?: LatLng(0.0, 0.0)