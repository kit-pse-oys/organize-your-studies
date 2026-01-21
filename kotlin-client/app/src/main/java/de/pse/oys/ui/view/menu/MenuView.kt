package de.pse.oys.ui.view.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.properties.Darkmode
import de.pse.oys.data.properties.Properties
import de.pse.oys.ui.navigation.accountSettings
import de.pse.oys.ui.navigation.editQuestionnaire
import de.pse.oys.ui.navigation.myFreeTimes
import de.pse.oys.ui.navigation.myModules
import de.pse.oys.ui.navigation.myTasks
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
fun MenuView(viewModel: IMenuViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        val darkmode by viewModel.darkmode.collectAsStateWithLifecycle()

        Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.menu_header))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Darkmode.entries.forEachIndexed { i, value ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = i,
                                count = Darkmode.entries.size
                            ),
                            onClick = { viewModel.setDarkmode(value) },
                            selected = value == darkmode,
                            label = {
                                Text(
                                    value.label(),
                                    maxLines = 1,
                                    fontSize = LocalTextStyle.current.fontSize / 1.5f
                                )
                            }
                        )
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    onClick = viewModel::navigateToModules
                ) { Text(stringResource(R.string.my_modules_button)) }
                Button(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    onClick = viewModel::navigateToTasks
                ) { Text(stringResource(R.string.my_tasks_button)) }
                Button(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    onClick = viewModel::navigateToFreeTimes
                ) { Text(stringResource(R.string.my_free_times_button)) }
                Button(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    onClick = viewModel::navigateToEditQuestionnaire
                ) { Text(stringResource(R.string.edit_questionnaire_button)) }
                Button(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    onClick = viewModel::navigateToAccountSettings
                ) { Text(stringResource(R.string.account_settings_button)) }
                Button(modifier = Modifier.fillMaxWidth(0.7f), onClick = viewModel::updatePlan) {
                    Text(
                        stringResource(R.string.update_plan_button)
                    )
                }
            }
        }
    }
}

@Composable
private fun Darkmode.label(): String = when (this) {
    Darkmode.DISABLED -> stringResource(R.string.darkmode_disabled)
    Darkmode.ENABLED -> stringResource(R.string.darkmode_enabled)
    Darkmode.SYSTEM -> stringResource(R.string.darkmode_system)
}

interface IMenuViewModel {
    val darkmode: StateFlow<Darkmode>
    fun setDarkmode(darkmode: Darkmode)

    fun navigateToModules()
    fun navigateToTasks()
    fun navigateToFreeTimes()
    fun navigateToEditQuestionnaire()
    fun navigateToAccountSettings()

    fun updatePlan()
}


class MenuViewModel(val properties: Properties, val api: RemoteAPI, val navController: NavController) : ViewModel(),
    IMenuViewModel {
    override val darkmode = properties.darkmode.stateIn(viewModelScope, SharingStarted.Eagerly, Darkmode.SYSTEM)

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
        TODO("Not yet implemented")
    }
}