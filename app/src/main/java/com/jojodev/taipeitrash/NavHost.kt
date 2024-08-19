package com.jojodev.taipeitrash

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jojodev.taipeitrash.data.TrashCan
import com.jojodev.taipeitrash.presentation.TrashCanScreen.TrashCanScreen
import kotlinx.serialization.Serializable

@Composable
fun MainNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.TrashCanScreen) {
        composable<Routes.TrashCanScreen> {
            TrashCanScreen(navController)
        }
        
    }
}

@Serializable
sealed class Routes {
    @Serializable
    data object TrashCanScreen : Routes()

    @Serializable
    data class TrashCanDetailScreen(val trashLoc: TrashCan) : Routes()


}