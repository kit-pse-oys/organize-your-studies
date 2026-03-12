package de.pse.oys.ui.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.pse.oys.R
import de.pse.oys.ui.theme.Typography
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaDayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarWeek(
    modifier: Modifier = Modifier,
    events: Map<DayOfWeek, List<CalendarEvent>> = mapOf(),
    hourStart: Int = 6,
    hourEnd: Int = 22,
    hourHeight: Dp = 48.dp
) {
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    val dayWidth = 120.dp
    val hourColumnWidth = 48.dp
    val topPadding = 12.dp
    val totalHeight = hourHeight * (hourEnd - hourStart) + topPadding

    CompositionLocalProvider(LocalContentColor provides Color.Black) {
        Column(modifier = modifier) {
            Text(
                text = stringResource(R.string.week_plan_header),
                style = Typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(16.dp)
            )
            Row(
                Modifier
                    .fillMaxWidth()
            ) {
                Spacer(Modifier.width(hourColumnWidth))
                Row(
                    Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScroll)
                ) {
                    for (day in DayOfWeek.entries) {
                        Text(
                            modifier = Modifier.width(dayWidth),
                            text = day.toJavaDayOfWeek()
                                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()),
                            style = Typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(verticalScroll)
                    .padding(top = topPadding)
            ) {
                HourColumn(
                    modifier = Modifier
                        .width(hourColumnWidth)
                        .height(totalHeight),
                    hourStart = hourStart,
                    hourEnd = hourEnd,
                    hourHeight = hourHeight
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(totalHeight)
                ) {
                    HourLines(
                        modifier = Modifier.matchParentSize(),
                        hourStart = hourStart,
                        hourEnd = hourEnd,
                        hourHeight = hourHeight
                    )
                    Row(
                        Modifier
                            .matchParentSize()
                            .horizontalScroll(horizontalScroll)
                    ) {
                        for (day in DayOfWeek.entries) {
                            val dayEvents = events[day] ?: listOf()
                            DayColumn(
                                modifier = Modifier.width(dayWidth),
                                events = dayEvents,
                                hourStart = hourStart,
                                hourHeight = hourHeight
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    modifier: Modifier = Modifier,
    events: List<CalendarEvent> = listOf(),
    hourStart: Int = 6,
    hourEnd: Int = 22,
    hourHeight: Dp = 48.dp
) {
    val scroll = rememberScrollState()

    val topPadding = 12.dp
    val totalHeight = hourHeight * (hourEnd - hourStart) + topPadding

    CompositionLocalProvider(LocalContentColor provides Color.Black) {
        Column(modifier = modifier) {
            Text(
                text = stringResource(R.string.day_plan_header),
                style = Typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scroll)
                    .padding(top = topPadding)
            ) {
                HourColumn(
                    modifier = Modifier
                        .width(48.dp)
                        .height(totalHeight),
                    hourStart = hourStart,
                    hourEnd = hourEnd,
                    hourHeight = hourHeight
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(totalHeight)
                ) {
                    HourLines(
                        modifier = Modifier.matchParentSize(),
                        hourStart = hourStart,
                        hourEnd = hourEnd,
                        hourHeight = hourHeight
                    )
                    DayColumn(
                        modifier = Modifier.matchParentSize(),
                        events = events,
                        hourStart = hourStart,
                        hourHeight = hourHeight
                    )
                }
            }
        }
    }
}

@Composable
private fun DayColumn(
    modifier: Modifier = Modifier,
    events: List<CalendarEvent>,
    hourStart: Int,
    hourHeight: Dp
) {
    Box(modifier) {
        val minuteHeight = hourHeight / 60

        events.forEach { event ->
            val startMinutes = (event.start.hour - hourStart) * 60 + event.start.minute
            val durationMinutes = (event.end.hour * 60 + event.end.minute) -
                    (event.start.hour * 60 + event.start.minute)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = minuteHeight * startMinutes)
                    .height(minuteHeight * durationMinutes)
                    .padding(end = 8.dp, bottom = 2.dp)
            ) {
                event.Draw(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun HourColumn(
    modifier: Modifier = Modifier,
    hourStart: Int,
    hourEnd: Int,
    hourHeight: Dp
) {
    Box(modifier) {
        for (i in hourStart..hourEnd) {
            val text = if (i < 10) "0$i:00" else "$i:00"
            val offsetY = hourHeight * (i - hourStart)

            Text(
                text = text,
                style = Typography.labelSmall,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY - 8.dp)
                    .padding(end = 8.dp)
            )
        }
    }
}

@Composable
private fun HourLines(
    modifier: Modifier = Modifier,
    hourStart: Int,
    hourEnd: Int,
    hourHeight: Dp
) {
    Canvas(modifier) {
        for (i in hourStart..hourEnd) {
            val y = (i - hourStart) * hourHeight.toPx()
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }
    }
}

interface CalendarEvent {
    val start: LocalTime
    val end: LocalTime

    @Composable
    fun Draw(modifier: Modifier = Modifier)
}
