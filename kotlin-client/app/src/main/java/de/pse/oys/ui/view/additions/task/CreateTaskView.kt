package de.pse.oys.ui.view.additions.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.LocalDatePickerDialog
import de.pse.oys.ui.util.LocalTimePickerDialog
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock


@Composable
fun CreateTaskView(viewModel: ICreateTaskViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedModule by remember { mutableStateOf("Kein Modul gewählt") }
    var showExamDatePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showTimeLoadPicker by remember { mutableStateOf(false) }
    var showSubmissionDatePicker by remember { mutableStateOf(false) }
    var showSubmissionTimePicker by remember { mutableStateOf(false) }

    val sub = viewModel.submissionDate
    val submissionText = "${sub.day.toString().padStart(2, '0')}.${
        sub.month.number.toString().padStart(2, '0')
    }.${sub.year}, " +
            "${sub.hour.toString().padStart(2, '0')}:${sub.minute.toString().padStart(2, '0')} Uhr"
    val timeLoadDisplay = "${
        (viewModel.weeklyTimeLoad / 60).toString().padStart(2, '0')
    }:${(viewModel.weeklyTimeLoad % 60).toString().padStart(2, '0')} h"


    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Neue Aufgabe",
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
                    "Modul:",
                    style = typography.titleLarge,
                    modifier = Modifier.padding(end = 20.dp)
                )
                OutlinedButton(
                    onClick = { showDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = LightBlue,
                    )
                ) {
                    Text(text = selectedModule)
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 20.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Zeitaufwand pro Woche:",
                    style = typography.titleLarge,
                    modifier = Modifier.padding(end = 20.dp)
                )
                OutlinedButton(
                    onClick = { showTimeLoadPicker = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = LightBlue,
                    )
                ) {
                    Text(text = timeLoadDisplay)
                }
            }
            if (showTimeLoadPicker) {
                LocalTimePickerDialog(
                    initialTime = LocalTime(
                        hour = (viewModel.weeklyTimeLoad / 60).coerceIn(0, 23),
                        minute = (viewModel.weeklyTimeLoad % 60).coerceIn(0, 59)
                    ),
                    onTimeSelected = { selectedTime ->
                        viewModel.weeklyTimeLoad = (selectedTime.hour * 60) + selectedTime.minute
                        showTimeLoadPicker = false
                    },
                    onDismiss = { showTimeLoadPicker = false }
                )
            }
            Text(
                "Aufgabentyp wählen:",
                style = typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 20.dp, bottom = 8.dp)
            )
            TaskTypeChips(
                current = viewModel.type,
                onSelect = { viewModel.type = it }
            )

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Modul auswählen") },
                    text = {
                        Column {
                            viewModel.availableModules.forEach { title ->
                                TextButton(
                                    onClick = {
                                        selectedModule = title
                                        showDialog = false
                                    },
                                ) {
                                    Text(title, textAlign = TextAlign.Left)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Abbrechen")
                        }
                    }
                )
            }

            if (viewModel.type == TaskType.EXAM) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 20.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Klausurdatum:",
                        style = typography.titleLarge,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                    OutlinedButton(
                        onClick = { showExamDatePicker = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = LightBlue,
                        )
                    ) {
                        val d = viewModel.examDate
                        Text(
                            "${d.day.toString().padStart(2, '0')}.${
                                d.month.number.toString().padStart(2, '0')
                            }.${d.year}"
                        )
                    }
                }
            }
            if (viewModel.type == TaskType.SUBMISSION) {
                Text(
                    "erster Abgabetermin:",
                    style = typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 20.dp)
                )
                OutlinedButton(
                    onClick = { showSubmissionDatePicker = true },
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = LightBlue,
                    )
                ) {
                    Text(text = submissionText)
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 20.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Wochenzyklus eingeben:",
                        style = typography.titleLarge
                    )
                    TextField(
                        value = viewModel.submissionCycle.toString(),
                        onValueChange = { viewModel.submissionCycle = it.toIntOrNull() ?: 0 },
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp),
                        textStyle = TextStyle(textAlign = TextAlign.Center),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = LightBlue,
                            unfocusedContainerColor = LightBlue,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }
            if (viewModel.type == TaskType.OTHER) {
                Text(
                    "Aufgabezeitraum festlegen:",
                    style = typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 20.dp)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 20.dp, bottom = 10.dp, top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Startdatum:",
                        style = typography.titleMedium,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = LightBlue,
                        )
                    ) {
                        val d = viewModel.start
                        Text(
                            "${d.day.toString().padStart(2, '0')}.${
                                d.month.number.toString().padStart(2, '0')
                            }.${d.year}"
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 20.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Enddatum:",
                        style = typography.titleMedium,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = LightBlue,
                        )
                    ) {
                        val d = viewModel.start
                        Text(
                            "${d.day.toString().padStart(2, '0')}.${
                                d.month.number.toString().padStart(2, '0')
                            }.${d.year}"
                        )
                    }
                }
            }
            if (viewModel.type == TaskType.EXAM || viewModel.type == TaskType.SUBMISSION) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 20.dp)
                        .toggleable(
                            value = viewModel.sendNotification,
                            onValueChange = { viewModel.sendNotification = it },
                            role = Role.Checkbox
                        ), verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = viewModel.sendNotification,
                        onCheckedChange = null,
                        modifier = Modifier.padding(end = 10.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Blue,
                            uncheckedColor = LightBlue
                        )
                    )
                    Text(
                        text = "Ich will benachrichtigt werden.",
                    )
                }
            }

            if (showExamDatePicker) {
                LocalDatePickerDialog(
                    currentDate = viewModel.examDate,
                    onDateSelected = { selectedDate ->
                        if (selectedDate != null) {
                            viewModel.examDate = selectedDate
                        }
                        showExamDatePicker = false
                    },
                    onDismiss = { showExamDatePicker = false }
                )
            }

            if (showStartDatePicker) {
                LocalDatePickerDialog(
                    currentDate = viewModel.start,
                    onDateSelected = { selectedDate ->
                        if (selectedDate != null) {
                            viewModel.start = selectedDate
                        }
                        showStartDatePicker = false
                    },
                    onDismiss = { showStartDatePicker = false }
                )
            }

            if (showEndDatePicker) {
                LocalDatePickerDialog(
                    currentDate = viewModel.end,
                    onDateSelected = { selectedDate ->
                        if (selectedDate != null && selectedDate >= viewModel.start) {
                            viewModel.end = selectedDate
                        }
                        showEndDatePicker = false
                    },
                    onDismiss = { showEndDatePicker = false }
                )
            }

            if (showSubmissionDatePicker) {
                LocalDatePickerDialog(
                    currentDate = viewModel.submissionDate.date,
                    onDateSelected = { selectedDate ->
                        if (selectedDate != null) {
                            viewModel.submissionDate = LocalDateTime(
                                selectedDate.year,
                                selectedDate.month.number, selectedDate.day,
                                viewModel.submissionDate.hour, viewModel.submissionDate.minute
                            )
                            showSubmissionDatePicker = false
                            showSubmissionTimePicker = true
                        }
                    },
                    onDismiss = { showSubmissionDatePicker = false }
                )
            }

            if (showSubmissionTimePicker) {
                LocalTimePickerDialog(
                    initialTime = viewModel.submissionDate.time,
                    onTimeSelected = { selectedTime ->
                        val d = viewModel.submissionDate.date
                        viewModel.submissionDate = LocalDateTime(
                            d.year, d.month.number, d.day,
                            selectedTime.hour, selectedTime.minute
                        )
                        showSubmissionTimePicker = false
                    },
                    onDismiss = { showSubmissionTimePicker = false }
                )
            }
        }
    }
}


