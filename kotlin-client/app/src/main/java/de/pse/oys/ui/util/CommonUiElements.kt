package de.pse.oys.ui.util

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.pse.oys.R
import de.pse.oys.data.facade.Rating
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.theme.MediumBlue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Creates basic header for views.
 * @param text the header to be displayed.
 */
@Composable
fun ViewHeader(
    text: String,
) {
    Text(
        text = text,
        style = typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
    )
}

/**
 * Creates a back button in the form of an arrow.
 * @param onClick the function to be called when the button is clicked.
 */
@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(12.dp)
            .size(56.dp),
        shape = CircleShape
    ) {
        Icon(
            painter = painterResource(id = R.drawable.outline_arrow_back_24),
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = Blue
        )
    }
}

@Composable
fun ViewHeaderWithBackOption(
    onBack: () -> Unit,
    text: String,
    showDelete: Boolean = false,
    onDelete: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        BackButton {
            onBack()
        }
        Box(modifier = Modifier.align(Alignment.Center)) {
            ViewHeader(text)
        }
        if (showDelete) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                DeleteButton {
                    onDelete()
                }
            }
        }
    }
}

/**
 * Creates a single line input field with a maxLength character limit.
 * @param value the current value of the input field.
 * @param onValueChange the function to be called when the value changes.
 */
@Composable
fun SingleLineInput(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val maxLength = 35
    val tooLong = value.length > maxLength
    TextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        isError = tooLong,
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
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.DarkGray
        ),
        singleLine = true
    )
}

/**
 * Creates a label to be put over/ next to input fields.
 * @param text the label to be displayed.
 */
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

/**
 * Creates a simple button.
 * @param label the label to be displayed.
 * @param onClick the function to be called when the button is clicked.
 */
@Composable
fun SimpleMenuAndAdditionsButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 20.dp)
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, Blue),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = LightBlue)
    ) {
        Text(
            text = label,
            style = typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Blue
        )
    }
}

/**
 * Creates a checkbox with clickable text.
 * @param text the text to be displayed next to the checkbox.
 * @param checked the current state of the checkbox.
 * @param onCheckedChange the function to be called when the checkbox is clicked.
 */
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

/**
 * Creates a button for date selection.
 * @param label the label to be displayed next to the button.
 * @param dateText the current date to be displayed.
 * @param onClick the function to be called when the button is clicked.
 */
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

/**
 * Creates a slider for [Rating] with 5 options.
 * @param currentRating the current [Rating] to be displayed.
 * @param labels the labels to be displayed for the slider options.
 * @param enabled whether the slider is enabled or not.
 * @param onRatingChange the function to be called when the slider is changed.
 * @param activeColor the color of the slider when it is active.
 */
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

/**
 * Creates a button with gradient for submitting or saving things.
 * @param label the label to be displayed on the button.
 * @param enabled whether the button is enabled or not.
 * @param onClick the function to be called when the button is clicked.
 */
@Composable
fun SubmitButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    val gradientColors = if (enabled) {
        listOf(Blue, MediumBlue)
    } else {
        listOf(Color.Gray, Color.LightGray)
    }
    val gradient = Brush.linearGradient(colors = gradientColors)
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .padding(vertical = 20.dp)
            .background(gradient, shape = shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 12.dp),
            shape = shape,
            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
        ) {
            Text(
                text = label,
                style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

/**
 * Creates a delete button in the form of a trashcan icon.
 * @param onClick the function to be called when the icon is clicked.
 */
@Composable
fun DeleteButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(12.dp)
            .size(56.dp),
        shape = CircleShape
    ) {
        Icon(
            painter = painterResource(id = R.drawable.outline_delete_24),
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = Blue
        )
    }
}

/**
 * Dialog that confirms whether an element should be deleted.
 * @param onDismiss the function to be called when the dialog is dismissed.
 * @param onConfirm the function to be called when the dialog is confirmed
 */
@Composable
fun DeleteElementDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Blue)
            ) { Text(stringResource(R.string.confirm_yes)) }
        }, dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Blue)
            ) { Text(stringResource(R.string.confirm_no)) }
        }, title = {
            Text(stringResource(R.string.delete_element_confirm_header))
        }, text = {
            Text(stringResource(R.string.delete_element_confirm_body))
        })
}

/**
 * Formats a date to a readable string.
 * @return the formatted date.
 */
fun LocalDate.toFormattedString(): String {
    val formatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())

    return this.toJavaLocalDate().format(formatter)
}

/**
 * Formats a time to a readable string.
 * @return the formatted time.
 */
fun LocalTime.toFormattedString(): String {
    val formatter = DateTimeFormatter
        .ofLocalizedTime(FormatStyle.SHORT)
        .withLocale(Locale.getDefault())

    return this.toJavaLocalTime().format(formatter)
}

/**
 * Formats a date and time to a readable string.
 * @return the formatted date and time.
 */
fun LocalDateTime.toFormattedString(): String {
    return "${date.toFormattedString()}, ${time.toFormattedString()}"
}

/**
 * Formats an Int to a HH:MM time string.
 * @return the formatted time.
 */
fun Int.toFormattedTimeString(): String {
    val hours = this / 60
    val minutes = this % 60
    return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
}