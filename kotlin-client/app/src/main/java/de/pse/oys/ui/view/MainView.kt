package de.pse.oys.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.additions
import de.pse.oys.ui.navigation.availableRatings
import de.pse.oys.ui.navigation.menu
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

@Composable
fun MainView(viewModel: IMainViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
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