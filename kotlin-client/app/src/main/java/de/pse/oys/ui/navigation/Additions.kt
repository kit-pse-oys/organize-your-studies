package de.pse.oys.ui.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import de.pse.oys.data.facade.FreeTime
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.Task
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed class Intent {
    @Serializable
    data object Create : Intent()

    @Serializable
    data class Edit(val target: Uuid) : Intent() {
        fun module(model: ModelFacade) = model.modules?.get(target)?.let { Module(it, target) }
        fun freeTime(model: ModelFacade) = model.freeTimes?.get(target)?.let { FreeTime(it, target) }
        fun task(model: ModelFacade) = model.tasks?.get(target)?.let { Task(it, target) }
    }
}

@Serializable
data object Additions

@MainThread
fun NavController.additions() = navigate(route = Additions)

@Serializable
data class CreateModule(val intent: Intent)

@MainThread
fun NavController.createModule() = navigate(route = CreateModule(Intent.Create))

@MainThread
fun NavController.editModule(module: Module) =
    navigate(route = CreateModule(Intent.Edit(module.id)))

@Serializable
data class CreateFreeTime(val intent: Intent)

@MainThread
fun NavController.createFreeTime() = navigate(route = CreateFreeTime(Intent.Create))

@MainThread
fun NavController.editFreeTime(freeTime: FreeTime) =
    navigate(route = CreateFreeTime(Intent.Edit(freeTime.id)))

@Serializable
data class CreateTask(val intent: Intent)

@MainThread
fun NavController.createTask() = navigate(route = CreateTask(Intent.Create))

@MainThread
fun NavController.editTask(task: Task) = navigate(route = CreateTask(Intent.Edit(task.id)))