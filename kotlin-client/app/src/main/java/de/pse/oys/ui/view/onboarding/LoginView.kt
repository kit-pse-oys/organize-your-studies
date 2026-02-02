package de.pse.oys.ui.view.onboarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.pse.oys.R
import de.pse.oys.data.api.Credentials
import de.pse.oys.data.api.OIDCType
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.defaultHandleError
import de.pse.oys.ui.navigation.Login
import de.pse.oys.ui.navigation.main
import de.pse.oys.ui.navigation.questionnaire
import de.pse.oys.ui.theme.Blue
import de.pse.oys.ui.theme.LightBlue
import de.pse.oys.ui.theme.MediumBlue
import de.pse.oys.ui.util.ViewHeader
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration

/**
 * View for the login screen.
 * Allows the user to login or register and switches between the two.
 * Login/ Register Button only enabled when username and password are not empty and passwords match.
 * @param viewModel the [ILoginViewModel] for this view.
 */
@Composable
fun LoginView(viewModel: ILoginViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val authLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                viewModel.handleOIDCResult(result.data)
            } else {
                viewModel.handleOIDCResult(null)
            }
        }

    LaunchedEffect(viewModel.error) {
        if (viewModel.error) {
            snackbarHostState.showSnackbar("Something went wrong...")
            viewModel.error = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
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

            GoogleLoginButton(registering) {
                if (registering) viewModel.registerWithOIDC(OIDCType.GOOGLE, authLauncher)
                else viewModel.loginWithOIDC(OIDCType.GOOGLE, authLauncher)
            }
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
 * Button for logging in or registering with Google.
 * @param registering whether the user is registering or logging in.
 * @param onClick the function to be called when the button is clicked.
 */
@Composable
fun GoogleLoginButton(registering: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(
            if (registering) stringResource(R.string.register_with_google_button)
            else stringResource(R.string.sign_in_with_google_button),
            fontWeight = FontWeight.Bold
        )
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
    var error: Boolean

    var username: String
    var password: String

    /**
     * Logs in the user.
     */
    fun login()

    /**
     * Logs in the user with OIDC.
     */
    fun loginWithOIDC(type: OIDCType, launcher: ActivityResultLauncher<Intent>)

    /**
     * Registers the user.
     */
    fun register()

    /**
     * Registers the user with OIDC.
     */
    fun registerWithOIDC(type: OIDCType, launcher: ActivityResultLauncher<Intent>)

    /**
     * Handles the result of the OIDC login.
     */
    fun handleOIDCResult(intent: Intent?)
}

/**
 * View model for the [LoginView].
 * @param api the [RemoteAPI] for this view.
 * @param navController the [NavController] for this view.
 */
class LoginViewModel(
    private val api: RemoteAPI,
    context: Context,
    private val navController: NavController
) :
    ViewModel(),
    ILoginViewModel {
    companion object {
        private val GOOGLE_AUTH_CONFIG = AuthorizationServiceConfiguration(
            "https://accounts.google.com/o/oauth2/v2/auth".toUri(),
            "https://www.googleapis.com/oauth2/v4/token".toUri(),
        )
        private const val CLIENT_ID = "549888352558-jpn57b44j23ulud1vpmcqn7sbr8rvcd7"
        private val REDIRECT_URI =
            "com.googleusercontent.apps.$CLIENT_ID:/oauth2redirect".toUri()
    }

    private val authService = AuthorizationService(context)
    private var pendingAuthRequest: CompletableDeferred<String?>? = null

    override fun onCleared() {
        super.onCleared()
        authService.dispose()
    }

    private suspend fun getOIDCToken(
        oidcType: OIDCType,
        launcher: ActivityResultLauncher<Intent>
    ): String? {
        val deferred = CompletableDeferred<String?>()
        pendingAuthRequest = deferred

        val authRequest = when (oidcType) {
            OIDCType.GOOGLE -> AuthorizationRequest.Builder(
                GOOGLE_AUTH_CONFIG,
                CLIENT_ID,
                "id_token",
                REDIRECT_URI
            ).setScopes("openid", "profile", "email").build()
        }

        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        launcher.launch(authIntent)

        return deferred.await()
    }

    override fun handleOIDCResult(intent: Intent?) {
        val deferred = pendingAuthRequest ?: return
        pendingAuthRequest = null

        if (intent != null) {
            val response = AuthorizationResponse.fromIntent(intent)
            AuthorizationException.fromIntent(intent)?.let {
                Log.e("LoginViewModel", "Error during OIDC login", it)
            }

            if (response != null) {
                authService.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse, exception ->
                    exception?.let {
                        Log.e("LoginViewModel", "Error during OIDC token request", it)
                    }

                    if (tokenResponse != null) {
                        val token = tokenResponse.idToken
                        deferred.complete(token)
                    } else {
                        deferred.complete(null)
                        error = true
                    }
                }

                return
            }
        }

        deferred.complete(null)
        error = true
    }

    override var username by mutableStateOf("")
    override var password by mutableStateOf("")

    override var error: Boolean by mutableStateOf(false)

    override fun login() {
        viewModelScope.launch {
            if (api.login(Credentials.UsernamePassword(username, password))
                    .defaultHandleError(navController) { error = true } != null
            ) {
                withContext(Dispatchers.Main.immediate) {
                    navController.main(dontGoBack = Login)
                }
            }
        }
    }

    override fun loginWithOIDC(type: OIDCType, launcher: ActivityResultLauncher<Intent>) {
        viewModelScope.launch {
            val token = getOIDCToken(type, launcher)
            if (token != null && api.login(Credentials.OIDC(token, type))
                    .defaultHandleError(navController) { error = true } != null
            ) {
                withContext(Dispatchers.Main.immediate) {
                    navController.main(dontGoBack = Login)
                }
            }
        }
    }

    override fun register() {
        viewModelScope.launch {
            if (api.register(Credentials.UsernamePassword(username, password))
                    .defaultHandleError(navController) { error = true } != null
            ) {
                withContext(Dispatchers.Main.immediate) {
                    navController.questionnaire(dontGoBack = Login)
                }
            }
        }
    }

    override fun registerWithOIDC(type: OIDCType, launcher: ActivityResultLauncher<Intent>) {
        viewModelScope.launch {
            val token = getOIDCToken(type, launcher)
            if (token != null && api.register(Credentials.OIDC(token, type))
                    .defaultHandleError(navController) { error = true } != null
            ) {
                withContext(Dispatchers.Main.immediate) {
                    navController.questionnaire(dontGoBack = Login)
                }
            }
        }
    }
}