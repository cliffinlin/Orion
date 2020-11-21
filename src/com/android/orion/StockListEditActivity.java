package com.android.orion;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.android.orion.database.Stock;

public class StockListEditActivity extends DatabaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

	static final int LOADER_ID_STOCK_LIST = 0;

	String mSortOrder = null;
	ListView mListView = null;
	CustomSimpleCursorAdapter mAdapter = null;

	String[] mFrom = new String[] { DatabaseContract.COLUMN_NAME,
			DatabaseContract.COLUMN_CODE, DatabaseContract.COLUMN_PRICE,
			DatabaseContract.COLUMN_NET };
	int[] mTo = new int[] { R.id.name, R.id.code, R.id.price, R.id.net };

	String getSelection() {
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_edit);

		mSortOrder = getIntent().getStringExtra(
				Constants.EXTRA_STOCK_LIST_SORT_ORDER);

		mListView = (ListView) findViewById(R.id.stock_listview);

		mAdapter = new CustomSimpleCursorAdapter(this,
				R.layout.activity_stock_edit_item, null, mFrom, mTo, 0);

		if ((mListView != null) && (mAdapter != null)) {
			mListView.setAdapter(mAdapter);
			mListView.setOnItemClickListener(this);
		}

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
			Stock stock = new Stock();

			stock.set(cursor);

			setViewText(holder.mTextViewName, stock.getName());
			setViewText(holder.mTextViewCode, stock.getCode());
			setViewText(holder.mTextViewPrice, String.valueOf(stock.getPrice()));
			setViewText(holder.mTextViewNet, String.valueOf(stock.getNet()));

			if (!TextUtils.isEmpty(stock.getMark())) {
				holder.mImageViewFavorite.setImageResource(R.drawable.ic_favorite);
			} else {
				holder.mImageViewFavorite.setImageResource(R.drawable.ic_none_favorite);
			}
			
			if (stock.getHold() == 0) {
				holder.mImageViewDelete.setImageResource(R.drawable.ic_delete);
			} else {
				holder.mImageViewDelete.setImageResource(R.drawable.ic_undeletable);
			}

			holder.mImageViewFavorite.setTag(stock.getId());
			holder.mImageViewFavorite.setOnClickListener(this);
			
			holder.mImageViewDelete.setTag(stock.getId());
			holder.mImageViewDelete.setOnClickListener(this);

			super.bindView(view, context, cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = super.newView(context, cursor, parent);

			ViewHolder holder = new ViewHolder();

			holder.mTextViewName = (TextView) view.findViewById(R.id.name);
			holder.mTextViewCode = (TextView) view.findViewById(R.id.code);
			holder.mTextViewPrice = (TextView) view.findViewById(R.id.price);
			holder.mTextViewNet = (TextView) view.findViewById(R.id.net);
			holder.mImageViewFavorite = (ImageView) view.findViewById(R.id.favorite);
			holder.mImageViewDelete = (ImageView) view.findViewById(R.id.delete);

			view.setTag(holder);

			return view;
		}

		@Override
		public void onClick(View view) {
			if (view == null) {
				return;
			}
			
			Cursor cursor = null;
			long stockId = (Long) view.getTag();
			Stock stock = new Stock();
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
				switch (view.getId()) {
				case R.id.favorite:
					if (TextUtils.isEmpty(stock.getMark())) {
						updateStockMark(stockId,
								Constants.STOCK_FLAG_MARK_FAVORITE);
						startService(Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE,
								Constants.EXECUTE_IMMEDIATE);
					} else {
						if (stock.getHold() == 0) {
							updateStockMark(stockId, Constants.STOCK_FLAG_NONE);
						}
					}
					break;
					
				case R.id.delete:
					if (stock.getHold() == 0) {
						mStockDatabaseManager.deleteStock(stock.getId());
					}
					break;

				default:
					break;
				}


			} catch (Exception e) {
				e.printStackTrace();
			}

			restartLoader();
		}
	}

	class ViewHolder {
		public TextView mTextViewName;
		public TextView mTextViewCode;
		public TextView mTextViewPrice;
		public TextView mTextViewNet;
		public ImageView mImageViewFavorite;
		public ImageView mImageViewDelete;
	}
}