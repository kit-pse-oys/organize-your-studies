package de.pse.oys.ui.view.ratings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.pse.oys.data.facade.Rating
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class RatingViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): IRatingViewModel = mockk {
        every { error } returns false
        every { getRating(any()) } returns Rating.MEDIUM
    }

    @Test
    fun submit() {
        val viewModel = createMockViewModel()
        every { viewModel.submitRating() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                RatingView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("Save Rating").performClick()
        verify { viewModel.submitRating() }
    }

    @Test
    fun missed() {
        val viewModel = createMockViewModel()
        every { viewModel.submitMissed() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                RatingView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("I missed this unit.").performClick()
        verify { viewModel.submitMissed() }
    }

    @Test
    fun back() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateBack() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                RatingView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Go Back").performClick()
        verify { viewModel.navigateBack() }
    }
}