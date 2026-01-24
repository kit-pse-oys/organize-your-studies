package de.pse.oys.ui.view.additions.freetime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.DateSelectionRow
import de.pse.oys.ui.util.InputLabel
import de.pse.oys.ui.util.LocalDatePickerDialog
import de.pse.oys.ui.util.LocalTimePickerDialog
import de.pse.oys.ui.util.NotifyCheckbox
import de.pse.oys.ui.util.SingleLineInput
import de.pse.oys.ui.util.ViewHeaderBig
import de.pse.oys.ui.util.toFormattedString
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock


@Composable
fun CreateFreeTimeView(viewModel: ICreateFreeTimeViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    val dateText = viewModel.date?.toFormattedString() ?: "Nicht gewählt"

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewHeaderBig(text = "Neue Freizeit")
            InputLabel(text = "Titel:")
            SingleLineInput(viewModel.title) { viewModel.title = it }
            DateSelectionRow("Datum wählen:", dateText) { showDatePicker = true }
            InputLabel(text = "Zeitraum wählen:")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimePickerButton(label = "Von:", time = viewModel.start) {
                    showStartTimePicker = true
                }
                Text("-", style = typography.headlineLarge)
                TimePickerButton(label = "Bis:", time = viewModel.end) { showEndTimePicker = true }
            }
            NotifyCheckbox(
                "Freizeit wöchentlich wiederholen",
                viewModel.weekly
            ) { viewModel.weekly = it }

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

@Composable
private fun TimePickerButton(label: String, time: LocalTime, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = typography.bodyMedium)
        OutlinedButton(
            onClick = onClick,
            colors = ButtonDefaults.outlinedButtonColors(containerColor = LightBlue)
        ) {
            Text(time.toFormattedString())
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
    override var start by mutableStateOf(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    )
    override var end by mutableStateOf(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    )
    override var weekly by mutableStateOf(false)
}