package de.pse.oys.data.api

import de.pse.oys.data.QuestionState
import de.pse.oys.data.facade.FreeTime
import de.pse.oys.data.facade.FreeTimeData
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.ModuleData
import de.pse.oys.data.facade.UnitRatings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.clearAuthTokens
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
import kotlin.time.Duration
import kotlin.uuid.Uuid

data class Response<T>(val response: T?, val status: Int)

interface RemoteAPI {
    suspend fun login(credentials: Credentials): Response<Unit>
    suspend fun register(credentials: Credentials): Response<Unit>

    suspend fun updateQuestionnaire(questions: QuestionState): Response<Unit>

    suspend fun markUnitFinished(unit: Uuid, actualDuration: Duration): Response<Unit>
    suspend fun moveUnitAutomatically(unit: Uuid): Response<RemoteStep>
    suspend fun moveUnit(unit: Uuid, newTime: LocalDateTime): Response<Unit>

    suspend fun queryRateable(): Response<List<Uuid>>
    suspend fun rateUnit(unit: Uuid, ratings: UnitRatings): Response<Unit>
    suspend fun rateUnitMissed(unit: Uuid): Response<Unit>

    suspend fun updatePlan(): Response<Unit>
    suspend fun queryUnits(): Response<Map<DayOfWeek, List<RemoteStep>>>

    suspend fun queryModules(): Response<List<Module>>
    suspend fun createModule(module: ModuleData): Response<Uuid>
    suspend fun updateModule(module: Module): Response<Unit>
    suspend fun deleteModule(module: Uuid): Response<Unit>

    suspend fun queryTasks(): Response<List<RemoteTask>>
    suspend fun createTask(task: RemoteTaskData): Response<Uuid>
    suspend fun updateTask(task: RemoteTask): Response<Unit>
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

        private fun HttpResponse.statusResponse(): Response<Unit> {
            if (status.isSuccess()) {
                return Response(Unit, status.value)
            }
            return Response(null, status.value)
        }

        private suspend fun HttpResponse.idResponse(): Response<Uuid> {
            if (status.isSuccess()) {
                return Response(body<Routes.Id>().id, status.value)
            }
            return Response(null, status.value)
        }

        private suspend inline fun <reified T> HttpResponse.responseAs(): Response<T> {
            if (status.isSuccess()) {
                return Response(body<T>(), status.value)
            }
            return Response(null, status.value)
        }
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

    private suspend fun requestTokens(path: String, credentials: Credentials): Response<Unit> {
        val response = client.post(serverUrl) {
            url {
                apiPath(path)
            }

            contentType(ContentType.Application.Json)
            setBody(credentials)
        }

        if (response.status.isSuccess()) {
            session.setSession(response.body())
            client.clearAuthTokens()
        }

        return response.statusResponse()
    }

    override suspend fun register(credentials: Credentials): Response<Unit> =
        withContext(Dispatchers.IO) {
            requestTokens("users", credentials)
        }

    override suspend fun login(credentials: Credentials): Response<Unit> =
        withContext(Dispatchers.IO) {
            requestTokens("auth/login", credentials)
        }

