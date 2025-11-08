package com.jojodev.taipeitrash.core.model

/**
 * Supported cities for trash collection data
 */
enum class City(val displayName: String) {
    TAIPEI("Taipei City"),
    HSINCHU("Hsinchu City");

    companion object {
        fun fromString(value: String): City {
            return entries.find { it.name == value } ?: TAIPEI
        }
    }
}

