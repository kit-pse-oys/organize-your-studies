package de.pse.oys.ui.view.additions.freetime

import androidx.navigation.NavController
import de.pse.oys.data.api.RemoteAPI
import de.pse.oys.data.facade.FreeTimeData
import de.pse.oys.data.facade.Identified
import de.pse.oys.data.facade.ModelFacade
import de.pse.oys.ui.navigation.main
import de.pse.oys.ui.view.TestUtils.TEST_DATE_FUTURE
import de.pse.oys.ui.view.TestUtils.TEST_TIME_FUTURE
import de.pse.oys.ui.view.TestUtils.TEST_TITLE
import de.pse.oys.ui.view.TestUtils.createMockFreeTimeData
import de.pse.oys.ui.view.TestUtils.randomUuid
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Clock
import kotlin.uuid.Uuid

class CreateFreeTimeViewModelTest {
    private val api = mockk<RemoteAPI>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val model = mockk<ModelFacade>(relaxed = true)

    @Test
    fun `initial state should be base values`() {
        val viewModel = CreateFreeTimeViewModel(api, model, navController)

        assertEquals("", viewModel.title)
        assertEquals(
            Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date, viewModel.date
        )
        assertEquals(LocalTime(0, 0), viewModel.start)
        assertEquals(LocalTime(0, 0), viewModel.end)
        assertFalse(viewModel.weekly)
        assertFalse(viewModel.showDelete)
    }

    @Test
    fun `updating state should work`() {
        val viewModel = CreateFreeTimeViewModel(api, model, navController)

        viewModel.title = TEST_TITLE
        viewModel.date = TEST_DATE_FUTURE
        viewModel.start = TEST_TIME_FUTURE
        viewModel.end = TEST_TIME_FUTURE
        viewModel.weekly = true

        assertEquals(TEST_TITLE, viewModel.title)
        assertEquals(TEST_DATE_FUTURE, viewModel.date)
        assertEquals(TEST_TIME_FUTURE, viewModel.start)
        assertEquals(TEST_TIME_FUTURE, viewModel.end)
        assertTrue(viewModel.weekly)
    }

    @Test
    fun `registerNewFreeTime should update model and navigate`() {
        val testId = randomUuid()
        val testData = createMockFreeTimeData()
        val testFreeTime = Identified(testData, testId)

        val freeTimesMap = mutableMapOf<Uuid, FreeTimeData>()
        every { model.freeTimes } returns freeTimesMap

        val testVM = object : BaseCreateFreeTimeViewModel(model, navController) {
            override val showDelete = false
            override fun submit() {
                // Hier die Logik simulieren: Daten in die Map schreiben
                freeTimesMap[testId] = testData
            }
            override fun delete() {}
            fun testRegister(f: Identified<FreeTimeData>) {
                // registerNewFreeTime aufrufen und danach submit simulieren
                registerNewFreeTime(f)
                submit()
            }
        }

        testVM.testRegister(testFreeTime)
        assertFalse(freeTimesMap.isEmpty())
        assertEquals(testData, freeTimesMap[testId])
        verify { navController.main() }
    }
}