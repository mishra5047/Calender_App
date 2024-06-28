package com.example.calenderApp.util

import com.example.calenderApp.adapter.Month
import com.google.api.client.util.DateTime
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * @author Abhishek Mishra
 * Created 19/06/24
 * Utility class to handle calendar related operations.
 */
class CalenderUtil {

    /**
     * Get short month names like Jan, Feb, etc.
     */
    fun getMonthsInShort(): Array<out String>? {
        return DateFormatSymbols().shortMonths
    }

    /**
     * Create a list of Month objects with short month names and their indices.
     */
    fun generateMonths(): List<Month>? {
        return getMonthsInShort()?.mapIndexed { index, month -> Month(month, index) }
    }

    /**
     * Get the current month number (0-indexed).
     */
    fun getCurrentMonthNumber(): Int {
        return LocalDate.now().monthValue - 1
    }

    /**
     * Convert a DateTime string to a formatted date string in IST timezone.
     * Example input: 2024-01-31T12:30:00.000+05:30
     */
    fun getDateInIst(dateTimeString: DateTime?): String {
        return try {
            val parsedDate = ZonedDateTime.parse(
                dateTimeString.toString(),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
            val day = parsedDate.dayOfMonth
            val month = parsedDate.month.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH)
            "$month\n$day"
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Get the current date and time in IST timezone formatted as a string.
     */
    fun getCurrentDateTimeInIST(): String {
        val now = ZonedDateTime.now()
        val istZoneId = ZoneId.of("Asia/Kolkata")
        val dateTimeString =
            now.withZoneSameInstant(istZoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return getDateInIst(DateTime(dateTimeString))
    }

    /**
     * Convert a DateTime string to a single line date string in IST timezone.
     */
    fun getDateInSingleLine(dateTimeString: DateTime?): String {
        return try {
            val parsedDate = ZonedDateTime.parse(
                dateTimeString.toString(),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
            val day = parsedDate.dayOfMonth
            val month = parsedDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            "$month $day"
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Get the current date formatted as "dd-MM-yyyy".
     */
    fun getCurrentDateFormatted(): String {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    }

    /**
     * Convert a date and time string to a DateTime object in IST timezone.
     * Example input: "31-01-2024" and "12:30"
     */
    fun convertTimeToDateTime(dateStr: String, timeStr: String): DateTime? {
        return try {
            val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val timeFormatter =
                DateTimeFormatter.ofPattern("HH:mm") // Handles single digit hours and minutes
            val date = LocalDate.parse(dateStr, dateFormatter)
            val time = LocalTime.parse(timeStr, timeFormatter)

            val localDateTime = LocalDateTime.of(date, time)
            val istZoneId = ZoneId.of("Asia/Kolkata")
            val zonedDateTime = ZonedDateTime.of(localDateTime, istZoneId)
            val dateTimeString = zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            DateTime(dateTimeString)
        } catch (thr: Throwable) {
            null
        }
    }

    /**
     * Get the current time formatted as "HH:mm".
     */
    fun getCurrentTimeFormatted(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    /**
     * Get the current time plus one hour, formatted as "HH:mm".
     */
    fun getCurrentTimePlusOneHour(): String {
        val calendar = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    }

    /**
     * Convert a DateTime string to a time string in IST timezone.
     */
    fun getTimeInIst(dateTimeString: DateTime?): String {
        return try {
            val zonedDateTime = ZonedDateTime.parse(
                dateTimeString.toString(),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
            val istDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
            istDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Get all dates between two DateTime objects.
     */
    fun getAllDatesBetween(startDateTime: DateTime, endDateTime: DateTime): Set<DateTime> {
        val calendar = Calendar.getInstance()
        val dates = mutableListOf<DateTime>()
        val endDate = Date(endDateTime.value)

        calendar.time = Date(startDateTime.value)
        while (!calendar.time.after(endDate)) {
            dates.add(DateTime(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return dates.toSet()
    }

    /**
     * Get pairs of five days for each month.
     */
    fun getPairOfFiveForAllMonths(): ArrayList<ArrayList<Pair<DateTime, DateTime>>> {
        val listOf5DaysPerMonth = ArrayList<ArrayList<Pair<DateTime, DateTime>>>()
        for (i in 0..11) { // Months are 0-based in Calendar
            listOf5DaysPerMonth.add(getDaysInGroupsOfFive(i))
        }
        return listOf5DaysPerMonth
    }

    /**
     * Get groups of five days for a given month.
     * @param month Month index (0-based)
     * @return Pairs of five days for the given month.
     * Example: January 2024 has 31 days, so it will be divided into
     * [1-5], [6-10], [11-15], [16-20], [21-25], [26-31]
     * Done as improvement to allow finding days with different months.
     */
    private fun getDaysInGroupsOfFive(month: Int): ArrayList<Pair<DateTime, DateTime>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, month)  // Calendar months are 0-based
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val datePairs = ArrayList<Pair<DateTime, DateTime>>()

        var startDay = 1
        while (startDay <= totalDays) {
            calendar.set(Calendar.DAY_OF_MONTH, startDay)
            val startDate = DateTime(calendar.time)
            var endDay = startDay + 4
            if (endDay > totalDays) endDay = totalDays
            calendar.set(Calendar.DAY_OF_MONTH, endDay)
            val endDate = DateTime(calendar.time)
            datePairs.add(Pair(startDate, endDate))
            startDay += 5
        }
        return datePairs
    }
}