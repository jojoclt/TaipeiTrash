package com.jojodev.taipeitrash.trashcar.data

import androidx.compose.runtime.Stable
import com.google.android.gms.maps.model.LatLng
import com.jojodev.taipeitrash.core.model.DayOfWeek
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
    val timeLeave: String,
    // Schedule fields - only for TrashCar (garbage trucks)
    val isFixed: Boolean,
    val trashDays: Set<DayOfWeek> = emptySet(),  // Days when trash is collected
    val recycleDays: Set<DayOfWeek> = emptySet() // Days when recycling is collected
) : TrashModel {
    /**
     * Check if trash collection is available on the given day
     */
    fun isTrashDayAvailable(day: DayOfWeek): Boolean {
        return trashDays.contains(day)
    }

    /**
     * Check if recycling collection is available on the given day
     */
    fun isRecycleDayAvailable(day: DayOfWeek): Boolean {
        return recycleDays.contains(day)
    }

    /**
     * Check if any collection (trash or recycle) is available on the given day
     */
    fun isAvailableOn(day: DayOfWeek): Boolean {
        return isTrashDayAvailable(day) || isRecycleDayAvailable(day)
    }
}

fun TrashCar?.toLatLng() = this?.let {
    LatLng(it.latitude, it.longitude)
} ?: LatLng(0.0, 0.0)