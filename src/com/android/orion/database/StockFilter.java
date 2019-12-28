package com.android.orion.database;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.orion.utility.Preferences;

public class StockFilter extends Setting {
	Context mContext;

	boolean mEnable = false;

	String mDiscount = "";
	String mPE = "";
	String mPB = "";
	String mDividend = "";
	String mYield = "";
	String mDelta = "";

	boolean mDefaultEnable = false;
	String mDefaultDiscount = "";
	String mDefaultPE = "";
	String mDefaultPB = "";
	String mDefaultDividend = "";
	String mDefaultYield = "";
	String mDefaultDelta = "";

	public StockFilter(Context context) {
		mContext = context;
	}

	public boolean getEnable() {
		return mEnable;
	}

	public String getDiscount() {
		return mDiscount;
	}
	
	public String getPE() {
		return mPE;
	}

	public String getPB() {
		return mPB;
	}

	public String getDividend() {
		return mDividend;
	}

	public String getYield() {
		return mYield;
	}

	public String getDelta() {
		return mDelta;
	}

	public void setEnable(boolean enable) {
		mEnable = enable;
	}

	public void setDiscount(String discount) {
		mDiscount = discount;
	}
	
	public void setPE(String pe) {
		mPE = pe;
	}

	public void setPB(String pb) {
		mPB = pb;
	}

	public void setDividend(String dividend) {
		mDividend = dividend;
	}

	public void setYield(String yield) {
		mYield = yield;
	}

	public void setDelta(String delta) {
		mDelta = delta;
	}

	public void setDefaultEnable(boolean enable) {
		mDefaultEnable = enable;
	}

	public void setDefaultDiscount(String discount) {
		mDefaultDiscount = discount;
	}
	
	public void setDefaultPE(String pe) {
		mDefaultPE = pe;
	}

	public void setDefaultPB(String pb) {
		mDefaultPB = pb;
	}

	public void setDefaultDividend(String dividend) {
		mDefaultDividend = dividend;
	}

	public void setDefaultYield(String yield) {
		mDefaultYield = yield;
	}

	public void setDefaultDelta(String delta) {
		mDefaultDelta = delta;
	}

	public void read() {
		mEnable = Preferences.readBoolean(mContext, Setting.KEY_STOCK_FILTER_ENABLE,
				mDefaultEnable);

		mDiscount = Preferences.readString(mContext, Setting.KEY_STOCK_FILTER_DISCOUNT,
				mDefaultDiscount);
		mPE = Preferences.readString(mContext, Setting.KEY_STOCK_FILTER_PE,
				mDefaultPE);
		mPB = Preferences.readString(mContext, Setting.KEY_STOCK_FILTER_PB,
				mDefaultPB);
		mDividend = Preferences.readString(mContext,
				Setting.KEY_STOCK_FILTER_DIVIDEND, mDefaultDividend);
		mYield = Preferences.readString(mContext,
				Setting.KEY_STOCK_FILTER_YIELD, mDefaultYield);
		mDelta = Preferences.readString(mContext,
				Setting.KEY_STOCK_FILTER_DELTA, mDefaultDelta);
	}

	public void write() {
		Preferences.writeBoolean(mContext, Setting.KEY_STOCK_FILTER_ENABLE, mEnable);

		Preferences.writeString(mContext, Setting.KEY_STOCK_FILTER_DISCOUNT, mDiscount);
		Preferences.writeString(mContext, Setting.KEY_STOCK_FILTER_PE, mPE);
		Preferences.writeString(mContext, Setting.KEY_STOCK_FILTER_PB, mPB);
		Preferences.writeString(mContext, Setting.KEY_STOCK_FILTER_DIVIDEND,
				mDividend);
		Preferences.writeString(mContext, Setting.KEY_STOCK_FILTER_YIELD,
				mYield);
		Preferences.writeString(mContext, Setting.KEY_STOCK_FILTER_DELTA,
				mDelta);
	}

	public void get(Bundle bundle) {
		if (bundle == null) {
			return;
		}

		mEnable = bundle.getBoolean(Setting.KEY_STOCK_FILTER_ENABLE, false);
		
		mDiscount = bundle.getString(Setting.KEY_STOCK_FILTER_DISCOUNT);
		mPE = bundle.getString(Setting.KEY_STOCK_FILTER_PE);
		mPB = bundle.getString(Setting.KEY_STOCK_FILTER_PB);
		mDividend = bundle.getString(Setting.KEY_STOCK_FILTER_DIVIDEND);
		mYield = bundle.getString(Setting.KEY_STOCK_FILTER_YIELD);
		mDelta = bundle.getString(Setting.KEY_STOCK_FILTER_DELTA);
	}

	public void put(Bundle bundle) {
		if (bundle == null) {
			return;
		}

		bundle.putBoolean(Setting.KEY_STOCK_FILTER_ENABLE, mEnable);
		
		bundle.putString(Setting.KEY_STOCK_FILTER_DISCOUNT, mDiscount);
		bundle.putString(Setting.KEY_STOCK_FILTER_PE, mPE);
		bundle.putString(Setting.KEY_STOCK_FILTER_PB, mPB);
		bundle.putString(Setting.KEY_STOCK_FILTER_DIVIDEND, mDividend);
		bundle.putString(Setting.KEY_STOCK_FILTER_YIELD, mYield);
		bundle.putString(Setting.KEY_STOCK_FILTER_DELTA, mDelta);
	}

	public String getSelection() {
		String selection = "";

		if (mEnable) {
			if (!TextUtils.isEmpty(mDiscount)) {
				selection += " AND " + "discount" + mDiscount;
			}
			
			if (!TextUtils.isEmpty(mPE)) {
				selection += " AND " + "pe" + mPE;
			}

			if (!TextUtils.isEmpty(mPB)) {
				selection += " AND " + "pb" + mPB;
			}

			if (!TextUtils.isEmpty(mDividend)) {
				selection += " AND " + "dividend" + mDividend;
			}

			if (!TextUtils.isEmpty(mYield)) {
				selection += " AND " + "yield" + mYield;
			}

			if (!TextUtils.isEmpty(mDelta)) {
				selection += " AND " + "delta" + mDelta;
			}
		}

		return selection;
	}
}
