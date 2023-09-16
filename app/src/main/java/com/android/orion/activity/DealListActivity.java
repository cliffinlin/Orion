package com.android.orion.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
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

import com.android.orion.setting.Constants;
import com.android.orion.R;
import com.android.orion.setting.Settings;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.RecordFile;
import com.android.orion.view.SyncHorizontalScrollView;

public class DealListActivity extends ListActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
        OnItemLongClickListener, OnClickListener {
    static final String TAG = Constants.TAG + " "
            + DealListActivity.class.getSimpleName();

    static final int LOADER_ID_DEAL_LIST = 0;

    static final int FILTER_TYPE_NONE = 0;
    static final int FILTER_TYPE_OPERATE = 1;
    static final int FILTER_TYPE_BUY = 2;
    static final int FILTER_TYPE_SELL = 3;
    static final int FILTER_TYPE_ALL = 4;

    static final int MESSAGE_DELETE_DEAL = 0;
    static final int MESSAGE_DELETE_DEAL_LIST = 1;

    static final int MESSAGE_VIEW_STOCK_DEAL = 4;
    static final int MESSAGE_VIEW_STOCK_CHAT = 5;
    static final int MESSAGE_VIEW_STOCK_TREND = 6;

    static final int REQUEST_CODE_DEAL_INSERT = 0;
    static final int REQUEST_CODE_DEAL_EDIT = 1;

    static final int mHeaderTextDefaultColor = Color.BLACK;
    static final int mHeaderTextHighlightColor = Color.RED;

    String mSelection = null;
    String mSortOrderColumn = DatabaseContract.COLUMN_NET;
    String mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
    String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
    String mSortOrder = mSortOrderDefault;

    SyncHorizontalScrollView mTitleSHSV = null;
    SyncHorizontalScrollView mContentSHSV = null;

    TextView mTextViewStockNameCode = null;
    TextView mTextViewPrice = null;
    TextView mTextViewNet = null;
    TextView mTextViewBuy = null;
    TextView mTextViewSell = null;
    TextView mTextViewVolume = null;
    TextView mTextViewValue = null;
    TextView mTextViewBonus = null;
    TextView mTextViewYield = null;
    TextView mTextViewFee = null;
    TextView mTextViewProfit = null;
    TextView mTextViewAccount = null;
    TextView mTextViewAction = null;
    TextView mTextViewCreated = null;
    TextView mTextViewModified = null;

    ListView mLeftListView = null;
    ListView mRightListView = null;

    SimpleCursorAdapter mLeftAdapter = null;
    SimpleCursorAdapter mRightAdapter = null;

    ActionMode mCurrentActionMode = null;

    StockDeal mDeal = new StockDeal();
    List<StockDeal> mDealList = new ArrayList<StockDeal>();

    Stock mStock = new Stock();

    int mFilterType = FILTER_TYPE_NONE;

    Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Intent intent = null;

            switch (msg.what) {
                case MESSAGE_DELETE_DEAL:
                    getStock();
                    RecordFile.writeDealFile(mStock, mDeal, Constants.DEAL_OPERATE_DELETE);
                    mStockDatabaseManager.deleteStockDeal(mDeal);
                    mStockDatabaseManager.updateStockDeal(mStock);
                    mStockDatabaseManager.updateStock(mStock,
                            mStock.getContentValues());
                    break;

                case MESSAGE_DELETE_DEAL_LIST:
                    break;

                case MESSAGE_VIEW_STOCK_DEAL:
                    getStock();

                    intent = new Intent(mContext, StockEditActivity.class);
                    intent.setAction(StockEditActivity.ACTION_STOCK_EDIT);
                    intent.putExtra(Constants.EXTRA_STOCK_ID, mStock.getId());
                    startActivity(intent);
                    break;

                case MESSAGE_VIEW_STOCK_CHAT:
                    getStock();

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

                case MESSAGE_VIEW_STOCK_TREND:
                    mStockDatabaseManager.getStock(mStock);

                    mIntent = new Intent(mContext, StockTrendListActivity.class);
                    mIntent.setAction(StockTrendListActivity.ACTION_STOCK_TREND_LIST);
                    mIntent.putExtra(Constants.EXTRA_STOCK_ID, mStock.getId());
                    startActivity(mIntent);
                    break;

                default:
                    break;
            }
        }
    };

    ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            // restartLoader();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }
    };

    private ActionMode.Callback mModeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Actions");
            mode.getMenuInflater().inflate(R.menu.stock_deal_list_action, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_edit:
                    mIntent = new Intent(mContext, StockDealActivity.class);
                    mIntent.setAction(StockDealActivity.ACTION_DEAL_EDIT);
                    mIntent.putExtra(StockDealActivity.EXTRA_DEAL_ID,
                            mDeal.getId());
                    startActivityForResult(mIntent, REQUEST_CODE_DEAL_EDIT);
                    mode.finish();
                    return true;
                case R.id.menu_delete:
                    new AlertDialog.Builder(mContext)
                            .setTitle(R.string.delete)
                            .setMessage(R.string.delete_confirm)
                            .setPositiveButton(R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            mHandler.sendEmptyMessage(MESSAGE_DELETE_DEAL);
                                            mode.finish();
                                        }
                                    })
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            mode.finish();
                                        }
                                    }).setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mCurrentActionMode = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_deal_list);

        mFilterType = FILTER_TYPE_SELL;

        mSortOrder = Preferences.getString(mContext, Settings.KEY_SORT_ORDER_DEAL_LIST,
                mSortOrderDefault);

        initHeader();

        initListView();

        mLoaderManager.initLoader(LOADER_ID_DEAL_LIST, null, this);

        getContentResolver().registerContentObserver(
                DatabaseContract.StockDeal.CONTENT_URI, true, mContentObserver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stock_deal_list, menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_new:
                mIntent = new Intent(this, StockDealActivity.class);
                mIntent.setAction(StockDealActivity.ACTION_DEAL_INSERT);
                if (mBundle != null) {
                    mIntent.putExtras(mBundle);
                }
                startActivityForResult(mIntent, REQUEST_CODE_DEAL_INSERT);
                return true;

            case R.id.action_none:
                mFilterType = FILTER_TYPE_NONE;
                restartLoader();
                return true;

            case R.id.action_operate:
                mFilterType = FILTER_TYPE_OPERATE;
                restartLoader();
                return true;

            case R.id.action_buy:
                mFilterType = FILTER_TYPE_BUY;
                restartLoader();
                return true;

            case R.id.action_sell:
                mFilterType = FILTER_TYPE_SELL;
                restartLoader();
                return true;

            case R.id.action_all:
                mFilterType = FILTER_TYPE_ALL;
                restartLoader();
                return true;

            case R.id.action_trend:
                mHandler.sendEmptyMessage(MESSAGE_VIEW_STOCK_TREND);
                return true;

            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    Long doInBackgroundLoad(Object... params) {
        super.doInBackgroundLoad(params);
        int execute = (Integer) params[0];

        switch (execute) {

            default:
                break;
        }

        return RESULT_SUCCESS;
    }

    void onPostExecuteLoad(Long result) {
        super.onPostExecuteLoad(result);
        mOrionService.download();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_DEAL_INSERT:
                case REQUEST_CODE_DEAL_EDIT:
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
            case R.id.account:
                mSortOrderColumn = DatabaseContract.COLUMN_ACCOUNT;
                break;
            case R.id.action:
                mSortOrderColumn = DatabaseContract.COLUMN_ACTION;
                break;
            case R.id.price:
                mSortOrderColumn = DatabaseContract.COLUMN_PRICE;
                break;
            case R.id.net:
                mSortOrderColumn = DatabaseContract.COLUMN_NET;
                break;
            case R.id.buy:
                mSortOrderColumn = DatabaseContract.COLUMN_BUY;
                break;
            case R.id.sell:
                mSortOrderColumn = DatabaseContract.COLUMN_SELL;
                break;
            case R.id.volume:
                mSortOrderColumn = DatabaseContract.COLUMN_VOLUME;
                break;
            case R.id.value:
                mSortOrderColumn = DatabaseContract.COLUMN_VALUE;
                break;
            case R.id.profit:
                mSortOrderColumn = DatabaseContract.COLUMN_PROFIT;
                break;
            case R.id.fee:
                mSortOrderColumn = DatabaseContract.COLUMN_FEE;
                break;
            case R.id.created:
                mSortOrderColumn = DatabaseContract.COLUMN_CREATED;
                break;
            case R.id.modified:
                mSortOrderColumn = DatabaseContract.COLUMN_MODIFIED;
                break;
            default:
                mSortOrderColumn = DatabaseContract.COLUMN_CODE;
                break;
        }

        if (mSortOrderDirection.equals(DatabaseContract.ORDER_DIRECTION_ASC)) {
            mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_DESC;
        } else {
            mSortOrderDirection = DatabaseContract.ORDER_DIRECTION_ASC;
        }

        mSortOrder = mSortOrderColumn + mSortOrderDirection;

        Preferences.putString(mContext, Settings.KEY_SORT_ORDER_DEAL_LIST, mSortOrder);

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
        setHeaderTextColor(mTextViewStockNameCode, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewPrice, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewNet, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewBuy, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewSell, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewVolume, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewValue, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewBonus, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewYield, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewFee, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewProfit, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewAccount, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewAction, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewCreated, mHeaderTextDefaultColor);
        setHeaderTextColor(mTextViewModified, mHeaderTextDefaultColor);
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

        mTextViewStockNameCode = (TextView) findViewById(R.id.stock_name_code);
        mTextViewStockNameCode.setOnClickListener(this);

        mTextViewPrice = (TextView) findViewById(R.id.price);
        mTextViewPrice.setOnClickListener(this);

        mTextViewNet = (TextView) findViewById(R.id.net);
        mTextViewNet.setOnClickListener(this);

        mTextViewBuy = (TextView) findViewById(R.id.buy);
        mTextViewBuy.setOnClickListener(this);

        mTextViewSell = (TextView) findViewById(R.id.sell);
        mTextViewSell.setOnClickListener(this);

        mTextViewVolume = (TextView) findViewById(R.id.volume);
        mTextViewVolume.setOnClickListener(this);

        mTextViewValue = (TextView) findViewById(R.id.value);
        mTextViewValue.setOnClickListener(this);

        mTextViewBonus = (TextView) findViewById(R.id.bonus);
        mTextViewBonus.setOnClickListener(this);

        mTextViewYield = (TextView) findViewById(R.id.yield);
        mTextViewYield.setOnClickListener(this);

        mTextViewFee = (TextView) findViewById(R.id.fee);
        mTextViewFee.setOnClickListener(this);

        mTextViewProfit = (TextView) findViewById(R.id.profit);
        mTextViewProfit.setOnClickListener(this);

        mTextViewAccount = (TextView) findViewById(R.id.account);
        mTextViewAccount.setOnClickListener(this);

        mTextViewAction = (TextView) findViewById(R.id.action);
        mTextViewAction.setOnClickListener(this);

        mTextViewCreated = (TextView) findViewById(R.id.created);
        mTextViewCreated.setOnClickListener(this);

        mTextViewModified = (TextView) findViewById(R.id.modified);
        mTextViewModified.setOnClickListener(this);

        if (mSortOrder.contains(DatabaseContract.COLUMN_CODE)) {
            setHeaderTextColor(mTextViewStockNameCode,
                    mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_PRICE)) {
            setHeaderTextColor(mTextViewPrice, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_NET)) {
            setHeaderTextColor(mTextViewNet, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_BUY)) {
            setHeaderTextColor(mTextViewBuy, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_SELL)) {
            setHeaderTextColor(mTextViewSell, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_VOLUME)) {
            setHeaderTextColor(mTextViewVolume, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_VALUE)) {
            setHeaderTextColor(mTextViewValue, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_BONUS)) {
            setHeaderTextColor(mTextViewBonus, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_YIELD)) {
            setHeaderTextColor(mTextViewYield, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_FEE)) {
            setHeaderTextColor(mTextViewFee, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_PROFIT)) {
            setHeaderTextColor(mTextViewProfit, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_ACCOUNT)) {
            setHeaderTextColor(mTextViewAccount, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_ACTION)) {
            setHeaderTextColor(mTextViewAction, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_CREATED)) {
            setHeaderTextColor(mTextViewCreated, mHeaderTextHighlightColor);
        } else if (mSortOrder.contains(DatabaseContract.COLUMN_MODIFIED)) {
            setHeaderTextColor(mTextViewModified, mHeaderTextHighlightColor);
        } else {
        }
    }

    void initListView() {
        String[] mLeftFrom = new String[]{DatabaseContract.COLUMN_NAME,
                DatabaseContract.COLUMN_CODE};
        int[] mLeftTo = new int[]{R.id.name, R.id.code};

        String[] mRightFrom = new String[]{
                DatabaseContract.COLUMN_PRICE,
                DatabaseContract.COLUMN_NET,
                DatabaseContract.COLUMN_BUY,
                DatabaseContract.COLUMN_SELL,
                DatabaseContract.COLUMN_VOLUME,
                DatabaseContract.COLUMN_VALUE,
                DatabaseContract.COLUMN_BONUS,
                DatabaseContract.COLUMN_YIELD,
                DatabaseContract.COLUMN_FEE,
                DatabaseContract.COLUMN_PROFIT,
                DatabaseContract.COLUMN_ACCOUNT,
                DatabaseContract.COLUMN_ACTION,
                DatabaseContract.COLUMN_CREATED,
                DatabaseContract.COLUMN_MODIFIED};
        int[] mRightTo = new int[]{
                R.id.price,
                R.id.net,
                R.id.buy,
                R.id.sell,
                R.id.volume,
                R.id.value,
                R.id.bonus,
                R.id.yield,
                R.id.fee,
                R.id.profit,
                R.id.account,
                R.id.action,
                R.id.created,
                R.id.modified};

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
                R.layout.activity_stock_deal_list_right_item, null, mRightFrom,
                mRightTo, 0);
        if ((mRightListView != null) && (mRightAdapter != null)) {
            mRightAdapter.setViewBinder(new CustomViewBinder());
            mRightListView.setAdapter(mRightAdapter);
            mRightListView.setOnItemClickListener(this);
            mRightListView.setOnItemLongClickListener(this);
        }
    }

    void restartLoader(Intent intent) {
        restartLoader();
    }

    void restartLoader() {
        mLoaderManager.restartLoader(LOADER_ID_DEAL_LIST, null, this);
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

        getContentResolver().unregisterContentObserver(mContentObserver);
    }

    void setupSelection() {
        mSelection = null;

        switch (mFilterType) {
            case FILTER_TYPE_NONE:
                mSelection = DatabaseContract.COLUMN_ACTION + " = ''";
                break;

            case FILTER_TYPE_OPERATE:
                mSelection = DatabaseContract.COLUMN_ACTION + " != ''";
                break;

            case FILTER_TYPE_BUY:
                mSelection = DatabaseContract.COLUMN_ACTION + " != ''";
                mSelection += " AND " + DatabaseContract.COLUMN_VOLUME + " < " + 0;
                break;

            case FILTER_TYPE_SELL:
                mSelection = DatabaseContract.COLUMN_ACTION + " != ''";
                mSelection += " AND " + DatabaseContract.COLUMN_VOLUME + " > " + 0;
                mSelection += " AND " + DatabaseContract.COLUMN_PROFIT + " > " + DatabaseContract.COLUMN_BONUS;
                mSelection += " AND " + DatabaseContract.COLUMN_NET + " > " + Math.max(Constants.STOCK_NATURAL_THRESHOLD, Constants.STOCK_NATURAL_THRESHOLD);
                break;

            case FILTER_TYPE_ALL:
                mSelection = null;
                break;

            default:
                mSelection = null;
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
        CursorLoader loader = null;

        switch (id) {
            case LOADER_ID_DEAL_LIST:
                setupSelection();

                loader = new CursorLoader(this,
                        DatabaseContract.StockDeal.CONTENT_URI,
                        DatabaseContract.StockDeal.PROJECTION_ALL, mSelection,
                        null, mSortOrder);
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
            case LOADER_ID_DEAL_LIST:
                setStockList(cursor);

                mLeftAdapter.swapCursor(cursor);
                mRightAdapter.swapCursor(cursor);
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
    }

    void setStockList(Cursor cursor) {
        ArrayMap<String, Stock> stockMap = new ArrayMap<>();
        StockDeal stockDeal = new StockDeal();

        try {
            mStockList.clear();
            if ((cursor != null) && (cursor.getCount() > 0)) {
                while (cursor.moveToNext()) {
                    stockDeal.set(cursor);
                    if (!stockMap.containsKey(stockDeal.getSE() + stockDeal.getCode())) {
                        Stock stock = new Stock();
                        stock.setSE(stockDeal.getSE());
                        stock.setCode(stockDeal.getCode());
                        mStockDatabaseManager.getStock(stock);
                        stockMap.put(stockDeal.getSE() + stockDeal.getCode(), stock);
                    }
                }
                cursor.moveToFirst();
                mStockList = new ArrayList<>(stockMap.values());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        if (parent.getId() == R.id.left_listview) {
            mDeal.setId(id);
            mHandler.sendEmptyMessage(MESSAGE_VIEW_STOCK_DEAL);
        } else {
            if (mCurrentActionMode == null) {
                mDeal.setId(id);
                mHandler.sendEmptyMessage(MESSAGE_VIEW_STOCK_CHAT);
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                   int position, long id) {

        if (mCurrentActionMode != null) {
            return false;
        }

        mDeal.setId(id);
        mCurrentActionMode = startActionMode(mModeCallBack);
        view.setSelected(true);
        return true;
    }

    boolean setTextViewValue(String key, View textView) {
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

    void getStock() {
        mStockDatabaseManager.getStockDealById(mDeal);

        mStock.setSE(mDeal.getSE());
        mStock.setCode(mDeal.getCode());

        mStockDatabaseManager.getStock(mStock);
    }

    private class CustomViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if ((view == null) || (cursor == null) || (columnIndex == -1)) {
                return false;
            }

            return false;
        }
    }
}
