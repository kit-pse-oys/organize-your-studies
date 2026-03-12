package de.pse.oys.ui.view.onboarding

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import de.pse.oys.data.QuestionState
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.Main
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditQuestionnaireViewModelTest {

    private val api = mockk<RemoteAPI>(relaxed = true)
    private val model = mockk<ModelFacade>(relaxed = true)
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
    fun `init should fetch questionnaire and update state`() = runTest {
        val mockState = QuestionState()
        coEvery { api.getQuestionnaire() } returns Response(mockState, 200)

        val viewModel = EditQuestionnaireViewModel(api, model, navController)
        advanceUntilIdle()

        coVerify { api.getQuestionnaire() }
        assertFalse(viewModel.showWelcome)
    }

    @Test
    fun `showWelcome is always false`() {
        val viewModel = EditQuestionnaireViewModel(api, model, navController)
        assertFalse(viewModel.showWelcome)
    }

    @Test
    fun `MapsToMain should navigate`() {
        val viewModel = EditQuestionnaireViewModel(api, model, navController)
        viewModel.navigateToMain()
        verify {
            navController.navigate(
                Main,
                any<NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @Test
    fun `MapsBack should pop backstack`() {
        val viewModel = EditQuestionnaireViewModel(api, model, navController)
        viewModel.navigateBack()
        verify { navController.popBackStack() }
    }

    @Test
    fun `submitQuestionnaire should update api and navigate`() = runTest {
        coEvery { api.updateQuestionnaire(any()) } returns Response(Unit, 200)

        val viewModel = EditQuestionnaireViewModel(api, model, navController)
        viewModel.submitQuestionnaire()
        advanceUntilIdle()

        coVerify { api.updateQuestionnaire(any()) }
        verify { model.steps = null }
        verify {
            navController.navigate(
                Main,
                any<NavOptionsBuilder.() -> Unit>()
            )
        }
    }
}