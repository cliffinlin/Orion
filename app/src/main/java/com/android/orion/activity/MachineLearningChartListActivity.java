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
import com.android.orion.ai.ml.learning.HousePrices;
import com.android.orion.chart.MachineLearningChart;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
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

public class MachineLearningChartListActivity extends BaseActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	public static final int ITEM_VIEW_TYPE_MAIN = 0;
	public static final int ITEM_VIEW_TYPE_SUB = 1;
	public static final int LOADER_ID_LIST = 0;
	public static final int MESSAGE_REFRESH = 0;

	String mSortOrder = DatabaseContract.COLUMN_VALUATION
			+ DatabaseContract.ORDER_DESC;

	String mDescription = "";
	Menu mMenu = null;
	ListView mListView = null;
	MachineLearningChartArrayAdapter mMachineLearningChartArrayAdapter = null;
	ArrayList<MachineLearningChartItem> mMachineLearningChartItemList = null;
	ArrayList<MachineLearningChartItemMain> mMachineLearningChartItemMainList = null;
	ArrayList<MachineLearningChart> mMachineLearningChartList = null;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
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
		// For chart init only
		Utils.init(this);
		// For chart init only

		setContentView(R.layout.activity_machine_learning_chart_list);

		initListView();
		initLoader();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		getMenuInflater().inflate(R.menu.machine_learning_chart, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;

			case R.id.action_refresh:
				mHandler.sendEmptyMessage(MESSAGE_REFRESH);
				return true;

			case R.id.action_setting:
				return true;

			default:
				return super.onOptionsItemSelected(item);
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

		mMachineLearningChartItemList.clear();

		mMachineLearningChartItemList.add(mMachineLearningChartItemMainList.get(0));

		restartLoader();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		if (id == LOADER_ID_LIST) {
			loader = getStockCursorLoader();
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

		if (id == LOADER_ID_LIST) {
			swapCursor(mMachineLearningChartList.get(0), cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = 0;

		if (loader == null) {
			return;
		}

		id = loader.getId();

		if (id == LOADER_ID_LIST) {
			swapCursor(mMachineLearningChartList.get(0), null);
		}
	}

	void initListView() {
		mListView = findViewById(R.id.listView);

		if (mMachineLearningChartList == null) {
			mMachineLearningChartList = new ArrayList<>();
		}

		if (mMachineLearningChartItemList == null) {
			mMachineLearningChartItemList = new ArrayList<>();
		}

		if (mMachineLearningChartItemMainList == null) {
			mMachineLearningChartItemMainList = new ArrayList<>();
		}

		mMachineLearningChartList.add(new MachineLearningChart());
		mMachineLearningChartItemMainList.add(new MachineLearningChartItemMain(
				mMachineLearningChartList.get(0)));
		mMachineLearningChartItemList.add(mMachineLearningChartItemMainList.get(0));

		mMachineLearningChartArrayAdapter = new MachineLearningChartArrayAdapter(this,
				mMachineLearningChartItemList);
		mListView.setAdapter(mMachineLearningChartArrayAdapter);
	}

	void initLoader() {
		mLoaderManager.initLoader(LOADER_ID_LIST, null, this);
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_LIST, null, this);
	}

	CursorLoader getStockCursorLoader() {
		String selection = DatabaseContract.COLUMN_CLASSES + " = '" + Stock.CLASS_A + "'";

		CursorLoader loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
				DatabaseContract.Stock.PROJECTION_ALL, selection, null,
				mSortOrder);

		return loader;
	}

	public void swapCursor(MachineLearningChart chart,
	                            Cursor cursor) {
		int index = 0;

		chart.clear();

		try {
			/*
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					Stock stock = new Stock();
					stock.set(cursor);

					index = chart.mXValues.size();
					chart.mXValues.add(stock.getName());

					BubbleEntry pointEntry = new BubbleEntry(index, (float) stock.getRoi(), 0);
					chart.mPointEntryList.add(pointEntry);

					Entry pointEntry = new Entry(index, (float) stock.getRoi());
//					chart.mPointEntryList.add(pointEntry);

					Entry lineEntry = new Entry(index, (float) stock.getRoe());
					chart.mLineEntryList.add(lineEntry);
				}
			}
			 */
			HousePrices housePrices = new HousePrices();
			housePrices.test();

			for (int i = 0; i < housePrices.xArray.length; i++) {
				index = chart.mXValues.size();
				chart.mXValues.add(String.valueOf(housePrices.xArray[i]));

				Entry pointEntry = new Entry((float) housePrices.yArray[i], index);
				chart.mPointEntryList.add(pointEntry);

				Entry lineEntry = new Entry((float) housePrices.trainer.predict(index), index);
				chart.mLineEntryList.add(lineEntry);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mDatabaseManager.closeCursor(cursor);
		}

		chart.setMainChartData();
		mMachineLearningChartArrayAdapter.notifyDataSetChanged();
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
		private final WeakReference<MachineLearningChartListActivity> mActivity;

		MainHandler(MachineLearningChartListActivity activity) {
			mActivity = new WeakReference<MachineLearningChartListActivity>(
					activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	}

	class MachineLearningChartItem {
		int mItemViewType;
		int mResource;
		MachineLearningChart mMachineLearningChart;

		public MachineLearningChartItem() {
		}

		public MachineLearningChartItem(int itemViewType, int resource,
		                           MachineLearningChart chart) {
			mItemViewType = itemViewType;
			mResource = resource;
			mMachineLearningChart = chart;
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

				viewHolder.mCombinedChart
						.setData(mMachineLearningChart.mCombinedDataMain);
				viewHolder.mCombinedChart.setDescription(mDescription);
			} else {
			}

			return view;
		}

		class ViewHolder {
			CombinedChart mCombinedChart;
			PieChart mPieChart;
		}
	}

	class MachineLearningChartItemMain extends MachineLearningChartItem {
		public MachineLearningChartItemMain(MachineLearningChart chart) {
			super(ITEM_VIEW_TYPE_MAIN,
					R.layout.activity_machine_learning_chart_list_item_main,
					chart);
		}
	}

	class MachineLearningChartArrayAdapter extends ArrayAdapter<MachineLearningChartItem> {

		public MachineLearningChartArrayAdapter(Context context,
		                                   List<MachineLearningChartItem> objects) {
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
			return mMachineLearningChartItemList.size();
		}
	}
}
