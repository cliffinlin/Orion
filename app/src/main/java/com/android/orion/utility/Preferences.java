package com.android.orion.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.android.orion.application.MainApplication;

public class Preferences {
	public static final Context mContext = MainApplication.getContext();

	public static void putBoolean(Context context, String key, boolean value) {
		getEditor().putBoolean(key, value).commit();
	}

	public static boolean getBoolean(String key, boolean defValue) {
		return getPreferences().getBoolean(key, defValue);
	}

	public static void putBoolean(String key, boolean value) {
		getEditor().putBoolean(key, value).commit();
	}

	public static void putInt(String key, int value) {
		getEditor().putInt(key, value).commit();

	}

	public static int getInt(String key, int defValue) {
		return getPreferences().getInt(key, defValue);
	}

	public static void putString(String key, String value) {
		getEditor().putString(key, value).commit();
	}

	public static String getString(String key, String defValue) {
		return getPreferences().getString(key, defValue);
	}

	public static void putFloat(String key, float value) {
		getEditor().putFloat(key, value).commit();
	}

	public static float getFloat(String key, float defValue) {
		return getPreferences().getFloat(key, defValue);
	}

	public static void putLong(String key, long value) {
		getEditor().putLong(key, value).commit();
	}

	public static long getLong(String key, long defValue) {
		return getPreferences().getLong(key, defValue);
	}

	public static SharedPreferences getPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public static Editor getEditor() {
		return getPreferences().edit();
	}
}