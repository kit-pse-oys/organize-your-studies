package de.pse.oys.ui.view.additions.module

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import de.pse.oys.data.facade.Priority
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme
import io.mockk.core.ValueClassSupport.boxedValue
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class CreateModuleViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): ICreateModuleViewModel = mockk {
        every { error } returns false
        every { showDelete } returns true
        every { title } returns ""
        every { description } returns ""
        every { priority } returns Priority.MEDIUM
        every { color.boxedValue } returns Color.White.boxedValue
    }

    @Test
    fun back() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateBack() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                CreateModuleView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Go Back").performClick()
        verify { viewModel.navigateBack() }
    }
}