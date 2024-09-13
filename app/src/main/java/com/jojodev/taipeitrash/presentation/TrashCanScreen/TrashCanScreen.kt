package com.jojodev.taipeitrash.presentation.TrashCanScreen

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.jojodev.taipeitrash.ApiStatus
import com.jojodev.taipeitrash.IndeterminateCircularIndicator
import com.jojodev.taipeitrash.data.TrashCan

@Composable
fun TrashCanScreen(onButtonClick: (Boolean) -> Unit, uiStatus: ApiStatus, res: List<TrashCan>) {
    var locationAccess by remember {
        mutableStateOf(false)
    }
    Column {
        Text("TrashCanScreen")
        when (uiStatus) {
            ApiStatus.LOADING -> {
                IndeterminateCircularIndicator(false) { onButtonClick(it) }
            }

            ApiStatus.ERROR -> {
                IndeterminateCircularIndicator(false) { onButtonClick(it) }
                Text(
                    text = "ERROR",
                    modifier = Modifier.padding(16.dp)
                )
            }

            ApiStatus.DONE -> {

                Log.i("TrashCanScreen", res.size.toString())
                if (!locationAccess) {
                    RequestMultiplePermissions {
                        locationAccess = it
                        Log.v("TrashCanScreen", "RequestMultiplePermissions")
                        if (it) {
                            Log.v("TrashCanScreen", "RequestMultiplePermissions: true")
                        } else {
                            Log.v("TrashCanScreen", "RequestMultiplePermissions: false")
                        }
                    }
                }

                if (locationAccess) {
                    TrashCanMap(res)
                }
                else {
                    Text("Location Access Denied")
                }
            }
        }
    }
}

@Composable
fun RequestMultiplePermissions(permissions: (Boolean) -> Unit) {
    // State to store whether all permissions are granted
    val permissionsGranted = remember { mutableStateMapOf<String, Boolean>() }
//    Despite using "remember", it does not persist, it will get destroyed when changing screen, but persist across the same composable recompositions.

    // Permission launcher for multiple permissions
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
                    Manifest.permission.CAMERA,
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
            Log.i("RequestMultiplePermissions", "$permission: ${if (granted) "Granted" else "Denied"}")
        }
    }
}


@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun TrashCanMap(trashCan: List<TrashCan>) {
    val taipeiMain = LatLng(25.0330, 121.5654)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(taipeiMain, 10f)
    }
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
//                maxZoomPreference = 10f,
                minZoomPreference = 10f,
                isMyLocationEnabled = true
            )
        )
    }
    var mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(mapToolbarEnabled = true)
        )
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings,
        properties = mapProperties
    ) {

        val trashMarker = remember {
            trashCan.mapNotNull {
                try {
                    MarkerItem(
                        LatLng(
                            it.latitude.removePrefix("?").toDouble(),
                            it.longitude.removePrefix("?").toDouble()
                        ), it.address, "Marker in ${it._id}"
                    )
                } catch (e: Exception) {
                    Log.e("TrashCanMap", "Error Converting at idx ${it._id}\n $it")
                    null
                }
            }.toMutableStateList()

        }
//        else this foreach will run forever (cuz of recomposition?)
//        LaunchedEffect(key1 = trashCan) {
//            trashMarker.clear()
//            trashCan.forEach {
////                Log.v("TrashCanMap", "trashCan: ${it.address}")
//                trashMarker.add(MarkerItem(LatLng(it.latitude, it.longitude), it.address, "Marker in ${it._id}"))
//            }
//            Log.v("TrashCanMap", "trashMarker: ${trashMarker.size}")
//        }
        Clustering(items = trashMarker)
    }
}


@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun mapCompose() {
    val hydePark = LatLng(51.508610, -0.163611)
    val regentsPark = LatLng(51.531143, -0.159893)
    val primroseHill = LatLng(51.539556, -0.16076088)

    val crystalPalacePark = LatLng(51.42153, -0.05749)
    val greenwichPark = LatLng(51.476688, 0.000130)
    val lloydPark = LatLng(51.364188, -0.080703)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(hydePark, 10f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {

        val parkMarkers = remember {
            mutableStateListOf(
                MarkerItem(hydePark, "Hyde Park", "Marker in hyde Park"),
                MarkerItem(regentsPark, "Regents Park", "Marker in Regents Park"),
                MarkerItem(primroseHill, "Primrose Hill", "Marker in Primrose Hill"),
                MarkerItem(crystalPalacePark, "Crystal Palace", "Marker in Crystal Palace"),
                MarkerItem(greenwichPark, "Greenwich Park", "Marker in Greenwich Park"),
                MarkerItem(lloydPark, "Lloyd park", "Marker in Lloyd Park"),
            )
        }

        Clustering(items = parkMarkers)
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
