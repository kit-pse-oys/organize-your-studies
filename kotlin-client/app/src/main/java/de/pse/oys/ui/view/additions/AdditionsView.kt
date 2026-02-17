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
import de.pse.oys.ui.util.ViewHeaderWithBackOption

/**
 * View for the additions menu with buttons to add a new module, task or freetime.
 * @param viewModel the [IAdditionsViewModel] used to handle navigation logic.
 */
@Composable
fun AdditionsView(viewModel: IAdditionsViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewHeaderWithBackOption(
                viewModel::navigateBack,
                stringResource(id = R.string.additions_header)
            )
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

/**
 * Interface for the view model of the [AdditionsView].
 */
interface IAdditionsViewModel {
    /**
     * Navigate to the CreateModuleView.
     */
    fun navigateToCreateModule()

    /**
     * Navigate to the CreateTaskView.
     */
    fun navigateToCreateTask()

    /**
     * Navigate to the CreateFreeTimeView.
     */
    fun navigateToCreateFreeTime()

    /**
     * Navigate back to the previous view.
     */
    fun navigateBack()
}

/**
 * View model for the [AdditionsView] that implements the navigation.
 * @param navController the [NavController] for this view.
 */
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

    override fun navigateBack() {
        navController.popBackStack()
    }
}