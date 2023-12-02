package com.android.orion.activity;

import android.os.Bundle;
import android.util.ArrayMap;

import com.android.orion.R;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Preferences;
import com.dtr.settingview.lib.SettingView;
import com.dtr.settingview.lib.entity.SettingData;
import com.dtr.settingview.lib.entity.SettingViewItemData;
import com.dtr.settingview.lib.item.SwitchItemView;

import java.util.ArrayList;
import java.util.List;

public class SettingPeriodActivity extends BaseActivity {

	SettingData mItemData;
	SettingViewItemData mItemViewData;
	List<SettingViewItemData> mListData = new ArrayList<>();
	SettingView mSettingView;
	ArrayMap<Integer, String> mKeyMap = new ArrayMap();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_setting_period);
		initView();
	}

	private void initView() {
		mSettingView = findViewById(R.id.setting_view_period);
		mSettingView.setOnSettingViewItemSwitchListener(new SettingView.onSettingViewItemSwitchListener() {

			@Override
			public void onSwitchChanged(int index, boolean isChecked) {
				Preferences.putBoolean(mKeyMap.get(index), isChecked);
			}
		});

//		initView(Setting.SETTING_PERIOD_YEAR, R.string.setting_period_year);
//		initView(Setting.SETTING_PERIOD_QUARTER, R.string.setting_period_quarter);
		initView(Setting.SETTING_PERIOD_MONTH, R.string.setting_period_month);
		initView(Setting.SETTING_PERIOD_WEEK, R.string.setting_period_week);
		initView(Setting.SETTING_PERIOD_DAY, R.string.setting_period_day);
		initView(Setting.SETTING_PERIOD_MIN60, R.string.setting_period_min60);
		initView(Setting.SETTING_PERIOD_MIN30, R.string.setting_period_min30);
		initView(Setting.SETTING_PERIOD_MIN15, R.string.setting_period_min15);
		initView(Setting.SETTING_PERIOD_MIN5, R.string.setting_period_min5);
//		initView(Setting.SETTING_PERIOD_MIN1, R.string.setting_period_min1);

		mSettingView.setAdapter(mListData);
	}

	void initView(String settingKey, int titleResId) {
		mKeyMap.put(mListData.size(), settingKey);
		mItemData = new SettingData();
		mItemData.setTitle(getString(titleResId));
		mItemData.setChecked(Preferences.getBoolean(settingKey, false));
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingPeriodActivity.this));
		mListData.add(mItemViewData);
	}
}
