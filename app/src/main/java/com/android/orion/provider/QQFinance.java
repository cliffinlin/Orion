package com.android.orion.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.interfaces.IStockDataProvider;
import com.android.orion.constant.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Market;
import com.android.orion.utility.StopWatch;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

import okhttp3.Request;
import okhttp3.Response;

public class QQFinance extends StockDataProvider {

	public static final String PROVIDER_NAME = "QQFinance";

	// 实时行情（单只/多只），返回 GBK 编码文本，字段以 ~ 分隔
	public static final String QQ_FINANCE_URL_REAL_TIME = "http://qt.gtimg.cn/q=";
	// 简要信息（s_ 前缀）
	public static final String QQ_FINANCE_URL_SIMPLE = "http://qt.gtimg.cn/q=s_";
	// 历史 K 线（日/周/月），param=代码,周期,开始日期,结束日期,数量,复权类型
	// 复权类型留空 = 不复权（qfq = 前复权，hfq = 后复权）
	public static final String QQ_FINANCE_URL_KLINE = "https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?";
	// 分钟 K 线，param=代码,周期,开始日期,结束日期,数量（该接口不涉及复权）
	public static final String QQ_FINANCE_URL_MKLINE = "https://proxy.finance.qq.com/ifzqgtimg/appstock/app/kline/mkline?";

	ArrayList<ContentValues> mContentValuesList = new ArrayList<>();

	private QQFinance() {
		super();
	}

	public static IStockDataProvider getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private static class InstanceHolder {
		static final QQFinance INSTANCE = new QQFinance();
	}

	// ==================== URL 构造 ====================

	public String getStockRealTimeURLString(Stock stock) {
		if (stock == null) return "";
		return QQ_FINANCE_URL_REAL_TIME + stock.getSE() + stock.getCode();
	}

	public String getStockInformationURLString(Stock stock) {
		if (stock == null) return "";
		return QQ_FINANCE_URL_SIMPLE + stock.getSE() + stock.getCode();
	}

	public String getStockDataRealTimeURLString(Stock stock) {
		if (stock == null) return "";
		return QQ_FINANCE_URL_REAL_TIME + stock.getSE() + stock.getCode();
	}

	public String getStockDataHistoryURLString(Stock stock, StockData stockData, int len) {
		if (stock == null || stockData == null) return "";

		String symbol = stock.getSE() + stock.getCode();
		String period = stockData.getPeriod();
		String periodType = convertPeriodToQQType(period);
		if (TextUtils.isEmpty(periodType)) return "";

		if (Period.isMinutePeriod(period)) {
			// 分钟线：param=代码,周期,开始日期,结束日期,数量
			// 注意：mkline 只有 5 个字段，中间两个日期留空，即 ",," 后直接跟数量
			String param = symbol + "," + periodType + ",," + len;
			return QQ_FINANCE_URL_MKLINE + "param=" + param;
		}

		// 日/周/月线：param=代码,周期,开始日期,结束日期,数量,复权类型
		// 复权类型留空 = 不复权
		String param = symbol + "," + periodType + ",,," + len + ",";
		return QQ_FINANCE_URL_KLINE + "param=" + param;
	}

	private String convertPeriodToQQType(String period) {
		if (TextUtils.equals(period, Period.DAY)) return "day";
		if (TextUtils.equals(period, Period.WEEK)) return "week";
		if (TextUtils.equals(period, Period.MONTH)) return "month";
		if (TextUtils.equals(period, Period.MIN5)) return "m5";
		if (TextUtils.equals(period, Period.MIN15)) return "m15";
		if (TextUtils.equals(period, Period.MIN30)) return "m30";
		if (TextUtils.equals(period, Period.MIN60)) return "m60";
		return "";
	}

	// ==================== 下载实现 ====================

	@Override
	public int downloadStockRealTime(Stock stock) {
		if (stock == null) return RESULT_NONE;

		String urlString = getStockRealTimeURLString(stock);
		Log.d(urlString);

		Request request = new Request.Builder().url(urlString).build();
		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if (response != null && response.body() != null) {
				String resultString = response.body().string();
				handleResponseStockRealTime(stock, resultString);
				Thread.sleep(Config.downloadSleep);
				return RESULT_SUCCESS;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return RESULT_FAILED;
		}
		return RESULT_NONE;
	}

	@Override
	public int downloadStockInformation(Stock stock) {
		if (stock == null) return RESULT_NONE;

		String urlString = getStockInformationURLString(stock);
		Log.d(urlString);

		Request request = new Request.Builder().url(urlString).build();
		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if (response != null && response.body() != null) {
				String resultString = response.body().string();
				handleResponseStockInformation(stock, resultString);
				Thread.sleep(Config.downloadSleep);
				return RESULT_SUCCESS;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return RESULT_FAILED;
		}
		return RESULT_NONE;
	}

