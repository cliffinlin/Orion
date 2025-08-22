package com.android.orion.utility

import android.text.TextUtils
import java.util.*

object Market {
    private const val FIRST_HALF_START_TIME = "09:30:00"
    private const val FIRST_HALF_END_TIME = "11:30:00"
    private const val SECOND_HALF_START_TIME = "13:00:00"
    const val SECOND_HALF_END_TIME = "15:00:00"

    private const val START_IN_MINUTES = 9 * 60 + 30 //FIRST_HALF_START_TIME
    private const val LUNCH_TIME_IN_MINUTES =
        1 * 60 + 30 //from FIRST_HALF_END_TIME to SECOND_HALF_START_TIME

    private fun getMinutesOfToday(): Int {
        val calendar = Calendar.getInstance()
        return (calendar[Calendar.HOUR_OF_DAY] * 60
                + calendar[Calendar.MINUTE])
    }

    @JvmStatic
    fun getScheduleMinutes(): Int {
        return if (!isWeekday()) {
            0
        } else if (isFirstHalf()) {
            getMinutesOfToday() - START_IN_MINUTES
        } else if (isSecondHalf()) {
            getMinutesOfToday() - (START_IN_MINUTES
                    + LUNCH_TIME_IN_MINUTES)
        } else {
            0
        }
    }

    @JvmStatic
    fun isWeekday(): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
        return dayOfWeek > Calendar.SUNDAY && dayOfWeek < Calendar.SATURDAY
    }

    @JvmStatic
    fun isOutofDate(modified: String): Boolean {
        var result = false
        if (TextUtils.isEmpty(modified)) {
            result = true
        }
        if (!modified.contains(Utility.getCalendarDateString(Calendar.getInstance()))) {
            result = true
        }
        return result
    }

    @JvmStatic
    fun isTradingHours(): Boolean {
        return isFirstHalf() || isSecondHalf()
    }

    @JvmStatic
    fun beforeOpen(): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        return if (!isWeekday()) {
            false
        } else calendar.before(getFirstHalfStartCalendar())
    }

    @JvmStatic
    fun isFirstHalf(): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        return if (!isWeekday()) {
            false
        } else calendar.after(getFirstHalfStartCalendar())
                && calendar.before(getFirstHalfEndCalendar())
    }

    @JvmStatic
    fun isLunchTime(): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        return if (!isWeekday()) {
            false
        } else calendar.after(getFirstHalfEndCalendar())
                && calendar.before(getSecondHalfStartCalendar())
    }

    @JvmStatic
    fun isSecondHalf(): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        return if (!isWeekday()) {
            false
        } else calendar.after(getSecondHalfStartCalendar())
                && calendar.before(getSecondHalfEndCalendar())
    }

    @JvmStatic
    fun afterClosed(): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        return if (!isWeekday()) {
            return false
        } else calendar.after(getSecondHalfEndCalendar())
    }

    fun getCalendar(timeString: String): Calendar {
        val calendar: Calendar = Calendar.getInstance()
        val dateTimeString = (Utility.getCalendarDateString(calendar) + " "
                + timeString)
        return Utility.getCalendar(
            dateTimeString,
            Utility.CALENDAR_DATE_TIME_FORMAT
        )
    }

    @JvmStatic
    fun getFirstHalfStartCalendar(): Calendar {
        return getCalendar(FIRST_HALF_START_TIME)
    }

    @JvmStatic
    fun getFirstHalfEndCalendar(): Calendar {
        return getCalendar(
            FIRST_HALF_END_TIME
        )
    }

    @JvmStatic
    fun getSecondHalfStartCalendar(): Calendar {
        return getCalendar(
            SECOND_HALF_START_TIME
        )
    }

    @JvmStatic
    fun getSecondHalfEndCalendar(): Calendar {
        return getCalendar(
            SECOND_HALF_END_TIME
        )
    }
}