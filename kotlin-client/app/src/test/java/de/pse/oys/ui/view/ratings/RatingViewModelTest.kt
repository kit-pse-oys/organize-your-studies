package de.pse.oys.ui.view.ratings

import androidx.navigation.NavController
import de.pse.oys.data.RatingAspect
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.api.Response
import de.pse.oys.data.facade.Rating
import de.pse.oys.ui.navigation.AvailableRatings
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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class RatingViewModelTest {
    private val api = mockk<RemoteAPI>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val testTarget = Uuid.random()
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
    fun `initial ratings should be medium`() {
        val viewModel = RatingViewModel(api, testTarget, navController)
        assertEquals(Rating.MEDIUM, viewModel.getRating(RatingAspect.GOAL))
        assertEquals(Rating.MEDIUM, viewModel.getRating(RatingAspect.DURATION))
        assertEquals(Rating.MEDIUM, viewModel.getRating(RatingAspect.MOTIVATION))
    }

    @Test
    fun `updateRating should change the state`() {
        val viewModel = RatingViewModel(api, testTarget, navController)
        viewModel.updateRating(RatingAspect.GOAL, Rating.HIGH)
        assertEquals(Rating.HIGH, viewModel.getRating(RatingAspect.GOAL))
    }

    @Test
    fun `submitRating should call api`() = runTest {
        val viewModel = RatingViewModel(api, testTarget, navController)
        viewModel.updateRating(RatingAspect.GOAL, Rating.LOW)
        viewModel.submitRating()
        advanceUntilIdle()
        coVerify {
            api.rateUnit(testTarget, withArg {
                assertEquals(Rating.LOW, it.goalCompletion)
                assertEquals(Rating.MEDIUM, it.perceivedDuration)
            })
        }
    }

    @Test
    fun `submitMissed should call correct api and navigate`() = runTest {
        coEvery { api.rateUnitMissed(testTarget) } returns Response(Unit, 200)

        val viewModel = RatingViewModel(api, testTarget, navController)

        viewModel.submitMissed()
        advanceUntilIdle()

        coVerify { api.rateUnitMissed(testTarget) }

        verify {
            navController.navigate(
                eq(AvailableRatings),
                any<androidx.navigation.NavOptionsBuilder.() -> Unit>()
            )
        }
    }

    @Test
    fun `submitRating error should set error state`() = runTest {
        coEvery { api.rateUnit(any(), any()) } returns Response(null, 500)
        val viewModel = RatingViewModel(api, testTarget, navController)

        viewModel.submitRating()
        advanceUntilIdle()

        assertEquals(true, viewModel.error)
    }

    @Test
    fun `navigateBack calls popBackStack`() {
        val viewModel = RatingViewModel(api, testTarget, navController)
        viewModel.navigateBack()
        verify { navController.popBackStack() }
    }
}