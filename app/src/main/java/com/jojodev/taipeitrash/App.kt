package com.jojodev.taipeitrash

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.jojodev.taipeitrash.core.helper.openAppSettings
import com.jojodev.taipeitrash.core.helper.plus
import com.jojodev.taipeitrash.core.model.TrashModel
import com.jojodev.taipeitrash.core.model.TrashType
import com.jojodev.taipeitrash.core.presentation.BottomSheetContent
import com.jojodev.taipeitrash.core.presentation.TaipeiTrashBottomSheet
import com.jojodev.taipeitrash.core.presentation.TrashDetailBottomSheet
import com.jojodev.taipeitrash.core.presentation.TrashMap
import com.jojodev.taipeitrash.core.presentation.TrashMarkerIcon
import com.jojodev.taipeitrash.core.presentation.TrashTab
import com.jojodev.taipeitrash.startup.StartupViewModel
import com.jojodev.taipeitrash.trashcan.data.TrashCan
import com.jojodev.taipeitrash.trashcar.data.TrashCar
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest

@Composable
fun App(modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }

    // Request permission first
    LocationPermissionRequest {
        AppContent(
            isExpanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            modifier = modifier
        )
    }
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
    val loadingProgress by startupViewModel.loadingProgress.collectAsStateWithLifecycle()

    // Persist across tab switches and settings navigation
    var selectedTrashModel by remember { mutableStateOf<Pair<TrashModel, TrashType>?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(TrashTab.TrashCan) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    // Check if first time loading or already loaded
    when {
        isLoaded == null -> {
            // Initial splash screen - show before checking anything
            SplashScreen(modifier = modifier)
        }

        !isLoaded!! -> {
            // Loading data - show progress
            val loadingFloat by animateFloatAsState(loadingProgress)
            BoardingScreen(
                progress = loadingFloat,
                modifier = modifier
            )
        }

        else -> {
            // Data loaded - render main content always and overlay settings as animated sheet
            var isMapLoaded by remember { mutableStateOf(false) }

            val trashCan by startupViewModel.trashCan.collectAsStateWithLifecycle()
            val trashCar by startupViewModel.trashCar.collectAsStateWithLifecycle()
            var filteredTrashCan by remember { mutableStateOf(emptyList<TrashCan>()) }
            var filteredTrashCar by remember { mutableStateOf(emptyList<TrashCar>()) }

            // Track selected marker ID for highlighting
            val selectedMarkerId = selectedTrashModel?.first?.id

            val cameraPositionState = rememberCameraPositionState {
                val taipeiMain = LatLng(25.0474, 121.5171)
                position = CameraPosition.fromLatLngZoom(taipeiMain, 17f)
            }

            LaunchedEffect(selectedTab, cameraPositionState.isMoving, isMapLoaded) {
                snapshotFlow { cameraPositionState.isMoving }
                    .mapLatest { it }
                    .filter { !it }.collect {
                        val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
                        val zoom = cameraPositionState.position.zoom

                        if (zoom >= 16f) {
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

            // Set expanded by default if there's a selection
            LaunchedEffect(selectedTrashModel) {
                if (selectedTrashModel != null) {
                    onExpandedChange(true)
                }
            }

            // Clear selection when tab doesn't match selected item type
            LaunchedEffect(selectedTab, selectedTrashModel) {
                selectedTrashModel?.let { (_, type) ->
                    val tabMatchesSelection = when (selectedTab) {
                        TrashTab.TrashCan -> type == TrashType.TRASH_CAN
                        TrashTab.GarbageTruck -> type == TrashType.GARBAGE_TRUCK
                    }
                    if (!tabMatchesSelection) {
                        selectedTrashModel = null
                    }
                }
            }

            // Center map on selected marker
            LaunchedEffect(selectedTrashModel) {
                selectedTrashModel?.let { (model, _) ->
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(
                            model.toLatLng(),
                            17f
                        ),
                        durationMs = 500
                    )
                }
            }

            // Main UI with bottom sheet and map (always mounted)
            TaipeiTrashBottomSheet(
                isExpanded = isExpanded,
                selectedTab = selectedTab,
                bottomSheetContent = {
                    selectedTrashModel?.let { (model, type) ->
                        TrashDetailBottomSheet(
                            trashModel = model,
                            trashType = type
                        )
                    } ?: BottomSheetContent()
                },
                onTabChange = {
                    selectedTab = it
                },
                onExpanded = onExpandedChange,
                modifier = modifier
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    TrashMap(
                        cameraPositionState = cameraPositionState,
                        contentPadding = paddingValues + WindowInsets.statusBars.asPaddingValues(),
                        onMapLoaded = { isMapLoaded = true }
                    ) {
                        when (selectedTab) {
                            TrashTab.TrashCan -> {
                                // Use fastForEach for performance; remember marker state per item
                                filteredTrashCan.fastForEach { trashCan ->
                                    val isSelected = selectedMarkerId == trashCan.id

                                    // Use the composable helper which keeps marker state updated with position changes
                                    val markerState = rememberUpdatedMarkerState(position = trashCan.toLatLng())

                                    MarkerComposable(
                                        keys = arrayOf(trashCan.id),
                                        state = markerState,
                                        title = trashCan.address,
                                        alpha = if (isSelected) 1f else 0.85f,
                                        zIndex = if (isSelected) 10f else 0f,
                                        onClick = {
                                            selectedTrashModel = trashCan to TrashType.TRASH_CAN
                                            true
                                        }
                                    ) {
                                        TrashMarkerIcon(
                                            trashType = TrashType.TRASH_CAN,
                                            modifier = Modifier.size(if (isSelected) 48.dp else 32.dp)
                                        )
                                    }
                                }
                            }

                            TrashTab.GarbageTruck -> {
                                filteredTrashCar.fastForEach { trashCar ->
                                    val isSelected = selectedMarkerId == trashCar.id

                                    val markerState = rememberUpdatedMarkerState(position = trashCar.toLatLng())

                                    MarkerComposable(
                                        keys = arrayOf(trashCar.id),
                                        state = markerState,
                                        title = trashCar.address,
                                        alpha = if (isSelected) 1f else 0.85f,
                                        zIndex = if (isSelected) 10f else 0f,
                                        onClick = {
                                            selectedTrashModel = trashCar to TrashType.GARBAGE_TRUCK
                                            true
                                        }
                                    ) {
                                        TrashMarkerIcon(
                                            trashType = TrashType.GARBAGE_TRUCK,
                                            modifier = Modifier.size(if (isSelected) 48.dp else 32.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Settings FAB - Top Left with status bar padding
                    SmallFloatingActionButton(
                        onClick = { showSettings = true },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(
                                start = 16.dp,
                                top = WindowInsets.statusBars.asPaddingValues()
                                    .calculateTopPadding() + 16.dp
                            )
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            }

            // Overlay settings screen on top of the map (keeps map mounted underneath)
            AnimatedVisibility(
                visible = showSettings,
                enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = spring()),
                exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = spring())
            ) {
                com.jojodev.taipeitrash.settings.SettingsScreen(
                    onNavigateBack = { showSettings = false },
                    startupViewModel = startupViewModel
                )
            }

        }
    }

}


@Composable
fun SplashScreen(
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
            // App icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = "Taipei Trash",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
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
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // App icon or logo placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = "Taipei Trash",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Loading trash data...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.height(64.dp),
                strokeWidth = 6.dp
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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

    // Don't show UI until we've checked permission state
    if (permissionGranted == null) {
        // Still checking permission status
        return
    }
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

    if (permissionGranted == true) {
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
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
}