    override fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            session.setSession(null)
            client.clearAuthTokens()
        }
    }

    override suspend fun deleteAccount(): Response<Unit> =
        withContext(Dispatchers.IO) {
            client.delete(serverUrl) {
                url {
                    apiPath("users")
                }

                // No body, since the authorized user should be deleted
            }.statusResponse()
        }

    override suspend fun updateQuestionnaire(questions: QuestionState): Response<Unit> =
        withContext(Dispatchers.IO) {
            client.put(serverUrl) {
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

    override suspend fun markUnitFinished(unit: Uuid, actualDuration: Duration): Response<Unit> =
        withContext(Dispatchers.IO) {
            client.post(serverUrl) {
                url {
                    apiPath("plan/units/finished")
                }

                contentType(ContentType.Application.Json)
                setBody(Routes.Unit(unit, actualDuration = actualDuration.inWholeMinutes.toInt()))
            }.statusResponse()
        }

    override suspend fun moveUnitAutomatically(unit: Uuid): Response<RemoteStep> =
        withContext(Dispatchers.IO) {
            client.post(serverUrl) {
                url {
                    apiPath("plan/units/moveAuto")
                }

                contentType(ContentType.Application.Json)
                setBody(Routes.Unit(unit))
            }.responseAs()
        }

    override suspend fun moveUnit(
        unit: Uuid,
        newTime: LocalDateTime
    ): Response<Unit> = withContext(Dispatchers.IO) {
        client.post(serverUrl) {
            url {
                apiPath("plan/units/move")
            }

            contentType(ContentType.Application.Json)
            setBody(Routes.Unit(unit, newTime = newTime))
        }.statusResponse()
    }

    override suspend fun queryRateable(): Response<List<Uuid>> = withContext(Dispatchers.IO) {
        client.get(serverUrl) {
            url {
                apiPath("plan/units/ratings")
            }
        }.responseAs()
    }

    override suspend fun rateUnit(
        unit: Uuid,
        ratings: UnitRatings
    ): Response<Unit> = withContext(Dispatchers.IO) {
        client.post(serverUrl) {
            url {
                apiPath("plan/units/ratings")
            }

            contentType(ContentType.Application.Json)
            setBody(Routes.Unit(unit, ratings = ratings))
        }.statusResponse()
    }

    override suspend fun rateUnitMissed(
        unit: Uuid
    ): Response<Unit> = withContext(Dispatchers.IO) {
        client.post(serverUrl) {
            url {
                apiPath("plan/units/ratings/missed")
            }

            contentType(ContentType.Application.Json)
            setBody(Routes.Id(unit))
        }.statusResponse()
    }

    override suspend fun updatePlan(): Response<Unit> = withContext(Dispatchers.IO) {
        client.put(serverUrl) {
            url {
                apiPath("plan")
            }
        }.statusResponse()
    }

    override suspend fun queryUnits(): Response<Map<DayOfWeek, List<RemoteStep>>> =
        withContext(Dispatchers.IO) {
            client.get(serverUrl) {
                url {
                    apiPath("plan")
                }
            }.responseAs()
        }

    override suspend fun queryModules(): Response<List<Module>> = withContext(Dispatchers.IO) {
        client.get(serverUrl) {
            url {
                apiPath("modules")
            }
        }.responseAs()
    }

    override suspend fun createModule(module: ModuleData): Response<Uuid> =
        withContext(Dispatchers.IO) {
            client.post(serverUrl) {
                url {
                    apiPath("modules")
                }

                contentType(ContentType.Application.Json)
                setBody(module)
            }.idResponse()
        }

    override suspend fun updateModule(module: Module): Response<Unit> =
        withContext(Dispatchers.IO) {
            client.put(serverUrl) {
                url {
                    apiPath("modules")
                }

                contentType(ContentType.Application.Json)
                setBody(module)
            }.statusResponse()
        }

    override suspend fun deleteModule(module: Uuid): Response<Unit> = withContext(Dispatchers.IO) {
        client.delete(serverUrl) {
            url {
                apiPath("modules")
            }

            contentType(ContentType.Application.Json)
            setBody(Routes.Id(module))
        }.statusResponse()
    }

    override suspend fun queryTasks(): Response<List<RemoteTask>> = withContext(Dispatchers.IO) {
        client.get(serverUrl) {
            url {
                apiPath("tasks")
            }
        }.responseAs()
    }

    override suspend fun createTask(task: RemoteTaskData): Response<Uuid> =
        withContext(Dispatchers.IO) {
            client.post(serverUrl) {
                url {
                    apiPath("tasks")
                }

                contentType(ContentType.Application.Json)
                setBody(task)
            }.idResponse()
        }

    override suspend fun updateTask(task: RemoteTask): Response<Unit> =
        withContext(Dispatchers.IO) {
            client.put(serverUrl) {
                url {
                    apiPath("tasks")
                }

                contentType(ContentType.Application.Json)
                setBody(task)
            }.statusResponse()
        }

    override suspend fun deleteTask(task: Uuid): Response<Unit> = withContext(Dispatchers.IO) {
        client.delete(serverUrl) {
            url {
                apiPath("tasks")
            }

            contentType(ContentType.Application.Json)
            setBody(Routes.Id(task))
        }.statusResponse()
    }

    override suspend fun queryFreeTimes(): Response<List<FreeTime>> = withContext(Dispatchers.IO) {
        client.get(serverUrl) {
            url {
                apiPath("freeTimes")
            }
        }.responseAs()
    }

    override suspend fun createFreeTime(freeTime: FreeTimeData): Response<Uuid> =
        withContext(Dispatchers.IO) {
            client.post(serverUrl) {
                url {
                    apiPath("freeTimes")
                }

                contentType(ContentType.Application.Json)
                setBody(freeTime)
            }.idResponse()
        }

    override suspend fun updateFreeTime(freeTime: FreeTime): Response<Unit> =
        withContext(Dispatchers.IO) {
            client.put(serverUrl) {
                url {
                    apiPath("freeTimes")
                }

                contentType(ContentType.Application.Json)
                setBody(freeTime)
            }.statusResponse()
        }

    override suspend fun deleteFreeTime(freeTime: Uuid): Response<Unit> =
        withContext(Dispatchers.IO) {
            client.delete(serverUrl) {
                url {
                    apiPath("freeTimes")
                }

                contentType(ContentType.Application.Json)
                setBody(Routes.Id(freeTime))
            }.statusResponse()
        }
}