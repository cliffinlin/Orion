package com.android.orion.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.orion.R;
import com.android.orion.data.Period;
import com.android.orion.service.StockService;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Market;
import com.android.orion.utility.Utility;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

	Context mContext;
	Logger Log = Logger.getLogger();
	private long mExitTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//      for AppCompatActivity hide action bar
//		if (getSupportActionBar() != null) {
//			getSupportActionBar().hide();
//		}
		mContext = getApplicationContext();
		initSharedPreferences();

		RecyclerView recyclerView = findViewById(R.id.recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		List<HeaderItem> items = new ArrayList<>();
		items.add(new HeaderItem(
				R.drawable.ic_list,
				getString(R.string.favorite),
				new Intent(this, StockFavoriteListActivity.class)
		));
		items.add(new HeaderItem(
				R.drawable.ic_list,
				getString(R.string.financial),
				new Intent(this, StockFinancialListActivity.class)
		));
		items.add(new HeaderItem(
				R.drawable.ic_service,
				getString(R.string.setting),
				new Intent(this, SettingActivity.class)
		));
		items.add(new HeaderItem(
				R.drawable.ic_list,
				getString(R.string.trend),
				new Intent(this, StockTrendListActivity.class)
		));
		items.add(new HeaderItem(
				R.drawable.ic_list,
				getString(R.string.stock_statistics),
				new Intent(this, StockStatisticsChartListActivity.class)
		));
		items.add(new HeaderItem(
				R.drawable.ic_list,
				getString(R.string.deal),
				new Intent(this, StockDealListActivity.class)
		));
		items.add(new HeaderItem(
				R.drawable.ic_list,
				getString(R.string.quant),
				new Intent(this, StockQuantListActivity.class)
		));
		items.add(new HeaderItem(
				R.drawable.ic_about,
				getString(R.string.about),
				new Intent(this, AboutActivity.class)
		));

		HeaderAdapter adapter = new HeaderAdapter(items);
		recyclerView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_exit: {
				finish();
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				if (!Market.isTradingHours()) {
					Toast.makeText(this,
							getResources().getString(R.string.press_again_to_exit),
							Toast.LENGTH_SHORT).show();
				}
				mExitTime = System.currentTimeMillis();
			} else {
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startService();
		if (!Utility.isNetworkConnected(this)) {
			Toast.makeText(this,
					getResources().getString(R.string.network_unavailable),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	void initSharedPreferences() {
		PreferenceManager.getDefaultSharedPreferences(this);
		if (!Setting.getPreferenceInit()) {
			Setting.setPreferenceInit(true);

			Setting.setPeriod(Period.MONTH, Setting.SETTING_PERIOD_MONTH_DEFAULT);
			Setting.setPeriod(Period.WEEK, Setting.SETTING_PERIOD_WEEK_DEFAULT);
			Setting.setPeriod(Period.DAY, Setting.SETTING_PERIOD_DAY_DEFAULT);
			Setting.setPeriod(Period.MIN60, Setting.SETTING_PERIOD_MIN60_DEFAULT);
			Setting.setPeriod(Period.MIN30, Setting.SETTING_PERIOD_MIN30_DEFAULT);
			Setting.setPeriod(Period.MIN15, Setting.SETTING_PERIOD_MIN15_DEFAULT);
			Setting.setPeriod(Period.MIN5, Setting.SETTING_PERIOD_MIN5_DEFAULT);

			Setting.setDisplayMainIncome(Setting.SETTING_DISPLAY_MAIN_INCOME_DEFAULT);
			Setting.setDisplayRZValue(Setting.SETTING_DISPLAY_RZ_VALUE_DEFAULT);
			Setting.setDisplayAdaptive(Setting.SETTING_DISPLAY_ADAPTIVE_DEFAULT);
			Setting.setDisplayGroup(Setting.SETTING_DISPLAY_GROUP_DEFAULT);
			Setting.setDisplayFilled(Setting.SETTING_DISPLAY_FILLED_DEFAULT);
			Setting.setDisplayNet(Setting.SETTING_DISPLAY_NET_DEFAULT);
			Setting.setDisplayDraw(Setting.SETTING_DISPLAY_DRAW_DEFAULT);
			Setting.setDisplayStroke(Setting.SETTING_DISPLAY_STROKE_DEFAULT);
			Setting.setDisplaySegment(Setting.SETTING_DISPLAY_SEGMENT_DEFAULT);
			Setting.setDisplayLine(Setting.SETTING_DISPLAY_LINE_DEFAULT);
			Setting.setDisplayOutLine(Setting.SETTING_DISPLAY_OUT_LINE_DEFAULT);
			Setting.setDisplaySuperLine(Setting.SETTING_DISPLAY_SUPER_LINE_DEFAULT);
			Setting.setDisplayTrendLine(Setting.SETTING_DISPLAY_TREND_LINE_DEFAULT);
		}
	}

	public void startService() {
		Log.d("startService");
		Intent serviceIntent = new Intent(mContext, StockService.class);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(serviceIntent);
		} else {
			startService(serviceIntent);
		}
	}

	public void stopService() {
		Log.d("stopService");
		Intent serviceIntent = new Intent(mContext, StockService.class);
		stopService(serviceIntent);
	}

	public static class HeaderItem {
		private final int iconResId;
		private final String title;
		private final Intent intent;

		public HeaderItem(int iconResId, String title, Intent intent) {
			this.iconResId = iconResId;
			this.title = title;
			this.intent = intent;
		}

		public int getIconResId() {
			return iconResId;
		}

		public String getTitle() {
			return title;
		}

		public Intent getIntent() {
			return intent;
		}
	}

	public static class HeaderAdapter extends RecyclerView.Adapter<HeaderAdapter.ViewHolder> {

		private final List<HeaderItem> items;

		public HeaderAdapter(List<HeaderItem> items) {
			this.items = items;
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_header, parent, false);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			HeaderItem item = items.get(position);
			holder.icon.setImageResource(item.getIconResId());
			holder.title.setText(item.getTitle());

			holder.itemView.setOnClickListener(v -> {
				Intent intent = item.getIntent();
				v.getContext().startActivity(intent);
			});
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

		public static class ViewHolder extends RecyclerView.ViewHolder {
			ImageView icon;
			TextView title;

			public ViewHolder(@NonNull View itemView) {
				super(itemView);
				icon = itemView.findViewById(R.id.icon);
				title = itemView.findViewById(R.id.title);
			}
		}
	}
}