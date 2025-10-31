package com.jojodev.taipeitrash.core.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme

enum class TrashTab {
    TrashCan,
    GarbageTruck
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaipeiTrashBottomSheet(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    selectedTab: TrashTab = TrashTab.TrashCan,
    onTabChange: (TrashTab) -> Unit = {},
    bottomSheetContent: @Composable (ColumnScope.() -> Unit) = {},
    content: @Composable (PaddingValues) -> Unit
) {
    BaseSheetScaffold(isExpanded = isExpanded, modifier = modifier, bottomSheetContent = {
        TopHeader(selectedTab = selectedTab, onTabChange = onTabChange)
        bottomSheetContent()
    }, content = content)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopHeader(
    modifier: Modifier = Modifier,
    selectedTab: TrashTab = TrashTab.TrashCan,
    onTabChange: (TrashTab) -> Unit = {}
) {
    val options = listOf("Trash Can", "Garbage Truck")
//        val unCheckedIcons =
//            listOf(Icons.Outlined.Work, Icons.Outlined.Restaurant, Icons.Outlined.Coffee)
//        val checkedIcons = listOf(Icons.Filled.Work, Icons.Filled.Restaurant, Icons.Filled.Coffee)

    Row(
        modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        val modifiers = listOf(Modifier.weight(1f), Modifier.weight(1.5f), Modifier.weight(1f))

        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = selectedTab.ordinal == index,
                onCheckedChange = {
                    onTabChange(TrashTab.entries[index])
                },
                modifier = modifiers[index].semantics { role = Role.RadioButton },
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                elevation = ButtonDefaults.elevatedButtonElevation()
            ) {
//                    Icon(
//                        if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index],
//                        contentDescription = "Localized description",
//                    )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(label)
            }
        }
    }
}

@Preview
@Composable
private fun TaipeiTrashBottomSheetPreview() {
    TaipeiTrashTheme {
        TaipeiTrashBottomSheet(isExpanded = true) {}
    }
}