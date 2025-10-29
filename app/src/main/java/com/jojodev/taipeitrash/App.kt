package com.jojodev.taipeitrash

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jojodev.taipeitrash.core.presentation.BottomSheetContent
import com.jojodev.taipeitrash.core.presentation.TaipeiTrashBottomSheet
import com.jojodev.taipeitrash.core.presentation.TrashMap
import com.jojodev.taipeitrash.startup.StartupViewModel
import com.jojodev.taipeitrash.trashcan.presentation.openAppSettings
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme

@Composable
fun App(modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    AppContent(
        isExpanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val loadingState by startupViewModel.loadingProgress.collectAsStateWithLifecycle()

    val loadingFloat by animateFloatAsState(loadingState)

    when {
        !isLoaded -> {
            // Show loading screen
            BoardingScreen(
                progress = loadingFloat,
                modifier = modifier
            )
        }
        else -> {
            TaipeiTrashBottomSheet(isExpanded = isExpanded, bottomSheetContent = {
                BottomSheetContent()
            }) { paddingValues ->
                LocationPermissionRequest() {
                    TrashMap(contentPadding = paddingValues) { }
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
                text = "載入垃圾車資料中...",
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
