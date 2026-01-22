package de.pse.oys.ui.view.additions.freetime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.ViewModel
import de.pse.oys.ui.util.LocalDatePickerDialog
import de.pse.oys.ui.util.LocalTimePickerDialog
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock


@Composable
fun CreateFreeTimeView(viewModel: ICreateFreeTimeViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val dateText = viewModel.date?.let {
        "${it.day.toString().padStart(2, '0')}.${
            it.month.number.toString().padStart(2, '0')
        }.${it.year}"
    } ?: "Nicht gewählt"
    val startTimeText = "${viewModel.start.hour.toString().padStart(2, '0')}:${
        viewModel.start.minute.toString().padStart(2, '0')
    }"
    val endTimeText = "${viewModel.end.hour.toString().padStart(2, '0')}:${
        viewModel.end.minute.toString().padStart(2, '0')
    }"

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Neue Freizeit")
            TextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                singleLine = true,
                label = { Text("") })
            Text("Datum wählen:")
            OutlinedButton(
                onClick = { showDatePicker = true },
            ) {
                Text(dateText)
            }
            Text("Startzeit wählen:")
            OutlinedButton(
                onClick = { showStartTimePicker = true },
            ) {
                Text(startTimeText)
            }
            Text("Endzeit wählen:")
            OutlinedButton(
                onClick = { showEndTimePicker = true },
            ) {
                Text(endTimeText)
            }
            Row(
                modifier = Modifier.toggleable(
                    value = viewModel.weekly,
                    onValueChange = { viewModel.weekly = it },
                    role = Role.Checkbox
                )
            ) {
                Checkbox(
                    checked = viewModel.weekly,
                    onCheckedChange = null
                )
                Text(
                    text = "Wöchentlich wiederholen",
                )
            }
            if (showDatePicker) {
                LocalDatePickerDialog(
                    currentDate = viewModel.date,
                    onDateSelected = { viewModel.date = it },
                    onDismiss = { showDatePicker = false }
                )
            }
            if (showStartTimePicker) {
                LocalTimePickerDialog(
                    initialTime = viewModel.start,
                    onTimeSelected = { viewModel.start = it },
                    onDismiss = { showStartTimePicker = false }
                )
            }
            if (showEndTimePicker) {
                LocalTimePickerDialog(
                    initialTime = viewModel.end,
                    onTimeSelected = { if (it >= viewModel.start) viewModel.end = it },
                    onDismiss = { showEndTimePicker = false }
                )
            }
        }
    }
}

interface ICreateFreeTimeViewModel {
    val showDelete: Boolean

    var title: String
    var date: LocalDate?
    var start: LocalTime
    var end: LocalTime
    var weekly: Boolean

    fun submit()
    fun delete()
}

abstract class BaseCreateFreeTimeViewModel : ViewModel(), ICreateFreeTimeViewModel {
    override var title by mutableStateOf("")
    override var date by mutableStateOf<LocalDate?>(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
    override var start by mutableStateOf<LocalTime>(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    )
    override var end by mutableStateOf<LocalTime>(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    )
    override var weekly by mutableStateOf(false)
}