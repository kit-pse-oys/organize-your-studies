package de.pse.oys.ui.view.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.Credentials
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.ui.navigation.Login
import de.pse.oys.ui.navigation.main
import de.pse.oys.ui.navigation.questionnaire
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.theme.MediumBlue
import de.pse.oys.ui.util.ViewHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * View for the login screen.
 * Allows the user to login or register and switches between the two.
 * Login/ Register Button only enabled when username and password are not empty and passwords match.
 * @param viewModel the [ILoginViewModel] for this view.
 */
@Composable
fun LoginView(viewModel: ILoginViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        var confirmPassword by remember { mutableStateOf("") }
        var registering by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ViewHeader(
                if (registering) stringResource(R.string.register_header)
                else stringResource(R.string.login_header)
            )
            Spacer(Modifier.weight(1f))
            UsernameTextField(
                viewModel.username,
                onUsernameChanged = { viewModel.username = it })
            Spacer(Modifier.height(6.dp))
            PasswordTextField(
                viewModel.password,
                onPasswordChanged = { viewModel.password = it })
            if (registering) {
                Spacer(Modifier.height(6.dp))
                PasswordTextField(
                    confirmPassword,
                    confirm = true,
                    isError = viewModel.password.isNotBlank() && viewModel.password != confirmPassword
                ) { confirmPassword = it }
            }
            Spacer(Modifier.height(10.dp))
            LoginButton(
                registering,
                viewModel.username.isNotBlank() && viewModel.password.isNotBlank() && (viewModel.password == confirmPassword || !registering)
            ) {
                if (registering) viewModel.register() else viewModel.login()
            }
            SwitchModeButton(
                registering,
                onSwitchMode = {
                    viewModel.password = ""
                    confirmPassword = ""
                    registering = !registering
                })
            Spacer(Modifier.weight(1f))

            // TODO: OIDC button
        }
    }
}

/**
 * Text field for the username, character limit is 20.
 * @param username the current username.
 * @param onUsernameChanged the function to be called when the username is changed.
 */
@Composable
private fun UsernameTextField(
    username: String,
    onUsernameChanged: (String) -> Unit
) {
    val maxLength = 20
    val tooLong = username.length > maxLength
    TextField(
        value = username,
        onValueChange = onUsernameChanged,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = LightBlue,
            unfocusedContainerColor = LightBlue,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Gray
        ),
        isError = tooLong,
        singleLine = true,
        label = { Text(stringResource(R.string.username_field)) })
}

/**
 * Text field for the password, character limit is 20.
 * @param password the current password.
 * @param confirm whether the passwordTextField is for confirmation.
 * @param isError whether the password is too long.
 * @param onPasswordChanged the function to be called when the password is changed.
 */
@Composable
private fun PasswordTextField(
    password: String,
    confirm: Boolean = false,
    isError: Boolean = false,
    onPasswordChanged: (String) -> Unit
) {
    val maxLength = 20
    val tooLong = password.length > maxLength
    TextField(
        value = password,
        onValueChange = onPasswordChanged,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = LightBlue,
            unfocusedContainerColor = LightBlue,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Gray
        ),
        singleLine = true,
        isError = isError || tooLong,
        label = {
            Text(
                if (confirm) stringResource(R.string.confirm_password_field) else stringResource(
                    R.string.password_field
                )
            )
        },
        visualTransformation = PasswordVisualTransformation()
    )
}

/**
 * Button for logging in or registering.
 * @param registering whether the user is registering or logging in.
 */
@Composable
private fun LoginButton(registering: Boolean, enabled: Boolean, onLogin: () -> Unit) {
    val gradientColors = if (enabled) {
        listOf(Blue, MediumBlue)
    } else {
        listOf(Color.Gray, Color.LightGray)
    }
    val gradient = Brush.linearGradient(colors = gradientColors)
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .background(gradient, shape = shape)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onLogin,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 12.dp),
            shape = shape,
            modifier = Modifier.defaultMinSize(minHeight = 40.dp)
        ) {
            Text(
                if (registering) stringResource(R.string.register_button)
                else stringResource(R.string.login_button),
                style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

/**
 * Button for switching between login and register.
 * @param registering whether the user is registering or logging in.
 * @param onSwitchMode the function to be called when the button is clicked.
 */
@Composable
private fun SwitchModeButton(registering: Boolean, onSwitchMode: () -> Unit) {
    Row {
        Text((if (registering) stringResource(R.string.login_account_yes) else stringResource(R.string.login_account_no)) + " ")
        Text(
            if (registering) stringResource(R.string.login_button) else stringResource(R.string.register_button),
            modifier = Modifier.clickable(onClick = onSwitchMode),
            textDecoration = TextDecoration.Underline
        )
    }
}

/**
 * Interface for the view model of the [LoginView].
 * @property username the current username.
 * @property password the current password.
 */
interface ILoginViewModel {
    var username: String
    var password: String

    /**
     * Logs in the user.
     */
    fun login()

    /**
     * Logs in the user with OIDC.
     */
    fun loginWithOIDC()

    /**
     * Registers the user.
     */
    fun register()

    /**
     * Registers the user with OIDC.
     */
    fun registerWithOIDC()
}

/**
 * View model for the [LoginView].
 * @param api the [RemoteAPI] for this view.
 * @param navController the [NavController] for this view.
 */
class LoginViewModel(private val api: RemoteAPI, private val navController: NavController) :
    ViewModel(),
    ILoginViewModel {
    override var username by mutableStateOf("")
    override var password by mutableStateOf("")

    override fun login() {
        viewModelScope.launch {
            api.login(Credentials.UsernamePassword(username, password))

            withContext(Dispatchers.Main.immediate) {
                navController.main(dontGoBack = Login)
            }
        }
    }

    override fun loginWithOIDC() {
        TODO("Not yet implemented")
    }

    override fun register() {
        viewModelScope.launch {
            api.register(Credentials.UsernamePassword(username, password))

            withContext(Dispatchers.Main.immediate) {
                navController.questionnaire(dontGoBack = Login)
            }
        }
    }

    override fun registerWithOIDC() {
        TODO("Not yet implemented")
    }
}