package de.pse.oys.ui.view.additions.task

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.pse.oys.data.facade.ExamTaskData
import de.pse.oys.data.facade.Module
import de.pse.oys.data.facade.Task
import de.pse.oys.ui.theme.OrganizeYourStudiesTheme
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test
import kotlin.uuid.Uuid

class CreateTaskViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): ICreateTaskViewModel = mockk {
        every { error } returns false
        every { showDelete } returns true
        every { title } returns ""
        every { module } returns ""
        every { type } returns TaskType.EXAM
        every { weeklyTimeLoad } returns 0
        every { examDate } returns LocalDate(2000, 1, 1)
        every { submissionDate } returns LocalDateTime(2000, 1, 1, 8, 0)
        every { submissionCycle } returns 1
        every { start } returns LocalDate(2000, 1, 1)
        every { end } returns LocalDate(2000, 1, 1)
    }

    @Test
    fun back() {
        val viewModel = createMockViewModel()
        every { viewModel.navigateBack() } just runs

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                CreateTaskView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Go Back").performClick()
        verify { viewModel.navigateBack() }
    }
}