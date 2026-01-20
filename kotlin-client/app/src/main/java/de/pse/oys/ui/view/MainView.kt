package de.pse.oys.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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