package com.jojodev.taipeitrash.core.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseSheetScaffold(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    sheetPeekHeight: Dp = 144.dp,
    bottomSheetContent: @Composable (ColumnScope.() -> Unit) = {},
    onExpanded: (Boolean) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit = {}
) {

    Log.d("BaseSheetScaffold", "isExpanded: $isExpanded")

    BackHandler(isExpanded) {
        onExpanded(false)
    }
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = rememberStandardBottomSheetState())

    // Track sheet state changes to notify parent
    LaunchedEffect(scaffoldState.bottomSheetState) {
        snapshotFlow { scaffoldState.bottomSheetState.currentValue }
            .collect { sheetValue ->
                when (sheetValue) {
                    SheetValue.Expanded -> onExpanded(true)
                    SheetValue.PartiallyExpanded -> onExpanded(false)
                    SheetValue.Hidden -> onExpanded(false)
                }
            }
    }

    // Handle expansion state changes directly
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                bottomSheetContent()
            }
        },
        sheetPeekHeight = sheetPeekHeight,
        modifier = modifier,
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content(innerPadding)
        }
    }
}
