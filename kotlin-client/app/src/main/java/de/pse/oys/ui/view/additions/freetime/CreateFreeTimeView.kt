package de.pse.oys.ui.view.additions.freetime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
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
            Text(
                "Neue Freizeit",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
            )
            Text(
                "Titel:",
                style = typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 20.dp)
            )
            TextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
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
            Row(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 20.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Datum wählen:",
                    style = typography.titleLarge,
                    modifier = Modifier.padding(end = 20.dp)
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = LightBlue,
                    )
                ) {
                    Text(dateText)
                }
            }
            Text(
                "Zeitraum wählen:",
                style = typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 20.dp)
            )
            Row(
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                ) {
                    Text("Von:")
                    OutlinedButton(
                        onClick = { showStartTimePicker = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = LightBlue,
                        )
                    ) {
                        Text(startTimeText)
                    }
                }
                Text(
                    "-",
                    style = typography.headlineLarge
                )
                Column(
                    modifier = Modifier
                ) {
                    Text("Bis:")
                    OutlinedButton(
                        onClick = { showEndTimePicker = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = LightBlue,
                        )
                    ) {
                        Text(endTimeText)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 20.dp)
                    .toggleable(
                        value = viewModel.weekly,
                        onValueChange = { viewModel.weekly = it },
                        role = Role.Checkbox
                    ), verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewModel.weekly,
                    onCheckedChange = null,
                    modifier = Modifier.padding(end = 10.dp),
                    colors = CheckboxDefaults.colors(
                        checkedColor = Blue,
                        uncheckedColor = LightBlue
                    )
                )
                Text(
                    text = "Freizeit wöchentlich wiederholen",
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
    override var start by mutableStateOf(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    )
    override var end by mutableStateOf(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    )
    override var weekly by mutableStateOf(false)
}