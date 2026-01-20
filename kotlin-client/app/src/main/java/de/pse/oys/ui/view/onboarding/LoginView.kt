package de.pse.oys.ui.view.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LoginView(viewModel: ILoginViewModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        TODO()
    }
}

interface ILoginViewModel {
    var username: String
    var password: String

    fun login()
    fun loginWithOIDC()

    fun register()
    fun registerWithOIDC()
}