package com.android.orion.utility;

import java.util.Calendar;

import android.text.TextUtils;

public class Market {

	public static final int OPEN_MINUTES = 9 * 60 + 30;
	public static final int LUNCH_MINUTES = 1 * 60 + 30;

	public static final String OPEN_TIME = "09:25:00";
	public static final String LUNCH_BEGIN_TIME = "11:45:00";
	public static final String LUNCH_END_TIME = "12:55:00";
	public static final String CLOSE_TIME = "15:15:00";

	private Market() {
	}

	public static int getCalendarDayMinutes(Calendar calendar) {
		return calendar.get(Calendar.HOUR_OF_DAY) * 60
				+ calendar.get(Calendar.MINUTE);
	}

	public static int getScheduleMinutes() {
		int result = 0;
		int start = 0;

		Calendar currentCalendar;

		currentCalendar = Calendar.getInstance();
		if (!isWeekday(currentCalendar)) {
			return result;
		}

		start = OPEN_MINUTES;

		if (inFirstHalf(currentCalendar)) {
			result = Market.getCalendarDayMinutes(currentCalendar) - start;
		} else if (inSecondHalf(currentCalendar)) {
			result = Market.getCalendarDayMinutes(currentCalendar) - start
					- LUNCH_MINUTES;
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

	public static boolean inFirstHalf(Calendar calendar) {
		boolean result = false;
		Calendar currentCalendar;
		Calendar stockMarketOpenCalendar;
		Calendar stockMarketLunchBeginCalendar;

		if (!isWeekday(calendar)) {
			return result;
		}

		currentCalendar = Calendar.getInstance();
		stockMarketOpenCalendar = getStockMarketOpenCalendar(currentCalendar);
		stockMarketLunchBeginCalendar = getStockMarketLunchBeginCalendar(currentCalendar);

		if (currentCalendar.after(stockMarketOpenCalendar)
				&& currentCalendar.before(stockMarketLunchBeginCalendar)) {
			result = true;
		}

		return result;
	}

	public static boolean inHalfTime(Calendar calendar) {
		boolean result = false;
		Calendar currentCalendar;
		Calendar stockMarketLunchBeginCalendar;
		Calendar stockMarketLunchEndCalendar;

		if (!isWeekday(calendar)) {
			return result;
		}

		currentCalendar = Calendar.getInstance();
		stockMarketLunchBeginCalendar = getStockMarketLunchBeginCalendar(currentCalendar);
		stockMarketLunchEndCalendar = getStockMarketLunchEndCalendar(currentCalendar);

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
		stockMarketLunchEndCalendar = getStockMarketLunchEndCalendar(currentCalendar);
		stockMarketCloseCalendar = getStockMarketCloseCalendar(currentCalendar);

		if (currentCalendar.after(stockMarketLunchEndCalendar)
				&& currentCalendar.before(stockMarketCloseCalendar)) {
			result = true;
		}

		return result;
	}
	
	public static boolean afterStockMarketClose(Calendar calendar) {
		boolean result = false;
		Calendar currentCalendar;
		Calendar stockMarketCloseCalendar;

		if (!isWeekday(calendar)) {
			return result;
		}

		currentCalendar = Calendar.getInstance();
		stockMarketCloseCalendar = getStockMarketCloseCalendar(currentCalendar);

		if (currentCalendar.after(stockMarketCloseCalendar)) {
			result = true;
		}

		return result;
	}
	
	public static Calendar getStockMarketCalendar(Calendar calendar,
			String timeString) {
		Calendar result = Calendar.getInstance();
		String dateTimeString = Utility.getCalendarDateString(calendar) + " "
				+ timeString;
		result = Utility.getCalendar(dateTimeString,
				Utility.CALENDAR_DATE_TIME_FORMAT);
		return result;
	}

	public static Calendar getStockMarketOpenCalendar(Calendar calendar) {
		return getStockMarketCalendar(calendar,
				OPEN_TIME);
	}

	public static Calendar getStockMarketLunchBeginCalendar(Calendar calendar) {
		return getStockMarketCalendar(calendar,
				LUNCH_BEGIN_TIME);
	}

	public static Calendar getStockMarketLunchEndCalendar(Calendar calendar) {
		return getStockMarketCalendar(calendar,
				LUNCH_END_TIME);
	}

	public static Calendar getStockMarketCloseCalendar(Calendar calendar) {
		return getStockMarketCalendar(calendar,
				CLOSE_TIME);
	}

}
