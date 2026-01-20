package de.pse.oys.ui.view.additions.module

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import de.pse.oys.data.facade.Priority

@Composable
fun CreateModuleView(viewModel: ICreateModuleViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
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