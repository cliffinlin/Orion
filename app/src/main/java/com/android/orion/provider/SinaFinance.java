package com.android.orion.provider;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.orion.R;
import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.data.Trend;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.TotalShare;
import com.android.orion.interfaces.IStockDataProvider;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Market;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Utility;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import okhttp3.Request;
import okhttp3.Response;

public class SinaFinance extends StockDataProvider {

	public static final String PROVIDER_NAME = "SinaFinance";

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

	public static final int DOWNLOAD_HISTORY_LENGTH_UNLIMITED = -1;
	public static final int DOWNLOAD_HISTORY_LENGTH_NONE = 0;
	public static final int DOWNLOAD_HISTORY_LENGTH_DEFAULT = 120;

	public static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN5 = 242;
	public static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN15 = 192;
	public static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN30 = 192;
	public static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN60 = 192;
	static StringBuffer mContentTitle = new StringBuffer();
	ArrayList<ContentValues> ContentValuesList = new ArrayList<>();
	ArrayList<String> mAccessDeniedStringArray = new ArrayList<>();
	ArrayMap<String, String> mRequestHeader = new ArrayMap<>();

	private SinaFinance() {
		super();

		mAccessDeniedStringArray.add(mContext.getResources().getString(
				R.string.access_denied_jp));
		mAccessDeniedStringArray.add(mContext.getResources().getString(
				R.string.access_denied_zh));
		mAccessDeniedStringArray.add(mContext.getResources().getString(
				R.string.access_denied_default));

		mRequestHeader.put(SINA_FINANCE_HEAD_REFERER_KEY, SINA_FINANCE_HEAD_REFERER_VALUE);
	}

	private static class InstanceHolder {
		private static final IStockDataProvider INSTANCE = new SinaFinance();
	}

	public static IStockDataProvider getInstance() {
		return InstanceHolder.INSTANCE;
	}

