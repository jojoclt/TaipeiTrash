package com.jojodev.taipeitrash.presentation.TrashCanScreen

import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
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

@Composable
fun TrashCanScreen(navController: NavHostController) {
    Text("TrashCanScreen")
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    NewComposable()
//    mapCompose()
//    GoogleMap(
//        modifier = Modifier.fillMaxSize(),
//        cameraPositionState = cameraPositionState
//    ) {
//        Marker(
//            state = MarkerState(position = singapore),
//            title = "Singapore",
//            snippet = "Marker in Singapore"
//        )
//    }
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
        cameraPositionState = cameraPositionState) {

        val parkMarkers = remember {
            mutableStateListOf(
                ParkItem(hydePark, "Hyde Park", "Marker in hyde Park"),
                ParkItem(regentsPark, "Regents Park", "Marker in Regents Park"),
                ParkItem(primroseHill, "Primrose Hill", "Marker in Primrose Hill"),
                ParkItem(crystalPalacePark, "Crystal Palace", "Marker in Crystal Palace"),
                ParkItem(greenwichPark, "Greenwich Park", "Marker in Greenwich Park"),
                ParkItem(lloydPark, "Lloyd park", "Marker in Lloyd Park"),
            )
        }

        Clustering(items = parkMarkers)
    }
}


data class ParkItem(
    val itemPosition: LatLng,
    val itemTitle: String,
    val itemSnippet: String,
    val itemZIndex: Float = 0f) : ClusterItem {
    override fun getPosition(): LatLng =
        itemPosition

    override fun getTitle(): String =
        itemTitle

    override fun getSnippet(): String =
        itemSnippet

    override fun getZIndex(): Float =
        itemZIndex
}
@Composable
fun NewComposable() {
    val viewModel = MainViewModel()
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column {
                Greeting(
                    name = stringResource(R.string.android),
                    modifier = Modifier.padding(innerPadding)
                )
                if (viewModel.uistate == ApiStatus.LOADING) {
                    IndeterminateCircularIndicator()
                }
                else if (viewModel.uistate == ApiStatus.ERROR) {
                    Text(
                        text = "ERROR",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                else {
                    LazyColumn {
                        val res = viewModel.response!!.result.trashCans
                        items(res) {
                            Text(
                                text = it.address,
                                modifier = Modifier.padding(innerPadding)
                            )
                            Text(
                                text = "${it.latitude}, ${it.longitude}",
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
//                            viewModel.result?.let {
//                                for (result in it.result.results) {
//                                    Text(
//                                        text = result.address,
//                                        modifier = Modifier.padding(innerPadding)
//                                    )
//                                }
//                            }
//                            Text(
//                                text = viewModel.result.toString(),
//                                modifier = Modifier.padding(innerPadding)
//                            )
                }
            }
        }

}