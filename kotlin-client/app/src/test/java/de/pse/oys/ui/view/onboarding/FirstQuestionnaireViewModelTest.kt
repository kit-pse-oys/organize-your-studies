package de.pse.oys.ui.view.onboarding

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.Main
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirstQuestionnaireViewModelTest {

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
    fun `initial state should show welcome screen`() {
        val viewModel = FirstQuestionnaireViewModel(api, model, navController)
        assertTrue(viewModel.showWelcome)
    }

    @Test
    fun `showQuestionnaire should hide welcome screen`() {
        val viewModel = FirstQuestionnaireViewModel(api, model, navController)

        viewModel.showQuestionnaire()

        assertFalse(viewModel.showWelcome)
    }

    @Test
    fun `navigate back returns to welcome from first questionnaire`() {
        val viewModel = FirstQuestionnaireViewModel(api, model, navController)

        viewModel.showQuestionnaire()
        assertFalse(viewModel.showWelcome)

        viewModel.navigateBack()
        assertTrue(viewModel.showWelcome)

        verify(exactly = 0) { navController.popBackStack() }
    }

    @Test
    fun `navigate back should pop back stack if on welcome screen`() {
        val viewModel = FirstQuestionnaireViewModel(api, model, navController)

        viewModel.navigateBack()

        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `submitQuestionnaire should call navigateToMain with specific options`() = runTest {
        coEvery { api.updateQuestionnaire(any()) } returns Response(Unit, 200)

        val viewModel = FirstQuestionnaireViewModel(api, model, navController)

        viewModel.submitQuestionnaire()
        advanceUntilIdle()

        verify {
            navController.navigate(
                Main,
                any<NavOptionsBuilder.() -> Unit>()
            )
        }
    }
}