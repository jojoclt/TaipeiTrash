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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.tooling.preview.Preview
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
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.jojodev.taipeitrash.IndeterminateCircularIndicator
import com.jojodev.taipeitrash.PermissionViewModel
import com.jojodev.taipeitrash.core.Results
import com.jojodev.taipeitrash.core.presentation.BaseSheetScaffold
import com.jojodev.taipeitrash.core.presentation.TrashMap
import com.jojodev.taipeitrash.trashcan.TrashCanAction
import com.jojodev.taipeitrash.trashcan.TrashCanViewModel
import com.jojodev.taipeitrash.trashcan.data.TrashCan
import com.jojodev.taipeitrash.trashcan.data.toLatLng
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
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
            } else {
                var isExpanded by remember { mutableStateOf(false) }
                var selectedTrash by remember { mutableStateOf<TrashCan?>(null) }
                BaseSheetScaffold(
                    isExpanded = isExpanded,
                    onExpanded = { isExpanded = it },
                    bottomSheetContent = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            selectedTrash?.let { trashCan ->
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
                                            text = "Trash Can Details",
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                                        DetailRow(title = "ID", value = "#${trashCan.id}")
                                        DetailRow(title = "District", value = trashCan.district)
                                        DetailRow(title = "Address", value = trashCan.address)
                                        DetailRow(
                                            title = "Import Date",
                                            value = trashCan.importDate
                                        )

                                        if (trashCan.remark.isNotEmpty()) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(
                                                        text = "Remarks",
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Text(
                                                        text = trashCan.remark,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                        }

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
                                                    trashCan.latitude.toString().take(7)
                                                }, ${trashCan.longitude.toString().take(7)}",
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
                                        Text("Select a trash can to view details")
                                    }
                                }
                            }
                        }
                    }) {
                    TrashCanScreenContent(data = data, selectedTrash = selectedTrash) {
                        when (it) {
                            is TrashCanAction.ShowDetail -> {
                                selectedTrash = it.trashCan
                                isExpanded = true
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun TrashCanScreenContent(
    modifier: Modifier = Modifier,
    data: List<TrashCan>,
    selectedTrash: TrashCan? = null,
    onAction: (TrashCanAction) -> Unit = {}
) {
    Box(modifier = modifier) {
        if (data.isNotEmpty()) {
            TrashCanMap(
                data,
                selectedTrash = selectedTrash,
                onClick = { onAction(TrashCanAction.ShowDetail(it)) })
        }
    }
}

@Preview
@Composable
private fun TrashCanScreenPreview() {
    TaipeiTrashTheme {
        TrashCanScreenContent(data = emptyList())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun TrashCanMap(
    trashCan: List<TrashCan> = emptyList(),
    selectedTrash: TrashCan? = null,
    onClick: (TrashCan) -> Unit = {}
) {

    var filteredTrashCan by remember { mutableStateOf(trashCan) }
    var markerItem by remember { mutableStateOf(emptyList<MarkerItem>()) }

    val markerState = rememberUpdatedMarkerState(selectedTrash.toLatLng())
    LaunchedEffect(selectedTrash) {
        if (selectedTrash != null)
            markerState.showInfoWindow()
    }


    val cameraPositionState = rememberCameraPositionState {
        val taipeiMain = LatLng(25.0330, 121.5654)
        position = CameraPosition.fromLatLngZoom(taipeiMain, 12f)
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        snapshotFlow { cameraPositionState.isMoving }
            .mapLatest { it }
            .filter { !it }.collect {
                val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
                val zoom = cameraPositionState.position.zoom

                if (zoom >= 15)
                    bounds?.let { bound ->
                        filteredTrashCan = trashCan.fastFilter { bound.contains(it.toLatLng()) }
                    }
                else filteredTrashCan = emptyList()

            }
    }
//    val infiniteTransition = rememberInfiniteTransition(label = "PulseComponentAnimation")
//
//    val radius by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 50f,
//        animationSpec = InfiniteRepeatableSpec(
//            animation = tween(3000), repeatMode = RepeatMode.Restart
//        ),
//        label = "radius"
//    )
//
//    val alpha by infiniteTransition.animateFloat(
//        initialValue = 1f,
//        targetValue = 0f,
//        animationSpec = InfiniteRepeatableSpec(
//            animation = tween(3000),
//            repeatMode = RepeatMode.Restart
//        ), label = "alpha"
//    )
    TrashMap(cameraPositionState = cameraPositionState) {
        selectedTrash?.let { it ->

//            val latLng = it.toLatLng()
//            val newLat = latLng.latitude + -0.00004
//            val newLng = latLng.longitude
//
//            MarkerComposable(
//                keys = arrayOf(it.id),
//                state = MarkerState(position = LatLng(newLat, newLng))
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(18.dp)
//                        .background(color = Color(0xff2FAA59), shape = CircleShape)
//                        .border(width = 2.dp, color = Color.White, shape = CircleShape)
//                )
//            }
//
//            Circle(
//                center = latLng,
//                clickable = false,
//                fillColor = Color(0xff2FAA59).copy(alpha = alpha),
//                radius = radius.toDouble(),
//                strokeColor = Color.Transparent,
//                strokeWidth = 0f,
//                tag = "",
//                onClick = { }
//            )
            Marker(
                state = markerState,
                title = it.address,
                onClick = { _ ->
                    onClick(it)
                    false
                },
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                zIndex = 1f
            )

        }
        filteredTrashCan.fastFilter { it != selectedTrash }.fastForEach { ele ->
//            MarkerComposable(
//                keys = arrayOf(ele.id),
//                state = MarkerState(position = ele.toLatLng()),
//                title = ele.address,
//                onClick = {
//                    onClick(ele)
//                    false
//                }) {
//                Box(
//                    modifier = Modifier
//                        .size(18.dp)
//                        .background(color = Color.Red, shape = CircleShape)
//                        .border(width = 2.dp, color = Color.White, shape = CircleShape)
//                )
//
//            }
            Marker(
                state = MarkerState(position = ele.toLatLng()),
                title = ele.address,
                onClick = { _ ->
                    onClick(ele)
                    false
                },
            )
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
    fun Activity.openAppSettings() {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).also(::startActivity)
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