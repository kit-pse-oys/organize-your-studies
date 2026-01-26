package de.pse.oys.ui.view.additions.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import de.pse.oys.data.facade.Priority
import de.pse.oys.ui.navigation.Intent
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.util.ColorPicker
import de.pse.oys.ui.util.InputLabel
import de.pse.oys.ui.util.SingleLineInput
import de.pse.oys.ui.util.ViewHeaderBig

@Composable
fun CreateModuleView(viewModel: ICreateModuleViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewHeaderBig(text = stringResource(id = R.string.new_module))
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
        }
    }
}

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
            labelColor = Color.DarkGray,
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

interface ICreateModuleViewModel {
    val showDelete: Boolean

    var title: String
    var description: String
    var priority: Priority
    var color: Color

    fun submit()
    fun delete()
}

abstract class BaseCreateModuleViewModel(
    initialTitle: String = "",
    initialDescription: String = "",
    initialPriority: Priority = Priority.NEUTRAL,
    initialColor: Color = Color.Black
) : ViewModel(), ICreateModuleViewModel {

    override var title by mutableStateOf(initialTitle)
    override var description by mutableStateOf(initialDescription)
    override var priority by mutableStateOf(initialPriority)
    override var color by mutableStateOf(initialColor)
}

class CreateModuleViewModel(val api: RemoteAPI, val navController: NavController) :
    BaseCreateModuleViewModel() {
    override val showDelete = false

    override fun submit() {
        TODO("Not yet implemented")
    }

    override fun delete() {
        TODO("Not yet implemented")
    }
}

class EditModuleViewModel(
    val api: RemoteAPI,
    val target: Module,
    val navController: NavController
) : BaseCreateModuleViewModel(
    initialTitle = target.data.title,
    initialDescription = target.data.description,
    initialPriority = target.data.priority,
    initialColor = target.data.color
) {
    override val showDelete = true

    override fun submit() {
        TODO("Not yet implemented")
    }

    override fun delete() {
        TODO("Not yet implemented")
    }
}

fun CreateModuleViewModel(
    intent: Intent,
    api: RemoteAPI,
    model: ModelFacade,
    navController: NavController
): BaseCreateModuleViewModel {
    return when (intent) {
        is Intent.Create -> CreateModuleViewModel(api, navController)
        is Intent.Edit -> EditModuleViewModel(
            api,
            intent.module(model) ?: TODO("Think about if this is even possible"),
            navController
        )
    }
}

