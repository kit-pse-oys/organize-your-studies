package de.pse.oys.ui.view.menu

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.pse.oys.data.properties.Darkmode
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class MenuViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): IMenuViewModel = mockk {
        every { error } returns false
        every { darkmode } returns MutableStateFlow(Darkmode.SYSTEM)
    }

    @Test
    fun selectDarkmode() {
        val viewModel = createMockViewModel()
        every { viewModel.setDarkmode(any()) } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                MenuView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("Always dark").performClick()
        verify { viewModel.setDarkmode(Darkmode.ENABLED) }
    }

    @Test
    fun navigateModules() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateToModules() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                MenuView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("My Modules").performClick()
        verify { viewModel.navigateToModules() }
    }

    @Test
    fun navigateTasks() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateToTasks() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                MenuView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("My Tasks").performClick()
        verify { viewModel.navigateToTasks() }
    }

    @Test
    fun navigateFreetimes() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateToFreeTimes() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                MenuView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("My Freetimes").performClick()
        verify { viewModel.navigateToFreeTimes() }
    }

    @Test
    fun navigateQuestionnaire() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateToEditQuestionnaire() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                MenuView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("Edit Questionnaire").performClick()
        verify { viewModel.navigateToEditQuestionnaire() }
    }

    @Test
    fun navigateAccount() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateToAccountSettings() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                MenuView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("Account Settings").performClick()
        verify { viewModel.navigateToAccountSettings() }
    }

    @Test
    fun updatePlan() {
        val viewModel = createMockViewModel()
        every { viewModel.updatePlan() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                MenuView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("Update Plan").performClick()
        verify { viewModel.updatePlan() }
    }

    @Test
    fun back() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateBack() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                MenuView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Go Back").performClick()
        verify { viewModel.navigateBack() }
    }
}