package de.pse.oys.ui.view.additions.task

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.RemoteExamTaskData
import de.pse.oys.data.api.RemoteOtherTaskData
import de.pse.oys.data.api.RemoteSubmissionTaskData
import de.pse.oys.data.api.RemoteTask
import de.pse.oys.data.api.RemoteTaskData
import de.pse.oys.data.defaultHandleError
import de.pse.oys.data.facade.ExamTaskData
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.OtherTaskData
import de.pse.oys.data.facade.SubmissionTaskData
import de.pse.oys.data.facade.Task
import de.pse.oys.data.facade.TaskData
import de.pse.oys.ui.navigation.main
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.DateSelectionRow
import de.pse.oys.ui.util.DeleteElementDialog
import de.pse.oys.ui.util.InputLabel
import de.pse.oys.ui.util.LocalDatePickerDialog
import de.pse.oys.ui.util.LocalTimePickerDialog
import de.pse.oys.ui.util.SingleLineInput
import de.pse.oys.ui.util.SubmitButton
import de.pse.oys.ui.util.ViewHeaderWithBackOption
import de.pse.oys.ui.util.toFormattedString
import de.pse.oys.ui.util.toFormattedTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * View for creating a new task or editing an existing one.
 * Different user input fields filled with either default values (when creating) or existing values (when editing).
 * @param viewModel the [ICreateTaskViewModel] for this view.
 */
