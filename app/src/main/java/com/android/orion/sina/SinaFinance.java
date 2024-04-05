package com.android.orion.sina;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.ArrayMap;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.orion.R;
import com.android.orion.config.Config;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.TotalShare;
import com.android.orion.provider.StockDataProvider;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Market;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Utility;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import okhttp3.Request;
import okhttp3.Response;

public class SinaFinance extends StockDataProvider {

	public static final String SINA_FINANCE_URL_HQ_NODE_DATA = "http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?";
	public static final String SINA_FINANCE_URL_HQ_KLINE_DATA = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?";
	public static final String SINA_FINANCE_URL_HQ_JS_LIST = "http://hq.sinajs.cn/list=";
	public static final String SINA_FINANCE_URL_HQ_JS_LIST_SIMPLE = "http://hq.sinajs.cn/list=s_";
	public static final String SINA_FINANCE_URL_VFD_FINANCESUMMARY = "http://money.finance.sina.com.cn/corp/go.php/vFD_FinanceSummary/stockid/";// stock_id.phtml
	public static final String SINA_FINANCE_URL_VFD_FINANCEREPORT2022 = "https://quotes.sina.cn/cn/api/openapi.php/CompanyFinanceService.getFinanceReport2022?paperCode=";
	public static final String SINA_FINANCE_URL_VISSUE_SHAREBONUS = "http://vip.stock.finance.sina.com.cn/corp/go.php/vISSUE_ShareBonus/stockid/";// stock_id.phtml
	public static final String SINA_FINANCE_URL_VCI_STOCK_STRUCTURE_HISTORY = "http://vip.stock.finance.sina.com.cn/corp/go.php/vCI_StockStructureHistory/stockid/";// stocktype/TotalStock.phtml
	public static final String SINA_FINANCE_URL_NEWSTOCK_ISSUE = "https://vip.stock.finance.sina.com.cn/corp/go.php/vRPD_NewStockIssue/page/1.phtml";
	public static final String SINA_FINANCE_HEAD_REFERER_KEY = "Referer";
	public static final String SINA_FINANCE_HEAD_REFERER_VALUE = "http://vip.stock.finance.sina.com.cn/";

	public static final int DOWNLOAD_HISTORY_LENGTH_DEFAULT = 120;
	public static final int DOWNLOAD_HISTORY_LENGTH_NONE = 0;
	public static final int DOWNLOAD_HISTORY_LENGTH_UNLIMITED = -1;

	public static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN5 = 242;
	public static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN15 = 192;
	public static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN30 = 192;
	public static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN60 = 192;
	private static SinaFinance mInstance;
	ArrayList<String> mAccessDeniedStringArray = new ArrayList<>();
	Logger Log = Logger.getLogger();

	private SinaFinance() {
		super();

		mAccessDeniedStringArray.add(mContext.getResources().getString(
				R.string.access_denied_jp));
		mAccessDeniedStringArray.add(mContext.getResources().getString(
				R.string.access_denied_zh));
		mAccessDeniedStringArray.add(mContext.getResources().getString(
				R.string.access_denied_default));
	}

	public static SinaFinance getInstance() {
		synchronized (SinaFinance.class) {
			if (mInstance == null) {
				mInstance = new SinaFinance();
			}
		}
		return mInstance;
	}

	public int getAvailableHistoryLength(String period) {
		if (period.equals(DatabaseContract.COLUMN_MIN1)) {
			return DOWNLOAD_HISTORY_LENGTH_NONE;
		} else if (period.equals(DatabaseContract.COLUMN_MIN5)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN5;
		} else if (period.equals(DatabaseContract.COLUMN_MIN15)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN15;
		} else if (period.equals(DatabaseContract.COLUMN_MIN30)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN30;
		} else if (period.equals(DatabaseContract.COLUMN_MIN60)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN60;
		} else if (period.equals(DatabaseContract.COLUMN_DAY)) {
			return DOWNLOAD_HISTORY_LENGTH_UNLIMITED;
		} else if (period.equals(DatabaseContract.COLUMN_WEEK)) {
			return DOWNLOAD_HISTORY_LENGTH_UNLIMITED;
		} else if (period.equals(DatabaseContract.COLUMN_MONTH)) {
			return DOWNLOAD_HISTORY_LENGTH_UNLIMITED;
		} else if (period.equals(DatabaseContract.COLUMN_QUARTER)) {
			return DOWNLOAD_HISTORY_LENGTH_NONE;
		} else if (period.equals(DatabaseContract.COLUMN_YEAR)) {
			return DOWNLOAD_HISTORY_LENGTH_NONE;
		} else {
		}

		return 0;
	}

	public ArrayMap<String, String> getRequestHeader() {
		ArrayMap<String, String> result = new ArrayMap<>();

		result.put(SINA_FINANCE_HEAD_REFERER_KEY, SINA_FINANCE_HEAD_REFERER_VALUE);

		return result;
	}

	public String getStockInformationURLString(Stock stock) {
		String urlString = "";
		if (stock == null) {
			return urlString;
		}
		urlString = SINA_FINANCE_URL_HQ_JS_LIST + stock.getSE()
				+ stock.getCode() + "_i";
		return urlString;
	}

	public String getStockRealTimeURLString(Stock stock) {
		String urlString = "";
		if (stock == null) {
			return urlString;
		}
		urlString = SINA_FINANCE_URL_HQ_JS_LIST_SIMPLE + stock.getSE()
				+ stock.getCode();
		return urlString;
	}

	public String getStockHSAURLString() {
		String page = "&page=1";
		String num = "&num=1000000";
		String sort = "&sort=symbol";
		String asc = "&asc=1";
		String node = "&node=hs_a";
		String urlString = SINA_FINANCE_URL_HQ_NODE_DATA + page + num + sort
				+ asc + node;
		return urlString;
	}

	public String getStockDataHistoryURLString(Stock stock, StockData stockData,
											   int len) {
		String symbol = "";
		String scale = "";
		String ma = "";
		String datalen = "";
		String urlString = "";

		if ((stock == null) || (stockData == null)) {
			return urlString;
		}

		symbol = "&symbol=" + stock.getSE() + stock.getCode();
		scale = "&scale=" + getPeriodMinutes(stockData.getPeriod());
		ma = "&ma=" + "no";
		datalen = "&datalen=" + len;

		urlString = SINA_FINANCE_URL_HQ_KLINE_DATA + symbol + scale + ma
				+ datalen;
		return urlString;
	}

	public String getStockDataRealTimeURLString(Stock stock) {
		String urlString = "";
		if (stock == null) {
			return urlString;
		}
		urlString = SINA_FINANCE_URL_HQ_JS_LIST + stock.getSE()
				+ stock.getCode();
		return urlString;
	}

	public String getStockFinancialURLString(Stock stock) {
		String urlString = "";
		if (stock == null) {
			return urlString;
		}

		urlString = SINA_FINANCE_URL_VFD_FINANCEREPORT2022
				+ stock.getSE() + stock.getCode()
				+ "&source=gjzb&type=0&page=1&num=10000";

		return urlString;
	}

