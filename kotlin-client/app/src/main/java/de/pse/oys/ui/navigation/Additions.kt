package de.pse.oys.ui.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import de.pse.oys.data.facade.FreeTime
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.Task
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data object Additions

@MainThread
fun NavController.additions() = navigate(route = Additions)

@Serializable
data object CreateModule

@Serializable
data class EditModule(val id: String)

@MainThread
fun NavController.createModule() = navigate(route = CreateModule)

@MainThread
fun NavController.editModule(module: Module) = navigate(route = EditModule(module.id.toHexDashString()))

@Serializable
data object CreateFreeTime

@Serializable
data class EditFreeTime(val id: String)

@MainThread
fun NavController.createFreeTime() = navigate(route = CreateFreeTime)

@MainThread
fun NavController.editFreeTime(freeTime: FreeTime) = navigate(route = EditFreeTime(freeTime.id.toHexDashString()))

@Serializable
data object CreateTask

@Serializable
data class EditTask(val id: String)

@MainThread
fun NavController.createTask() = navigate(route = CreateTask)

@MainThread
fun NavController.editTask(task: Task) = navigate(route = EditTask(task.id.toHexDashString()))