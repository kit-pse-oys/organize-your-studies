package de.pse.oys.ui.view.onboarding

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class QuestionnaireViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): IQuestionnaireViewModel = mockk {
        every { error } returns false
        every { showWelcome } returns false
        every { isValid } returns true
        every { selected(any(), any()) } returns MutableStateFlow(false)
    }

    @Test
    fun select() {
        val viewModel = createMockViewModel()
        every {
            viewModel.select(
                match { it.id == "time_before_deadlines" },
                match { it.id == "2" })
        } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                QuestionnaireView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("2 days").performClick()
        verify {
            viewModel.select(
                match { it.id == "time_before_deadlines" },
                match { it.id == "2" })
        }
    }

    @Test
    fun submit() {
        val viewModel = createMockViewModel()
        every { viewModel.submitQuestionnaire() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                QuestionnaireView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("Save answers").performScrollTo().performClick()
        verify { viewModel.submitQuestionnaire() }
    }

    @Test
    fun back() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateBack() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                QuestionnaireView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Go Back").performClick()
        verify { viewModel.navigateBack() }
    }
}