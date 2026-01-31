package de.pse.oys.ui.view.additions.freetime

import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.view.TestUtils.TEST_DATE
import de.pse.oys.ui.view.TestUtils.TEST_DATE_FUTURE
import de.pse.oys.ui.view.TestUtils.TEST_TIME
import de.pse.oys.ui.view.TestUtils.TEST_TIME_FUTURE
import de.pse.oys.ui.view.TestUtils.TEST_TITLE
import de.pse.oys.ui.view.TestUtils.createMockFreeTimeData
import de.pse.oys.ui.view.TestUtils.randomUuid
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EditFreeTimeViewModelTest {
    private val api = mockk<RemoteAPI>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val model = mockk<ModelFacade>(relaxed = true)
    private lateinit var viewModel: EditFreeTimeViewModel

    @Before
    fun setup() {
        val existingData = createMockFreeTimeData()
        val targetFreeTime = Identified(existingData, randomUuid())
        viewModel = EditFreeTimeViewModel(api, model, targetFreeTime, navController)
    }

    @Test
    fun `initial state should be filled with target freeTime data`() {
        assertEquals(TEST_TITLE, viewModel.title)
        assertEquals(TEST_DATE, viewModel.date)
        assertEquals(TEST_TIME, viewModel.start)
        assertEquals(TEST_TIME, viewModel.end)
        assertFalse(viewModel.weekly)
        assertTrue(viewModel.showDelete)
    }

    @Test
    fun `editing fields should change the state`() {
        val newTitle = "New-Title"
        viewModel.title = newTitle
        assertEquals(newTitle, viewModel.title)

        viewModel.date = TEST_DATE_FUTURE
        assertEquals(TEST_DATE_FUTURE, viewModel.date)

        viewModel.start = TEST_TIME_FUTURE
        assertEquals(TEST_TIME_FUTURE, viewModel.start)

        viewModel.end = TEST_TIME_FUTURE
        assertEquals(TEST_TIME_FUTURE, viewModel.end)

        viewModel.weekly = true
        assertTrue(viewModel.weekly)
    }
}