package com.jojodev.taipeitrash.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jojodev.taipeitrash.core.model.DayOfWeek
import com.jojodev.taipeitrash.ui.theme.TaipeiTrashTheme

/**
 * Displays a week indicator showing which days trash/recycle collection is available
 *
 * @param trashDays Days when trash is collected (shown in red/green)
 * @param recycleDays Days when recycling is collected (shown in blue/green)
 * @param modifier Modifier for the container
 */
@Composable
fun WeekDayIndicator(
    trashDays: Set<DayOfWeek>,
    recycleDays: Set<DayOfWeek>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DayOfWeek.entries.forEach { day ->
            val hasTrash = trashDays.contains(day)
            val hasRecycle = recycleDays.contains(day)

            DayIndicator(
                day = day,
                hasTrash = hasTrash,
                hasRecycle = hasRecycle
            )
        }
    }
}

@Composable
private fun DayIndicator(
    day: DayOfWeek,
    hasTrash: Boolean,
    hasRecycle: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        hasTrash && hasRecycle -> Color(0xFF4CAF50) // Green - both
        hasTrash -> Color(0xFFE57373) // Light Red - trash only
        hasRecycle -> Color(0xFF64B5F6) // Light Blue - recycle only
        else -> MaterialTheme.colorScheme.surfaceVariant // Gray - none
    }

    val textColor = when {
        hasTrash || hasRecycle -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.shortName,
            fontSize = 10.sp,
            fontWeight = if (hasTrash || hasRecycle) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Preview
@Composable
fun WeekDayIndicatorPreview() {
    TaipeiTrashTheme {
        WeekDayIndicator(
            trashDays = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
            ),
            recycleDays = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.FRIDAY
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
fun WeekDayIndicatorAllDaysPreview() {
    TaipeiTrashTheme {
        WeekDayIndicator(
            trashDays = DayOfWeek.entries.toSet(),
            recycleDays = emptySet(),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
fun WeekDayIndicatorNoDaysPreview() {
    TaipeiTrashTheme {
        WeekDayIndicator(
            trashDays = emptySet(),
            recycleDays = emptySet(),
            modifier = Modifier.padding(16.dp)
        )
    }
}