	public int getAvailableHistoryLength(String period) {
		if (TextUtils.equals(period, Period.MIN5)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN5;
		} else if (TextUtils.equals(period, Period.MIN15)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN15;
		} else if (TextUtils.equals(period, Period.MIN30)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN30;
		} else if (TextUtils.equals(period, Period.MIN60)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN60;
		} else if (TextUtils.equals(period, Period.DAY)) {
			return DOWNLOAD_HISTORY_LENGTH_UNLIMITED;
		} else if (TextUtils.equals(period, Period.WEEK)) {
			return DOWNLOAD_HISTORY_LENGTH_UNLIMITED;
		} else if (TextUtils.equals(period, Period.MONTH)) {
			return DOWNLOAD_HISTORY_LENGTH_UNLIMITED;
		}

		return 0;
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

	public String getStockHSAURLString(int page) {
		String urlString = SINA_FINANCE_URL_HQ_NODE_DATA;

		urlString += "page=" + page;
		urlString += "&num=100";
		urlString += "&sort=symbol&asc=1&node=hs_a&symbol=&_s_r_a=init";
		return urlString;
	}

	public String getStockDataHistoryURLString(Stock stock, StockData stockData, int len) {
		String urlString = "";

		if ((stock == null) || (stockData == null)) {
			return urlString;
		}

		String symbol = "&symbol=" + stock.getSE() + stock.getCode();
		String scale = "&scale=" + Period.getPeriodMinutes(stockData.getPeriod());
		String ma = "&ma=" + "no";
		String datalen = "&datalen=" + len;

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
		int availableHistoryLength = getAvailableHistoryLength(period);

		if (availableHistoryLength > 0) {
			result = availableHistoryLength;
		} else if (availableHistoryLength == DOWNLOAD_HISTORY_LENGTH_UNLIMITED) {
			result = DOWNLOAD_HISTORY_LENGTH_DEFAULT;
		}

		return result;
	}

	private int getDownloadStockDataLength(StockData stockData) {
		int result = 0;

		if (stockData == null) {
			return result;
		}

		Cursor cursor = null;
		try {
			long stockId = stockData.getStockId();
			String period = stockData.getPeriod();
			int defaultValue = getDownloadHistoryLengthDefault(period);
			String selection = mDatabaseManager.getStockDataSelection(stockId,
					period, Trend.LEVEL_NONE);
			String sortOrder = mDatabaseManager.getStockDataOrder();
			cursor = mDatabaseManager.queryStockData(selection, null,
					sortOrder);
			if ((cursor == null) || (cursor.getCount() == 0) || (cursor.getCount() == 1)) {
				return defaultValue;
			}

			cursor.moveToLast();
			stockData.set(cursor);

			if (Market.isTradingHours() || Market.isLunchTime()) {
				int scheduleMinutes = Market.getScheduleMinutes();
				if (scheduleMinutes != 0) {
					result = 1;

					switch (period) {
						case Period.MIN60:
							result += scheduleMinutes
									/ Constant.MIN60;
							break;
						case Period.MIN30:
							result += scheduleMinutes
									/ Constant.MIN30;
							break;
						case Period.MIN15:
							result += scheduleMinutes
									/ Constant.MIN15;
							break;
						case Period.MIN5:
							result += scheduleMinutes
									/ Constant.MIN5;
							break;
					}
				}
			} else {
				result = defaultValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mDatabaseManager.closeCursor(cursor);
		}

		return result;
	}

	@Override
	public int downloadStockHSA() {
		int result = RESULT_NONE;

		if (System.currentTimeMillis() - Setting.getDownloadStockHSA() < Config.downloadStockHSAInterval) {
			return result;
		}

		int page = 1;
		while (true) {
			result = downloadStockHSA(mRequestHeader, getStockHSAURLString(page));
			if (result != RESULT_SUCCESS) {
				break;
			}
			page++;
		}

		return result;
	}

	private int downloadStockHSA(ArrayMap<String, String> requestHeaderArray, String urlString) {
		int result = RESULT_NONE;

		if (requestHeaderArray == null) {
			return result;
		}

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
				} else if (TextUtils.isEmpty(resultString) || TextUtils.equals(resultString, "[]")) {
					return RESULT_NONE;
				} else {
					result = RESULT_SUCCESS;
				}

				handleResponseStockHSA(resultString);
				Thread.sleep(Config.downloadSleep);
			}
		} catch (Exception e) {
			result = RESULT_FAILED;
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseStockHSA(String response) {
		StopWatch.start();
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

		if (mDatabaseManager.getStockCount(DatabaseContract.COLUMN_CLASSES
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

					mDatabaseManager.getStock(stock);

					stock.setClasses(Stock.CLASS_A);

					nameChanged = false;
					if (!TextUtils.equals(stock.getName(), jsonObject.getString("name"))) {
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
						if (!mDatabaseManager.isStockExist(stock)) {
							stock.setCreated(Utility.getCurrentDateTimeString());
							stock.setModified(Utility
									.getCurrentDateTimeString());
							mDatabaseManager.insertStock(stock);
						} else {
							stock.setModified(Utility
									.getCurrentDateTimeString());
							if (nameChanged) {
								mDatabaseManager.updateStock(stock,
										stock.getContentValues());
							}
						}
					}
				}
			}

			if (bulkInsert) {
				mDatabaseManager.bulkInsertStock(contentValuesArray);
			}
			Setting.setDownloadStockHSA(System.currentTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d("size:" + jsonArray.size()
				+ " " + StopWatch.getInterval() + "s");
	}

	@Override
	public int downloadStockInformation(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		return downloadStockInformation(stock, mRequestHeader, getStockInformationURLString(stock));
	}

	private int downloadStockInformation(Stock stock, ArrayMap<String, String> requestHeaderArray, String urlString) {
		int result = RESULT_NONE;

		if (requestHeaderArray == null) {
			return result;
		}

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
				Thread.sleep(Config.downloadSleep);
			}
		} catch (Exception e) {
			result = RESULT_FAILED;
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseStockInformation(Stock stock, String response) {
		StopWatch.start();
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

			if (!TextUtils.equals(stock.getSE(), codeInfo[2].substring(0, 2))
					|| !TextUtils.equals(stock.getCode(), codeInfo[2].substring(2, 8))) {
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
				stock.setTotalShare(Double.parseDouble(stockInfo[7])
						* Constant.DOUBLE_CONSTANT_WAN);
			}

			mDatabaseManager.updateStock(stock,
					stock.getContentValuesInformation());

			Setting.setStockDataChanged(stock.getSE(), stock.getCode(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.getName() + " "
				+ stock.getClasses() + " " + stock.getPinyin() + " "
				+ stock.getTotalShare() + " " + StopWatch.getInterval()
				+ "s");
	}

	@Override
	public int downloadStockRealTime(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		return downloadStockRealTime(stock, mRequestHeader, getStockRealTimeURLString(stock));
	}

	private int downloadStockRealTime(Stock stock, ArrayMap<String, String> requestHeaderArray, String urlString) {
		int result = RESULT_NONE;

		if (requestHeaderArray == null) {
			return result;
		}

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
				Thread.sleep(Config.downloadSleep);
			}
		} catch (Exception e) {
			result = RESULT_FAILED;
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseStockRealTime(Stock stock, String response) {
		StopWatch.start();
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

			if (!TextUtils.equals(stock.getSE(), codeInfo[3].substring(0, 2))
					|| !TextUtils.equals(stock.getCode(), codeInfo[3].substring(2, 8))) {
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

			if (!TextUtils.equals(stock.getName(), stockInfo[0])) {
				stock.setName(stockInfo[0]);
			}

			if (!TextUtils.isEmpty(stockInfo[1])) {
				stock.setPrice(Double.parseDouble(stockInfo[1]));
			}

			if (!TextUtils.isEmpty(stockInfo[2])) {
				stock.setChange(Double.parseDouble(stockInfo[2]));
			}

			if (!TextUtils.isEmpty(stockInfo[3])) {
				stock.setNet(Double.parseDouble(stockInfo[3]));
			}

			if (!TextUtils.isEmpty(stockInfo[4])) {
				stock.setVolume(Long.parseLong(stockInfo[4]));
			}

			if (!TextUtils.isEmpty(stockInfo[5])) {
				stock.setValue(Double.valueOf(stockInfo[5]).longValue());
			}

			mDatabaseManager.updateStock(stock,
					stock.getContentValuesRealTime());

			Setting.setStockDataChanged(stock.getSE(), stock.getCode(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.getName() + " "
				+ stock.getPrice() + " " + stock.getChange() + " "
				+ stock.getNet() + " " + stock.getVolume() + " "
				+ stock.getValue() + " " + StopWatch.getInterval()
				+ "s");
	}

	@Override
	public int downloadStockDataHistory(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
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
		mDatabaseManager.getStockData(stockData);
		stockData.setSE(stock.getSE());
		stockData.setCode(stock.getCode());
		stockData.setName(stock.getName());

		int len = getDownloadStockDataLength(stockData);
		if (len <= 0) {
			Log.d(stock.getName() + " return, period=" + period + " len=" + len);
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
				Thread.sleep(Config.downloadSleep);
			}
		} catch (Exception e) {
			result = RESULT_FAILED;
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseStockDataHistory(Stock stock, StockData stockData,
	                                           String response) {
		StopWatch.start();
		boolean bulkInsert = false;
		int defaultValue = 0;
		String dateTimeString = "";
		String[] dateTime = null;
		JSONArray jsonArray = null;
		ContentValuesList.clear();
		ArrayMap<String, StockData> stockDataMap = new ArrayMap<>();

		if (stock == null || stockData == null) {
			return;
		}

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
				mDatabaseManager.deleteStockData(stockData.getStockId(),
						stockData.getPeriod());
				bulkInsert = true;
			}

			if (bulkInsert) {
				if (Setting.getDebugDataFile()) {
					importStockDataFile(stock, stockData, ContentValuesList, stockDataMap);
				}
			}

			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);

				if (jsonObject != null) {
					dateTimeString = jsonObject.getString(Period.DAY);
					if (!TextUtils.isEmpty(dateTimeString)) {
						dateTime = dateTimeString.trim().split(" ");
						switch (dateTime.length) {
							case 1:
								stockData.setDate(dateTime[0]);
								stockData.setTime("");
								break;
							case 2:
								stockData.setDate(dateTime[0]);
								if (Period.isMinutePeriod(stockData.getPeriod())) {
									stockData.setTime(dateTime[1]);
								}
								break;
							default:
								break;
						}
					}

					stockData.getCandlestick().setOpen(jsonObject.getDouble("open"));
					stockData.getCandlestick().setClose(jsonObject.getDouble("close"));
					stockData.getCandlestick().setHigh(jsonObject.getDouble("high"));
					stockData.getCandlestick().setLow(jsonObject.getDouble("low"));

					stockData.getTrend().setVertexHigh(stockData.getCandlestick().getHigh());
					stockData.getTrend().setVertexLow(stockData.getCandlestick().getLow());

					if (bulkInsert) {
						stockData.setCreated(Utility.getCurrentDateTimeString());
						stockData.setModified(Utility.getCurrentDateTimeString());

						if (Period.isMinutePeriod(stockData.getPeriod())) {
							if (!stockDataMap.containsKey(stockData.getDateTime())) {
								stockDataMap.put(stockData.getDateTime(), new StockData(stockData));
								ContentValuesList.add(stockData.getContentValues());
							}
						} else {
							ContentValuesList.add(stockData.getContentValues());
						}
					} else {
						if (!mDatabaseManager.isStockDataExist(stockData)) {
							stockData.setCreated(Utility
									.getCurrentDateTimeString());
							stockData.setModified(Utility
									.getCurrentDateTimeString());
							mDatabaseManager.insertStockData(stockData);
						} else {
							stockData.setModified(Utility
									.getCurrentDateTimeString());
							mDatabaseManager.updateStockData(stockData,
									stockData.getContentValues());
						}
					}
				}
			}

			if (bulkInsert) {
				if (Setting.getDebugDataFile()) {
					exportStockDataFile(stock, stockData, stockDataMap);
				}

				if (ContentValuesList.size() > 0) {
					fixContentValuesList(stockData, ContentValuesList);

					ContentValues[] contentValuesArray = new ContentValues[ContentValuesList
							.size()];
					contentValuesArray = ContentValuesList
							.toArray(contentValuesArray);
					mDatabaseManager.bulkInsertStockData(contentValuesArray);
				}
			}

			Setting.setStockDataChanged(stock.getSE(), stock.getCode(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.getName() + " "
				+ stockData.getPeriod() + " " + StopWatch.getInterval() + "s");
	}

	@Override
	public int downloadStockDataRealTime(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
				if (TextUtils.equals(period, Period.DAY)) {
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
		mDatabaseManager.getStockData(stockData);
		stockData.setSE(stock.getSE());
		stockData.setCode(stock.getCode());
		stockData.setName(stock.getName());

		return downloadStockDataRealTime(stock, stockData, mRequestHeader, getStockDataRealTimeURLString(stock));
	}

	private int downloadStockDataRealTime(Stock stock, StockData stockData, ArrayMap<String, String> requestHeaderArray, String urlString) {
		int result = RESULT_NONE;

		if (requestHeaderArray == null) {
			return result;
		}

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
				Thread.sleep(Config.downloadSleep);
			}
		} catch (Exception e) {
			result = RESULT_FAILED;
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseStockDataRealTime(Stock stock, StockData stockData,
	                                            String response) {
		StopWatch.start();
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

			if (!TextUtils.isEmpty(stockInfo[32])) {
				if (stockInfo[32].startsWith("00")) {
					stock.setStatus("");
				} else {
					stock.setStatus(Stock.STATUS_SUSPENSION);
				}
			}

			for (int i = 1; i < 6; i++) {
				if (Float.parseFloat(stockInfo[i]) <= 0) {
					Log.d("return, Float.parseFloat(stockInfo[" + i + "]) = "
							+ Float.valueOf(stockInfo[i]));
					return;
				}
			}

			stockData.getCandlestick().setOpen(Double.parseDouble(stockInfo[1]));
			stockData.getCandlestick().setClose(Double.parseDouble(stockInfo[3]));
			stockData.getCandlestick().setHigh(Double.parseDouble(stockInfo[4]));
			stockData.getCandlestick().setLow(Double.parseDouble(stockInfo[5]));

			stockData.getTrend().setVertexHigh(stockData.getCandlestick().getHigh());
			stockData.getTrend().setVertexLow(stockData.getCandlestick().getLow());

			stockData.setDate(stockInfo[30]);
			if (Period.isMinutePeriod(stockData.getPeriod())) {
				stockData.setTime(stockInfo[31]);
			}

			if (!mDatabaseManager.isStockDataExist(stockData)) {
				stockData.setCreated(Utility.getCurrentDateTimeString());
				mDatabaseManager.insertStockData(stockData);
			} else {
				stockData.setModified(Utility.getCurrentDateTimeString());
				mDatabaseManager.updateStockData(stockData,
						stockData.getContentValues());
			}

			Setting.setStockDataChanged(stock.getSE(), stock.getCode(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.getName() + Constant.TAB
				+ stockData.getDate() + Constant.TAB + stockData.getTime() + Constant.TAB
				+ stockData.getCandlestick().toString()
				+ StopWatch.getInterval() + "s");
	}

	@Override
	public int downloadStockFinancial(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		StockFinancial stockFinancial = new StockFinancial();
		stockFinancial.setStockId(stock.getId());
		mDatabaseManager.getStockFinancial(stock, stockFinancial);

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
				Thread.sleep(Config.downloadSleep);
			}
		} catch (Exception e) {
			result = RESULT_FAILED;
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseStockFinancial(Stock stock, StockFinancial stockFinancial,
	                                         String response) {
		StopWatch.start();
		boolean bulkInsert = false;
		String keyString = "";
		String valueString = "";
		ContentValuesList.clear();

		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d("return, stock = "
					+ stock + " response = " + response);
			return;
		}

		if (TextUtils.isEmpty(stockFinancial.getCreated())) {
			mDatabaseManager.deleteStockFinancial(stockFinancial
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

					double valueDouble = Double.parseDouble(valueString);
					if (TextUtils.equals(keyString, "BIZINCO") || TextUtils.equals(keyString, "BIZTOTINCO")) {//营业总收入
						stockFinancial.setMainBusinessIncome(valueDouble);
					} else if (TextUtils.equals(keyString, "BIZEXPE") || TextUtils.equals(keyString, "BIZTOTCOST")) {//营业成本
						stockFinancial.setFinancialExpenses(valueDouble);
					} else if (TextUtils.equals(keyString, "NETPROFIT")) {//净利润
//					} else if (TextUtils.equals(keyString, "NETPARECOMPPROF") || TextUtils.equals(keyString, "NETPARESHARPROF") || TextUtils.equals(keyString, "PARENETP")) {//归母净利润
						stockFinancial.setNetProfit(valueDouble);
					} else if (TextUtils.equals(keyString, "NAPS")) {//每股净资产
						stockFinancial.setBookValuePerShare(valueDouble);
					} else if (TextUtils.equals(keyString, "OPNCFPS")) {//每股现金流
						stockFinancial.setCashFlowPerShare(valueDouble);
					} else if (TextUtils.equals(keyString, mContext.getResources().getString(R.string.key_total_current_assets))) {
						stockFinancial.setTotalCurrentAssets(valueDouble);
					} else if (TextUtils.equals(keyString, mContext.getResources().getString(R.string.key_total_assets))) {
						stockFinancial.setTotalAssets(valueDouble);
					} else if (TextUtils.equals(keyString, "ASSLIABRT")) {//资产负债率
						stockFinancial
								.setDebtToNetAssetsRatio(valueDouble);
						stockFinancial.setupNetProfitPerShare(stock
								.getTotalShare());

						if (bulkInsert) {
							stockFinancial.setCreated(Utility
									.getCurrentDateTimeString());
							stockFinancial.setModified(Utility
									.getCurrentDateTimeString());
							ContentValuesList.add(stockFinancial
									.getContentValues());
						} else {
							if (!mDatabaseManager
									.isStockFinancialExist(stockFinancial)) {
								stockFinancial.setCreated(Utility
										.getCurrentDateTimeString());
								stockFinancial.setModified(Utility
										.getCurrentDateTimeString());
								mDatabaseManager
										.insertStockFinancial(stockFinancial);
							} else {
								stockFinancial.setModified(Utility
										.getCurrentDateTimeString());
								mDatabaseManager
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
				if (ContentValuesList.size() > 0) {
					ContentValues[] contentValuesArray = new ContentValues[ContentValuesList
							.size()];
					contentValuesArray = ContentValuesList
							.toArray(contentValuesArray);
					mDatabaseManager
							.bulkInsertStockFinancial(contentValuesArray);
				}
			}

			Setting.setStockDataChanged(stock.getSE(), stock.getCode(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.getName() + " "
				+ StopWatch.getInterval() + "s");
	}

	@Override
	public int downloadShareBonus(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		ShareBonus shareBonus = new ShareBonus();
		shareBonus.setStockId(stock.getId());
		mDatabaseManager.getShareBonus(stock.getId(), shareBonus);

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
				Thread.sleep(Config.downloadSleep);
			}
		} catch (Exception e) {
			result = RESULT_FAILED;
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseShareBonus(Stock stock, ShareBonus shareBonus,
	                                     String response) {
		StopWatch.start();
		boolean bulkInsert = false;
		String dateString = "";
		String dividendString = "";
		String rDateString = "";
		ContentValuesList.clear();

		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d("return, stock = " + stock
					+ " response = " + response);
			return;
		}

		mDatabaseManager.deleteShareBonus(shareBonus.getStockId());
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
							|| dateString.contains(Stock.STATUS_SUSPENSION)) {
						continue;
					}

					dividendString = tdElements.get(3).text();
					if (TextUtils.isEmpty(dividendString)
							|| dividendString.contains(Stock.STATUS_SUSPENSION)) {
						continue;
					}

					rDateString = tdElements.get(6).text();
					if (TextUtils.isEmpty(rDateString)) {
						continue;
					}

					shareBonus.setDate(dateString);
					shareBonus.setDividend(Double.parseDouble(dividendString));
					shareBonus.setRDate(rDateString);
					if (!rDateString.equals(Stock.STATUS_SUSPENSION)) {
						shareBonus.setDate(rDateString);
					}

					if (bulkInsert) {
						shareBonus.setCreated(Utility
								.getCurrentDateTimeString());
						shareBonus.setModified(Utility
								.getCurrentDateTimeString());
						ContentValuesList.add(shareBonus.getContentValues());
					} else {
						if (!mDatabaseManager
								.isShareBonusExist(shareBonus)) {
							shareBonus.setCreated(Utility
									.getCurrentDateTimeString());
							shareBonus.setModified(Utility
									.getCurrentDateTimeString());
							mDatabaseManager.insertShareBonus(shareBonus);
						} else {
							shareBonus.setModified(Utility
									.getCurrentDateTimeString());
							mDatabaseManager.updateShareBonus(shareBonus,
									shareBonus.getContentValues());
						}
					}
				}
			}

			if (bulkInsert) {
				if (ContentValuesList.size() > 0) {
					ContentValues[] contentValuesArray = new ContentValues[ContentValuesList
							.size()];
					contentValuesArray = ContentValuesList
							.toArray(contentValuesArray);
					mDatabaseManager
							.bulkInsertShareBonus(contentValuesArray);
				}
			}

			Setting.setStockDataChanged(stock.getSE(), stock.getCode(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.getName() + " "
				+ StopWatch.getInterval() + "s");
	}

	@Override
	public int downloadTotalShare(Stock stock) {
		int result = RESULT_NONE;

		if (stock == null) {
			return result;
		}

		TotalShare totalShare = new TotalShare();
		totalShare.setStockId(stock.getId());
		mDatabaseManager.getTotalShare(stock.getId(), totalShare);

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
				Thread.sleep(Config.downloadSleep);
			}
		} catch (Exception e) {
			result = RESULT_FAILED;
			e.printStackTrace();
		}

		return result;
	}

	public void handleResponseTotalShare(Stock stock, TotalShare totalShare,
	                                     String response) {
		StopWatch.start();
		boolean bulkInsert = false;
		String dateString = "";
		String totalShareString = "";
		ContentValuesList.clear();

		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d("return, stock = " + stock
					+ " response = " + response);
			return;
		}

		mDatabaseManager.deleteTotalShare(totalShare.getStockId());
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
							|| dateString.contains(Stock.STATUS_SUSPENSION)) {
						continue;
					}

					totalShareString = tdElements.get(1).text();
					if (TextUtils.isEmpty(totalShareString)
							|| totalShareString.contains(Stock.STATUS_SUSPENSION)) {
						continue;
					}

					if (totalShareString.contains(mContext.getResources().getString(R.string.ten_thousand_shares))) {
						totalShareString = totalShareString.replace(mContext.getResources().getString(R.string.ten_thousand_shares), "");
						totalShare.setDate(dateString);
						totalShare.setTotalShare(Double
								.parseDouble(totalShareString)
								* Constant.DOUBLE_CONSTANT_WAN);
					} else {
						continue;
					}

					if (bulkInsert) {
						totalShare.setCreated(Utility
								.getCurrentDateTimeString());
						totalShare.setModified(Utility
								.getCurrentDateTimeString());
						ContentValuesList.add(totalShare.getContentValues());
					} else {
						if (!mDatabaseManager
								.isTotalShareExist(totalShare)) {
							totalShare.setCreated(Utility
									.getCurrentDateTimeString());
							totalShare.setModified(Utility
									.getCurrentDateTimeString());
							mDatabaseManager.insertTotalShare(totalShare);
						} else {
							totalShare.setModified(Utility
									.getCurrentDateTimeString());
							mDatabaseManager.updateTotalShare(totalShare,
									totalShare.getContentValues());
						}
					}
				}
			}

			if (bulkInsert) {
				if (ContentValuesList.size() > 0) {
					ContentValues[] contentValuesArray = new ContentValues[ContentValuesList
							.size()];
					contentValuesArray = ContentValuesList
							.toArray(contentValuesArray);
					mDatabaseManager
							.bulkInsertTotalShare(contentValuesArray);
				}
			}

			Setting.setStockDataChanged(stock.getSE(), stock.getCode(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.getName() + " "
				+ StopWatch.getInterval() + "s");
	}

	private boolean isAccessDenied(String string) {
		boolean result = false;

		if (TextUtils.isEmpty(string)) {
			return result;
		}

		mContentTitle.setLength(0);
		String accessDeniedString;
		for (int i = 0; i < mAccessDeniedStringArray.size(); i++) {
			accessDeniedString = mAccessDeniedStringArray.get(i);

			if (string.contains(accessDeniedString)) {
				mContentTitle.append(mContext.getResources().getString(R.string.action_download));
				mContentTitle.append(" ");
				mContentTitle.append(accessDeniedString);

				mStockAnalyzer.notify(Config.SERVICE_NOTIFICATION_ID, Config.MESSAGE_CHANNEL_ID, Config.MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH,
						mContentTitle.toString(), "");
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