@Composable
fun CreateTaskView(viewModel: ICreateTaskViewModel) {
    var showExamDatePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val submitButtonActive =
        viewModel.title.isNotBlank() && viewModel.module != stringResource(id = R.string.nothing_chosen)
                && viewModel.weeklyTimeLoad >= 0 && (viewModel.type != TaskType.SUBMISSION || viewModel.submissionCycle in 1..<10)
                && (viewModel.type != TaskType.OTHER || viewModel.start <= viewModel.end)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.error) {
        if (viewModel.error) {
            snackbarHostState.showSnackbar("Something went wrong...")
            viewModel.error = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewHeaderWithBackOption(
                viewModel::navigateBack,
                if (viewModel.showDelete) stringResource(R.string.edit_task) else stringResource(id = R.string.new_task),
                viewModel.showDelete,
                { if (viewModel.showDelete) confirmDelete = true })
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
                    DateSelectionRow(
                        stringResource(id = R.string.select_end_date),
                        viewModel.end.toFormattedString()
                    ) { showEndDatePicker = true }
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

/**
 * Button for selecting a module.
 * Opens a dialog that lets the user choose between their existing modules.
 * @param viewModel the [ICreateTaskViewModel] for this view.
 */
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

/**
 * Button for selecting the weekly time load.
 * Opens a dialog that lets the user choose a time.
 * @param viewModel the [ICreateTaskViewModel] for this view.
 */
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

/**
 * Button for selecting the submission date.
 * Opens a dialog that lets the user choose a date, then another for the time.
 * @param viewModel the [ICreateTaskViewModel] for this view.
 */
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

/**
 * Field that lets the user put in the submission cycle.
 * @param viewModel the [ICreateTaskViewModel] for this view.
 */
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

/**
 * Chips for selecting the task type.
 * @param current the current task type.
 * @param onSelect the function to be called when a task type is selected.
 */
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

/**
 * Chip for selecting a task type.
 * @param taskType the [TaskType] to be displayed.
 * @param current the current [TaskType].
 * @param label the label to be displayed on the chip.
 * @param onSelect the function to be called when the chip is clicked.
 */
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
            selectedLabelColor = Color.DarkGray,
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

/**
 * Type of task.
 */
enum class TaskType {
    EXAM,
    SUBMISSION,
    OTHER,
}

/**
 * Interface for the view model of [CreateTaskView].
 */
interface ICreateTaskViewModel {
    var error: Boolean
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

    /**
     * Submits the form data to the server and navigates to the main screen.
     */
    fun submit()

    /**
     * Deletes the task from the server and navigates to the main screen.
     */
    fun delete()

    /**
     * Navigates back to the previous screen.
     */
    fun navigateBack()
}

/**
 * Base view model for [CreateTaskView] with default values.
 * @param model the [ModelFacade] for the app.
 * @param navController the [NavController] for navigation.
 * @param task the [TaskData] to be used instead of default values.
 */
abstract class BaseCreateTaskViewModel(
    private val model: ModelFacade,
    protected val navController: NavController,
    task: TaskData? = null
) : ViewModel(), ICreateTaskViewModel {
    init {
        require(model.modules != null)
    }

    override var error: Boolean by mutableStateOf(false)
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

    /**
     * Registers or deletes a task in the [ModelFacade].
     * @param id The uuid of the task
     * @param task the [Task] to be registered or null to delete.
     */
    protected fun registerNewTask(id: Uuid, task: TaskData?) {
        val tasks = model.tasks.orEmpty().toMutableMap()
        model.tasks = tasks
        model.steps = null
        if (task != null) {
            tasks[id] = task
        } else {
            tasks.remove(id)
        }

        navController.main()
    }

    protected fun assembleRemoteTask(): RemoteTaskData {
        val module = model.modules!!.entries.find { it.value.title == module }!!.key
        return when (type) {
            TaskType.EXAM -> RemoteExamTaskData(title, module, weeklyTimeLoad, examDate)
            TaskType.SUBMISSION -> RemoteSubmissionTaskData(
                title,
                module,
                weeklyTimeLoad,
                submissionDate,
                end.atTime(submissionDate.time),
                submissionCycle
            )

            TaskType.OTHER -> RemoteOtherTaskData(
                title,
                module,
                weeklyTimeLoad,
                start.atTime(0, 0),
                end.atTime(23, 59, 59)
            )
        }
    }

    protected fun assembleTask(): TaskData {
        val module = model.modules!!.entries.find { it.value.title == module }!!
            .let { Module(it.value, it.key) }
        return when (type) {
            TaskType.EXAM -> ExamTaskData(title, module, weeklyTimeLoad, examDate)
            TaskType.SUBMISSION -> SubmissionTaskData(
                title,
                module,
                weeklyTimeLoad,
                submissionDate,
                end,
                submissionCycle
            )

            TaskType.OTHER -> OtherTaskData(title, module, weeklyTimeLoad, start, end)
        }
    }
}

/**
 * View model for creating a new task.
 * @param api the [RemoteAPI] for the app.
 * @param model the [ModelFacade] for the app.
 * @param navController the [NavController] for navigation.
 */
class CreateTaskViewModel(
    private val api: RemoteAPI,
    model: ModelFacade,
    navController: NavController
) : BaseCreateTaskViewModel(model, navController) {
    override val showDelete = false

    override fun submit() {
        viewModelScope.launch {
            val data = assembleRemoteTask()
            api.createTask(data).defaultHandleError(navController) { error = true }?.let { id ->
                withContext(Dispatchers.Main.immediate) {
                    registerNewTask(id, assembleTask())
                }
            }
        }
    }

    override fun delete() {
        error("Can't delete Task before creating it")
    }

    override fun navigateBack() {
        navController.popBackStack()
    }
}

/**
 * View model for editing an existing task.
 * @param api the [RemoteAPI] for the app.
 * @param model the [ModelFacade] for the app.
 * @param target the [Task] to be edited.
 * @param navController the [NavController] for navigation.
 */
class EditTaskViewModel(
    private val api: RemoteAPI,
    model: ModelFacade,
    target: Task,
    navController: NavController
) : BaseCreateTaskViewModel(model, navController, target.data) {
    private val uuid = target.id

    override val showDelete = true

    override fun submit() {
        viewModelScope.launch {
            val data = assembleRemoteTask()
            val task = RemoteTask(data, uuid)
            api.updateTask(task).defaultHandleError(navController) { error = true }?.let {
                withContext(Dispatchers.Main.immediate) {
                    registerNewTask(uuid, assembleTask())
                }
            }
        }
    }

    override fun delete() {
        viewModelScope.launch {
            api.deleteTask(uuid).defaultHandleError(navController) { error = true }?.let {
                withContext(Dispatchers.Main.immediate) {
                    registerNewTask(uuid, null)
                }
            }
        }
    }

    override fun navigateBack() {
        navController.popBackStack()
    }
}