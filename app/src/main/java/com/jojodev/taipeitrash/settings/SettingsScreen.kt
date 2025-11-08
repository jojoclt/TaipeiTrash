package com.jojodev.taipeitrash.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jojodev.taipeitrash.BuildConfig
import com.jojodev.taipeitrash.core.model.City
import com.jojodev.taipeitrash.startup.StartupViewModel
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    startupViewModel: StartupViewModel = hiltViewModel()
) {
    var showCitySelection by remember { mutableStateOf(false) }

    // Collect state inside this top-level composable (viewModel is obtained here)
    val isLoaded by startupViewModel.isLoaded.collectAsStateWithLifecycle()
    val loadingProgress by startupViewModel.loadingProgress.collectAsStateWithLifecycle()
    val lastRefresh by startupViewModel.lastRefresh.collectAsStateWithLifecycle("")
    val trashCanLast by startupViewModel.trashCanLastRefresh.collectAsStateWithLifecycle("")
    val trashCarLast by startupViewModel.trashCarLastRefresh.collectAsStateWithLifecycle("")
    val selectedCity by startupViewModel.selectedCity.collectAsStateWithLifecycle()

    BackHandler {
        if (showCitySelection) {
            showCitySelection = false
        } else {
            onNavigateBack()
        }
    }

    AnimatedContent(
        showCitySelection,
        transitionSpec = {
            val enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = spring()
            )
            val exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = spring()
            )
            enter.togetherWith(exit)
        },
    ) {
        if (it) CitySelectionScreen(
            selectedCity = selectedCity,
            onCitySelected = startupViewModel::setCity,
            onNavigateBack = { showCitySelection = false }
        )
        else SettingsScreenContent(
            onNavigateBack = onNavigateBack,
            isLoaded = isLoaded,
            loadingProgress = loadingProgress,
            lastRefresh = lastRefresh,
            trashCanLast = trashCanLast,
            trashCarLast = trashCarLast,
            selectedCity = selectedCity,
            onCityClick = { showCitySelection = true },
            onForceRefresh = startupViewModel::forceRefresh,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreenContent(
    onNavigateBack: () -> Unit,
    isLoaded: Boolean?,
    loadingProgress: Float,
    lastRefresh: String,
    trashCanLast: String,
    trashCarLast: String,
    selectedCity: City,
    onCityClick: () -> Unit,
    onForceRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // City Selection Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = onCityClick
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "City",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedCity.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select city",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Tap to change city",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Data Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider()

                    if (isLoaded == false) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(24.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Refreshing... ${(loadingProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Button(
                            onClick = onForceRefresh,
                            modifier = Modifier.fillMaxWidth(),
                            shapes = ButtonDefaults.shapes()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Force Refresh Data")
                        }

                        if (lastRefresh.isNotEmpty()) {
                            Text(
                                text = "Last refreshed: $lastRefresh",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Show per-source last refresh times
                        Spacer(Modifier.height(8.dp))

                        SettingItem(
                            label = "Trash cans",
                            value = if (trashCanLast.isNotEmpty()) trashCanLast else "Not available"
                        )

                        if (trashCarLast.isNotEmpty()) {
                            SettingItem(
                                label = "Garbage trucks",
                                value = trashCarLast
                            )
                        }

                        Text(
                            text = "Refresh trash can and garbage truck data from the server",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // About Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider()

                    SettingItem(
                        label = "Version",
                        value = BuildConfig.VERSION_NAME
                    )

                    SettingItem(
                        label = "Build",
                        value = BuildConfig.VERSION_CODE.toString()
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Made with ❤️ by Jojonosaurus",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}


@Composable
private fun SettingItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    TaipeiTrashTheme {
        SettingsScreenContent(
            onNavigateBack = {},
            isLoaded = true,
            loadingProgress = 1f,
            lastRefresh = "2025-11-04 12:00",
            trashCanLast = "2025-11-04 11:50",
            trashCarLast = "2025-11-04 11:55",
            selectedCity = City.TAIPEI,
            onCityClick = {},
            onForceRefresh = {}
        )
    }
}
