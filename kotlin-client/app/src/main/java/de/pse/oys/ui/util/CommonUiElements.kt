package de.pse.oys.ui.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.pse.oys.data.facade.Rating
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ViewHeader(
    text: String,
) {
    Text(
        text = text,
        style = typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
    )
}

@Composable
fun ViewHeaderBig(
    text: String,
) {
    Text(
        text = text,
        style = typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
    )
}

@Composable
fun SingleLineInput(
    value: String,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(bottom = 14.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = LightBlue,
            unfocusedContainerColor = LightBlue,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun InputLabel(
    text: String,
) {
    Text(
        text = text,
        style = typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, bottom = 4.dp, top = 4.dp)
    )
}

@Composable
fun NotifyCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 10.dp)
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(end = 10.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = Blue,
                uncheckedColor = LightBlue,
            )
        )
        Text(
            text = text,
        )
    }
}

@Composable
fun DateSelectionRow(label: String, dateText: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = typography.titleLarge, modifier = Modifier.padding(end = 20.dp))
        OutlinedButton(
            onClick = onClick,
            colors = ButtonDefaults.outlinedButtonColors(containerColor = LightBlue),
            modifier = Modifier.padding(end = 20.dp)
        ) {
            Text(dateText)
        }
    }
}

@Composable
fun RatingSlider(
    currentRating: Rating,
    labels: List<String>,
    enabled: Boolean,
    onRatingChange: (Rating) -> Unit,
    activeColor: Color = Blue
) {
    val currentValue = currentRating.ordinal.toFloat()
    val steps = 3
    val maxRange = 4f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Slider(
            value = currentValue,
            onValueChange = { newValue ->
                val index = newValue.roundToInt()
                onRatingChange(Rating.entries[index])
            },
            valueRange = 0f..maxRange,
            steps = steps,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = activeColor.copy(alpha = 0.2f),
                activeTickColor = Color.White,
                inactiveTickColor = activeColor.copy(alpha = 0.4f),
                disabledThumbColor = Color.Gray,
                disabledActiveTrackColor = Color.DarkGray,
                disabledInactiveTrackColor = Color.LightGray,
                disabledActiveTickColor = Color.Transparent,
                disabledInactiveTickColor = Color.Transparent
            )
        )
        Text(
            text = labels.getOrElse(currentRating.ordinal) { "" },
            style = typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) activeColor else Color.DarkGray,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(labels.first(), style = typography.bodySmall, color = Color.Gray)
            Text(labels.last(), style = typography.bodySmall, color = Color.Gray)
        }
    }
}

fun LocalTime.toFormattedString(): String {
    val javaTime = java.time.LocalTime.of(hour, minute)
    val formatter =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault())
    return javaTime.format(formatter)
}

fun LocalDate.toFormattedString(): String {
    val javaDate = java.time.LocalDate.of(year, month.number, day)
    val formatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.getDefault())
    return javaDate.format(formatter)
}

fun LocalDateTime.toFormattedString(): String {
    return "${date.toFormattedString()}, ${time.toFormattedString()}"
}

fun Int.toFormattedTimeString(): String {
    val hours = this / 60
    val minutes = this % 60
    return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
}