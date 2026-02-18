package com.jojodev.taipeitrash.core.presentation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.jojodev.taipeitrash.core.model.TrashModel
import com.jojodev.taipeitrash.core.model.TrashType
import com.jojodev.taipeitrash.trashcan.data.TrashCan
import com.jojodev.taipeitrash.trashcar.data.TrashCar
import com.jojodev.taipeitrash.ui.components.WeekDayIndicator
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme
import com.jojodev.taipeitrash.ui.theme.chipColorGray
import com.jojodev.taipeitrash.ui.theme.markerTrashCan
import com.jojodev.taipeitrash.ui.theme.markerTruckGreen
import com.jojodev.taipeitrash.ui.theme.markerTruckNeutral
import com.jojodev.taipeitrash.ui.theme.markerTruckRed
import com.jojodev.taipeitrash.ui.theme.markerTruckYellow
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun TrashDetailBottomSheet(
    trashModel: TrashModel,
    trashType: TrashType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with icon and title
            TrashDetailHeader(trashModel = trashModel, trashType = trashType)
            if (trashModel is TrashCan) return@Column

            // Details section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                when (trashModel) {
                    is TrashCar -> {
                        val (color, minutes) = computeTruckMarkerState(
                            trashModel.timeArrive,
                            trashModel.timeLeave,
                            rememberCurrentMinute()
                        )

                        if (minutes == null && color == markerTrashCan)
                            TruckStatus(isFixed = trashModel.isFixed)
                        DetailItem(
                            label = "Arrival Time",
                            value = trashModel.timeArrive.formatTime()
                        )
                        DetailItem(
                            label = "Departure Time",
                            value = trashModel.timeLeave.formatTime()
                        )

                        HorizontalDivider()

                        // Show schedule if available (for cities like Hsinchu)
                        if (trashModel.trashDays.isNotEmpty() || trashModel.recycleDays.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Collection Schedule",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                WeekDayIndicator(
                                    trashDays = trashModel.trashDays,
                                    recycleDays = trashModel.recycleDays
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    ScheduleLegend(
                                        color = Color(0xFFE57373),
                                        label = "Trash"
                                    )
                                    ScheduleLegend(
                                        color = Color(0xFF64B5F6),
                                        label = "Recycle"
                                    )
                                    ScheduleLegend(
                                        color = Color(0xFF4CAF50),
                                        label = "Both"
                                    )
                                }
                            }
                        }
                    }
                }

//                DetailItem(
//                    label = "Last Updated",
//                    value = trashModel.importDate
//                )
            }
        }
    }
}

