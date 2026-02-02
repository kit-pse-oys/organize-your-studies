package de.pse.oys.ui.view.additions.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.ModuleData
import de.pse.oys.data.facade.Priority
import de.pse.oys.ui.navigation.main
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.ColorPicker
import de.pse.oys.ui.util.DeleteButton
import de.pse.oys.ui.util.DeleteElementDialog
import de.pse.oys.ui.util.InputLabel
import de.pse.oys.ui.util.SingleLineInput
import de.pse.oys.ui.util.SubmitButton
import de.pse.oys.ui.util.ViewHeaderBig

/**
 * View for creating a new module or editing an existing one.
 * Different user input fields filled with either default values (when creating) or existing values (when editing).
 * @param viewModel the [ICreateModuleViewModel] for this view.
 */
@Composable
fun CreateModuleView(viewModel: ICreateModuleViewModel) {
    var confirmDelete by remember { mutableStateOf(false) }
    val submitButtonActive = viewModel.title.isNotBlank() && viewModel.description.isNotBlank()
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (viewModel.showDelete) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                    DeleteButton { confirmDelete = true }
                }
            }
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ViewHeaderBig(
                    if (viewModel.showDelete) stringResource(R.string.edit_module) else stringResource(
                        id = R.string.new_module
                    )
                )
                InputLabel(text = stringResource(id = R.string.enter_title))
                SingleLineInput(viewModel.title) { viewModel.title = it }
                InputLabel(stringResource(id = R.string.enter_description))
                SingleLineInput(viewModel.description) { viewModel.description = it }
                InputLabel(stringResource(id = R.string.select_priority))
                PriorityChips(
                    current = viewModel.priority,
                    onSelect = { viewModel.priority = it })
                InputLabel(stringResource(id = R.string.select_color))
                ColorPicker(onColorChanged = { viewModel.color = it })
                Spacer(modifier = Modifier.weight(1f))
                SubmitButton(
                    if (viewModel.showDelete) stringResource(R.string.save_changes_button) else stringResource(
                        id = R.string.add_module_button
                    ),
                    submitButtonActive
                ) { viewModel.submit() }
            }

            if (confirmDelete) {
                DeleteElementDialog(
                    onDismiss = { confirmDelete = false },
                    onConfirm = viewModel::delete
                )
            }
        }
    }
}

/**
 * Chips for selecting a priority.
 * @param current the current [Priority] to be displayed.
 * @param onSelect the function to be called when a priority is selected.
 */
@Composable
private fun PriorityChips(current: Priority, onSelect: (Priority) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
        PriorityChip(
            priority = Priority.HIGH,
            current = current,
            icon = painterResource(R.drawable.outline_expand_circle_up_24),
            label = stringResource(id = R.string.priority_high),
            onSelect = onSelect
        )
        PriorityChip(
            priority = Priority.NEUTRAL,
            current = current,
            icon = painterResource(R.drawable.outline_expand_circle_right_24),
            label = stringResource(id = R.string.priority_neutral),
            onSelect = onSelect
        )
        PriorityChip(
            priority = Priority.LOW,
            current = current,
            icon = painterResource(R.drawable.outline_expand_circle_down_24),
            label = stringResource(id = R.string.priority_low),
            onSelect = onSelect
        )
    }
}

/**
 * Chip for selecting a priority.
 * @param priority the [Priority] to be displayed.
 * @param current the current [Priority] to be displayed.
 * @param icon the icon to be displayed next to the label.
 * @param label the label to be displayed.
 * @param onSelect the function to be called when the chip is selected.
 */
@Composable
private fun PriorityChip(
    priority: Priority,
    current: Priority,
    icon: Painter,
    label: String,
    onSelect: (Priority) -> Unit
) {
    FilterChip(
        selected = priority == current,
        onClick = { onSelect(priority) },
        leadingIcon = { Icon(painter = icon, contentDescription = label) },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = LightBlue,
            containerColor = Color.Transparent,
            selectedLabelColor = Color.DarkGray,
            selectedLeadingIconColor = Color.Blue,
            iconColor = LightBlue
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = priority == current,
            borderColor = LightBlue,
            selectedBorderColor = Blue,
            borderWidth = 1.2.dp,
            selectedBorderWidth = 1.2.dp
        )
    )
}

/**
 * Interface for the view model of [CreateModuleView].
 */
interface ICreateModuleViewModel {
    val showDelete: Boolean

    var title: String
    var description: String
    var priority: Priority
    var color: Color

    /**
     * Submits the form data to the server and navigates to the main screen.
     */
    fun submit()

    /**
     * Deletes the module from the server and navigates to the main screen.
     */
    fun delete()
}

/**
 * Base view model for [CreateModuleView] with default values.
 * @param model the [ModelFacade] for the app.
 * @param navController the [NavController] for navigation.
 * @param module the [ModuleData] to be used instead of default values.
 */
abstract class BaseCreateModuleViewModel(
    private val model: ModelFacade,
    private val navController: NavController,
    module: ModuleData? = null
) : ViewModel(), ICreateModuleViewModel {
    override var title by mutableStateOf(module?.title ?: "")
    override var description by mutableStateOf(module?.description ?: "")
    override var priority by mutableStateOf(module?.priority ?: Priority.NEUTRAL)
    override var color by mutableStateOf(module?.color ?: Color.Unspecified)

    /**
     * Registers a new module in the [ModelFacade].
     * @param module the [Module] to be registered.
     */
    protected fun registerNewModule(module: Module) {
        val modules = model.modules.orEmpty().toMutableMap()
        model.modules = modules
        modules[module.id] = module.data

        navController.main()
    }
}

/**
 * View model for creating a new module.
 * @param api the [RemoteAPI] for the app.
 * @param model the [ModelFacade] for the app.
 * @param navController the [NavController] for navigation.
 */
class CreateModuleViewModel(
    private val api: RemoteAPI,
    model: ModelFacade,
    navController: NavController
) :
    BaseCreateModuleViewModel(model, navController) {
    override val showDelete = false

    override fun submit() {
        TODO("Not yet implemented")
    }

    override fun delete() {
        error("Can't delete Module before creating it")
    }
}

/**
 * View model for editing an existing module.
 * @param api the [RemoteAPI] for the app.
 * @param model the [ModelFacade] for the app.
 * @param target the existing [Module] to be edited.
 * @param navController the [NavController] for navigation.
 */
class EditModuleViewModel(
    private val api: RemoteAPI,
    model: ModelFacade,
    target: Module,
    navController: NavController
) : BaseCreateModuleViewModel(model, navController, target.data) {
    private val uuid = target.id

    override val showDelete = true

    override fun submit() {
        TODO("Not yet implemented")
    }

    override fun delete() {
        TODO("Not yet implemented")
    }
}
