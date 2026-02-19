package de.pse.oys.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedIconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.theme.MediumBlue
import de.pse.oys.ui.theme.Typography
import de.pse.oys.ui.util.CalendarDay
import de.pse.oys.ui.util.CalendarEvent
import de.pse.oys.ui.util.CalendarWeek
import de.pse.oys.ui.util.SimpleMenuAndAdditionsButton
import de.pse.oys.ui.util.ViewHeader
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
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

/**
 * View for the main page.
 * Shows the plan with units and free times for the current day or the whole week.
 * Allows the user to go to menu, additions or unit rating and shows upcoming units.
 * @param viewModel the [IMainViewModel] for this view.
 */
@Composable
fun MainView(viewModel: IMainViewModel) {
    var weeklyCalendar by remember { mutableStateOf(false) }
    var clickedEvent by remember { mutableStateOf<PlannedUnit?>(null) }
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
                OutlinedIconButton(
                    onClick = viewModel::navigateToMenu,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .size(48.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Blue)
                ) {
                    Icon(
                        painterResource(R.drawable.outline_menu_24),
                        stringResource(R.string.open_menu_button),
                        tint = Color.White
                    )
                }
                ViewHeader(stringResource(R.string.welcome_name))
                OutlinedIconButton(
                    onClick = viewModel::navigateToAdditions,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .size(48.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Blue)
                ) {
                    Icon(
                        painterResource(R.drawable.outline_add_24),
                        stringResource(R.string.open_additions_button),
                        tint = Color.White
                    )
                }
            }

            PlanerSwitch(
                isWeekly = weeklyCalendar,
                onToggle = { weeklyCalendar = it },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (weeklyCalendar) {
                val events = viewModel.units.map { (day, list) ->
                    day to list.map {
                        PlannedEvent(it.title, it.description, it.color, it.start, it.end) {
                            clickedEvent = it
                        }
                    }
                } + viewModel.freeTimes.map { (day, list) ->
                    day to list.map {
                        PlannedEvent(it.title, null, Color.Black, it.start, it.end) {}
                    }
                }
                val eventsMap = events.groupBy { it.first }.mapValues { (_, lists) ->
                    lists.flatMap { it.second }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .weight(1.7f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightBlue)
                        .border(2.dp, Blue, RoundedCornerShape(16.dp))
                ) {
                    CalendarWeek(Modifier, events = eventsMap)
                }
            } else {
                val events = viewModel.unitsToday.map {
                    PlannedEvent(it.title, it.description, it.color, it.start, it.end) {
                        clickedEvent = it
                    }
                } + viewModel.freeTimesToday.map {
                    PlannedEvent(it.title, null, Color.Black, it.start, it.end) {}
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .weight(1.7f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LightBlue)
                        .border(2.dp, Blue, RoundedCornerShape(16.dp))
                ) {
                    CalendarDay(Modifier, events = events)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                RateUnitsButton(onClick = { viewModel.navigateToUnitRating() })
            }

            if (!weeklyCalendar) {
                Text(
                    stringResource(R.string.upcoming_units_header),
                    modifier = Modifier
                        .padding(start = 30.dp)
                        .padding(top = 10.dp, bottom = 6.dp),
                    style = typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyColumn(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (viewModel.unitsTomorrow.isEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.no_upcomig_units_available),
                                style = typography.bodySmall,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 30.dp)
                            )
                        }
                    } else {
                        items(viewModel.unitsTomorrow) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                UpcomingUnitField(it.title, it.start.toString(), it.end.toString())
                            }
                        }
                    }
                }
            }
        }

        clickedEvent?.let { unit ->
            Dialog({ clickedEvent = null }) {
                Column {
                    val start = unit.date.atTime(unit.start)
                    val end = unit.date.atTime(unit.end)
                    val now = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                    if (end >= now) {
                        SimpleMenuAndAdditionsButton(stringResource(R.string.move_automatically_button)) {
                            viewModel.moveUnitAutomatically(unit)
                            clickedEvent = null
                        }
                    }
                    if (now in start..end) {
                        SimpleMenuAndAdditionsButton(stringResource(R.string.mark_as_finished_early_button)) {
                            viewModel.marksAsFinished(unit)
                            clickedEvent = null
                        }
                    }
                }
            }
        }
    }
}

/**
 * Switch between weekly and daily planer.
 * @param isWeekly whether the weekly planer should be used.
 * @param onToggle the function to be called when the switch is toggled.
 * @param modifier the modifier to be applied to the switch.
 */
