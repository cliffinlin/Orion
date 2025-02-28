package com.android.orion.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
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

import androidx.annotation.NonNull;

import com.android.orion.R;
import com.android.orion.database.DatabaseContract;
import com.android.orion.database.Stock;
import com.android.orion.setting.Constant;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Utility;
import com.android.orion.view.SyncHorizontalScrollView;

public class StockFinancialListActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	public static final int LOADER_ID_STOCK_FINANCIAL_LIST = 0;

	public static final int REQUEST_CODE_STOCK_INSERT = 0;

	public static final int mHeaderTextDefaultColor = Color.BLACK;
	public static final int mHeaderTextHighlightColor = Color.RED;

	String mSortOrderColumn = DatabaseContract.COLUMN_ROI;
	String mSortOrderDirection = DatabaseContract.ORDER_ASC;
	String mSortOrderDefault = mSortOrderColumn + mSortOrderDirection;
	String mSortOrder = mSortOrderDefault;

	SyncHorizontalScrollView mTitleSHSV = null;
	SyncHorizontalScrollView mContentSHSV = null;

	TextView mTextViewNameCode = null;
	TextView mTextViewPrice = null;
	TextView mTextViewNet = null;
	TextView mTextViewRoi = null;
	TextView mTextViewIR = null;
	TextView mTextViewIRR = null;
	TextView mTextViewRoe = null;
	TextView mTextViewPE = null;
	TextView mTextViewPB = null;
	TextView mTextViewHold = null;
	TextView mTextViewProfit = null;
	TextView mTextViewBonus = null;
	TextView mTextViewValuation = null;
	TextView mTextViewShare = null;
	TextView mTextViewMarketValue = null;
	TextView mTextViewMainBusinessIncome = null;
	TextView mTextViewMainBusinessIncomeInYear = null;
	TextView mTextViewNetProfit = null;
	TextView mTextViewNetProfitInYear = null;
	TextView mTextViewNetProfitMargin = null;
	TextView mDebtToNetAssetsRato = null;
	TextView mTextViewBookValuePerShare = null;
	TextView mTextViewCashFlowPerShare = null;
	TextView mTextViewNetProfitPerShare = null;
	TextView mTextViewNetProfitPerShareInYear = null;
	TextView mTextViewRate = null;
	TextView mTextViewDividend = null;
	TextView mTextViewYield = null;
	TextView mTextViewDividendRatio = null;
	TextView mTextViewRDate = null;

	ListView mLeftListView = null;
	ListView mRightListView = null;

	SimpleCursorAdapter mLeftAdapter = null;
	SimpleCursorAdapter mRightAdapter = null;

	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
				case MESSAGE_REFRESH:
					try {
						mDatabaseManager.loadStockArrayMap(mStockArrayMap);
						for (Stock stock : mStockArrayMap.values()) {
							Setting.setDownloadStock(stock.getSE(), stock.getCode(), 0);
							mStockDataProvider.download(stock);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stock_financial_list);

		mSortOrder = Preferences.getString(Setting.SETTING_SORT_ORDER_FINANCIAL_LIST,
				mSortOrderDefault);

		initHeader();
		setupListView();

		mLoaderManager.initLoader(LOADER_ID_STOCK_FINANCIAL_LIST, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stock_favorite_list, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;

			case R.id.action_new: {
				Intent intent = new Intent(this, StockActivity.class);
				intent.setAction(Constant.ACTION_FAVORITE_STOCK_INSERT);
				startActivityForResult(intent, REQUEST_CODE_STOCK_INSERT);
				return true;
			}

			case R.id.action_search:
				startActivity(new Intent(this, StockSearchActivity.class));
				return true;

			case R.id.action_refresh:
				try {
					mDatabaseManager.loadStockArrayMap(mStockArrayMap);
					for (Stock stock : mStockArrayMap.values()) {
						mDatabaseManager.deleteStockFinancial(stock);
						mDatabaseManager.deleteStockBonus(stock);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				mHandler.sendEmptyMessage(MESSAGE_REFRESH);
				return true;

			case R.id.action_setting:
				startActivity(new Intent(this, SettingActivity.class));
				return true;

			case R.id.action_load:
				performLoadFromFile();
				return true;

			case R.id.action_save:
				performSaveToFile();
				return true;

			case R.id.action_deal:
				startActivity(new Intent(this, StockDealListActivity.class));
				return true;

			case R.id.action_list:
				startActivity(new Intent(this, StockListActivity.class));
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
				case REQUEST_CODE_STOCK_INSERT:
					mStockDataProvider.download(mStock);
					break;

				default:
					break;
			}
		}
	}

	@Override
	public void onClick(@NonNull View view) {
		int id = view.getId();

		resetHeaderTextColor();
		setHeaderTextColor(id, mHeaderTextHighlightColor);

		switch (id) {
			case R.id.stock_name_code:
				mSortOrderColumn = DatabaseContract.COLUMN_CODE;
				break;
			case R.id.price:
				mSortOrderColumn = DatabaseContract.COLUMN_PRICE;
				break;
			case R.id.net:
				mSortOrderColumn = DatabaseContract.COLUMN_NET;
				break;
			case R.id.roi:
				mSortOrderColumn = DatabaseContract.COLUMN_ROI;
				break;
			case R.id.ir:
				mSortOrderColumn = DatabaseContract.COLUMN_IR;
				break;
			case R.id.irr:
				mSortOrderColumn = DatabaseContract.COLUMN_IRR;
				break;
			case R.id.roe:
				mSortOrderColumn = DatabaseContract.COLUMN_ROE;
				break;
			case R.id.pe:
				mSortOrderColumn = DatabaseContract.COLUMN_PE;
				break;
			case R.id.rate:
				mSortOrderColumn = DatabaseContract.COLUMN_RATE;
				break;
			case R.id.pb:
				mSortOrderColumn = DatabaseContract.COLUMN_PB;
				break;
			case R.id.valuation:
				mSortOrderColumn = DatabaseContract.COLUMN_VALUATION;
				break;
			case R.id.hold:
				mSortOrderColumn = DatabaseContract.COLUMN_HOLD;
				break;
			case R.id.profit:
				mSortOrderColumn = DatabaseContract.COLUMN_PROFIT;
				break;
			case R.id.bonus:
				mSortOrderColumn = DatabaseContract.COLUMN_BONUS;
				break;
			case R.id.share:
				mSortOrderColumn = DatabaseContract.COLUMN_SHARE;
				break;
			case R.id.market_value:
				mSortOrderColumn = DatabaseContract.COLUMN_MARKET_VALUE;
				break;
			case R.id.main_business_income:
				mSortOrderColumn = DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME;
				break;
			case R.id.main_business_income_in_year:
				mSortOrderColumn = DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME_IN_YEAR;
				break;
			case R.id.net_profit:
				mSortOrderColumn = DatabaseContract.COLUMN_NET_PROFIT;
				break;
			case R.id.net_profit_in_year:
				mSortOrderColumn = DatabaseContract.COLUMN_NET_PROFIT_IN_YEAR;
				break;
			case R.id.net_profit_margin:
				mSortOrderColumn = DatabaseContract.COLUMN_NET_PROFIT_MARGIN;
				break;
			case R.id.debt_to_net_assets_rato:
				mSortOrderColumn = DatabaseContract.COLUMN_DEBT_TO_NET_ASSETS_RATIO;
				break;
			case R.id.book_value_per_share:
				mSortOrderColumn = DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE;
				break;
			case R.id.cash_flow_per_share:
				mSortOrderColumn = DatabaseContract.COLUMN_CASH_FLOW_PER_SHARE;
				break;
			case R.id.net_profit_per_share:
				mSortOrderColumn = DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE;
				break;
			case R.id.net_profit_per_share_in_year:
				mSortOrderColumn = DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR;
				break;
			case R.id.dividend:
				mSortOrderColumn = DatabaseContract.COLUMN_DIVIDEND;
				break;
			case R.id.yield:
				mSortOrderColumn = DatabaseContract.COLUMN_YIELD;
				break;
			case R.id.dividend_ratio:
				mSortOrderColumn = DatabaseContract.COLUMN_DIVIDEND_RATIO;
				break;
			case R.id.r_date:
				mSortOrderColumn = DatabaseContract.COLUMN_R_DATE;
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

		Preferences.putString(Setting.SETTING_SORT_ORDER_FINANCIAL_LIST, mSortOrder);

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
		setHeaderTextColor(mTextViewNet, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewRoi, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewIR, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewIRR, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewRoe, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPE, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewPB, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewHold, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewProfit, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewBonus, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewValuation, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewShare, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMarketValue, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMainBusinessIncome, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewMainBusinessIncomeInYear, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNetProfit, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNetProfitInYear, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNetProfitMargin, mHeaderTextDefaultColor);
		setHeaderTextColor(mDebtToNetAssetsRato, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewBookValuePerShare, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewCashFlowPerShare, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNetProfitPerShare, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewNetProfitPerShareInYear,
				mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewRate, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDividend, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewYield, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewDividendRatio, mHeaderTextDefaultColor);
		setHeaderTextColor(mTextViewRDate, mHeaderTextDefaultColor);
	}

	void initHeader() {
		mTitleSHSV = findViewById(R.id.title_shsv);
		mContentSHSV = findViewById(R.id.content_shsv);

		if (mTitleSHSV != null && mContentSHSV != null) {
			mTitleSHSV.setScrollView(mContentSHSV);
			mContentSHSV.setScrollView(mTitleSHSV);
		}

		mTextViewNameCode = findViewById(R.id.stock_name_code);
		if (mTextViewNameCode != null) {
			mTextViewNameCode.setOnClickListener(this);
		}

		mTextViewPrice = findViewById(R.id.price);
		if (mTextViewPrice != null) {
			mTextViewPrice.setOnClickListener(this);
		}

		mTextViewNet = findViewById(R.id.net);
		if (mTextViewNet != null) {
			mTextViewNet.setOnClickListener(this);
			setVisibility(mTextViewNet, Setting.getDisplayNet());
		}

		mTextViewRoi = findViewById(R.id.roi);
		if (mTextViewRoi != null) {
			mTextViewRoi.setOnClickListener(this);
		}

		mTextViewIR = findViewById(R.id.ir);
		if (mTextViewIR != null) {
			mTextViewIR.setOnClickListener(this);
		}

		mTextViewIRR = findViewById(R.id.irr);
		if (mTextViewIRR != null) {
			mTextViewIRR.setOnClickListener(this);
		}

		mTextViewRoe = findViewById(R.id.roe);
		if (mTextViewRoe != null) {
			mTextViewRoe.setOnClickListener(this);
		}

		mTextViewPE = findViewById(R.id.pe);
		if (mTextViewPE != null) {
			mTextViewPE.setOnClickListener(this);
		}

		mTextViewPB = findViewById(R.id.pb);
		if (mTextViewPB != null) {
			mTextViewPB.setOnClickListener(this);
		}

		mTextViewHold = findViewById(R.id.hold);
		if (mTextViewHold != null) {
			mTextViewHold.setOnClickListener(this);
		}

		mTextViewProfit = findViewById(R.id.profit);
		if (mTextViewProfit != null) {
			mTextViewProfit.setOnClickListener(this);
		}

		mTextViewBonus = findViewById(R.id.bonus);
		if (mTextViewBonus != null) {
			mTextViewBonus.setOnClickListener(this);
		}

		mTextViewValuation = findViewById(R.id.valuation);
		if (mTextViewValuation != null) {
			mTextViewValuation.setOnClickListener(this);
		}

		mTextViewShare = findViewById(R.id.share);
		if (mTextViewShare != null) {
			mTextViewShare.setOnClickListener(this);
		}

		mTextViewMarketValue = findViewById(R.id.market_value);
		if (mTextViewMarketValue != null) {
			mTextViewMarketValue.setOnClickListener(this);
		}

		mTextViewMainBusinessIncome = findViewById(R.id.main_business_income);
		if (mTextViewMainBusinessIncome != null) {
			mTextViewMainBusinessIncome.setOnClickListener(this);
		}

		mTextViewMainBusinessIncomeInYear = findViewById(R.id.main_business_income_in_year);
		if (mTextViewMainBusinessIncomeInYear != null) {
			mTextViewMainBusinessIncomeInYear.setOnClickListener(this);
		}

		mTextViewNetProfit = findViewById(R.id.net_profit);
		if (mTextViewNetProfit != null) {
			mTextViewNetProfit.setOnClickListener(this);
		}

		mTextViewNetProfitInYear = findViewById(R.id.net_profit_in_year);
		if (mTextViewNetProfitInYear != null) {
			mTextViewNetProfitInYear.setOnClickListener(this);
		}

		mTextViewNetProfitMargin = findViewById(R.id.net_profit_margin);
		if (mTextViewNetProfitMargin != null) {
			mTextViewNetProfitMargin.setOnClickListener(this);
		}

		mDebtToNetAssetsRato = findViewById(R.id.debt_to_net_assets_rato);
		if (mDebtToNetAssetsRato != null) {
			mDebtToNetAssetsRato.setOnClickListener(this);
		}

		mTextViewBookValuePerShare = findViewById(R.id.book_value_per_share);
		if (mTextViewBookValuePerShare != null) {
			mTextViewBookValuePerShare.setOnClickListener(this);
		}

		mTextViewCashFlowPerShare = findViewById(R.id.cash_flow_per_share);
		if (mTextViewCashFlowPerShare != null) {
			mTextViewCashFlowPerShare.setOnClickListener(this);
		}

		mTextViewNetProfitPerShare = findViewById(R.id.net_profit_per_share);
		if (mTextViewNetProfitPerShare != null) {
			mTextViewNetProfitPerShare.setOnClickListener(this);
		}

		mTextViewNetProfitPerShareInYear = findViewById(R.id.net_profit_per_share_in_year);
		if (mTextViewNetProfitPerShareInYear != null) {
			mTextViewNetProfitPerShareInYear.setOnClickListener(this);
		}

		mTextViewRate = findViewById(R.id.rate);
		if (mTextViewRate != null) {
			mTextViewRate.setOnClickListener(this);
		}

		mTextViewDividend = findViewById(R.id.dividend);
		if (mTextViewDividend != null) {
			mTextViewDividend.setOnClickListener(this);
		}

		mTextViewYield = findViewById(R.id.yield);
		if (mTextViewYield != null) {
			mTextViewYield.setOnClickListener(this);
		}

		mTextViewDividendRatio = findViewById(R.id.dividend_ratio);
		if (mTextViewDividendRatio != null) {
			mTextViewDividendRatio.setOnClickListener(this);
		}

		mTextViewRDate = findViewById(R.id.r_date);
		if (mTextViewRDate != null) {
			mTextViewRDate.setOnClickListener(this);
		}

		if (mSortOrder.contains(DatabaseContract.COLUMN_CODE)) {
			setHeaderTextColor(mTextViewNameCode, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PRICE)) {
			setHeaderTextColor(mTextViewPrice, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET)) {
			setHeaderTextColor(mTextViewNet, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_ROI)) {
			setHeaderTextColor(mTextViewRoi, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_IR)) {
			setHeaderTextColor(mTextViewIR, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_IRR)) {
			setHeaderTextColor(mTextViewIRR, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_ROE)) {
			setHeaderTextColor(mTextViewRoe, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PE)) {
			setHeaderTextColor(mTextViewPE, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PB)) {
			setHeaderTextColor(mTextViewPB, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_HOLD)) {
			setHeaderTextColor(mTextViewHold, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_PROFIT)) {
			setHeaderTextColor(mTextViewProfit, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_BONUS)) {
			setHeaderTextColor(mTextViewBonus, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_VALUATION)) {
			setHeaderTextColor(mTextViewValuation, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_SHARE)) {
			setHeaderTextColor(mTextViewShare, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MARKET_VALUE)) {
			setHeaderTextColor(mTextViewMarketValue, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME)) {
			setHeaderTextColor(mTextViewMainBusinessIncome, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME_IN_YEAR)) {
			setHeaderTextColor(mTextViewMainBusinessIncomeInYear, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET_PROFIT)) {
			setHeaderTextColor(mTextViewNetProfit, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET_PROFIT_IN_YEAR)) {
			setHeaderTextColor(mTextViewNetProfitInYear, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_NET_PROFIT_MARGIN)) {
			setHeaderTextColor(mTextViewNetProfitMargin, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.COLUMN_DEBT_TO_NET_ASSETS_RATIO)) {
			setHeaderTextColor(mDebtToNetAssetsRato, mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE)) {
			setHeaderTextColor(mTextViewBookValuePerShare,
					mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.COLUMN_CASH_FLOW_PER_SHARE)) {
			setHeaderTextColor(mTextViewCashFlowPerShare,
					mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE)) {
			setHeaderTextColor(mTextViewNetProfitPerShare,
					mHeaderTextHighlightColor);
		} else if (mSortOrder
				.contains(DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR)) {
			setHeaderTextColor(mTextViewNetProfitPerShareInYear,
					mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_RATE)) {
			setHeaderTextColor(mTextViewRate, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DIVIDEND)) {
			setHeaderTextColor(mTextViewDividend, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_YIELD)) {
			setHeaderTextColor(mTextViewYield, mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_DIVIDEND_RATIO)) {
			setHeaderTextColor(mTextViewDividendRatio,
					mHeaderTextHighlightColor);
		} else if (mSortOrder.contains(DatabaseContract.COLUMN_R_DATE)) {
			setHeaderTextColor(mTextViewRDate, mHeaderTextHighlightColor);
		} else {
		}
	}

	void setupListView() {
		String[] mLeftFrom = new String[]{DatabaseContract.COLUMN_NAME,
				DatabaseContract.COLUMN_CODE};
		int[] mLeftTo = new int[]{R.id.name, R.id.code};

		String[] mRightFrom = new String[]{
				DatabaseContract.COLUMN_PRICE,
				DatabaseContract.COLUMN_NET,
				DatabaseContract.COLUMN_ROI,
				DatabaseContract.COLUMN_IR,
				DatabaseContract.COLUMN_IRR,
				DatabaseContract.COLUMN_ROE,
				DatabaseContract.COLUMN_PE,
				DatabaseContract.COLUMN_PB,
				DatabaseContract.COLUMN_HOLD,
				DatabaseContract.COLUMN_PROFIT,
				DatabaseContract.COLUMN_BONUS,
				DatabaseContract.COLUMN_VALUATION,
				DatabaseContract.COLUMN_SHARE,
				DatabaseContract.COLUMN_MARKET_VALUE,
				DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME,
				DatabaseContract.COLUMN_MAIN_BUSINESS_INCOME_IN_YEAR,
				DatabaseContract.COLUMN_NET_PROFIT,
				DatabaseContract.COLUMN_NET_PROFIT_IN_YEAR,
				DatabaseContract.COLUMN_NET_PROFIT_MARGIN,
				DatabaseContract.COLUMN_DEBT_TO_NET_ASSETS_RATIO,
				DatabaseContract.COLUMN_BOOK_VALUE_PER_SHARE,
				DatabaseContract.COLUMN_CASH_FLOW_PER_SHARE,
				DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE,
				DatabaseContract.COLUMN_NET_PROFIT_PER_SHARE_IN_YEAR,
				DatabaseContract.COLUMN_RATE,
				DatabaseContract.COLUMN_DIVIDEND,
				DatabaseContract.COLUMN_YIELD,
				DatabaseContract.COLUMN_DIVIDEND_RATIO,
				DatabaseContract.COLUMN_R_DATE};
		int[] mRightTo = new int[]{
				R.id.price,
				R.id.net,
				R.id.roi,
				R.id.ir,
				R.id.irr,
				R.id.roe,
				R.id.pe,
				R.id.pb,
				R.id.hold,
				R.id.profit,
				R.id.bonus,
				R.id.valuation,
				R.id.share,
				R.id.market_value,
				R.id.main_business_income,
				R.id.main_business_income_in_year,
				R.id.net_profit,
				R.id.net_profit_in_year,
				R.id.net_profit_margin,
				R.id.debt_to_net_assets_rato,
				R.id.book_value_per_share,
				R.id.cash_flow_per_share,
				R.id.net_profit_per_share,
				R.id.net_profit_per_share_in_year,
				R.id.rate,
				R.id.dividend,
				R.id.yield,
				R.id.dividend_ratio,
				R.id.r_date};

		mLeftListView = findViewById(R.id.left_listview);
		mLeftAdapter = new SimpleCursorAdapter(this,
				R.layout.activity_stock_list_left_item, null, mLeftFrom,
				mLeftTo, 0);
		if (mLeftListView != null) {
			mLeftAdapter.setViewBinder(new LeftViewBinder());
			mLeftListView.setAdapter(mLeftAdapter);
			mLeftListView.setOnItemClickListener(this);
			mLeftListView.setOnItemLongClickListener(this);
		}

		mRightListView = findViewById(R.id.right_listview);
		mRightAdapter = new SimpleCursorAdapter(this,
				R.layout.activity_stock_financial_list_right_item, null, mRightFrom, mRightTo, 0);
		if (mRightListView != null) {
			mRightAdapter.setViewBinder(new RightViewBinder());
			mRightListView.setAdapter(mRightAdapter);
			mRightListView.setOnItemClickListener(this);
			mRightListView.setOnItemLongClickListener(this);
		}
	}

	void restartLoader() {
		mLoaderManager.restartLoader(LOADER_ID_STOCK_FINANCIAL_LIST, null, this);
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
		CursorLoader loader = null;

		switch (id) {
			case LOADER_ID_STOCK_FINANCIAL_LIST:
				String selection = mDatabaseManager.hasFlagSelection(Stock.FLAG_FAVORITE);
				loader = new CursorLoader(this, DatabaseContract.Stock.CONTENT_URI,
						DatabaseContract.Stock.PROJECTION_ALL, selection, null,
						mSortOrder);
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
			case LOADER_ID_STOCK_FINANCIAL_LIST:
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
	                        long id) {

		if (id <= Stock.INVALID_ID) {
			return;
		}

		if (TextUtils.equals(mAction, Constant.ACTION_STOCK_ID)) {
			if (mIntent != null) {
				mIntent.putExtra(Constant.EXTRA_STOCK_ID, id);
				setResult(RESULT_OK, mIntent);
				finish();
			}
		} else {
			Intent intent = new Intent(this,
					StockFinancialChartListActivity.class);
			intent.putExtra(Constant.EXTRA_STOCK_LIST_SORT_ORDER,
					mSortOrder);
			intent.putExtra(Constant.EXTRA_STOCK_ID, id);
			startActivity(intent);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
	                               int position, long id) {
		Intent intent = new Intent(this, StockListActivity.class);
		startActivity(intent);
		return true;
	}

	void setLeftViewColor(View view, Cursor cursor) {
		if ((view == null) || (cursor == null)) {
			return;
		}

		String code = cursor.getString(cursor.getColumnIndex(DatabaseContract.COLUMN_CODE));
		TextView textView = (TextView) view;
		if (TextUtils.equals(mLoadingStockCode, code)) {
			textView.setTextColor(Color.RED);
		} else {
			textView.setTextColor(Color.GRAY);
		}
	}

	void setRightViewColor(View view, Cursor cursor) {
		if ((view == null) || (cursor == null)) {
			return;
		}

		int flag = cursor.getInt(cursor
				.getColumnIndex(DatabaseContract.COLUMN_FLAG));

		if (Utility.hasFlag(flag, Stock.FLAG_NOTIFY)) {
			view.setBackgroundColor(Color.rgb(240, 240, 240));
//			TextView textView = (TextView)view;
//			textView.setTextColor(Color.RED);
		}

		TextView textView = (TextView) view;
		textView.setTextSize(14f);
	}

	private class LeftViewBinder implements SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if ((view == null) || (cursor == null) || (columnIndex == -1)) {
				return false;
			}

			if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_CODE)) {
				setLeftViewColor(view, cursor);
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_NAME)) {
				setLeftViewColor(view, cursor);
			}

			return false;
		}
	}

	private class RightViewBinder implements SimpleCursorAdapter.ViewBinder {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if ((view == null) || (cursor == null) || (columnIndex == -1)) {
				return false;
			}

			if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_NET)) {
				return setVisibility(view, Setting.getDisplayNet());
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MONTH)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_MONTH));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_WEEK)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_WEEK));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_DAY)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_DAY));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN60)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_MIN60));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN30)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_MIN30));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN15)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_MIN15));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MIN5)) {
				return setVisibility(view, Setting.getPeriod(DatabaseContract.COLUMN_MIN5));
			} else if (columnIndex == cursor
					.getColumnIndex(DatabaseContract.COLUMN_MODIFIED)) {
			}

			return false;
		}
	}
}
