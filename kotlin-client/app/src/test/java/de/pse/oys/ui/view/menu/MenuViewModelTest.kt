package de.pse.oys.ui.view.menu

import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.properties.Darkmode
import de.pse.oys.data.properties.Properties
import de.pse.oys.ui.navigation.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val testDispatcher = StandardTestDispatcher()

    @MockK(relaxed = true)
    lateinit var properties: Properties

    @MockK
    lateinit var api: RemoteAPI

    @MockK(relaxed = true)
    lateinit var model: ModelFacade

    @MockK(relaxed = true)
    lateinit var navController: androidx.navigation.NavController

    private lateinit var viewModel: MenuViewModel
    private val darkmodeFlow = MutableStateFlow(Darkmode.SYSTEM)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { properties.darkmode } returns darkmodeFlow

        mockkStatic("de.pse.oys.ui.navigation.OnboardingKt")
        mockkStatic("de.pse.oys.ui.navigation.MainKt")

        viewModel = MenuViewModel(properties, api, model, navController)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `setDarkmode should call properties`() = runTest {
        viewModel.setDarkmode(Darkmode.ENABLED)
        advanceUntilIdle()

        coVerify { properties.setDarkmode(Darkmode.ENABLED) }
    }

    @Test
    fun `updatePlan should call api, reset steps and navigate to main`() = runTest {
        coEvery { api.updatePlan() } returns Response(Unit, 200)

        viewModel.updatePlan()
        advanceUntilIdle()

        coVerify { api.updatePlan() }
        verify { model.steps = null }
        verify { navController.main() }
    }

    @Test
    fun `error state should be set on failed updatePlan`() = runTest {
        coEvery { api.updatePlan() } returns Response(null, 500)

        viewModel.updatePlan()
        advanceUntilIdle()

        assertEquals(true, viewModel.error)
    }

    @Test
    fun `navigation functions should call correct extensions`() {
        viewModel.navigateToModules()
        verify { navController.myModules() }

        viewModel.navigateToTasks()
        verify { navController.myTasks() }

        viewModel.navigateToFreeTimes()
        verify { navController.myFreeTimes() }

        viewModel.navigateToEditQuestionnaire()
        verify { navController.editQuestionnaire() }

        viewModel.navigateToAccountSettings()
        verify { navController.accountSettings() }

        viewModel.navigateBack()
        verify { navController.popBackStack() }
    }

    @Test
    fun `error state should be resettable`() {
        viewModel.error = true
        viewModel.error = false
        assertEquals(false, viewModel.error)
    }
}