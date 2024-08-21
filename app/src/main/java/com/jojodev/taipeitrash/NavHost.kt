package com.jojodev.taipeitrash

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jojodev.taipeitrash.data.TrashCan
import com.jojodev.taipeitrash.presentation.TrashCanScreen.TrashCanScreen
import com.jojodev.taipeitrash.presentation.TrashCanScreen.TrashCarScreen
import kotlinx.serialization.Serializable

@Composable
fun MainNavigation(navController: NavHostController, startDestination: Routes = Routes.TrashCanScreen, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable<Routes.TrashCanScreen> {
            TrashCanScreen(navController)
        }
        composable<Routes.TrashCarScreen> {
            TrashCarScreen()
        }
    }
}

//to make the routes serializable, add var or val instead of non-defining type
//https://stackoverflow.com/questions/60409838/difference-between-val-parameter-or-without-val
@Serializable
sealed class Routes(val name: String) {
    @Serializable
    data object TrashCanScreen : Routes("Trash") {
    }

    @Serializable
    data object TrashCarScreen: Routes("Car") {
    }
//    @Serializable
//    data class TrashCanDetailScreen(val trashLoc: TrashCan) : Routes()


}