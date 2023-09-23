package com.android.orion.utility;

import android.os.Environment;

import com.android.orion.setting.Constant;
import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;

public class RecordFile {

    public static void writeDealFile(Stock stock, StockDeal deal, String action) {
        String fileName;
        StringBuilder logString = new StringBuilder();

        logString.append(Utility.getCurrentDateTimeString() + " ");

        logString.append(stock.getName() + " "
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
        logString.append("\r\n");

        try {
            fileName = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Android/" + Constant.DEAL + Constant.FILE_EXT_TEXT;
            Utility.writeFile(fileName, logString.toString(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeNotificationFile(String content) {
        String fileName;
        StringBuilder logString = new StringBuilder();

        logString.append(content);
        logString.append(" " + Utility.getCurrentDateTimeString() + "\r\n");

        try {
            fileName = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Android/" + Constant.NOTIFICATION + Constant.FILE_EXT_TEXT;
            Utility.writeFile(fileName, logString.toString(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
