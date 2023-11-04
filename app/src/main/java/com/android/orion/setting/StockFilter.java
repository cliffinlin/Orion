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

	public boolean getFavorite() {
		return mFavorite;
	}

	public String getHold() {
		return mHold;
	}

	public String getRoi() {
		return mRoi;
	}

	public String getRate() {
		return mRate;
	}

	public String getRoe() {
		return mRoe;
	}

	public String getPe() {
		return mPe;
	}

	public String getPb() {
		return mPb;
	}

	public String getDividend() {
		return mDividend;
	}

	public String getYield() {
		return mYield;
	}

	public String getDividendRatio() {
		return mDividendRatio;
	}

	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	public void setFavorite(boolean favorite) {
		mFavorite = favorite;
	}

	public void setHold(String hold) {
		mHold = hold;
	}

	public void setRoi(String roi) {
		mRoi = roi;
	}

	public void setRate(String rate) {
		mRate = rate;
	}

	public void setRoe(String roe) {
		mRoe = roe;
	}

	public void setPe(String pe) {
		mPe = pe;
	}

	public void setPb(String pb) {
		mPb = pb;
	}

	public void setDividend(String dividend) {
		mDividend = dividend;
	}

	public void setYield(String yield) {
		mYield = yield;
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
		mEnabled = Preferences.getBoolean(mContext,
				Setting.KEY_STOCK_FILTER_ENABLED, true);

		mFavorite = Preferences.getBoolean(mContext,
				Setting.KEY_STOCK_FILTER_FAVORITE, true);

		mHold = Preferences.getString(mContext, Setting.KEY_STOCK_FILTER_HOLD,
				"");
		mRoi = Preferences.getString(mContext, Setting.KEY_STOCK_FILTER_ROI,
				"");
		mRate = Preferences.getString(mContext, Setting.KEY_STOCK_FILTER_RATE,
				"");
		mRoe = Preferences.getString(mContext, Setting.KEY_STOCK_FILTER_ROE,
				"");
		mPe = Preferences.getString(mContext, Setting.KEY_STOCK_FILTER_PE, "");
		mPb = Preferences.getString(mContext, Setting.KEY_STOCK_FILTER_PB, "");
		mDividend = Preferences.getString(mContext,
				Setting.KEY_STOCK_FILTER_DIVIDEND, "");
		mYield = Preferences.getString(mContext,
				Setting.KEY_STOCK_FILTER_YIELD, "");
		mDividendRatio = Preferences.getString(mContext,
				Setting.KEY_STOCK_FILTER_DIVIDEND_RATIO, "");

		validate();
	}

	public void write() {
		Preferences.putBoolean(mContext, Setting.KEY_STOCK_FILTER_ENABLED,
				mEnabled);
		Preferences.putBoolean(mContext, Setting.KEY_STOCK_FILTER_FAVORITE,
				mFavorite);

		validate();

		Preferences.putString(mContext, Setting.KEY_STOCK_FILTER_HOLD, mHold);
		Preferences.putString(mContext, Setting.KEY_STOCK_FILTER_ROI, mRoi);
		Preferences.putString(mContext, Setting.KEY_STOCK_FILTER_RATE, mRate);
		Preferences.putString(mContext, Setting.KEY_STOCK_FILTER_ROE, mRoe);
		Preferences.putString(mContext, Setting.KEY_STOCK_FILTER_PE, mPe);
		Preferences.putString(mContext, Setting.KEY_STOCK_FILTER_PB, mPb);
		Preferences.putString(mContext, Setting.KEY_STOCK_FILTER_DIVIDEND,
				mDividend);
		Preferences
				.putString(mContext, Setting.KEY_STOCK_FILTER_YIELD, mYield);
		Preferences.putString(mContext,
				Setting.KEY_STOCK_FILTER_DIVIDEND_RATIO, mDividendRatio);
	}

	public void get(Bundle bundle) {
		if (bundle == null) {
			return;
		}

		mEnabled = bundle.getBoolean(Setting.KEY_STOCK_FILTER_ENABLED, true);
		mFavorite = bundle.getBoolean(Setting.KEY_STOCK_FILTER_FAVORITE, true);

		mHold = bundle.getString(Setting.KEY_STOCK_FILTER_HOLD);
		mRoi = bundle.getString(Setting.KEY_STOCK_FILTER_ROI);
		mRate = bundle.getString(Setting.KEY_STOCK_FILTER_RATE);
		mRoe = bundle.getString(Setting.KEY_STOCK_FILTER_ROE);
		mPe = bundle.getString(Setting.KEY_STOCK_FILTER_PE);
		mPb = bundle.getString(Setting.KEY_STOCK_FILTER_PB);
		mDividend = bundle.getString(Setting.KEY_STOCK_FILTER_DIVIDEND);
		mYield = bundle.getString(Setting.KEY_STOCK_FILTER_YIELD);
		mDividendRatio = bundle
				.getString(Setting.KEY_STOCK_FILTER_DIVIDEND_RATIO);
	}

	public void put(Bundle bundle) {
		if (bundle == null) {
			return;
		}

		bundle.putBoolean(Setting.KEY_STOCK_FILTER_ENABLED, mEnabled);
		bundle.putBoolean(Setting.KEY_STOCK_FILTER_FAVORITE, mFavorite);

		bundle.putString(Setting.KEY_STOCK_FILTER_HOLD, mHold);
		bundle.putString(Setting.KEY_STOCK_FILTER_ROI, mRoi);
		bundle.putString(Setting.KEY_STOCK_FILTER_RATE, mRate);
		bundle.putString(Setting.KEY_STOCK_FILTER_ROE, mRoe);
		bundle.putString(Setting.KEY_STOCK_FILTER_PE, mPe);
		bundle.putString(Setting.KEY_STOCK_FILTER_PB, mPb);
		bundle.putString(Setting.KEY_STOCK_FILTER_DIVIDEND, mDividend);
		bundle.putString(Setting.KEY_STOCK_FILTER_YIELD, mYield);
		bundle.putString(Setting.KEY_STOCK_FILTER_DIVIDEND_RATIO,
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