	public String getShareBonusURLString(Stock stock) {
		String urlString = "";
		if (stock == null) {
			return urlString;
		}
		urlString = SINA_FINANCE_URL_VISSUE_SHAREBONUS + stock.getCode()
				+ ".phtml";
		return urlString;
	}

	public String getTotalShareURLString(Stock stock) {
		String urlString = "";
		if (stock == null) {
			return urlString;
		}
		urlString = SINA_FINANCE_URL_VCI_STOCK_STRUCTURE_HISTORY
				+ stock.getCode() + "/stocktype/TotalStock.phtml";
		return urlString;
	}

	public int getDownloadHistoryLengthDefault(String period) {
		int result = 0;
		int availableHistoryLength = 0;

		availableHistoryLength = getAvailableHistoryLength(period);

		if (availableHistoryLength > 0) {
			result = availableHistoryLength;
		} else if (availableHistoryLength == DOWNLOAD_HISTORY_LENGTH_UNLIMITED) {
			result = DOWNLOAD_HISTORY_LENGTH_DEFAULT;
		}

		return result;
	}

	private int getDownloadStockDataLength(StockData stockData) {
		int result = 0;
		int defaultValue = 0;
		int scheduleMinutes = 0;
		long stockId = 0;
		String period = "";
		String modified = "";
		String selection = null;
		String sortOrder = null;
		Cursor cursor = null;

		try {
			if (stockData == null) {
				return result;
			}

			stockId = stockData.getStockId();
			period = stockData.getPeriod();

			defaultValue = getDownloadHistoryLengthDefault(period);

			selection = mStockDatabaseManager.getStockDataSelection(stockId,
					period, StockData.LEVEL_NONE);
			sortOrder = mStockDatabaseManager.getStockDataOrder();
			cursor = mStockDatabaseManager.queryStockData(selection, null,
					sortOrder);

			if ((cursor == null) || (cursor.getCount() == 0)) {
				return defaultValue;
			}

			cursor.moveToLast();
			stockData.set(cursor);

			modified = stockData.getModified();
			if (Market.isOutOfDateToday(modified)) {
				mStockDatabaseManager.deleteStockData(stockId, period);
				return defaultValue;
			}

			if (!Market.isWeekday(Calendar.getInstance())) {
				return result;
			}

			Calendar modifiedCalendar = Utility.getCalendar(modified,
					Utility.CALENDAR_DATE_TIME_FORMAT);
			Calendar stockMarketLunchBeginCalendar = Market
					.getMarketLunchBeginCalendar(Calendar
							.getInstance());
			Calendar stockMarketCloseCalendar = Market
					.getMarketCloseCalendar(Calendar.getInstance());

			if (Market.isTradingHours(Calendar.getInstance())) {
				scheduleMinutes = Market.getScheduleMinutes();
				if (scheduleMinutes != 0) {
					result = 1;

					switch (period) {
						case DatabaseContract.COLUMN_MIN60:
							result += scheduleMinutes
									/ Constant.MIN60;
							break;
						case DatabaseContract.COLUMN_MIN30:
							result += scheduleMinutes
									/ Constant.MIN30;
							break;
						case DatabaseContract.COLUMN_MIN15:
							result += scheduleMinutes
									/ Constant.MIN15;
							break;
						case DatabaseContract.COLUMN_MIN5:
							result += scheduleMinutes
									/ Constant.MIN5;
							break;
					}
				}
			} else if (Market.isLunchTime(Calendar.getInstance())) {
				if (period.equals(DatabaseContract.COLUMN_MONTH)
						|| period.equals(DatabaseContract.COLUMN_WEEK)
						|| period.equals(DatabaseContract.COLUMN_DAY)) {
					if (Market.isOutOfDateToday(stockData.getDate())) {
						result = 1;
					}
					return result;
				}

				if (modifiedCalendar.after(stockMarketLunchBeginCalendar)) {
					return result;
				}

				switch (period) {
					case DatabaseContract.COLUMN_MIN60:
						result = 2;
						break;
					case DatabaseContract.COLUMN_MIN30:
						result = 4;
						break;
					case DatabaseContract.COLUMN_MIN15:
						result = 8;
						break;
					case DatabaseContract.COLUMN_MIN5:
						result = 24;
						break;
				}
			} else if (Market.afterClosed(Calendar.getInstance())) {
				if (modifiedCalendar.after(stockMarketCloseCalendar)) {
					return result;
				}

				switch (period) {
					case DatabaseContract.COLUMN_MONTH:
					case DatabaseContract.COLUMN_WEEK:
					case DatabaseContract.COLUMN_DAY:
						result = 1;
						break;
					case DatabaseContract.COLUMN_MIN60:
						result = 4;
						break;
					case DatabaseContract.COLUMN_MIN30:
						result = 8;
						break;
					case DatabaseContract.COLUMN_MIN15:
						result = 16;
						break;
					case DatabaseContract.COLUMN_MIN5:
						result = 48;
						break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}

		return result;
	}

	public int downloadStockInformation(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		if (System.currentTimeMillis() - Setting.getDownloadStockInformationTimemillis(stock.getSE(), stock.getCode()) < Config.downloadStockInformationInterval) {
			return result;
		}

		return downloadStockInformation(stock, getRequestHeader(), getStockInformationURLString(stock));
	}

	private int downloadStockInformation(Stock stock, ArrayMap<String, String> requestHeaderArray, String urlString) {
		int result = RESULT_NONE;

		Log.d(urlString);

		Request.Builder builder = new Request.Builder();
		for (int i = 0; i < requestHeaderArray.size(); i++) {
			builder.addHeader(requestHeaderArray.keyAt(i), requestHeaderArray.valueAt(i));
		}
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return RESULT_FAILED;
				} else {
					result = RESULT_SUCCESS;
				}

				handleResponseStockInformation(stock, resultString);
				Thread.sleep(Config.downloadSleepInterval);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseStockInformation(Stock stock, String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		String[] keyValue = null;
		String[] codeInfo = null;
		String[] stockInfo = null;
//var hq_str_sh600036_i="A,zsyh,4.6100,5.2827,4.1500,31.6900,4255.813,2521984.5601,2062894.4429,2062894.4429,0,CNY,1199.2200,1332.2900,7.3000,1,13.6650,2648.3300,1069.2200,51.939,26.3,0.1,招商银行,X|O|0|0|0,39.71|32.49,20220930|35640666666.67,697.4600|89.7750,|,,1/1,EQA,,4.17,46.678|34.960|34.390,股份制银行Ⅱ,,1,344676000000";
		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d("return, stock = "
					+ stock + " response = " + response);
			return;
		}

		try {
			keyValue = response.trim().split("=");

			if (keyValue == null || keyValue.length != 2) {
				Log.d("return, keyValue == null || keyValue.length != 2");
				return;
			}

			if (keyValue[0] == null) {
				Log.d("return, keyValue[0] == null");
				return;
			}

			codeInfo = keyValue[0].trim().split("_");

			if (codeInfo == null || codeInfo.length != 4) {
				Log.d("return, codeInfo == null || codeInfo.length != 4");
				return;
			}

			if (!stock.getSE().equals(codeInfo[2].substring(0, 2))
					|| !stock.getCode().equals(codeInfo[2].substring(2, 8))) {
				Log.d("return, ...");
				return;
			}

			if (keyValue[1] == null) {
				Log.d("return, keyValue[1] == null");
				return;
			}

			stockInfo = keyValue[1].substring(1, keyValue[1].length() - 2)
					.split(",");

			if (stockInfo == null || stockInfo.length < 8) {
				Log.d("return, stockInfo == null || stockInfo.length < 8");
				return;
			}

			if (!TextUtils.isEmpty(stockInfo[0])) {
				stock.setClasses(stockInfo[0]);
			}

			if (!TextUtils.isEmpty(stockInfo[1])) {
				stock.setPinyin(stockInfo[1]);
			}

			if (!TextUtils.isEmpty(stockInfo[7])) {
				stock.setTotalShare(Double.valueOf(stockInfo[7])
						* Constant.DOUBLE_CONSTANT_WAN);
			}

			mStockDatabaseManager.updateStock(stock,
					stock.getContentValuesInformation());

			Setting.setDownloadStockInformationTimemillis(stock.getSE(), stock.getCode(), System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(stock.getName() + " "
				+ stock.getClasses() + " " + stock.getPinyin() + " "
				+ stock.getTotalShare() + " " + stopWatch.getInterval()
				+ "s");
	}

	public int downloadStockRealTime(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		if (!Market.isTradingHours(Calendar.getInstance())) {
			if (System.currentTimeMillis() - Setting.getDownloadStockRealTimeTimemillis(stock.getSE(), stock.getCode()) < Config.downloadStockRealTimeInterval) {
				return result;
			}
		}

		return downloadStockRealTime(stock, getRequestHeader(), getStockRealTimeURLString(stock));
	}

	private int downloadStockRealTime(Stock stock, ArrayMap<String, String> requestHeaderArray, String urlString) {
		int result = RESULT_NONE;

		Log.d(urlString);

		Request.Builder builder = new Request.Builder();
		for (int i = 0; i < requestHeaderArray.size(); i++) {
			builder.addHeader(requestHeaderArray.keyAt(i), requestHeaderArray.valueAt(i));
		}
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return RESULT_FAILED;
				} else {
					result = RESULT_SUCCESS;
				}

				handleResponseStockRealTime(stock, resultString);
				Thread.sleep(Config.downloadSleepInterval);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseStockRealTime(Stock stock, String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		String[] keyValue = null;
		String[] codeInfo = null;
		String[] stockInfo = null;

		//var hq_str_s_sh600048="保利发展,17.690,0.140,0.80,645118,115512";

		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d("return, stock = "
					+ stock + " response = " + response);
			return;
		}

		try {
			keyValue = response.trim().split("=");

			if (keyValue == null || keyValue.length != 2) {
				Log.d("return, (keyValue == null) || (keyValue.length != 2)");
				return;
			}

			if (keyValue[0] == null) {
				Log.d("return, keyValue[0] == null");
				return;
			}

			codeInfo = keyValue[0].trim().split("_");

			if (codeInfo == null || codeInfo.length != 4) {
				Log.d("return, (codeInfo == null) || (codeInfo.length != 4) ");
				return;
			}

			if (!stock.getSE().equals(codeInfo[3].substring(0, 2))
					|| !stock.getCode().equals(codeInfo[3].substring(2, 8))) {
				Log.d("return, ...");
				return;
			}

			if (keyValue[1] == null) {
				Log.d("return, keyValue[1] == null");
				return;
			}

			stockInfo = keyValue[1].substring(1, keyValue[1].length() - 2)
					.split(",");

			if (stockInfo == null || stockInfo.length != 6) {
				Log.d("return, stockInfo = "
						+ stockInfo);
				return;
			}

			if (!stock.getName().equals(stockInfo[0])) {
				stock.setName(stockInfo[0]);
			}

			if (!TextUtils.isEmpty(stockInfo[1])) {
				stock.setPrice(Double.valueOf(stockInfo[1]));
			}

			if (!TextUtils.isEmpty(stockInfo[2])) {
				stock.setChange(Double.valueOf(stockInfo[2]));
			}

			if (!TextUtils.isEmpty(stockInfo[3])) {
				stock.setNet(Double.valueOf(stockInfo[3]));
			}

			if (!TextUtils.isEmpty(stockInfo[4])) {
				stock.setVolume(Long.valueOf(stockInfo[4]));
			}

			if (!TextUtils.isEmpty(stockInfo[5])) {
				stock.setValue(Long.valueOf(stockInfo[5]));
			}

			mStockDatabaseManager.updateStock(stock,
					stock.getContentValuesRealTime());

			Setting.setDownloadStockRealTimeTimemillis(stock.getSE(), stock.getCode(), System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(stock.getName() + " "
				+ stock.getPrice() + " " + stock.getChange() + " "
				+ stock.getNet() + " " + stock.getVolume() + " "
				+ stock.getValue() + " " + stopWatch.getInterval()
				+ "s");
	}

	public void handleResponseStockHSA(String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		boolean nameChanged = false;
		boolean bulkInsert = false;
		ContentValues[] contentValuesArray = null;
		String symbol = "";
		String se = "";
		Stock stock = new Stock();
		JSONArray jsonArray = null;

		if (TextUtils.isEmpty(response)) {
			Log.d("return, response = " + response);
			return;
		}

		if (mStockDatabaseManager.getStockCount(DatabaseContract.COLUMN_CLASSES
				+ " = '" + Stock.CLASS_A + "'", null, null) == 0) {
			bulkInsert = true;
			Log.d("bulkInsert = " + bulkInsert);
		}

		try {
			jsonArray = JSON.parseArray(response);

			if (jsonArray == null) {
				Log.d("return, jsonArray = "
						+ jsonArray);
				return;
			}

			if (jsonArray.size() == 0) {
				Log.d("return, jsonArray.size() = "
						+ jsonArray.size());
				return;
			}

			if (bulkInsert) {
				if (contentValuesArray == null) {
					contentValuesArray = new ContentValues[jsonArray.size()];
				}
			}

			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);

				if (jsonObject != null) {
					symbol = jsonObject.getString("symbol");
					if (!TextUtils.isEmpty(symbol)) {
						se = symbol.substring(0, 2);
						stock.setSE(se);
					}
					stock.setCode(jsonObject.getString("code"));

					mStockDatabaseManager.getStock(stock);

					stock.setClasses(Stock.CLASS_A);

					nameChanged = false;
					if (!stock.getName().equals(jsonObject.getString("name"))) {
						nameChanged = true;
						stock.setName(jsonObject.getString("name"));
					}

					stock.setPrice(jsonObject.getDouble("trade"));
					stock.setChange(jsonObject.getDouble("pricechange"));
					stock.setNet(jsonObject.getDouble("changepercent"));
					stock.setVolume(jsonObject.getLong("volume"));
					stock.setValue(jsonObject.getLong("amount"));

					if (bulkInsert) {
						stock.setCreated(Utility.getCurrentDateTimeString());
						stock.setModified(Utility.getCurrentDateTimeString());
						contentValuesArray[i] = stock.getContentValues();
					} else {
						if (!mStockDatabaseManager.isStockExist(stock)) {
							stock.setCreated(Utility.getCurrentDateTimeString());
							stock.setModified(Utility
									.getCurrentDateTimeString());
							mStockDatabaseManager.insertStock(stock);
						} else {
							stock.setModified(Utility
									.getCurrentDateTimeString());
							if (nameChanged) {
								mStockDatabaseManager.updateStock(stock,
										stock.getContentValues());
							}
						}
					}
				}
			}

			if (bulkInsert) {
				mStockDatabaseManager.bulkInsertStock(contentValuesArray);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d("size:" + jsonArray.size()
				+ " " + stopWatch.getInterval() + "s");
	}

	public int downloadStockDataHistory(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		for (String period : DatabaseContract.PERIODS) {
			if (Preferences.getBoolean(period, false)) {
				result = downloadStockDataHistory(stock, period);
			}
		}

		return result;
	}

	private int downloadStockDataHistory(Stock stock, String period) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		StockData stockData = new StockData(period);
		stockData.setStockId(stock.getId());
		mStockDatabaseManager.getStockData(stockData);
		stockData.setSE(stock.getSE());
		stockData.setCode(stock.getCode());
		stockData.setName(stock.getName());

		int len = getDownloadStockDataLength(stockData);
		if (len <= 0) {
			return result;
		}

		return downloadStockDataHistory(stock, stockData, getStockDataHistoryURLString(stock,
				stockData, len));
	}

	private int downloadStockDataHistory(Stock stock, StockData stockData, String urlString) {
		int result = RESULT_NONE;

		Log.d(urlString);

		Request.Builder builder = new Request.Builder();
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return RESULT_FAILED;
				} else {
					result = RESULT_SUCCESS;
				}

				handleResponseStockDataHistory(stock, stockData, resultString);
				Thread.sleep(Config.downloadSleepInterval);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseStockDataHistory(@NonNull Stock stock, @NonNull StockData stockData,
											   String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		boolean bulkInsert = false;
		int defaultValue = 0;
		String dateTimeString = "";
		String[] dateTime = null;
		JSONArray jsonArray = null;

		ArrayList<ContentValues> contentValuesList = new ArrayList<ContentValues>();
		ArrayMap<String, StockData> stockDataMap = new ArrayMap<>();

		Calendar importCalendar = Utility.getCalendar("1998-01-01 00:00:00", Utility.CALENDAR_DATE_TIME_FORMAT);

		if (TextUtils.isEmpty(response)) {
			Log.d("return, stock = "
					+ stock + " stockData = " + stockData + " response = "
					+ response);
			return;
		}

		try {
			jsonArray = JSON.parseArray(response);

			if (jsonArray == null) {
				Log.d("return, jsonArray = "
						+ jsonArray);
				return;
			}

			if (jsonArray.size() == 0) {
				Log.d("return, jsonArray.size() = "
						+ jsonArray.size());
				return;
			}

			defaultValue = getDownloadHistoryLengthDefault(stockData
					.getPeriod());
			if (TextUtils.isEmpty(stockData.getCreated())
					|| (defaultValue == jsonArray.size())) {
				mStockDatabaseManager.deleteStockData(stockData.getStockId(),
						stockData.getPeriod());
				bulkInsert = true;
			}

			if (bulkInsert) {
				setupStockDataFile(stock);

				if (stockData.isMinutePeriod()) {
					importStockDataFile(stock, stockData, contentValuesList, stockDataMap);
				}
			}

			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);

				if (jsonObject != null) {
					dateTimeString = jsonObject.getString(DatabaseContract.COLUMN_DAY);
					if (!TextUtils.isEmpty(dateTimeString)) {
						dateTime = dateTimeString.trim().split(" ");
						switch (dateTime.length) {
							case 1:
								stockData.setDate(dateTime[0]);
								stockData.setTime("");
								break;
							case 2:
								stockData.setDate(dateTime[0]);
								if (stockData.isMinutePeriod()) {
									stockData.setTime(dateTime[1]);
								}
								break;
							default:
								break;
						}
					}

					stockData.setOpen(jsonObject.getDouble("open"));
					stockData.setClose(jsonObject.getDouble("close"));
					stockData.setHigh(jsonObject.getDouble("high"));
					stockData.setLow(jsonObject.getDouble("low"));

					stockData.setVertexHigh(stockData.getHigh());
					stockData.setVertexLow(stockData.getLow());

					if (bulkInsert) {
						stockData.setCreated(Utility.getCurrentDateTimeString());
						stockData.setModified(Utility.getCurrentDateTimeString());

						if (stockData.isMinutePeriod()) {
							if (!stockDataMap.containsKey(stockData.getDateTime())) {
								stockDataMap.put(stockData.getDateTime(), new StockData(stockData));
								contentValuesList.add(stockData.getContentValues());
							}
						} else {
							contentValuesList.add(stockData.getContentValues());
						}
					} else {
						if (!mStockDatabaseManager.isStockDataExist(stockData)) {
							stockData.setCreated(Utility
									.getCurrentDateTimeString());
							stockData.setModified(Utility
									.getCurrentDateTimeString());
							mStockDatabaseManager.insertStockData(stockData);
						} else {
							stockData.setModified(Utility
									.getCurrentDateTimeString());
							mStockDatabaseManager.updateStockData(stockData,
									stockData.getContentValues());
						}
					}
				}
			}

			if (bulkInsert) {
				if (stockData.isMinutePeriod()) {
					if (stockDataMap.size() > 0) {
						ArrayList<StockData> stockDataList = new ArrayList<>(stockDataMap.values());
						Collections.sort(stockDataList, StockData.comparator);
						exportStockDataFile(stock, stockData.getPeriod(), stockDataList);
					}
				}

				if (contentValuesList.size() > 0) {
					fixContentValuesList(stockData, contentValuesList);

					ContentValues[] contentValuesArray = new ContentValues[contentValuesList
							.size()];
					contentValuesArray = contentValuesList
							.toArray(contentValuesArray);
					mStockDatabaseManager.bulkInsertStockData(contentValuesArray);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(stock.getName() + " "
				+ stockData.getPeriod() + " " + stopWatch.getInterval() + "s");
	}

	String getStockDataFileName(@NonNull Stock stock) {
		String fileName = "";

		try {
			fileName = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Orion/"
					+ stock.getSE().toUpperCase(Locale.getDefault()) + "#" + stock.getCode() + Constant.FILE_EXT_TEXT;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileName;
	}

	String getStockDataFileName(@NonNull Stock stock, String period) {
		String fileName = "";

		try {
			fileName = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Orion/"
					+ stock.getSE().toUpperCase(Locale.getDefault()) + "#" + stock.getCode() + "#" + period + Constant.FILE_EXT_TEXT;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileName;
	}

	//					SH#600938.txt
	//					日期	    时间	    开盘	    最高	    最低	    收盘	    成交量	    成交额
	//					2023/01/03	0935	37.08	37.08	36.72	36.81	6066500	223727792.00

	StockData mergeStockData(@NonNull ArrayList<StockData> stockDataList, int size) {
		double high = 0;
		double low = 0;

		if (stockDataList.size() == 0 || size <= 0) {
			return null;
		}

		int j = 0;
		StockData result = new StockData();
		for (int i = stockDataList.size() - 1; i >= 0; i--, j++) {
			if (j >= size) {
				break;
			}

			StockData stockData = stockDataList.get(i);

			if (i == stockDataList.size() - 1) {
				high = stockData.getHigh();
				low = stockData.getLow();

				result.setDate(stockData.getDate());
				result.setTime(stockData.getTime());
			}

			result.setOpen(stockData.getOpen());

			if (stockData.getHigh() > high) {
				high = stockData.getHigh();
			}
			result.setHigh(high);

			if (stockData.getLow() < low) {
				low = stockData.getLow();
			}
			result.setLow(low);

			if (i == stockDataList.size() - 1) {
				result.setClose(stockData.getClose());
			}
		}

		return result;
	}

	void setupStockDataFile(@NonNull Stock stock) {
		String fileName;
		ArrayList<String> lineList = new ArrayList<>();

//		ArrayList<String> datetimeMin5List = new ArrayList<>();//based on min5
		ArrayList<String> datetimeMin15List = new ArrayList<>();
		ArrayList<String> datetimeMin30List = new ArrayList<>();
		ArrayList<String> datetimeMin60List = new ArrayList<>();

		ArrayList<StockData> StockDataMin5List = new ArrayList<>();
		ArrayList<StockData> StockDataMin15List = new ArrayList<>();
		ArrayList<StockData> StockDataMin30List = new ArrayList<>();
		ArrayList<StockData> StockDataMin60List = new ArrayList<>();

		try {
			fileName = getStockDataFileName(stock);//same as min5
			Utility.readFile(fileName, lineList);

			if (lineList.size() == 0) {
				return;
			}

			datetimeMin15List = StockData.getDatetimeMin15List();
			datetimeMin30List = StockData.getDatetimeMinL30ist();
			datetimeMin60List = StockData.getDatetimeMin60List();

			for (int i = 0; i < lineList.size(); i++) {
				String line = lineList.get(i);
				if (TextUtils.isEmpty(line)) {
					continue;
				}

				StockData stockDataMin5 = new StockData();
				if (stockDataMin5.fromString(line) == null) {
					continue;
				}

				StockDataMin5List.add(stockDataMin5);

				if (datetimeMin15List.contains(stockDataMin5.getTime())) {
					StockData stockData15 = mergeStockData(StockDataMin5List, 15 / 5);
					if (stockData15 != null) {
						StockDataMin15List.add(stockData15);
					}
				}

				if (datetimeMin30List.contains(stockDataMin5.getTime())) {
					StockData stockData30 = mergeStockData(StockDataMin15List, 30 / 15);
					if (stockData30 != null) {
						StockDataMin30List.add(stockData30);
					}
				}

				if (datetimeMin60List.contains(stockDataMin5.getTime())) {
					StockData stockData60 = mergeStockData(StockDataMin30List, 60 / 30);
					if (stockData60 != null) {
						StockDataMin60List.add(stockData60);
					}
				}
			}

			exportStockDataFile(stock, DatabaseContract.COLUMN_MIN5, StockDataMin5List);
			exportStockDataFile(stock, DatabaseContract.COLUMN_MIN15, StockDataMin15List);
			exportStockDataFile(stock, DatabaseContract.COLUMN_MIN30, StockDataMin30List);
			exportStockDataFile(stock, DatabaseContract.COLUMN_MIN60, StockDataMin60List);
			Utility.deleteFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void importStockDataFile(@NonNull Stock stock, @NonNull StockData stockData,
							 @NonNull ArrayList<ContentValues> contentValuesList,
							 @NonNull ArrayMap<String, StockData> stockDataMap) {
		String fileName = "";
		ArrayList<String> lineList = new ArrayList<>();

		try {
			fileName = getStockDataFileName(stock, stockData.getPeriod());
			Utility.readFile(fileName, lineList);

			for (int i = 0; i < lineList.size(); i++) {
				String line = lineList.get(i);
				if (TextUtils.isEmpty(line)) {
					continue;
				}

				if (stockData.fromString(line) != null) {
					contentValuesList.add(stockData.getContentValues());
					stockDataMap.put(stockData.getDateTime(), new StockData(stockData));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void exportStockDataFile(@NonNull Stock stock, String period, @NonNull ArrayList<StockData> stockDataList) {
		String fileName = "";
		ArrayList<String> lineList = new ArrayList<>();

		try {
			fileName = getStockDataFileName(stock, period);
			for (int i = 0; i < stockDataList.size(); i++) {
				lineList.add(stockDataList.get(i).toString());
			}
			Utility.writeFile(fileName, lineList, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int downloadStockDataRealTime(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		for (String period : DatabaseContract.PERIODS) {
			if (Preferences.getBoolean(period, false)) {
				if (DatabaseContract.COLUMN_DAY.equals(period)) {
					result = downloadStockDataRealTime(stock, period);
				}
			}
		}

		return result;
	}

	private int downloadStockDataRealTime(Stock stock, String period) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		StockData stockData = new StockData(period);
		stockData.setStockId(stock.getId());
		mStockDatabaseManager.getStockData(stockData);
		stockData.setSE(stock.getSE());
		stockData.setCode(stock.getCode());
		stockData.setName(stock.getName());

		int len = getDownloadStockDataLength(stockData);
		if (len <= 0) {
			return result;
		}

		return downloadStockDataRealTime(stock, stockData, getRequestHeader(), getStockDataRealTimeURLString(stock));
	}

	private int downloadStockDataRealTime(Stock stock, StockData stockData, ArrayMap<String, String> requestHeaderArray, String urlString) {
		int result = RESULT_NONE;

		Log.d(urlString);

		Request.Builder builder = new Request.Builder();
		for (int i = 0; i < requestHeaderArray.size(); i++) {
			builder.addHeader(requestHeaderArray.keyAt(i), requestHeaderArray.valueAt(i));
		}
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return RESULT_FAILED;
				} else {
					result = RESULT_SUCCESS;
				}

				handleResponseStockDataRealTime(stock, stockData, resultString);
				Thread.sleep(Config.downloadSleepInterval);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseStockDataRealTime(Stock stock, StockData stockData,
												String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		String[] keyValue = null;
		String[] codeInfo = null;
		String[] stockInfo = null;

		if ((stock == null) || (stockData == null)
				|| TextUtils.isEmpty(response)) {
			Log.d("return, stock = "
					+ stock + " stockData = " + stockData + " response = "
					+ response);
			return;
		}

		try {
			keyValue = response.trim().split("=");

			if (keyValue == null) {
				Log.d("return, keyValue = " + keyValue);
				return;
			}

			if (keyValue.length < 2) {
				Log.d("return, keyValue.length = " + keyValue.length);
				return;
			}

			if (keyValue[0] == null) {
				Log.d("return, keyValue[0] = " + keyValue[0]);
				return;
			}

			codeInfo = keyValue[0].trim().split("_");

			if (codeInfo == null) {
				Log.d("return, codeInfo = " + codeInfo);
				return;
			}

			if (codeInfo.length < 3) {
				Log.d("return, codeInfo.length = " + codeInfo.length);
				return;
			}

			if (codeInfo[2] == null) {
				Log.d("return, codeInfo[2] = " + codeInfo[2]);
				return;
			}

			if (keyValue[1] == null) {
				Log.d("return, keyValue[1] = " + keyValue[1]);
				return;
			}

			stockInfo = keyValue[1].trim().split(",");

			if (stockInfo == null) {
				Log.d("return, stockInfo = " + stockInfo);
				return;
			}

			if (stockInfo.length < 32) {
				Log.d("return, stockInfo.length = " + stockInfo.length);
				return;
			}

			for (int i = 1; i < 6; i++) {
				if (Float.valueOf(stockInfo[i]) <= 0) {
					Log.d("return, Float.valueOf(stockInfo[" + i + "]) = "
							+ Float.valueOf(stockInfo[i]));
					return;
				}
			}

			stockData.setOpen(Double.valueOf(stockInfo[1]));
			stockData.setClose(Double.valueOf(stockInfo[3]));
			stockData.setHigh(Double.valueOf(stockInfo[4]));
			stockData.setLow(Double.valueOf(stockInfo[5]));

			stockData.setVertexHigh(stockData.getHigh());
			stockData.setVertexLow(stockData.getLow());

			stockData.setDate(stockInfo[30]);
			if (stockData.isMinutePeriod()) {
				stockData.setTime(stockInfo[31]);
			}

			if (!TextUtils.isEmpty(stockInfo[32])) {
				if (stockInfo[32].startsWith("00")) {
					stock.setStatus("");
				} else {
					stock.setStatus(Stock.STATUS_SUSPENSION);
				}
			}

			if (!mStockDatabaseManager.isStockDataExist(stockData)) {
				stockData.setCreated(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.insertStockData(stockData);
			} else {
				stockData.setModified(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.updateStockData(stockData,
						stockData.getContentValues());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(stock.getName() + " "
				+ stockData.getDate() + " " + stockData.getTime() + " "
				+ stockData.getOpen() + " " + stockData.getClose() + " "
				+ stockData.getHigh() + " " + stockData.getLow() + " "
				+ stopWatch.getInterval() + "s");
	}

	public int downloadStockFinancial(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		StockFinancial stockFinancial = new StockFinancial();
		stockFinancial.setStockId(stock.getId());

		mStockDatabaseManager.getStockFinancial(stock, stockFinancial);

		if (System.currentTimeMillis() - Setting.getDownloadStockFinancialTimemillis(stock.getSE(), stock.getCode()) < Config.downloadStockFinancialInterval) {
			return result;
		}

		return downloadStockFinancial(stock, stockFinancial, getStockFinancialURLString(stock));
	}

	private int downloadStockFinancial(Stock stock, StockFinancial stockFinancial, String urlString) {
		int result = RESULT_NONE;

		Log.d(urlString);

		Request.Builder builder = new Request.Builder();
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return RESULT_FAILED;
				} else {
					result = RESULT_SUCCESS;
				}

				handleResponseStockFinancial(stock, stockFinancial, resultString);
				Thread.sleep(Config.downloadSleepInterval);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseStockFinancial(Stock stock, StockFinancial stockFinancial,
											 String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		boolean bulkInsert = false;
		String keyString = "";
		String valueString = "";
		ArrayList<ContentValues> contentValuesList = new ArrayList<ContentValues>();

		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d("return, stock = "
					+ stock + " response = " + response);
			return;
		}

		if (TextUtils.isEmpty(stockFinancial.getCreated())) {
			mStockDatabaseManager.deleteStockFinancial(stockFinancial
					.getStockId());
			bulkInsert = true;
		}

		try {
			JSONObject responseJSONObject = JSON.parseObject(response);
			if (responseJSONObject == null) {
				Log.d("return, responseJSONObject = "
						+ responseJSONObject);
				return;
			}

			JSONObject resultJSONObject = responseJSONObject.getJSONObject("result");
			if (resultJSONObject == null) {
				Log.d("return, resultJSONObject = "
						+ resultJSONObject);
				return;
			}

			JSONObject dataJSONObject = resultJSONObject.getJSONObject("data");
			if (dataJSONObject == null) {
				Log.d("return, dataJSONObject = "
						+ dataJSONObject);
				return;
			}

			JSONObject reportListJSONObject = dataJSONObject.getJSONObject("report_list");
			if (reportListJSONObject == null) {
				Log.d("return, reportListJSONObject = "
						+ reportListJSONObject);
				return;
			}

			JSONArray reportDateJSONArray = dataJSONObject.getJSONArray("report_date");
			if (reportDateJSONArray == null) {
				Log.d("return, reportDateJSONArray = "
						+ reportDateJSONArray);
				return;
			}

			for (int i = 0; i < reportDateJSONArray.size(); i++) {
				JSONObject reportDateJSONObject = reportDateJSONArray.getJSONObject(i);
				if (reportDateJSONObject == null) {
					continue;
				}

				String dateValue = reportDateJSONObject.getString("date_value");
				if (TextUtils.isEmpty(dateValue) || dateValue.length() < 8) {
					continue;
				}

				valueString = dateValue.substring(0, 4) + "-" + dateValue.substring(4, 6) + "-" + dateValue.substring(6);
				stockFinancial.setDate(valueString);

				JSONObject reportJSONObject = reportListJSONObject.getJSONObject(dateValue);
				if (reportJSONObject == null) {
					continue;
				}

				JSONArray dataJSONArray = reportJSONObject.getJSONArray("data");
				if (dataJSONArray == null) {
					continue;
				}

				for (int j = 0; j < dataJSONArray.size(); j++) {
					JSONObject jsonObject = dataJSONArray.getJSONObject(j);
					if (jsonObject == null) {
						continue;
					}

					keyString = jsonObject.getString("item_field");
					if (TextUtils.isEmpty(keyString)) {
						continue;
					}

					valueString = jsonObject.getString("item_value");
					if (TextUtils.isEmpty(valueString)) {
						continue;
					}

					if (keyString.equals("BIZINCO") || keyString.equals("BIZTOTINCO")) {//营业总收入
						stockFinancial.setMainBusinessIncome(Double
								.valueOf(valueString));
					} else if (keyString.equals("BIZEXPE") || keyString.equals("BIZTOTCOST")) {//营业成本
						stockFinancial.setFinancialExpenses(Double
								.valueOf(valueString));
					} else if (keyString.equals("NETPROFIT")) {//净利润
						stockFinancial.setNetProfit(Double
								.valueOf(valueString));
					} else if (keyString.equals("NAPS")) {//每股净资产
						stockFinancial.setBookValuePerShare(Double
								.valueOf(valueString));
					} else if (keyString.equals("OPNCFPS")) {//每股现金流
						stockFinancial.setCashFlowPerShare(Double
								.valueOf(valueString));
					} else if (keyString.equals(mContext.getResources().getString(R.string.key_total_current_assets))) {
						stockFinancial.setTotalCurrentAssets(Double
								.valueOf(valueString));
					} else if (keyString.equals(mContext.getResources().getString(R.string.key_total_assets))) {
						stockFinancial.setTotalAssets(Double
								.valueOf(valueString));
					} else if (keyString.equals("ASSLIABRT")) {//资产负债率
						stockFinancial
								.setDebtToNetAssetsRatio(Double
										.valueOf(valueString));

						stockFinancial.setupNetProfitPerShare(stock
								.getTotalShare());

						if (bulkInsert) {
							stockFinancial.setCreated(Utility
									.getCurrentDateTimeString());
							stockFinancial.setModified(Utility
									.getCurrentDateTimeString());
							contentValuesList.add(stockFinancial
									.getContentValues());
						} else {
							if (!mStockDatabaseManager
									.isStockFinancialExist(stockFinancial)) {
								stockFinancial.setCreated(Utility
										.getCurrentDateTimeString());
								stockFinancial.setModified(Utility
										.getCurrentDateTimeString());
								mStockDatabaseManager
										.insertStockFinancial(stockFinancial);
							} else {
								stockFinancial.setModified(Utility
										.getCurrentDateTimeString());
								mStockDatabaseManager
										.updateStockFinancial(
												stockFinancial,
												stockFinancial
														.getContentValues());
							}
						}

						break;
					}
				}
			}

			if (bulkInsert) {
				if (contentValuesList.size() > 0) {
					ContentValues[] contentValuesArray = new ContentValues[contentValuesList
							.size()];
					contentValuesArray = contentValuesList
							.toArray(contentValuesArray);
					mStockDatabaseManager
							.bulkInsertStockFinancial(contentValuesArray);
				}
			}

			Setting.setDownloadStockFinancialTimemillis(stock.getSE(), stock.getCode(), System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(stock.getName() + " "
				+ stopWatch.getInterval() + "s");
	}

	public int downloadShareBonus(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		ShareBonus shareBonus = new ShareBonus();
		shareBonus.setStockId(stock.getId());

		mStockDatabaseManager.getShareBonus(stock.getId(), shareBonus);

		if (System.currentTimeMillis() - Setting.getDownloadShareBonusTimemillis(stock.getSE(), stock.getCode()) < Config.downloadShareBonusInterval) {
			return result;
		}

		return downloadShareBonus(stock, shareBonus, getShareBonusURLString(stock));
	}

	private int downloadShareBonus(Stock stock, ShareBonus shareBonus, String urlString) {
		int result = RESULT_NONE;

		Log.d(urlString);

		Request.Builder builder = new Request.Builder();
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return RESULT_FAILED;
				} else {
					result = RESULT_SUCCESS;
				}

				handleResponseShareBonus(stock, shareBonus, resultString);
				Thread.sleep(Config.downloadSleepInterval);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseShareBonus(Stock stock, ShareBonus shareBonus,
										 String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		boolean bulkInsert = false;
		String dateString = "";
		String dividendString = "";
		String rDateString = "";
		ArrayList<ContentValues> contentValuesList = new ArrayList<ContentValues>();

		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d("return, stock = " + stock
					+ " response = " + response);
			return;
		}

		mStockDatabaseManager.deleteShareBonus(shareBonus.getStockId());
		bulkInsert = true;

		try {
			// String responseString = new
			// String(response.getBytes("ISO-8859-1"),
			// "GB2312");

			Document doc = Jsoup.parse(response);
			if (doc == null) {
				Log.d("return, doc = " + doc);
				return;
			}

			Elements tableElements = doc.select("table#sharebonus_1");
			if (tableElements == null) {
				Log.d("return, tableElements = " + tableElements);
				return;
			}

			Elements tbodyElements = tableElements.select("tbody");
			if (tbodyElements == null) {
				Log.d("return, tbodyElements = " + tbodyElements);
				return;
			}

			for (Element tbodyElement : tbodyElements) {
				if (tbodyElement == null) {
					Log.d("return, tbodyElement = " + tbodyElement);
					return;
				}

				Elements trElements = tbodyElement.select("tr");
				if (trElements == null) {
					Log.d("return, trElements = " + trElements);
					return;
				}

				for (Element trElement : trElements) {
					if (trElement == null) {
						Log.d("continue, trElement = " + trElement);
						continue;
					}

					Elements tdElements = trElement.select("td");
					if (tdElements == null) {
						Log.d("continue, tdElements = " + tdElements);
						continue;
					}

					if (tdElements.size() < 9) {
						Log.d("continue, tdElements.size() = " + tdElements.size());
						continue;
					}

					dateString = tdElements.get(0).text();
					if (TextUtils.isEmpty(dateString)
							|| dateString.contains("--")) {
						continue;
					}

					dividendString = tdElements.get(3).text();
					if (TextUtils.isEmpty(dividendString)
							|| dividendString.contains("--")) {
						continue;
					}

					rDateString = tdElements.get(6).text();
					if (TextUtils.isEmpty(rDateString)) {
						continue;
					}

					shareBonus.setDate(dateString);
					shareBonus.setDividend(Double.valueOf(dividendString));
					shareBonus.setRDate(rDateString);

					if (bulkInsert) {
						shareBonus.setCreated(Utility
								.getCurrentDateTimeString());
						shareBonus.setModified(Utility
								.getCurrentDateTimeString());
						contentValuesList.add(shareBonus.getContentValues());
					} else {
						if (!mStockDatabaseManager
								.isShareBonusExist(shareBonus)) {
							shareBonus.setCreated(Utility
									.getCurrentDateTimeString());
							shareBonus.setModified(Utility
									.getCurrentDateTimeString());
							mStockDatabaseManager.insertShareBonus(shareBonus);
						} else {
							shareBonus.setModified(Utility
									.getCurrentDateTimeString());
							mStockDatabaseManager.updateShareBonus(shareBonus,
									shareBonus.getContentValues());
						}
					}
				}
			}

			if (bulkInsert) {
				if (contentValuesList.size() > 0) {
					ContentValues[] contentValuesArray = new ContentValues[contentValuesList
							.size()];
					contentValuesArray = contentValuesList
							.toArray(contentValuesArray);
					mStockDatabaseManager
							.bulkInsertShareBonus(contentValuesArray);
				}
			}

			Setting.setDownloadShareBonusTimemillis(stock.getSE(), stock.getCode(), System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(stock.getName() + " "
				+ stopWatch.getInterval() + "s");
	}


	public int downloadTotalShare(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		TotalShare totalShare = new TotalShare();
		totalShare.setStockId(stock.getId());

		mStockDatabaseManager.getTotalShare(stock.getId(), totalShare);

		if (System.currentTimeMillis() - Setting.getDownloadTotalShareTimemillis(stock.getSE(), stock.getCode()) < Config.downloadTotalShareInterval) {
			return result;
		}

		return downloadTotalShare(stock, totalShare, getTotalShareURLString(stock));
	}

	private int downloadTotalShare(Stock stock, TotalShare totalShare, String urlString) {
		int result = RESULT_NONE;

		Log.d(urlString);

		Request.Builder builder = new Request.Builder();
		builder.url(urlString);
		Request request = builder.build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if ((response != null) && (response.body() != null)) {
				String resultString = response.body().string();
				if (isAccessDenied(resultString)) {
					return RESULT_FAILED;
				} else {
					result = RESULT_SUCCESS;
				}

				handleResponseTotalShare(stock, totalShare, resultString);
				Thread.sleep(Config.downloadSleepInterval);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseTotalShare(Stock stock, TotalShare totalShare,
										 String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		boolean bulkInsert = false;
		String dateString = "";
		String totalShareString = "";
		ArrayList<ContentValues> contentValuesList = new ArrayList<ContentValues>();

		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d("return, stock = " + stock
					+ " response = " + response);
			return;
		}

		mStockDatabaseManager.deleteTotalShare(totalShare.getStockId());
		bulkInsert = true;

		try {
			// String responseString = new
			// String(response.getBytes("ISO-8859-1"),
			// "GB2312");

			Document doc = Jsoup.parse(response);
			if (doc == null) {
				Log.d("return, doc = " + doc);
				return;
			}

			Elements tableElements = doc.select("table[id^=historyTable]");
			if (tableElements == null) {
				Log.d("return, tableElements = " + tableElements);
				return;
			}

			Elements tbodyElements = tableElements.select("tbody");
			if (tbodyElements == null) {
				Log.d("return, tbodyElements = " + tbodyElements);
				return;
			}

			for (Element tbodyElement : tbodyElements) {
				if (tbodyElement == null) {
					Log.d("return, tbodyElement = " + tbodyElement);
					return;
				}

				Elements trElements = tbodyElement.select("tr");
				if (trElements == null) {
					Log.d("return, trElements = " + trElements);
					return;
				}

				for (Element trElement : trElements) {
					if (trElement == null) {
						Log.d("continue, trElement = " + trElement);
						continue;
					}

					Elements tdElements = trElement.select("td");
					if (tdElements == null) {
						Log.d("continue, tdElements = " + tdElements);
						continue;
					}

					if (tdElements.size() < 2) {
						Log.d("continue, tdElements.size() = " + tdElements.size());
						continue;
					}

					dateString = tdElements.get(0).text();
					if (TextUtils.isEmpty(dateString)
							|| dateString.contains("--")) {
						continue;
					}

					totalShareString = tdElements.get(1).text();
					if (TextUtils.isEmpty(totalShareString)
							|| totalShareString.contains("--")) {
						continue;
					}

					if (totalShareString.contains(mContext.getResources().getString(R.string.ten_thousand_shares))) {
						totalShareString = totalShareString.replace(mContext.getResources().getString(R.string.ten_thousand_shares), "");
						totalShare.setDate(dateString);
						totalShare.setTotalShare(Double
								.valueOf(totalShareString)
								* Constant.DOUBLE_CONSTANT_WAN);
					} else {
						continue;
					}

					if (bulkInsert) {
						totalShare.setCreated(Utility
								.getCurrentDateTimeString());
						totalShare.setModified(Utility
								.getCurrentDateTimeString());
						contentValuesList.add(totalShare.getContentValues());
					} else {
						if (!mStockDatabaseManager
								.isTotalShareExist(totalShare)) {
							totalShare.setCreated(Utility
									.getCurrentDateTimeString());
							totalShare.setModified(Utility
									.getCurrentDateTimeString());
							mStockDatabaseManager.insertTotalShare(totalShare);
						} else {
							totalShare.setModified(Utility
									.getCurrentDateTimeString());
							mStockDatabaseManager.updateTotalShare(totalShare,
									totalShare.getContentValues());
						}
					}
				}
			}

			if (bulkInsert) {
				if (contentValuesList.size() > 0) {
					ContentValues[] contentValuesArray = new ContentValues[contentValuesList
							.size()];
					contentValuesArray = contentValuesList
							.toArray(contentValuesArray);
					mStockDatabaseManager
							.bulkInsertTotalShare(contentValuesArray);
				}
			}

			Setting.setDownloadTotalShareTimemillis(stock.getSE(), stock.getCode(), System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(stock.getName() + " "
				+ stopWatch.getInterval() + "s");
	}

	private boolean isAccessDenied(String string) {
		StringBuilder contentTitle = new StringBuilder();
		boolean result = false;

		if (TextUtils.isEmpty(string)) {
			return result;
		}

		String accessDeniedString;
		for (int i = 0; i < mAccessDeniedStringArray.size(); i++) {
			accessDeniedString = mAccessDeniedStringArray.get(i);

			if (string.contains(accessDeniedString)) {
				contentTitle.append(mContext.getResources().getString(R.string.action_download));
				contentTitle.append(" ");
				contentTitle.append(accessDeniedString);

				mStockAnalyzer.notify(Config.SERVICE_NOTIFICATION_ID, Config.MESSAGE_CHANNEL_ID, Config.MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH,
						contentTitle.toString(), "");
				onDestroy();

				result = true;
				break;
			}
		}

		return result;
	}

	public class StockInfo {
		public String code;

		public String name;// 0
		public String open_price;// 1
		public String yesterday_close_price;// 2
		public String current_price;// 3
		public String high_price;// 4
		public String low_price;// 5
		public String bid_price;// 6
		public String ask_price;// 7
		public String volume;// 8
		public String value;// 9
		public String bid_volume_1;// 10
		public String bid_price_1;// 11
		public String bid_volume_2;// 12
		public String bid_price_2;// 13
		public String bid_volume_3;// 14
		public String bid_price_3;// 15
		public String bid_volume_4;// 16
		public String bid_price_4;// 17
		public String bid_volume_5;// 18
		public String bid_price_5;// 19
		public String ask_volume_1;// 20
		public String ask_price_1;// 21
		public String ask_volume_2;// 22
		public String ask_price_2;// 23
		public String ask_volume_3;// 24
		public String ask_price_3;// 25
		public String ask_volume_4;// 26
		public String ask_price_4;// 27
		public String ask_volume_5;// 28
		public String ask_price_5;// 29
		public String date;// 30
		public String time;// 31
		public String status;// 32
	}

	public class StockInfo_i {
	}
}
