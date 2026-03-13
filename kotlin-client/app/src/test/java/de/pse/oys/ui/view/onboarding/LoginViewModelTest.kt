package de.pse.oys.ui.view.onboarding

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialResponse
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import de.pse.oys.data.api.OIDCType
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.ui.navigation.Main
import de.pse.oys.ui.navigation.Questionnaire
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val api = mockk<RemoteAPI>(relaxed = true)
    private val context = mockk<Activity>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockManager: CredentialManager
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(CredentialManager.Companion)
        mockManager = mockk(relaxed = true)
        every { CredentialManager.create(any()) } returns mockManager

        mockkObject(LoginViewModel.Companion)
        every { LoginViewModel.Companion["getGoogleCredentialOption"]() } returns mockk<GetSignInWithGoogleOption>(
            relaxed = true
        )

        viewModel = LoginViewModel(api, context, navController)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(CredentialManager.Companion)
    }

    @Test
    fun `login success should navigate to main`() = runTest {
        coEvery { api.login(any()) } returns Response(Unit, 200)

        viewModel.username = "test"
        viewModel.password = "password"
        viewModel.login()

        advanceUntilIdle()
        verify {
            navController.navigate(
                route = eq(Main),
                builder = any<NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @Test
    fun `login failure should set error true`() = runTest {
        coEvery { api.login(any()) } returns Response(null, 500)

        viewModel.login()
        advanceUntilIdle()
        kotlinx.coroutines.yield()

        assertTrue("Der Error-Status wurde nicht auf true gesetzt!", viewModel.error)
    }

    @Test
    fun `loginWithOIDC should handle parsing failure`() = runTest {
        val mockResponse = mockk<GetCredentialResponse>()
        every { mockResponse.credential } returns mockk()

        coEvery {
            mockManager.getCredential(any(), any<androidx.credentials.GetCredentialRequest>())
        } returns mockResponse

        viewModel.loginWithOIDC(OIDCType.GOOGLE)
        advanceUntilIdle()

        assertTrue(viewModel.error)
    }

    @Test
    fun `registerWithOIDC should handle cancellation`() = runTest {
        coEvery {
            mockManager.getCredential(
                any<Activity>(),
                any<androidx.credentials.GetCredentialRequest>()
            )
        } throws androidx.credentials.exceptions.GetCredentialCancellationException("User cancelled")

        viewModel.registerWithOIDC(OIDCType.GOOGLE)
        advanceUntilIdle()

        assertFalse(viewModel.error)
    }

    @Test
    fun `register success should navigate to questionnaire`() = runTest {
        coEvery { api.register(any()) } returns Response(Unit, 200)

        viewModel.username = "new"
        viewModel.password = "pass"
        viewModel.register()
        advanceUntilIdle()

        verify {
            navController.navigate(
                eq(Questionnaire(firstTime = true)),
                any<NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @Test
    fun `register failure should set error true`() = runTest {
        coEvery { api.register(any()) } returns Response(null, 400)

        viewModel.register()
        advanceUntilIdle()

        assertTrue(viewModel.error)
    }

    @Test
    fun `init should call logout`() {
        verify { api.logout() }
    }
}