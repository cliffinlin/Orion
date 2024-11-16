package com.android.orion.setting;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.utility.Preferences;

public class StockFilter {
	Context mContext;

	boolean mEnabled = true;
	boolean mFavorite = true;

	String mHold = "";
	String mRoi = "";
	String mRate = "";
	String mRoe = "";
	String mPe = "";
	String mPb = "";
	String mDividend = "";
	String mYield = "";
	String mDividendRatio = "";

	public StockFilter(Context context) {
		mContext = context;
	}

	public boolean getEnabled() {
		return mEnabled;
	}

	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	public boolean getFavorite() {
		return mFavorite;
	}

	public void setFavorite(boolean favorite) {
		mFavorite = favorite;
	}

	public String getHold() {
		return mHold;
	}

	public void setHold(String hold) {
		mHold = hold;
	}

	public String getRoi() {
		return mRoi;
	}

	public void setRoi(String roi) {
		mRoi = roi;
	}

	public String getRate() {
		return mRate;
	}

	public void setRate(String rate) {
		mRate = rate;
	}

	public String getRoe() {
		return mRoe;
	}

	public void setRoe(String roe) {
		mRoe = roe;
	}

	public String getPe() {
		return mPe;
	}

	public void setPe(String pe) {
		mPe = pe;
	}

	public String getPb() {
		return mPb;
	}

	public void setPb(String pb) {
		mPb = pb;
	}

	public String getDividend() {
		return mDividend;
	}

	public void setDividend(String dividend) {
		mDividend = dividend;
	}

	public String getYield() {
		return mYield;
	}

	public void setYield(String yield) {
		mYield = yield;
	}

	public String getDividendRatio() {
		return mDividendRatio;
	}

	public void setDividendRatio(String dividendRatio) {
		mDividendRatio = dividendRatio;
	}

	boolean containOperation(String valueString) {
		boolean result = false;

		if (!TextUtils.isEmpty(valueString)) {
			if (valueString.contains("<") || valueString.contains(">")
					|| valueString.contains("=")) {
				result = true;
			}
		}

		return result;
	}

	void validate() {
		if (!containOperation(mHold)) {
			mHold = "";
		}

		if (!containOperation(mRoi)) {
			mRoi = "";
		}

		if (!containOperation(mRate)) {
			mRate = "";
		}

		if (!containOperation(mRoe)) {
			mRoe = "";
		}

		if (!containOperation(mPe)) {
			mPe = "";
		}

		if (!containOperation(mPb)) {
			mPb = "";
		}

		if (!containOperation(mDividend)) {
			mDividend = "";
		}

		if (!containOperation(mYield)) {
			mYield = "";
		}

		if (!containOperation(mDividendRatio)) {
			mDividendRatio = "";
		}
	}

	public void read() {
		mEnabled = Preferences.getBoolean(Setting.SETTING_STOCK_FILTER_ENABLED, true);
		mFavorite = Preferences.getBoolean(Setting.SETTING_STOCK_FILTER_FAVORITE, true);
		mHold = Preferences.getString(Setting.SETTING_STOCK_FILTER_HOLD,
				"");
		mRoi = Preferences.getString(Setting.SETTING_STOCK_FILTER_ROI,
				"");
		mRate = Preferences.getString(Setting.SETTING_STOCK_FILTER_RATE,
				"");
		mRoe = Preferences.getString(Setting.SETTING_STOCK_FILTER_ROE,
				"");
		mPe = Preferences.getString(Setting.SETTING_STOCK_FILTER_PE, "");
		mPb = Preferences.getString(Setting.SETTING_STOCK_FILTER_PB, "");
		mDividend = Preferences.getString(
				Setting.SETTING_STOCK_FILTER_DIVIDEND, "");
		mYield = Preferences.getString(
				Setting.SETTING_STOCK_FILTER_YIELD, "");
		mDividendRatio = Preferences.getString(
				Setting.SETTING_STOCK_FILTER_DIVIDEND_RATIO, "");

		validate();
	}

	public void write() {
		Preferences.putBoolean(Setting.SETTING_STOCK_FILTER_ENABLED,
				mEnabled);
		Preferences.putBoolean(Setting.SETTING_STOCK_FILTER_FAVORITE,
				mFavorite);

		validate();

		Preferences.putString(Setting.SETTING_STOCK_FILTER_HOLD, mHold);
		Preferences.putString(Setting.SETTING_STOCK_FILTER_ROI, mRoi);
		Preferences.putString(Setting.SETTING_STOCK_FILTER_RATE, mRate);
		Preferences.putString(Setting.SETTING_STOCK_FILTER_ROE, mRoe);
		Preferences.putString(Setting.SETTING_STOCK_FILTER_PE, mPe);
		Preferences.putString(Setting.SETTING_STOCK_FILTER_PB, mPb);
		Preferences.putString(Setting.SETTING_STOCK_FILTER_DIVIDEND,
				mDividend);
		Preferences
				.putString(Setting.SETTING_STOCK_FILTER_YIELD, mYield);
		Preferences.putString(
				Setting.SETTING_STOCK_FILTER_DIVIDEND_RATIO, mDividendRatio);
	}

