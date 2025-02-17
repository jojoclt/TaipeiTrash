package com.jojodev.taipeitrash.trashcan.presentation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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

//import com.jojodev.taipeitrash.trashcar.presentation.MarkerItem
//import com.jojodev.taipeitrash.trashcar.presentation.openAppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TrashCanScreen(permissionViewModel: PermissionViewModel = viewModel()) {
    val viewModel = hiltViewModel<TrashCanViewModel>()

    val uiStatus by viewModel.uiState.collectAsStateWithLifecycle()
    val collectDate by viewModel.importDate.collectAsStateWithLifecycle("")

    val context = LocalContext.current
    LaunchedEffect(collectDate) {
        if (collectDate.isNotEmpty())
            Toast.makeText(context, collectDate, Toast.LENGTH_SHORT).show()
    }


    val locationPermission by permissionViewModel.permissionGranted.collectAsStateWithLifecycle()
    val isLaunchedOnce by permissionViewModel.isLaunchedOnce.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionViewModel.setPermissionGranted(granted)
    }


    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        )
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
    ) {
        CenterAlignedTopAppBar(title = { Text("Trash Can") }, actions = {
            IconButton(
                { viewModel.fetchData(forceUpdate = true) },
                modifier = if (uiStatus is Results.Loading) Modifier.graphicsLayer {
                    rotationZ = angle
                } else Modifier) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        })
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
            val items = trashCan.map {
                MarkerItem(
                    LatLng(
                        it.latitude,
                        it.longitude
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