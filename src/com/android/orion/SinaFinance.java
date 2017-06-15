package com.android.orion;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.pinyin.Pinyin;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Utility;

public class SinaFinance extends StockDataProvider {
	static final String TAG = Constants.TAG + " "
			+ SinaFinance.class.getSimpleName();

	private static final String SINA_FINANCE_URL_HQ_NODE_DATA = "http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?";
	private static final String SINA_FINANCE_URL_HQ_KLINE_DATA = "http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?";
	private static final String SINA_FINANCE_URL_HQ_JS_LIST = "http://hq.sinajs.cn/list=";
	private static final String SINA_FINANCE_URL_HQ_JS_LIST_SIMPLE = "http://hq.sinajs.cn/list=s_";

	private static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN5 = 242;
	private static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN15 = 192;
	private static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN30 = 192;
	private static final int DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN60 = 192;

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
	}

	@Override
	int getAvailableHistoryLength(String period) {
		if (period.equals(Constants.PERIOD_MIN1)) {
			return Constants.DOWNLOAD_HISTORY_LENGTH_NONE;
		} else if (period.equals(Constants.PERIOD_MIN5)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN5;
		} else if (period.equals(Constants.PERIOD_MIN15)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN15;
		} else if (period.equals(Constants.PERIOD_MIN30)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN30;
		} else if (period.equals(Constants.PERIOD_MIN60)) {
			return DOWNLOAD_HISTORY_LENGTH_PERIOD_MIN60;
		} else if (period.equals(Constants.PERIOD_DAY)) {
			return Constants.DOWNLOAD_HISTORY_LENGTH_UNLIMITED;
		} else if (period.equals(Constants.PERIOD_WEEK)) {
			return Constants.DOWNLOAD_HISTORY_LENGTH_UNLIMITED;
		} else if (period.equals(Constants.PERIOD_MONTH)) {
			return Constants.DOWNLOAD_HISTORY_LENGTH_UNLIMITED;
		} else if (period.equals(Constants.PERIOD_QUARTER)) {
			return Constants.DOWNLOAD_HISTORY_LENGTH_NONE;
		} else if (period.equals(Constants.PERIOD_YEAR)) {
			return Constants.DOWNLOAD_HISTORY_LENGTH_NONE;
		} else {
		}

		return 0;
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
				stock.setPinyin(Pinyin.toPinyin(mContext, stock.getName()));
				stock.setPinyinFixed(Constants.STOCK_FLAG_NONE);
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
					stock.setVolume(stockInfo[4]);
				}

				if (!TextUtils.isEmpty(stockInfo[5])) {
					stock.setValue(stockInfo[5]);
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
		ContentValues[] contentValues = null;
		String symbol = "";
		String se = "";
		Stock stock = null;
		JSONArray jsonArray = null;

		if (TextUtils.isEmpty(response)) {
			Log.d(TAG, "handleResponseStockHSA return response = " + response);
			return;
		}

		if (isStockHSAEmpty()) {
			bulkInsert = true;
			Log.d(TAG, "handleResponseStockHSA bulkInsert = " + bulkInsert);
		}

		try {
			jsonArray = JSON.parseArray(response);
			if (jsonArray == null || jsonArray.size() == 0) {
				Log.d(TAG, "handleResponseStockHSA return jsonArray = "
						+ jsonArray + " jsonArray.size() = " + jsonArray.size());
				return;
			}

			if (bulkInsert) {
				if (contentValues == null) {
					contentValues = new ContentValues[jsonArray.size()];
				}
			}

			stock = Stock.obtain();
			if (stock == null) {
				Log.d(TAG, "handleResponseStockHSA return stock = " + stock);
				return;
			}
			stock.init();

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

					stock.setClasses(Constants.STOCK_FLAG_CLASS_HSA);

					nameChanged = false;
					if (!stock.getName().equals(jsonObject.getString("name"))) {
						nameChanged = true;
						stock.setName(jsonObject.getString("name"));
						stock.setPinyin(Pinyin.toPinyin(mContext,
								stock.getName()));
						stock.setPinyinFixed(Constants.STOCK_FLAG_NONE);
					}

					stock.setPrice(jsonObject.getDouble("trade"));
					stock.setChange(jsonObject.getDouble("pricechange"));
					stock.setNet(jsonObject.getDouble("changepercent"));
					stock.setVolume(jsonObject.getString("volume"));
					stock.setValue(jsonObject.getString("amount"));

					if (bulkInsert) {
						stock.setCreated(Utility.getCurrentDateTimeString());
						contentValues[i] = stock.getContentValues();
					} else {
						if (!mStockDatabaseManager.isStockExist(stock)) {
							stock.setCreated(Utility.getCurrentDateTimeString());
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
				mStockDatabaseManager.bulkInsertStock(contentValues);
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
		ContentValues[] contentValues = null;
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

		if (TextUtils.isEmpty(stockData.getCreated())) {
			bulkInsert = true;
		}

		try {
			jsonArray = JSON.parseArray(response);
			if ((jsonArray == null) || (jsonArray.size() == 0)) {
				Log.d(TAG, "handleResponseStockDataHistory return jsonArray = "
						+ jsonArray + " jsonArray.size() = " + jsonArray.size());
				return;
			}

			defaultValue = getDownloadHistoryLengthDefault(stockData
					.getPeriod());
			if (defaultValue == jsonArray.size()) {
				mStockDatabaseManager.deleteStockData(stockData.getStockId(),
						stockData.getPeriod());
				bulkInsert = true;
			}

			if (bulkInsert) {
				if (contentValues == null) {
					contentValues = new ContentValues[jsonArray.size()];
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
						contentValues[i] = stockData.getContentValues();
					} else {
						if (!mStockDatabaseManager.isStockDataExist(stockData)) {
							stockData.setCreated(Utility
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
				mStockDatabaseManager.bulkInsertStockData(contentValues);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		stopWatch.stop();
		Log.d(TAG, "handleResponseStockDataHistory:" + stock.getName() + " "
				+ stockData.getPeriod() + " " + "size:" + jsonArray.size()
				+ " " + stopWatch.getInterval() + "s");
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

			if (keyValue[0] == null) {
				Log.d(TAG, "handleResponseStockDataRealTime return "
						+ " keyValue[0] = " + keyValue[0]);
				return;
			}

			codeInfo = keyValue[0].trim().split("_");

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
}
