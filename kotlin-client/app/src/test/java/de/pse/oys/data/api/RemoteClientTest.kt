package de.pse.oys.data.api

import androidx.compose.ui.graphics.Color
import de.pse.oys.data.Answer
import de.pse.oys.data.Question
import de.pse.oys.data.QuestionState
import de.pse.oys.data.facade.FreeTime
import de.pse.oys.data.facade.FreeTimeData
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.ModuleData
import de.pse.oys.data.facade.Priority
import de.pse.oys.data.facade.Rating
import de.pse.oys.data.facade.UnitRatings
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.engine.mock.toByteReadPacket
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.uuid.Uuid

class RemoteClientTest {
    private object MockSessionStore : SessionStore {
        val session = Session("ACCESS_TOKEN", "REFRESH_TOKEN")

        override suspend fun getSession(): Session {
            return session
        }

        override suspend fun setSession(session: Session?) {
            if (session != null) {
                assertEquals(this.session.accessToken, session.accessToken)
                assertEquals(this.session.refreshToken, session.refreshToken)
            }
        }

        class TrackUsage : SessionStore {
            var getSessionCalled = false
                private set
            var setSessionCalled = false
                private set
            var newSession: Session? = null
                private set

            override suspend fun getSession(): Session {
                getSessionCalled = true
                return session
            }

            override suspend fun setSession(session: Session?) {
                newSession = session
                setSessionCalled = true
            }
        }
    }

    private fun createClient(session: SessionStore = MockSessionStore): Pair<MockEngine.Queue, RemoteClient> {
        val engine = MockEngine.Queue()
        val client = RemoteClient("https://example.org/", session, engine)
        return engine to client
    }

