package com.jojodev.taipeitrash

import BoardingScreen
import SplashScreen
import android.app.Activity
import android.content.Context
import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.google.android.gms.location.LocationServices
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
import com.jojodev.taipeitrash.core.presentation.rememberCurrentMinute
import com.jojodev.taipeitrash.settings.SettingsScreen
import com.jojodev.taipeitrash.startup.StartupViewModel
import com.jojodev.taipeitrash.trashcan.data.TrashCan
import com.jojodev.taipeitrash.trashcar.data.TrashCar
import com.jojodev.taipeitrash.ui.components.MyLocationButton
import com.jojodev.taipeitrash.ui.components.PermissionDialog
import com.jojodev.taipeitrash.ui.components.UserLocationMarker
import com.jojodev.taipeitrash.ui.components.VerticalZoomControls
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun App(modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }

    val startupViewModel: StartupViewModel = hiltViewModel()
    val isLoaded by startupViewModel.isLoaded.collectAsStateWithLifecycle()
    val loadingProgress by startupViewModel.loadingProgress.collectAsStateWithLifecycle()
    val isFirstLaunch by startupViewModel.isFirstLaunch.collectAsStateWithLifecycle()

    val trashCan by startupViewModel.trashCan.collectAsStateWithLifecycle()
    val trashCar by startupViewModel.trashCar.collectAsStateWithLifecycle()
    val selectedCity by startupViewModel.selectedCity.collectAsStateWithLifecycle()

    // Show first launch city selection if it's the first time
    if (isFirstLaunch) {
        com.jojodev.taipeitrash.onboarding.FirstLaunchCitySelection(
            onCitySelected = { city ->
                startupViewModel.completeFirstLaunch(city)
            }
        )
    } else {
        // Show app content
        AppContent(
            isExpanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            modifier = modifier,
            isLoaded = isLoaded,
            loadingProgress = loadingProgress,
            trashCan = trashCan.toPersistentList(),
            trashCar = trashCar.toPersistentList(),
            selectedCity = selectedCity
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalCoroutinesApi::class)
@Composable
fun AppContent(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isLoaded: Boolean? = true,
    loadingProgress: Float = 0f,
    trashCan: PersistentList<TrashCan> = persistentListOf(),
    trashCar: PersistentList<TrashCar> = persistentListOf(),
    selectedCity: com.jojodev.taipeitrash.core.model.City = com.jojodev.taipeitrash.core.model.City.TAIPEI
) {
    val context = LocalContext.current

    // Persist across tab switches and settings navigation
    var selectedTrashModel by remember { mutableStateOf<Pair<TrashModel, TrashType>?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(TrashTab.TrashCan) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    when {
        isLoaded == null -> {
            // Initial splash screen - show before checking anything
            SplashScreen(modifier = modifier)
        }

        !isLoaded -> {
            // Loading data - show progress
            val loadingFloat by animateFloatAsState(loadingProgress)
            BoardingScreen(
                progress = loadingFloat,
                modifier = modifier
            )
        }

        else -> {
            val scope = rememberCoroutineScope()
            val activity = LocalActivity.current

            // Data loaded - render main content always and overlay settings as animated sheet
            var isMapLoaded by remember { mutableStateOf(false) }

            // Track user location manually using PermissionViewModel
            val permissionViewModel: PermissionViewModel =
                viewModel { PermissionViewModel(context) }
            val hasLocationPermission by permissionViewModel.permissionGranted.collectAsStateWithLifecycle()
            val wasPermissionDenied by permissionViewModel.wasPermissionDenied.collectAsStateWithLifecycle()
            var userLocation by remember { mutableStateOf<LatLng?>(null) }
            var showPermissionDialog by remember { mutableStateOf(false) }

            var filteredTrashCan by remember { mutableStateOf(emptyList<TrashCan>()) }
            var filteredTrashCar by remember { mutableStateOf(emptyList<TrashCar>()) }

            // Track selected marker ID for highlighting
            val selectedMarkerId = selectedTrashModel?.first?.id

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(selectedCity.getDefaultLocation(), 17f)
            }

            LaunchedEffect(selectedTab, cameraPositionState.isMoving, isMapLoaded) {
                snapshotFlow { cameraPositionState.isMoving }
                    .filter { !it }.collect {
                        val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
                        val zoom = cameraPositionState.position.zoom
                        if (zoom >= 16f) {
                            withContext(Dispatchers.Default) {
                                bounds?.let { bound ->
                                    when (selectedTab) {
                                        TrashTab.TrashCan -> filteredTrashCan =
                                            trashCan.fastFilter { bound.contains(it.toLatLng()) }

                                        TrashTab.GarbageTruck -> filteredTrashCar =
                                            trashCar.fastFilter { bound.contains(it.toLatLng()) }
                                    }
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
                        // Subscribe once per composition to the minute ticker and pass down to markers
                        val currentMinute = rememberCurrentMinute()

                        // Show custom user location marker if location is available
                        userLocation?.let { location ->
                            MarkerComposable(
                                state = rememberUpdatedMarkerState(position = location),
                                title = "My Location",
                                zIndex = 100f // Always on top
                            ) {
                                UserLocationMarker(modifier = Modifier.size(40.dp))
                            }
                        }

                        when (selectedTab) {
                            TrashTab.TrashCan -> {
                                // Use fastForEach for performance; remember marker state per item
                                filteredTrashCan.fastForEach { trashCan ->
                                    val isSelected = selectedMarkerId == trashCan.id

                                    // Use the composable helper which keeps marker state updated with position changes
                                    val markerState =
                                        rememberUpdatedMarkerState(position = trashCan.toLatLng())

                                    MarkerComposable(
                                        // include id and isSelected and importDate to force recreation when important data changes
                                        keys = arrayOf(
                                            trashCan.id,
                                            trashCan.importDate,
                                            isSelected
                                        ),
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
                                            currentMinute = currentMinute,
                                            modifier = Modifier.size(if (isSelected) 48.dp else 32.dp)
                                        )
                                    }
                                }
                            }

                            TrashTab.GarbageTruck -> {
                                filteredTrashCar.fastForEach { trashCar ->
                                    val isSelected = selectedMarkerId == trashCar.id

                                    val markerState =
                                        rememberUpdatedMarkerState(position = trashCar.toLatLng())

                                    MarkerComposable(
                                        // include id, arrival/departure times and selection so icon updates immediately
                                        keys = arrayOf(
                                            trashCar.id,
                                            trashCar.timeArrive,
                                            trashCar.timeLeave,
                                            isSelected
                                        ),
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
                                            arrivalTime = trashCar.timeArrive,
                                            departureTime = trashCar.timeLeave,
                                            currentMinute = currentMinute,
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
                            .systemBarsPadding()
                            .displayCutoutPadding()
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }

                    // Location permission launcher
                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        permissionViewModel.setPermissionGranted(isGranted)
                        if (isGranted) {
                            scope.launch {
                                val location = getCurrentLocation(context)
                                location?.let {
                                    userLocation = it
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(it, 17f)
                                    )
                                }
                            }
                        }
                    }

                    MyLocationButton(
                        hasPermission = hasLocationPermission == true,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .systemBarsPadding()
                            .displayCutoutPadding()
                            .padding(16.dp)
                    ) {
                        if (hasLocationPermission == true) {
                            scope.launch {
                                val location = getCurrentLocation(context)
                                location?.let {
                                    userLocation = it
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(it, 17f)
                                    )
                                }
                            }
                        } else {
                            // Check if permission is permanently denied
                            // Permission is permanently denied if:
                            // 1. User has denied it before (wasPermissionDenied = true from DataStore)
                            // 2. AND shouldShowRequestPermissionRationale returns false (meaning "Don't ask again" was checked)
                            val isPermanentlyDenied = wasPermissionDenied &&
                                    activity?.let { act ->
                                        !shouldShowRequestPermissionRationale(
                                            act,
                                            permissionViewModel.permission
                                        )
                                    } == true

                            if (isPermanentlyDenied) {
                                // Show dialog to go to settings
                                showPermissionDialog = true
                            } else {
                                // Request permission normally
                                locationPermissionLauncher.launch(permissionViewModel.permission)
                            }
                        }
                    }

                    // Vertical Zoom Controls
                    VerticalZoomControls(
                        onZoomIn = {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.zoomIn()
                                )
                            }
                        },
                        onZoomOut = {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.zoomOut()
                                )
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .displayCutoutPadding()
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                            .padding(paddingValues)
                            .padding(16.dp)

                    )

                    // Permission Dialog
                    if (showPermissionDialog) {
                        PermissionDialog(
                            onDismiss = { showPermissionDialog = false },
                            onOpenSettings = {
                                activity?.openAppSettings()
                                showPermissionDialog = false
                            }
                        )
                    }

                    // No data available message - show when trash can tab is selected but city has no trash cans
                    if (isMapLoaded && selectedTab == TrashTab.TrashCan && trashCan.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                )
                                .padding(20.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No Trash Cans Available",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${selectedCity.displayName} doesn't have trash can data in our system.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Try switching to Garbage Truck tab to see collection routes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Zoom prompt - show when zoom is too low to display markers
                    val currentZoom = cameraPositionState.position.zoom
                    if (isMapLoaded && currentZoom < 16f &&
                        !(selectedTab == TrashTab.TrashCan && trashCan.isEmpty())) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Zoom in to see markers",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Pinch to zoom or double tap",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    selectedCity.getDefaultLocation(),
                                                    17f
                                                ),
                                                durationMs = 1000
                                            )
                                        }
                                    }
                                ) {
                                    Text("Go to ${selectedCity.displayName}")
                                }
                            }
                        }
                    }
                }
            }

            // Overlay settings screen on top of the map (keeps map mounted underneath)
            AnimatedVisibility(
                visible = showSettings,
                enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = spring()),
                exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = spring())
            ) {
               SettingsScreen(
                    onNavigateBack = { showSettings = false },
                )
            }

        }
    }

}

suspend fun getCurrentLocation(context: Context): LatLng? {
    return try {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val location = fusedClient.lastLocation.await() ?: return null
        LatLng(location.latitude, location.longitude)
    } catch (e: SecurityException) {
        null
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

