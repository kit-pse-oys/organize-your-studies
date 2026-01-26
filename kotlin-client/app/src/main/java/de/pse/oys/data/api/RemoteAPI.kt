package de.pse.oys.data.api

import de.pse.oys.data.QuestionState
import de.pse.oys.data.Questions
import de.pse.oys.data.facade.FreeTime
import de.pse.oys.data.facade.FreeTimeData
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.ModuleData
import de.pse.oys.data.facade.Step
import de.pse.oys.data.facade.Task
import de.pse.oys.data.facade.TaskData
import de.pse.oys.data.facade.UnitRatings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.DefaultJson
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.uuid.Uuid

data class Response<T>(val response: T?, val status: Int)

interface RemoteAPI {
    suspend fun login(credentials: Credentials): Response<Unit>
    suspend fun register(credentials: Credentials): Response<Unit>

    suspend fun updateQuestionnaire(questions: QuestionState): Response<Unit>

    suspend fun markUnitFinished(unit: Uuid): Response<Unit>
    suspend fun moveUnitAutomatically(unit: Uuid): Response<RemoteStep>
    suspend fun moveUnit(unit: Uuid, newTime: LocalDateTime): Response<Unit>

    suspend fun queryRateable(): Response<List<Uuid>>
    suspend fun rateUnit(unit: Uuid, ratings: UnitRatings): Response<Unit>

    suspend fun updatePlan(): Response<Unit>
    suspend fun queryUnits(): Response<Map<DayOfWeek, List<RemoteStep>>>

    suspend fun queryModules(): Response<List<Module>>
    suspend fun createModule(module: ModuleData): Response<Uuid>
    suspend fun updateModule(module: Module): Response<Unit>
    suspend fun deleteModule(module: Uuid): Response<Unit>

