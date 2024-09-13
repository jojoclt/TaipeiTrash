package com.jojodev.taipeitrash.presentation.TrashCanScreen

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jojodev.taipeitrash.ApiStatus
import com.jojodev.taipeitrash.Greeting
import com.jojodev.taipeitrash.IndeterminateCircularIndicator
import com.jojodev.taipeitrash.R
import com.jojodev.taipeitrash.data.TrashCan
import java.text.SimpleDateFormat

@Composable
fun TrashCarScreen(
    onButtonClick: () -> Unit,
    uiStatus: ApiStatus,
    res: List<TrashCan>,
    importDate: String
) {
    Column {
        NewComposable(
            onButtonClick = onButtonClick,
            uiStatus = uiStatus,
            res = res.filter { it.address.isNotEmpty() },
            importDate = importDate
        )
    }
}

@Composable
fun NewComposable(
    onButtonClick: () -> Unit,
    uiStatus: ApiStatus,
    res: List<TrashCan>,
    importDate: String
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column {
            Greeting(
                name = stringResource(R.string.android),
                modifier = Modifier.padding(innerPadding)
            )
            when (uiStatus) {
                ApiStatus.LOADING -> {
                    IndeterminateCircularIndicator { onButtonClick() }
                }
                ApiStatus.ERROR -> {
                    Text(
                        text = "ERROR",
                        modifier = Modifier.padding(innerPadding)
                    )
                    IndeterminateCircularIndicator { onButtonClick() }
                }
                ApiStatus.DONE -> {
                    Log.i("TrashCanScreen", res.size.toString())
                    Text(
                        text = res.size.toString(),
                        modifier = Modifier.padding(innerPadding)
                    )
                    Text(text = "Date Imported: ${importDate.substring(0,10)}", modifier = Modifier.padding(innerPadding))
                    LazyColumn {
//                        val res = viewModel.response!!.result.trashCans
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
                }
            }

        }
    }

}
