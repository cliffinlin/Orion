package com.android.orion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.regression.SimpleRegression;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Setting;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.database.StockMatch;
import com.android.orion.utility.Utility;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultYAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.Utils;

public class StockMatchChartListActivity extends StorageActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnChartGestureListener {

	public static final String EXTRA_STOCK_MATCH_ID = "stock_match_id";

	static final int ITEM_VIEW_TYPE_MAIN = 0;
	static final int ITEM_VIEW_TYPE_SUB = 1;
	static final int STOCK_PERIOD_ARRAY_SIZE = 7;
	static final int LOADER_ID_STOCK_MATCH_LIST = STOCK_PERIOD_ARRAY_SIZE + 1;
	static final int FLING_DISTANCE = 50;
	static final int FLING_VELOCITY = 100;

	static final int EXECUTE_LOAD_STOCK_MATCH_LIST = 0;
	static final int EXECUTE_LOAD_STOCK_DATA = 1;

	static final long RESULT_LOAD_STOCK_MATCH_LIST_SUCCESS = RESULT_SUCCESS + 1;

	int mStockMatchListIndex = 0;

	Stock mStock_X;
	Stock mStock_Y;
	StockMatch mStockMatch;

	ArrayList<StockData> mStockDataList_X = null;
	ArrayList<StockData> mStockDataList_Y = null;
	ArrayList<StockMatch> mStockMatchList = null;

	Menu mMenu = null;
	SparseArray<String> mPeriodArray = null;

	String mSortOrder = null;

