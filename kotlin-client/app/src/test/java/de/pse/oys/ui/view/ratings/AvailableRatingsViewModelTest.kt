package de.pse.oys.ui.view.ratings

import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.data.facade.StepData
import de.pse.oys.data.facade.TaskData
import de.pse.oys.ui.navigation.rating
import de.pse.oys.ui.view.TestUtils.TEST_COLOR
import de.pse.oys.ui.view.TestUtils.TEST_TITLE
import de.pse.oys.ui.view.TestUtils.randomUuid
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.every
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AvailableRatingsViewModelTest {
    private val navController = mockk<NavController>(relaxed = true)
    private val api = mockk<RemoteAPI>()
    private val model = mockk<ModelFacade>()
    private val testDispatcher = StandardTestDispatcher()

    private val testUuid = randomUuid()


    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val mockStepData = mockk<StepData>(relaxed = true)

        every { model.steps } returns mapOf(testUuid to mockStepData)

        val mockTaskData = mockk<TaskData>(relaxed = true)
        every { mockStepData.task.data } returns mockTaskData
        every { mockTaskData.title } returns TEST_TITLE
        every { mockTaskData.module.data.color } returns TEST_COLOR

        coEvery { api.queryRateable() } returns Response(
            status = HttpStatusCode.OK.value,
            response = listOf(testUuid)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `available list matches the data from api and facade`() = runTest {
        val viewModel = AvailableRatingsViewModel(api, model, navController)
        advanceUntilIdle()

        assertEquals(1, viewModel.available.size)
        assertEquals(TEST_TITLE, viewModel.available[0].name)
        assertEquals(TEST_COLOR, viewModel.available[0].color)
    }

    @Test
    fun `selectRating calls navigate with correct uuid`() = runTest {
        val viewModel = AvailableRatingsViewModel(api, model, navController)
        advanceUntilIdle()

        val target = viewModel.available[0]
        viewModel.selectRating(target)

        verify { navController.rating(testUuid) }
    }

    @Test
    fun `selectRating should NOT navigate if rating is unknown`() = runTest {
        val viewModel = AvailableRatingsViewModel(api, model, navController)
        advanceUntilIdle()

        val unknownTarget = RatingTarget(TEST_TITLE, TEST_COLOR)
        viewModel.selectRating(unknownTarget)

        verify(exactly = 0) { navController.rating(any()) }
    }
}