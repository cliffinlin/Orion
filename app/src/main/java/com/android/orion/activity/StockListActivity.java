package com.android.orion.activity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
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

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.IndexComponent;
import com.android.orion.database.Stock;
import com.android.orion.setting.Constant;
import com.android.orion.utility.Utility;

import java.util.ArrayList;

public class StockListActivity extends DatabaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnClickListener {

	static final int LOADER_ID_STOCK_LIST = 0;

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	String mSortOrderColumn = DatabaseContract.COLUMN_HOLD;
	String mSortOrderDirection = DatabaseContract.ORDER_DESC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	ListView mListView = null;
	CustomSimpleCursorAdapter mAdapter = null;

	TextView mTextViewNameCode = null;
	TextView mTextViewPrice = null;
	TextView mTextViewHold = null;
	TextView mTextViewFavorite = null;
	TextView mTextViewComponent = null;

	String[] mFrom = new String[]{DatabaseContract.COLUMN_NAME,
			DatabaseContract.COLUMN_CODE, DatabaseContract.COLUMN_PRICE,
			DatabaseContract.COLUMN_HOLD};
	int[] mTo = new int[]{R.id.name, R.id.code, R.id.price, R.id.hold};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_list_edit);

		mSortOrder = getIntent().getStringExtra(
				Constant.EXTRA_STOCK_LIST_SORT_ORDER);
		if (mSortOrder == null) {
			mSortOrder = mSortOrderDefault;
		}

		initHeader();

		mListView = findViewById(R.id.stock_list_edit_view);

		mAdapter = new CustomSimpleCursorAdapter(this,
				R.layout.activity_stock_list_edit_item, null, mFrom, mTo, 0);

		if ((mListView != null) && (mAdapter != null)) {
			mListView.setAdapter(mAdapter);
			mListView.setOnItemClickListener(this);
		}

		mLoaderManager.initLoader(LOADER_ID_STOCK_LIST, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_list, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_new:
				Intent intent = new Intent(this, StockActivity.class);
				intent.setAction(Constant.ACTION_FAVORITE_STOCK_INSERT);
				startActivity(intent);
				return true;

			case R.id.action_search:
				startActivity(new Intent(this, StockSearchActivity.class));
				return true;

			case R.id.action_favorite_all:
				new Thread(new Runnable() {
					@Override
					public void run() {
						ArrayList<Stock> stockList = new ArrayList();
						mDatabaseManager.getStockList(null, stockList);
						for (Stock stock : stockList) {
							if (TextUtils.equals(stock.getClasses(), Stock.CLASS_A) && !stock.hasFlag(Stock.FLAG_FAVORITE)) {
								stock.addFlag(Stock.FLAG_FAVORITE);
								mStockManager.onAddFavorite(stock);
							}
						}
					}
				}).start();
				return true;

			case R.id.action_favorite_none:
				new Thread(new Runnable() {
					@Override
					public void run() {
						ArrayList<Stock> stockList = new ArrayList();
						mDatabaseManager.getStockList(null, stockList);
						for (Stock stock : stockList) {
							if (TextUtils.equals(stock.getClasses(), Stock.CLASS_A) && stock.hasFlag(Stock.FLAG_FAVORITE)) {
								stock.removeFlag(Stock.FLAG_FAVORITE);
								mStockManager.onRemoveFavorite(stock);
							}
						}
					}
				}).start();
				return true;

			case android.R.id.home:
				finish();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	String getSelection() {
		return null;
	}

	void restartLoader(Intent intent) {
		restartLoader();
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
	public void onClick(@NonNull View view) {

		int id = view.getId();

		resetHeaderTextColor();
		setHeaderTextColor(id, mHeaderTextHighlightColor);

		switch (id) {
			case R.id.stock_name_code:
				mSortOrderColumn = DatabaseContract.COLUMN_NAME;
				break;
			case R.id.price:
				mSortOrderColumn = DatabaseContract.COLUMN_PRICE;
				break;
			case R.id.hold:
				mSortOrderColumn = DatabaseContract.COLUMN_HOLD;
				break;
			case R.id.favorite:
				mSortOrderColumn = DatabaseContract.COLUMN_FLAG;
				break;

			default:
				mSortOrderColumn = DatabaseContract.COLUMN_CODE;
				break;
		}

		if (TextUtils.equals(mSortOrderDirection, DatabaseContract.ORDER_ASC)) {
			mSortOrderDirection = DatabaseContract.ORDER_DESC;
		} else {
			mSortOrderDirection = DatabaseContract.ORDER_ASC;
		}

		mSortOrder = mSortOrderColumn + mSortOrderDirection;

		restartLoader();
	}

	void setHeaderTextColor(int id, int color) {
		TextView textView = findViewById(id);
		setHeaderTextColor(textView, color);
	}

	void setHeaderTextColor(TextView textView, int color) {
		if (textView != null) {
			textView.setTextColor(color);
		}
	}

	void resetHeaderTextColor() {
		setHeaderTextColor(mTextViewNameCode, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPrice, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewHold, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewFavorite, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewComponent, mHeaderTextDefaultColor);
	}

	void initHeader() {
		mTextViewNameCode = findViewById(R.id.stock_name_code);
		if (mTextViewNameCode != null) {
			mTextViewNameCode.setOnClickListener(this);
		}

		mTextViewPrice = findViewById(R.id.price);
		if (mTextViewPrice != null) {
			mTextViewPrice.setOnClickListener(this);
		}

		mTextViewHold = findViewById(R.id.hold);
		if (mTextViewHold != null) {
			mTextViewHold.setOnClickListener(this);
		}

		mTextViewFavorite = findViewById(R.id.favorite);
		if (mTextViewFavorite != null) {
			mTextViewFavorite.setOnClickListener(this);
		}

		mTextViewComponent = findViewById(R.id.component);
		if (TextUtils.equals(mAction, Constant.ACTION_INDEX_COMPONENT_SELECT)) {
			mTextViewComponent.setVisibility(View.VISIBLE);
		} else {
			mTextViewComponent.setVisibility(View.GONE);
		}

		if (mSortOrder.contains(DatabaseContract.COLUMN_NAME)) {
			setHeaderTextColor(mTextViewNameCode, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PRICE)) {
			setHeaderTextColor(mTextViewPrice, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_HOLD)) {
			setHeaderTextColor(mTextViewHold, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_FLAG)) {
			setHeaderTextColor(mTextViewFavorite, mHeaderTextHighlightColor);
		} else {
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
	                        long id) {
		if (id <= Stock.INVALID_ID) {
			return;
		}

//		Intent intent = new Intent(StockListEditActivity.this,
//				StockDataChartListActivity.class);
//		intent.putExtra(Constant.EXTRA_STOCK_ID, id);
//		startActivity(intent);
	}

	public class CustomSimpleCursorAdapter extends SimpleCursorAdapter
			implements OnClickListener {

		public CustomSimpleCursorAdapter(Context context, int layout, Cursor c,
		                                 String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
		}

		@Override
		public void bindView(@NonNull View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();
			Stock stock = new Stock();

			stock.set(cursor);

			setViewText(holder.mTextViewName, stock.getName());
			setViewText(holder.mTextViewCode, stock.getCode());
			setViewText(holder.mTextViewPrice, String.valueOf(stock.getPrice()));
			setViewText(holder.mTextViewHold, String.valueOf(stock.getHold()));

			if (stock.hasFlag(Stock.FLAG_FAVORITE)) {
				holder.mImageViewFavorite
						.setImageResource(R.drawable.ic_favorite);
			} else {
				holder.mImageViewFavorite
						.setImageResource(R.drawable.ic_none_favorite);
			}

			if (stock.getHold() == 0) {
				holder.mImageViewDelete.setImageResource(R.drawable.ic_delete);
			} else {
				holder.mImageViewDelete
						.setImageResource(R.drawable.ic_undeletable);
			}

			holder.mImageViewFavorite.setTag(stock.getId());
			holder.mImageViewFavorite.setOnClickListener(this);

			holder.mImageViewDelete.setTag(stock.getId());
			holder.mImageViewDelete.setOnClickListener(this);

			holder.mImageViewgComponent.setTag(stock.getId());
			holder.mImageViewgComponent.setOnClickListener(this);

			if (TextUtils.equals(mAction, Constant.ACTION_INDEX_COMPONENT_SELECT)) {
				mStock.setId(stock.getId());
				mDatabaseManager.getStockById(mStock);

				IndexComponent indexComponent = new IndexComponent();
				indexComponent.setIndexCode(mIntent.getStringExtra(Constant.EXTRA_INDEX_CODE));
				indexComponent.setIndexName(mIntent.getStringExtra(Constant.EXTRA_INDEX_NAME));
				indexComponent.setIndexSE(mIntent.getStringExtra(Constant.EXTRA_INDEX_SE));
				indexComponent.setCode(mStock.getCode());
				indexComponent.setName(mStock.getName());
				indexComponent.setSE(mStock.getSE());

				if (mDatabaseManager.isIndexComponentExist(indexComponent)) {
					holder.mImageViewgComponent.setImageResource(R.drawable.ic_checked);
				} else {
					holder.mImageViewgComponent.setImageResource(R.drawable.ic_unchecked);
				}
			}

			super.bindView(view, context, cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = super.newView(context, cursor, parent);

			ViewHolder holder = new ViewHolder();

			holder.mTextViewName = view.findViewById(R.id.name);
			holder.mTextViewCode = view.findViewById(R.id.code);
			holder.mTextViewPrice = view.findViewById(R.id.price);
			holder.mTextViewHold = view.findViewById(R.id.hold);
			holder.mImageViewFavorite = view
					.findViewById(R.id.favorite);
			holder.mImageViewDelete = view
					.findViewById(R.id.delete);
			holder.mImageViewgComponent = view.findViewById(R.id.component);

			if (TextUtils.equals(mAction, Constant.ACTION_INDEX_COMPONENT_SELECT)) {
				holder.mImageViewgComponent.setVisibility(View.VISIBLE);
			} else {
				holder.mImageViewgComponent.setVisibility(View.GONE);
			}

			view.setTag(holder);

			return view;
		}

		@Override
		public void onClick(View view) {
			if (view == null) {
				return;
			}

			long stockId = (Long) view.getTag();
			Stock stock = new Stock();

			stock.setId(stockId);
			mDatabaseManager.getStockById(stock);

			try {
				switch (view.getId()) {
					case R.id.favorite:
						if (!stock.hasFlag(Stock.FLAG_FAVORITE)) {
							stock.addFlag(Stock.FLAG_FAVORITE);
							mStockManager.onAddFavorite(stock);
						} else {
							stock.removeFlag(Stock.FLAG_FAVORITE);
							mStockManager.onRemoveFavorite(stock);
						}
						break;

					case R.id.delete:
						if (stock.getHold() == 0) {
							final String stock_name = stock.getName();
							new AlertDialog.Builder(mContext)
									.setTitle(R.string.delete)
									.setMessage(getString(R.string.delete_confirm, stock_name))
									.setPositiveButton(R.string.ok,
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,
												                    int which) {
													mDatabaseManager.deleteStock(stockId);
													mDatabaseManager.deleteStockData(stockId);
													mDatabaseManager.deleteStockFinancial(stockId);
													mDatabaseManager.deleteShareBonus(stockId);
													mDatabaseManager.deleteTotalShare(stockId);
												}
											})
									.setNegativeButton(R.string.cancel,
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,
												                    int which) {
												}
											}).setIcon(android.R.drawable.ic_dialog_alert)
									.show();
						}
						break;

					case R.id.component:
						if (TextUtils.equals(mAction, Constant.ACTION_INDEX_COMPONENT_SELECT)) {
							mStock.setId(stock.getId());
							mDatabaseManager.getStockById(mStock);

							IndexComponent indexComponent = new IndexComponent();
							indexComponent.setIndexCode(mIntent.getStringExtra(Constant.EXTRA_INDEX_CODE));
							indexComponent.setIndexName(mIntent.getStringExtra(Constant.EXTRA_INDEX_NAME));
							indexComponent.setIndexSE(mIntent.getStringExtra(Constant.EXTRA_INDEX_SE));
							indexComponent.setCode(mStock.getCode());
							indexComponent.setName(mStock.getName());
							indexComponent.setSE(mStock.getSE());

							if (mDatabaseManager.isIndexComponentExist(indexComponent)) {
								mDatabaseManager.deleteIndexComponent(indexComponent);
							} else {
								indexComponent.setCreated(Utility.getCurrentDateTimeString());
								mDatabaseManager.insertIndexComponent(indexComponent);
							}
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
		public TextView mTextViewHold;
		public ImageView mImageViewFavorite;
		public ImageView mImageViewDelete;
		public ImageView mImageViewgComponent;
	}
}