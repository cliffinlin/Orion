package com.android.orion.utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.android.orion.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Utility {
	static boolean mLogable = true;

	private Utility() {
	}

	public static boolean getSettingBoolean(Context context, String key) {
		boolean result = false;

		result = PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(key, false);

		return result;
	}

	public static void setSettingBoolean(Context context, String key,
			boolean value) {
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static String getSettingString(Context context, String key) {
		String result = "";

		result = PreferenceManager.getDefaultSharedPreferences(context)
				.getString(key, result);

		return result;
	}

	public static void setSettingString(Context context, String key,
			String value) {
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getNumberString(String textString) {
		String result = "";

		textString = textString.replace("+", "");

		if (TextUtils.isEmpty(textString)) {
			return result;
		}

		if ((textString.length() == 1) && (textString.charAt(0) == '-')) {
			return result;
		}

		result = textString;

		return result;
	}

	public static boolean isNetworkConnected(Context context) {
		boolean result = false;
		ConnectivityManager connectivityManager = null;
		NetworkInfo networkInfo = null;

		if (context == null) {
			return result;
		}

		connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager != null) {
			networkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (networkInfo != null && networkInfo.isConnected()) {
				return true;
			}

			networkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			if (networkInfo != null && networkInfo.isConnected()) {
				return true;
			}
		}

		return result;
	}

	public static String getCalendarString(Calendar calendar, String format) {
		String result = "";

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format,
				Locale.getDefault());

		result = simpleDateFormat.format(calendar.getTime());

		return result;
	}

	public static String getDateString(int dateOffset) {
		Calendar calendar = Calendar.getInstance();

		if (dateOffset != 0) {
			calendar.add(Calendar.DATE, dateOffset);
		}

		return getCalendarString(calendar, Constants.CALENDAR_DATE_FORMAT);
	}

	public static String getCalendarDateString(Calendar calendar) {
		return getCalendarString(calendar, Constants.CALENDAR_DATE_FORMAT);
	}

	public static String getCalendarTimeString(Calendar calendar) {
		return getCalendarString(calendar, Constants.CALENDAR_TIME_FORMAT);
	}

	public static String getCalendarDateTimeString(Calendar calendar) {
		return getCalendarString(calendar, Constants.CALENDAR_DATE_TIME_FORMAT);
	}

	public static String getCurrentDateString() {
		return getCalendarString(Calendar.getInstance(),
				Constants.CALENDAR_DATE_FORMAT);
	}

	public static String getCurrentTimeString() {
		return getCalendarString(Calendar.getInstance(),
				Constants.CALENDAR_TIME_FORMAT);
	}

	public static String getCurrentDateTimeString() {
		return getCalendarString(Calendar.getInstance(),
				Constants.CALENDAR_DATE_TIME_FORMAT);
	}

	public static Calendar stringToCalendar(String string, String format) {
		Calendar calendar = Calendar.getInstance();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format,
				Locale.getDefault());

		Date data = null;

		if (TextUtils.isEmpty(string)) {
			return calendar;
		}

		try {
			data = simpleDateFormat.parse(string);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (data != null) {
			calendar.setTime(data);
		}

		return calendar;
	}

	public static long getMilliSeconds(String dateTime) {
		long nMilliSeconds = 0;
		Date date = null;

		SimpleDateFormat formatter = new SimpleDateFormat(
				Constants.CALENDAR_DATE_TIME_FORMAT, Locale.getDefault());

		try {
			date = formatter.parse(dateTime);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (date != null) {
			nMilliSeconds = date.getTime();
		}

		return nMilliSeconds;
	}

	public static long getMilliSeconds(String data, String time) {
		String dateTime;

		long result = 0;

		if (TextUtils.isEmpty(data)) {
			return result;
		}

		if (TextUtils.isEmpty(time)) {
			time = "00:00:00";
		}

		dateTime = data + " " + time;

		result = getMilliSeconds(dateTime);

		return result;
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

		minutes = Utility.getCalendarDayMinutes(Calendar.getInstance()) - begin;
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
			result = Utility.getCalendarDayMinutes(currentCalendar) - start;
		} else if (inSecondHalf(currentCalendar)) {
			result = Utility.getCalendarDayMinutes(currentCalendar) - start
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
				Constants.CALENDAR_DATE_TIME_FORMAT);
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

	public static String getDataString(String modified) {
		String dataString = null;

		if (modified != null) {
			String[] stringArray = modified.split(" ");
			if (stringArray != null) {
				dataString = stringArray[0];
			}
		}

		return dataString;
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

		if (TextUtils.isEmpty(modified)) {
			return true;
		}

		if (!getCalendarDateString(Calendar.getInstance()).equals(
				getDataString(modified))) {
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
		if (!Utility.isWeekday(currentCalendar)) {
			return result;
		}
		modifiedCalendar = Utility.stringToCalendar(modified,
				Constants.CALENDAR_DATE_TIME_FORMAT);

		stockMarketLunchBeginCalendar = Utility
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
		if (!Utility.isWeekday(currentCalendar)) {
			return result;
		}
		modifiedCalendar = Utility.stringToCalendar(modified,
				Constants.CALENDAR_DATE_TIME_FORMAT);

		stockMarketCloseCalendar = Utility
				.getStockMarketCloseCalendar(currentCalendar);
		if (modifiedCalendar.before(stockMarketCloseCalendar)
				&& currentCalendar.after(stockMarketCloseCalendar)) {
			result = true;
		}

		return result;
	}

	public static void Log(String msg) {
		if (mLogable) {
			android.util.Log.d(Constants.TAG, msg);
		}
	}

	public static double Round(double v, double n) {
		double p = Math.pow(10, n);
		return (Math.round(v * p)) / p;
	}

	public static void setLogable(boolean logable) {
		mLogable = logable;
	}

	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int totalHeight = 0;
		for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}
}