    @Test
    fun registerBasic() {
        runBlocking {
            val sessionStore = MockSessionStore.TrackUsage()
            val (engine, client) = createClient(sessionStore)

            engine += { request ->
                assertEquals("/api/v1/users/register", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("authType", "BASIC")
                    put("username", "USERNAME")
                    put("password", "PASSWORD")
                }, json)

                respond(
                    buildJsonObject {
                        put("accessToken", MockSessionStore.session.accessToken)
                        put("refreshToken", MockSessionStore.session.refreshToken)
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.register(Credentials.UsernamePassword("USERNAME", "PASSWORD"))
            assertEquals(200, response.status)
            assertTrue(sessionStore.setSessionCalled)
            assertEquals(MockSessionStore.session, sessionStore.newSession)
        }
    }

    @Test
    fun registerOIDC() {
        runBlocking {
            val sessionStore = MockSessionStore.TrackUsage()
            val (engine, client) = createClient(sessionStore)

            engine += { request ->
                assertEquals("/api/v1/users/register", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("authType", "OIDC")
                    put("externalToken", "TOKEN")
                    put("provider", "GOOGLE")
                }, json)

                respond(
                    buildJsonObject {
                        put("accessToken", MockSessionStore.session.accessToken)
                        put("refreshToken", MockSessionStore.session.refreshToken)
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.register(Credentials.OIDC("TOKEN", OIDCType.GOOGLE))
            assertEquals(200, response.status)
            assertTrue(sessionStore.setSessionCalled)
            assertEquals(MockSessionStore.session, sessionStore.newSession)
        }
    }

    @Test
    fun loginBasic() {
        runBlocking {
            val sessionStore = MockSessionStore.TrackUsage()
            val (engine, client) = createClient(sessionStore)

            engine += { request ->
                assertEquals("/api/v1/users/login", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("authType", "BASIC")
                    put("username", "USERNAME")
                    put("password", "PASSWORD")
                }, json)

                respond(
                    buildJsonObject {
                        put("accessToken", MockSessionStore.session.accessToken)
                        put("refreshToken", MockSessionStore.session.refreshToken)
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.login(Credentials.UsernamePassword("USERNAME", "PASSWORD"))
            assertEquals(200, response.status)
            assertTrue(sessionStore.setSessionCalled)
            assertEquals(MockSessionStore.session, sessionStore.newSession)
        }
    }

    @Test
    fun loginOIDC() {
        runBlocking {
            val sessionStore = MockSessionStore.TrackUsage()
            val (engine, client) = createClient(sessionStore)

            engine += { request ->
                assertEquals("/api/v1/users/login", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("authType", "OIDC")
                    put("externalToken", "TOKEN")
                    put("provider", "GOOGLE")
                }, json)

                respond(
                    buildJsonObject {
                        put("accessToken", MockSessionStore.session.accessToken)
                        put("refreshToken", MockSessionStore.session.refreshToken)
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.login(Credentials.OIDC("TOKEN", OIDCType.GOOGLE))
            assertEquals(200, response.status)
            assertTrue(sessionStore.setSessionCalled)
            assertEquals(MockSessionStore.session, sessionStore.newSession)
        }
    }

    @Test
    fun logout() {
        runBlocking {
            val sessionStore = MockSessionStore.TrackUsage()
            val (_, client) = createClient(sessionStore)

            client.logout()
            delay(100L)
            assertTrue(sessionStore.setSessionCalled)
            assertNull(sessionStore.newSession)
        }
    }

    @Test
    fun deleteAccount() {
        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/users", request.url.encodedPath)
                assertEquals(HttpMethod.Delete, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                respondOk()
            }
            val response = client.deleteAccount()
            assertEquals(200, response.status)
        }
    }

    @Test
    fun updateQuestionnaire() {
        val answer1 = Answer("answer1", {""})
        val answer2 = Answer("answer2", {""})
        val answer3 = Answer("answer3", {""})
        val question1 = Question("question1", {""}, true, listOf(answer1, answer2, answer3))
        val question2 = Question("question2", {""}, false, listOf(answer1, answer2, answer3))
        val question3 = Question("question3", {""}, true, listOf(answer1, answer2, answer3))
        val questions = QuestionState(questions = listOf(question1, question2, question3))
        questions.select(question1, answer2)
        questions.select(question1, answer3)
        questions.select(question2, answer1)
        questions.select(question3, answer1)
        questions.select(question3, answer2)

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/questionnaire", request.url.encodedPath)
                assertEquals(HttpMethod.Put, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("question1", buildJsonObject {
                        put("answer1", false)
                        put("answer2", true)
                        put("answer3", true)
                    })
                    put("question2", buildJsonObject {
                        put("answer1", true)
                        put("answer2", false)
                        put("answer3", false)
                    })
                    put("question3", buildJsonObject {
                        put("answer1", true)
                        put("answer2", true)
                        put("answer3", false)
                    })
                }, json)

                respondOk()
            }
            val response = client.updateQuestionnaire(questions)
            assertEquals(200, response.status)
        }
    }

    @Test
    fun markUnitFinished() {
        val step1 = Uuid.random()
        val duration = 100.toDuration(DurationUnit.MINUTES)

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/plan/units/finished", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("id", step1.toHexDashString())
                    put("actualDuration", duration.inWholeMinutes)
                }, json)

                respondOk()
            }
            val response = client.markUnitFinished(step1, duration)
            assertEquals(200, response.status)
        }
    }

