package com.android.orion.utility;

import android.text.TextUtils;

import java.util.Calendar;

public class Market {

	public static final int OPEN_TIME_IN_MINUTES = 9 * 60 + 30;
	public static final int LUNCH_TIME_IN_MINUTES = 1 * 60 + 30;

	public static final String OPEN_TIME = "09:30:00";
	public static final String LUNCH_BEGIN_TIME = "11:30:00";
	public static final String LUNCH_END_TIME = "13:00:00";
	public static final String CLOSE_TIME = "15:00:00";

	private Market() {
	}

	public static int getMinutesOfToday(Calendar calendar) {
		return calendar.get(Calendar.HOUR_OF_DAY) * 60
				+ calendar.get(Calendar.MINUTE);
	}

	public static int getScheduleMinutes() {
		int result = 0;
		int start = 0;

		Calendar calendar = Calendar.getInstance();
		if (!isWeekday(calendar)) {
			return result;
		}

		start = OPEN_TIME_IN_MINUTES;

		if (inFirstHalf(calendar)) {
			result = Market.getMinutesOfToday(calendar) - start;
		} else if (inSecondHalf(calendar)) {
			result = Market.getMinutesOfToday(calendar) - (start
					+ LUNCH_TIME_IN_MINUTES);
		} else {
			result = 0;
		}

		return result;
	}

	public static boolean isWeekday(Calendar calendar) {
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		boolean result = false;

		if (dayOfWeek > Calendar.SUNDAY && dayOfWeek < Calendar.SATURDAY) {
			result = true;
		}

		return result;
	}

	public static boolean isOutOfDateToday(String modified) {
		boolean result = false;

		if (TextUtils.isEmpty(modified)) {
			return true;
		}

		if (!modified.contains(Utility.getCalendarDateString(Calendar.getInstance()))) {
			return true;
		}

		return result;
	}

	public static boolean isTradingHours(Calendar calendar) {
		return inFirstHalf(calendar) || inSecondHalf(calendar);
	}

	public static boolean beforeOpen(Calendar calendar) {
		boolean result = false;
		Calendar currentCalendar;
		Calendar stockMarketOpenCalendar;

		if (!isWeekday(calendar)) {
			return result;
		}

		currentCalendar = Calendar.getInstance();
		stockMarketOpenCalendar = getMarketOpenCalendar(currentCalendar);

		if (currentCalendar.before(stockMarketOpenCalendar)) {
			result = true;
		}

		return result;
	}

	public static boolean inFirstHalf(Calendar calendar) {
		boolean result = false;
		Calendar currentCalendar;
		Calendar stockMarketOpenCalendar;
		Calendar stockMarketLunchBeginCalendar;

		if (!isWeekday(calendar)) {
			return result;
		}

		currentCalendar = Calendar.getInstance();
		stockMarketOpenCalendar = getMarketOpenCalendar(currentCalendar);
		stockMarketLunchBeginCalendar = getMarketLunchBeginCalendar(currentCalendar);

		if (currentCalendar.after(stockMarketOpenCalendar)
				&& currentCalendar.before(stockMarketLunchBeginCalendar)) {
			result = true;
		}

		return result;
	}

	public static boolean isLunchTime(Calendar calendar) {
		boolean result = false;
		Calendar currentCalendar;
		Calendar stockMarketLunchBeginCalendar;
		Calendar stockMarketLunchEndCalendar;

		if (!isWeekday(calendar)) {
			return result;
		}

		currentCalendar = Calendar.getInstance();
		stockMarketLunchBeginCalendar = getMarketLunchBeginCalendar(currentCalendar);
		stockMarketLunchEndCalendar = getMarketLunchEndCalendar(currentCalendar);

		if (currentCalendar.after(stockMarketLunchBeginCalendar)
				&& currentCalendar.before(stockMarketLunchEndCalendar)) {
			result = true;
		}

		return result;
	}

	public static boolean inSecondHalf(Calendar calendar) {
		boolean result = false;
		Calendar currentCalendar;
		Calendar stockMarketLunchEndCalendar;
		Calendar stockMarketCloseCalendar;

		if (!isWeekday(calendar)) {
			return result;
		}

		currentCalendar = Calendar.getInstance();
		stockMarketLunchEndCalendar = getMarketLunchEndCalendar(currentCalendar);
		stockMarketCloseCalendar = getMarketCloseCalendar(currentCalendar);

		if (currentCalendar.after(stockMarketLunchEndCalendar)
				&& currentCalendar.before(stockMarketCloseCalendar)) {
			result = true;
		}

		return result;
	}

	public static boolean afterClosed(Calendar calendar) {
		boolean result = false;
		Calendar currentCalendar;
		Calendar stockMarketCloseCalendar;

		if (!isWeekday(calendar)) {
			return result;
		}

		currentCalendar = Calendar.getInstance();
		stockMarketCloseCalendar = getMarketCloseCalendar(currentCalendar);

		if (currentCalendar.after(stockMarketCloseCalendar)) {
			result = true;
		}

		return result;
	}

	public static Calendar getMarketCalendar(Calendar calendar,
											 String timeString) {
		Calendar result;
		String dateTimeString = Utility.getCalendarDateString(calendar) + " "
				+ timeString;
		result = Utility.getCalendar(dateTimeString,
				Utility.CALENDAR_DATE_TIME_FORMAT);
		return result;
	}

	public static Calendar getMarketOpenCalendar(Calendar calendar) {
		return getMarketCalendar(calendar,
				OPEN_TIME);
	}

	public static Calendar getMarketLunchBeginCalendar(Calendar calendar) {
		return getMarketCalendar(calendar,
				LUNCH_BEGIN_TIME);
	}

	public static Calendar getMarketLunchEndCalendar(Calendar calendar) {
		return getMarketCalendar(calendar,
				LUNCH_END_TIME);
	}

	public static Calendar getMarketCloseCalendar(Calendar calendar) {
		return getMarketCalendar(calendar,
				CLOSE_TIME);
	}
}
