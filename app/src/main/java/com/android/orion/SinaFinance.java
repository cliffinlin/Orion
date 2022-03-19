package com.android.orion;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.StockFinancial;
import com.android.orion.database.IPO;
import com.android.orion.database.ShareBonus;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.TotalShare;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Utility;

public class SinaFinance extends StockDataProvider {
	static final String TAG = Constants.TAG + " "
			+ SinaFinance.class.getSimpleName();

	public static final String SINA_FINANCE_URL_HQ_NODE_DATA = "http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?";
	public static final String SINA_FINANCE_URL_HQ_KLINE_DATA = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?";
	public static final String SINA_FINANCE_URL_HQ_JS_LIST = "http://hq.sinajs.cn/list=";
	public static final String SINA_FINANCE_URL_HQ_JS_LIST_SIMPLE = "http://hq.sinajs.cn/list=s_";
	public static final String SINA_FINANCE_URL_VFD_FINANCESUMMARY = "http://money.finance.sina.com.cn/corp/go.php/vFD_FinanceSummary/stockid/";// stock_id.phtml
	public static final String SINA_FINANCE_URL_VISSUE_SHAREBONUS = "http://vip.stock.finance.sina.com.cn/corp/go.php/vISSUE_ShareBonus/stockid/";// stock_id.phtml
	public static final String SINA_FINANCE_URL_VCI_STOCK_STRUCTURE_HISTORY = "http://vip.stock.finance.sina.com.cn/corp/go.php/vCI_StockStructureHistory/stockid/";// stocktype/TotalStock.phtml
	public static final String SINA_FINANCE_URL_NEWSTOCK_ISSUE = "https://vip.stock.finance.sina.com.cn/corp/go.php/vRPD_NewStockIssue/page/1.phtml";

	public static final String SINA_FINANCE_HEAD_REFERER_KEY = "Referer";
	public static final String SINA_FINANCE_HEAD_REFERER_VALUE = "http://vip.stock.finance.sina.com.cn/";

	public static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN5 = 242;
	public static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN15 = 192;
	public static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN30 = 192;
	public static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN60 = 192;

	public static final int PERIOD_MINUTES_MIN1 = 1;
	public static final int PERIOD_MINUTES_MIN5 = 5;
	public static final int PERIOD_MINUTES_MIN15 = 15;
	public static final int PERIOD_MINUTES_MIN30 = 30;
	public static final int PERIOD_MINUTES_MIN60 = 60;
	public static final int PERIOD_MINUTES_DAY = 240;
	public static final int PERIOD_MINUTES_WEEK = 1680;
	public static final int PERIOD_MINUTES_MONTH = 7200;
	public static final int PERIOD_MINUTES_QUARTER = 28800;
	public static final int PERIOD_MINUTES_YEAR = 115200;

