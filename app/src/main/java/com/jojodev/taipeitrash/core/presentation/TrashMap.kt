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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.DefaultMapContentPadding
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings

@Composable
fun TrashMap(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
    contentPadding: PaddingValues = DefaultMapContentPadding,
    onMapLoaded: () -> Unit = {},
    content: @Composable @GoogleMapComposable () -> Unit = {}
) {

    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = false // Always false - we handle location manually
            )
        )
    }
    var mapUiSettings by remember {
        mutableStateOf(
            // disable map toolbar, compass, and location button for a cleaner UI
            MapUiSettings(
                mapToolbarEnabled = false,
                compassEnabled = false,
                myLocationButtonEnabled = false
            )
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
        onMapLoaded = onMapLoaded,
        content = content
    )
}

@Composable
fun BottomSheetContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Select a marker on the map to see details",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}