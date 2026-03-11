package de.pse.oys.ui.view.util

import de.pse.oys.ui.util.toFormattedString
import de.pse.oys.ui.util.toFormattedTimeString
import junit.framework.TestCase.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

import org.junit.Test

class DateTimeFormattingTest {
    @Test
    fun `toFormattedTimeString should format minutes correctly`() {
        assertEquals("00:00", 0.toFormattedTimeString())
        assertEquals("01:30", 90.toFormattedTimeString())
        assertEquals("23:59", 1439.toFormattedTimeString())
    }

    @Test
    fun `toFormattedString should respect different locales`() {
        val date = LocalDate(2024, 1, 1)
        java.util.Locale.setDefault(java.util.Locale.GERMANY)
        assertEquals("01.01.2024", date.toFormattedString())

        java.util.Locale.setDefault(java.util.Locale.US)
        assertEquals("Jan 1, 2024", date.toFormattedString())
    }

    @Test
    fun `LocalDateTime toFormattedString combines date and time with comma`() {
        java.util.Locale.setDefault(java.util.Locale.GERMANY)
        val dateTime = LocalDateTime(2024, 1, 1, 3, 0)
        assertEquals("01.01.2024, 03:00", dateTime.toFormattedString())
    }

    @Test
    fun `Int toFormattedTimeString formats minutes into HH-MM correctly`() {
        assertEquals("00:00", 0.toFormattedTimeString())
        assertEquals("00:05", 5.toFormattedTimeString())
        assertEquals("01:00", 60.toFormattedTimeString())
        assertEquals("10:25", 625.toFormattedTimeString())
    }
}
