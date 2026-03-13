package de.pse.oys.ui.view.additions.freetime

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.pse.oys.data.facade.FreeTime
import de.pse.oys.data.facade.FreeTimeData
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
import kotlin.uuid.Uuid

class FreeTimesViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): IFreeTimesViewModel = mockk {
        every { freeTimes } returns listOf()
    }

    @Test
    fun select() {
        val viewModel = createMockViewModel()
        every { viewModel.select(match { it.data.title == "ABC" }) } just runs
        every { viewModel.freeTimes } returns listOf(
            FreeTime(
                FreeTimeData(
                    "ABC",
                    LocalDate(2000, 1, 1),
                    LocalTime(8, 0),
                    LocalTime(9, 0),
                    false,
                ), Uuid.random()
            )
        )

        composeTestRule.setContent {
            OrganizeYourStudiesTheme {
                FreeTimesView(viewModel)
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
                FreeTimesView(viewModel)
            }
        }

        composeTestRule.onNodeWithContentDescription("Go Back").performClick()
        verify { viewModel.navigateBack() }
    }
}