package com.jojodev.taipeitrash.trashcar.presentation

import com.jojodev.taipeitrash.trashcan.presentation.TrashMap
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.ScaleBar
import com.jojodev.taipeitrash.IndeterminateCircularIndicator
import com.jojodev.taipeitrash.PermissionViewModel
import com.jojodev.taipeitrash.core.Results
import com.jojodev.taipeitrash.trashcar.TrashCarViewModel
import com.jojodev.taipeitrash.trashcar.data.TrashCar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TrashCarScreen(permissionViewModel: PermissionViewModel = viewModel()) {
    val viewModel: TrashCarViewModel = hiltViewModel()
    val uiStatus by viewModel.uiState.collectAsStateWithLifecycle()

    val locationPermission by permissionViewModel.permissionGranted.collectAsStateWithLifecycle()
    val isLaunchedOnce by permissionViewModel.isLaunchedOnce.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionViewModel.setPermissionGranted(granted)
    }

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
            Log.e("TrashCarScreen", s.toString())
            IndeterminateCircularIndicator(loadStatus = false) {
                when (it) {
                    true -> viewModel.fetchData()
                    false -> viewModel.cancelFetchData()
                }
            }
        }

        is Results.Success -> {
            val data = s.data
            Log.d("TrashCarScreen", "data: ${data.size}")
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
            } else TrashCarScreenContent(data = data)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashCarScreenContent(
    modifier: Modifier = Modifier,
    data: List<TrashCar>
) {
    Box(modifier = modifier) {
        if (data.isNotEmpty()) TrashCarMap(data)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CenterAlignedTopAppBar(
                title = { Text("Trash Car") }
            )
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class, MapsComposeExperimentalApi::class)
@Composable
fun TrashCarMap(
    trashCar: List<TrashCar> = emptyList(),
    onBoundsChange: (LatLngBounds?) -> Unit = {}
) {
    val taipeiMain = LatLng(25.0330, 121.5654)
    var filteredTrashCar by remember { mutableStateOf(trashCar) }
    var markerItem by remember { mutableStateOf(emptyList<MarkerItem>()) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(taipeiMain, 12f)
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        snapshotFlow { cameraPositionState.isMoving }
            .mapLatest { it }
            .filter { !it }.collect {
                val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
                onBoundsChange(bounds)

                if (bounds != null) {
                    filteredTrashCar = trashCar.filter {
                        bounds.contains(it.toLatLng())
                    }
                    markerItem = filteredTrashCar.fastMap {
                        MarkerItem(
                            it.toLatLng(),
                            "${it.timeArrive} ~ ${it.timeLeave}",
                            it.address
                        )
                    }
                }
            }
    }

    TrashMap(cameraPositionState = cameraPositionState) {
        Clustering(markerItem)
    }
}

data class MarkerItem(
    val itemPosition: LatLng,
    val itemTitle: String,
    val itemSnippet: String,
    val itemZIndex: Float = 0f
) : ClusterItem {
    override fun getPosition(): LatLng = itemPosition
    override fun getTitle(): String = itemTitle
    override fun getSnippet(): String = itemSnippet
    override fun getZIndex(): Float = itemZIndex
}

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}