package de.pse.oys.ui.view.additions.freetime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.FreeTime
import de.pse.oys.data.facade.FreeTimeData
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.main
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
    var confirmDelete by remember { mutableStateOf(false) }
    val dateText = viewModel.date.toFormattedString()
    val submitButtonActive = viewModel.title.isNotBlank() && viewModel.start <= viewModel.end

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
                    if (viewModel.showDelete) stringResource(R.string.edit_freetime) else stringResource(
                        id = R.string.new_freetime
                    )
                )
                InputLabel(text = stringResource(id = R.string.enter_title))
                SingleLineInput(viewModel.title) { viewModel.title = it }
                DateSelectionRow(stringResource(id = R.string.select_Date), dateText) {
                    showDatePicker = true
                }
                InputLabel(text = stringResource(id = R.string.select_time_period))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimePickerButton(
                        label = stringResource(id = R.string.select_start_time_from),
                        time = viewModel.start
                    ) {
                        showStartTimePicker = true
                    }
                    Text("-", style = typography.headlineLarge)
                    TimePickerButton(
                        label = stringResource(id = R.string.select_end_time_to),
                        time = viewModel.end
                    ) { showEndTimePicker = true }
                }
                NotifyCheckbox(
                    stringResource(id = R.string.repeat_freetime_weekly),
                    viewModel.weekly
                ) { viewModel.weekly = it }

                Spacer(modifier = Modifier.weight(1f))
                SubmitButton(
                    if (viewModel.showDelete) stringResource(R.string.save_changes_button) else stringResource(
                        id = R.string.add_freetime_button
                    ),
                    submitButtonActive
                ) { viewModel.submit() }

                if (showDatePicker) {
                    LocalDatePickerDialog(
                        currentDate = viewModel.date,
                        onDateSelected = {
                            if (it != null) {
                                viewModel.date = it
                            }
                        },
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
    var date: LocalDate
    var start: LocalTime
    var end: LocalTime
    var weekly: Boolean

    fun submit()
    fun delete()
}

abstract class BaseCreateFreeTimeViewModel(
    private val model: ModelFacade,
    private val navController: NavController,
    freeTime: FreeTimeData? = null
) : ViewModel(), ICreateFreeTimeViewModel {

    override var title by mutableStateOf(freeTime?.title ?: "")
    override var date by mutableStateOf(
        freeTime?.date ?: Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
    override var start by mutableStateOf(freeTime?.start ?: LocalTime(0, 0))
    override var end by mutableStateOf(freeTime?.end ?: LocalTime(0, 0))
    override var weekly by mutableStateOf(freeTime?.weekly ?: false)

    protected fun registerNewFreeTime(freeTime: FreeTime) {
        val freeTimes = model.freeTimes.orEmpty().toMutableMap()
        model.freeTimes = freeTimes
        freeTimes[freeTime.id] = freeTime.data

        navController.main()
    }
}

class CreateFreeTimeViewModel(
    private val api: RemoteAPI,
    model: ModelFacade,
    navController: NavController
) : BaseCreateFreeTimeViewModel(model, navController) {
    override val showDelete = false

    override fun submit() {
        TODO("Not yet implemented")
    }

    override fun delete() {
        error("Can't delete FreeTime before creating it")
    }
}

class EditFreeTimeViewModel(
    private val api: RemoteAPI,
    model: ModelFacade,
    target: FreeTime,
    navController: NavController
) : BaseCreateFreeTimeViewModel(model, navController, target.data) {
    override val showDelete = true

    override fun submit() {
        TODO("Not yet implemented")
    }

    override fun delete() {
        TODO("Not yet implemented")
    }
}