@Composable
private fun TrashDetailHeader(
    trashModel: TrashModel,
    trashType: TrashType,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Squircle icon container
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when (trashType) {
                        TrashType.TRASH_CAN -> MaterialTheme.colorScheme.primaryContainer
                        TrashType.GARBAGE_TRUCK -> MaterialTheme.colorScheme.secondaryContainer
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (trashType) {
                    TrashType.TRASH_CAN -> Icons.Default.Delete
                    TrashType.GARBAGE_TRUCK -> Icons.Default.LocalShipping
                },
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = when (trashType) {
                    TrashType.TRASH_CAN -> MaterialTheme.colorScheme.onPrimaryContainer
                    TrashType.GARBAGE_TRUCK -> MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (trashType) {
                    TrashType.TRASH_CAN -> "Trash Can"
                    TrashType.GARBAGE_TRUCK -> "Garbage Truck"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = trashModel.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Show arrival chip for garbage trucks when applicable
            if (trashType == TrashType.GARBAGE_TRUCK && trashModel is TrashCar) {
                // Subscribe to the minute ticker so the header recomposes every minute
                val currentMinute = rememberCurrentMinute()

                // fixme
                // Special-case: on Wednesdays and Sundays there is no service -> show a specific chip
                val today = LocalDate.now().dayOfWeek
                if (today == DayOfWeek.WEDNESDAY || today == DayOfWeek.SUNDAY) {
                    InfoChip(text = "Not available on Wed & Sun", background = chipColorGray)
                } else {
                    val (color, minutes) = computeTruckMarkerState(
                        trashModel.timeArrive,
                        trashModel.timeLeave,
                        currentMinute
                    )
                    // Show arriving minutes chip when available and within visible threshold
                    if (minutes != null && minutes <= GREEN_THRESHOLD_MIN) {
                        ArrivalChip(minutes = minutes, background = color)
                    } else {
                        // Show additional states: Collecting (blue) when active; No more for today (gray) otherwise
                        if (minutes == null) {
                            if (color == markerTrashCan) {
                                InfoChip(text = "Collecting", background = color)
                            } else {
                                InfoChip(text = "No more for today", background = color)
                            }
                        }
                    }
                }
            }

            // Show status for Trash Can: blue = Available, gray = Unknown
            if (trashType == TrashType.TRASH_CAN) {
                val isUnknown =
                    (trashModel.latitude == 0.0 && trashModel.longitude == 0.0) || trashModel.address.isBlank()
                val target = if (isUnknown) markerTruckNeutral else markerTrashCan
                StatusChip(isUnknown = isUnknown, background = target)
            }
        }

        // Directions button: open map using geo: URI (system chooses app) and fallback to web
        IconButton(onClick = {
            val latLng = trashModel.toLatLng()
            val lat = latLng.latitude
            val lng = latLng.longitude
            val label = trashModel.address

            val pm = context.packageManager

            // Use geo: URI so the system will open the user's preferred map app
            var intent = Intent(
                Intent.ACTION_VIEW,
                "geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})".toUri()
            )
            if (intent.resolveActivity(pm) != null) {
                if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return@IconButton
            }

            // Fallback to Google Maps web
            intent = Intent(
                Intent.ACTION_VIEW,
                "https://www.google.com/maps/search/?api=1&query=$lat,$lng".toUri()
            )
            if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }) {
            Icon(
                imageVector = Icons.Default.Directions,
                contentDescription = "Open directions"
            )
        }
    }
}

@Composable
private fun ArrivalChip(minutes: Int, background: Color, modifier: Modifier = Modifier) {
    // choose contrasting text color (white for dark backgrounds, black for light)
    val textColor =
        if ((background.red * 0.2126f + background.green * 0.7152f + background.blue * 0.0722f) < 0.6f) Color.White else Color.Black

    Box(
        modifier = modifier
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .wrapContentWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Arriving in ${minutes} min",
            color = textColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun InfoChip(text: String, background: Color, modifier: Modifier = Modifier) {
    // choose contrasting text color (white for dark backgrounds, black for light)
    val textColor =
        if ((background.red * 0.2126f + background.green * 0.7152f + background.blue * 0.0722f) < 0.6f) Color.White else Color.Black

    Box(
        modifier = modifier
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .wrapContentWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StatusChip(isUnknown: Boolean, background: Color, modifier: Modifier = Modifier) {
    val animated = animateColorAsState(targetValue = background)
    val text = if (isUnknown) "Status: Unknown" else "Status: Available"
    val textColor =
        if ((animated.value.red * 0.2126f + animated.value.green * 0.7152f + animated.value.blue * 0.0722f) < 0.6f) Color.White else Color.Black

    Box(
        modifier = modifier
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(animated.value)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .wrapContentWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ScheduleLegend(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun String.formatTime(): String {
    if (this.length != 4) return this
    return this.take(2) + ":" + this.takeLast(2)
}

@Preview(showBackground = true)
@Composable
fun ArrivalChipPreview() {
    TaipeiTrashTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ArrivalChip(minutes = 120, background = markerTruckGreen)
            Spacer(modifier = Modifier.height(8.dp))
            ArrivalChip(minutes = 45, background = markerTruckYellow)
            Spacer(modifier = Modifier.height(8.dp))
            ArrivalChip(minutes = 10, background = markerTruckRed)
            Spacer(modifier = Modifier.height(8.dp))
            ArrivalChip(minutes = 1, background = markerTruckNeutral)

            Spacer(modifier = Modifier.height(16.dp))

            // New InfoChip preview states
            InfoChip(text = "Collecting", background = markerTrashCan)
            Spacer(modifier = Modifier.height(8.dp))
            InfoChip(text = "No more for today", background = markerTruckNeutral)
        }
    }
}

@Composable
fun PulsatingDot(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF0056FE)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")

    // Animate scale from 1x to 2.5x
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    // Animate alpha from 0.7 to 0
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(modifier = modifier.size(14.dp), contentAlignment = Alignment.Center) {
        // Pulsating outer ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = color,
                radius = (size.minDimension / 2) * scale,
                alpha = alpha
            )
        }
        // Solid center dot
        Canvas(modifier = Modifier.size(6.dp)) {
            drawCircle(color = color)
        }
    }
}

@Composable
private fun TruckStatus(modifier: Modifier = Modifier, isFixed: Boolean) {
    val accentColor =
        if (isFixed) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF0056FE)
    val background = if (isFixed) Color(0xFFFEFEFE) else Color(0xFFFFFFFF)
    val text = if (isFixed) "At Collection Point" else "Moving Along Route"

    Box(
        modifier = modifier
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp)) // Added for visibility
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .wrapContentWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PulsatingDot(color = accentColor)

            Text(
                text = text,
                color = accentColor,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Preview
@Composable
private fun TruckStatusPreview() {
    TaipeiTrashTheme {
        Column {
            TruckStatus(isFixed = true)
            TruckStatus(isFixed = false)

        }
    }
}

@Preview
@Composable
private fun BottomSheetPreview() {
    TaipeiTrashTheme {

    }
}