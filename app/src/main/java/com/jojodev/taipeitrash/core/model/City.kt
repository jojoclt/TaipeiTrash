package com.jojodev.taipeitrash.core.model

import com.google.android.gms.maps.model.LatLng

/**
 * Supported cities for trash collection data
 */
enum class City(val displayName: String) {
    TAIPEI("Taipei City"),
    HSINCHU("Hsinchu City");

    /**
     * Get default location (city center) for this city
     */
    fun getDefaultLocation(): LatLng {
        return when (this) {
            TAIPEI -> LatLng(25.0330, 121.5654) // Taipei Main Station
            HSINCHU -> LatLng(24.8138, 120.9675) // Hsinchu City Hall
        }
    }

    companion object {
        fun fromString(value: String): City {
            return entries.find { it.name == value } ?: TAIPEI
        }
    }
}

