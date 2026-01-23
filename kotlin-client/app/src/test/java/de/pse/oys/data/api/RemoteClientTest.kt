package de.pse.oys.data.api

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.engine.mock.toByteReadPacket
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.*
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
    }

    private fun createClient(): Pair<MockEngine.Queue, RemoteClient> {
        val engine = MockEngine.Queue()
        val client = RemoteClient("https://example.org/", MockSessionStore, engine)
        return engine to client
    }

    @Test
    fun registerBasic() {
        runBlocking {
            val (engine, client) = createClient()

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

                respond(buildJsonObject {
                    put("accessToken", MockSessionStore.session.accessToken)
                    put("refreshToken", MockSessionStore.session.refreshToken)
                }.toString(), headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
            }
            val response = client.register(Credentials.UsernamePassword("USERNAME", "PASSWORD"))
            assertEquals(200, response.status)
        }
    }

    @Test
    fun registerOIDC() {
        runBlocking {
            val (engine, client) = createClient()

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

                respond(buildJsonObject {
                    put("accessToken", MockSessionStore.session.accessToken)
                    put("refreshToken", MockSessionStore.session.refreshToken)
                }.toString(), headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
            }
            val response = client.register(Credentials.OIDC("TOKEN", OIDCType.GOOGLE))
            assertEquals(200, response.status)
        }
    }

    @Test
    fun login() {
    }

    @Test
    fun logout() {
    }

    @Test
    fun deleteAccount() {
    }

    @Test
    fun updateQuestionnaire() {
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