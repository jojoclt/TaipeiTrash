package com.jojodev.taipeitrash.core.presentation

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jojodev.taipeitrash.core.model.TrashType
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme
import com.jojodev.taipeitrash.ui.theme.markerTrashCan
import com.jojodev.taipeitrash.ui.theme.markerTruckGreen
import com.jojodev.taipeitrash.ui.theme.markerTruckNeutral
import com.jojodev.taipeitrash.ui.theme.markerTruckRed
import com.jojodev.taipeitrash.ui.theme.markerTruckYellow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
 *
 * Optionally accept `currentTime` so callers can pass a minute-aligned time for deterministic results
 * Behavior tweak: if current time is after arrival but before departure -> show blue (markerTrashCan)
 */
fun computeTruckMarkerState(arrivalTime: String?, departureTime: String?, currentTime: LocalTime? = null): Pair<Color, Int?> {
    // Neutral on Wed/Sun
    val dow = LocalDate.now().dayOfWeek
    if (dow == DayOfWeek.WEDNESDAY || dow == DayOfWeek.SUNDAY) {
        return Pair(markerTruckNeutral, null)
    }

    val now = currentTime ?: LocalTime.now()

    val arrival = parseTimeHHmmOrNull(arrivalTime)
    val departure = parseTimeHHmmOrNull(departureTime)

    // If both arrival and departure available, and now is between arrival and departure => active (blue)
    if (arrival != null && departure != null) {
        var minutesToArrival = ChronoUnit.MINUTES.between(now, arrival).toInt()
        var minutesToDeparture = ChronoUnit.MINUTES.between(now, departure).toInt()

        // normalize negative arrival to next day for arrival comparison
        if (minutesToArrival < 0) minutesToArrival += 24 * 60
        // don't normalize departure; if departure is earlier and minutesToDeparture < 0, then it's already passed

        // if arrival already passed (minutesToArrival <= 0 in raw check) and departure in future
        val rawMinutesToArrival = ChronoUnit.MINUTES.between(now, arrival).toInt()
        if (rawMinutesToArrival <= 0 && minutesToDeparture > 0) {
            // currently active (between arrival and departure)
            return Pair(markerTrashCan, null)
        }
    }

    // If departure provided and already passed -> neutral
    if (departure != null) {
        val minutesToDeparture = ChronoUnit.MINUTES.between(now, departure).toInt()
        if (minutesToDeparture < 0) return Pair(markerTruckNeutral, null)
    }

    // If arrival absent -> neutral
    val arrivalNonNull = arrival ?: return Pair(markerTruckNeutral, null)

    var minutesToArrival = ChronoUnit.MINUTES.between(now, arrivalNonNull).toInt()
    if (minutesToArrival < 0) minutesToArrival += 24 * 60 // assume next day

    val color = when {
        minutesToArrival > GREEN_THRESHOLD_MIN -> markerTruckGreen
        minutesToArrival <= RED_THRESHOLD_MIN -> markerTruckRed
        else -> markerTruckYellow
    }

    return Pair(color, minutesToArrival)
}

/**
 * Compose helper to get the current LocalTime aligned to the minute boundary.
 * Collects the shared `minuteTicker` flow and returns LocalTime for recomposition.
 */
@Composable
fun rememberCurrentMinute(): LocalTime {
    // ✅ 1. Wrap the transformation in remember
    val minuteFlow = remember(minuteTicker) {
        minuteTicker.map { it.toLocalTime() }
    }

    // ✅ 2. Collect the remembered flow
    val current by minuteFlow.collectAsStateWithLifecycle(initialValue = LocalTime.now())

    return current
}

@Composable
fun TrashMarkerIcon(
    trashType: TrashType,
    modifier: Modifier = Modifier,
    arrivalTime: String? = null, // optional, format like "2003" (HHmm)
    departureTime: String? = null, // optional; if already passed -> neutral
    currentMinute: LocalTime? = null // optional injector so callers can subscribe once
) {
    // Ensure recomposition each minute by reading current minute; prefer provided minute
    val minute = currentMinute ?: rememberCurrentMinute()

    // Use computeTruckMarkerState passing the minute so the parsing uses aligned time
    val (targetColor, _) = if (trashType == TrashType.GARBAGE_TRUCK) {
        computeTruckMarkerState(arrivalTime, departureTime, minute)
    } else {
        Pair(markerTrashCan, null)
    }

    val initial = if (trashType == TrashType.TRASH_CAN) markerTrashCan else targetColor

    // animate between colors smoothly
    val animatedColor by animateColorAsState(targetValue = if (trashType == TrashType.TRASH_CAN) markerTrashCan else targetColor)

    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                color = animatedColor,
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Simulate active (between arrival and departure): arrival now-5min, departure +30min -> blue
                val now = LocalTime.now()
                val arr = now.minusMinutes(5).format(PARSE_HHMM)
                val leave = now.plusMinutes(30).format(PARSE_HHMM)
                TrashMarkerIcon(trashType = TrashType.GARBAGE_TRUCK, arrivalTime = arr, departureTime = leave, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                androidx.compose.material3.Text("Truck (active - blue)")
            }
        }
    }
}
