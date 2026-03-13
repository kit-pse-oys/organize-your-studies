package de.pse.oys.ui.view.ratings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test

class AvailableRatingsViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): IAvailableRatingsViewModel = mockk {
        every { error } returns false
        every { available } returns listOf()
    }

    @Test
    fun select() {
        val viewModel = createMockViewModel()
        every { viewModel.selectRating(match { it.name == "ABC" }) } just runs
        every { viewModel.available } returns listOf(
            RatingTarget(
                "ABC",
                Color.White,
                LocalDate(2000, 1, 1),
                LocalTime(8, 0),
                LocalTime(9, 0)
            )
        )

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                AvailableRatingsView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("ABC").performClick()
        verify { viewModel.selectRating(match { it.name == "ABC" }) }
    }

    @Test
    fun back() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateBack() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                AvailableRatingsView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Go Back").performClick()
        verify { viewModel.navigateBack() }
    }
}