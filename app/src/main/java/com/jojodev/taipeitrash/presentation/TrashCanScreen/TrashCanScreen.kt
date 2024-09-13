package com.jojodev.taipeitrash.presentation.TrashCanScreen

import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.jojodev.taipeitrash.ApiStatus
import com.jojodev.taipeitrash.Greeting
import com.jojodev.taipeitrash.IndeterminateCircularIndicator
import com.jojodev.taipeitrash.MainViewModel
import com.jojodev.taipeitrash.R
import com.jojodev.taipeitrash.data.TrashCan

@Composable
fun TrashCanScreen(onButtonClick: () -> Unit, uiStatus: ApiStatus, res: List<TrashCan>) {
    Column {
        Text("TrashCanScreen")

        when (uiStatus) {
            ApiStatus.LOADING -> {
                IndeterminateCircularIndicator { onButtonClick() }
            }

            ApiStatus.ERROR -> {
                Text(
                    text = "ERROR",
                    modifier = Modifier.padding(16.dp)
                )
            }

            ApiStatus.DONE -> {
                Log.i("TrashCanScreen", res.size.toString())
                TrashCanMap(trashCan = res)
            }
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
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
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
                }
                catch (e: Exception) {
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
