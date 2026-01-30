package de.pse.oys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import de.pse.oys.data.api.RemoteClient
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.properties.Darkmode
import de.pse.oys.data.properties.DataStoreProperties
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme
import de.pse.oys.ui.view.EntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val properties = DataStoreProperties(this)
        val api = RemoteClient("https://kit-pse-oys-backend.ka.bw-cloud-instance.org", properties)
        val model = ModelFacade()

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
                    startWithLogin = !loggedIn
                )
            }
        }
    }
}