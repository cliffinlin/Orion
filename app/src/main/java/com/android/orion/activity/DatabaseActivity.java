package com.android.orion.activity;

import android.os.Environment;

import com.android.orion.config.Config;
import com.android.orion.database.DatabaseContract;
import com.android.orion.utility.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class DatabaseActivity extends BaseActivity {

	private String getAppDir() {
		File appDir = getExternalFilesDir(null);
		if (appDir != null) {
			return appDir.getAbsolutePath();
		}
		return getFilesDir().getAbsolutePath();
	}

	String backupDatabase() {
		String result = "";
		try {
			File dbFile = getDatabasePath(DatabaseContract.DATABASE_FILE_NAME);
			String dbPath = dbFile.getAbsolutePath();
			String appDir = getAppDir();
			Utility.createDirectory(appDir);
			result = appDir + "/"
					+ DatabaseContract.DATABASE_NAME + "_" + Utility.getCurrentDateString() + DatabaseContract.DATABASE_EXT;
			FileChannel src = new FileInputStream(dbPath).getChannel();
			FileChannel dst = new FileOutputStream(result).getChannel();
			dst.transferFrom(src, 0, src.size());
			src.close();
			dst.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	void restoreDatabase() {
		try {
			String appDir = getAppDir();
			String backupPath = appDir + "/" + DatabaseContract.DATABASE_FILE_NAME;
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
}
