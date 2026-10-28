package com.android.orion.activity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.chart.StockStatisticsChart;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.constant.Constant;
import com.android.orion.setting.Setting;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultYAxisValueFormatter;
import com.github.mikephil.charting.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class StockStatisticsChartListActivity extends BaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	public static final int ITEM_VIEW_TYPE_MAIN = 0;
	public static final int ITEM_VIEW_TYPE_SUB = 1;
	public static final int LOADER_ID_STOCK_LIST = 0;

	int mStockListIndex = 0;

	String mSortOrder = DatabaseContract.COLUMN_VALUATION
			+ DatabaseContract.ORDER_DESC;

	float mTotalProfit = 0;
	float mTotalBonus = 0;
	String mDescription = "";
	Menu mMenu = null;
	ListView mListView = null;
	StatisticsChartArrayAdapter mStatisticsChartArrayAdapter = null;
	ArrayList<StatisticsChartItem> mStatisticsChartItemList = null;
	ArrayList<StatisticsChartItemMain> mStatisticsChartItemMainList = null;
	ArrayList<StatisticsChartItemSub> mStatisticsChartItemSubList = null;
	ArrayList<StockStatisticsChart> mStatisticsChartList = null;

	MainHandler mMainHandler = new MainHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// For chart init only
		Utils.init(this);
		// For chart init only

		setContentView(R.layout.activity_stock_statistics_chart_list);

		initListView();

		mStock.setId(getIntent().getLongExtra(Constant.EXTRA_STOCK_ID,
				DatabaseContract.INVALID_ID));

		initLoader();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		getMenuInflater().inflate(R.menu.stock_statistics_chart, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void handleOnOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_refresh:
				try {
					mStockDatabaseManager.loadStockArrayMap(mStockArrayMap);
					for (Stock stock : mStockArrayMap.values()) {
						mStockDatabaseManager.deleteStockFinancial(stock);
						mStockDatabaseManager.deleteStockBonus(stock);
						Setting.setDownloadStockTimeMillis(stock, 0);
						mStockDataProvider.download(stock);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case R.id.action_order_by_roi:
				mSortOrder = DatabaseContract.COLUMN_ROI
						+ DatabaseContract.ORDER_DESC;
				restartLoader();
				break;
			case R.id.action_order_by_roe:
				mSortOrder = DatabaseContract.COLUMN_ROE
						+ DatabaseContract.ORDER_DESC;
				restartLoader();
				break;
			case R.id.action_order_by_rate:
				mSortOrder = DatabaseContract.COLUMN_RATE
						+ DatabaseContract.ORDER_DESC;
				restartLoader();
				break;
			case R.id.action_order_by_pe:
				mSortOrder = DatabaseContract.COLUMN_PE
						+ DatabaseContract.ORDER_DESC;
				restartLoader();
				break;
			case R.id.action_order_by_yield:
				mSortOrder = DatabaseContract.COLUMN_YIELD
						+ DatabaseContract.ORDER_DESC;
				restartLoader();
				break;
			case R.id.action_order_by_dividend_ratio:
				mSortOrder = DatabaseContract.COLUMN_DIVIDEND_RATIO
						+ DatabaseContract.ORDER_DESC;
				restartLoader();
				break;
			case R.id.action_order_by_valuation:
				mSortOrder = DatabaseContract.COLUMN_VALUATION
						+ DatabaseContract.ORDER_DESC;
				restartLoader();
				break;
			default:
				super.handleOnOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	                                Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {

				default:
					break;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mStatisticsChartItemList.clear();

		mStatisticsChartItemList.add(mStatisticsChartItemMainList.get(0));
		mStatisticsChartItemList.add(mStatisticsChartItemSubList.get(0));

		restartLoader();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		if (id == LOADER_ID_STOCK_LIST) {
			loader = getStockCursorLoader();
		}

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader == null) {
			return;
		}

		int id = loader.getId();
		if (id == LOADER_ID_STOCK_LIST) {
			swapStockCursor(mStatisticsChartList.get(0), cursor);
		}

		mDescription = "Profit=" + mTotalProfit + ",  Bonus=" + mTotalBonus;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader == null) {
			return;
		}

		int id = loader.getId();

		if (id == LOADER_ID_STOCK_LIST) {
			swapStockCursor(mStatisticsChartList.get(0), null);
		}
	}

	void initListView() {
		mListView = findViewById(R.id.listView);

		if (mStatisticsChartList == null) {
			mStatisticsChartList = new ArrayList<>();
		}

		if (mStatisticsChartItemList == null) {
			mStatisticsChartItemList = new ArrayList<>();
		}

		if (mStatisticsChartItemMainList == null) {
			mStatisticsChartItemMainList = new ArrayList<>();
		}

		if (mStatisticsChartItemSubList == null) {
			mStatisticsChartItemSubList = new ArrayList<>();
		}

		mStatisticsChartList.add(new StockStatisticsChart());
		mStatisticsChartItemMainList.add(new StatisticsChartItemMain(
				mStatisticsChartList.get(0)));
		mStatisticsChartItemSubList.add(new StatisticsChartItemSub(
				mStatisticsChartList.get(0)));
		mStatisticsChartItemList.add(mStatisticsChartItemMainList.get(0));
		mStatisticsChartItemList.add(mStatisticsChartItemSubList.get(0));

		mStatisticsChartArrayAdapter = new StatisticsChartArrayAdapter(this,
				mStatisticsChartItemList);
		mListView.setAdapter(mStatisticsChartArrayAdapter);
	}

	void initLoader() {
		mLoaderManager.initLoader(LOADER_ID_STOCK_LIST, null, this);
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_STOCK_LIST, null, this);
	}

	CursorLoader getStockCursorLoader() {
		String selection = DatabaseContract.SELECTION_FLAG(Stock.FLAG_FAVORITE);

		CursorLoader loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
				DatabaseContract.Stock.PROJECTION_ALL, selection, null,
				mSortOrder);

		return loader;
	}

	void updateMenuAction() {
		if (mMenu == null) {
			return;
		}

		MenuItem actionPrev = mMenu.findItem(R.id.action_prev);
		MenuItem actionNext = mMenu.findItem(R.id.action_next);

		int size = mStockList.size();
		if (actionPrev != null) {
			actionPrev.setEnabled(size > 1);
		}

		if (actionNext != null) {
			actionNext.setEnabled(size > 1);
		}
	}

	public void swapStockCursor(StockStatisticsChart stockDataChart,
	                            Cursor cursor) {
		int index = 0;

		mTotalProfit = 0;
		mTotalBonus = 0;

		if (mStockList == null) {
			return;
		}

		stockDataChart.clear();

		mStockList.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					Stock stock = new Stock();
					stock.set(cursor);

					index = stockDataChart.mXValues.size();
					stockDataChart.mXValues.add(stock.getName());

					Entry roiEntry = new Entry((float) stock.getRoi(), index);
					stockDataChart.mRoiEntryList.add(roiEntry);

					Entry roeEntry = new Entry((float) stock.getRoe(), index);
					stockDataChart.mRoeEntryList.add(roeEntry);

					Entry rateEntry = new Entry((float) stock.getRate(), index);
					stockDataChart.mRateEntryList.add(rateEntry);

					BarEntry peEntry = new BarEntry((float) stock.getPe(),
							index);
					stockDataChart.mPeEntryList.add(peEntry);

					Entry yieldEntry = new Entry((float) stock.getYield(),
							index);
					stockDataChart.mYieldEntryList.add(yieldEntry);

					Entry dividendRatioEntry = new Entry(
							(float) stock.getDividendRatio(), index);
					stockDataChart.mDividendRatioEntryList
							.add(dividendRatioEntry);

					Entry valuationEntry = new Entry(
							(float) stock.getValuation(), index);
					stockDataChart.mValuationEntryList.add(valuationEntry);

					mTotalProfit += (float) stock.getProfit();
					mTotalBonus += (float) stock.getBonusInYear();

					if (stock != null) {
						if (mStock.getId() == stock.getId()) {
							mStock.set(cursor);
							mStockListIndex = mStockList.size();
							if (mMainHandler != null) {
								mMainHandler.sendEmptyMessage(0);
							}
						}
						mStockList.add(stock);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}

		stockDataChart.setMainChartData();
		stockDataChart.setSubChartData();

		mStatisticsChartArrayAdapter.notifyDataSetChanged();

		if (mMainHandler != null) {
			mMainHandler.sendEmptyMessage(0);
		}
	}

	@Override
	public void onAnalyzeStart(String stockCode) {
		restartLoader();
	}

	@Override
	public void onAnalyzeFinish(String stockCode) {
		restartLoader();
	}

	@Override
	public void onDownloadStart(String stockCode) {
		restartLoader();
	}

	@Override
	public void onDownloadComplete(String stockCode) {
		restartLoader();
	}

	static class MainHandler extends Handler {
		private final WeakReference<StockStatisticsChartListActivity> mActivity;

		MainHandler(StockStatisticsChartListActivity activity) {
			mActivity = new WeakReference<StockStatisticsChartListActivity>(
					activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			StockStatisticsChartListActivity activity = mActivity.get();
			activity.updateMenuAction();
		}
	}

	class StatisticsChartItem {
		int mItemViewType;
		int mResource;
		StockStatisticsChart mStatisticsChart;

		public StatisticsChartItem() {
		}

		public StatisticsChartItem(int itemViewType, int resource,
		                           StockStatisticsChart stockDataChart) {
			mItemViewType = itemViewType;
			mResource = resource;
			mStatisticsChart = stockDataChart;
		}

		public int getItemViewType() {
			return mItemViewType;
		}

		public View getView(int position, View view, Context context) {
			ViewHolder viewHolder = null;
			XAxis xAxis = null;
			YAxis leftYAxis = null;
			YAxis rightYAxis = null;

			// For android 5 and above solution:
			// if (view == null) {
			view = LayoutInflater.from(context).inflate(mResource, null);
			viewHolder = new ViewHolder();
			if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
				viewHolder.mCombinedChart = view
						.findViewById(R.id.chart);
			} else {
				viewHolder.mPieChart = view
						.findViewById(R.id.pie_chart);
			}
			view.setTag(viewHolder);
			// } else {
			// viewHolder = (ViewHolder) view.getTag();
			// }

			if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
				viewHolder.mCombinedChart.setBackgroundColor(Color.LTGRAY);
				viewHolder.mCombinedChart.setGridBackgroundColor(Color.LTGRAY);

				viewHolder.mCombinedChart.setMaxVisibleValueCount(0);

				xAxis = viewHolder.mCombinedChart.getXAxis();
				if (xAxis != null) {
					xAxis.setPosition(XAxisPosition.BOTTOM);
				}

				leftYAxis = viewHolder.mCombinedChart.getAxisLeft();
				if (leftYAxis != null) {
					leftYAxis.setPosition(YAxisLabelPosition.INSIDE_CHART);
					leftYAxis.setStartAtZero(false);
					leftYAxis.setValueFormatter(new DefaultYAxisValueFormatter(
							2));
					leftYAxis.removeAllLimitLines();
				}

				rightYAxis = viewHolder.mCombinedChart.getAxisRight();
				if (rightYAxis != null) {
					rightYAxis.setEnabled(false);
				}

				viewHolder.mCombinedChart
						.setData(mStatisticsChart.mCombinedDataMain);
				viewHolder.mCombinedChart.setDescription(mDescription);
			} else {
				viewHolder.mPieChart.setData(mStatisticsChart.mPieData);
				viewHolder.mPieChart.setRotationEnabled(false);
				viewHolder.mPieChart.setUsePercentValues(true);
				viewHolder.mPieChart.setDescription(mDescription);
			}

			return view;
		}

		class ViewHolder {
			CombinedChart mCombinedChart;
			PieChart mPieChart;
		}
	}

	class StatisticsChartItemMain extends StatisticsChartItem {
		public StatisticsChartItemMain(StockStatisticsChart stockDataChart) {
			super(ITEM_VIEW_TYPE_MAIN,
					R.layout.activity_stock_statistics_chart_list_item_main,
					stockDataChart);
		}
	}

	class StatisticsChartItemSub extends StatisticsChartItem {
		public StatisticsChartItemSub(StockStatisticsChart stockDataChart) {
			super(ITEM_VIEW_TYPE_SUB,
					R.layout.activity_stock_statistics_chart_list_item_sub,
					stockDataChart);
		}
	}

	class StatisticsChartArrayAdapter extends ArrayAdapter<StatisticsChartItem> {

		public StatisticsChartArrayAdapter(Context context,
		                                   List<StatisticsChartItem> objects) {
			super(context, 0, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getItem(position).getView(position, convertView,
					getContext());
		}

		@Override
		public int getItemViewType(int position) {
			return getItem(position).getItemViewType();
		}

		@Override
		public int getViewTypeCount() {
			return mStatisticsChartItemList.size();
		}
	}
}
