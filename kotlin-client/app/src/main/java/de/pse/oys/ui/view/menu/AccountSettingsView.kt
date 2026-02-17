package de.pse.oys.ui.view.menu

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.defaultHandleError
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.Main
import de.pse.oys.ui.navigation.login
import de.pse.oys.ui.util.SimpleMenuAndAdditionsButton
import de.pse.oys.ui.util.ViewHeaderWithBackOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * View for the account settings.
 * Allows the user to logout and delete their account.
 * @param viewModel the [IAccountSettingsViewModel] for this view.
 */
@Composable
fun AccountSettingsView(viewModel: IAccountSettingsViewModel) {
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
        var confirmDelete by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewHeaderWithBackOption(
                viewModel::navigateBack,
                stringResource(id = R.string.account_settings_button)
            )
            SimpleMenuAndAdditionsButton(
                stringResource(R.string.logout_button),
                viewModel::logout
            )
            SimpleMenuAndAdditionsButton(
                stringResource(R.string.delete_account_button)
            ) { confirmDelete = true }
        }

        if (confirmDelete) {
            DeleteAccountDialog(
                onDismiss = { confirmDelete = false },
                onConfirm = viewModel::deleteAccount
            )
        }
    }
}

/**
 * Dialog for confirming the deletion of an account.
 * @param onDismiss the function to be called when the dialog is dismissed.
 * @param onConfirm the function to be called when the user confirms the deletion.
 */
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

/**
 * Interface for the view model of [AccountSettingsView].
 */
interface IAccountSettingsViewModel {
    var error: Boolean

    /**
     * Logs out the user and navigates to the login screen.
     */
    fun logout()

    /**
     * Deletes the user's account and navigates to the login screen.
     */
    fun deleteAccount()

    /**
     * Navigates back to the previous view.
     */
    fun navigateBack()
}

/**
 * View model for [AccountSettingsView].
 * @param api the [RemoteAPI] for the app.
 * @param navController the [NavController] for navigation.
 */
class AccountSettingsViewModel(
    private val api: RemoteAPI,
    private val model: ModelFacade,
    context: Context,
    private val navController: NavController
) : ViewModel(),
    IAccountSettingsViewModel {
    override var error: Boolean by mutableStateOf(false)

    private val credentialManager = CredentialManager.create(context)

    private fun resetModel() {
        model.modules = null
        model.tasks = null
        model.freeTimes = null
        model.steps = null
    }

    override fun logout() {
        viewModelScope.launch {
            api.logout()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            resetModel()

            withContext(Dispatchers.Main.immediate) {
                navController.login(dontGoBack = Main)
            }
        }
    }

    override fun deleteAccount() {
        viewModelScope.launch {
            api.deleteAccount().defaultHandleError(navController) { error = true }?.let {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                resetModel()

                withContext(Dispatchers.Main.immediate) {
                    navController.login(dontGoBack = Main)
                }
            }
        }
    }

    override fun navigateBack() {
        navController.popBackStack()
    }
}