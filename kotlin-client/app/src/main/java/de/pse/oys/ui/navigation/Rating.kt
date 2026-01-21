package de.pse.oys.ui.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object AvailableRatings

@MainThread
fun NavController.availableRatings() = navigate(route = AvailableRatings)

@Serializable
data object Rating

@MainThread
fun NavController.rating() = navigate(route = Rating)