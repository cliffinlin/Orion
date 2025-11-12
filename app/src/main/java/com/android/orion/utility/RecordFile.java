package com.android.orion.utility;

import android.os.Environment;

import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.constant.Constant;

public class RecordFile {
	static StringBuffer stringBuffer = new StringBuffer();

	public static void writeDealFile(Stock stock, StockDeal deal, String action) {
		if (stock == null || deal == null) {
			return;
		}

		stringBuffer.setLength(0);
		stringBuffer.append(Utility.getCurrentDateTimeString() + " ");
		stringBuffer.append(stock.getName() + " "
				+ deal.getPrice() + " "
				+ deal.getNet() + " "
				+ deal.getBuy() + " "
				+ deal.getSell() + " "
				+ deal.getVolume() + " "
				+ deal.getValue() + " "
				+ deal.getBonus() + " "
				+ deal.getYield() + " "
				+ deal.getFee() + " "
				+ deal.getProfit() + " "
				+ action + " "
				+ deal.getCreated() + " "
				+ deal.getModified() + " ");
		stringBuffer.append("\r\n");

		try {
			String fileName = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Android/" + Constant.DEAL + Constant.FILE_EXT_TEXT;
			Utility.writeFile(fileName, stringBuffer.toString(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeNotificationFile(String content) {
		stringBuffer.setLength(0);
		stringBuffer.append(content);
		stringBuffer.append(" " + Utility.getCurrentDateTimeString() + "\r\n");

		try {
			String fileName = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Android/" + Constant.NOTIFICATION + Constant.FILE_EXT_TEXT;
			Utility.writeFile(fileName, stringBuffer.toString(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
