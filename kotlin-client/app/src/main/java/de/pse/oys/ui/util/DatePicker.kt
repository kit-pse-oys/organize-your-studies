package de.pse.oys.ui.util

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant


// Hilfsfunktion: Kotlin LocalDate -> Millisekunden für den Picker
fun LocalDate?.toMillis(): Long {
    return this?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
        ?: Clock.System.now().toEpochMilliseconds()
}

// Hilfsfunktion: Millisekunden vom Picker -> Kotlin LocalDate
fun Long?.toLocalDate(): LocalDate? {
    return this?.let {
        Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalDatePickerDialog(
    currentDate: LocalDate?, // Jetzt kotlinx.datetime.LocalDate
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.toMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Erlaubt nur Daten ab heute
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.UTC).date
                    .atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
                return utcTimeMillis >= today
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis.toLocalDate())
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalTimePickerDialog(
    initialTime: LocalTime, // Jetzt kotlinx.datetime.LocalTime
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // Erstellt eine neue kotlinx.datetime.LocalTime
                onTimeSelected(LocalTime(timePickerState.hour, timePickerState.minute))
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
        text = { TimePicker(state = timePickerState) }
    )
}