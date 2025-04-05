package com.jojodev.taipeitrash.trashcar.presentation

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.jojodev.taipeitrash.IndeterminateCircularIndicator
import com.jojodev.taipeitrash.PermissionViewModel
import com.jojodev.taipeitrash.core.Results
import com.jojodev.taipeitrash.core.presentation.TrashMap
import com.jojodev.taipeitrash.trashcan.presentation.BottomSheetScaffold
import com.jojodev.taipeitrash.trashcan.presentation.openAppSettings
import com.jojodev.taipeitrash.trashcar.TrashCarViewModel
import com.jojodev.taipeitrash.trashcar.data.TrashCar
import com.jojodev.taipeitrash.trashcar.data.toLatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
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
            } else {
                var isExpanded by remember { mutableStateOf(false) }
                var selectedTrashCar by remember { mutableStateOf<TrashCar?>(null) }

                BottomSheetScaffold(
                    isExpanded = isExpanded,
                    onExpanded = { isExpanded = it },
                    bottomSheetContent = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            selectedTrashCar?.let { trashCar ->
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Trash Car Details",
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                                        DetailRow(title = "ID", value = trashCar.id.toString())
                                        DetailRow(title = "Address", value = trashCar.address)
                                        DetailRow(
                                            title = "Arrival Time",
                                            value = trashCar.timeArrive
                                        )
                                        DetailRow(
                                            title = "Departure Time",
                                            value = trashCar.timeLeave
                                        )
                                        DetailRow(title = "District", value = trashCar.district)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Location",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "${
                                                    trashCar.latitude.toString().take(7)
                                                }, ${trashCar.longitude.toString().take(7)}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            } ?: run {
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Select a trash car to view details")
                                    }
                                }
                            }
                        }
                    }) {
                    TrashCarScreenContent(
                        data = data,
                        selectedTrashCar = selectedTrashCar,
                        onSelect = {
                            selectedTrashCar = it
                            isExpanded = true
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashCarScreenContent(
    modifier: Modifier = Modifier,
    data: List<TrashCar>,
    selectedTrashCar: TrashCar? = null,
    onSelect: (TrashCar) -> Unit = {}
) {
    Box(modifier = modifier) {
        if (data.isNotEmpty()) TrashCarMap(
            data,
            selectedTrash = selectedTrashCar,
            onClick = onSelect
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun TrashCarMap(
    trashCar: List<TrashCar> = emptyList(),
    selectedTrash: TrashCar? = null,
    onClick: (TrashCar) -> Unit = {}
) {
    val taipeiMain = LatLng(25.0330, 121.5654)
    var filteredTrashCar by remember { mutableStateOf(trashCar) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(taipeiMain, 14f)
    }

    val markerState = rememberUpdatedMarkerState(selectedTrash.toLatLng())

    LaunchedEffect(selectedTrash) {
        if (selectedTrash != null)
            markerState.showInfoWindow()
    }


    LaunchedEffect(cameraPositionState.isMoving) {
        snapshotFlow { cameraPositionState.isMoving }
            .mapLatest { it }
            .filter { !it }.collect {
                val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
                val zoom = cameraPositionState.position.zoom
                if (zoom >= 16)
                    bounds?.let { bound ->
                        filteredTrashCar = trashCar.fastFilter { bound.contains(it.toLatLng()) }
                    }
                else filteredTrashCar = emptyList()
            }
    }

    TrashMap(cameraPositionState = cameraPositionState) {
        selectedTrash?.let {
            MarkerInfoWindow(
                state = markerState,
                onClick = {
                    false
                },
                title = "${it.timeArrive}~${it.timeLeave}",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                snippet = it.address
            ) { marker ->
                Card(
                    modifier = Modifier.padding(4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${marker.title}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = marker.snippet ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        filteredTrashCar.fastFilter {selectedTrash != it} .fastForEach { trashCar ->
            MarkerInfoWindow(
                state = MarkerState(trashCar.toLatLng()),
                onClick = {
                    onClick(trashCar)
                    false
                },
                title = trashCar.timeArrive,
            ) { marker ->
                Card(
                    modifier = Modifier.padding(4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Arrival: ${marker.title}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = marker.snippet ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}