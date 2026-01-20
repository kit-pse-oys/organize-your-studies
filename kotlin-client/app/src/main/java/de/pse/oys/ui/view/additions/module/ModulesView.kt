package de.pse.oys.ui.view.additions.module

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.pse.oys.data.facade.Module

@Composable
fun ModulesView(viewModel: IModulesViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
}

interface IModulesViewModel {
    val modules: List<Module>

    fun select(module: Module)
}