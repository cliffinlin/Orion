package com.android.orion.utility;

import android.os.Environment;

import com.android.orion.Constants;
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
                + deal.getDeal() + " "
                + deal.getVolume() + " "
                + deal.getValue() + " "
                + deal.getProfit() + " "
                + deal.getFee() + " "
                + deal.getBonus() + " "
                + deal.getYield() + " "
                + action + " "
                + deal.getCreated() + " "
                + deal.getModified() + " ");
        logString.append("\n");

        try {
            fileName = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Android/" + Constants.DEAL + Constants.DEAL_FILE_EXT;
            Utility.writeFile(fileName, logString.toString(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeNotificationFile(Stock stock, String action) {
        String fileName;
        StringBuilder logString = new StringBuilder();

        logString.append(stock.getName() + " " + stock.getPrice() + " "
                + stock.getNet() + " ");
        logString.append(" " + action);
        logString.append(stock.getModified() + "\n");

        try {
            fileName = Environment.getExternalStorageDirectory().getCanonicalPath() + "/Android/" + Constants.NOTIFICATION + Constants.NOTIFICATION_FILE_EXT;
            Utility.writeFile(fileName, logString.toString(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}