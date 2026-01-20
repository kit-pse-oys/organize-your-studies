package de.pse.oys.ui.view.menu

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.pse.oys.data.properties.Darkmode

@Composable
fun MenuView(viewModel: IMenuViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
}

interface IMenuViewModel {
    val darkmode: Darkmode

    fun navigateToModules()
    fun navigateToTasks()
    fun navigateToFreeTimes()
    fun navigateToEditQuestionnaire()
    fun navigateToAccountSettings()

    fun updatePlan()
}