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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.additions
import de.pse.oys.ui.navigation.availableRatings
import de.pse.oys.ui.navigation.menu
import de.pse.oys.ui.theme.Typography
import de.pse.oys.ui.util.CalendarDay
import de.pse.oys.ui.util.CalendarEvent
import de.pse.oys.ui.util.CalendarWeek
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

@Composable
fun MainView(viewModel: IMainViewModel) {
    var weeklyCalendar by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
    override val units: Map<DayOfWeek, List<PlannedUnit>>
        get() = TODO("Not yet implemented")
    override val unitsToday: List<PlannedUnit>
        get() = TODO("Not yet implemented")
    override val unitsTomorrow: List<PlannedUnit>
        get() = TODO("Not yet implemented")

    override val freeTimes: Map<DayOfWeek, List<PlannedFreeTime>>
        get() = TODO("Not yet implemented")
    override val freeTimesToday: List<PlannedFreeTime>
        get() = TODO("Not yet implemented")

    override fun moveUnitToday(
        unit: PlannedUnit,
        newStart: LocalTime
    ) {
        TODO("Not yet implemented")
    }

    override fun moveUnit(
        unit: PlannedUnit,
        newDay: DayOfWeek,
        newStart: LocalTime
    ) {
        TODO("Not yet implemented")
    }

    override fun moveUnitAutomatically(unit: PlannedUnit) {
        TODO("Not yet implemented")
    }

    override fun marksAsFinished(unit: PlannedUnit) {
        TODO("Not yet implemented")
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