package com.android.orion;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.SettingDatabase;
import com.android.orion.database.Stock;

public abstract class StockEditActivity extends DatabaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

	static final int LOADER_ID_STOCK_DEAL_ARRAY_MAP = 0;
	static final int LOADER_ID_STOCK_LIST = 1;

	String mSortOrder = null;
	ListView mListView = null;
	CustomSimpleCursorAdapter mAdapter = null;

	String[] mFrom = new String[] { DatabaseContract.COLUMN_NAME,
			DatabaseContract.COLUMN_CODE, DatabaseContract.COLUMN_PRICE,
			DatabaseContract.COLUMN_NET };
	int[] mTo = new int[] { R.id.name, R.id.code, R.id.price, R.id.net };

	abstract String getSelection();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_edit);

		mSortOrder = getIntent().getStringExtra(
				SettingDatabase.KEY_SORT_ORDER_STOCK_LIST);

		mListView = (ListView) findViewById(R.id.stock_listview);

		mAdapter = new CustomSimpleCursorAdapter(this,
				R.layout.activity_stock_edit_item, null, mFrom, mTo, 0);

		if ((mListView != null) && (mAdapter != null)) {
			mListView.setAdapter(mAdapter);
			mListView.setOnItemClickListener(this);
		}

		startLoadTask(LOADER_ID_STOCK_DEAL_ARRAY_MAP);

		mLoaderManager.initLoader(LOADER_ID_STOCK_LIST, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_STOCK_LIST, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
				DatabaseContract.Stock.PROJECTION_ALL, getSelection(), null,
				mSortOrder);

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
	
	@Override
	void doInBackgroundLoad(Object... params) {
		super.doInBackgroundLoad(params);
		loadStockDealArrayMap();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
	}

	public class CustomSimpleCursorAdapter extends SimpleCursorAdapter
			implements OnClickListener {

		public CustomSimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();
			Stock stock = Stock.obtain();
			
			stock.set(cursor);

			setViewText(holder.nameTextView, stock.getName());
			setViewText(holder.codeTextView, stock.getCode());
			setViewText(holder.priceTextView, String.valueOf(stock.getPrice()));
			setViewText(holder.netTextView, String.valueOf(stock.getNet()));

			if (!TextUtils.isEmpty(stock.getMark())) {
				holder.actionImageView.setImageResource(R.drawable.ic_minus);
			} else {
				holder.actionImageView.setImageResource(R.drawable.ic_plus);
			}

			holder.actionImageView.setTag(stock.getId());
			holder.actionImageView.setOnClickListener(this);

			super.bindView(view, context, cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = super.newView(context, cursor, parent);

			ViewHolder holder = new ViewHolder();

			holder.nameTextView = (TextView) view.findViewById(R.id.name);
			holder.codeTextView = (TextView) view.findViewById(R.id.code);
			holder.priceTextView = (TextView) view.findViewById(R.id.price);
			holder.netTextView = (TextView) view.findViewById(R.id.net);
			holder.actionImageView = (ImageView) view.findViewById(R.id.action);

			view.setTag(holder);

			return view;
		}

		@Override
		public void onClick(View view) {
			if (view != null) {
				Cursor cursor = null;
				long stockId = (Long) view.getTag();
				Stock stock = Stock.obtain();
				Uri uri = ContentUris.withAppendedId(
						DatabaseContract.Stock.CONTENT_URI, stockId);

				try {
					cursor = mContentResolver.query(uri,
							DatabaseContract.Stock.PROJECTION_ALL, null, null,
							null);
					if (cursor != null) {
						cursor.moveToNext();
						stock.set(cursor);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					mStockDatabaseManager.closeCursor(cursor);
				}

				try {
					if (TextUtils.isEmpty(stock.getMark())) {
						updateStockMark(stockId,
								Constants.STOCK_FLAG_MARK_FAVORITE);
						startService(Constants.SERVICE_ADD_STOCK_FAVORITE,
								Constants.EXECUTE_IMMEDIATE);
						startService(Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE,
								Constants.EXECUTE_IMMEDIATE);
					} else {
						if (!mStockDealArrayMap.containsKey(stock.getSE()+stock.getCode())) {
							updateStockMark(stockId, Constants.STOCK_FLAG_NONE);
							startService(Constants.SERVICE_REMOVE_STOCK_FAVORITE,
									Constants.EXECUTE_IMMEDIATE);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			restartLoader();
		}
	}

	class ViewHolder {
		public TextView nameTextView;
		public TextView codeTextView;
		public TextView priceTextView;
		public TextView netTextView;
		public ImageView actionImageView;
	}
}