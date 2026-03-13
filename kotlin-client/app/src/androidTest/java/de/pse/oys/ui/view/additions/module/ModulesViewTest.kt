package de.pse.oys.ui.view.additions.module

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.ModuleData
import de.pse.oys.data.facade.Priority
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import kotlin.uuid.Uuid

class ModulesViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): IModulesViewModel = mockk {
        every { modules } returns listOf()
    }

    @Test
    fun select() {
        val viewModel = createMockViewModel()
        every { viewModel.select(match { it.data.title == "ABC" }) } just runs
        every { viewModel.modules } returns listOf(
            Module(
                ModuleData(
                    "ABC",
                    "a",
                    Priority.MEDIUM,
                    Color.White,
                ), Uuid.random()
            )
        )

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                ModulesView(viewModel)
            }
        }

        composeTestRule.onNodeWithText("ABC").performClick()
        verify { viewModel.select(match { it.data.title == "ABC" }) }
    }

    @Test
    fun back() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateBack() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                ModulesView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Go Back").performClick()
        verify { viewModel.navigateBack() }
    }
}