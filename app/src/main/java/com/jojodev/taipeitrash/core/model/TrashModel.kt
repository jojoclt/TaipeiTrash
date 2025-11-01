package com.jojodev.taipeitrash.core.model

import com.google.android.gms.maps.model.LatLng

/**
 * Base interface for all trash-related models (TrashCan, TrashCar)
 */
interface TrashModel {
    val id: Int
    val importDate: String
    val address: String
    val latitude: Double
    val longitude: Double
    val district: String

    fun toLatLng(): LatLng = LatLng(latitude, longitude)
}

/**
 * Type of trash item for UI display
 */
enum class TrashType {
    TRASH_CAN,
    GARBAGE_TRUCK
}

