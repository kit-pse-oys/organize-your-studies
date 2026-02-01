package de.pse.oys.ui.view.additions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.ui.navigation.createFreeTime
import de.pse.oys.ui.navigation.createModule
import de.pse.oys.ui.navigation.createTask
import de.pse.oys.ui.util.SimpleMenuAndAdditionsButton
import de.pse.oys.ui.util.ViewHeader

@Composable
fun AdditionsView(viewModel: IAdditionsViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewHeader(text = stringResource(id = R.string.additions_header))
            SimpleMenuAndAdditionsButton(
                label = stringResource(id = R.string.new_module),
                onClick = { viewModel.navigateToCreateModule() }
            )
            SimpleMenuAndAdditionsButton(
                label = stringResource(id = R.string.new_task),
                onClick = { viewModel.navigateToCreateTask() }
            )
            SimpleMenuAndAdditionsButton(
                label = stringResource(id = R.string.new_freetime),
                onClick = { viewModel.navigateToCreateFreeTime() }
            )
        }
    }
}

interface IAdditionsViewModel {
    fun navigateToCreateModule()
    fun navigateToCreateTask()
    fun navigateToCreateFreeTime()
}

class AdditionsViewModel(private val navController: NavController) : ViewModel(),
    IAdditionsViewModel {

    override fun navigateToCreateModule() {
        navController.createModule()
    }

    override fun navigateToCreateTask() {
        navController.createTask()
    }

    override fun navigateToCreateFreeTime() {
        navController.createFreeTime()
    }
}