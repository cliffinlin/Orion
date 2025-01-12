package com.android.orion.activity;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import com.android.orion.config.Config;
import com.android.orion.database.DatabaseContract;
import com.android.orion.utility.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class DatabaseActivity extends BaseActivity {

	public static final long RESULT_FAILURE = -1;
	public static final long RESULT_SUCCESS = 0;

	protected DatabaseContentObserver mDatabaseContentObserver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (mDatabaseContentObserver == null) {
			mDatabaseContentObserver = new DatabaseContentObserver(
					new Handler());
		}
	}

	String backupDatabase() {
		String result = "";
		try {
			File dbFile = getDatabasePath(DatabaseContract.DATABASE_FILE_NAME);
			String dbPath = dbFile.getAbsolutePath();
			Utility.createDirectory(Environment.getExternalStorageDirectory() + "/" + Config.APP_NAME);
			String backupPath = Environment.getExternalStorageDirectory() + "/" + Config.APP_NAME + "/"
					+ DatabaseContract.DATABASE_NAME + "_" + Utility.getCurrentDateString() + DatabaseContract.DATABASE_EXT;
			FileChannel src = new FileInputStream(dbPath).getChannel();
			FileChannel dst = new FileOutputStream(backupPath).getChannel();
			dst.transferFrom(src, 0, src.size());
			src.close();
			dst.close();
			result = backupPath;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	void restoreDatabase() {
		try {
			String backupPath = Environment.getExternalStorageDirectory() + "/" + Config.APP_NAME + "/" + DatabaseContract.DATABASE_FILE_NAME;
			File backupFile = new File(backupPath);
			if (!backupFile.exists()) {
				return;
			}
			File dbFile = getDatabasePath(DatabaseContract.DATABASE_FILE_NAME);
			String dbPath = dbFile.getAbsolutePath();
			FileChannel src = new FileInputStream(backupPath).getChannel();
			FileChannel dst = new FileOutputStream(dbPath).getChannel();
			dst.transferFrom(src, 0, src.size());
			src.close();
			dst.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class DatabaseContentObserver extends ContentObserver {

		public DatabaseContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);
		}
	}
}
