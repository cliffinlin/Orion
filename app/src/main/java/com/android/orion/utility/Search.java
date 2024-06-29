package com.android.orion.utility;

import android.text.TextUtils;

import com.android.orion.database.ShareBonus;
import com.android.orion.database.StockData;
import com.android.orion.database.StockFinancial;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

public class Search {

	Comparator<StockFinancial> comparator = new Comparator<StockFinancial>() {

		@Override
		public int compare(StockFinancial arg0, StockFinancial arg1) {
			Calendar calendar0;
			Calendar calendar1;

			if (arg0 == null || arg1 == null) {
				return 0;
			}

			calendar0 = Utility.getCalendar(arg0.getDate(),
					Utility.CALENDAR_DATE_TIME_FORMAT);
			calendar1 = Utility.getCalendar(arg1.getDate(),
					Utility.CALENDAR_DATE_TIME_FORMAT);
			if (calendar1.before(calendar0)) {
				return -1;
			} else if (calendar1.after(calendar0)) {
				return 1;
			} else {
				return 0;
			}
		}
	};

	static int binarySearch(int[] arr, int l, int r, int x) {
		if (r >= l) {
			int mid = l + (r - l) / 2;

			// If the element is present at the
			// middle itself
			if (arr[mid] == x)
				return mid;

			// If element is smaller than mid, then
			// it can only be present in left subarray
			if (arr[mid] > x)
				return binarySearch(arr, l, mid - 1, x);

			// Else the element can only be present
			// in right subarray
			return binarySearch(arr, mid + 1, r, x);
		}

		// We reach here when element is not present
		// in array
		return -1;
	}

	static int binarySearchStockData(int l, int r, Calendar calendar,
									 ArrayList<StockData> stockDataList) {
		if (r >= l) {
			int mid = l + (r - l) / 2;

			Calendar calendarMid = Utility.getCalendar(stockDataList
					.get(mid).getDateTime(), Utility.CALENDAR_DATE_TIME_FORMAT);

			// If the element is present at the
			// middle itself
			if (calendarMid.equals(calendar))
				return mid;

			// If element is smaller than mid, then
			// it can only be present in left subarray
			if (calendar.before(calendarMid))
				return binarySearchStockData(l, mid - 1, calendar,
						stockDataList);

			Calendar calendarMid1 = Utility.getCalendar(stockDataList
					.get(mid + 1).getDateTime(), Utility.CALENDAR_DATE_TIME_FORMAT);
			if (calendar.after(calendarMid) && (calendar.before(calendarMid1)))
				return mid;

			// Else the element can only be present
			// in right subarray
			return binarySearchStockData(mid + 1, r, calendar,
					stockDataList);
		}

		// We reach here when element is not present
		// in array
		return -1;
	}

	public static StockData getStockDataByDateTime(String dateTimeString,
	                                               ArrayList<StockData> stockDataList) {
		int index = 0;
		StockData stockData = null;

		if (stockDataList == null || stockDataList.size() < 1) {
			return stockData;
		}

		if (TextUtils.isEmpty(dateTimeString)) {
			return stockData;
		}

		if (TextUtils.equals(dateTimeString, stockDataList
				.get(0).getDateTime())) {
			return stockDataList.get(0);
		}

		if (TextUtils.equals(dateTimeString, stockDataList
				.get(stockDataList.size() - 1).getDateTime())) {
			return stockDataList.get(stockDataList.size() - 1);
		}

		Calendar calendar = Utility.getCalendar(dateTimeString,
				Utility.CALENDAR_DATE_TIME_FORMAT);
		Calendar calendarMin = Utility.getCalendar(stockDataList
				.get(0).getDateTime(), Utility.CALENDAR_DATE_TIME_FORMAT);
		Calendar calendarMax = Utility.getCalendar(
				stockDataList.get(stockDataList.size() - 1).getDateTime(),
				Utility.CALENDAR_DATE_TIME_FORMAT);

		if (calendar.before(calendarMin)) {
			return stockData;
		} else if (calendar.after(calendarMax)) {
			return stockData;//stockDataList.get(stockDataList.size() - 1);
		} else {
			index = binarySearchStockData(0, stockDataList.size() - 1,
					calendar, stockDataList);

			if ((index > 0) && (index < stockDataList.size())) {
				stockData = stockDataList.get(index);
			}
		}

		return stockData;
	}

