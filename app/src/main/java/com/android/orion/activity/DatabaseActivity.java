package com.android.orion.activity;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

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

	Long doInBackgroundLoad(Object... params) {
		return RESULT_SUCCESS;
	}

	void onPostExecuteLoad(Long result) {
	}

	Long doInBackgroundSave(Object... params) {
		return RESULT_SUCCESS;
	}

	void onPostExecuteSave(Long result) {
	}

	void startLoadTask(Object... params) {
		LoadAsyncTask loadAsyncTask = new LoadAsyncTask();
		loadAsyncTask.execute(params);
	}

	void startSaveTask(Object... params) {
		SaveAsyncTask saveAsyncTask = new SaveAsyncTask();
		saveAsyncTask.execute(params);
	}

	class LoadAsyncTask extends AsyncTask<Object, Integer, Long> {

		@Override
		protected Long doInBackground(Object... params) {
			return doInBackgroundLoad(params);
		}

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);

			onPostExecuteLoad(result);
		}
	}

	class SaveAsyncTask extends AsyncTask<Object, Integer, Long> {

		@Override
		protected Long doInBackground(Object... params) {
			return doInBackgroundSave(params);
		}

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);

			onPostExecuteSave(result);
		}
	}

	void onDatabaseChanged(boolean selfChange, Uri uri) {
	}

	class DatabaseContentObserver extends ContentObserver {

		public DatabaseContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			super.onChange(selfChange, uri);

			onDatabaseChanged(selfChange, uri);
		}
	}
}
