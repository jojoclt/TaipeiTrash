package com.jojodev.taipeitrash

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jojodev.taipeitrash.presentation.TrashCanScreen.TrashCanScreen
import com.jojodev.taipeitrash.presentation.TrashCanScreen.TrashCarScreen
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Composable
fun MainNavigation(
    navController: NavHostController,
    startDestination: Routes = Routes.TrashCanScreen,
    modifier: Modifier = Modifier
) {
//    val viewModel: MainViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Routes.TrashCanScreen> {
            val parent = remember(it) {
                navController.getBackStackEntry(Routes.TrashCanScreen)
            }
            val viewModel: TrashCanViewModel = viewModel(parent)
//            val permissionViewModel: PermissionViewModel = viewModel(parent)

            TrashCanScreen(viewModel)
        }
        composable<Routes.TrashCarScreen> {
            val parent = remember(it) {
                navController.getBackStackEntry(Routes.TrashCanScreen)
            }
            val viewModel: TrashCanViewModel = viewModel(parent)
            val uiStatus by viewModel.uistate.collectAsStateWithLifecycle()
            val trashCan by viewModel.trashCan.collectAsStateWithLifecycle()
            val importDate by viewModel.importDate.collectAsStateWithLifecycle()

            TrashCarScreen(
                onButtonClick = { status ->
                    if (status) viewModel.fetchData()
                    else viewModel.cancelFetchData()
                },
                uiStatus = uiStatus,
                res = trashCan,
                importDate = importDate
            )
        }
    }
}

//to make the routes serializable, add var or val instead of non-defining type
//https://stackoverflow.com/questions/60409838/difference-between-val-parameter-or-without-val
@Serializable
sealed class Routes(val name: String, @Contextual val icon: ImageVector) {
    @Serializable
    data object TrashCanScreen : Routes("Trash", Icons.Filled.Build) {
    }

    @Serializable
    data object TrashCarScreen : Routes("Car", Icons.Filled.Info) {
    }
//    @Serializable
//    data class TrashCanDetailScreen(val trashLoc: TrashCan) : Routes()


}