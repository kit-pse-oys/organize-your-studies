package de.pse.oys.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.additions
import de.pse.oys.ui.navigation.availableRatings
import de.pse.oys.ui.navigation.menu
import de.pse.oys.ui.util.CalendarDay
import de.pse.oys.ui.util.CalendarWeek
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

@Composable
fun MainView(viewModel: IMainViewModel) {
    var weeklyCalendar by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                // TODO: Menu button, Header, Additions button
            }

            if (weeklyCalendar) {
                CalendarWeek(Modifier.weight(1f))
            } else {
                CalendarDay(Modifier.weight(1f))
            }

            // TODO: Rate units button

            Column(Modifier.weight(1f)) {
                for (unit in viewModel.unitsTomorrow) {
                    // TODO: Units tomorrow
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
    val unitsToday: List<PlannedUnit>
    val unitsTomorrow: List<PlannedUnit>
    val freeTimesToday: List<PlannedFreeTime>

    fun unitsFor(day: DayOfWeek): StateFlow<List<PlannedUnit>>
    fun freeTimesFor(day: DayOfWeek): StateFlow<List<PlannedFreeTime>>

    fun moveUnitToday(unit: PlannedUnit, newStart: LocalTime)
    fun moveUnit(unit: PlannedUnit, newDay: DayOfWeek, newStart: LocalTime)
    fun moveUnitAutomatically(unit: PlannedUnit)
    fun marksAsFinished(unit: PlannedUnit)

    fun navigateToMenu()
    fun navigateToAdditions()
    fun navigateToUnitRating()
}

class MainViewModel(private val api: RemoteAPI, private val model: ModelFacade, private val navController: NavController) : ViewModel(), IMainViewModel {
    override val unitsToday: List<PlannedUnit>
        get() = TODO("Not yet implemented")
    override val unitsTomorrow: List<PlannedUnit>
        get() = TODO("Not yet implemented")
    override val freeTimesToday: List<PlannedFreeTime>
        get() = TODO("Not yet implemented")

    override fun unitsFor(day: DayOfWeek): StateFlow<List<PlannedUnit>> {
        TODO("Not yet implemented")
    }

    override fun freeTimesFor(day: DayOfWeek): StateFlow<List<PlannedFreeTime>> {
        TODO("Not yet implemented")
    }

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