package de.pse.oys

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import de.pse.oys.data.api.RemoteClient
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.properties.Darkmode
import de.pse.oys.data.properties.DataStoreProperties
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme
import de.pse.oys.ui.view.EntryPoint
import de.pse.oys.ui.view.onboarding.ILoginViewModel
import de.pse.oys.ui.view.onboarding.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var properties: DataStoreProperties
    private lateinit var api: RemoteClient
    private lateinit var model: ModelFacade

    private lateinit var testNavController: NavController

    private lateinit var loginViewModel: LoginViewModel
    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account.idToken
                    if (idToken != null) {
                        loginViewModel.loginWithGoogle(idToken)
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        properties = DataStoreProperties(this)
        api = RemoteClient("https://organizeyourstudies.tech", properties)
        model = ModelFacade()
        testNavController = NavController(this)
        loginViewModel = LoginViewModel(api, testNavController)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("549888352558-jpn57b44j23ulud1vpmcqn7sbr8rvcd7.apps.googleusercontent.com")
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val loggedIn =
            runBlocking { properties.hasSession.stateIn(lifecycleScope + Dispatchers.IO) }

        setContent {
            val darkmode by properties.darkmode.collectAsStateWithLifecycle(Darkmode.SYSTEM)
            val darkTheme = when (darkmode) {
                Darkmode.ENABLED -> true
                Darkmode.DISABLED -> false
                Darkmode.SYSTEM -> isSystemInDarkTheme()
            }

            val loggedIn by loggedIn.collectAsStateWithLifecycle()

            OrganizeYourStudiesTheme(darkTheme = darkTheme) {
                EntryPoint(
                    model = model,
                    api = api,
                    properties = properties,
                    startWithLogin = !loggedIn,
                    onGoogleLogin = {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                )
            }
        }
    }
}