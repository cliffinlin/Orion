package com.android.orion.interfaces;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public interface IBackgroundHandler {
	void handleOnCreate(Bundle savedInstanceState);
	void handleOnResume();
	void handleOnPause();
	void handleOnDestroy();
	void handleOnStart();
	void handleOnRestart();
	void handleOnStop();
	void handleOnNewIntent(Intent intent);
	void handleOnCreateOptionsMenu(Menu menu);
	void handleOnMenuItemSelected(MenuItem item);
}