@Composable
private fun TaskTypeChips(current: TaskType, onSelect: (TaskType) -> Unit) {
    Row(
        modifier = Modifier.padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        TaskTypeChip(
            taskType = TaskType.EXAM,
            current = current,
            label = "Klausur",
            onSelect = onSelect
        )
        TaskTypeChip(
            taskType = TaskType.SUBMISSION,
            current = current,
            label = "Abgabe",
            onSelect = onSelect
        )
        TaskTypeChip(
            taskType = TaskType.OTHER,
            current = current,
            label = "Sonstige",
            onSelect = onSelect
        )
    }
}

@Composable
private fun TaskTypeChip(
    taskType: TaskType,
    current: TaskType,
    label: String,
    onSelect: (TaskType) -> Unit
) {
    FilterChip(
        selected = taskType == current,
        onClick = { onSelect(taskType) },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = LightBlue,
            containerColor = Color.Transparent,
            labelColor = Color.DarkGray,
            selectedLeadingIconColor = Color.Blue,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = taskType == current,
            borderColor = LightBlue,
            selectedBorderColor = Blue,
            borderWidth = 1.2.dp,
            selectedBorderWidth = 1.2.dp
        )
    )
}

enum class TaskType {
    EXAM,
    SUBMISSION,
    OTHER,
}

interface ICreateTaskViewModel {
    val availableModules: List<String>
    val showDelete: Boolean

    var title: String
    var module: String
    var type: TaskType
    var weeklyTimeLoad: Int
    var sendNotification: Boolean

    var examDate: LocalDate

    var submissionDate: LocalDateTime
    var submissionCycle: Int

    var start: LocalDate
    var end: LocalDate

    fun submit()
    fun delete()
}

abstract class BaseCreateTaskViewModel : ViewModel(), ICreateTaskViewModel {
    override val availableModules by mutableStateOf<List<String>>(listOf())
    override var title by mutableStateOf("")
    override var module by mutableStateOf("")
    override var type by mutableStateOf(TaskType.OTHER)
    override var weeklyTimeLoad by mutableIntStateOf(0)
    override var sendNotification by mutableStateOf(false)
    override var examDate by mutableStateOf(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
    override var submissionDate by mutableStateOf(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    )
    override var submissionCycle by mutableIntStateOf(0)
    override var start by mutableStateOf(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
    override var end by mutableStateOf(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
}