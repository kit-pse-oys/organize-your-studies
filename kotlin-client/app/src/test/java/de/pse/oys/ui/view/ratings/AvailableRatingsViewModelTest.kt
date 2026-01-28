package de.pse.oys.ui.view.ratings

import androidx.navigation.NavController
import de.pse.oys.ui.navigation.rating
import de.pse.oys.ui.theme.Blue
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class AvailableRatingsViewModelTest {
    private val navController = mockk<NavController>(relaxed = true)
    val testTarget = RatingTarget("Test-Title", Blue)

    @Test
    fun `available list matches the data passed in constructor`() {
        val viewModel = AvailableRatingsViewModel(navController, listOf(testTarget))

        assertEquals(1, viewModel.available.size)
        assertEquals(testTarget, viewModel.available[0])
    }

    @Test
    fun `selectRating calls navigate`() {
        val viewModel = AvailableRatingsViewModel(navController, listOf(testTarget))
        viewModel.selectRating(testTarget)
        verify { navController.rating() }
    }

    @Test
    fun `select should NOT navigate if rating is not in the list`() {
        val viewModel = AvailableRatingsViewModel(navController, emptyList())
        val unknownTarget = testTarget
        viewModel.selectRating(unknownTarget)
        verify(exactly = 0) { navController.rating() }
    }
}