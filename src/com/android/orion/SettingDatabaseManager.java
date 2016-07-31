package com.android.orion;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.SettingDatabase;
import com.android.orion.utility.Utility;

public class SettingDatabaseManager extends DatabaseManager {
	private static SettingDatabaseManager mInstance = null;

	public static synchronized SettingDatabaseManager getInstance(
			Context context) {
		if (mInstance == null) {
			mInstance = new SettingDatabaseManager(context);
		}
		return mInstance;
	}

	private SettingDatabaseManager() {
		super();
	}

	private SettingDatabaseManager(Context context) {
		super(context);
	}

	Uri insert(SettingDatabase setting) {
		Uri uri = null;

		if (setting == null) {
			return uri;
		}

		if (mContentResolver == null) {
			return uri;
		}

		uri = mContentResolver.insert(DatabaseContract.Setting.CONTENT_URI,
				setting.getContentValues());

		return uri;
	}

	Cursor query(String selection, String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;

		if (mContentResolver == null) {
			return cursor;
		}

		cursor = mContentResolver.query(DatabaseContract.Setting.CONTENT_URI,
				DatabaseContract.Setting.PROJECTION_ALL, selection,
				selectionArgs, sortOrder);

		return cursor;
	}

	Cursor query(SettingDatabase setting) {
		Cursor cursor = null;

		if (setting == null) {
			return cursor;
		}

		String selection = DatabaseContract.Setting.COLUMN_KEY + " = " + "\'"
				+ setting.getKey() + "\'";

		cursor = query(selection, null, null);

		return cursor;
	}

	boolean isExist(SettingDatabase setting) {
		boolean result = false;
		Cursor cursor = null;

		if (setting == null) {
			return result;
		}

		try {
			cursor = query(setting);

			if (cursor != null) {
				result = (cursor.getCount() > 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return result;
	}

	int update(SettingDatabase setting) {
		int result = 0;

		if (setting == null) {
			return result;
		}

		if (mContentResolver == null) {
			return result;
		}

		String where = DatabaseContract.Setting.COLUMN_KEY + " = " + "\'"
				+ setting.getKey() + "\'";

		result = mContentResolver.update(DatabaseContract.Setting.CONTENT_URI,
				setting.getContentValues(), where, null);

		return result;
	}

	void save(SettingDatabase setting) {
		String now = Utility.getCurrentDateTimeString();

		if (setting == null) {
			return;
		}

		if (!isExist(setting)) {
			setting.setCreated(now);
			setting.setModified(now);
			insert(setting);
		} else {
			setting.setModified(now);
			update(setting);
		}
	}

	void save(String key, String value) {
		SettingDatabase setting = new SettingDatabase();

		setting.setKey(key);
		setting.setValue(value);

		save(setting);
	}

	String load(String key) {
		String value = "";
		Cursor cursor = null;
		SettingDatabase setting = new SettingDatabase();

		setting.setKey(key);

		try {
			cursor = query(setting);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				cursor.moveToNext();
				setting.set(cursor);
				value = setting.getValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}

		return value;
	}
}
