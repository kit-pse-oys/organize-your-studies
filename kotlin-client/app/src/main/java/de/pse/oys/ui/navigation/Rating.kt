package de.pse.oys.ui.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data object AvailableRatings

@MainThread
fun NavController.availableRatings(dontGoBack: Any? = null) = navigate(route = AvailableRatings) {
    if (dontGoBack != null) {
        popUpTo(dontGoBack) { inclusive = true }
    }
}

@Serializable
data class Rating(val step: Uuid)

@MainThread
fun NavController.rating(step: Uuid) = navigate(route = Rating(step))