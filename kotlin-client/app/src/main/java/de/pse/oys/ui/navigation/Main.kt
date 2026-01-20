package de.pse.oys.ui.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object Main

@MainThread
fun NavController.main(dontGoBack: Any? = Main) = navigate(route = Main) {
    if (dontGoBack != null) {
        popUpTo(dontGoBack) { inclusive = true }
    }
}