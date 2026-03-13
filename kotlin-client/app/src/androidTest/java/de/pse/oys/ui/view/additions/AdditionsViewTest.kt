package de.pse.oys.ui.view.additions

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
import org.junit.Rule
import org.junit.Test

class AdditionsViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): IAdditionsViewModel = mockk()

    @Test
    fun navigateModule() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateToCreateModule() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                AdditionsView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("New Module").performClick()
        verify { viewModel.navigateToCreateModule() }
    }

    @Test
    fun navigateTask() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateToCreateTask() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                AdditionsView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("New Task").performClick()
        verify { viewModel.navigateToCreateTask() }
    }

    @Test
    fun navigateFreetime() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateToCreateFreeTime() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                AdditionsView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("New Freetime").performClick()
        verify { viewModel.navigateToCreateFreeTime() }
    }

    @Test
    fun back() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateBack() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                AdditionsView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Go Back").performClick()
        verify { viewModel.navigateBack() }
    }
}