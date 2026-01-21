package de.pse.oys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.pse.oys.data.api.RemoteClient
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.properties.Darkmode
import de.pse.oys.data.properties.DataStoreProperties
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme
import de.pse.oys.ui.view.EntryPoint

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val properties = DataStoreProperties(this)
        val api = RemoteClient("TODO", properties) // TODO: Server URL
        val model = ModelFacade()
        setContent {
            val darkmode by properties.darkmode.collectAsStateWithLifecycle(Darkmode.SYSTEM)
            val darkTheme = when (darkmode) {
                Darkmode.ENABLED -> true
                Darkmode.DISABLED -> false
                Darkmode.SYSTEM -> isSystemInDarkTheme()
            }

            OrganizeYourStudiesTheme(darkTheme = darkTheme) {
                EntryPoint(
                    model = model,
                    api = api,
                    properties = properties
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OrganizeYourStudiesTheme {
        Greeting("Android")
    }
}