	public void get(Bundle bundle) {
		if (bundle == null) {
			return;
		}

		mEnabled = bundle.getBoolean(Setting.SETTING_STOCK_FILTER_ENABLED, true);
		mFavorite = bundle.getBoolean(Setting.SETTING_STOCK_FILTER_FAVORITE, true);
		mHold = bundle.getString(Setting.SETTING_STOCK_FILTER_HOLD);
		mRoi = bundle.getString(Setting.SETTING_STOCK_FILTER_ROI);
		mRate = bundle.getString(Setting.SETTING_STOCK_FILTER_RATE);
		mRoe = bundle.getString(Setting.SETTING_STOCK_FILTER_ROE);
		mPe = bundle.getString(Setting.SETTING_STOCK_FILTER_PE);
		mPb = bundle.getString(Setting.SETTING_STOCK_FILTER_PB);
		mDividend = bundle.getString(Setting.SETTING_STOCK_FILTER_DIVIDEND);
		mYield = bundle.getString(Setting.SETTING_STOCK_FILTER_YIELD);
		mDividendRatio = bundle
				.getString(Setting.SETTING_STOCK_FILTER_DIVIDEND_RATIO);
	}

	public void put(Bundle bundle) {
		if (bundle == null) {
			return;
		}

		bundle.putBoolean(Setting.SETTING_STOCK_FILTER_ENABLED, mEnabled);
		bundle.putBoolean(Setting.SETTING_STOCK_FILTER_FAVORITE, mFavorite);
		bundle.putString(Setting.SETTING_STOCK_FILTER_HOLD, mHold);
		bundle.putString(Setting.SETTING_STOCK_FILTER_ROI, mRoi);
		bundle.putString(Setting.SETTING_STOCK_FILTER_RATE, mRate);
		bundle.putString(Setting.SETTING_STOCK_FILTER_ROE, mRoe);
		bundle.putString(Setting.SETTING_STOCK_FILTER_PE, mPe);
		bundle.putString(Setting.SETTING_STOCK_FILTER_PB, mPb);
		bundle.putString(Setting.SETTING_STOCK_FILTER_DIVIDEND, mDividend);
		bundle.putString(Setting.SETTING_STOCK_FILTER_YIELD, mYield);
		bundle.putString(Setting.SETTING_STOCK_FILTER_DIVIDEND_RATIO,
				mDividendRatio);
	}

	public String getSelection() {
		String selection = "";

		if (mEnabled) {
			if (mFavorite) {
				selection += DatabaseContract.COLUMN_FLAG + " >= "
						+ Stock.FLAG_FAVORITE;
			} else {
				selection += " 1 ";
			}

			if (!TextUtils.isEmpty(mHold)) {
				selection += " AND " + DatabaseContract.COLUMN_HOLD + mHold;
			}

			if (!TextUtils.isEmpty(mRoi)) {
				selection += " AND " + DatabaseContract.COLUMN_ROI + mRoi;
			}

			if (!TextUtils.isEmpty(mRate)) {
				selection += " AND " + DatabaseContract.COLUMN_RATE + mRate;
			}

			if (!TextUtils.isEmpty(mRoe)) {
				selection += " AND " + DatabaseContract.COLUMN_ROE + mRoe;
			}

			if (!TextUtils.isEmpty(mPe)) {
				selection += " AND " + DatabaseContract.COLUMN_PE + mPe;
			}

			if (!TextUtils.isEmpty(mPb)) {
				selection += " AND " + DatabaseContract.COLUMN_PB + mPb;
			}

			if (!TextUtils.isEmpty(mDividend)) {
				selection += " AND " + DatabaseContract.COLUMN_DIVIDEND
						+ mDividend;
			}

			if (!TextUtils.isEmpty(mYield)) {
				selection += " AND " + DatabaseContract.COLUMN_YIELD + mYield;
			}

			if (!TextUtils.isEmpty(mDividendRatio)) {
				selection += " AND " + DatabaseContract.COLUMN_DIVIDEND_RATIO
						+ mDividendRatio;
			}
		}

		return selection;
	}
}
