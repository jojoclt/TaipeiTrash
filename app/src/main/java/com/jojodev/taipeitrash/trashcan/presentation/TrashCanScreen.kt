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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TrashCanScreen(permissionViewModel: PermissionViewModel = viewModel()) {
    val viewModel: TrashCanViewModel = hiltViewModel()

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

    Box() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
        ) {
            CenterAlignedTopAppBar(
                title = { Text("Trash Can") },
                actions = {
                    IconButton(
                        { viewModel.fetchData(forceUpdate = true) },
                        modifier = if (uiStatus is Results.Loading) Modifier.graphicsLayer {
                            rotationZ = angle
                        } else Modifier) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    IconButton({}) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                },
            )
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
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Surface(shape = MaterialTheme.shapes.extraLarge) {
                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            false,
                            {},
                            shape = RoundedCornerShape(4.dp)
                        ) { Text("First") }
                        SegmentedButton(
                            false,
                            {},
                            shape = RoundedCornerShape(4.dp)
                        ) { Text("Second") }
                    }
                }
            }
            Box(modifier = Modifier.weight(2f))
        }
    }
}

@OptIn(MapsComposeExperimentalApi::class, ExperimentalCoroutinesApi::class)
@Composable
fun TrashCanMap(
    trashCan: List<TrashCan> = emptyList(),
    onBoundsChange: (LatLngBounds?) -> Unit = {}
) {
    val taipeiMain = LatLng(25.0330, 121.5654)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(taipeiMain, 12f)
    }
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
    var filteredTrashCan by remember { mutableStateOf(trashCan) }
    var markerItem by remember { mutableStateOf(emptyList<MarkerItem>()) }

    LaunchedEffect(cameraPositionState.isMoving) {
        snapshotFlow { cameraPositionState.isMoving }
            .mapLatest { it }
            .filter { !it }.collect {
                val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
                onBoundsChange(
                    bounds
                )
                if (bounds != null) {
                    filteredTrashCan = trashCan.filter {
                        bounds.contains(it.toLatLng())
                    }
                    markerItem = filteredTrashCan.fastMap {
                        MarkerItem(
                            it.toLatLng(),
                            it.id.toString(),
                            it.address
                        )
                    }
                }
            }
    }
    Box {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings,
            properties = mapProperties,
            onMapLoaded = {
                val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
                onBoundsChange(
                    bounds
                )
                if (bounds != null) {
                    filteredTrashCan = trashCan.filter {
                        bounds.contains(it.toLatLng())
                    }
                    markerItem = filteredTrashCan.fastMap {
                        MarkerItem(
                            it.toLatLng(),
                            it.id.toString(),
                            it.address
                        )
                    }
                }
            }
        ) {
//            val list = filteredTrashCan.map {
//                MarkerItem(
//                    it.toLatLng(),
//                    it.id.toString(),
//                    it.address
//                )
//            }
            Clustering(markerItem)
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