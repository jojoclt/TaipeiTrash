package com.jojodev.taipeitrash

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jojodev.taipeitrash.core.helper.plus
import com.jojodev.taipeitrash.core.presentation.BottomSheetContent
import com.jojodev.taipeitrash.core.presentation.TaipeiTrashBottomSheet
import com.jojodev.taipeitrash.core.presentation.TrashMap
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme

@Composable
fun App(modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }

    AppContent(
        isExpanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier
    )
}

@Composable
fun AppContent(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    TaipeiTrashBottomSheet(isExpanded = isExpanded, bottomSheetContent = {
        BottomSheetContent()
    }) { paddingValues ->
        TrashMap(contentPadding = paddingValues + WindowInsets.systemBars.asPaddingValues()) { }
    }

}

@Preview
@Composable
private fun AppContentPreview() {
    TaipeiTrashTheme {
        AppContent(isExpanded = false, onExpandedChange = {})
    }
}