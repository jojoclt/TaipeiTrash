package com.jojodev.taipeitrash.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jojodev.taipeitrash.core.model.TrashType

@Composable
fun TrashMarkerIcon(
    trashType: TrashType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                color = when (trashType) {
                    TrashType.TRASH_CAN -> Color(0xFF4CAF50) // Green
                    TrashType.GARBAGE_TRUCK -> Color(0xFF2196F3) // Blue
                },
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

