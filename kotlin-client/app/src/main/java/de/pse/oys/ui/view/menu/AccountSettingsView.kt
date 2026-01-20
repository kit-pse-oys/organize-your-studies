package de.pse.oys.ui.view.menu

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AccountSettingsView(viewModel: IAccountSettingsViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
}

interface IAccountSettingsViewModel {
    fun logout()
    fun deleteAccount()
}