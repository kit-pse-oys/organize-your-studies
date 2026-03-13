package de.pse.oys.ui.view

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
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test

class MainViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): IMainViewModel = mockk {
        every { error } returns false
        every { units } returns mapOf()
        every { unitsToday } returns listOf()
        every { unitsTomorrow } returns listOf()
        every { freeTimes } returns mapOf()
        every { freeTimesToday } returns listOf()
    }

    @Test
    fun navigateMenu() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateToMenu() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                MainView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Open Menu").performClick()
        verify { viewModel.navigateToMenu() }
    }

    @Test
    fun navigateAdditions() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateToAdditions() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                MainView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Open Additions").performClick()
        verify { viewModel.navigateToAdditions() }
    }

    @Test
    fun navigateRatings() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateToUnitRating() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                MainView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("Rate Units").performClick()
        verify { viewModel.navigateToUnitRating() }
    }

    @Test
    fun displayFreeTime() {
        val viewModel = createMockViewModel()
        every { viewModel.freeTimesToday } returns listOf(
            PlannedFreeTime(
                "ABC",
                LocalTime(8, 0),
                LocalTime(9, 0)
            )
        )

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                MainView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("ABC").assertExists()
    }
}