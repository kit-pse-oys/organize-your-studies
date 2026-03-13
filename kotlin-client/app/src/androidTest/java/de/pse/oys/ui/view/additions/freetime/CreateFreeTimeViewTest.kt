package de.pse.oys.ui.view.additions.freetime

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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

class CreateFreeTimeViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): ICreateFreeTimeViewModel = mockk {
        every { error } returns false
        every { showDelete } returns true
        every { title } returns ""
        every { date } returns LocalDate(2000, 1, 1)
        every { start } returns LocalTime(8, 0)
        every { end } returns LocalTime(9, 0)
        every { weekly } returns false
    }

    @Test
    fun back() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateBack() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                CreateFreeTimeView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Go Back").performClick()
        verify { viewModel.navigateBack() }
    }
}