package de.pse.oys.data

import android.util.Log
import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.RemoteExamTaskData
import de.pse.oys.data.api.RemoteOtherTaskData
import de.pse.oys.data.api.RemoteSubmissionTaskData
import de.pse.oys.data.api.Response
import de.pse.oys.data.facade.ExamTaskData
import de.pse.oys.data.facade.FreeTimeData
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.ModuleData
import de.pse.oys.data.facade.OtherTaskData
import de.pse.oys.data.facade.StepData
import de.pse.oys.data.facade.SubmissionTaskData
import de.pse.oys.data.facade.Task
import de.pse.oys.data.facade.TaskData
import de.pse.oys.ui.navigation.Main
import de.pse.oys.ui.navigation.login
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import kotlin.uuid.Uuid

suspend fun ModelFacade.ensureModules(api: RemoteAPI): Response<Map<Uuid, ModuleData>> {
    if (modules == null) {
        val response = api.queryModules()
        if (response.status != HttpStatusCode.OK.value) {
            return Response(null, response.status)
        }
        modules = response.response?.associate { it.id to it.data }
    }

    return Response(modules, HttpStatusCode.OK.value)
}

suspend fun ModelFacade.ensureTasks(api: RemoteAPI): Response<Map<Uuid, TaskData>> {
    if (tasks == null) {
        val moduleResponse = ensureModules(api)
        if (moduleResponse.status != HttpStatusCode.OK.value) {
            return Response(null, moduleResponse.status)
        }
        val modules = moduleResponse.response ?: error("No response with Status 200")

        val response = api.queryTasks()
        if (response.status != HttpStatusCode.OK.value) {
            return Response(null, response.status)
        }
        tasks = response.response?.associate { (task, id) ->
            id to when (task) {
                is RemoteExamTaskData -> ExamTaskData(
                    task.title,
                    Module(modules[task.module]!!, task.module),
                    task.weeklyTimeLoad,
                    task.examDate
                )

                is RemoteSubmissionTaskData -> SubmissionTaskData(
                    task.title,
                    Module(modules[task.module]!!, task.module),
                    task.weeklyTimeLoad,
                    task.firstDate,
                    task.cycle
                )

                is RemoteOtherTaskData -> OtherTaskData(
                    task.title,
                    Module(modules[task.module]!!, task.module),
                    task.weeklyTimeLoad,
                    task.start,
                    task.end
                )
            }
        }
    }

    return Response(tasks, HttpStatusCode.OK.value)
}

suspend fun ModelFacade.ensureFreeTimes(api: RemoteAPI): Response<Map<Uuid, FreeTimeData>> {
    if (freeTimes == null) {
        val response = api.queryFreeTimes()
        if (response.status != HttpStatusCode.OK.value) {
            return Response(null, response.status)
        }
        freeTimes = response.response?.associate { it.id to it.data }
    }

    return Response(freeTimes, HttpStatusCode.OK.value)
}

suspend fun ModelFacade.ensureUnits(api: RemoteAPI): Response<Map<DayOfWeek, Map<Uuid, StepData>>> {
    if (steps == null) {
        val taskResponse = ensureTasks(api)
        if (taskResponse.status != HttpStatusCode.OK.value) {
            return Response(null, taskResponse.status)
        }
        val tasks = taskResponse.response ?: error("No response with Status 200")

        val response = api.queryUnits()
        if (response.status != HttpStatusCode.OK.value) {
            return Response(null, response.status)
        }
        steps = response.response?.mapValues { (_, units) ->
            units.associate { (unit, id) ->
                id to StepData(
                    Task(tasks[unit.task]!!, unit.task),
                    unit.date,
                    unit.start,
                    unit.end
                )
            }
        }
    }

    return Response(steps, HttpStatusCode.OK.value)
}

suspend fun <T> Response<T>.defaultHandleError(navController: NavController, error: () -> Unit): T? {
    if (status != HttpStatusCode.OK.value) {
        if (status == HttpStatusCode.Unauthorized.value) {
            withContext(Dispatchers.Main.immediate) {
                navController.login(dontGoBack = Main)
            }
        } else {
            Log.e("RemoteAPI", "Error with Status $status")
            error()
        }

        return null
    } else {
        return response ?: error("No response with Status 200")
    }
}