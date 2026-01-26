package de.pse.oys.data.api

import de.pse.oys.data.Answer
import de.pse.oys.data.Question
import de.pse.oys.data.QuestionState
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
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
                assertEquals("/api/v1/users", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("type", "BASIC")
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
                assertEquals("/api/v1/users", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("type", "OIDC")
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
                assertEquals("/api/v1/auth/login", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("type", "BASIC")
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
                assertEquals("/api/v1/auth/login", request.url.encodedPath)
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(ContentType.Application.Json, request.body.contentType)

                val json = Json.parseToJsonElement(request.body.toByteReadPacket().readString())
                assertEquals(buildJsonObject {
                    put("type", "OIDC")
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
                assertEquals("Bearer ${MockSessionStore.session.accessToken}", request.headers[HttpHeaders.Authorization])

                respondOk()
            }
            val response = client.deleteAccount()
            assertEquals(200, response.status)
        }
    }

    @Test
    fun updateQuestionnaire() {
        val answer1 = Answer("answer1")
        val answer2 = Answer("answer2")
        val answer3 = Answer("answer3")
        val question1 = Question("question1", true, listOf(answer1, answer2, answer3))
        val question2 = Question("question2", false, listOf(answer1, answer2, answer3))
        val question3 = Question("question3", true, listOf(answer1, answer2, answer3))
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
                assertEquals("Bearer ${MockSessionStore.session.accessToken}", request.headers[HttpHeaders.Authorization])
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
    }

    @Test
    fun moveUnitAutomatically() {
    }

    @Test
    fun moveUnit() {
    }

    @Test
    fun queryRateable() {
    }

    @Test
    fun rateUnit() {
    }

    @Test
    fun updatePlan() {
    }

    @Test
    fun queryUnits() {
    }

    @Test
    fun queryModules() {
    }

    @Test
    fun createModule() {
    }

    @Test
    fun updateModule() {
    }

    @Test
    fun deleteModule() {
    }

    @Test
    fun queryTasks() {
    }

    @Test
    fun createTask() {
    }

    @Test
    fun updateTask() {
    }

    @Test
    fun deleteTask() {
    }

    @Test
    fun queryFreeTimes() {
    }

    @Test
    fun createFreeTime() {
    }

    @Test
    fun updateFreeTime() {
    }

    @Test
    fun deleteFreeTime() {
    }
}