@Composable
private fun PlanerSwitch(
    isWeekly: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(listOf(Blue, MediumBlue))

    Box(
        modifier = modifier
            .fillMaxWidth(0.45f)
            .padding(vertical = 12.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(LightBlue)
            .border(1.dp, Blue, RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight()
                .padding(2.dp)
                .align(if (isWeekly) Alignment.CenterEnd else Alignment.CenterStart)
                .clip(RoundedCornerShape(20.dp))
                .background(gradient)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .clickable { onToggle(false) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.planer_switch_daily),
                    color = if (!isWeekly) Color.White else Blue,
                    fontWeight = FontWeight.Bold,
                    style = typography.bodySmall
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .clickable { onToggle(true) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.planer_switch_weekly),
                    color = if (isWeekly) Color.White else Blue,
                    fontWeight = FontWeight.Bold,
                    style = typography.bodySmall
                )
            }
        }
    }
}

/**
 * Button for user to go to unit rating.
 * @param onClick the function to be called when the button is clicked.
 */
@Composable
private fun RateUnitsButton(onClick: () -> Unit) {
    val gradientColors = listOf(Blue, MediumBlue)
    val gradient = Brush.linearGradient(colors = gradientColors)
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .padding(vertical = 20.dp)
            .background(gradient, shape = shape)
            .clip(shape)
            .fillMaxWidth(0.6f),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 12.dp),
            shape = shape,
            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
        ) {
            Text(
                text = stringResource(R.string.rate_units_header),
                style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

/**
 * Field for displaying an upcoming unit.
 * @param title the title of the unit.
 * @param start the start time of the unit.
 * @param end the end time of the unit.
 */
@Composable
private fun UpcomingUnitField(title: String, start: String, end: String) {
    Box(
        modifier = Modifier
            .padding(bottom = 10.dp)
            .background(Blue, shape = RoundedCornerShape(12.dp))
            .fillMaxWidth(0.85f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Icon(
                painterResource(R.drawable.outline_calendar_clock_24),
                stringResource(R.string.open_menu_button),
                tint = Color.White,
                modifier = Modifier
                    .padding(end = 20.dp)
                    .size(36.dp)
            )
            Column {
                Text(
                    title,
                    style = typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "$start - $end",
                    style = typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = LightBlue
                )
            }
        }
    }
}

/**
 * Represents a planned event.
 * @param title the title of the event.
 * @param description the description of the event.
 * @param color the color of the event.
 * @param start the start time of the event.
 * @param end the end time of the event.
 * @param onClick the handler for when the event is clicked
 */
private class PlannedEvent(
    private val title: String,
    private val description: String?,
    private val color: Color,
    override val start: LocalTime,
    override val end: LocalTime,
    private val onClick: () -> Unit,
) : CalendarEvent {
    @Composable
    override fun Draw(modifier: Modifier) {
        Row(
            modifier = modifier
                .background(color.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                .padding(4.dp)
                .clickable(onClick = onClick)
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

/**
 * Data class representing a planned unit with its parameters.
 */
data class PlannedUnit(
    val title: String,
    val description: String,
    val color: Color,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
)

/**
 * Data class representing a planned free time with its parameters.
 */
data class PlannedFreeTime(
    val title: String,
    val start: LocalTime,
    val end: LocalTime,
)

/**
 * Interface for the view model of the [MainView].
 * @property error whether an error occurred.
 */
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

/**
 * View model for the [MainView].
 * @param api the [RemoteAPI] for this view.
 * @param model the [ModelFacade] for this view.
 * @param navController the [NavController] for this view.
 */
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
                    unit.task.data.module.data.title,
                    unit.task.data.module.data.color,
                    unit.date,
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
            this.units[DayOfWeek((today.dayOfWeek.isoDayNumber % 7) + 1)]?.let {
                it.sortedBy { it.start }
            } ?: listOf()
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
                freeTime.startTime,
                freeTime.endTime
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
                reloadUnits()
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

    private suspend fun reloadUnits() {
        model.steps = null
        model.ensureUnits(api).defaultHandleError(navController) { error = true }?.let {
            updateUnits(it)
        }
    }

    override fun moveUnitToday(
        unit: PlannedUnit,
        newStart: LocalTime
    ) {
        val uuid = _units[unit] ?: return
        viewModelScope.launch {
            api.moveUnit(uuid, today.atTime(newStart))
                .defaultHandleError(navController) { error = true }?.let {
                    reloadUnits()
                }
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
                .defaultHandleError(navController) { error = true }?.let {
                    reloadUnits()
                }
        }
    }

    override fun moveUnitAutomatically(unit: PlannedUnit) {
        val uuid = _units[unit] ?: return
        viewModelScope.launch {
            api.moveUnitAutomatically(uuid)
                .defaultHandleError(navController) { error = true }?.let {
                    reloadUnits()
                }
        }
    }

    override fun marksAsFinished(unit: PlannedUnit) {
        val uuid = _units[unit] ?: return
        val actualDuration =
            Clock.System.now() - unit.start.atDate(today).toInstant(TimeZone.currentSystemDefault())
        viewModelScope.launch {
            api.markUnitFinished(uuid, actualDuration)
                .defaultHandleError(navController) { error = true }
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