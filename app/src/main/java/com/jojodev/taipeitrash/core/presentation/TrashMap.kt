package com.jojodev.taipeitrash.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.DefaultMapContentPadding
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun TrashMap(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState {
        val taipeiMain = LatLng(25.0330, 121.5654)
        position = CameraPosition.fromLatLngZoom(taipeiMain, 12f)
    },
    contentPadding: PaddingValues = DefaultMapContentPadding,
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
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .background(Color.Black)
                .fillMaxSize()
        )
        return
    }
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings,
        properties = mapProperties,
        contentPadding = contentPadding,
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
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

@Composable
fun BottomSheetContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.Blue)

            .fillMaxSize()
    ) {
        Text("Bottom Sheet Content")
    }
}