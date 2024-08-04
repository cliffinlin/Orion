package com.android.orion.provider;

import android.content.SearchRecentSuggestionsProvider;

public class StockSearchRecentSuggestionsProvider extends
		SearchRecentSuggestionsProvider {

	public final static String AUTHORITY = "com.android.orion.StockSearchRecentSuggestionsProvider";
	public final static int MODE = DATABASE_MODE_QUERIES;

	public StockSearchRecentSuggestionsProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}
}
