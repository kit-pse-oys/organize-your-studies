package de.pse.oys.ui.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object Menu

@MainThread
fun NavController.menu() = navigate(route = Menu)

@Serializable
data object MyModules

@MainThread
fun NavController.myModules() = navigate(route = MyModules)

@Serializable
data object MyTasks

@MainThread
fun NavController.myTasks() = navigate(route = MyTasks)

@Serializable
data object MyFreeTimes

@MainThread
fun NavController.myFreeTimes() = navigate(route = MyFreeTimes)

@Serializable
data object AccountSettings

@MainThread
fun NavController.accountSettings() = navigate(route = AccountSettings)