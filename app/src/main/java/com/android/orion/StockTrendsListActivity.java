package com.android.orion;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockData;
import com.android.orion.utility.Preferences;
import com.android.orion.view.SyncHorizontalScrollView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static com.android.orion.Settings.KEY_PERIOD_DAY;

public class StockTrendsListActivity extends ListActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
        OnItemLongClickListener, OnClickListener {

    public static final String ACTION_STOCK_ID = "orion.intent.action.ACTION_STOCK_ID";
    public static final String ACTION_STOCK_TRENDS_LIST = "orion.intent.action.ACTION_STOCK_TRENDS_LIST";

    public static final int LOADER_ID_STOCK_TRENDS_LIST = 0;

    public static final int REQUEST_CODE_STOCK_TRENDS_INSERT = 0;
    public static final int REQUEST_CODE_STOCK_TRENDS_SELECT = 1;

    static final int MESSAGE_VIEW_STOCK_DEAL = 4;
    static final int MESSAGE_VIEW_STOCK_CHAT = 5;

    static final int mHeaderTextDefaultColor = Color.BLACK;
    static final int mHeaderTextHighlightColor = Color.RED;

    String mSortOrderColumn = DatabaseContract.COLUMN_MODIFIED;
    String mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
    String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
    String mSortOrder = mSortOrderDefault;

    SyncHorizontalScrollView mTitleSHSV = null;
    SyncHorizontalScrollView mContentSHSV = null;

    TextView mTextViewNameCode = null;
    TextView mTextViewNaturalRally = null;
    TextView mTextViewUpwardTrend = null;
    TextView mTextViewDownwardTrend = null;
    TextView mTextViewNaturalReaction = null;
    TextView mTextViewDate = null;
    TextView mTextViewTime = null;

    ListView mLeftListView = null;
    ListView mRightListView = null;

    SimpleCursorAdapter mLeftAdapter = null;
    SimpleCursorAdapter mRightAdapter = null;

    ActionMode mCurrentActionMode = null;

    StockData mStockData = new StockData();

    Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Intent intent = null;

            switch (msg.what) {
                case MESSAGE_REFRESH:
                    if (mOrionService != null) {
                        mStockDatabaseManager.deleteStockData();
                        mOrionService.download();
                        restartLoader();
                    }
                    break;

                case MESSAGE_VIEW_STOCK_DEAL:
                    intent = new Intent(mContext, StockEditActivity.class);
                    intent.setAction(StockEditActivity.ACTION_STOCK_EDIT);
                    intent.putExtra(Constants.EXTRA_STOCK_ID, mStock.getId());
                    startActivity(intent);
                    break;

                case MESSAGE_VIEW_STOCK_CHAT:
                    ArrayList<String> stockIDList = new ArrayList<String>();
                    for (Stock stock : mStockList) {
                        stockIDList.add(String.valueOf(stock.getId()));
                    }

                    intent = new Intent(mContext, StockDataChartListActivity.class);
                    intent.putExtra(Constants.EXTRA_STOCK_ID, mStock.getId());
                    intent.putStringArrayListExtra(Constants.EXTRA_STOCK_ID_LIST,
                            stockIDList);
                    intent.putExtra(Constants.EXTRA_STOCK_DEAL, true);
                    startActivity(intent);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stock_trends_list);

        mSortOrder = Preferences.getString(mContext, Settings.KEY_SORT_ORDER_STOCK_TRENDS_LIST,
                mSortOrderDefault);

        initHeader();

        initListView();

        mLoaderManager.initLoader(LOADER_ID_STOCK_TRENDS_LIST, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stock_trends_list, menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_delete:
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.title_delete)
                        .setMessage(R.string.delete_confirm)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        StockData stockData = new StockData();
                                        long stockId = getIntent().getLongExtra(Constants.EXTRA_STOCK_ID,
                                                Stock.INVALID_ID);
                                        stockData.setStockId(stockId);
                                        mStockDatabaseManager.deleteStockData(stockData);
                                    }
                                })
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                    }
                                }).setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;

            case R.id.action_delete_all:
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.title_delete_all)
                        .setMessage(R.string.delete_confirm)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        mStockDatabaseManager.deleteStockData();
                                    }
                                })
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                    }
                                }).setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;

            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_STOCK_TRENDS_INSERT:
                case REQUEST_CODE_STOCK_TRENDS_SELECT:
                    if (mOrionService != null) {
                        mOrionService.download(mStock);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        resetHeaderTextColor();
        setHeaderTextColor(id, mHeaderTextHighlightColor);

        switch (id) {
            case R.id.stock_name_code:
                mSortOrderColumn = DatabaseContract.COLUMN_CODE;
                break;
            case R.id.natural_rally:
                mSortOrderColumn = DatabaseContract.COLUMN_NATURAL_RALLY;
                break;
            case R.id.upward_trend:
                mSortOrderColumn = DatabaseContract.COLUMN_UPWARD_TREND;
                break;
            case R.id.downward_trend:
                mSortOrderColumn = DatabaseContract.COLUMN_DOWNWARD_TREND;
                break;
            case R.id.natural_reaction:
                mSortOrderColumn = DatabaseContract.COLUMN_NATURAL_REACTION;
                break;
            case R.id.date:
                mSortOrderColumn = DatabaseContract.COLUMN_DATE;
                break;
            case R.id.time:
                mSortOrderColumn = DatabaseContract.COLUMN_TIME;
                break;
            default:
                mSortOrderColumn = DatabaseContract.COLUMN_DATE;
                break;
        }

        if (mSortOrderDirection.equals(DatabaseContract.ORDER_DIRECTION_ASC)) {
            mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_DESC;
        } else {
            mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
        }

        mSortOrder = mSortOrderColumn + mSortOrderDirection;

        Preferences.putString(mContext, Settings.KEY_SORT_ORDER_STOCK_TRENDS_LIST, mSortOrder);

        restartLoader();
    }

    void setHeaderTextColor(int id, int color) {
        TextView textView = (TextView) findViewById(id);
        setHeaderTextColor(textView, color);
    }

    void setHeaderTextColor(TextView textView, int color) {
        if (textView != null) {
            textView.setTextColor(color);
        }
    }

    void resetHeaderTextColor() {
        setHeaderTextColor(mTextViewNameCode, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewNaturalRally, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewUpwardTrend, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewDownwardTrend, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewNaturalReaction, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewDate, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewTime, mHeaderTextDefaultColor);
    }

    void setVisibility(String key, TextView textView) {
        if (textView != null) {
            if (Preferences.getBoolean(this, key, false)) {
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
        }
    }

    void initHeader() {
        mTitleSHSV = (SyncHorizontalScrollView) findViewById(R.id.title_shsv);
        mContentSHSV = (SyncHorizontalScrollView) findViewById(R.id.content_shsv);

        if (mTitleSHSV != null && mContentSHSV != null) {
            mTitleSHSV.setScrollView(mContentSHSV);
            mContentSHSV.setScrollView(mTitleSHSV);
        }

        mTextViewNameCode = (TextView) findViewById(R.id.stock_name_code);
        if (mTextViewNameCode != null) {
            mTextViewNameCode.setOnClickListener(this);
        }

        mTextViewNaturalRally = (TextView) findViewById(R.id.natural_rally);
        if (mTextViewNaturalRally != null) {
            mTextViewNaturalRally.setOnClickListener(this);
        }

        mTextViewUpwardTrend = (TextView) findViewById(R.id.upward_trend);
        if (mTextViewUpwardTrend != null) {
            mTextViewUpwardTrend.setOnClickListener(this);
        }

        mTextViewDownwardTrend = (TextView) findViewById(R.id.downward_trend);
        if (mTextViewDownwardTrend != null) {
            mTextViewDownwardTrend.setOnClickListener(this);
        }

        mTextViewNaturalReaction = (TextView) findViewById(R.id.natural_reaction);
        if (mTextViewNaturalReaction != null) {
            mTextViewNaturalReaction.setOnClickListener(this);
        }

        mTextViewDate = (TextView) findViewById(R.id.date);
        if (mTextViewDate != null) {
            mTextViewDate.setOnClickListener(this);
        }

        mTextViewTime = (TextView) findViewById(R.id.time);
        if (mTextViewTime != null) {
            mTextViewTime.setOnClickListener(this);
        }

        if (mSortOrder.contains(DatabaseContract.COLUMN_CODE)) {
            setHeaderTextColor(mTextViewNameCode, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_NATURAL_RALLY)) {
            setHeaderTextColor(mTextViewNaturalRally, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_UPWARD_TREND)) {
            setHeaderTextColor(mTextViewUpwardTrend, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_DOWNWARD_TREND)) {
            setHeaderTextColor(mTextViewDownwardTrend, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_NATURAL_REACTION)) {
            setHeaderTextColor(mTextViewNaturalReaction, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_DATE)) {
            setHeaderTextColor(mTextViewDate, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_TIME)) {
            setHeaderTextColor(mTextViewTime, mHeaderTextHighlightColor);
        } else {
        }
    }

    void initListView() {
        String[] mLeftFrom = new String[]{DatabaseContract.COLUMN_NAME,
                DatabaseContract.COLUMN_CODE};
        int[] mLeftTo = new int[]{R.id.name, R.id.code};

        String[] mRightFrom = new String[]{
                DatabaseContract.COLUMN_NATURAL_RALLY,
                DatabaseContract.COLUMN_UPWARD_TREND,
                DatabaseContract.COLUMN_DOWNWARD_TREND,
                DatabaseContract.COLUMN_NATURAL_REACTION,
                DatabaseContract.COLUMN_DATE,
                DatabaseContract.COLUMN_TIME};
        int[] mRightTo = new int[]{R.id.natural_rally, R.id.upward_trend,
                R.id.downward_trend, R.id.natural_reaction,
                R.id.date, R.id.time};

        mLeftListView = (ListView) findViewById(R.id.left_listview);
        mLeftAdapter = new SimpleCursorAdapter(this,
                R.layout.activity_stock_list_left_item, null, mLeftFrom,
                mLeftTo, 0);
        if ((mLeftListView != null) && (mLeftAdapter != null)) {
            mLeftListView.setAdapter(mLeftAdapter);
            mLeftListView.setOnItemClickListener(this);
            mLeftListView.setOnItemLongClickListener(this);
        }

        mRightListView = (ListView) findViewById(R.id.right_listview);
        mRightAdapter = new SimpleCursorAdapter(this,
                R.layout.activity_stock_trends_list_right_item, null, mRightFrom,
                mRightTo, 0);
        if ((mRightListView != null) && (mRightAdapter != null)) {
            mRightAdapter.setViewBinder(new RightViewBinder());
            mRightListView.setAdapter(mRightAdapter);
            mRightListView.setOnItemClickListener(this);
            mRightListView.setOnItemLongClickListener(this);
        }
    }

    void restartLoader(Intent intent) {
        restartLoader();
    }

    void restartLoader() {
        mLoaderManager.restartLoader(LOADER_ID_STOCK_TRENDS_LIST, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        restartLoader();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        String selection = "";
        String[] selectionArgs = null;
        CursorLoader loader = null;

        switch (id) {
            case LOADER_ID_STOCK_TRENDS_LIST:
                if (ACTION_STOCK_TRENDS_LIST.equals(mIntent.getAction())) {
                    long stockId = getIntent().getLongExtra(Constants.EXTRA_STOCK_ID,
                            Stock.INVALID_ID);
                    selection = "(" + DatabaseContract.COLUMN_STOCK_ID + " = " + stockId + ") "
                            + " AND "
                            + "(" + DatabaseContract.COLUMN_PERIOD + " = \'" + KEY_PERIOD_DAY + "\') ";
                    selectionArgs = null;
                    mStock.setId(stockId);
                } else {
                    ArrayList<Stock> stockList = new ArrayList<>();
                    StringBuilder placeHolder = new StringBuilder();
                    StringBuilder stockIds = new StringBuilder();

                    mStockDatabaseManager.getFavoriteStockList(stockList);
                    if (stockList.size() > 0) {
                        placeHolder.append("?");
                        stockIds.append(stockList.get(0).getId());
                        for (int i = 1; i < stockList.size(); i++) {
                            placeHolder.append("," + "?");
                            stockIds.append("," + stockList.get(i).getId());
                        }

                        selection = DatabaseContract.COLUMN_STOCK_ID + " in (" + placeHolder.toString() + " )";
                        selectionArgs = stockIds.toString().split(",");
                    }
                }

                loader = new CursorLoader(this, DatabaseContract.StockData.CONTENT_URI,
                        DatabaseContract.StockData.PROJECTION_ALL, selection, selectionArgs,
                        mSortOrder);

//			mStockList.clear();
                break;

            default:
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader == null) {
            return;
        }

        switch (loader.getId()) {
            case LOADER_ID_STOCK_TRENDS_LIST:
                mLeftAdapter.swapCursor(cursor);
                mRightAdapter.swapCursor(cursor);
                mRightAdapter.notifyDataSetChanged();
                break;

            default:
                break;
        }

        setListViewHeightBasedOnChildren(mLeftListView);
        setListViewHeightBasedOnChildren(mRightListView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mLeftAdapter.swapCursor(null);
        mRightAdapter.swapCursor(null);
        mRightAdapter.notifyDataSetChanged();

//		mStockList.clear();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        if (id <= Stock.INVALID_ID) {
            return;
        }

        if (ACTION_STOCK_ID.equals(mAction)) {
            if (mIntent != null) {
                mIntent.putExtra(Constants.EXTRA_STOCK_ID, id);
                setResult(RESULT_OK, mIntent);
                finish();
            }
        } else {
            if (parent.getId() == R.id.left_listview) {
                mStockData.setId(id);
                mHandler.sendEmptyMessage(MESSAGE_VIEW_STOCK_DEAL);
            } else {
                if (mCurrentActionMode == null) {
                    mStockData.setId(id);
                    mHandler.sendEmptyMessage(MESSAGE_VIEW_STOCK_CHAT);
                }
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {
        return true;
    }

    boolean setRightTextViewVisibility(String key, View textView) {
        if (textView != null) {
            if (Preferences.getBoolean(this, key, false)) {
                textView.setVisibility(View.VISIBLE);
                return false;
            } else {
                textView.setVisibility(View.GONE);
                return true;
            }
        }

        return false;
    }

    boolean setRightTextView(String key, View view) {
        if (view == null) {
            return false;
        }

        TextView textView = (TextView) view;
        String text = textView.getText().toString();
        if (TextUtils.isEmpty(text) || text.equals("0")) {
            return false;
        }

        if (key.equals(DatabaseContract.COLUMN_NATURAL_RALLY)) {
            textView.setTextColor(Color.BLUE);
        } else if (key.equals(DatabaseContract.COLUMN_UPWARD_TREND)) {
            textView.setTextColor(Color.RED);
        } else if (key.equals(DatabaseContract.COLUMN_DOWNWARD_TREND)) {
            textView.setTextColor(Color.GREEN);
        } else if (key.equals(DatabaseContract.COLUMN_NATURAL_REACTION)) {
            textView.setTextColor(Color.YELLOW);
        }

        return true;
    }

    private class RightViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if ((view == null) || (cursor == null) || (columnIndex == -1)) {
                return false;
            }

            if (columnIndex == cursor
                    .getColumnIndex(DatabaseContract.COLUMN_NATURAL_RALLY)) {
                return setRightTextView(DatabaseContract.COLUMN_NATURAL_RALLY, view);
            } else if (columnIndex == cursor
                    .getColumnIndex(DatabaseContract.COLUMN_UPWARD_TREND)) {
                return setRightTextView(DatabaseContract.COLUMN_UPWARD_TREND, view);
            } else if (columnIndex == cursor
                    .getColumnIndex(DatabaseContract.COLUMN_DOWNWARD_TREND)) {
                return setRightTextView(DatabaseContract.COLUMN_DOWNWARD_TREND, view);
            } else if (columnIndex == cursor
                    .getColumnIndex(DatabaseContract.COLUMN_NATURAL_REACTION)) {
                return setRightTextView(DatabaseContract.COLUMN_NATURAL_REACTION, view);
            }

            return false;
        }
    }
}
