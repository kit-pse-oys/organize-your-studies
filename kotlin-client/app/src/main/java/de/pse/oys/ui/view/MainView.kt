package de.pse.oys.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.defaultHandleError
import de.pse.oys.data.ensureFreeTimes
import de.pse.oys.data.ensureUnits
import de.pse.oys.data.facade.FreeTimeData
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.StepData
import de.pse.oys.ui.navigation.additions
import de.pse.oys.ui.navigation.availableRatings
import de.pse.oys.ui.navigation.menu
import de.pse.oys.ui.theme.Typography
import de.pse.oys.ui.util.CalendarDay
import de.pse.oys.ui.util.CalendarEvent
import de.pse.oys.ui.util.CalendarWeek
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.atTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Composable
fun MainView(viewModel: IMainViewModel) {
    var weeklyCalendar by remember { mutableStateOf(false) }
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
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                IconButton(onClick = viewModel::navigateToMenu) {
                    Icon(
                        painterResource(R.drawable.outline_menu_24),
                        stringResource(R.string.open_menu_button)
                    )
                }
                Text(stringResource(R.string.welcome_name))
                IconButton(onClick = viewModel::navigateToAdditions) {
                    Icon(
                        painterResource(R.drawable.outline_add_24),
                        stringResource(R.string.open_additions_button)
                    )
                }
            }

            if (weeklyCalendar) {
                val events = viewModel.units.mapValues { (_, list) ->
                    list.map {
                        PlannedEvent(it.title, it.description, it.color, it.start, it.end)
                    }
                } + viewModel.freeTimes.mapValues { (_, list) ->
                    list.map {
                        PlannedEvent(it.title, null, Color.Black, it.start, it.end)
                    }
                }
                CalendarWeek(Modifier.weight(1f), events = events)
            } else {
                val events = viewModel.unitsToday.map {
                    PlannedEvent(it.title, it.description, it.color, it.start, it.end)
                } + viewModel.freeTimesToday.map {
                    PlannedEvent(it.title, null, Color.Black, it.start, it.end)
                }
                CalendarDay(Modifier.weight(1f), events = events)
            }

            // TODO: Rate units button

            LazyColumn(Modifier.weight(1f)) {
                items(viewModel.unitsTomorrow) {
                    Row(
                        modifier = Modifier
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                        )
                        Column(Modifier.padding(start = 4.dp)) {
                            Text(
                                text = it.title,
                                style = Typography.bodySmall,
                                color = Color.Black
                            )
                            // TODO: Start time / end time
                        }
                    }
                }
            }
        }
    }
}

