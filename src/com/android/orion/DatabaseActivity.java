package com.android.orion;

import android.os.AsyncTask;
import android.os.Bundle;

public class DatabaseActivity extends OrionBaseActivity {

	public static final long RESULT_FAILURE = -1;
	public static final long RESULT_SUCCESS = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	void startLoadTask(Object... params) {
		LoadTask task = new LoadTask();
		task.execute(params);
	}

	void startSaveTask(Object... params) {
		SaveTask task = new SaveTask();
		task.execute(params);
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

	public class LoadTask extends AsyncTask<Object, Integer, Long> {

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

	public class SaveTask extends AsyncTask<Object, Integer, Long> {

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
}