    suspend fun queryTasks(): Response<List<RemoteTask>>
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

class RemoteClient
internal constructor(
    private val serverUrl: String,
    private val session: SessionStore,
    engine: HttpClientEngine
) : RemoteAPI {
    companion object {
        operator fun invoke(serverUrl: String, session: SessionStore) =
            RemoteClient(serverUrl, session, OkHttp.create())

        private fun URLBuilder.apiPath(path: String) = appendPathSegments("api/v1", path)

        private fun HttpResponse.statusResponse() = Response(Unit, status.value)

        private suspend fun HttpResponse.idResponse() = Response(body<Routes.Id>().id, status.value)

        private suspend inline fun <reified T> HttpResponse.responseAs() =
            Response(body<T>(), status.value)
    }

    private val client = HttpClient(engine) {
        install(Auth) {
            bearer {
                loadTokens {
                    session.getSession()?.run {
                        BearerTokens(accessToken, refreshToken)
                    }
                }

                refreshTokens {
                    val refreshToken = oldTokens?.refreshToken!!
                    val tokenInfo = client.post(serverUrl) {
                        url {
                            apiPath("auth/refresh")
                        }

                        contentType(ContentType.Application.Json)
                        setBody(Routes.Auth.RefreshToken(refreshToken))

                        markAsRefreshTokenRequest()
                    }.body<Routes.Auth.AccessToken>()
                    BearerTokens(tokenInfo.accessToken, refreshToken)
                }
            }
        }

        install(ContentNegotiation) {
            json(Json(from = DefaultJson) {
                explicitNulls = false
            })
        }
    }

    private suspend fun requestTokens(path: String, credentials: Credentials): Response<Unit> =
        withContext(Dispatchers.IO) {
            val response = client.post(serverUrl) {
                url {
                    apiPath(path)
                }

                contentType(ContentType.Application.Json)
                setBody(credentials)
            }

            if (response.status.isSuccess()) {
                session.setSession(response.body())
                client.authProvider<BearerAuthProvider>()!!.clearToken()
            }

            response.statusResponse()
        }

    override suspend fun register(credentials: Credentials): Response<Unit> {
        return requestTokens("users", credentials)
    }

    override suspend fun login(credentials: Credentials): Response<Unit> {
        return requestTokens("auth/login", credentials)
    }

    override fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            session.setSession(null)
        }
    }

    override suspend fun deleteAccount(): Response<Unit> {
        return client.delete(serverUrl) {
            url {
                apiPath("users")
            }

            // No body, since the authorized user should be deleted
        }.statusResponse()
    }

    override suspend fun updateQuestionnaire(questions: QuestionState): Response<Unit> {
        return client.put(serverUrl) {
            url {
                apiPath("questionnaire")
            }

            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                for (question in questions.questions) { // Use <questions.questions> instead of <Questions> for Testability
                    put(question.id, buildJsonObject {
                        for (answer in question.answers) {
                            put(answer.id, questions.selected(question, answer))
                        }
                    })
                }
            })
        }.statusResponse()
    }

    override suspend fun markUnitFinished(unit: Uuid): Response<Unit> {
        return client.post(serverUrl) {
            url {
                apiPath("plan/units")
            }

            contentType(ContentType.Application.Json)
            setBody(Routes.Unit(unit, finished = true))
        }.statusResponse()
    }

    override suspend fun moveUnitAutomatically(unit: Uuid): Response<RemoteStep> {
        return client.post(serverUrl) {
            url {
                apiPath("plan/units")
            }

            contentType(ContentType.Application.Json)
            setBody(Routes.Unit(unit, automaticNewTime = true))
        }.responseAs()
    }

    override suspend fun moveUnit(
        unit: Uuid,
        newTime: LocalDateTime
    ): Response<Unit> {
        return client.post(serverUrl) {
            url {
                apiPath("plan/units")
            }

            contentType(ContentType.Application.Json)
            setBody(Routes.Unit(unit, newTime = newTime))
        }.statusResponse()
    }

    override suspend fun queryRateable(): Response<List<Uuid>> {
        return client.get(serverUrl) {
            url {
                apiPath("plan/units/rateable")
            }
        }.responseAs()
    }

    override suspend fun rateUnit(
        unit: Uuid,
        ratings: UnitRatings
    ): Response<Unit> {
        return client.post(serverUrl) {
            url {
                apiPath("plan/units/ratings")
            }

            contentType(ContentType.Application.Json)
            setBody(Routes.Unit(unit, ratings = ratings))
        }.statusResponse()
    }

    override suspend fun updatePlan(): Response<Unit> {
        return client.put(serverUrl) {
            url {
                apiPath("plan")
            }
        }.statusResponse()
    }

    override suspend fun queryUnits(): Response<Map<DayOfWeek, List<RemoteStep>>> {
        return client.get(serverUrl) {
            url {
                apiPath("plan")
            }
        }.responseAs()
    }

    override suspend fun queryModules(): Response<List<Module>> {
        return client.get(serverUrl) {
            url {
                apiPath("modules")
            }
        }.responseAs()
    }

    override suspend fun createModule(module: ModuleData): Response<Uuid> {
        return client.post(serverUrl) {
            url {
                apiPath("modules")
            }

            contentType(ContentType.Application.Json)
            setBody(module)
        }.idResponse()
    }

    override suspend fun updateModule(module: Module): Response<Unit> {
        return client.put(serverUrl) {
            url {
                apiPath("modules")
            }

            contentType(ContentType.Application.Json)
            setBody(module)
        }.statusResponse()
    }

    override suspend fun deleteModule(module: Uuid): Response<Unit> {
        return client.delete(serverUrl) {
            url {
                apiPath("modules")
            }

            contentType(ContentType.Application.Json)
            setBody(Routes.Id(module))
        }.statusResponse()
    }

    override suspend fun queryTasks(): Response<List<RemoteTask>> {
        return client.get(serverUrl) {
            url {
                apiPath("tasks")
            }
        }.responseAs()
    }

    override suspend fun createTask(task: TaskData): Response<Uuid> {
        return client.post(serverUrl) {
            url {
                apiPath("tasks")
            }

            contentType(ContentType.Application.Json)
            setBody(task)
        }.idResponse()
    }

    override suspend fun updateTask(task: Task): Response<Unit> {
        return client.put(serverUrl) {
            url {
                apiPath("tasks")
            }

            contentType(ContentType.Application.Json)
            setBody(task)
        }.statusResponse()
    }

    override suspend fun deleteTask(task: Uuid): Response<Unit> {
        return client.delete(serverUrl) {
            url {
                apiPath("tasks")
            }

            contentType(ContentType.Application.Json)
            setBody(Routes.Id(task))
        }.statusResponse()
    }

    override suspend fun queryFreeTimes(): Response<List<FreeTime>> {
        return client.get(serverUrl) {
            url {
                apiPath("freeTimes")
            }
        }.responseAs()
    }

    override suspend fun createFreeTime(freeTime: FreeTimeData): Response<Uuid> {
        return client.post(serverUrl) {
            url {
                apiPath("freeTimes")
            }

            contentType(ContentType.Application.Json)
            setBody(freeTime)
        }.idResponse()
    }

    override suspend fun updateFreeTime(freeTime: FreeTime): Response<Unit> {
        return client.put(serverUrl) {
            url {
                apiPath("freeTimes")
            }

            contentType(ContentType.Application.Json)
            setBody(freeTime)
        }.statusResponse()
    }

    override suspend fun deleteFreeTime(freeTime: Uuid): Response<Unit> {
        return client.delete(serverUrl) {
            url {
                apiPath("freeTimes")
            }

            contentType(ContentType.Application.Json)
            setBody(Routes.Id(freeTime))
        }.statusResponse()
    }
}