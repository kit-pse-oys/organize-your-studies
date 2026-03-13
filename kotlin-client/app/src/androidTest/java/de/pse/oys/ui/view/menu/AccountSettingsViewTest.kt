package de.pse.oys.ui.view.menu

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

class AccountSettingsViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): IAccountSettingsViewModel = mockk {
        every { error } returns false
    }

    @Test
    fun logout() {
        val viewModel = createMockViewModel()
        every { viewModel.logout() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                AccountSettingsView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("Logout").performClick()
        verify { viewModel.logout() }
    }

    @Test
    fun deleteAccount() {
        val viewModel = createMockViewModel()
        every { viewModel.deleteAccount() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                AccountSettingsView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("Delete Account").performClick()
        composeTestRule.onNodeWithText("Yes").performClick()
        verify { viewModel.deleteAccount() }
    }

    @Test
    fun back() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateBack() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                AccountSettingsView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Go Back").performClick()
        verify { viewModel.navigateBack() }
    }
}