package com.android.orion.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.config.Config;
import com.android.orion.data.Period;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Symbol;
import com.android.orion.utility.Utility;
import com.android.orion.view.SyncHorizontalScrollView;

import java.util.HashMap;
import java.util.Map;

public class StockFavoriteListActivity extends ListActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, View.OnClickListener {

    private static final int LOADER_ID_STOCK_FAVORITE_LIST = 0;
    private static final int HEADER_TEXT_DEFAULT_COLOR = Color.BLACK;
    private static final int HEADER_TEXT_HIGHLIGHT_COLOR = Color.RED;
    private int mColumnIndexCode = -1;
    private int mColumnIndexName = -1;
    private int mColumnIndexFlag = -1;
    private int mColumnIndexBuyProfit = -1;
    private int mColumnIndexSellProfit = -1;

    private final Map<Integer, TextView> mHeaderTextViews = new HashMap<>();
    private final Map<String, Integer> mColumnToViewIdMap = new HashMap<>();
    
    private String mSortOrderColumn = DatabaseContract.COLUMN_NET;
    private String mSortOrderDirection = DatabaseContract.ORDER_DESC;
    private final String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
    private String mSortOrder = mSortOrderDefault;
    
    private SyncHorizontalScrollView mTitleSHSV, mContentSHSV;
    private ListView mLeftListView, mRightListView;
    private SimpleCursorAdapter mLeftAdapter, mRightAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_favorite_list);

        initColumnMapping();
        initHeaderViews();
        setupListView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initLoader();
    }

    @Override
    protected void onStop() {
        super.onStop();
        destroyLoader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetHeaderTextColor();
        highlightCurrentSortColumn();
    }

    @Override
    public void handleOnResume() {
        super.handleOnResume();
        restartLoader();
    }

    private void initColumnMapping() {
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_CODE, R.id.stock_name_code);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_PRICE, R.id.price);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_NET, R.id.net);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_BUY_PROFIT, R.id.trade);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_SELL_PROFIT, R.id.trade);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_TREND_THUMBNAIL, R.id.trend);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_EXPECT, R.id.expect);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_COMPONENT_THUMBNAIL, R.id.component);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_YEAR_THUMBNAIL, R.id.period_year);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_MONTH6_THUMBNAIL, R.id.period_month6);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_QUARTER_THUMBNAIL, R.id.period_quarter);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_MONTH2_THUMBNAIL, R.id.period_month2);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_MONTH_THUMBNAIL, R.id.period_month);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_WEEK_THUMBNAIL, R.id.period_week);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_DAY_THUMBNAIL, R.id.period_day);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_MIN60_THUMBNAIL, R.id.period_min60);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_MIN30_THUMBNAIL, R.id.period_min30);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_MIN15_THUMBNAIL, R.id.period_min15);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_MIN5_THUMBNAIL, R.id.period_min5);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_FLAG, R.id.flag);
        mColumnToViewIdMap.put(DatabaseContract.COLUMN_MODIFIED, R.id.modified);
    }

    private void initHeaderViews() {
        mTitleSHSV = findViewById(R.id.title_shsv);
        mContentSHSV = findViewById(R.id.content_shsv);

        if (mTitleSHSV != null && mContentSHSV != null) {
            mTitleSHSV.setScrollView(mContentSHSV);
            mContentSHSV.setScrollView(mTitleSHSV);
        }

        int[] headerViewIds = {
                R.id.stock_name_code, R.id.price, R.id.net, R.id.trade, R.id.trend, R.id.expect, R.id.component,
                R.id.period_year, R.id.period_month6, R.id.period_quarter, R.id.period_month2, R.id.period_month, R.id.period_week,
                R.id.period_day, R.id.period_min60, R.id.period_min30, R.id.period_min15, R.id.period_min5,
                R.id.flag, R.id.modified
        };

        for (int viewId : headerViewIds) {
            TextView textView = findViewById(viewId);
            if (textView != null) {
                textView.setOnClickListener(this);
                mHeaderTextViews.put(viewId, textView);
                setHeaderVisibility(viewId);
            }
        }
    }

    private void setHeaderVisibility(int viewId) {
        TextView textView = mHeaderTextViews.get(viewId);
        if (textView == null) return;

        if (viewId == R.id.flag) {
            textView.setVisibility(View.GONE);
            return;
        }

        if (viewId == R.id.expect) {
            textView.setVisibility(View.GONE);
            return;
        }

        Map<Integer, String> periodViewMap = createPeriodViewMap();
        String period = periodViewMap.get(viewId);
        if (period != null) {
            setVisibility(textView, Setting.getPeriod(period));
        }
    }

    private Map<Integer, String> createPeriodViewMap() {
        Map<Integer, String> map = new HashMap<>();
        map.put(R.id.period_year, Period.YEAR);
        map.put(R.id.period_month6, Period.MONTH6);
        map.put(R.id.period_quarter, Period.QUARTER);
        map.put(R.id.period_month2, Period.MONTH2);
        map.put(R.id.period_month, Period.MONTH);
        map.put(R.id.period_week, Period.WEEK);
        map.put(R.id.period_day, Period.DAY);
        map.put(R.id.period_min60, Period.MIN60);
        map.put(R.id.period_min30, Period.MIN30);
        map.put(R.id.period_min15, Period.MIN15);
        map.put(R.id.period_min5, Period.MIN5);
        return map;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stock_favorite_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void handleOnOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_new) {
            startStockActivity(Constant.ACTION_FAVORITE_STOCK_INSERT);
        } else if (itemId == R.id.action_refresh) {
            refreshStockData();
        } else if (itemId == R.id.action_load) {
            performLoadFromFile(Constant.FILE_TYPE_FAVORITE, false);
        } else if (itemId == R.id.action_save) {
            performSaveToFile(Constant.FILE_TYPE_FAVORITE);
        } else if (itemId == R.id.action_deal) {
            startActivity(new Intent(this, StockDealListActivity.class));
        } else if (itemId == R.id.action_list) {
            startActivity(new Intent(this, StockListActivity.class));
        } else if (itemId == R.id.action_import) {
            performLoadFromFile(Constant.FILE_TYPE_TDX_DATA, true);
        } else {
            super.handleOnOptionsItemSelected(item);
        }
    }

    private void startStockActivity(String action) {
        Intent intent = new Intent(this, StockActivity.class);
        intent.setAction(action);
        startActivity(intent);
    }

    private void refreshStockData() {
        try {
            mStockDatabaseManager.loadStockArrayMap(mStockArrayMap);
            for (Stock stock : mStockArrayMap.values()) {
                clearStockData(stock);
                Setting.setDownloadStockDataTimeMillis(stock, 0);
                mStockDataProvider.download(stock);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearStockData(Stock stock) {
        mStockDatabaseManager.deleteStockData(stock);
        mStockDatabaseManager.deleteStockTrend(stock);
        mStockDatabaseManager.deleteStockPerceptron(stock.getId());
    }

    @Override
    public void onClick(@NonNull View view) {
        resetHeaderTextColor();
        setHeaderTextColor(view.getId(), HEADER_TEXT_HIGHLIGHT_COLOR);

        int viewId = view.getId();

        if (viewId == R.id.trend) {
            mSortOrderColumn = DatabaseContract.COLUMN_EXPECT;
        } else {
            mSortOrderColumn = getSortColumnForView(viewId);
        }

        toggleSortOrderDirection();

        mSortOrder = mSortOrderColumn + mSortOrderDirection;
        Preferences.putString(Setting.SETTING_SORT_ORDER_FAVORITE_LIST, mSortOrder);
        restartLoader();
    }

    private void toggleSortOrderDirection() {
        mSortOrderDirection = TextUtils.equals(mSortOrderDirection, DatabaseContract.ORDER_ASC)
                ? DatabaseContract.ORDER_DESC
                : DatabaseContract.ORDER_ASC;
    }

    private String getSortColumnForView(int viewId) {
        for (Map.Entry<String, Integer> entry : mColumnToViewIdMap.entrySet()) {
            if (entry.getValue() == viewId) {
                return entry.getKey();
            }
        }
        return DatabaseContract.COLUMN_NET;
    }

    private void resetHeaderTextColor() {
        for (TextView textView : mHeaderTextViews.values()) {
            if (textView != null) {
                textView.setTextColor(HEADER_TEXT_DEFAULT_COLOR);
            }
        }
    }

    private void setHeaderTextColor(int viewId, int color) {
        TextView textView = mHeaderTextViews.get(viewId);
        if (textView != null) {
            textView.setTextColor(color);
        }
    }

    private void highlightCurrentSortColumn() {
        Integer viewId = mColumnToViewIdMap.get(mSortOrderColumn);
        if (viewId != null) {
            setHeaderTextColor(viewId, HEADER_TEXT_HIGHLIGHT_COLOR);
        }
    }

    private void setupListView() {
        setupLeftListView();
        setupRightListView();
    }

    private void setupLeftListView() {
        String[] leftFrom = {DatabaseContract.COLUMN_NAME, DatabaseContract.COLUMN_CODE};
        int[] leftTo = {R.id.name, R.id.code};

        mLeftListView = findViewById(R.id.left_listview);
        mLeftAdapter = new SimpleCursorAdapter(this,
                R.layout.activity_stock_favorite_list_left_item, null, leftFrom, leftTo, 0);
        
        if (mLeftListView != null) {
            mLeftAdapter.setViewBinder(new LeftViewBinder());
            mLeftListView.setAdapter(mLeftAdapter);
            mLeftListView.setOnItemClickListener(this);
            mLeftListView.setOnItemLongClickListener(this);
        }
    }

    private void setupRightListView() {
        String[] rightFrom = {
                DatabaseContract.COLUMN_PRICE,
                DatabaseContract.COLUMN_NET,
                DatabaseContract.COLUMN_BUY_PROFIT,
                DatabaseContract.COLUMN_SELL_PROFIT,
                DatabaseContract.COLUMN_TREND_THUMBNAIL,
                DatabaseContract.COLUMN_EXPECT,
                DatabaseContract.COLUMN_COMPONENT_THUMBNAIL,  
                DatabaseContract.COLUMN_YEAR_THUMBNAIL,       
                DatabaseContract.COLUMN_MONTH6_THUMBNAIL,     
                DatabaseContract.COLUMN_QUARTER_THUMBNAIL,    
                DatabaseContract.COLUMN_MONTH2_THUMBNAIL,     
                DatabaseContract.COLUMN_MONTH_THUMBNAIL,      
                DatabaseContract.COLUMN_WEEK_THUMBNAIL,       
                DatabaseContract.COLUMN_DAY_THUMBNAIL,        
                DatabaseContract.COLUMN_MIN60_THUMBNAIL,      
                DatabaseContract.COLUMN_MIN30_THUMBNAIL,      
                DatabaseContract.COLUMN_MIN15_THUMBNAIL,      
                DatabaseContract.COLUMN_MIN5_THUMBNAIL,       
                DatabaseContract.COLUMN_FLAG,                 
                DatabaseContract.COLUMN_MODIFIED              
        };

        int[] rightTo = {
                R.id.price,
                R.id.net,
                R.id.buy_profit,
                R.id.sell_profit,
                R.id.trend,           
                R.id.expect,
                R.id.component,       
                R.id.year,            
                R.id.month6,          
                R.id.quarter,         
                R.id.month2,          
                R.id.month,           
                R.id.week,            
                R.id.day,             
                R.id.min60,           
                R.id.min30,           
                R.id.min15,           
                R.id.min5,            
                R.id.flag,            
                R.id.modified         
        };

        mRightListView = findViewById(R.id.right_listview);
        mRightAdapter = new SimpleCursorAdapter(this,
                R.layout.activity_stock_favorite_list_right_item, null, rightFrom, rightTo, 0);

        if (mRightListView != null) {
            mRightAdapter.setViewBinder(new RightViewBinder());
            mRightListView.setAdapter(mRightAdapter);
            mRightListView.setOnItemClickListener(this);
            mRightListView.setOnItemLongClickListener(this);
        }
    }

    private void initLoader() {
        mSortOrder = Preferences.getString(Setting.SETTING_SORT_ORDER_FAVORITE_LIST, mSortOrderDefault);
        if (!TextUtils.isEmpty(mSortOrder)) {
            String[] strings = mSortOrder.split(Symbol.WHITE_SPACE);
            if (strings != null && strings.length > 1) {
                mSortOrderColumn = strings[0];
            }
        }
        mLoaderManager.initLoader(LOADER_ID_STOCK_FAVORITE_LIST, null, this);
    }

    private void destroyLoader() {
        mLoaderManager.destroyLoader(LOADER_ID_STOCK_FAVORITE_LIST);
    }

    void restartLoader() {
        mLoaderManager.restartLoader(LOADER_ID_STOCK_FAVORITE_LIST, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        if (id == LOADER_ID_STOCK_FAVORITE_LIST) {
            String selection = DatabaseContract.SELECTION_FLAG(Stock.FLAG_FAVORITE);
            return new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
                    DatabaseContract.Stock.PROJECTION_ALL, selection, null, mSortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader == null || loader.getId() != LOADER_ID_STOCK_FAVORITE_LIST) {
            return;
        }

        cacheColumnIndices(cursor);

        mLeftAdapter.swapCursor(cursor);
        mRightAdapter.swapCursor(cursor);

        setListViewHeightBasedOnChildren(mLeftListView);
        setListViewHeightBasedOnChildren(mRightListView);
    }

    private void cacheColumnIndices(Cursor cursor) {
        if (cursor != null) {
            mColumnIndexCode = cursor.getColumnIndex(DatabaseContract.COLUMN_CODE);
            mColumnIndexName = cursor.getColumnIndex(DatabaseContract.COLUMN_NAME);
            mColumnIndexFlag = cursor.getColumnIndex(DatabaseContract.COLUMN_FLAG);
            mColumnIndexBuyProfit = cursor.getColumnIndex(DatabaseContract.COLUMN_BUY_PROFIT);
            mColumnIndexSellProfit = cursor.getColumnIndex(DatabaseContract.COLUMN_SELL_PROFIT);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mLeftAdapter.swapCursor(null);
        mRightAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (id <= DatabaseContract.INVALID_ID) return;

        if (TextUtils.equals(mAction, Constant.ACTION_STOCK_ID)) {
            handleStockSelectionResult(id);
        } else {
            if (parent.getId() == R.id.left_listview) {
                handleLeftListItemClick(id);
            } else {
                handleRightListItemClick(id);
            }
        }
    }

    private void handleStockSelectionResult(long id) {
        if (mIntent != null) {
            mIntent.putExtra(Constant.EXTRA_STOCK_ID, id);
            setResult(RESULT_OK, mIntent);
            finish();
        }
    }

    private void handleLeftListItemClick(long id) {
        mStock.setId(id);
        mStockDatabaseManager.getStockById(mStock);
        
        Intent intent;
        if (mStock.getHold() > 0) {
            intent = new Intent(mContext, StockDealListActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(Constant.EXTRA_STOCK_SE, mStock.getSE());
            bundle.putString(Constant.EXTRA_STOCK_CODE, mStock.getCode());
            intent.putExtras(bundle);
        } else {
            intent = new Intent(this, StockActivity.class);
            intent.setAction(Constant.ACTION_STOCK_EDIT);
            intent.putExtra(Constant.EXTRA_STOCK_ID, mStock.getId());
        }
        startActivity(intent);
    }

    private void handleRightListItemClick(long id) {
        Intent intent = new Intent(this, StockFavoriteChartListActivity.class);
        intent.putExtra(Constant.EXTRA_STOCK_LIST_SORT_ORDER, mSortOrder);
        intent.putExtra(Constant.EXTRA_STOCK_ID, id);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(this, StockListActivity.class));
        return true;
    }

    private class LeftViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view == null || cursor == null || columnIndex == -1) {
                return false;
            }

            if (columnIndex == mColumnIndexCode || columnIndex == mColumnIndexName) {
                setViewColor(view, cursor);
            }
            return false;
        }

        void setViewColor(View view, Cursor cursor) {
            if (view == null || cursor == null) {
                return;
            }

            String code = cursor.getString(cursor.getColumnIndex(DatabaseContract.COLUMN_CODE));
            TextView textView = (TextView) view;
            if (TextUtils.equals(mAnalyzingStockCode, code)) {
                textView.setTextColor(Color.RED);
            } else {
                textView.setTextColor(Color.GRAY);
            }
        }
    }

    private class RightViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view == null || cursor == null || columnIndex == -1) {
                return true; // Return true to indicate we handled this binding
            }

            String columnName = cursor.getColumnName(columnIndex);
            if (columnName == null) {
                return false;
            }

            // Handle BLOB columns first
            if (DatabaseContract.isPeriodThumbnailColumn(columnName) ||
                    DatabaseContract.COLUMN_TREND_THUMBNAIL.equals(columnName) ||
                    DatabaseContract.COLUMN_COMPONENT_THUMBNAIL.equals(columnName)) {

                return handleBlobColumn(view, cursor, columnIndex, columnName);
            }

            // Handle other column types
            return handleTextColumns(view, cursor, columnIndex, columnName);
        }

        private boolean handleBlobColumn(View view, Cursor cursor, int columnIndex, String columnName) {
            // Set visibility based on settings for period thumbnails
            if (DatabaseContract.isPeriodThumbnailColumn(columnName)) {
                String period = Period.fromColumnName(columnName);
                view.setVisibility(Setting.getPeriod(period) ? View.VISIBLE : View.GONE);
            } else {
                view.setVisibility(View.VISIBLE);
            }

            // Handle ImageView BLOB data
            if (view instanceof ImageView) {
                try {
                    if (!cursor.isNull(columnIndex)) {
                        byte[] blobData = cursor.getBlob(columnIndex);
                        if (blobData != null && blobData.length > 0) {
                            ((ImageView) view).setImageDrawable(
                                    Utility.bytesToThumbnail(StockFavoriteListActivity.this, blobData)
                            );
                        } else {
                            ((ImageView) view).setImageDrawable(null);
                        }
                    } else {
                        ((ImageView) view).setImageDrawable(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ((ImageView) view).setImageDrawable(null);
                }
                return true; // We handled this binding
            }

            return true; // For non-ImageViews, we still handle it to prevent string conversion
        }

        private boolean handleTextColumns(View view, Cursor cursor, int columnIndex, String columnName) {
            if (!(view instanceof TextView)) {
                return false;
            }

            TextView textView = (TextView) view;

            if (DatabaseContract.COLUMN_FLAG.equals(columnName) ||
                    DatabaseContract.COLUMN_EXPECT.equals(columnName)) {
                textView.setText("");
                textView.setVisibility(View.GONE);
                return true;
            }

            try {
                if (cursor.isNull(columnIndex)) {
                    textView.setText("");
                    return true;
                }

                if (DatabaseContract.COLUMN_PRICE.equals(columnName) ||
                        DatabaseContract.COLUMN_NET.equals(columnName) ||
                        DatabaseContract.COLUMN_EXPECT.equals(columnName)) {
                    // Handle numeric columns
                    double value = cursor.getDouble(columnIndex);
                    textView.setText(String.valueOf(value));

                } else if (DatabaseContract.COLUMN_BUY_PROFIT.equals(columnName)) {
                    if (Utility.hasFlag(cursor.getInt(mColumnIndexFlag), Stock.FLAG_TRADE)) {
                        double profit = cursor.getDouble(columnIndex);
                        textView.setText(String.valueOf(profit));
                        textView.setTextColor(profit > 0 ? Color.RED : Color.GRAY);
                    } else {
                        textView.setText("");
                    }
                    return true;

                } else if (DatabaseContract.COLUMN_SELL_PROFIT.equals(columnName)) {
                    if (Utility.hasFlag(cursor.getInt(mColumnIndexFlag), Stock.FLAG_TRADE)) {
                        double profit = cursor.getDouble(columnIndex);
                        textView.setText(String.valueOf(profit));
                        textView.setTextColor(profit < 0 ? Color.RED : Color.GRAY);
                    } else {
                        textView.setText("");
                    }
                    return true;

                } else if (DatabaseContract.COLUMN_MODIFIED.equals(columnName)) {
                    textView.setText(cursor.getString(columnIndex));

                } else {
                    return false; // Let the adapter handle other columns
                }

            } catch (Exception e) {
                e.printStackTrace();
                textView.setText("");
            }

            return true;
        }
    }
}