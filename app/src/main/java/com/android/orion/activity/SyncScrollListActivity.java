package com.android.orion.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.database.DatabaseContract;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.view.SyncHorizontalScrollView;

import java.util.HashMap;
import java.util.Map;

public abstract class SyncScrollListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor>,
		AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener,
		View.OnClickListener {

	protected static final int mHeaderTextDefaultColor = Color.BLACK;
	protected static final int mHeaderTextHighlightColor = Color.RED;

	// 排序相关
	protected String mSortOrderColumn;
	protected String mSortOrderDirection = DatabaseContract.ORDER_DESC;
	protected String mSortOrderDefault;
	protected String mSortOrder;
	protected String mSortOrderPreferenceKey;

	// 视图组件
	protected SyncHorizontalScrollView mTitleSHSV;
	protected SyncHorizontalScrollView mContentSHSV;
	protected ListView mLeftListView;
	protected ListView mRightListView;
	protected SimpleCursorAdapter mLeftAdapter;
	protected SimpleCursorAdapter mRightAdapter;

	// 表头映射
	protected Map<Integer, String> mHeaderColumnMap = new HashMap<>();
	protected Map<Integer, TextView> mHeaderTextViewMap = new HashMap<>();

	// ContentObserver 用于监听数据变化
	protected ContentObserver mContentObserver;

	// 加载器ID
	protected abstract int getLoaderId();
	protected abstract String getSelection();
	protected abstract void setupHeaderColumnMap();
	protected abstract void setupAdapters();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResourceId());

		// 初始化 ContentObserver
		initContentObserver();

		initSortOrder();
		setupHeaderColumnMap();
		initHeader();
		setupListView();
		initLoader();
	}

	protected abstract int getLayoutResourceId();

	/**
	 * 初始化 ContentObserver，子类可以重写此方法提供自定义实现
	 */
	protected void initContentObserver() {
		mContentObserver = new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				onDataChanged();
			}
		};
	}

	/**
	 * 数据变化时的回调，子类可以重写
	 */
	protected void onDataChanged() {
		// 默认实现：重新加载数据
		restartLoader();
	}

	/**
	 * 注册 ContentObserver，子类可以调用
	 */
	protected void registerContentObserver() {
		if (mContentObserver != null && getContentUri() != null) {
			getContentResolver().registerContentObserver(
					getContentUri(), true, mContentObserver);
		}
	}

	/**
	 * 取消注册 ContentObserver，子类可以调用
	 */
	protected void unregisterContentObserver() {
		if (mContentObserver != null) {
			getContentResolver().unregisterContentObserver(mContentObserver);
		}
	}

	private void initSortOrder() {
		mSortOrder = Preferences.getString(mSortOrderPreferenceKey, mSortOrderDefault);
		if (!TextUtils.isEmpty(mSortOrder)) {
			String[] strings = mSortOrder.split("\\s+");
			if (strings != null && strings.length > 0) {
				mSortOrderColumn = strings[0];
			}
		}
	}

	protected void initHeader() {
		mTitleSHSV = findViewById(R.id.title_shsv);
		mContentSHSV = findViewById(R.id.content_shsv);

		if (mTitleSHSV != null && mContentSHSV != null) {
			mTitleSHSV.setScrollView(mContentSHSV);
			mContentSHSV.setScrollView(mTitleSHSV);
		}

		// 初始化表头点击监听
		for (Map.Entry<Integer, String> entry : mHeaderColumnMap.entrySet()) {
			TextView textView = findViewById(entry.getKey());
			if (textView != null) {
				textView.setOnClickListener(this);
				mHeaderTextViewMap.put(entry.getKey(), textView);
			}
		}

		highlightCurrentSortColumn();
	}

	protected void setupListView() {
		setupAdapters();

		if (mLeftListView != null && mLeftAdapter != null) {
			mLeftListView.setAdapter(mLeftAdapter);
			mLeftListView.setOnItemClickListener(this);
			mLeftListView.setOnItemLongClickListener(this);
		}

		if (mRightListView != null && mRightAdapter != null) {
			mRightListView.setAdapter(mRightAdapter);
			mRightListView.setOnItemClickListener(this);
			mRightListView.setOnItemLongClickListener(this);
		}
	}

	protected void initLoader() {
		mLoaderManager.initLoader(getLoaderId(), null, this);
	}

	protected void destroyLoader() {
		mLoaderManager.destroyLoader(getLoaderId());
	}

	protected void restartLoader() {
		mLoaderManager.restartLoader(getLoaderId(), null, this);
	}

	@Override
	public void onClick(@NonNull View view) {
		int viewId = view.getId();

		if (mHeaderColumnMap.containsKey(viewId)) {
			resetHeaderTextColor();
			setHeaderTextColor(viewId, mHeaderTextHighlightColor);

			mSortOrderColumn = mHeaderColumnMap.get(viewId);
			toggleSortOrderDirection();
			updateSortOrder();

			restartLoader();
		}
	}

	protected void toggleSortOrderDirection() {
		if (TextUtils.equals(mSortOrderDirection, DatabaseContract.ORDER_ASC)) {
			mSortOrderDirection = DatabaseContract.ORDER_DESC;
		} else {
			mSortOrderDirection = DatabaseContract.ORDER_ASC;
		}
	}

	protected void updateSortOrder() {
		mSortOrder = mSortOrderColumn + " " + mSortOrderDirection;
		Preferences.putString(mSortOrderPreferenceKey, mSortOrder);
	}

	protected void setHeaderTextColor(int viewId, int color) {
		TextView textView = findViewById(viewId);
		if (textView != null) {
			textView.setTextColor(color);
		}
	}

	protected void setHeaderTextColor(TextView textView, int color) {
		if (textView != null) {
			textView.setTextColor(color);
		}
	}

	protected void resetHeaderTextColor() {
		for (TextView textView : mHeaderTextViewMap.values()) {
			setHeaderTextColor(textView, mHeaderTextDefaultColor);
		}
	}

	protected void highlightCurrentSortColumn() {
		resetHeaderTextColor();

		for (Map.Entry<Integer, String> entry : mHeaderColumnMap.entrySet()) {
			if (TextUtils.equals(entry.getValue(), mSortOrderColumn)) {
				setHeaderTextColor(entry.getKey(), mHeaderTextHighlightColor);
				break;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		highlightCurrentSortColumn();
		restartLoader();
	}

	@Override
	protected void onStop() {
		super.onStop();
		destroyLoader();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterContentObserver();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == getLoaderId()) {
			return new CursorLoader(this,
					getContentUri(),
					getProjection(),
					getSelection(),
					null,
					mSortOrder);
		}
		return null;
	}

	protected abstract android.net.Uri getContentUri();
	protected abstract String[] getProjection();

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() == getLoaderId()) {
			if (mLeftAdapter != null) {
				mLeftAdapter.swapCursor(cursor);
			}
			if (mRightAdapter != null) {
				mRightAdapter.swapCursor(cursor);
			}

			setListViewHeightBasedOnChildren(mLeftListView);
			setListViewHeightBasedOnChildren(mRightListView);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == getLoaderId()) {
			if (mLeftAdapter != null) {
				mLeftAdapter.swapCursor(null);
			}
			if (mRightAdapter != null) {
				mRightAdapter.swapCursor(null);
			}
		}
	}

	// 子类需要实现的抽象方法
	protected abstract SimpleCursorAdapter createLeftAdapter();
	protected abstract SimpleCursorAdapter createRightAdapter();
}