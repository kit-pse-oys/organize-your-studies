package de.pse.oys.ui.view.additions.freetime

import androidx.navigation.NavController
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.editFreeTime
import de.pse.oys.ui.view.TestUtils.createMockFreeTimeData
import de.pse.oys.ui.view.TestUtils.randomUuid
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FreeTimesViewModelTest {
    private val navController = mockk<NavController>(relaxed = true)
    private val modelFacade = mockk<ModelFacade>()

    private val freeTimeData = createMockFreeTimeData()
    private val testId = randomUuid()
    private val testFreeTime = Identified(id = testId, data = freeTimeData)

    @Before
    fun setup() {
        every { modelFacade.freeTimes } returns mapOf(testId to freeTimeData)
    }

    @Test
    fun `available list matches the data from model facade`() {
        val viewModel = FreeTimesViewModel(modelFacade, navController)
        assertEquals(1, viewModel.freeTimes.size)
        assertEquals(testFreeTime, viewModel.freeTimes[0])
    }

    @Test
    fun `select calls navigate with FreeTime object`() {
        val viewModel = FreeTimesViewModel(modelFacade, navController)
        val freeTimeToSelect = viewModel.freeTimes[0]

        viewModel.select(freeTimeToSelect)

        verify { navController.editFreeTime(freeTimeToSelect) }
    }

    @Test
    fun `select should NOT navigate if freeTimr is not in the list`() {
        val viewModel = FreeTimesViewModel(modelFacade, navController)

        val unknownFreeTime = Identified(freeTimeData, randomUuid())

        viewModel.select(unknownFreeTime)

        verify(exactly = 0) { navController.editFreeTime(unknownFreeTime) }
    }
}