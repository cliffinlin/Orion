package com.android.orion.utility;

import java.util.Calendar;

import android.text.TextUtils;

import com.android.orion.Constants;

public class Market {

	private Market() {
	}

	public static int getCalendarDayMinutes(Calendar calendar) {
		return calendar.get(Calendar.HOUR_OF_DAY) * 60
				+ calendar.get(Calendar.MINUTE);
	}

	public static boolean isTimeUp(int begin, int interval) {
		boolean result = false;
		int minutes = 0;
		int remainder = 0;

		if (interval == 0) {
			return true;
		}

		minutes = Market.getCalendarDayMinutes(Calendar.getInstance()) - begin;
		remainder = minutes % interval;

		if (remainder == 0) {
			result = true;
		}

		return result;
	}

	public static int getScheduleMinutes() {
		int result = 0;
		int start = 0;

		Calendar currentCalendar;

		currentCalendar = Calendar.getInstance();
		if (!isWeekday(currentCalendar)) {
			return result;
		}

		start = Constants.STOCK_MARKET_OPEN_MINUTES;

		if (inFirstHalf(currentCalendar)) {
			result = Market.getCalendarDayMinutes(currentCalendar) - start;
		} else if (inSecondHalf(currentCalendar)) {
			result = Market.getCalendarDayMinutes(currentCalendar) - start
					- Constants.STOCK_MARKET_LUNCH_MINUTES;
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

	public static boolean isOpeningHours(Calendar calendar) {
		boolean result = false;
		Calendar currentCalendar;
		Calendar stockMarketOpenCalendar;
		Calendar stockMarketCloseCalendar;

		if (!isWeekday(calendar)) {
			return result;
		}

		currentCalendar = Calendar.getInstance();
		stockMarketOpenCalendar = getStockMarketOpenCalendar(currentCalendar);
		stockMarketCloseCalendar = getStockMarketCloseCalendar(currentCalendar);

		if (currentCalendar.after(stockMarketOpenCalendar)
				&& currentCalendar.before(stockMarketCloseCalendar)) {
			result = true;
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

	public static Calendar getStockMarketCalendar(Calendar calendar,
			String timeString) {
		Calendar result = Calendar.getInstance();
		String dateTimeString = Utility.getCalendarDateString(calendar) + " "
				+ timeString;
		result = Utility.stringToCalendar(dateTimeString,
				Utility.CALENDAR_DATE_TIME_FORMAT);
		return result;
	}

	public static Calendar getStockMarketOpenCalendar(Calendar calendar) {
		return getStockMarketCalendar(calendar,
				Constants.STOCK_MARKET_OPEN_TIME);
	}

	public static Calendar getStockMarketLunchBeginCalendar(Calendar calendar) {
		return getStockMarketCalendar(calendar,
				Constants.STOCK_MARKET_LUNCH_BEGIN_TIME);
	}

	public static Calendar getStockMarketLunchEndCalendar(Calendar calendar) {
		return getStockMarketCalendar(calendar,
				Constants.STOCK_MARKET_LUNCH_END_TIME);
	}

	public static Calendar getStockMarketCloseCalendar(Calendar calendar) {
		return getStockMarketCalendar(calendar,
				Constants.STOCK_MARKET_CLOSE_TIME);
	}

	public static boolean isOutOfDate(String modified) {
		boolean result = false;

		if (TextUtils.isEmpty(modified) || isOutOfDateNotToday(modified)
				|| isOutOfDateFirstHalf(modified)
				|| isOutOfDateSecendHalf(modified)) {
			return true;
		}

		return result;
	}

	public static boolean isOutOfDateNotToday(String modified) {
		boolean result = false;
		String dataString = "";

		if (TextUtils.isEmpty(modified)) {
			return true;
		}

		String[] stringArray = modified.split(" ");
		if (stringArray != null && stringArray.length > 0) {
			dataString = stringArray[0];
		}

		if (!Utility.getCalendarDateString(Calendar.getInstance()).equals(
				dataString)) {
			return true;
		}

		return result;
	}

	public static boolean isOutOfDateFirstHalf(String modified) {
		boolean result = false;

		Calendar currentCalendar;
		Calendar modifiedCalendar;
		Calendar stockMarketLunchBeginCalendar;

		if (TextUtils.isEmpty(modified)) {
			return true;
		}

		currentCalendar = Calendar.getInstance();
		if (!Market.isWeekday(currentCalendar)) {
			return result;
		}
		modifiedCalendar = Utility.stringToCalendar(modified,
				Utility.CALENDAR_DATE_TIME_FORMAT);

		stockMarketLunchBeginCalendar = Market
				.getStockMarketLunchBeginCalendar(currentCalendar);
		if (modifiedCalendar.before(stockMarketLunchBeginCalendar)
				&& currentCalendar.after(stockMarketLunchBeginCalendar)) {
			result = true;
		}

		return result;
	}

	public static boolean isOutOfDateSecendHalf(String modified) {
		boolean result = false;

		Calendar currentCalendar;
		Calendar modifiedCalendar;
		Calendar stockMarketCloseCalendar;

		if (TextUtils.isEmpty(modified)) {
			return true;
		}

		currentCalendar = Calendar.getInstance();
		if (!Market.isWeekday(currentCalendar)) {
			return result;
		}
		modifiedCalendar = Utility.stringToCalendar(modified,
				Utility.CALENDAR_DATE_TIME_FORMAT);

		stockMarketCloseCalendar = Market
				.getStockMarketCloseCalendar(currentCalendar);
		if (modifiedCalendar.before(stockMarketCloseCalendar)
				&& currentCalendar.after(stockMarketCloseCalendar)) {
			result = true;
		}

		return result;
	}
}
