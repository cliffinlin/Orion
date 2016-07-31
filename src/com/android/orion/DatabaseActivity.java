package com.android.orion;

import android.os.AsyncTask;
import android.os.Bundle;

public class DatabaseActivity extends OrionBaseActivity {

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

	void doInBackgroundLoad(Object... params) {
	}

	void onPostExecuteLoad(Long result) {
	}

	void doInBackgroundSave(Object... params) {
	}

	void onPostExecuteSave(Long result) {
	}

	public class LoadTask extends AsyncTask<Object, Integer, Long> {

		@Override
		protected Long doInBackground(Object... params) {
			doInBackgroundLoad(params);
			return null;
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
			doInBackgroundSave(params);
			return null;
		}

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);
			onPostExecuteSave(result);
		}
	}
}
