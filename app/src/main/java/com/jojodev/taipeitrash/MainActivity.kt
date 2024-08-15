package com.jojodev.taipeitrash

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme

// Use AppCompatActivity instead of ComponentActivity for Android 12 and lower to support language switching (AppCompat API)
class MainActivity : AppCompatActivity() {

    val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaipeiTrashTheme {
                Log.v("MainActivity", "viewModel.uistate: ${viewModel.uistate}")
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
                                val res = viewModel.result!!.result.results.map { it.address }
                                items(res) {
                                    Text(
                                        text = it,
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
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.hello, name),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TaipeiTrashTheme {
        Greeting("Android")
    }
}

@Composable
fun IndeterminateCircularIndicator() {
    var loading by remember { mutableStateOf(false) }

    Button(onClick = { loading = true }, enabled = !loading) {
        Text("Start loading")
    }

    if (!loading) return

    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Preview(showBackground = true)
@Composable
fun IndeterminateCircularIndicatorPreview() {
    TaipeiTrashTheme {
        IndeterminateCircularIndicator()
    }
}