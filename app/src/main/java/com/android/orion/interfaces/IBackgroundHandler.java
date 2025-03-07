package com.android.orion.interfaces;

public interface IBackgroundHandler {
	void onCreateHandler();
	void onResumeHandler();
	void onPauseHandler();
	void onDestroyHandler();
	void onNewIntentHandler();
	void onCreateOptionsMenuHandler();
	void onMenuItemSelectedHandler();
	void onMenuItemSelectedHomeHandler();
	void onMenuItemSelectedSearchHandler();
	void onMenuItemSelectedNewHandler();
	void onMenuItemSelectedRefreshHandler();
	void onMenuItemSelectedSettingsHandler();

}
