package de.pse.oys.ui.view.menu

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.Main
import de.pse.oys.ui.navigation.login
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountSettingsViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var api: RemoteAPI

    @MockK(relaxed = true)
    lateinit var model: ModelFacade

    @MockK(relaxed = true)
    lateinit var navController: NavController

    @MockK(relaxed = true)
    lateinit var context: Context

    @MockK(relaxed = true)
    lateinit var credentialManager: CredentialManager

    private lateinit var viewModel: AccountSettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("de.pse.oys.ui.navigation.OnboardingKt")

        viewModel = AccountSettingsViewModel(
            api,
            model,
            context,
            navController,
            credentialManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `logout should clear credentials, reset model and navigate to login`() = runTest {
        coEvery { api.logout() } returns Unit

        viewModel.logout()
        advanceUntilIdle()

        coVerify { api.logout() }
        coVerify { credentialManager.clearCredentialState(any<ClearCredentialStateRequest>()) }

        verify { model.modules = null }
        verify { model.tasks = null }
        verify { model.freeTimes = null }
        verify { model.steps = null }

        verify { navController.login(dontGoBack = Main) }
    }

    @Test
    fun `navigate back should call popBackStack`() {
        viewModel.navigateBack()

        verify { navController.popBackStack() }
    }

    @Test
    fun `error state should be set on failed deleteAccount`() = runTest {
        coEvery { api.deleteAccount() } returns Response(response = null, status = 500)

        viewModel.deleteAccount()
        advanceUntilIdle()
        assertEquals(true, viewModel.error)
    }

    @Test
    fun `deleteAccount should clear credentials, reset model and navigate on success`() = runTest {
        coEvery { api.deleteAccount() } returns Response(response = Unit, status = 200)

        viewModel.deleteAccount()
        advanceUntilIdle()

        coVerify { api.deleteAccount() }
        coVerify { credentialManager.clearCredentialState(any()) }

        verify { model.modules = null }
        verify { model.tasks = null }

        verify { navController.login(dontGoBack = Main) }
    }

    @Test
    fun `error state can be reset`() {
        viewModel.error = true
        viewModel.error = false
        assertEquals(false, viewModel.error)
    }
}