	@Override
	public int downloadStockDataHistory(Stock stock) {
		if (stock == null) return RESULT_NONE;

		int result = RESULT_NONE;
		for (String period : Period.PERIODS) {
			if (Setting.getPeriod(period)) {
				result = downloadStockDataHistory(stock, period);
			}
		}
		return result;
	}

	private int downloadStockDataHistory(Stock stock, String period) {
		StockData stockData = new StockData(period);
		stockData.setSE(stock.getSE());
		stockData.setCode(stock.getCode());
		mStockDatabaseManager.getStockData(stockData);
		stockData.setName(stock.getName());

		int len = getDownloadStockDataLength(stockData);
		if (len <= 0) return RESULT_NONE;

		String urlString = getStockDataHistoryURLString(stock, stockData, len);
		if (TextUtils.isEmpty(urlString)) return RESULT_NONE;

		Log.d(urlString);
		Request request = new Request.Builder().url(urlString).build();

		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if (response != null && response.body() != null) {
				String resultString = response.body().string();
				handleResponseStockDataHistory(stock, stockData, resultString);
				Thread.sleep(Config.downloadSleep);
				return RESULT_SUCCESS;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return RESULT_FAILED;
		}
		return RESULT_NONE;
	}

	public void handleResponseStockDataHistory(Stock stock, StockData stockData, String response) {
		StopWatch.start();
		boolean bulkInsert = false;
		int defaultValue = 0;
		JSONObject root = null;
		JSONObject data = null;
		JSONObject stockNode = null;
		JSONArray klineArray = null;
		String periodType = "";
		String timeStr = "";
		String dateStr = "";
		String dateTimeStr = "";
		String[] dateTime = null;
		ArrayMap<String, StockData> stockDataMap = new ArrayMap<>();

		mContentValuesList.clear();

		if (stock == null || stockData == null) {
			return;
		}

		if (TextUtils.isEmpty(response)) {
			Log.d("return, stock = " + stock + " stockData = " + stockData + " response = " + response);
			return;
		}

		try {
			root = JSON.parseObject(response);
			if (root == null) {
				Log.d("return, root = " + root);
				return;
			}

			data = root.getJSONObject("data");
			if (data == null) {
				Log.d("return, data = " + data);
				return;
			}

			String symbol = stock.getSE() + stock.getCode();
			stockNode = data.getJSONObject(symbol);
			if (stockNode == null) {
				stockNode = data.getJSONObject(symbol.toLowerCase());
			}
			if (stockNode == null) {
				Log.d("return, stockNode = " + stockNode);
				return;
			}

			periodType = convertPeriodToQQType(stockData.getPeriod());
			if (TextUtils.isEmpty(periodType)) {
				return;
			}

			klineArray = stockNode.getJSONArray(periodType);
			if (klineArray == null) {
				Log.d("return, klineArray = " + klineArray);
				return;
			}

			if (klineArray.size() == 0) {
				Log.d("return, klineArray.size() = " + klineArray.size());
				return;
			}

			defaultValue = getAvailableHistoryLength(stockData.getPeriod());
			if (TextUtils.isEmpty(stockData.getCreated()) || (defaultValue == klineArray.size())) {
				mStockDatabaseManager.deleteStockData(stockData.getSE(), stockData.getCode(), stockData.getPeriod());
				bulkInsert = true;
			}

			if (bulkInsert) {
				loadTDXDatabase(stock, stockData, mContentValuesList, stockDataMap);
			}

			boolean isMinute = Period.isMinutePeriod(stockData.getPeriod());

			for (int i = 0; i < klineArray.size(); i++) {
				JSONArray item = klineArray.getJSONArray(i);
				if (item == null || item.size() < 6) {
					continue;
				}

				if (isMinute) {
					timeStr = item.getString(0);
					if (TextUtils.isEmpty(timeStr) || timeStr.length() < 12) {
						continue;
					}
					stockData.setDate(timeStr.substring(0, 4) + Symbol.MINUS
							+ timeStr.substring(4, 6) + Symbol.MINUS
							+ timeStr.substring(6, 8));
					stockData.setTime(timeStr.substring(8, 10) + ":"
							+ timeStr.substring(10, 12) + ":00");
				} else {
					dateTimeStr = item.getString(0);
					if (!TextUtils.isEmpty(dateTimeStr)) {
						dateTime = dateTimeStr.trim().split(" ");
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
				}

				stockData.getMacd().init();
				stockData.getCandle().init();
				stockData.getCandle().setOpen(item.getDouble(1));
				stockData.getCandle().setClose(item.getDouble(2));
				stockData.getCandle().setHigh(item.getDouble(3));
				stockData.getCandle().setLow(item.getDouble(4));

				if (i == klineArray.size() - 1) {
					mergeStockDataDay(stockData);
				}

				if (bulkInsert) {
					stockData.setCreated(Utility.getCurrentDateTimeString());
					stockData.setModified(Utility.getCurrentDateTimeString());
					if (Period.isMinutePeriod(stockData.getPeriod())) {
						if (!stockDataMap.containsKey(stockData.getDateTime())) {
							stockDataMap.put(stockData.getDateTime(), new StockData(stockData));
							mContentValuesList.add(stockData.getContentValues());
						}
					} else {
						mContentValuesList.add(stockData.getContentValues());
					}
				} else {
					if (!mStockDatabaseManager.isStockDataExist(stockData)) {
						stockData.setCreated(Utility.getCurrentDateTimeString());
						stockData.setModified(Utility.getCurrentDateTimeString());
						mStockDatabaseManager.insertStockData(stockData);
					} else {
						stockData.setModified(Utility.getCurrentDateTimeString());
						mStockDatabaseManager.updateStockData(stockData, stockData.getContentValues());
					}
				}
			}

			if (bulkInsert) {
				saveTDXDatabase(stock, stockData.getPeriod(), stockDataMap);

				if (mContentValuesList.size() > 0) {
					fixContentValuesList(stockData, mContentValuesList);

					ContentValues[] contentValuesArray = new ContentValues[mContentValuesList.size()];
					contentValuesArray = mContentValuesList.toArray(contentValuesArray);
					mStockDatabaseManager.bulkInsertStockData(contentValuesArray);
				}
			}

			Setting.setStockDataChanged(stock, true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.toLogString() + Symbol.TAB + stockData.getPeriod() + Symbol.TAB + StopWatch.getIntervalString());
	}

	@Override
	public int downloadStockDataRealTime(Stock stock) {
		if (stock == null) return RESULT_NONE;

		StockData stockData = new StockData(Period.DAY);
		stockData.setSE(stock.getSE());
		stockData.setCode(stock.getCode());
		mStockDatabaseManager.getStockData(stockData);
		stockData.setName(stock.getName());

		String urlString = getStockDataRealTimeURLString(stock);
		Log.d(urlString);

		Request request = new Request.Builder().url(urlString).build();
		try {
			Response response = mOkHttpClient.newCall(request).execute();
			if (response != null && response.body() != null) {
				String resultString = response.body().string();
				handleResponseStockDataRealTime(stock, stockData, resultString);
				Thread.sleep(Config.downloadSleep);
				return RESULT_SUCCESS;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return RESULT_FAILED;
		}
		return RESULT_NONE;
	}

	// 腾讯公开接口暂无与新浪对等的财务/分红/股本历史端点，沿用父类默认返回
	@Override
	public int downloadStockHSA() {
		return RESULT_NONE;
	}

	// ==================== 响应解析 ====================

	/**
	 * 解析实时行情。腾讯返回格式：
	 * v_sh600519="1~贵州茅台~600519~1460.00~1466.21~1466.99~..."
	 * 字段索引：[1]名称 [2]代码 [3]当前价 [4]昨收 [5]今开
	 * [30]时间 [31]涨跌 [32]涨跌% [33]最高 [34]最低
	 * [36]成交量(手) [37]成交额(万元)
	 */
	public void handleResponseStockRealTime(Stock stock, String response) {
		StopWatch.start();
		if (stock == null || TextUtils.isEmpty(response)) return;

		try {
			String content = extractQuotedContent(response);
			if (TextUtils.isEmpty(content)) return;

			String[] fields = content.split("~");
			if (fields.length < 38) {
				Log.d("return, fields.length=" + fields.length);
				return;
			}

			if (!TextUtils.isEmpty(fields[1])) {
				stock.setName(fields[1]);
			}

			double price = Double.parseDouble(fields[3]);
			if (price > 0) {
				stock.setPrice(price);
			}

			stock.setChange(Double.parseDouble(fields[31]));
			stock.setNet(Double.parseDouble(fields[32]));

			if (!TextUtils.isEmpty(fields[36])) {
				stock.setVolume(Long.parseLong(fields[36]));
			}
			if (!TextUtils.isEmpty(fields[37])) {
				// 成交额单位：万元，转换为元
				stock.setValue(Double.parseDouble(fields[37]) * 10000);
			}

			mStockDatabaseManager.updateStock(stock, stock.getContentValuesRealTime());
			Setting.setStockDataChanged(stock, true);

		} catch (Exception e) {
			e.printStackTrace();
		}

		StopWatch.stop();
		Log.d(stock.toLogString() + Symbol.TAB + StopWatch.getIntervalString());
	}

	/**
	 * 解析简要信息（s_ 前缀），主要用于获取名称。
	 */
	public void handleResponseStockInformation(Stock stock, String response) {
		if (stock == null || TextUtils.isEmpty(response)) return;

		try {
			String content = extractQuotedContent(response);
			if (TextUtils.isEmpty(content)) return;

			String[] fields = content.split("~");
			if (fields.length > 1 && !TextUtils.isEmpty(fields[1])) {
				stock.setName(fields[1]);
				mStockDatabaseManager.updateStock(stock, stock.getContentValuesInformation());
				Setting.setStockDataChanged(stock, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析实时 K 线数据（当日）。从实时行情接口字段中取 OHLC。
	 */
	public void handleResponseStockDataRealTime(Stock stock, StockData stockData, String response) {
		if (stock == null || stockData == null || TextUtils.isEmpty(response)) return;

		try {
			String content = extractQuotedContent(response);
			if (TextUtils.isEmpty(content)) return;

			String[] fields = content.split("~");
			if (fields.length < 35) return;

			// [3]当前价 [5]今开 [33]最高 [34]最低 [30]日期 [31]时间
			stockData.getCandle().setOpen(Double.parseDouble(fields[5]));
			stockData.getCandle().setClose(Double.parseDouble(fields[3]));
			stockData.getCandle().setHigh(Double.parseDouble(fields[33]));
			stockData.getCandle().setLow(Double.parseDouble(fields[34]));

			if (fields.length > 30 && !TextUtils.isEmpty(fields[30])) {
				// 腾讯时间字段格式为 yyyyMMddHHmmss，取前 8 位作为日期
				String timeStr = fields[30];
				if (timeStr.length() >= 8) {
					stockData.setDate(timeStr.substring(0, 4) + "-"
							+ timeStr.substring(4, 6) + "-"
							+ timeStr.substring(6, 8));
				}
			}
			if (fields.length > 31 && Period.isMinutePeriod(stockData.getPeriod())) {
				stockData.setTime(fields[31]);
			}

			if (!mStockDatabaseManager.isStockDataExist(stockData)) {
				stockData.setCreated(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.insertStockData(stockData);
			} else {
				stockData.setModified(Utility.getCurrentDateTimeString());
				mStockDatabaseManager.updateStockData(stockData, stockData.getContentValues());
			}

			Setting.setStockDataChanged(stock, true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ==================== 工具方法 ====================

	/**
	 * 从 v_xxx="..." 格式中提取引号内的内容
	 */
	private String extractQuotedContent(String response) {
		if (TextUtils.isEmpty(response)) return "";
		int start = response.indexOf('"');
		int end = response.lastIndexOf('"');
		if (start >= 0 && end > start) {
			return response.substring(start + 1, end);
		}
		return "";
	}

	private int getDownloadStockDataLength(StockData stockData) {
		if (stockData == null) return 0;

		Cursor cursor = null;
		try {
			String period = stockData.getPeriod();
			int defaultValue = getAvailableHistoryLength(period);
			String selection = DatabaseContract.SELECTION_STOCK_PERIOD(
					stockData.getSE(), stockData.getCode(), period);
			cursor = mStockDatabaseManager.queryStockData(selection, null,
					DatabaseContract.ORDER_DATE_TIME_ASC);
			if (cursor == null || cursor.getCount() <= 1) {
				return defaultValue;
			}

			cursor.moveToLast();
			stockData.set(cursor);

			if (Market.isTradingHours()) {
				int scheduleMinutes = Market.getScheduleMinutes();
				if (scheduleMinutes != 0) {
					int result = 1;
					switch (period) {
						case Period.MIN60:
							result += scheduleMinutes / Constant.MIN60;
							break;
						case Period.MIN30:
							result += scheduleMinutes / Constant.MIN30;
							break;
						case Period.MIN15:
							result += scheduleMinutes / Constant.MIN15;
							break;
						case Period.MIN5:
							result += scheduleMinutes / Constant.MIN5;
							break;
						default:
							return defaultValue;
					}
					return result;
				}
			} else {
				return defaultValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
		return 0;
	}

	private int getAvailableHistoryLength(String period) {
		if (TextUtils.equals(period, Period.MONTH)) return Config.DOWNLOAD_HISTORY_LENGTH_MONTH;
		if (TextUtils.equals(period, Period.WEEK)) return Config.DOWNLOAD_HISTORY_LENGTH_WEEK;
		if (TextUtils.equals(period, Period.DAY)) return Config.DOWNLOAD_HISTORY_LENGTH_DAY;
		if (TextUtils.equals(period, Period.MIN60)) return Config.DOWNLOAD_HISTORY_LENGTH_MIN60;
		if (TextUtils.equals(period, Period.MIN30)) return Config.DOWNLOAD_HISTORY_LENGTH_MIN30;
		if (TextUtils.equals(period, Period.MIN15)) return Config.DOWNLOAD_HISTORY_LENGTH_MIN15;
		if (TextUtils.equals(period, Period.MIN5)) return Config.DOWNLOAD_HISTORY_LENGTH_MIN5;
		return 0;
	}
}