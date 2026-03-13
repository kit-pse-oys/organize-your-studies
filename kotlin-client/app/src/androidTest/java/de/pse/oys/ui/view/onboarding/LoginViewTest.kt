package de.pse.oys.ui.view.onboarding

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme
import de.pse.oys.ui.view.onTextInputWithLabel
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class LoginViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): ILoginViewModel = mockk {
        every { error } returns false
        every { username } returns ""
        every { username = "" } just runs
        every { password } returns ""
        every { password = "" } just runs
    }

    @Test
    fun enterLogin() {
        val viewModel = createMockViewModel()

        every { viewModel.username = "aaaa" } just runs
        every { viewModel.password = "aaaaaaaa" } just runs
        every { viewModel.login() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                LoginView(viewModel)
            }
        }

        composeTestRule.onTextInputWithLabel("Username").performTextInput("aaaa")
        composeTestRule.onTextInputWithLabel("Password").performTextInput("aaaaaaaa")
        composeTestRule.onNodeWithText("Confirm", substring = true)
            .assertDoesNotExist()

        verify { viewModel.username = "aaaa" }
        verify { viewModel.password = "aaaaaaaa" }
    }

    @Test
    fun login() {
        val viewModel = createMockViewModel()

        every { viewModel.username } returns "aaaa"
        every { viewModel.password } returns "aaaaaaaa"
        every { viewModel.login() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                LoginView(viewModel)
            }
        }

        composeTestRule.onNode(hasClickAction() and hasText("Login")).performClick()
        verify { viewModel.login() }
    }

    @Test
    fun enterRegister() {
        val viewModel = createMockViewModel()

        every { viewModel.username = "aaaa" } just runs
        every { viewModel.password = "aaaaaaaa" } just runs
        every { viewModel.login() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                LoginView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("Register").performClick()
        composeTestRule.onTextInputWithLabel("Username").performTextInput("aaaa")
        composeTestRule.onTextInputWithLabel("Password").performTextInput("aaaaaaaa")
        composeTestRule.onTextInputWithLabel("Confirm").performTextInput("aaaaaaaa")

        verify { viewModel.username = "aaaa" }
        verify { viewModel.password = "aaaaaaaa" }
    }

    @Test
    fun register() {
        val viewModel = createMockViewModel()

        every { viewModel.username } returns "aaaa"
        every { viewModel.password } returns "aaaaaaaa"
        every { viewModel.register() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                LoginView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("Register").performClick()
        composeTestRule.onTextInputWithLabel("Confirm").performTextInput("aaaaaaaa")
        composeTestRule.onNode(hasClickAction() and hasText("Register")).performClick()
        verify { viewModel.register() }
    }
}