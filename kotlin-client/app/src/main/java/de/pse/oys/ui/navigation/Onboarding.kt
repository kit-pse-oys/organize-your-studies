package de.pse.oys.ui.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object Login

@MainThread
fun NavController.login(dontGoBack: Any? = null) = navigate(route = Login) {
    if (dontGoBack != null) {
        popUpTo(dontGoBack) { inclusive = true }
    }
}

@Serializable
data object Register

@MainThread
fun NavController.register() = navigate(route = Register)

@Serializable
data class Questionnaire(val firstTime: Boolean)

@MainThread
fun NavController.questionnaire(dontGoBack: Any? = null) = navigate(route = Questionnaire(true)) {
    if (dontGoBack != null) {
        popUpTo(dontGoBack) { inclusive = true }
    }
}

@MainThread
fun NavController.editQuestionnaire() = navigate(route = Questionnaire(false))