package com.jojodev.taipeitrash.core.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.jojodev.taipeitrash.core.model.TrashType

/**
 * Helper to create custom marker bitmap from Composable
 * This is a reference implementation for future use
 */
object MarkerUtils {

    /**
     * Create a colored marker with the appropriate icon
     * Usage in your Marker composable:
     *
     * Marker(
     *     state = MarkerState(position = trashCan.toLatLng()),
     *     title = trashCan.address,
     *     icon = MarkerUtils.getMarkerIcon(TrashType.TRASH_CAN),
     *     onClick = { ... }
     * )
     */
    fun getMarkerIcon(trashType: TrashType): com.google.android.gms.maps.model.BitmapDescriptor {
        // For now, use default colored markers
        // You can enhance this to use custom bitmap icons
        val hue = when (trashType) {
            TrashType.TRASH_CAN -> 120f // Green
            TrashType.GARBAGE_TRUCK -> 210f // Blue
        }
        return BitmapDescriptorFactory.defaultMarker(hue)
    }

    /**
     * Alternative: Get marker color based on type
     */
    fun getMarkerColor(trashType: TrashType): Int {
        return when (trashType) {
            TrashType.TRASH_CAN -> Color(0xFF4CAF50).toArgb() // Green
            TrashType.GARBAGE_TRUCK -> Color(0xFF2196F3).toArgb() // Blue
        }
    }
}

/**
 * Example usage in App.kt:
 *
 * Instead of:
 *   Marker(
 *       state = MarkerState(position = trashCan.toLatLng()),
 *       title = trashCan.address,
 *       onClick = { ... }
 *   )
 *
 * Use:
 *   Marker(
 *       state = MarkerState(position = trashCan.toLatLng()),
 *       title = trashCan.address,
 *       icon = MarkerUtils.getMarkerIcon(TrashType.TRASH_CAN),
 *       onClick = { ... }
 *   )
 */