private class PlannedEvent(
    private val title: String,
    private val description: String?,
    private val color: Color,
    override val start: LocalTime,
    override val end: LocalTime,
) : CalendarEvent {
    @Composable
    override fun Draw(modifier: Modifier) {
        Row(
            modifier = modifier
                .background(color.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(2.dp))
            )
            Column(Modifier.padding(start = 4.dp)) {
                Text(
                    text = title,
                    style = Typography.bodySmall,
                    color = Color.Black
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = Typography.bodySmall,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

data class PlannedUnit(
    val title: String,
    val description: String,
    val color: Color,
    val start: LocalTime,
    val end: LocalTime,
)

data class PlannedFreeTime(
    val title: String,
    val start: LocalTime,
    val end: LocalTime,
)

interface IMainViewModel {
    var error: Boolean

    val units: Map<DayOfWeek, List<PlannedUnit>>
    val unitsToday: List<PlannedUnit>
    val unitsTomorrow: List<PlannedUnit>
    val freeTimes: Map<DayOfWeek, List<PlannedFreeTime>>
    val freeTimesToday: List<PlannedFreeTime>

    fun moveUnitToday(unit: PlannedUnit, newStart: LocalTime)
    fun moveUnit(unit: PlannedUnit, newDay: DayOfWeek, newStart: LocalTime)
    fun moveUnitAutomatically(unit: PlannedUnit)
    fun marksAsFinished(unit: PlannedUnit)

    fun navigateToMenu()
    fun navigateToAdditions()
    fun navigateToUnitRating()
}

class MainViewModel(
    private val api: RemoteAPI,
    private val model: ModelFacade,
    private val navController: NavController
) : ViewModel(), IMainViewModel {
    private val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private var _units: Map<PlannedUnit, Uuid> = mapOf()
    private fun updateUnits(units: Map<DayOfWeek, Map<Uuid, StepData>>) {
        val _units = units.flatMap { (day, units) ->
            units.map { (id, unit) ->
                id to (day to PlannedUnit(
                    unit.task.data.title,
                    unit.task.data.module.data.description,
                    unit.task.data.module.data.color,
                    unit.start,
                    unit.end
                ))
            }
        }.toMap()
        this._units = _units.map { (id, unit) -> unit.second to id }.toMap()

        this.units =
            _units.map { it.value }.groupBy { it.first }.mapValues { it.value.map { it.second } }
        this.unitsToday = this.units[today.dayOfWeek] ?: listOf()
        this.unitsTomorrow =
            this.units[DayOfWeek((today.dayOfWeek.isoDayNumber % 7) + 1)] ?: listOf()
    }

    private var _freeTimes: Map<PlannedFreeTime, Uuid> = mapOf()
    private fun updateFreeTimes(freeTimes: Map<Uuid, FreeTimeData>) {
        val startOfWeek =
            today - (DateTimePeriod(days = today.dayOfWeek.isoDayNumber - 1) as DatePeriod)
        val endOfWeek =
            today + (DateTimePeriod(days = 7 - today.dayOfWeek.isoDayNumber) as DatePeriod)
        val week = startOfWeek..endOfWeek
        val _freeTimes = freeTimes.mapNotNull { (id, freeTime) ->
            if (!freeTime.weekly && freeTime.date !in week) return@mapNotNull null

            val dayOfWeek = freeTime.date.dayOfWeek
            (id to (dayOfWeek to PlannedFreeTime(
                freeTime.title,
                freeTime.start,
                freeTime.end
            )))
        }.toMap()
        this._freeTimes = _freeTimes.map { (id, freeTime) -> freeTime.second to id }.toMap()

        this.freeTimes = _freeTimes.map { it.value }.groupBy { it.first }
            .mapValues { it.value.map { it.second } }
        this.freeTimesToday = this.freeTimes[today.dayOfWeek] ?: listOf()
    }

    override var units: Map<DayOfWeek, List<PlannedUnit>> by mutableStateOf(mapOf())
    override var unitsToday: List<PlannedUnit> by mutableStateOf(listOf())
    override var unitsTomorrow: List<PlannedUnit> by mutableStateOf(listOf())

    override var freeTimes: Map<DayOfWeek, List<PlannedFreeTime>> by mutableStateOf(mapOf())
    override var freeTimesToday: List<PlannedFreeTime> by mutableStateOf(listOf())

    override var error: Boolean by mutableStateOf(false)

    init {
        if (model.steps != null) {
            updateUnits(model.steps!!)
        } else {
            viewModelScope.launch {
                model.ensureUnits(api).defaultHandleError(navController) { error = true }?.let {
                    updateUnits(it)
                }
            }
        }

        if (model.freeTimes != null) {
            updateFreeTimes(model.freeTimes!!)
        } else {
            viewModelScope.launch {
                model.ensureFreeTimes(api).defaultHandleError(navController) { error = true }?.let {
                    updateFreeTimes(it)
                }
            }
        }
    }

    override fun moveUnitToday(
        unit: PlannedUnit,
        newStart: LocalTime
    ) {
        val uuid = _units[unit] ?: return
        viewModelScope.launch {
            api.moveUnit(uuid, today.atTime(newStart))
        }
    }

    override fun moveUnit(
        unit: PlannedUnit,
        newDay: DayOfWeek,
        newStart: LocalTime
    ) {
        val uuid = _units[unit] ?: return
        val date =
            today + (DateTimePeriod(days = (newDay.isoDayNumber - today.dayOfWeek.isoDayNumber + 7) % 7) as DatePeriod)
        viewModelScope.launch {
            api.moveUnit(uuid, date.atTime(newStart))
        }
    }

    override fun moveUnitAutomatically(unit: PlannedUnit) {
        val uuid = _units[unit] ?: return
        viewModelScope.launch {
            api.moveUnitAutomatically(uuid)
        }
    }

    override fun marksAsFinished(unit: PlannedUnit) {
        val uuid = _units[unit] ?: return
        val actualDuration =
            Clock.System.now() - unit.start.atDate(today).toInstant(TimeZone.currentSystemDefault())
        viewModelScope.launch {
            api.markUnitFinished(uuid, actualDuration)
        }
    }

    override fun navigateToMenu() {
        navController.menu()
    }

    override fun navigateToAdditions() {
        navController.additions()
    }

    override fun navigateToUnitRating() {
        navController.availableRatings()
    }
}