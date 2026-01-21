package de.pse.oys.ui.view.additions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AdditionsView(viewModel: IAdditionsViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
}

interface IAdditionsViewModel {
    fun navigateToCreateModule()
    fun navigateToCreateTask()
    fun navigateToCreateFreeTime()
}