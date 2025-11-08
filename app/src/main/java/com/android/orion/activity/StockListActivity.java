package com.android.orion.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.orion.R;
import com.android.orion.adapter.StockListAdapter;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Symbol;

import java.util.ArrayList;

public class StockListActivity extends StorageActivity implements View.OnClickListener,
		StockListAdapter.OnStockClickListener {

	static final int mHeaderTextDefaultColor = Color.BLACK;
	static final int mHeaderTextHighlightColor = Color.RED;

	String mSortOrderColumn = DatabaseContract.COLUMN_HOLD;
	String mSortOrderDirection = DatabaseContract.ORDER_DESC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	TextView mTextViewNameCode = null;
	TextView mTextViewPrice = null;
	TextView mTextViewHold = null;
	TextView mTextViewFavorite = null;

	RecyclerView mRecyclerView;
	StockListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_list);

		initHeader();
		setupRecyclerView();
		loadStockList();
	}

	@Override
	public void handleOnCreate(Bundle savedInstanceState) {
		super.handleOnCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		resetHeaderTextColor();
		initHeader();
		loadStockList(); // 在onResume中重新加载数据
	}

	@Override
	public void handleOnResume() {
		super.handleOnResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_list, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void handleOnOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_new:
				Intent intent = new Intent(this, StockActivity.class);
				intent.setAction(Constant.ACTION_FAVORITE_STOCK_INSERT);
				startActivity(intent);
				break;
			case R.id.action_refresh:
				try {
					mStockDatabaseManager.loadStockArrayMap(mStockArrayMap);
					for (Stock stock : mStockArrayMap.values()) {
						mStockDatabaseManager.deleteStockData(stock);
						mStockDatabaseManager.deleteStockTrend(stock);
						mStockDatabaseManager.deleteStockPerceptron(stock.getId());
						Setting.setDownloadStockDataTimeMillis(stock, 0);
						mStockDataProvider.download(stock);
					}
					loadStockList(); // 刷新后重新加载数据
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case R.id.action_load:
				performLoadFromFile(Constant.FILE_TYPE_FAVORITE, false);
				loadStockList(); // 加载文件后刷新列表
				break;
			case R.id.action_save:
				performSaveToFile(Constant.FILE_TYPE_FAVORITE);
				break;
			case R.id.action_favorite_all:
				handleFavoriteAll();
				break;
			case R.id.action_favorite_hold:
				handleFavoriteHold();
				break;
			case R.id.action_favorite_none:
				handleFavoriteNone();
				break;
			default:
				super.handleOnOptionsItemSelected(item);
		}
	}

	private void handleFavoriteAll() {
		try {
			ArrayList<Stock> stockList = new ArrayList<>();
			mStockDatabaseManager.getStockList(DatabaseContract.SELECTION_CLASSES(Stock.CLASS_A), null, stockList);
			onFavoriteChanged(stockList, true);
			mRecyclerView.postDelayed(() -> loadStockList(), 100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleFavoriteHold() {
		try {
			ArrayList<Stock> stockList = new ArrayList<>();
			mStockDatabaseManager.getStockList(DatabaseContract.SELECTION_HOLD(), null, stockList);
			onFavoriteChanged(stockList, true);
			mRecyclerView.postDelayed(() -> loadStockList(), 100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleFavoriteNone() {
		try {
			ArrayList<Stock> stockList = new ArrayList<>();
			mStockDatabaseManager.getStockList(DatabaseContract.SELECTION_CLASSES(Stock.CLASS_A), null, stockList);
			onFavoriteChanged(stockList, false);
			mRecyclerView.postDelayed(() -> loadStockList(), 100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void onFavoriteChanged(ArrayList<Stock> stockList, boolean addFavorite) {
		if (stockList == null) {
			return;
		}
		for (Stock stock : stockList) {
			if (addFavorite && !stock.hasFlag(Stock.FLAG_FAVORITE)) {
				stock.addFlag(Stock.FLAG_FAVORITE);
				mStockManager.onAddFavorite(stock);
			} else if (!addFavorite && stock.hasFlag(Stock.FLAG_FAVORITE)) {
				stock.removeFlag(Stock.FLAG_FAVORITE);
				mStockManager.onRemoveFavorite(stock);
			}
		}
	}

	@Override
	public void onClick(@NonNull View view) {
		int viewId = view.getId();

		resetHeaderTextColor();
		setHeaderTextColor(viewId, mHeaderTextHighlightColor);

		String oldSortOrderColumn = mSortOrderColumn;

		switch (viewId) {
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

		if (TextUtils.equals(mSortOrderColumn, oldSortOrderColumn)) {
			if (TextUtils.equals(mSortOrderDirection, DatabaseContract.ORDER_ASC)) {
				mSortOrderDirection = DatabaseContract.ORDER_DESC;
			} else {
				mSortOrderDirection = DatabaseContract.ORDER_ASC;
			}
		} else {
			mSortOrderDirection = DatabaseContract.ORDER_DESC;
		}

		mSortOrder = mSortOrderColumn + Symbol.WHITE_SPACE + mSortOrderDirection; // 确保有空格
		Preferences.putString(Setting.SETTING_SORT_ORDER_STOCK_LIST, mSortOrder);
		loadStockList(); // 排序后重新加载数据
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
	}

	void initHeader() {
		// 从偏好设置加载排序设置
		mSortOrder = Preferences.getString(Setting.SETTING_SORT_ORDER_STOCK_LIST, mSortOrderDefault);
		if (!TextUtils.isEmpty(mSortOrder)) {
			String[] strings = mSortOrder.split(Symbol.WHITE_SPACE);
			if (strings != null && strings.length > 1) {
				mSortOrderColumn = strings[0];
				mSortOrderDirection = strings[1];
			}
			Log.d("StockList" + "从偏好设置加载排序: " + mSortOrder + ", 字段=" + mSortOrderColumn + ", 方向=" + mSortOrderDirection);
		}

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

		// 设置当前排序字段的高亮颜色
		if (mSortOrder.contains(DatabaseContract.COLUMN_NAME)) {
			setHeaderTextColor(mTextViewNameCode, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PRICE)) {
			setHeaderTextColor(mTextViewPrice, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_HOLD)) {
			setHeaderTextColor(mTextViewHold, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_FLAG)) {
			setHeaderTextColor(mTextViewFavorite, mHeaderTextHighlightColor);
		}
	}

	void setupRecyclerView() {
		mRecyclerView = findViewById(R.id.recycler_view_stock_list);
		mAdapter = new StockListAdapter(this, this);

		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.setAdapter(mAdapter);
	}

	void loadStockList() {
		try {
			mStockList.clear();
			// 使用你自己的数据库管理器获取数据
			mStockDatabaseManager.getStockList(getSelection(), mSortOrder, mStockList);
			mAdapter.setStockList(mStockList);
		} catch (Exception e) {
			e.printStackTrace();
			mAdapter.setStockList(new ArrayList<Stock>());
		}
	}

	String getSelection() {
		// 可以根据需要修改筛选条件
		// 这里返回null获取所有股票，或者使用原来的筛选条件
		return null;
	}

	// StockListAdapter.OnStockClickListener 接口实现
	@Override
	public void onStockClick(Stock stock) {
		// 处理股票项点击事件
		if (stock != null) {
			// 可以在这里添加跳转到股票详情页面的逻辑
			// 例如：
			// Intent intent = new Intent(this, StockDetailActivity.class);
			// intent.putExtra("stock_id", stock.getId());
			// startActivity(intent);
		}
	}

	@Override
	public void onFavoriteClick(Stock stock) {
		// 处理收藏按钮点击
		try {
			if (!stock.hasFlag(Stock.FLAG_FAVORITE)) {
				stock.addFlag(Stock.FLAG_FAVORITE);
				mStockManager.onAddFavorite(stock);
			} else {
				stock.removeFlag(Stock.FLAG_FAVORITE);
				mStockManager.onRemoveFavorite(stock);
			}

			// 更新数据库
//			mStockDatabaseManager.updateStock(stock);

			// 直接更新单个item，避免整个列表刷新
			int position = findStockPosition(stock);
			if (position != -1) {
				mAdapter.notifyItemChanged(position);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDeleteClick(Stock stock) {
		// 处理删除按钮点击
		if (stock.getHold() == 0) {
			final String stockName = stock.getName();
			new AlertDialog.Builder(this)
					.setTitle(R.string.delete)
					.setMessage(getString(R.string.delete_confirm, stockName))
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									try {
										mStockDatabaseManager.deleteStock(stock.getId());
										mStockDatabaseManager.deleteTDXData(stock);
										mStockDatabaseManager.deleteStockData(stock);
										mStockDatabaseManager.deleteStockFinancial(stock);
										mStockDatabaseManager.deleteStockBonus(stock);
										mStockDatabaseManager.deleteStockShare(stock);
										mStockDatabaseManager.deleteStockTrend(stock);
										Setting.setDownloadStockTimeMillis(stock, 0);
										Setting.setDownloadStockDataTimeMillis(stock, 0);
										loadStockList(); // 删除后重新加载数据
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								}
							}).setIcon(android.R.drawable.ic_dialog_alert)
					.show();
		}
	}

	/**
	 * 在列表中查找股票的位置
	 */
	private int findStockPosition(Stock stock) {
		for (int i = 0; i < mStockList.size(); i++) {
			if (mStockList.get(i).getId() == stock.getId()) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 刷新数据（供外部调用）
	 */
	public void refreshStockList() {
		loadStockList();
	}
}