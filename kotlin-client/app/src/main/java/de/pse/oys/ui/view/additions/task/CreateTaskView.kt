package de.pse.oys.ui.view.additions.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.ExamTaskData
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.OtherTaskData
import de.pse.oys.data.facade.SubmissionTaskData
import de.pse.oys.data.facade.Task
import de.pse.oys.data.facade.TaskData
import de.pse.oys.ui.navigation.main
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.DateSelectionRow
import de.pse.oys.ui.util.DeleteButton
import de.pse.oys.ui.util.DeleteElementDialog
import de.pse.oys.ui.util.InputLabel
import de.pse.oys.ui.util.LocalDatePickerDialog
import de.pse.oys.ui.util.LocalTimePickerDialog
import de.pse.oys.ui.util.NotifyCheckbox
import de.pse.oys.ui.util.SingleLineInput
import de.pse.oys.ui.util.SubmitButton
import de.pse.oys.ui.util.ViewHeaderBig
import de.pse.oys.ui.util.toFormattedString
import de.pse.oys.ui.util.toFormattedTimeString
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock


@Composable
fun CreateTaskView(viewModel: ICreateTaskViewModel) {
    var showExamDatePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val submitButtonActive =
        viewModel.title.isNotBlank() && viewModel.module != stringResource(id = R.string.nothing_chosen)
                && viewModel.weeklyTimeLoad >= 0 && (if (viewModel.type == TaskType.SUBMISSION) viewModel.submissionCycle in 1..<10 else false)
                && (if (viewModel.type == TaskType.OTHER) viewModel.start <= viewModel.end else false)
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (viewModel.showDelete) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                    DeleteButton { confirmDelete = true }
                }
            }
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ViewHeaderBig(
                    if (viewModel.showDelete) stringResource(R.string.edit_task) else stringResource(
                        id = R.string.new_task
                    )
                )
                InputLabel(text = stringResource(id = R.string.enter_title))
                SingleLineInput(viewModel.title) { viewModel.title = it }
                ModuleSelection(viewModel)
                TimeLoadSelection(viewModel)
                InputLabel(text = stringResource(id = R.string.select_task_type))
                TaskTypeChips(
                    current = viewModel.type,
                    onSelect = { viewModel.type = it }
                )

                when (viewModel.type) {
                    TaskType.EXAM -> {
                        DateSelectionRow(
                            stringResource(id = R.string.enter_exam_date),
                            viewModel.examDate.toFormattedString()
                        ) { showExamDatePicker = true }
                    }

                    TaskType.SUBMISSION -> {
                        InputLabel(text = stringResource(id = R.string.enter_submission_date))
                        SubmissionDateSelection(viewModel)
                        SubmissionCycleSelection(viewModel)
                    }

                    TaskType.OTHER -> {
                        InputLabel(text = stringResource(id = R.string.select_time_period))
                        DateSelectionRow(
                            stringResource(id = R.string.select_start_date),
                            viewModel.start.toFormattedString()
                        ) {
                            showStartDatePicker = true
                        }
                        DateSelectionRow(
                            stringResource(id = R.string.select_end_date),
                            viewModel.end.toFormattedString()
                        ) { showEndDatePicker = true }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                SubmitButton(
                    if (viewModel.showDelete) stringResource(R.string.save_changes_button) else stringResource(
                        id = R.string.add_task_button
                    ),
                    submitButtonActive
                ) { viewModel.submit() }

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

                if (confirmDelete) {
                    DeleteElementDialog(
                        onDismiss = { confirmDelete = false },
                        onConfirm = viewModel::delete
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleSelection(viewModel: ICreateTaskViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .padding(start = 20.dp, bottom = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(id = R.string.module),
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
            Text(text = viewModel.module.ifEmpty { stringResource(id = R.string.nothing_chosen) })
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(id = R.string.select_module)) },
            text = {
                Column {
                    viewModel.availableModules.forEach { module ->
                        TextButton(
                            onClick = {
                                viewModel.module = module
                                showDialog = false
                            },
                        ) {
                            Text(module, textAlign = TextAlign.Left)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(id = R.string.confirm_cancel))
                }
            }
        )
    }
}

@Composable
private fun TimeLoadSelection(viewModel: ICreateTaskViewModel) {
    var showTimeLoadPicker by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .padding(start = 20.dp, bottom = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(id = R.string.enter_weekly_time_load),
            style = typography.titleLarge,
            modifier = Modifier.padding(end = 20.dp)
        )
        OutlinedButton(
            onClick = { showTimeLoadPicker = true },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = LightBlue,
            )
        ) {
            Text(text = viewModel.weeklyTimeLoad.toFormattedTimeString())
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
}

@Composable
private fun SubmissionDateSelection(viewModel: ICreateTaskViewModel) {
    var showSubmissionDatePicker by remember { mutableStateOf(false) }
    var showSubmissionTimePicker by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = { showSubmissionDatePicker = true },
        modifier = Modifier.padding(bottom = 10.dp, top = 10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = LightBlue,
        )
    ) {
        Text(text = viewModel.submissionDate.toFormattedString())
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

@Composable
private fun SubmissionCycleSelection(viewModel: ICreateTaskViewModel) {
    Row(
        modifier = Modifier
            .padding(start = 20.dp, bottom = 10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(id = R.string.enter_submission_cycle),
            style = typography.titleLarge
        )
        TextField(
            value = viewModel.submissionCycle.toString(),
            onValueChange = { viewModel.submissionCycle = it.toIntOrNull() ?: 1 },
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

@Composable
private fun TaskTypeChips(current: TaskType, onSelect: (TaskType) -> Unit) {
    Row(
        modifier = Modifier.padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(26.dp)
    ) {
        TaskTypeChip(
            taskType = TaskType.EXAM,
            current = current,
            label = stringResource(id = R.string.examTask),
            onSelect = onSelect
        )
        TaskTypeChip(
            taskType = TaskType.SUBMISSION,
            current = current,
            label = stringResource(id = R.string.submissionTask),
            onSelect = onSelect
        )
        TaskTypeChip(
            taskType = TaskType.OTHER,
            current = current,
            label = stringResource(id = R.string.otherTask),
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

    var examDate: LocalDate

    var submissionDate: LocalDateTime
    var submissionCycle: Int

    var start: LocalDate
    var end: LocalDate

    fun submit()
    fun delete()
}

abstract class BaseCreateTaskViewModel(
    private val model: ModelFacade,
    private val navController: NavController,
    task: TaskData? = null
) : ViewModel(), ICreateTaskViewModel {
    init {
        require(model.modules != null)
    }

    override val availableModules: List<String> = model.modules!!.map { it.value.title }

    override var title by mutableStateOf(task?.title ?: "")
    override var module by mutableStateOf(task?.module?.data?.title ?: "")
    override var type by mutableStateOf(
        when (task) {
            is ExamTaskData -> TaskType.EXAM
            is SubmissionTaskData -> TaskType.SUBMISSION
            is OtherTaskData -> TaskType.OTHER
            null -> TaskType.EXAM
        }
    )
    override var weeklyTimeLoad by mutableIntStateOf(task?.weeklyTimeLoad ?: 0)

    override var examDate by mutableStateOf(
        (task as? ExamTaskData?)?.examDate ?: Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
    override var submissionDate by mutableStateOf(
        (task as? SubmissionTaskData?)?.firstDate ?: Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
    )
    override var submissionCycle by mutableIntStateOf((task as? SubmissionTaskData?)?.cycle ?: 1)

    override var start by mutableStateOf(
        (task as? OtherTaskData?)?.start ?: Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
    override var end by mutableStateOf(
        (task as? OtherTaskData?)?.end ?: Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
    )

    protected fun registerNewTask(task: Task) {
        val tasks = model.tasks.orEmpty().toMutableMap()
        model.tasks = tasks
        tasks[task.id] = task.data

        navController.main()
    }
}

class CreateTaskViewModel(
    private val api: RemoteAPI,
    model: ModelFacade,
    navController: NavController
) : BaseCreateTaskViewModel(model, navController) {
    override val showDelete = false

    override fun submit() {
        TODO("Not yet implemented")
    }

    override fun delete() {
        error("Can't delete Task before creating it")
    }
}

class EditTaskViewModel(
    private val api: RemoteAPI,
    model: ModelFacade,
    target: Task,
    navController: NavController
) : BaseCreateTaskViewModel(model, navController, target.data) {
    override val showDelete = true

    override fun submit() {
        TODO("Not yet implemented")
    }

    override fun delete() {
        TODO("Not yet implemented")
    }
}