package com.android.orion;

import android.content.SearchRecentSuggestionsProvider;

public class OrionSearchRecentSuggestionsProvider extends
		SearchRecentSuggestionsProvider {

	public final static String AUTHORITY = "com.android.orion.OrionSearchRecentSuggestionsProvider";
	public final static int MODE = DATABASE_MODE_QUERIES;

	public OrionSearchRecentSuggestionsProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}
}
