package com.jojodev.taipeitrash.core.presentation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.graphics.Color
import com.jojodev.taipeitrash.core.model.TrashModel
import com.jojodev.taipeitrash.core.model.TrashType
import com.jojodev.taipeitrash.trashcar.data.TrashCar

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

            HorizontalDivider()

            // Details section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailItem(
                    label = "District",
                    value = trashModel.district
                )

                when (trashModel) {
                    is TrashCar -> {
                        DetailItem(
                            label = "Arrival Time",
                            value = trashModel.timeArrive.formatTime()
                        )
                        DetailItem(
                            label = "Departure Time",
                            value = trashModel.timeLeave.formatTime()
                        )
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
                val (color, minutes) = computeTruckMarkerState(trashModel.timeArrive, trashModel.timeLeave)
                // Only show when minutes is not null and less than or equal to GREEN_THRESHOLD_MIN
                if (minutes != null && minutes <= GREEN_THRESHOLD_MIN) {
                    ArrivalChip(minutes = minutes, background = color)
                }
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
            var intent = Intent(Intent.ACTION_VIEW, "geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})".toUri())
            if (intent.resolveActivity(pm) != null) {
                if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return@IconButton
            }

            // Fallback to Google Maps web
            intent = Intent(Intent.ACTION_VIEW, "https://www.google.com/maps/search/?api=1&query=$lat,$lng".toUri())
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
    val textColor = if ((background.red * 0.2126f + background.green * 0.7152f + background.blue * 0.0722f) < 0.6f) Color.White else Color.Black

    Box(
        modifier = modifier
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .wrapContentWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Arriving in ${minutes} min", color = textColor, style = MaterialTheme.typography.bodySmall)
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun String.formatTime(): String {
    if (this.length != 4) return this
    return this.take(2) + ":" + this.takeLast(2)
}