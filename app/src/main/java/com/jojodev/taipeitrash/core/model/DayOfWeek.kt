package com.jojodev.taipeitrash.core.model

/**
 * Day of week enum for trash collection schedules
 */
enum class DayOfWeek(val dayNumber: Int, val shortName: String) {
    MONDAY(1, "M"),
    TUESDAY(2, "T"),
    WEDNESDAY(3, "W"),
    THURSDAY(4, "T"),
    FRIDAY(5, "F"),
    SATURDAY(6, "S"),
    SUNDAY(7, "S");

    companion object {
        fun fromDayNumber(dayNumber: Int): DayOfWeek? {
            return entries.find { it.dayNumber == dayNumber }
        }

        /**
         * Parse comma-separated day numbers to Set of DayOfWeek
         * e.g., "1,2,4,5,6" -> [MONDAY, TUESDAY, THURSDAY, FRIDAY, SATURDAY]
         */
        fun parseFromString(daysString: String?): Set<DayOfWeek> {
            if (daysString.isNullOrBlank()) return emptySet()

            return daysString.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .mapNotNull { fromDayNumber(it) }
                .toSet()
        }

        /**
         * Convert Set of DayOfWeek to comma-separated string for storage
         */
        fun toStorageString(days: Set<DayOfWeek>): String {
            return days.sortedBy { it.dayNumber }.joinToString(",") { it.dayNumber.toString() }
        }
    }
}

