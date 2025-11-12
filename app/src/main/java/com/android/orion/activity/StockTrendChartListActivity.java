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
import android.os.Looper;
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
import com.android.orion.chart.StockTrendChart;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.StockPerceptron;
import com.android.orion.database.StockTrend;
import com.android.orion.provider.StockContentProvider;
import com.android.orion.provider.StockPerceptronProvider;
import com.android.orion.constant.Constant;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultYAxisValueFormatter;
import com.github.mikephil.charting.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class StockTrendChartListActivity extends BaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	static final int LOADER_ID_TREND_LIST = StockContentProvider.STOCK_TREND;

	static final int MESSAGE_INIT_LOADER = 100;
	static final int MESSAGE_RESTART_LOADER = 110;
	static final int MESSAGE_REFRESH = 200;

	static final int ITEM_VIEW_TYPE_MAIN = 0;
	static final int ITEM_VIEW_TYPE_SUB = 1;

	String mSortOrder = DatabaseContract.COLUMN_PRICE
			+ DatabaseContract.ORDER_ASC;

	String mDescription = "";
	StockPerceptron mStockPerceptron = new StockPerceptron();
	Menu mMenu = null;
	ListView mListView = null;
	StockTrendChartArrayAdapter mStockTrendChartArrayAdapter = null;
	ArrayList<StockTrendChartItem> mStockTrendChartItemList = null;
	ArrayList<StockTrendChartItemMain> mStockTrendChartItemMainList = null;
	ArrayList<StockTrendChart> mStockTrendChartList = null;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case MESSAGE_INIT_LOADER:
					mStockPerceptron.setId(mIntent.getLongExtra(Constant.EXTRA_STOCK_PERCEPTRON_ID,
							DatabaseContract.INVALID_ID));
					mStockDatabaseManager.getStockPerceptronById(mStockPerceptron);
					mStockPerceptron = StockPerceptronProvider.getInstance().getStockPerceptron(mStockPerceptron.getPeriod(), mStockPerceptron.getLevel(), mStockPerceptron.getType());
					mDescription = mStockPerceptron.toDescriptionString();
					mLoaderManager.initLoader(LOADER_ID_TREND_LIST, null, StockTrendChartListActivity.this);
					break;
				case MESSAGE_RESTART_LOADER:
					mLoaderManager.restartLoader(LOADER_ID_TREND_LIST, null, StockTrendChartListActivity.this);
					break;
				case MESSAGE_REFRESH:
					break;

				default:
					break;
			}
		}
	};

	MainHandler mMainHandler = new MainHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stock_trend_chart_list);

		// For chart init only
		Utils.init(this);
		// For chart init only

		initListView();
		initLoader();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		getMenuInflater().inflate(R.menu.stock_trend_chart, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void handleOnOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_refresh:
				mHandler.sendEmptyMessage(MESSAGE_REFRESH);
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

		mStockTrendChartItemList.clear();

		mStockTrendChartItemList.add(mStockTrendChartItemMainList.get(0));

		restartLoader();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		if (id == LOADER_ID_TREND_LIST) {
			loader = getCursorLoader();
		}

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int id = 0;

		if (loader == null) {
			return;
		}

		id = loader.getId();

		if (id == LOADER_ID_TREND_LIST) {
			swapCursor(mStockTrendChartList.get(0), cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = 0;

		if (loader == null) {
			return;
		}

		id = loader.getId();

		if (id == LOADER_ID_TREND_LIST) {
			swapCursor(mStockTrendChartList.get(0), null);
		}
	}

	void initListView() {
		mListView = findViewById(R.id.listView);

		if (mStockTrendChartList == null) {
			mStockTrendChartList = new ArrayList<>();
		}

		if (mStockTrendChartItemList == null) {
			mStockTrendChartItemList = new ArrayList<>();
		}

		if (mStockTrendChartItemMainList == null) {
			mStockTrendChartItemMainList = new ArrayList<>();
		}

		mStockTrendChartList.add(new StockTrendChart());
		mStockTrendChartItemMainList.add(new StockTrendChartItemMain(
				mStockTrendChartList.get(0)));
		mStockTrendChartItemList.add(mStockTrendChartItemMainList.get(0));

		mStockTrendChartArrayAdapter = new StockTrendChartArrayAdapter(this,
				mStockTrendChartItemList);
		mListView.setAdapter(mStockTrendChartArrayAdapter);
	}

	void initLoader() {
		Log.d("initLoader");
		mHandler.sendEmptyMessage(MESSAGE_INIT_LOADER);
	}

	void restartLoader() {
		mHandler.sendEmptyMessage(MESSAGE_RESTART_LOADER);
	}

	CursorLoader getCursorLoader() {
		String selection = DatabaseContract.COLUMN_PERIOD + " = '" + mStockPerceptron.getPeriod() + "'"
				+ " AND " + DatabaseContract.COLUMN_LEVEL + " = " + mStockPerceptron.getLevel()
				+ " AND " + DatabaseContract.COLUMN_TYPE + " = '" + mStockPerceptron.getType() + "'";

		CursorLoader loader = new CursorLoader(this, DatabaseContract.StockTrend.CONTENT_URI,
				DatabaseContract.StockTrend.PROJECTION_ALL, selection, null,
				mSortOrder);

		return loader;
	}

	public void swapCursor(StockTrendChart chart, Cursor cursor) {
		int index = 0;

		chart.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockTrend stockTrend = new StockTrend();
					stockTrend.set(cursor);

					index = chart.mXValues.size();
					chart.mXValues.add(String.valueOf(stockTrend.getName()));

					Entry pointEntry = new Entry((float) stockTrend.getNet(), index);
					chart.mPointEntryList.add(pointEntry);

					Entry lineEntry = new Entry((float) stockTrend.getPredict(), index);
					chart.mLineEntryList.add(lineEntry);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}

		chart.setMainChartData();
		mStockTrendChartArrayAdapter.notifyDataSetChanged();
		if (mMainHandler != null) {
			mMainHandler.sendEmptyMessage(0);
		}
	}

	@Override
	public void onAnalyzeStart(String stockCode) {
//		restartLoader();
	}

	@Override
	public void onAnalyzeFinish(String stockCode) {
		restartLoader();
	}

	@Override
	public void onDownloadStart(String stockCode) {
//		restartLoader();
	}

	@Override
	public void onDownloadComplete(String stockCode) {
//		restartLoader();
	}

	static class MainHandler extends Handler {
		private final WeakReference<StockTrendChartListActivity> mActivity;

		MainHandler(StockTrendChartListActivity activity) {
			mActivity = new WeakReference<StockTrendChartListActivity>(
					activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	}

	class StockTrendChartItem {
		int mItemViewType;
		int mResource;
		StockTrendChart mStockTrendChart;

		public StockTrendChartItem() {
		}

		public StockTrendChartItem(int itemViewType, int resource,
		                           StockTrendChart chart) {
			mItemViewType = itemViewType;
			mResource = resource;
			mStockTrendChart = chart;
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
//					xAxis.setAxisMinimum(0);
				}

				leftYAxis = viewHolder.mCombinedChart.getAxisLeft();
				if (leftYAxis != null) {
//					leftYAxis.setAxisMinimum(0);
					leftYAxis.setValueFormatter(new DefaultYAxisValueFormatter(2));
				}

				rightYAxis = viewHolder.mCombinedChart.getAxisRight();
				if (rightYAxis != null) {
					rightYAxis.setEnabled(false);
				}

				viewHolder.mCombinedChart.setDescription(mDescription);
				viewHolder.mCombinedChart
						.setData(mStockTrendChart.mCombinedDataMain);
			} else {
			}

			return view;
		}

		class ViewHolder {
			CombinedChart mCombinedChart;
			PieChart mPieChart;
		}
	}

	class StockTrendChartItemMain extends StockTrendChartItem {
		public StockTrendChartItemMain(StockTrendChart chart) {
			super(ITEM_VIEW_TYPE_MAIN,
					R.layout.activity_stock_trend_chart_list_item_main,
					chart);
		}
	}

	class StockTrendChartArrayAdapter extends ArrayAdapter<StockTrendChartItem> {

		public StockTrendChartArrayAdapter(Context context,
		                                   List<StockTrendChartItem> objects) {
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
			return mStockTrendChartItemList.size();
		}
	}
}
