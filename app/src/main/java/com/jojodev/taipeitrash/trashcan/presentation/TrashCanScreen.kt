package com.jojodev.taipeitrash.trashcan.presentation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.jojodev.taipeitrash.IndeterminateCircularIndicator
import com.jojodev.taipeitrash.PermissionViewModel
import com.jojodev.taipeitrash.core.Results
import com.jojodev.taipeitrash.trashcan.TrashCanViewModel
import com.jojodev.taipeitrash.trashcan.data.TrashCan
import com.jojodev.taipeitrash.trashcar.presentation.MarkerItem
import com.jojodev.taipeitrash.trashcar.presentation.openAppSettings

@Composable
@Preview
fun TrashCanScreen(permissionViewModel: PermissionViewModel = viewModel()) {
    val viewModel = hiltViewModel<TrashCanViewModel>()

    val uiStatus by viewModel.uiState.collectAsStateWithLifecycle()

    val locationPermission by permissionViewModel.permissionGranted.collectAsStateWithLifecycle()
    val isLaunchedOnce by permissionViewModel.isLaunchedOnce.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionViewModel.setPermissionGranted(granted)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val s = uiStatus) {
            Results.Loading -> {
                IndeterminateCircularIndicator(loadStatus = true) {
                    when (it) {
                        true -> viewModel.fetchData()
                        false -> viewModel.cancelFetchData()
                    }
                }
            }

            is Results.Error -> {
                Text("Error")
                Log.e("TrashCanScreen", s.toString())
                IndeterminateCircularIndicator(loadStatus = false) {
                    when (it) {
                        true -> viewModel.fetchData()
                        false -> viewModel.cancelFetchData()
                    }
                }
            }

            is Results.Success -> {
                val data = s.data
                Log.d("TrashCanScreen", "data: ${data.size}")
                Log.i("PermissionViewModel", "locationPermission: $locationPermission")
                if (!locationPermission) {
                    if (!isLaunchedOnce) {
                        permissionViewModel.setLaunchedOnce(true)
                        SideEffect { locationPermissionLauncher.launch(permissionViewModel.permission) }
                    }
                    val activity = LocalContext.current as Activity
                    if (!shouldShowRequestPermissionRationale(
                            activity,
                            permissionViewModel.permission
                        )
                    ) {
                        Text("Please enable location permission in settings")
                        Button(onClick = {
                            activity.openAppSettings()
                        }) {
                            Text("Enable Location Permission")
                        }
                    }
                } else TrashCanMap(data)
            }
        }
    }

    @Composable
    fun RequestMultiplePermissions(permissions: (Boolean) -> Unit) {

        // State to store whether all permissions are granted
        val permissionsGranted = remember { mutableStateMapOf<String, Boolean>() }
//    Despite using "remember", it does not persist, it will get destroyed when changing screen, but persist across the same composable recompositions.

        val multiplePermissionsLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Callback when the result is received
            permissionsGranted.putAll(permissions)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Button to request multiple permissions
            Button(onClick = {
                // Launch the permission request for multiple permissions
                multiplePermissionsLauncher.launch(
                    arrayOf(
//                    Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }) {
                Text("Request Camera & Location Permissions")
            }


            permissionsGranted[Manifest.permission.ACCESS_FINE_LOCATION]?.let { permissions(it) }

            // Display the permission states
            permissionsGranted.forEach { (permission, granted) ->
                Text(
                    text = "$permission: ${if (granted) "Granted" else "Denied"}",
                    color = if (granted) Color.Green else Color.Red
                )
                Log.i(
                    "RequestMultiplePermissions",
                    "$permission: ${if (granted) "Granted" else "Denied"}"
                )
            }
        }
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun TrashCanMap(
    trashCan: List<TrashCan> = emptyList(),
    onBoundsChange: (LatLngBounds?) -> Unit = {}
) {
    val taipeiMain = LatLng(25.0330, 121.5654)

    var filteredTrashCan by remember { mutableStateOf(trashCan) }
    Log.i("TrashCanMap", "filteredTrashCan: ${filteredTrashCan.size}")
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(taipeiMain, 12f)
    }
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
//                maxZoomPreference = 10f,
//                minZoomPreference = 12f,
                isMyLocationEnabled = true
            )
        )
    }
    var mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(mapToolbarEnabled = false)
        )
    }
    // whenever cameraPositionState.isMoving changes, launch a coroutine
    //    to fire onBoundsChange. We'll report the visibleRegion
    //    LatLngBounds
//    LaunchedEffect(cameraPositionState.isMoving) {
//        if (!cameraPositionState.isMoving) {
//            val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
//            onBoundsChange(
//                bounds
//            )
//            if (bounds != null) {
//                filteredTrashCan = trashCan
//                Log.d("TrashCanMap", "filteredTrashCan: ${filteredTrashCan.size}")
//            }
////                filteredTrashCan = trashCan.filter {
////                    try {
////                        bounds.contains(
////                            LatLng(
////                                it.latitude.removePrefix("?").toDouble(),
////                                it.longitude.removePrefix("?").toDouble()
////                            )
////                        )
////                    } catch (e: Exception) {
////                        false
////                    }
////                }
////            }
////            Log.i("TrashCanMap", "filteredTrashCan: ${filteredTrashCan.size}")
//        }
//    }
    Box {
//        Box(modifier = Modifier.fillMaxSize().zIndex(2f)) {
//            Text("TrashCanMap")
//            Text(trashCan.toString())
//        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings,
            properties = mapProperties,
            onMapLoaded = {
//                onBoundsChange(cameraPositionState.projection?.visibleRegion?.latLngBounds)
            }
        ) {

            val trashMarker = remember {
                trashCan.mapNotNull {
                    try {
                        MarkerItem(
                            LatLng(
                                it.latitude.removePrefix("?").toDouble(),
                                it.longitude.removePrefix("?").toDouble()
                            ), it.address, "Marker in ${it.id}"
                        )
                    } catch (e: Exception) {
                        Log.e("TrashCanMap", "Error Converting at idx ${it.id}\n $it")
                        null
                    }
                }.toMutableStateList()

            }
            val items = trashCan.map {
                MarkerItem(
                    LatLng(
                        it.latitude.toDouble(),
                        it.longitude.toDouble()
                    ), it.address, "Marker in ${it.id}"
                )
            }
            Log.d("TrashCanMap", "mappeditems: ${items.size}")
//
//        }
            Clustering(items = items)
        }
    }

}

data class MarkerItem(
    val itemPosition: LatLng,
    val itemTitle: String,
    val itemSnippet: String,
    val itemZIndex: Float = 0f
) : ClusterItem {
    override fun getPosition(): LatLng =
        itemPosition

    override fun getTitle(): String =
        itemTitle

    override fun getSnippet(): String =
        itemSnippet

    override fun getZIndex(): Float =
        itemZIndex
}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}