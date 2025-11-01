package com.jojodev.taipeitrash.trashcan.data

import androidx.compose.runtime.Stable
import com.google.android.gms.maps.model.LatLng
import com.jojodev.taipeitrash.core.model.TrashModel

@Stable
data class TrashCan(
    override val id: Int,
    override val importDate: String,
    val remark: String,
    override val address: String,
    override val latitude: Double,
    override val longitude: Double,
    override val district: String
) : TrashModel

fun TrashCan?.toLatLng() = this?.let {
    LatLng(it.latitude, it.longitude)
} ?: LatLng(0.0, 0.0)