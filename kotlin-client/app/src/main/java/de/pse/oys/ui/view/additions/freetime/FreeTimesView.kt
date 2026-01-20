package de.pse.oys.ui.view.additions.freetime

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.pse.oys.data.facade.FreeTime

@Composable
fun FreeTimesView(viewModel: IFreeTimesViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
}

interface IFreeTimesViewModel {
    val freeTimes: List<FreeTime>

    fun select(freeTime: FreeTime)
}