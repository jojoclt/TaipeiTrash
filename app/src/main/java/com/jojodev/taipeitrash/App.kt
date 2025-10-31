package com.jojodev.taipeitrash

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.jojodev.taipeitrash.core.helper.plus
import com.jojodev.taipeitrash.core.presentation.BottomSheetContent
import com.jojodev.taipeitrash.core.presentation.TaipeiTrashBottomSheet
import com.jojodev.taipeitrash.core.presentation.TrashMap
import com.jojodev.taipeitrash.core.presentation.TrashTab
import com.jojodev.taipeitrash.startup.StartupViewModel
import com.jojodev.taipeitrash.trashcan.data.TrashCan
import com.jojodev.taipeitrash.trashcan.data.toLatLng
import com.jojodev.taipeitrash.trashcan.presentation.openAppSettings
import com.jojodev.taipeitrash.trashcar.data.TrashCar
import com.jojodev.taipeitrash.trashcar.data.toLatLng
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest

@Composable
fun App(modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    AppContent(
        isExpanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalCoroutinesApi::class)
@Composable
fun AppContent(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
//    val trashViewModel: TrashCanViewModel = hiltViewModel()
//    val uiState by trashViewModel.uiState.collectAsStateWithLifecycle()

    val startupViewModel: StartupViewModel = hiltViewModel()
    val isLoaded by startupViewModel.isLoaded.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(TrashTab.TrashCan) }

    when {
        !isLoaded -> {
            val loadingState by startupViewModel.loadingProgress.collectAsStateWithLifecycle()
            val loadingFloat by animateFloatAsState(loadingState)

            BoardingScreen(
                progress = loadingFloat,
                modifier = modifier
            )
        }

        else -> {
            val trashCan by startupViewModel.trashCan.collectAsStateWithLifecycle()
            val trashCar by startupViewModel.trashCar.collectAsStateWithLifecycle()
            var filteredTrashCan by remember { mutableStateOf(emptyList<TrashCan>()) }
            var filteredTrashCar by remember { mutableStateOf(emptyList<TrashCar>()) }

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

                        if (zoom >= 15) {
                            bounds?.let { bound ->
                                when (selectedTab) {
                                    TrashTab.TrashCan -> filteredTrashCan =
                                        trashCan.fastFilter { bound.contains(it.toLatLng()) }

                                    TrashTab.GarbageTruck -> filteredTrashCar =
                                        trashCar.fastFilter { bound.contains(it.toLatLng()) }
                                }
                            }
                        } else {
                            filteredTrashCan = emptyList()
                            filteredTrashCar = emptyList()
                        }

                    }
            }

            TaipeiTrashBottomSheet(
                isExpanded = isExpanded,
                selectedTab = selectedTab,
                bottomSheetContent = {
                    BottomSheetContent()
                },
                onTabChange = {
                    selectedTab = it
                }) { paddingValues ->
                LocationPermissionRequest() {
                    TrashMap(
                        cameraPositionState = cameraPositionState,
                        contentPadding = paddingValues + WindowInsets.statusBars.asPaddingValues()
                    ) {
                        when (selectedTab) {
                            TrashTab.TrashCan -> {
                                filteredTrashCan.fastForEach {
                                    Marker(
                                        state = MarkerState(position = it.toLatLng()),
                                        title = it.address,
                                        onClick = { _ ->
//                                            onClick(ele)
                                            false
                                        },
                                    )
                                }
                            }

                            TrashTab.GarbageTruck -> {
                                filteredTrashCar.fastForEach {
                                    Marker(
                                        state = MarkerState(
                                            position = it.toLatLng()
                                        ),
                                        title = it.address,
                                        onClick = { _ ->
//                                            onClick(ele)
                                            false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun BoardingScreen(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Fetching Trash Data",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.height(64.dp)
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
        }
    }

}

@Preview
@Composable
private fun AppContentPreview() {
    TaipeiTrashTheme {
        AppContent(isExpanded = false, onExpandedChange = {})
    }
}

@Composable
fun LocationPermissionRequest(
    modifier: Modifier = Modifier,
    onPermissionGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val permissionViewModel: PermissionViewModel = viewModel { PermissionViewModel(context) }

    val permissionGranted by permissionViewModel.permissionGranted.collectAsStateWithLifecycle()
    val isLaunchedOnce by permissionViewModel.isLaunchedOnce.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = permissionViewModel::setPermissionGranted
    )

    LaunchedEffect(Unit) {
        // auto-launch permission if not launched yet
        if (!isLaunchedOnce) {
            permissionViewModel.setLaunchedOnce(true)
            launcher.launch(permissionViewModel.permission)
        }
    }

    if (permissionGranted) {
        onPermissionGranted()
    } else {
        // Consider it permanently denied only after we've already requested once
        val isPermanentlyDenied = isLaunchedOnce && !shouldShowRequestPermissionRationale(
            activity,
            permissionViewModel.permission
        )

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isPermanentlyDenied) {
                    Text("Please enable location permission in Settings.")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = activity::openAppSettings) {
                        Text("Open Settings")
                    }
                } else {
                    Text("Location permission is required to display your current position.")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { launcher.launch(permissionViewModel.permission) }) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}