	public SinaFinance(Context context) {
		super(context);
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

	@Override
	int getAvailableHistoryLength(String period) {
		if (period.equals(Settings.KEY_PERIOD_MIN1)) {
			return Constants.DOWNLOAD_HISTORY_LENGTH_NONE;
		} else if (period.equals(Settings.KEY_PERIOD_MIN5)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN5;
		} else if (period.equals(Settings.KEY_PERIOD_MIN15)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN15;
		} else if (period.equals(Settings.KEY_PERIOD_MIN30)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN30;
		} else if (period.equals(Settings.KEY_PERIOD_MIN60)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN60;
		} else if (period.equals(Settings.KEY_PERIOD_DAY)) {
			return Constants.DOWNLOAD_HISTORY_LENGTH_UNLIMITED;
		} else if (period.equals(Settings.KEY_PERIOD_WEEK)) {
			return Constants.DOWNLOAD_HISTORY_LENGTH_UNLIMITED;
		} else if (period.equals(Settings.KEY_PERIOD_MONTH)) {
			return Constants.DOWNLOAD_HISTORY_LENGTH_UNLIMITED;
		} else if (period.equals(Settings.KEY_PERIOD_QUARTER)) {
			return Constants.DOWNLOAD_HISTORY_LENGTH_NONE;
		} else if (period.equals(Settings.KEY_PERIOD_YEAR)) {
			return Constants.DOWNLOAD_HISTORY_LENGTH_NONE;
		} else {
		}

		return 0;
	}

	@Override
	ArrayMap<String, String> getRequestHeader() {
		ArrayMap<String, String> result = new ArrayMap<>();

		result.put(SINA_FINANCE_HEAD_REFERER_KEY, SINA_FINANCE_HEAD_REFERER_VALUE);

		return result;
	}

	@Override
	String getStockInformationURLString(Stock stock) {
		String urlString = "";
		if (stock == null) {
			return urlString;
		}
		urlString = SINA_FINANCE_URL_HQ_JS_LIST + stock.getSE()
				+ stock.getCode() + "_i";
		return urlString;
	}

	@Override
	String getStockRealTimeURLString(Stock stock) {
		String urlString = "";
		if (stock == null) {
			return urlString;
		}
		urlString = SINA_FINANCE_URL_HQ_JS_LIST_SIMPLE + stock.getSE()
				+ stock.getCode();
		return urlString;
	}

	@Override
	String getStockHSAURLString() {
		String page = "&page=1";
		String num = "&num=1000000";
		String sort = "&sort=symbol";
		String asc = "&asc=1";
		String node = "&node=hs_a";
		String urlString = SINA_FINANCE_URL_HQ_NODE_DATA + page + num + sort
				+ asc + node;
		return urlString;
	}

	int getPeriodMinutes(String period) {
		int result = 0;

		if (period.equals(Settings.KEY_PERIOD_MIN1)) {
			result = PERIOD_MINUTES_MIN1;
		} else if (period.equals(Settings.KEY_PERIOD_MIN5)) {
			result = PERIOD_MINUTES_MIN5;
		} else if (period.equals(Settings.KEY_PERIOD_MIN15)) {
			result = PERIOD_MINUTES_MIN15;
		} else if (period.equals(Settings.KEY_PERIOD_MIN30)) {
			result = PERIOD_MINUTES_MIN30;
		} else if (period.equals(Settings.KEY_PERIOD_MIN60)) {
			result = PERIOD_MINUTES_MIN60;
		} else if (period.equals(Settings.KEY_PERIOD_DAY)) {
			result = PERIOD_MINUTES_DAY;
		} else if (period.equals(Settings.KEY_PERIOD_WEEK)) {
			result = PERIOD_MINUTES_WEEK;
		} else if (period.equals(Settings.KEY_PERIOD_MONTH)) {
			result = PERIOD_MINUTES_MONTH;
		} else if (period.equals(Settings.KEY_PERIOD_QUARTER)) {
			result = PERIOD_MINUTES_QUARTER;
		} else if (period.equals(Settings.KEY_PERIOD_YEAR)) {
			result = PERIOD_MINUTES_YEAR;
		} else {
		}

		return result;
	}

	@Override
	String getStockDataHistoryURLString(Stock stock, StockData stockData,
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

	@Override
	String getStockDataRealTimeURLString(Stock stock) {
		String urlString = "";
		if (stock == null) {
			return urlString;
		}
		urlString = SINA_FINANCE_URL_HQ_JS_LIST + stock.getSE()
				+ stock.getCode();
		return urlString;
	}

	@Override
	String getStockFinancialURLString(Stock stock) {
		String urlString = "";
		if (stock == null) {
			return urlString;
		}
		urlString = SINA_FINANCE_URL_VFD_FINANCESUMMARY + stock.getCode()
				+ ".phtml";
		return urlString;
	}

	@Override
	String getShareBonusURLString(Stock stock) {
		String urlString = "";
		if (stock == null) {
			return urlString;
		}
		urlString = SINA_FINANCE_URL_VISSUE_SHAREBONUS + stock.getCode()
				+ ".phtml";
		return urlString;
	}

	@Override
	String getTotalShareURLString(Stock stock) {
		String urlString = "";
		if (stock == null) {
			return urlString;
		}
		urlString = SINA_FINANCE_URL_VCI_STOCK_STRUCTURE_HISTORY
				+ stock.getCode() + "/stocktype/TotalStock.phtml";
		return urlString;
	}

	@Override
	String getIPOURLString() {
		String urlString = "";

		urlString = SINA_FINANCE_URL_NEWSTOCK_ISSUE;

		return urlString;
	}

	@Override
	void handleResponseStockInformation(Stock stock, String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		String keyValue[] = null;
		String codeInfo[] = null;
		String stockInfo[] = null;

		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d(TAG, "handleResponseStockInformation return " + " stock = "
					+ stock + " response = " + response);
			return;
		}

		try {
			keyValue = response.trim().split("=");

			if (keyValue[0] == null) {
				Log.d(TAG,
						"handleResponseStockInformation return keyValue[0] = "
								+ keyValue[0]);
				return;
			}

			codeInfo = keyValue[0].trim().split("_");

			if (codeInfo[2] == null) {
				Log.d(TAG,
						"handleResponseStockInformation return codeInfo[2] = "
								+ codeInfo[2]);
				return;
			}

			if (!stock.getSE().equals(codeInfo[2].substring(0, 2))
					|| !stock.getCode().equals(codeInfo[2].substring(2, 8))) {
				Log.d(TAG, "handleResponseStockInformation return");
				return;
			}

			if (keyValue[1] == null) {
				Log.d(TAG,
						"handleResponseStockInformation return keyValue[1] = "
								+ keyValue[1]);
				return;
			}

			stockInfo = keyValue[1].substring(1, keyValue[1].length() - 2)
					.split(",");

			if (stockInfo == null) {
				Log.d(TAG, "handleResponseStockInformation return stockInfo = "
						+ stockInfo);
				return;
			}

			if (!TextUtils.isEmpty(stockInfo[0])) {
				stock.setClasses(stockInfo[0]);
			}

			if (!TextUtils.isEmpty(stockInfo[1])) {
				stock.setPinyin(stockInfo[1]);
			}

			if (stockInfo.length > 7) {
				if (!TextUtils.isEmpty(stockInfo[7])) {
					stock.setTotalShare(Double.valueOf(stockInfo[7])
							* Constants.DOUBLE_CONSTANT_WAN);
				}
			}

			if (!mStockDatabaseManager.isStockExist(stock)) {
				stock.setCreated(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.insertStock(stock);
			} else {
				stock.setModified(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.updateStock(stock,
						stock.getContentValues());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(TAG,
				"handleResponseStockInformation:" + stock.getName() + " "
						+ stock.getPrice() + " " + stock.getChange() + " "
						+ stock.getNet() + " " + stock.getVolume() + " "
						+ stock.getValue() + " " + stopWatch.getInterval()
						+ "s");
	}

	@Override
	void handleResponseStockRealTime(Stock stock, String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		String keyValue[] = null;
		String codeInfo[] = null;
		String stockInfo[] = null;

		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d(TAG, "handleResponseStockRealTime return " + " stock = "
					+ stock + " response = " + response);
			return;
		}

		try {
			keyValue = response.trim().split("=");

			if (keyValue[0] == null) {
				Log.d(TAG, "handleResponseStockRealTime return keyValue[0] = "
						+ keyValue[0]);
				return;
			}

			codeInfo = keyValue[0].trim().split("_");

			if (codeInfo[3] == null) {
				Log.d(TAG, "handleResponseStockRealTime return codeInfo[3] = "
						+ codeInfo[3]);
				return;
			}

			if (!stock.getSE().equals(codeInfo[3].substring(0, 2))
					|| !stock.getCode().equals(codeInfo[3].substring(2, 8))) {
				Log.d(TAG, "handleResponseStockRealTime return");
				return;
			}

			if (keyValue[1] == null) {
				Log.d(TAG, "handleResponseStockRealTime return keyValue[1] = "
						+ keyValue[1]);
				return;
			}

			stockInfo = keyValue[1].substring(1, keyValue[1].length() - 2)
					.split(",");

			if (stockInfo == null) {
				Log.d(TAG, "handleResponseStockRealTime return stockInfo = "
						+ stockInfo);
				return;
			}

			if (!stock.getName().equals(stockInfo[0])) {
				stock.setName(stockInfo[0]);
			}

			if (stockInfo.length == 6) {
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
			}

			if (!mStockDatabaseManager.isStockExist(stock)) {
				stock.setCreated(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.insertStock(stock);
			} else {
				stock.setModified(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.updateStock(stock,
						stock.getContentValues());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(TAG,
				"handleResponseStockRealTime:" + stock.getName() + " "
						+ stock.getPrice() + " " + stock.getChange() + " "
						+ stock.getNet() + " " + stock.getVolume() + " "
						+ stock.getValue() + " " + stopWatch.getInterval()
						+ "s");
	}

	@Override
	void handleResponseStockHSA(String response) {
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
			Log.d(TAG, "handleResponseStockHSA return response = " + response);
			return;
		}

		if (mStockDatabaseManager.getStockCount(DatabaseContract.COLUMN_CLASSES
				+ " = '" + Stock.CLASS_A + "'", null, null) == 0) {
			bulkInsert = true;
			Log.d(TAG, "handleResponseStockHSA bulkInsert = " + bulkInsert);
		}

		try {
			jsonArray = JSON.parseArray(response);

			if (jsonArray == null) {
				Log.d(TAG, "handleResponseStockHSA return jsonArray = "
						+ jsonArray);
				return;
			}

			if (jsonArray.size() == 0) {
				Log.d(TAG, "handleResponseStockHSA return jsonArray.size() = "
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
		Log.d(TAG, "handleResponseStockHSA:" + " " + "size:" + jsonArray.size()
				+ " " + stopWatch.getInterval() + "s");
	}

	@Override
	void handleResponseStockDataHistory(Stock stock, StockData stockData,
			String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		boolean bulkInsert = false;
		int defaultValue = 0;
		ContentValues[] contentValuesArray = null;
		String dateTimeString = "";
		String dateTime[] = null;
		JSONArray jsonArray = null;

		if ((stock == null) || (stockData == null)
				|| TextUtils.isEmpty(response)) {
			Log.d(TAG, "handleResponseStockDataHistory return " + " stock = "
					+ stock + " stockData = " + stockData + " response = "
					+ response);
			return;
		}

		try {
			jsonArray = JSON.parseArray(response);

			if (jsonArray == null) {
				Log.d(TAG, "handleResponseStockDataHistory return jsonArray = "
						+ jsonArray);
				return;
			}

			if (jsonArray.size() == 0) {
				Log.d(TAG,
						"handleResponseStockDataHistory return jsonArray.size() = "
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
				if (contentValuesArray == null) {
					contentValuesArray = new ContentValues[jsonArray.size()];
				}
			}

			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);

				if (jsonObject != null) {
					dateTimeString = jsonObject.getString("day");
					if (!TextUtils.isEmpty(dateTimeString)) {
						dateTime = dateTimeString.trim().split(" ");
						switch (dateTime.length) {
						case 1:
							stockData.setDate(dateTime[0]);
							stockData.setTime("");
							break;
						case 2:
							stockData.setDate(dateTime[0]);
							stockData.setTime(dateTime[1]);
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
						stockData
								.setCreated(Utility.getCurrentDateTimeString());
						stockData.setModified(Utility
								.getCurrentDateTimeString());
						contentValuesArray[i] = stockData.getContentValues();
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
				mStockDatabaseManager.bulkInsertStockData(contentValuesArray);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(TAG, "handleResponseStockDataHistory:" + stock.getName() + " "
				+ stockData.getPeriod() + " " + stopWatch.getInterval() + "s");
	}

	@Override
	void handleResponseStockDataRealTime(Stock stock, StockData stockData,
			String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		String keyValue[] = null;
		String codeInfo[] = null;
		String stockInfo[] = null;

		if ((stock == null) || (stockData == null)
				|| TextUtils.isEmpty(response)) {
			Log.d(TAG, "handleResponseStockDataRealTime return " + " stock = "
					+ stock + " stockData = " + stockData + " response = "
					+ response);
			return;
		}

		try {
			keyValue = response.trim().split("=");

			if (keyValue == null) {
				Log.d(TAG, "handleResponseStockDataRealTime return "
						+ " keyValue = " + keyValue);
				return;
			}

			if (keyValue.length < 2) {
				Log.d(TAG, "handleResponseStockDataRealTime return "
						+ " keyValue.length = " + keyValue.length);
				return;
			}

			if (keyValue[0] == null) {
				Log.d(TAG, "handleResponseStockDataRealTime return "
						+ " keyValue[0] = " + keyValue[0]);
				return;
			}

			codeInfo = keyValue[0].trim().split("_");

			if (codeInfo == null) {
				Log.d(TAG, "handleResponseStockDataRealTime return "
						+ " codeInfo = " + codeInfo);
				return;
			}

			if (codeInfo.length < 3) {
				Log.d(TAG, "handleResponseStockDataRealTime return "
						+ " codeInfo.length = " + codeInfo.length);
				return;
			}

			if (codeInfo[2] == null) {
				Log.d(TAG, "handleResponseStockDataRealTime return "
						+ " codeInfo[2] = " + codeInfo[2]);
				return;
			}

			if (keyValue[1] == null) {
				Log.d(TAG, "handleResponseStockDataRealTime return "
						+ " keyValue[1] = " + keyValue[1]);
				return;
			}

			stockInfo = keyValue[1].trim().split(",");

			if (stockInfo == null) {
				Log.d(TAG, "handleResponseStockDataRealTime return "
						+ " stockInfo = " + stockInfo);
				return;
			}

			if (stockInfo.length < 32) {
				Log.d(TAG, "handleResponseStockDataRealTime return "
						+ " stockInfo.length = " + stockInfo.length);
				return;
			}

			for (int i = 1; i < 6; i++) {
				if (Float.valueOf(stockInfo[i]) <= 0) {
					Log.d(TAG,
							"handleResponseStockDataRealTime return "
									+ " Float.valueOf(stockInfo[" + i + "]) = "
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
			stockData.setTime(stockInfo[31]);

			if ("00".equals(stockInfo[32])) {
				stock.setStatus("");
			} else {
				stock.setStatus(Stock.STATUS_SUSPENSION);
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
		Log.d(TAG, "handleResponseStockDataRealTime:" + stock.getName() + " "
				+ stockData.getDate() + " " + stockData.getTime() + " "
				+ stockData.getOpen() + " " + stockData.getClose() + " "
				+ stockData.getHigh() + " " + stockData.getLow() + " "
				+ stopWatch.getInterval() + "s");
	}

	@Override
	void handleResponseStockFinancial(Stock stock, StockFinancial stockFinancial,
			String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		boolean bulkInsert = false;
		String keyString = "";
		String valueString = "";
		List<ContentValues> contentValuesList = new ArrayList<ContentValues>();

		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d(TAG, "handleResponseStockFinancial return " + " stock = "
					+ stock + " response = " + response);
			return;
		}

		if (TextUtils.isEmpty(stockFinancial.getCreated())) {
			mStockDatabaseManager.deleteStockFinancial(stockFinancial
					.getStockId());
			bulkInsert = true;
		}

		try {
			// String responseString = new
			// String(response.getBytes("ISO-8859-1"),
			// "GB2312");

			Document doc = Jsoup.parse(response);
			if (doc == null) {
				Log.d(TAG, "handleResponseStockFinancial return " + " doc = "
						+ doc);
				return;
			}

			Elements tableElements = doc.select("table#FundHoldSharesTable");
			if (tableElements == null) {
				Log.d(TAG, "handleResponseStockFinancial return "
						+ " tableElements = " + tableElements);
				return;
			}

			Elements tbodyElements = tableElements.select("tbody");
			if (tbodyElements == null) {
				Log.d(TAG, "handleResponseStockFinancial return "
						+ " tbodyElements = " + tbodyElements);
				return;
			}

			for (Element tbodyElement : tbodyElements) {
				if (tbodyElement == null) {
					Log.d(TAG, "handleResponseStockFinancial return "
							+ " tbodyElement = " + tbodyElement);
					return;
				}

				Elements trElements = tbodyElement.select("tr");
				if (trElements == null) {
					Log.d(TAG, "handleResponseStockFinancial return "
							+ " trElements = " + trElements);
					return;
				}

				for (Element trElement : trElements) {
					if (trElement == null) {
						// Log.d(TAG, "handleResponseStockFinancial continue "
						// + " trElement = " + trElement);
						continue;
					}

					Elements tdElements = trElement.select("td");
					if (tdElements == null) {
						// Log.d(TAG, "handleResponseStockFinancial continue "
						// + " tdElements = " + tdElements);
						continue;
					}

					if (tdElements.size() < 2) {
						// Log.d(TAG, "handleResponseStockFinancial continue "
						// + " tdElements.size() = " + tdElements.size());
						continue;
					}

					keyString = tdElements.get(0).text();
					if (!TextUtils.isEmpty(keyString)) {
						valueString = tdElements.get(1).text();

						if (!TextUtils.isEmpty(valueString)) {
							valueString = valueString.replace(mContext.getResources().getString(R.string.yuan), "");
							valueString = valueString.replace(",", "");

							if (keyString.equals(mContext.getResources().getString(R.string.key_deadline_date))) {
								stockFinancial.setDate(valueString);
							} else if (keyString.equals(mContext.getResources().getString(R.string.key_book_value_per_share_diluted))) {
								stockFinancial.setBookValuePerShare(Double
										.valueOf(valueString));
							} else if (keyString.equals(mContext.getResources().getString(R.string.key_cash_flow_per_share))) {
								stockFinancial.setCashFlowPerShare(Double
										.valueOf(valueString));
							} else if (keyString.equals(mContext.getResources().getString(R.string.key_total_current_assets))) {
								stockFinancial.setTotalCurrentAssets(Double
										.valueOf(valueString));
							} else if (keyString.equals(mContext.getResources().getString(R.string.key_total_assets))) {
								stockFinancial.setTotalAssets(Double
										.valueOf(valueString));
							} else if (keyString.equals(mContext.getResources().getString(R.string.key_total_longTerm_liabilities))) {
								stockFinancial
										.setTotalLongTermLiabilities(Double
												.valueOf(valueString));
							} else if (keyString.equals(mContext.getResources().getString(R.string.key_main_business_income))) {
								stockFinancial.setMainBusinessIncome(Double
										.valueOf(valueString));
							} else if (keyString.equals(mContext.getResources().getString(R.string.key_financial_expenses))) {
								stockFinancial.setFinancialExpenses(Double
										.valueOf(valueString));
							} else if (keyString.equals(mContext.getResources().getString(R.string.key_net_profit))) {
								stockFinancial.setNetProfit(Double
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
							}
						}
					}
				}
			}

			if (bulkInsert) {
				if (contentValuesList.size() > 0) {
					ContentValues[] contentValuesArray = new ContentValues[contentValuesList
							.size()];
					contentValuesArray = (ContentValues[]) contentValuesList
							.toArray(contentValuesArray);
					mStockDatabaseManager
							.bulkInsertStockFinancial(contentValuesArray);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(TAG, "handleResponseStockFinancial:" + stock.getName() + " "
				+ stopWatch.getInterval() + "s");
	}

	@Override
	void handleResponseShareBonus(Stock stock, ShareBonus shareBonus,
			String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		boolean bulkInsert = false;
		String dateString = "";
		String dividendString = "";
		String rDateString = "";
		List<ContentValues> contentValuesList = new ArrayList<ContentValues>();

		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d(TAG, "handleResponseShareBonus return " + " stock = " + stock
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
				Log.d(TAG, "handleResponseShareBonus return " + " doc = " + doc);
				return;
			}

			Elements tableElements = doc.select("table#sharebonus_1");
			if (tableElements == null) {
				Log.d(TAG, "handleResponseShareBonus return "
						+ " tableElements = " + tableElements);
				return;
			}

			Elements tbodyElements = tableElements.select("tbody");
			if (tbodyElements == null) {
				Log.d(TAG, "handleResponseShareBonus return "
						+ " tbodyElements = " + tbodyElements);
				return;
			}

			for (Element tbodyElement : tbodyElements) {
				if (tbodyElement == null) {
					Log.d(TAG, "handleResponseShareBonus return "
							+ " tbodyElement = " + tbodyElement);
					return;
				}

				Elements trElements = tbodyElement.select("tr");
				if (trElements == null) {
					Log.d(TAG, "handleResponseShareBonus return "
							+ " trElements = " + trElements);
					return;
				}

				for (Element trElement : trElements) {
					if (trElement == null) {
						Log.d(TAG, "handleResponseShareBonus continue "
								+ " trElement = " + trElement);
						continue;
					}

					Elements tdElements = trElement.select("td");
					if (tdElements == null) {
						Log.d(TAG, "handleResponseShareBonus continue "
								+ " tdElements = " + tdElements);
						continue;
					}

					if (tdElements.size() < 9) {
						Log.d(TAG, "handleResponseShareBonus continue "
								+ " tdElements.size() = " + tdElements.size());
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
					contentValuesArray = (ContentValues[]) contentValuesList
							.toArray(contentValuesArray);
					mStockDatabaseManager
							.bulkInsertShareBonus(contentValuesArray);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(TAG, "handleResponseShareBonus:" + stock.getName() + " "
				+ stopWatch.getInterval() + "s");
	}

	@Override
	void handleResponseTotalShare(Stock stock, TotalShare totalShare,
			String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		boolean bulkInsert = false;
		String dateString = "";
		String totalShareString = "";
		List<ContentValues> contentValuesList = new ArrayList<ContentValues>();

		if ((stock == null) || TextUtils.isEmpty(response)) {
			Log.d(TAG, "handleResponseTotalShare return " + " stock = " + stock
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
				Log.d(TAG, "handleResponseTotalShare return " + " doc = " + doc);
				return;
			}

			Elements tableElements = doc.select("table[id^=historyTable]");
			if (tableElements == null) {
				Log.d(TAG, "handleResponseTotalShare return "
						+ " tableElements = " + tableElements);
				return;
			}

			Elements tbodyElements = tableElements.select("tbody");
			if (tbodyElements == null) {
				Log.d(TAG, "handleResponseTotalShare return "
						+ " tbodyElements = " + tbodyElements);
				return;
			}

			for (Element tbodyElement : tbodyElements) {
				if (tbodyElement == null) {
					Log.d(TAG, "handleResponseTotalShare return "
							+ " tbodyElement = " + tbodyElement);
					return;
				}

				Elements trElements = tbodyElement.select("tr");
				if (trElements == null) {
					Log.d(TAG, "handleResponseTotalShare return "
							+ " trElements = " + trElements);
					return;
				}

				for (Element trElement : trElements) {
					if (trElement == null) {
						Log.d(TAG, "handleResponseTotalShare continue "
								+ " trElement = " + trElement);
						continue;
					}

					Elements tdElements = trElement.select("td");
					if (tdElements == null) {
						Log.d(TAG, "handleResponseTotalShare continue "
								+ " tdElements = " + tdElements);
						continue;
					}

					if (tdElements.size() < 2) {
						Log.d(TAG, "handleResponseTotalShare continue "
								+ " tdElements.size() = " + tdElements.size());
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
								* Constants.DOUBLE_CONSTANT_WAN);
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
					contentValuesArray = (ContentValues[]) contentValuesList
							.toArray(contentValuesArray);
					mStockDatabaseManager
							.bulkInsertTotalShare(contentValuesArray);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(TAG, "handleResponseTotalShare:" + stock.getName() + " "
				+ stopWatch.getInterval() + "s");
	}

	@Override
	void handleResponseIPO(String response) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		IPO ipo = new IPO();

		boolean bulkInsert = false;

		Calendar today = Utility.getCalendar(
				Utility.getCurrentDateString(), Utility.CALENDAR_DATE_FORMAT);

		String codeString = "";
		String nameString = "";
		String dateString = "";
		String timeToMarketString = "";
		String priceString = "";
		String peString = "";

		List<ContentValues> contentValuesList = new ArrayList<ContentValues>();

		if (TextUtils.isEmpty(response)) {
			Log.d(TAG, "handleResponseIPO return " + " response = " + response);
			return;
		}

		if (TextUtils.isEmpty(ipo.getCreated())) {
			mStockDatabaseManager.deleteIPO();
			bulkInsert = true;
		}

		try {
			// String responseString = new
			// String(response.getBytes("ISO-8859-1"),
			// "GB2312");

			Document doc = Jsoup.parse(response);
			if (doc == null) {
				Log.d(TAG, "handleResponseIPO return " + " doc = " + doc);
				return;
			}

			Elements tableElements = doc.select("table#NewStockTable");
			if (tableElements == null) {
				Log.d(TAG, "handleResponseIPO return " + " tableElements = "
						+ tableElements);
				return;
			}

			Elements tbodyElements = tableElements.select("tbody");
			if (tbodyElements == null) {
				Log.d(TAG, "handleResponseIPO return " + " tbodyElements = "
						+ tbodyElements);
				return;
			}

			for (Element tbodyElement : tbodyElements) {
				if (tbodyElement == null) {
					Log.d(TAG, "handleResponseIPO return " + " tbodyElement = "
							+ tbodyElement);
					return;
				}

				Elements trElements = tbodyElement.select("tr");
				if (trElements == null) {
					Log.d(TAG, "handleResponseIPO return " + " trElements = "
							+ trElements);
					return;
				}

				for (Element trElement : trElements) {
					if (trElement == null) {
						Log.d(TAG, "handleResponseIPO continue "
								+ " trElement = " + trElement);
						continue;
					}

					Elements tdElements = trElement.select("td");
					if (tdElements == null) {
						Log.d(TAG, "handleResponseIPO continue "
								+ " tdElements = " + tdElements);
						continue;
					}

					if (tdElements.size() < 9) {
						Log.d(TAG, "handleResponseIPO continue "
								+ " tdElements.size() = " + tdElements.size());
						continue;
					}

					codeString = tdElements.get(1).text();
					if (TextUtils.isEmpty(codeString)
							|| codeString.contains(mContext.getResources().getString(R.string.securities_code))) {
						continue;
					}

					nameString = tdElements.get(2).text();
					if (TextUtils.isEmpty(nameString)
							|| nameString.contains(mContext.getResources().getString(R.string.securities_abbreviation))) {
						continue;
					}

					dateString = tdElements.get(3).text();
					if (TextUtils.isEmpty(dateString)
							|| dateString.contains("--")) {
						continue;
					}

					if (!TextUtils.isEmpty(dateString)) {
						Calendar calendar = Utility.getCalendar(
								dateString, Utility.CALENDAR_DATE_FORMAT);
						if (!calendar.before(today)) {
							nameString = nameString
									+ StockData.ACTION_STAR;
						}
					}

					timeToMarketString = tdElements.get(4).text();
					if (!TextUtils.isEmpty(timeToMarketString)) {
						Calendar calendar = Utility.getCalendar(
								timeToMarketString,
								Utility.CALENDAR_DATE_FORMAT);
						if (calendar.before(today)) {
							break;
						}
					}

					priceString = tdElements.get(7).text();
					if (TextUtils.isEmpty(priceString)
							|| !Utility.isNumber(priceString)) {
						priceString = "0";
					}

					peString = tdElements.get(8).text();
					if (TextUtils.isEmpty(peString)) {
						peString = "0";
					}

					ipo.setCode(codeString);
					ipo.setName(nameString);
					ipo.setDate(dateString);
					ipo.setTimeToMarket(timeToMarketString);
					ipo.setPrice(Double.valueOf(priceString));
					ipo.setPE(Double.valueOf(peString));

					if (bulkInsert) {
						ipo.setCreated(Utility.getCurrentDateTimeString());
						ipo.setModified(Utility.getCurrentDateTimeString());
						contentValuesList.add(ipo.getContentValues());
					} else {
						if (!mStockDatabaseManager.isIPOExist(ipo)) {
							ipo.setCreated(Utility.getCurrentDateTimeString());
							ipo.setModified(Utility.getCurrentDateTimeString());
							mStockDatabaseManager.insertIPO(ipo);
						} else {
							ipo.setModified(Utility.getCurrentDateTimeString());
							mStockDatabaseManager.updateIPO(ipo,
									ipo.getContentValues());
						}
					}
				}
			}

			if (bulkInsert) {
				if (contentValuesList.size() > 0) {
					ContentValues[] contentValuesArray = new ContentValues[contentValuesList
							.size()];
					contentValuesArray = (ContentValues[]) contentValuesList
							.toArray(contentValuesArray);
					mStockDatabaseManager.bulkInsertIPO(contentValuesArray);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(TAG, "handleResponseIPO:" + contentValuesList.size() + " "
				+ stopWatch.getInterval() + "s");
	}
}
