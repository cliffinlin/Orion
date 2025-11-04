package com.android.orion.utility;

import java.util.Calendar;

public class Market {
    private static final String FIRST_HALF_START_TIME = "09:30:00";
    private static final String FIRST_HALF_END_TIME = "11:30:00";
    private static final String SECOND_HALF_START_TIME = "13:00:00";
    public static final String SECOND_HALF_END_TIME = "15:00:00";

    private static final int START_IN_MINUTES = 9 * 60 + 30; //FIRST_HALF_START_TIME
    private static final int LUNCH_TIME_IN_MINUTES = 1 * 60 + 30; //from FIRST_HALF_END_TIME to SECOND_HALF_START_TIME

    private static int getMinutesOfToday() {
        Calendar calendar = Calendar.getInstance();
        return (calendar.get(Calendar.HOUR_OF_DAY) * 60
                + calendar.get(Calendar.MINUTE));
    }

    public static int getScheduleMinutes() {
        if (!isWeekday()) {
            return 0;
        } else if (isFirstHalf()) {
            return getMinutesOfToday() - START_IN_MINUTES;
        } else if (isSecondHalf()) {
            return getMinutesOfToday() - (START_IN_MINUTES
                    + LUNCH_TIME_IN_MINUTES);
        } else {
            return 0;
        }
    }

    public static boolean isWeekday() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek > Calendar.SUNDAY && dayOfWeek < Calendar.SATURDAY;
    }

    public static boolean isOutofDate(String modified) {
        boolean result = false;
        if (modified == null || modified.isEmpty()) {
            result = true;
        }
        if (!modified.contains(Utility.getCalendarDateString(Calendar.getInstance()))) {
            result = true;
        }
        return result;
    }

    public static boolean isTradingHours() {
        return isFirstHalf() || isSecondHalf();
    }

    public static boolean beforeOpen() {
        Calendar calendar = Calendar.getInstance();
        if (!isWeekday()) {
            return false;
        } else {
            return calendar.before(getFirstHalfStartCalendar());
        }
    }

    public static boolean isFirstHalf() {
        Calendar calendar = Calendar.getInstance();
        if (!isWeekday()) {
            return false;
        } else {
            return calendar.after(getFirstHalfStartCalendar())
                    && calendar.before(getFirstHalfEndCalendar());
        }
    }

    public static boolean isLunchTime() {
        Calendar calendar = Calendar.getInstance();
        if (!isWeekday()) {
            return false;
        } else {
            return calendar.after(getFirstHalfEndCalendar())
                    && calendar.before(getSecondHalfStartCalendar());
        }
    }

    public static boolean isSecondHalf() {
        Calendar calendar = Calendar.getInstance();
        if (!isWeekday()) {
            return false;
        } else {
            return calendar.after(getSecondHalfStartCalendar())
                    && calendar.before(getSecondHalfEndCalendar());
        }
    }

    public static boolean afterClosed() {
        Calendar calendar = Calendar.getInstance();
        if (!isWeekday()) {
            return false;
        } else {
            return calendar.after(getSecondHalfEndCalendar());
        }
    }

    private static Calendar getCalendar(String timeString) {
        Calendar calendar = Calendar.getInstance();
        String dateTimeString = (Utility.getCalendarDateString(calendar) + " "
                + timeString);
        return Utility.getCalendar(
                dateTimeString,
                Utility.CALENDAR_DATE_TIME_FORMAT
        );
    }

    public static Calendar getFirstHalfStartCalendar() {
        return getCalendar(FIRST_HALF_START_TIME);
    }

    public static Calendar getFirstHalfEndCalendar() {
        return getCalendar(FIRST_HALF_END_TIME);
    }

    public static Calendar getSecondHalfStartCalendar() {
        return getCalendar(SECOND_HALF_START_TIME);
    }

    public static Calendar getSecondHalfEndCalendar() {
        return getCalendar(SECOND_HALF_END_TIME);
    }
}