	ListView mListView = null;
	StockMatchChartArrayAdapter mStockMatchChartArrayAdapter = null;
	ArrayList<StockMatchChartItem> mStockMatchChartItemList = null;
	ArrayList<StockMatchChartItemMain> mStockMatchChartItemMainList = null;
	ArrayList<StockMatchChartItemSub> mStockMatchChartItemSubList = null;
	ArrayList<StockMatchChartData> mStockMatchChartDataList = null;

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mResumed) {

				int serviceType = intent.getIntExtra(
						Constants.EXTRA_KEY_SERVICE_TYPE,
						Constants.SERVICE_TYPE_NONE);

				if ((serviceType == Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE_REALTIME)
						|| (serviceType == Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE_DATA_HISTORY)
						|| (serviceType == Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE_DATA_REALTIME)) {
					if (intent.getLongExtra(Constants.EXTRA_KEY_STOCK_ID, 0) == mStock_X
							.getId()
							|| intent.getLongExtra(
									Constants.EXTRA_KEY_STOCK_ID, 0) == mStock_Y
									.getId()) {
						// restartLoader();
					}
				}
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

		setContentView(R.layout.activity_stock_match_chart_list);

		fillPeriodArray();

		initListView();

		if (mStock_X == null) {
			mStock_X = Stock.obtain();
		}

		if (mStock_Y == null) {
			mStock_Y = Stock.obtain();
		}

		if (mStockMatch == null) {
			mStockMatch = StockMatch.obtain();
		}

		if (mStockDataList_X == null) {
			mStockDataList_X = new ArrayList<StockData>();
		}

		if (mStockDataList_Y == null) {
			mStockDataList_Y = new ArrayList<StockData>();
		}

		if (mStockMatchList == null) {
			mStockMatchList = new ArrayList<StockMatch>();
		}

		mStockMatch.setId(getIntent().getLongExtra(EXTRA_STOCK_MATCH_ID, 0));

		mSortOrder = getIntent().getStringExtra(
				Setting.KEY_SORT_ORDER_STOCK_LIST);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver,
				new IntentFilter(Constants.ACTION_SERVICE_FINISHED));

		startLoadTask(EXECUTE_LOAD_STOCK_MATCH_LIST);

		updateTitle();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		getMenuInflater().inflate(R.menu.stock_chart, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			return true;
		}
		case R.id.action_deal: {
			Bundle bundle = new Bundle();
			bundle.putString(Constants.EXTRA_KEY_STOCK_SE, mStock.getSE());
			bundle.putString(Constants.EXTRA_KEY_STOCK_CODE, mStock.getCode());
			Intent intent = new Intent(this, StockDealListActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
			return true;
		}
		case R.id.action_prev: {
			navigateStockMatch(-1);
			return true;
		}
		case R.id.action_next: {
			navigateStockMatch(1);
			return true;
		}
		case R.id.action_clean_data: {
			deleteStockData(mStock.getId());
			startService(Constants.SERVICE_DOWNLOAD_STOCK_FAVORITE,
					Constants.EXECUTE_IMMEDIATE, mStock.getSE(),
					mStock.getCode());
			// restartLoader();
			return true;
		}
		case R.id.action_remove_favorite: {
			updateStockMark(mStock.getId(), Constants.STOCK_FLAG_NONE);
			if (mStockMatchListIndex < mStockMatchList.size()) {
				mStockMatchList.remove(mStockMatchListIndex);
			}
			startService(Constants.SERVICE_REMOVE_STOCK_FAVORITE,
					Constants.EXECUTE_IMMEDIATE);
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mStockMatchChartItemList.clear();

		for (int i = 0; i < STOCK_PERIOD_ARRAY_SIZE; i++) {
			if (Utility.getSettingBoolean(this, mPeriodArray.get(i))) {
				mStockMatchChartItemList.add(mStockMatchChartItemMainList
						.get(i));
				mStockMatchChartItemList
						.add(mStockMatchChartItemSubList.get(i));
			}
		}

		// restartLoader();
		startLoadTask(EXECUTE_LOAD_STOCK_MATCH_LIST);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mBroadcastReceiver);
	}

	Long doInBackgroundLoad(Object... params) {
		super.doInBackgroundSave(params);

		int execute = (Integer) params[0];
		int index = 0;

		Cursor cursor = null;
		String selection = null;
		String sortOrder = null;

		if ((mStockMatchList == null) || (mStockData == null)) {
			return RESULT_FAILURE;
		}

		if (execute == EXECUTE_LOAD_STOCK_MATCH_LIST) {
			mStockMatchList.clear();

			try {
				cursor = mStockDatabaseManager.queryStockMatch(selection, null,
						null);

				if ((cursor != null) && (cursor.getCount() > 0)) {
					while (cursor.moveToNext()) {
						StockMatch stockMatch = StockMatch.obtain();
						stockMatch.set(cursor);

						if (mStockMatch.getId() == stockMatch.getId()) {
							mStockMatch.set(cursor);
							mStockMatchListIndex = mStockMatchList.size();
						}

						mStockMatchList.add(stockMatch);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				mStockDatabaseManager.closeCursor(cursor);
			}

			mStock_X.setSE(mStockMatch.getSE_X());
			mStock_X.setCode(mStockMatch.getCode_X());
			mStockDatabaseManager.getStock(mStock_X);

			mStock_Y.setSE(mStockMatch.getSE_Y());
			mStock_Y.setCode(mStockMatch.getCode_Y());
			mStockDatabaseManager.getStock(mStock_Y);

			return RESULT_LOAD_STOCK_MATCH_LIST_SUCCESS;
		} else {
			for (int i = 0; i < STOCK_PERIOD_ARRAY_SIZE; i++) {
				if (!mPeriodArray.get(i).equals(Constants.PERIOD_DAY)) {
					continue;
				}

				getStockDataList(mStock_X.getId(), mPeriodArray.get(i),
						mStockDataList_X);
				getStockDataList(mStock_Y.getId(), mPeriodArray.get(i),
						mStockDataList_Y);

				if (mStockDataList_X.size() != mStockDataList_Y.size()) {
					continue;
				}

				if (mStockDataList_X.size() < Constants.STOCK_VERTEX_TYPING_SIZE) {
					continue;
				}

				setupStockMatchChat(mStockMatchChartDataList.get(i));
			}
		}

		return RESULT_SUCCESS;
	}

	void onPostExecuteLoad(Long result) {
		super.onPostExecuteLoad(result);

		if (result == RESULT_LOAD_STOCK_MATCH_LIST_SUCCESS) {
			startLoadTask(EXECUTE_LOAD_STOCK_DATA);
		} else {
			updateTitle();
			mStockMatchChartArrayAdapter.notifyDataSetChanged();
		}
	}

	void getStockDataList(long stockId, String period,
			ArrayList<StockData> stockDataList) {
		Cursor cursor = null;
		String selection = mStockDatabaseManager.getStockDataSelection(stockId,
				period);
		String sortOrder = mStockDatabaseManager.getStockDataOrder();

		if (stockDataList == null) {
			return;
		}

		stockDataList.clear();

		try {
			cursor = mStockDatabaseManager.queryStockData(selection, null,
					sortOrder);

			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockData stockData = StockData.obtain();
					stockData.set(cursor);
					stockDataList.add(stockData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mStockDatabaseManager.closeCursor(cursor);
		}
	}

	void setupStockMatchChat(StockMatchChartData stockMatchChartData) {
		int index = 0;

		double x = 0;
		double y = 0;
		double minX = 0;
		double maxX = 0;
		double slop = 0;
		double intercept = 0;
		double fitValue = 0;

		SimpleRegression simpleRegression = new SimpleRegression();

		simpleRegression.clear();

		minX = mStockDataList_X.get(0).getClose();
		maxX = mStockDataList_X.get(0).getClose();
		for (int i = 0; i < mStockDataList_X.size(); i++) {
			x = mStockDataList_X.get(i).getClose();
			y = mStockDataList_Y.get(i).getClose();
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);

			simpleRegression.addData(x, y);
		}

		slop = simpleRegression.getSlope();
		intercept = simpleRegression.getIntercept();

		stockMatchChartData.clear();

		for (int i = 0; i < mStockDataList_X.size(); i++) {
			StockData stockData_X = mStockDataList_X.get(i);
			StockData stockData_Y = mStockDataList_Y.get(i);

			index = stockMatchChartData.mXValues.size();

			stockMatchChartData.mXValues.add(Double.toString(stockData_X
					.getClose()));

			Entry drawEntry = new Entry((float) stockData_Y.getClose(), index);
			stockMatchChartData.mDrawEntryList.add(drawEntry);

			x = minX + i * (maxX - minX) / (mStockDataList_X.size() - 1);
			fitValue = x * slop + intercept;
			Entry fitEntry = new Entry((float) fitValue, index);
			stockMatchChartData.mFitEntryList.add(fitEntry);
		}

		stockMatchChartData.updateDescription(mStock);
		stockMatchChartData.setMainChartData();
		stockMatchChartData.setSubChartData();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		CursorLoader loader = null;

		if (id == LOADER_ID_STOCK_MATCH_LIST) {
			loader = getStockMatchCursorLoader();
		} else {
			loader = getStockMatchDataCursorLoader(mPeriodArray.get(id));
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

		if (id == LOADER_ID_STOCK_MATCH_LIST) {
			swapStockMatchCursor(cursor);
		} else {
			swapStockMatchDataCursor(mStockMatchChartDataList.get(id), cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = 0;

		if (loader == null) {
			return;
		}

		id = loader.getId();

		if (id == LOADER_ID_STOCK_MATCH_LIST) {
			swapStockMatchCursor(null);
		} else {
			swapStockMatchDataCursor(mStockMatchChartDataList.get(id), null);
		}
	}

	@Override
	public void onChartLongPressed(MotionEvent me) {
	}

	@Override
	public void onChartDoubleTapped(MotionEvent me) {
	}

	@Override
	public void onChartSingleTapped(MotionEvent me) {
	}

	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX,
			float velocityY) {
		int distance = FLING_DISTANCE;
		// int velocitx = FLING_VELOCITY;
		int velocity = FLING_VELOCITY;

		if (me1.getX() - me2.getX() > distance
				&& Math.abs(velocityX) > velocity) {
			navigateStockMatch(-1);
		}

		if (me2.getX() - me1.getX() > distance
				&& Math.abs(velocityX) > velocity) {
			navigateStockMatch(1);
		}
	}

	@Override
	public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
	}

	@Override
	public void onChartTranslate(MotionEvent me, float dX, float dY) {
	}

	@Override
	public void onChartGestureStart(MotionEvent me,
			ChartGesture lastPerformedGesture) {
	}

	@Override
	public void onChartGestureEnd(MotionEvent me,
			ChartGesture lastPerformedGesture) {
	}

	void fillPeriodArray() {
		if (mPeriodArray == null) {
			mPeriodArray = new SparseArray<String>();

			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_MONTH);
			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_WEEK);
			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_DAY);
			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_60MIN);
			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_30MIN);
			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_15MIN);
			mPeriodArray.put(mPeriodArray.size(), Constants.PERIOD_5MIN);
		}
	}

	void initListView() {
		mListView = (ListView) findViewById(R.id.listView);

		if (mStockMatchChartDataList == null) {
			mStockMatchChartDataList = new ArrayList<StockMatchChartData>();
		}

		if (mStockMatchChartItemList == null) {
			mStockMatchChartItemList = new ArrayList<StockMatchChartItem>();
		}

		if (mStockMatchChartItemMainList == null) {
			mStockMatchChartItemMainList = new ArrayList<StockMatchChartItemMain>();
		}

		if (mStockMatchChartItemSubList == null) {
			mStockMatchChartItemSubList = new ArrayList<StockMatchChartItemSub>();
		}

		for (int i = 0; i < STOCK_PERIOD_ARRAY_SIZE; i++) {
			mStockMatchChartDataList.add(new StockMatchChartData(mPeriodArray
					.get(i)));
			mStockMatchChartItemMainList.add(new StockMatchChartItemMain(
					mStockMatchChartDataList.get(i)));
			mStockMatchChartItemSubList.add(new StockMatchChartItemSub(
					mStockMatchChartDataList.get(i)));
			mStockMatchChartItemList.add(mStockMatchChartItemMainList.get(i));
			mStockMatchChartItemList.add(mStockMatchChartItemSubList.get(i));
		}

		mStockMatchChartArrayAdapter = new StockMatchChartArrayAdapter(this,
				mStockMatchChartItemList);
		mListView.setAdapter(mStockMatchChartArrayAdapter);
	}

	void initLoader() {
		mLoaderManager.initLoader(LOADER_ID_STOCK_MATCH_LIST, null, this);
		for (int i = 0; i < STOCK_PERIOD_ARRAY_SIZE; i++) {
			mLoaderManager.initLoader(i, null, this);
		}
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_STOCK_MATCH_LIST, null, this);
		for (int i = 0; i < STOCK_PERIOD_ARRAY_SIZE; i++) {
			mLoaderManager.restartLoader(i, null, this);
		}
	}

	CursorLoader getStockMatchCursorLoader() {
		String selection = "";
		CursorLoader loader = null;

		loader = new CursorLoader(this,
				DatabaseContract.StockMatch.CONTENT_URI,
				DatabaseContract.StockMatch.PROJECTION_ALL, selection, null,
				mSortOrder);

		return loader;
	}

	CursorLoader getStockMatchDataCursorLoader(String period) {
		String selection = "";
		String sortOrder = "";
		CursorLoader loader = null;

		selection = mStockDatabaseManager.getStockDataSelection(mStock.getId(),
				period);

		sortOrder = mStockDatabaseManager.getStockDataOrder();

		loader = new CursorLoader(this, DatabaseContract.StockData.CONTENT_URI,
				DatabaseContract.StockData.PROJECTION_ALL, selection, null,
				sortOrder);

		return loader;
	}

	void updateMenuAction() {
		int size = 0;
		if (mMenu == null) {
			return;
		}

		MenuItem actionPrev = mMenu.findItem(R.id.action_prev);
		MenuItem actionNext = mMenu.findItem(R.id.action_next);

		size = mStockMatchList.size();

		if (actionPrev != null) {
			if (size > 1) {
				actionPrev.setEnabled(true);
			} else {
				actionPrev.setEnabled(false);
			}
		}

		if (actionNext != null) {
			if (size > 1) {
				actionNext.setEnabled(true);
			} else {
				actionNext.setEnabled(false);
			}
		}
	}

	void updateTitle() {
		if (mStockMatch != null) {
			setTitle(mStockMatch.getName_X() + " " + mStockMatch.getName_Y());
		}
	}

	public void swapStockMatchCursor(Cursor cursor) {
		if (mStockMatchList == null) {
			return;
		}

		mStockMatchList.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					StockMatch stockMatch = StockMatch.obtain();
					stockMatch.set(cursor);

					if (stockMatch != null) {
						if (mStockMatch.getId() == stockMatch.getId()) {
							mStockMatch.set(cursor);

							mStockMatchListIndex = mStockMatchList.size();

							if (mMainHandler != null) {
								mMainHandler.sendEmptyMessage(0);
							}
						}
						mStockMatchList.add(stockMatch);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				if (!cursor.isClosed()) {
					cursor.close();
				}
			}
		}

		if (mMainHandler != null) {
			mMainHandler.sendEmptyMessage(0);
		}
	}

	public void swapStockMatchDataCursor(
			StockMatchChartData stockMatchChartData, Cursor cursor) {
		int index = 0;

		if (mStockData == null) {
			return;
		}

		stockMatchChartData.clear();

		try {
			if ((cursor != null) && (cursor.getCount() > 0)) {
				while (cursor.moveToNext()) {
					index = stockMatchChartData.mXValues.size();
					mStockData.set(cursor);

					stockMatchChartData.mXValues.add(mStockData.getDate());

					CandleEntry candleEntry = new CandleEntry(index,
							(float) mStockData.getHigh(),
							(float) mStockData.getLow(),
							(float) mStockData.getOpen(),
							(float) mStockData.getClose(),
							mStockData.getAction());
					stockMatchChartData.mCandleEntryList.add(candleEntry);

					if (mStockData.vertexOf(Constants.STOCK_VERTEX_TOP)) {
						Entry drawEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockMatchChartData.mDrawEntryList.add(drawEntry);
					} else if (mStockData
							.vertexOf(Constants.STOCK_VERTEX_BOTTOM)) {
						Entry drawEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockMatchChartData.mDrawEntryList.add(drawEntry);
					}

					if (mStockData.vertexOf(Constants.STOCK_VERTEX_TOP_STROKE)) {
						Entry strokeEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockMatchChartData.mStrokeEntryList.add(strokeEntry);
					} else if (mStockData
							.vertexOf(Constants.STOCK_VERTEX_BOTTOM_STROKE)) {
						Entry strokeEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockMatchChartData.mStrokeEntryList.add(strokeEntry);
					}

					if (mStockData.vertexOf(Constants.STOCK_VERTEX_TOP_SEGMENT)) {
						Entry segmentEntry = new Entry(
								(float) mStockData.getVertexHigh(), index);
						stockMatchChartData.mSegmentEntryList.add(segmentEntry);
					} else if (mStockData
							.vertexOf(Constants.STOCK_VERTEX_BOTTOM_SEGMENT)) {
						Entry segmentEntry = new Entry(
								(float) mStockData.getVertexLow(), index);
						stockMatchChartData.mSegmentEntryList.add(segmentEntry);
					}

					if ((mStockData.getOverlapHigh() > 0)
							&& (mStockData.getOverlapLow() > 0)) {
						Entry overlayHighEntry = new Entry(
								(float) mStockData.getOverlapHigh(), index);
						stockMatchChartData.mOverlapHighEntryList
								.add(overlayHighEntry);

						Entry overlapLowEntry = new Entry(
								(float) mStockData.getOverlapLow(), index);
						stockMatchChartData.mOverlapLowEntryList
								.add(overlapLowEntry);
					}

					Entry average5Entry = new Entry(
							(float) mStockData.getAverage5(), index);
					stockMatchChartData.mAverage5EntryList.add(average5Entry);

					Entry average10Entry = new Entry(
							(float) mStockData.getAverage10(), index);
					stockMatchChartData.mAverage10EntryList.add(average10Entry);

					Entry difEntry = new Entry((float) mStockData.getDIF(),
							index);
					stockMatchChartData.mDIFEntryList.add(difEntry);

					Entry deaEntry = new Entry((float) mStockData.getDEA(),
							index);
					stockMatchChartData.mDEAEntryList.add(deaEntry);

					BarEntry histogramBarEntry = new BarEntry(
							(float) mStockData.getHistogram(), index);
					stockMatchChartData.mHistogramEntryList
							.add(histogramBarEntry);

					Entry averageEntry = new Entry(
							(float) mStockData.getAverage(), index);
					stockMatchChartData.mAverageEntryList.add(averageEntry);

					Entry velocityEntry = new Entry(
							(float) mStockData.getVelocity(), index);
					stockMatchChartData.mVelocityEntryList.add(velocityEntry);

					Entry acclerateEntry = new Entry(
							(float) mStockData.getAcceleration(), index);
					stockMatchChartData.mAccelerateEntryList
							.add(acclerateEntry);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				if (!cursor.isClosed()) {
					cursor.close();
				}
			}
		}

		updateTitle();

		stockMatchChartData.updateDescription(mStock);
		stockMatchChartData.setMainChartData();
		stockMatchChartData.setSubChartData();

		mStockMatchChartArrayAdapter.notifyDataSetChanged();
	}

	void navigateStockMatch(int direction) {
		boolean loop = true;

		if ((mStockMatchList == null) || (mStockMatchList.size() == 0)) {
			return;
		}

		mStockMatchListIndex += direction;

		if (mStockMatchListIndex > mStockMatchList.size() - 1) {
			if (loop) {
				mStockMatchListIndex = 0;
			} else {
				mStockMatchListIndex = mStockMatchList.size() - 1;
			}
		}

		if (mStockMatchListIndex < 0) {
			if (loop) {
				mStockMatchListIndex = mStockMatchList.size() - 1;
			} else {
				mStockMatchListIndex = 0;
			}
		}

		mStockMatch = mStockMatchList.get(mStockMatchListIndex);

		// restartLoader();
	}

	static class MainHandler extends Handler {
		private final WeakReference<StockMatchChartListActivity> mActivity;

		MainHandler(StockMatchChartListActivity activity) {
			mActivity = new WeakReference<StockMatchChartListActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			StockMatchChartListActivity activity = mActivity.get();
			activity.updateTitle();
			activity.updateMenuAction();
		}
	}

	class StockMatchChartItem {
		int mItemViewType;
		int mResource;
		StockMatchChartData mStockMatchChartData;

		public StockMatchChartItem() {
		}

		public StockMatchChartItem(int itemViewType, int resource,
				StockMatchChartData stockMatchChartData) {
			mItemViewType = itemViewType;
			mResource = resource;
			mStockMatchChartData = stockMatchChartData;
		}

		public int getItemViewType() {
			return mItemViewType;
		}

		public View getView(int position, View view, Context context) {
			ViewHolder viewHolder = null;
			XAxis xAxis = null;
			YAxis leftAxis = null;
			YAxis rightAxis = null;

			if (view == null) {
				view = LayoutInflater.from(context).inflate(mResource, null);
				viewHolder = new ViewHolder();
				viewHolder.chart = (CombinedChart) view
						.findViewById(R.id.chart);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			viewHolder.chart.setBackgroundColor(Color.LTGRAY);
			viewHolder.chart.setGridBackgroundColor(Color.LTGRAY);

			viewHolder.chart.setMaxVisibleValueCount(0);

			xAxis = viewHolder.chart.getXAxis();
			if (xAxis != null) {
				xAxis.setPosition(XAxisPosition.BOTTOM);
			}

			leftAxis = viewHolder.chart.getAxisLeft();
			if (leftAxis != null) {
				leftAxis.setPosition(YAxisLabelPosition.INSIDE_CHART);
				leftAxis.setStartAtZero(false);
				leftAxis.setValueFormatter(new DefaultYAxisValueFormatter(2));
				leftAxis.removeAllLimitLines();
				if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
					for (int i = 0; i < mStockMatchChartData.mLimitLineList
							.size(); i++) {
						leftAxis.addLimitLine(mStockMatchChartData.mLimitLineList
								.get(i));
					}
				}
			}

			rightAxis = viewHolder.chart.getAxisRight();
			if (rightAxis != null) {
				rightAxis.setEnabled(false);
			}

			viewHolder.chart.setDescription(mStockMatchChartData.mDescription);

			if (mItemViewType == ITEM_VIEW_TYPE_MAIN) {
				viewHolder.chart
						.setData(mStockMatchChartData.mCombinedDataMain);
			} else {
				viewHolder.chart.setData(mStockMatchChartData.mCombinedDataSub);
			}

			return view;
		}

		class ViewHolder {
			CombinedChart chart;
		}
	}

	class StockMatchChartItemMain extends StockMatchChartItem {
		public StockMatchChartItemMain(StockMatchChartData stockMatchChartData) {
			super(ITEM_VIEW_TYPE_MAIN,
					R.layout.activity_stock_chart_list_item_main,
					stockMatchChartData);
		}
	}

	class StockMatchChartItemSub extends StockMatchChartItem {
		public StockMatchChartItemSub(StockMatchChartData stockMatchChartData) {
			super(ITEM_VIEW_TYPE_SUB,
					R.layout.activity_stock_chart_list_item_sub,
					stockMatchChartData);
		}
	}

	class StockMatchChartArrayAdapter extends ArrayAdapter<StockMatchChartItem> {

		public StockMatchChartArrayAdapter(Context context,
				List<StockMatchChartItem> objects) {
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
			return mStockMatchChartItemList.size();
		}
	}
}
