package com.android.orion.utility

import android.text.TextUtils
import java.util.*

class MarketTime {
    val FIRST_HALF_START_TIME = "09:30:00"
    val FIRST_HALF_END_TIME = "11:30:00"
    val SECOND_HALF_START_TIME = "13:00:00"
    val SECOND_HALF_END_TIME = "15:00:00"

    val START_IN_MINUTES = 9 * 60 + 30 //OPEN_TIME
    val LUNCH_TIME_IN_MINUTES = 1 * 60 + 30 //from FIRST_HALF_END_TIME to SECOND_HALF_START_TIME

    fun getMinutesOfToday(calendar: Calendar): Int {
        return (calendar[Calendar.HOUR_OF_DAY] * 60
                + calendar[Calendar.MINUTE])
    }

    fun isWeekday(calendar: Calendar): Boolean {
        val dayOfWeek = calendar[Calendar.DAY_OF_WEEK]
        return dayOfWeek > Calendar.SUNDAY && dayOfWeek < Calendar.SATURDAY
    }

    fun isOutOfDateToday(modified: String): Boolean {
        val result = false
        if (TextUtils.isEmpty(modified)) {
            return true
        }
        return if (!modified.contains(Utility.getCalendarDateString(Calendar.getInstance()))) {
            true
        } else result
    }

    fun getCalendar(
        calendar: Calendar,
        timeString: String
    ): Calendar {
        val result: Calendar
        val dateTimeString = (Utility.getCalendarDateString(calendar) + " "
                + timeString)
        result = Utility.getCalendar(
            dateTimeString,
            Utility.CALENDAR_DATE_TIME_FORMAT
        )
        return result
    }

    fun getFirstHalfStartCalendar(calendar: Calendar): Calendar {
        return getCalendar(
            calendar,
            FIRST_HALF_START_TIME
        )
    }

    fun getFirstHalfEndCalendar(calendar: Calendar): Calendar {
        return getCalendar(
            calendar,
            FIRST_HALF_END_TIME
        )
    }

    fun getSecondHalfStartCalendar(calendar: Calendar): Calendar {
        return getCalendar(
            calendar,
            SECOND_HALF_START_TIME
        )
    }

    fun getSecondHalfEndCalendar(calendar: Calendar): Calendar {
        return getCalendar(
            calendar,
            SECOND_HALF_END_TIME
        )
    }
}