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
import org.junit.Rule
import org.junit.Test
import kotlin.uuid.Uuid

class TasksViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): ITasksViewModel = mockk {
        every { tasks } returns listOf()
    }

    @Test
    fun select() {
        val viewModel = createMockViewModel()
        every { viewModel.select(match { it.data.title == "ABC" }) } just runs
        every { viewModel.tasks } returns listOf(
            Task(
                ExamTaskData(
                    "ABC",
                    Module(mockk {
                        every { title } returns "XYZ"
                    }, Uuid.random()),
                    1,
                    LocalDate(2000, 1, 1),
                ), Uuid.random()
            )
        )

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                TasksView(viewModel)
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
                TasksView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Go Back").performClick()
        verify { viewModel.navigateBack() }
    }
}