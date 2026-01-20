package de.pse.oys.data.api

import de.pse.oys.data.QuestionState
import de.pse.oys.data.facade.FreeTime
import de.pse.oys.data.facade.FreeTimeData
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.ModuleData
import de.pse.oys.data.facade.Step
import de.pse.oys.data.facade.Task
import de.pse.oys.data.facade.TaskData
import de.pse.oys.data.facade.UnitRatings
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.Uuid

data class Response<T>(val response: T?, val status: Int)

interface RemoteAPI {
    val isLoggedIn: Boolean
    suspend fun login(credentials: Credentials): Response<Unit>
    suspend fun register(credentials: Credentials): Response<Unit>

    suspend fun updateQuestionnaire(questions: QuestionState): Response<Unit>

    suspend fun markUnitFinished(unit: Uuid): Response<Unit>
    suspend fun moveUnitAutomatically(unit: Uuid): Response<Step>
    suspend fun moveUnit(unit: Uuid, newTime: LocalDateTime): Response<Unit>

    suspend fun queryRateable(): Response<List<Uuid>>
    suspend fun rateUnit(unit: Uuid, ratings: UnitRatings): Response<Uuid>

    suspend fun queryUnit(): Response<Map<DayOfWeek, List<Step>>>

    suspend fun queryModules(): Response<List<Module>>
    suspend fun createModule(module: ModuleData): Response<Uuid>
    suspend fun updateModule(module: Module): Response<Unit>
    suspend fun deleteModule(module: Uuid): Response<Unit>

    suspend fun queryTasks(): Response<List<Task>>
    suspend fun createTask(task: TaskData): Response<Uuid>
    suspend fun updateTask(task: Task): Response<Unit>
    suspend fun deleteTask(task: Uuid): Response<Unit>

    suspend fun queryFreeTimes(): Response<List<FreeTime>>
    suspend fun createFreeTime(freeTime: FreeTimeData): Response<Uuid>
    suspend fun updateFreeTime(freeTime: FreeTime): Response<Unit>
    suspend fun deleteFreeTime(freeTime: Uuid): Response<Unit>

    fun logout()
    suspend fun deleteAccount(): Response<Unit>
}