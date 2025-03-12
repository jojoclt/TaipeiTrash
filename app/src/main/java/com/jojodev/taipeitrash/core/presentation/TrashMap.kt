package com.jojodev.taipeitrash.core.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings

@Composable
fun TrashMap(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
    content: @Composable @GoogleMapComposable () -> Unit = {}
) {

    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
//                maxZoomPreference = 10f,
                minZoomPreference = 14f,
                isMyLocationEnabled = true
            )
        )
    }
    var mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(mapToolbarEnabled = false)
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings,
        properties = mapProperties,
        onMapLoaded = {
            val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
//            onBoundsChange(
//                bounds
//            )
//            if (bounds != null) {
//                filteredTrashCan = trashCan.filter {
//                    bounds.contains(it.toLatLng())
//                }
//                markerItem = filteredTrashCan.fastMap {
//                    MarkerItem(
//                        it.toLatLng(),
//                        it.id.toString(),
//                        it.address
//                    )
//                }
//            }
        }
    ) {
        content()
    }
}