	static int binarySearchStockFinancial(int l, int r, Calendar calendar,
										  ArrayList<StockFinancial> stockFinancialList) {
		if (r >= l) {
			int mid = l + (r - l) / 2;

			Calendar calendarMid = Utility.getCalendar(stockFinancialList
					.get(mid).getDate(), Utility.CALENDAR_DATE_FORMAT);

			// If the element is present at the
			// middle itself
			if (calendarMid.equals(calendar))
				return mid;

			// If element is smaller than mid, then
			// it can only be present in left subarray
			if (calendar.before(calendarMid))
				return binarySearchStockFinancial(l, mid - 1, calendar,
						stockFinancialList);

			Calendar calendarMid1 = Utility.getCalendar(stockFinancialList
					.get(mid + 1).getDate(), Utility.CALENDAR_DATE_FORMAT);
			if (calendar.after(calendarMid) && (calendar.before(calendarMid1)))
				return mid;

			// Else the element can only be present
			// in right subarray
			return binarySearchStockFinancial(mid + 1, r, calendar,
					stockFinancialList);
		}

		// We reach here when element is not present
		// in array
		return -1;
	}

	public static StockFinancial getStockFinancialByDate(String dateString,
	                                                     ArrayList<StockFinancial> stockFinancialList) {
		int index = 0;
		StockFinancial stockFinancial = null;

		if (stockFinancialList == null || stockFinancialList.size() < 1) {
			return stockFinancial;
		}

		if (TextUtils.isEmpty(dateString)) {
			return stockFinancial;
		}

		Calendar calendar = Utility.getCalendar(dateString,
				Utility.CALENDAR_DATE_FORMAT);
		Calendar calendarMin = Utility.getCalendar(stockFinancialList
				.get(0).getDate(), Utility.CALENDAR_DATE_FORMAT);
		Calendar calendarMax = Utility.getCalendar(
				stockFinancialList.get(stockFinancialList.size() - 1).getDate(),
				Utility.CALENDAR_DATE_FORMAT);

		if (calendar.before(calendarMin)) {
			return stockFinancial;
		} else if (calendar.after(calendarMax)) {
			return stockFinancialList.get(stockFinancialList.size() - 1);
		} else {
			index = binarySearchStockFinancial(0, stockFinancialList.size() - 1,
					calendar, stockFinancialList);

			if ((index > 0) && (index < stockFinancialList.size())) {
				stockFinancial = stockFinancialList.get(index);
			}
		}

		return stockFinancial;
	}

	static int binarySearchShareBonus(int l, int r, Calendar calendar,
									  ArrayList<ShareBonus> shareBonusList) {
		if (r >= l) {
			int mid = l + (r - l) / 2;

			Calendar calendarMid = Utility.getCalendar(
					shareBonusList.get(mid).getDate(),
					Utility.CALENDAR_DATE_FORMAT);

			// If the element is present at the
			// middle itself
			if (calendarMid.equals(calendar))
				return mid;

			// If element is smaller than mid, then
			// it can only be present in left subarray
			if (calendar.before(calendarMid))
				return binarySearchShareBonus(l, mid - 1, calendar,
						shareBonusList);

			Calendar calendarMid1 = Utility.getCalendar(shareBonusList
					.get(mid + 1).getDate(), Utility.CALENDAR_DATE_FORMAT);
			if (calendar.after(calendarMid) && (calendar.before(calendarMid1)))
				return mid;

			// Else the element can only be present
			// in right subarray
			return binarySearchShareBonus(mid + 1, r, calendar, shareBonusList);
		}

		// We reach here when element is not present
		// in array
		return -1;
	}

	public static ShareBonus getShareBonusByDate(String dateString,
	                                             ArrayList<ShareBonus> shareBonusList) {
		int index = 0;
		ShareBonus shareBonus = null;

		if (shareBonusList == null || shareBonusList.size() < 1) {
			return shareBonus;
		}

		if (TextUtils.isEmpty(dateString)) {
			return shareBonus;
		}

		Calendar calendar = Utility.getCalendar(dateString,
				Utility.CALENDAR_DATE_FORMAT);
		Calendar calendarMin = Utility.getCalendar(shareBonusList.get(0)
				.getDate(), Utility.CALENDAR_DATE_FORMAT);
		Calendar calendarMax = Utility.getCalendar(
				shareBonusList.get(shareBonusList.size() - 1).getDate(),
				Utility.CALENDAR_DATE_FORMAT);

		if (calendar.before(calendarMin)) {
			return shareBonus;
		} else if (calendar.after(calendarMax)) {
			return shareBonusList.get(shareBonusList.size() - 1);
		} else {
			index = binarySearchShareBonus(0, shareBonusList.size() - 1,
					calendar, shareBonusList);

			if ((index > 0) && (index < shareBonusList.size())) {
				shareBonus = shareBonusList.get(index);
			}
		}

		return shareBonus;
	}
}
