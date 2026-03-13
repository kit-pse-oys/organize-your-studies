package de.pse.oys.ui.view.onboarding

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import de.pse.oys.data.api.Credentials
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.ui.navigation.Main
import de.pse.oys.ui.navigation.Questionnaire
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val api = mockk<RemoteAPI>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should call logout`() {
        LoginViewModel(api, context, navController)
        verify { api.logout() }
    }

    @Test
    fun `login success should navigate to main`() = runTest {
        val viewModel = LoginViewModel(api, context, navController)
        viewModel.username = "testUser"
        viewModel.password = "password123"

        coEvery { api.login(any<Credentials.UsernamePassword>()) } returns Response(Unit, 200)

        viewModel.login()
        advanceUntilIdle()

        verify {
            navController.navigate(
                Main,
                any<NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @Test
    fun `login failure should set error true`() = runTest {
        val viewModel = LoginViewModel(api, context, navController)

        coEvery { api.login(any()) } returns Response(null, 400)

        viewModel.login()
        advanceUntilIdle()

        assertTrue("Error flag should be true", viewModel.error)
        verify(exactly = 0) {
            navController.navigate(
                any<Any>(),
                any<NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @Test
    fun `register success should navigate to questionnaire`() = runTest {
        val viewModel = LoginViewModel(api, context, navController)
        viewModel.username = "newUser"
        viewModel.password = "securePass123"

        coEvery { api.register(any<Credentials.UsernamePassword>()) } returns Response(Unit, 200)

        viewModel.register()
        advanceUntilIdle()

        verify {
            navController.navigate(
                Questionnaire(firstTime=true),
                any<NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @Test
    fun `register failure should set error true`() = runTest {
        val viewModel = LoginViewModel(api, context, navController)

        coEvery { api.register(any()) } returns Response(null, 400)

        viewModel.register()
        advanceUntilIdle()

        assertTrue(viewModel.error)
    }
}