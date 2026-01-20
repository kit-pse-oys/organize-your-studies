package de.pse.oys.ui.view.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.RemoteAPI
import kotlinx.coroutines.launch

@Composable
fun AccountSettingsView(viewModel: IAccountSettingsViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        var confirmDelete by remember { mutableStateOf(false) }

        Column(modifier = Modifier.padding(innerPadding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.manage_account_header))
            Button(onClick = viewModel::logout) { Text(stringResource(R.string.logout_button)) }
            Button(onClick = {
                confirmDelete = true
            }) {
                Text(stringResource(R.string.delete_account_button))
            }
        }

        if (confirmDelete) {
            DeleteAccountDialog(
                onDismiss = { confirmDelete = false },
                onConfirm = viewModel::deleteAccount
            )
        }
    }
}

@Composable
private fun DeleteAccountDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.confirm_yes)) }
        }, dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.confirm_no)) }
        }, title = {
            Text(stringResource(R.string.delete_account_confirm_header))
        }, text = {
            Text(stringResource(R.string.delete_account_confirm_body))
        })
}

interface IAccountSettingsViewModel {
    fun logout()
    fun deleteAccount()
}

class AccountSettingsViewModel(val api: RemoteAPI, val navController: NavController) : ViewModel(),
    IAccountSettingsViewModel {
    override fun logout() {
        viewModelScope.launch {
            api.logout()
            TODO("Navigate to login")
        }
    }

    override fun deleteAccount() {
        viewModelScope.launch {
            api.deleteAccount()
            TODO("Navigate to login")
        }
    }
}