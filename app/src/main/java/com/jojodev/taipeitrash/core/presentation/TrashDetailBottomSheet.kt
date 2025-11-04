package com.jojodev.taipeitrash.core.presentation

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
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
            Row(
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

                Column {
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
                }
            }

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
                            value = trashModel.timeArrive
                        )
                        DetailItem(
                            label = "Departure Time",
                            value = trashModel.timeLeave
                        )
                    }
                }

                DetailItem(
                    label = "Last Updated",
                    value = trashModel.importDate
                )
            }
        }
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