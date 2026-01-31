package de.pse.oys.ui.view.ratings

import androidx.navigation.NavController
import de.pse.oys.data.RatingAspect
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.Rating
import de.pse.oys.ui.navigation.availableRatings
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
    fun `submitRating should call api and navigate`() = runTest {
        val viewModel = RatingViewModel(api, testTarget, navController)
        viewModel.updateRating(RatingAspect.GOAL, Rating.LOW)
        viewModel.submitRating()
        advanceUntilIdle()
        coVerify {
            api.rateUnit(testTarget, withArg {
                assertEquals(Rating.LOW, it.goalCompletion)
                assertEquals(Rating.MEDIUM, it.duration)
            })
        }
        verify { navController.availableRatings(any()) }
    }
}