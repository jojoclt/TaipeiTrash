package com.jojodev.taipeitrash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jojodev.taipeitrash.trashcan.presentation.TrashCanScreen
import com.jojodev.taipeitrash.trashcar.presentation.TrashCarScreen
import kotlinx.serialization.Serializable

@Composable
fun MainNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Routes = TrashCanScreen
) {
//    val viewModel: MainViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<TrashCanScreen> {
            val context = LocalContext.current
            val parent = remember(it) {
                navController.getBackStackEntry(TrashCanScreen)
            }
            val permissionViewModel: PermissionViewModel = viewModel(parent) {
                PermissionViewModel(context)
            }

            TrashCanScreen(permissionViewModel)
        }
        composable<TrashCarScreen> {
            val context = LocalContext.current
            val parent = remember(it) {
                navController.getBackStackEntry(TrashCanScreen)
            }
            val permissionViewModel: PermissionViewModel = viewModel(parent) {
                PermissionViewModel(context)
            }
            TrashCarScreen(permissionViewModel)
        }
    }
}

//to make the routes serializable, add var or val instead of non-defining type
//https://stackoverflow.com/questions/60409838/difference-between-val-parameter-or-without-val
@Serializable
sealed class Routes

@Serializable
data object TrashCanScreen : Routes()

@Serializable
data object TrashCarListScreen : Routes()

@Serializable
data object TrashCarScreen : Routes()