    @Test
    fun moveUnitAutomatically() {
        val step1 = Uuid.random()
        val task1 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/plan/units/moveAuto", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("id", step1.toHexDashString())
                }, json)

                respond(
                    buildJsonObject {
                        put("id", step1.toHexDashString())
                        put("data", buildJsonObject {
                            put("task", task1.toHexDashString())
                            put("date", "2026-01-01")
                            put("start", "00:00")
                            put("end", "01:00")
                        })
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.moveUnitAutomatically(step1)
            assertEquals(200, response.status)
            assertEquals(
                RemoteStep(
                    RemoteStepData(
                        task1,
                        LocalDate(2026, 1, 1),
                        LocalTime(0, 0, 0),
                        LocalTime(1, 0, 0)
                    ), step1
                ), response.response
            )
        }
    }

    @Test
    fun moveUnit() {
        val step1 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/plan/units/move", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("id", step1.toHexDashString())
                    put("newTime", "2026-01-01T00:00")
                }, json)

                respondOk()
            }
            val response = client.moveUnit(step1, LocalDateTime(2026, 1, 1, 0, 0))
            assertEquals(200, response.status)
        }
    }

    @Test
    fun queryRateable() {
        val uuid1 = Uuid.random()
        val uuid2 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/plan/units/ratings", request.url.encodedPath)
                assertEquals(HttpMethod.Get, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                respond(
                    buildJsonArray {
                        add(uuid1.toHexDashString())
                        add(uuid2.toHexDashString())
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.queryRateable()
            assertEquals(200, response.status)
            assertEquals(listOf(uuid1, uuid2), response.response)
        }
    }

    @Test
    fun rateUnit() {
        val step1 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/plan/units/ratings", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("id", step1.toHexDashString())
                    put("ratings", buildJsonObject {
                        put("goalCompletion", "HIGH")
                        put("duration", "LOWEST")
                        put("motivation", "MEDIUM")
                    })
                }, json)

                respondOk()
            }
            val response = client.rateUnit(
                step1, UnitRatings(
                    goalCompletion = Rating.HIGH,
                    duration = Rating.LOWEST,
                    motivation = Rating.MEDIUM
                )
            )
            assertEquals(200, response.status)
        }
    }

    @Test
    fun rateUnitMissed() {
        val step1 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/plan/units/ratings/missed", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("id", step1.toHexDashString())
                }, json)

                respondOk()
            }
            val response = client.rateUnitMissed(step1)
            assertEquals(200, response.status)
        }
    }

    @Test
    fun updatePlan() {
        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/plan", request.url.encodedPath)
                assertEquals(HttpMethod.Put, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                respondOk()
            }
            val response = client.updatePlan()
            assertEquals(200, response.status)
        }
    }

    @Test
    fun queryUnits() {
        val task1 = Uuid.random()
        val step1 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/plan/units", request.url.encodedPath)
                assertEquals(HttpMethod.Get, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                respond(
                    buildJsonArray {
                        add(buildJsonObject {
                            put("id", step1.toHexDashString())
                            put("data", buildJsonObject {
                                put("task", task1.toHexDashString())
                                put("date", "2026-01-01")
                                put("start", "00:00:00")
                                put("end", "01:00:00")
                            })
                        })
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.queryUnits()
            assertEquals(200, response.status)
            assertEquals(buildMap {
                put(
                    DayOfWeek.MONDAY, listOf(
                        RemoteStep(
                            RemoteStepData(
                                task1,
                                LocalDate(2026, 1, 1),
                                LocalTime(0, 0, 0),
                                LocalTime(1, 0, 0)
                            ), step1
                        )
                    )
                )
            }, response.response)
        }
    }

    @Test
    fun queryModules() {
        val module1 = Module(
            ModuleData(
                title = "Module 1",
                description = "Description 1",
                priority = Priority.MEDIUM,
                color = Color(0xFF, 0x00, 0x00)
            ), Uuid.random()
        )
        val module2 = Module(
            ModuleData(
                title = "Module 2",
                description = "Description 2",
                priority = Priority.HIGH,
                color = Color(0x00, 0xFF, 0xFF)
            ), Uuid.random()
        )

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/modules", request.url.encodedPath)
                assertEquals(HttpMethod.Get, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                respond(
                    buildJsonArray {
                        add(buildJsonObject {
                            put("id", module1.id.toHexDashString())
                            put("data", buildJsonObject {
                                put("title", "Module 1")
                                put("description", "Description 1")
                                put("priority", "NEUTRAL")
                                put("color", "#FF0000")
                            })
                        })
                        add(buildJsonObject {
                            put("id", module2.id.toHexDashString())
                            put("data", buildJsonObject {
                                put("title", "Module 2")
                                put("description", "Description 2")
                                put("priority", "HIGH")
                                put("color", "#00FFFF")
                            })
                        })
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.queryModules()
            assertEquals(200, response.status)
            assertEquals(listOf(module1, module2), response.response)
        }
    }

    @Test
    fun createModule() {
        val uuid1 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/modules", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("title", "Module 1")
                    put("description", "Description 1")
                    put("priority", "NEUTRAL")
                    put("color", "#FF0000")
                }, json)

                respond(
                    buildJsonObject {
                        put("id", uuid1.toHexDashString())
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.createModule(
                ModuleData(
                    title = "Module 1",
                    description = "Description 1",
                    priority = Priority.MEDIUM,
                    color = Color(0xFF, 0x00, 0x00)
                )
            )
            assertEquals(200, response.status)
            assertEquals(uuid1, response.response)
        }
    }

    @Test
    fun updateModule() {
        val uuid1 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/modules", request.url.encodedPath)
                assertEquals(HttpMethod.Put, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("id", uuid1.toHexDashString())
                    put("data", buildJsonObject {
                        put("title", "Module 1")
                        put("description", "Description 1")
                        put("priority", "NEUTRAL")
                        put("color", "#FF0000")
                    })
                }, json)

                respondOk()
            }
            val response = client.updateModule(
                Module(
                    ModuleData(
                        title = "Module 1",
                        description = "Description 1",
                        priority = Priority.MEDIUM,
                        color = Color(0xFF, 0x00, 0x00)
                    ), uuid1
                )
            )
            assertEquals(200, response.status)
        }
    }

    @Test
    fun deleteModule() {
        val uuid1 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/modules", request.url.encodedPath)
                assertEquals(HttpMethod.Delete, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("id", uuid1.toHexDashString())
                }, json)

                respondOk()
            }
            val response = client.deleteModule(uuid1)
            assertEquals(200, response.status)
        }
    }

    @Test
    fun queryTasks() {
        val task1 = RemoteTask(
            RemoteExamTaskData(
                title = "Task 1",
                module = Uuid.random(),
                weeklyTimeLoad = 2,
                examDate = LocalDate(2026, 1, 1)
            ), Uuid.random()
        )
        val task2 = RemoteTask(
            RemoteSubmissionTaskData(
                title = "Task 2",
                module = Uuid.random(),
                weeklyTimeLoad = 6,
                firstDate = LocalDateTime(2026, 1, 1, 0, 0),
                cycle = 1
            ), Uuid.random()
        )
        val task3 = RemoteTask(
            RemoteOtherTaskData(
                title = "Task 3",
                module = Uuid.random(),
                weeklyTimeLoad = 3,
                start = LocalDate(2026, 1, 1),
                end = LocalDate(2026, 2, 1)
            ), Uuid.random()
        )

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/tasks", request.url.encodedPath)
                assertEquals(HttpMethod.Get, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                respond(
                    buildJsonArray {
                        add(buildJsonObject {
                            put("id", task1.id.toHexDashString())
                            put("data", buildJsonObject {
                                put("category", "exam")
                                put("title", "Task 1")
                                put("module", task1.data.module.toHexDashString())
                                put("weeklyTimeLoad", 2)
                                put("examDate", "2026-01-01")
                            })
                        })
                        add(buildJsonObject {
                            put("id", task2.id.toHexDashString())
                            put("data", buildJsonObject {
                                put("category", "submission")
                                put("title", "Task 2")
                                put("module", task2.data.module.toHexDashString())
                                put("weeklyTimeLoad", 6)
                                put("firstDate", "2026-01-01T00:00")
                                put("cycle", 1)
                            })
                        })
                        add(buildJsonObject {
                            put("id", task3.id.toHexDashString())
                            put("data", buildJsonObject {
                                put("category", "other")
                                put("title", "Task 3")
                                put("module", task3.data.module.toHexDashString())
                                put("weeklyTimeLoad", 3)
                                put("start", "2026-01-01")
                                put("end", "2026-02-01")
                            })
                        })
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.queryTasks()
            assertEquals(200, response.status)
            assertEquals(listOf(task1, task2, task3), response.response)
        }
    }

    @Test
    fun createTask() {
        val uuid1 = Uuid.random()
        val uuid2 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/tasks", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("category", "EXAM")
                    put("title", "Task 1")
                    put("module", uuid2.toHexDashString())
                    put("weeklyTimeLoad", 2)
                    put("examDate", "2026-01-01")
                }, json)

                respond(
                    buildJsonObject {
                        put("id", uuid1.toHexDashString())
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.createTask(
                RemoteExamTaskData(
                    title = "Task 1",
                    module = uuid2,
                    weeklyTimeLoad = 2,
                    examDate = LocalDate(2026, 1, 1)
                )
            )
            assertEquals(200, response.status)
            assertEquals(uuid1, response.response)
        }
    }

    @Test
    fun updateTask() {
        val uuid1 = Uuid.random()
        val uuid2 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/tasks", request.url.encodedPath)
                assertEquals(HttpMethod.Put, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("id", uuid1.toHexDashString())
                    put("data", buildJsonObject {
                        put("category", "EXAM")
                        put("title", "Task 1")
                        put("module", uuid2.toHexDashString())
                        put("weeklyTimeLoad", 2)
                        put("examDate", "2026-01-01")
                    })
                }, json)

                respondOk()
            }
            val response = client.updateTask(
                RemoteTask(
                    RemoteExamTaskData(
                        title = "Task 1",
                        module = uuid2,
                        weeklyTimeLoad = 2,
                        examDate = LocalDate(2026, 1, 1)
                    ), uuid1
                )
            )
            assertEquals(200, response.status)
        }
    }

    @Test
    fun deleteTask() {
        val uuid1 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/tasks", request.url.encodedPath)
                assertEquals(HttpMethod.Delete, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("id", uuid1.toHexDashString())
                }, json)

                respondOk()
            }
            val response = client.deleteTask(uuid1)
            assertEquals(200, response.status)
        }
    }

    @Test
    fun queryFreeTimes() {
        val freeTime1 = FreeTime(
            FreeTimeData(
                title = "FreeTime 1",
                date = LocalDate(2026, 1, 1),
                startTime = LocalTime(0, 0, 0),
                endTime = LocalTime(1, 0, 0),
                weekly = false,
            ), Uuid.random()
        )
        val freeTime2 = FreeTime(
            FreeTimeData(
                title = "FreeTime 2",
                date = LocalDate(2026, 1, 1),
                startTime = LocalTime(1, 0, 0),
                endTime = LocalTime(2, 0, 0),
                weekly = true,
            ), Uuid.random()
        )

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/freeTimes", request.url.encodedPath)
                assertEquals(HttpMethod.Get, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                respond(
                    buildJsonArray {
                        add(buildJsonObject {
                            put("id", freeTime1.id.toHexDashString())
                            put("data", buildJsonObject {
                                put("title", "FreeTime 1")
                                put("date", "2026-01-01")
                                put("startTime", "00:00:00")
                                put("endTime", "01:00:00")
                                put("weekly", false)
                            })
                        })
                        add(buildJsonObject {
                            put("id", freeTime2.id.toHexDashString())
                            put("data", buildJsonObject {
                                put("title", "FreeTime 2")
                                put("date", "2026-01-01")
                                put("startTime", "01:00:00")
                                put("endTime", "02:00:00")
                                put("weekly", true)
                            })
                        })
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.queryFreeTimes()
            assertEquals(200, response.status)
            assertEquals(listOf(freeTime1, freeTime2), response.response)
        }
    }

    @Test
    fun createFreeTime() {
        val uuid1 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/freeTimes", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("title", "FreeTime 1")
                    put("date", "2026-01-01")
                    put("startTime", "00:00")
                    put("endTime", "01:00")
                    put("weekly", false)
                }, json)

                respond(
                    buildJsonObject {
                        put("id", uuid1.toHexDashString())
                    }.toString(),
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            val response = client.createFreeTime(
                FreeTimeData(
                    title = "FreeTime 1",
                    date = LocalDate(2026, 1, 1),
                    startTime = LocalTime(0, 0, 0),
                    endTime = LocalTime(1, 0, 0),
                    weekly = false,
                )
            )
            assertEquals(200, response.status)
            assertEquals(uuid1, response.response)
        }
    }

    @Test
    fun updateFreeTime() {
        val uuid1 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/freeTimes", request.url.encodedPath)
                assertEquals(HttpMethod.Put, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("id", uuid1.toHexDashString())
                    put("data", buildJsonObject {
                        put("title", "FreeTime 1")
                        put("date", "2026-01-01")
                        put("startTime", "00:00")
                        put("endTime", "01:00")
                        put("weekly", false)
                    })
                }, json)

                respondOk()
            }
            val response = client.updateFreeTime(
                FreeTime(
                    FreeTimeData(
                        title = "FreeTime 1",
                        date = LocalDate(2026, 1, 1),
                        startTime = LocalTime(0, 0, 0),
                        endTime = LocalTime(1, 0, 0),
                        weekly = false,
                    ), uuid1
                )
            )
            assertEquals(200, response.status)
        }
    }

    @Test
    fun deleteFreeTime() {
        val uuid1 = Uuid.random()

        runBlocking {
            val (engine, client) = createClient()

            engine += { request ->
                assertEquals("/api/v1/freeTimes", request.url.encodedPath)
                assertEquals(HttpMethod.Delete, request.method)
                assertEquals(
                    "Bearer ${MockSessionStore.session.accessToken}",
                    request.headers[HttpHeaders.Authorization]
                )

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("id", uuid1.toHexDashString())
                }, json)

                respondOk()
            }
            val response = client.deleteFreeTime(uuid1)
            assertEquals(200, response.status)
        }
    }
}