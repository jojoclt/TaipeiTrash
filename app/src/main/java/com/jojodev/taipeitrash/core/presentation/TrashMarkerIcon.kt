package com.jojodev.taipeitrash.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jojodev.taipeitrash.core.model.TrashType
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val TRASH_CAN_COLOR = Color(0xFF2196F3) // Blue (changed per request)
private val TRUCK_GREEN = Color(0xFF4CAF50)
private val TRUCK_YELLOW = Color(0xFFFFC107)
private val TRUCK_RED = Color(0xFFF44336)
private val TRUCK_NEUTRAL = Color(0xFF9E9E9E)

// Thresholds (minutes) - made public so other files can read them
const val GREEN_THRESHOLD_MIN = 60 // > 60 min => green
const val RED_THRESHOLD_MIN = 15   // <= 15 min => red

private val PARSE_HHMM = DateTimeFormatter.ofPattern("HHmm")

private fun parseTimeHHmmOrNull(raw: String?): LocalTime? {
    if (raw == null) return null
    val digits = raw.filter { it.isDigit() }
    if (digits.length < 3 || digits.length > 4) return null
    val padded = digits.padStart(4, '0')
    return try {
        LocalTime.parse(padded, PARSE_HHMM)
    } catch (_: Exception) {
        null
    }
}

/**
 * Compute the truck marker color and minutes-to-arrival.
 * Returns Pair(color, minutesToArrivalNullable).
 * - color: the marker background color (neutral when no data or special conditions)
 * - minutesToArrival: minutes until arrival (positive), or null when not applicable
 */
fun computeTruckMarkerState(arrivalTime: String?, departureTime: String?): Pair<Color, Int?> {
    // Neutral on Wed/Sun
    val dow = LocalDate.now().dayOfWeek
    if (dow == DayOfWeek.WEDNESDAY || dow == DayOfWeek.SUNDAY) {
        return Pair(TRUCK_NEUTRAL, null)
    }

    val now = LocalTime.now()

    // If departure provided and already passed -> neutral
    val departure = parseTimeHHmmOrNull(departureTime)
    if (departure != null) {
        val minutesToDeparture = ChronoUnit.MINUTES.between(now, departure).toInt()
        if (minutesToDeparture < 0) return Pair(TRUCK_NEUTRAL, null)
    }

    val arrival = parseTimeHHmmOrNull(arrivalTime) ?: return Pair(TRUCK_NEUTRAL, null)

    var minutesToArrival = ChronoUnit.MINUTES.between(now, arrival).toInt()
    if (minutesToArrival < 0) minutesToArrival += 24 * 60 // assume next day

    val color = when {
        minutesToArrival > GREEN_THRESHOLD_MIN -> TRUCK_GREEN
        minutesToArrival <= RED_THRESHOLD_MIN -> TRUCK_RED
        else -> TRUCK_YELLOW
    }

    return Pair(color, minutesToArrival)
}

@Composable
fun TrashMarkerIcon(
    trashType: TrashType,
    modifier: Modifier = Modifier,
    arrivalTime: String? = null, // optional, format like "2003" (HHmm)
    departureTime: String? = null // optional; if already passed -> neutral
) {
    val backgroundColor = when (trashType) {
        TrashType.TRASH_CAN -> TRASH_CAN_COLOR
        TrashType.GARBAGE_TRUCK -> run {
            // Neutral color on Wednesday or Sunday
            val dow = LocalDate.now().dayOfWeek
            if (dow == DayOfWeek.WEDNESDAY || dow == DayOfWeek.SUNDAY) {
                TRUCK_NEUTRAL
            } else {
                // If departureTime is provided and already passed (now > departure) -> neutral
                val departure = parseTimeHHmmOrNull(departureTime)
                val now = LocalTime.now()
                if (departure != null) {
                    val minutesToDeparture = ChronoUnit.MINUTES.between(now, departure).toInt()
                    if (minutesToDeparture < 0) {
                        // departure earlier today -> considered already passed
                        TRUCK_NEUTRAL
                    } else {
                        // Otherwise, compute based on arrivalTime if available
                        val arrival = parseTimeHHmmOrNull(arrivalTime)
                        if (arrival == null) {
                            TRUCK_NEUTRAL
                        } else {
                            var minutesToArrival = ChronoUnit.MINUTES.between(now, arrival).toInt()
                            if (minutesToArrival < 0) {
                                // arrival likely on next day
                                minutesToArrival += 24 * 60
                            }

                            when {
                                minutesToArrival > GREEN_THRESHOLD_MIN -> TRUCK_GREEN
                                minutesToArrival <= RED_THRESHOLD_MIN -> TRUCK_RED
                                else -> TRUCK_YELLOW
                            }
                        }
                    }
                } else {
                    // No departure provided - proceed with arrival
                    val arrival = parseTimeHHmmOrNull(arrivalTime)
                    if (arrival == null) {
                        TRUCK_NEUTRAL
                    } else {
                        var minutesToArrival = ChronoUnit.MINUTES.between(now, arrival).toInt()
                        if (minutesToArrival < 0) {
                            minutesToArrival += 24 * 60
                        }

                        when {
                            minutesToArrival > GREEN_THRESHOLD_MIN -> TRUCK_GREEN
                            minutesToArrival <= RED_THRESHOLD_MIN -> TRUCK_RED
                            else -> TRUCK_YELLOW
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (trashType) {
                TrashType.TRASH_CAN -> Icons.Default.Delete
                TrashType.GARBAGE_TRUCK -> Icons.Default.LocalShipping
            },
            contentDescription = when (trashType) {
                TrashType.TRASH_CAN -> "Trash Can"
                TrashType.GARBAGE_TRUCK -> "Garbage Truck"
            },
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrashMarkerIconPreview() {
    TaipeiTrashTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TrashMarkerIcon(trashType = TrashType.TRASH_CAN, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                androidx.compose.material3.Text("Trash Can (blue)")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Simulate truck arriving in > 60 min (green)
                TrashMarkerIcon(trashType = TrashType.GARBAGE_TRUCK, arrivalTime = "2359", modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                androidx.compose.material3.Text("Truck (green)")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Simulate truck arriving in 30 min (yellow)
                val now = LocalTime.now()
                val t = now.plusMinutes(30).format(PARSE_HHMM)
                TrashMarkerIcon(trashType = TrashType.GARBAGE_TRUCK, arrivalTime = t, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                androidx.compose.material3.Text("Truck (yellow)")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Simulate truck arriving in 10 min (red)
                val now = LocalTime.now()
                val t = now.plusMinutes(10).format(PARSE_HHMM)
                TrashMarkerIcon(trashType = TrashType.GARBAGE_TRUCK, arrivalTime = t, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                androidx.compose.material3.Text("Truck (red)")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Simulate neutral: no times
                TrashMarkerIcon(trashType = TrashType.GARBAGE_TRUCK, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                androidx.compose.material3.Text("Truck (neutral)")
            }
        }
    }
}
