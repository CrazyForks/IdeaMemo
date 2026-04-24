package com.ldlywt.note.ui.page.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.ldlywt.note.ui.page.LocalMemosViewModel
import com.moriafly.salt.ui.SaltTheme
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("MonthHeader"),
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = SaltTheme.colors.subText,
                text = dayOfWeek.displayText(),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun Day(day: CalendarDay, today: LocalDate, hasScheme: Boolean, isSelected: Boolean, onClick: (CalendarDay) -> Unit) {
    val noteViewModel = LocalMemosViewModel.current
    val firstImagePath = noteViewModel.dateFirstImageMap[day.date]

    val backgroundColor = if (day.date == today) {
        SaltTheme.colors.highlight.copy(alpha = 0.1f)
    } else {
        if (isSelected) SaltTheme.colors.subBackground else Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .testTag("MonthDay")
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = backgroundColor)
            .clickable(
                showRipple = true,
                onClick = { onClick(day) },
            ),
    ) {
        if (firstImagePath != null) {
            AsyncImage(
                model = File(firstImagePath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(2.dp))
                    .graphicsLayer(alpha = 0.9f)
            )
        }

        var textColor = when (day.position) {
            DayPosition.MonthDate -> if (firstImagePath != null) Color.White else SaltTheme.colors.text
            DayPosition.InDate, DayPosition.OutDate -> SaltTheme.colors.subText.copy(alpha = 0.5f)
        }

        if (day.date == today && firstImagePath == null) {
            textColor = SaltTheme.colors.highlight
        }

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontWeight = if (day.date == today || isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp,
            style = if (firstImagePath != null) {
                MaterialTheme.typography.bodyMedium.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        blurRadius = 4f
                    )
                )
            } else {
                MaterialTheme.typography.bodyMedium
            }
        )

        if (hasScheme && firstImagePath == null) {
            Canvas(
                modifier = Modifier
                    .size(4.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            ) {
                drawCircle(
                    color = Color.Gray,
                    radius = size.width / 2
                )
            }
        }
    }
}

fun DayOfWeek.displayText(uppercase: Boolean = false): String {
    return getDisplayName(TextStyle.SHORT, Locale.getDefault()).let { value ->
        if (uppercase) value.uppercase(Locale.getDefault()) else value
    }
}


fun YearMonth.displayText(short: Boolean = false): String {
    return "${this.month.displayText(short = short)} ${this.year}"
}

fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, Locale.getDefault())
}

fun Modifier.clickable(
    enabled: Boolean = true,
    showRipple: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = if (showRipple) LocalIndication.current else null,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = onClick,
    )
}
