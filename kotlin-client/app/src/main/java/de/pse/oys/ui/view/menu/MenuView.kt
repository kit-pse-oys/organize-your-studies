package de.pse.oys.ui.view.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.defaultHandleError
import de.pse.oys.data.properties.Darkmode
import de.pse.oys.data.properties.Properties
import de.pse.oys.ui.navigation.accountSettings
import de.pse.oys.ui.navigation.editQuestionnaire
import de.pse.oys.ui.navigation.myFreeTimes
import de.pse.oys.ui.navigation.myModules
import de.pse.oys.ui.navigation.myTasks
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.MediumBlue
import de.pse.oys.ui.util.SimpleMenuAndAdditionsButton
import de.pse.oys.ui.util.ViewHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

/**
 * View for the menu screen with buttons to navigate to different views.
 * @param viewModel the [IMenuViewModel] for this view.
 */
@Composable
fun MenuView(viewModel: IMenuViewModel) {
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
        val darkmode by viewModel.darkmode.collectAsStateWithLifecycle()

        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize(), contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ViewHeader(stringResource(R.string.menu_header))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth(0.95f)) {
                    Darkmode.entries.forEachIndexed { i, value ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = i,
                                count = Darkmode.entries.size
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Blue,
                                activeContentColor = Color.White,
                                inactiveContainerColor = Color.Transparent,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                                inactiveBorderColor = MediumBlue
                            ),
                            onClick = { viewModel.setDarkmode(value) },
                            selected = value == darkmode,
                            label = {
                                Text(
                                    value.label(),
                                    maxLines = 1,
                                    style = typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        )
                    }
                }
                SimpleMenuAndAdditionsButton(
                    stringResource(R.string.my_modules_button),
                    onClick = viewModel::navigateToModules
                )
                SimpleMenuAndAdditionsButton(
                    stringResource(R.string.my_tasks_button),
                    onClick = viewModel::navigateToTasks
                )
                SimpleMenuAndAdditionsButton(
                    stringResource(R.string.my_free_times_button),
                    onClick = viewModel::navigateToFreeTimes
                )
                SimpleMenuAndAdditionsButton(
                    stringResource(R.string.edit_questionnaire_button),
                    onClick = viewModel::navigateToEditQuestionnaire
                )
                SimpleMenuAndAdditionsButton(
                    stringResource(R.string.account_settings_button),
                    onClick = viewModel::navigateToAccountSettings
                )
                SimpleMenuAndAdditionsButton(
                    stringResource(R.string.update_plan_button),
                    onClick = viewModel::updatePlan
                )
            }
        }
    }
}

/**
 * Converts a [Darkmode] to a string.
 */
@Composable
private fun Darkmode.label(): String = when (this) {
    Darkmode.DISABLED -> stringResource(R.string.darkmode_disabled)
    Darkmode.ENABLED -> stringResource(R.string.darkmode_enabled)
    Darkmode.SYSTEM -> stringResource(R.string.darkmode_system)
}

/**
 * Interface for the view model of the [MenuView].
 * @property darkmode the [StateFlow] of the current [Darkmode].
 */
interface IMenuViewModel {
    var error: Boolean
    val darkmode: StateFlow<Darkmode>

    /**
     * Sets the darkmode to the given value.
     * @param darkmode the [Darkmode] to be set.
     */
    fun setDarkmode(darkmode: Darkmode)

    /**
     * Navigate to the ModulesView.
     */
    fun navigateToModules()

    /**
     * Navigate to the TasksView.
     */
    fun navigateToTasks()

    /**
     * Navigate to the FreeTimesView.
     */
    fun navigateToFreeTimes()

    /**
     * Navigate to the EditQuestionnaireView.
     */
    fun navigateToEditQuestionnaire()

    /**
     * Navigate to the AccountSettingsView.
     */
    fun navigateToAccountSettings()

    /**
     * Updates the plan of the user.
     */
    fun updatePlan()
}

/**
 * View model for the [MenuView].
 * @param properties the [Properties] for this view.
 * @param api the [RemoteAPI] for this view.
 * @param navController the [NavController] for this view.
 */
class MenuViewModel(
    private val properties: Properties,
    private val api: RemoteAPI,
    private val navController: NavController
) : ViewModel(),
    IMenuViewModel {
    override var error: Boolean by mutableStateOf(false)
    override val darkmode = properties.darkmode.stateIn(
        viewModelScope + Dispatchers.IO,
        SharingStarted.Eagerly,
        Darkmode.SYSTEM
    )

    override fun setDarkmode(darkmode: Darkmode) {
        viewModelScope.launch {
            properties.setDarkmode(darkmode)
        }
    }

    override fun navigateToModules() {
        navController.myModules()
    }

    override fun navigateToTasks() {
        navController.myTasks()
    }

    override fun navigateToFreeTimes() {
        navController.myFreeTimes()
    }

    override fun navigateToEditQuestionnaire() {
        navController.editQuestionnaire()
    }

    override fun navigateToAccountSettings() {
        navController.accountSettings()
    }

    override fun updatePlan() {
        viewModelScope.launch {
            api.updatePlan().defaultHandleError(navController) { error = true }
        }
    }
}