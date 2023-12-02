package com.android.orion.activity;

import android.content.Intent;
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

public class SettingDisplayActivity extends BaseActivity {

	SettingData mItemData;
	SettingViewItemData mItemViewData;
	List<SettingViewItemData> mListData = new ArrayList<>();
	SettingView mSettingView;
	ArrayMap<Integer, String> mKeyMap = new ArrayMap();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_setting_display);
		initView();
	}

	private void initView() {
		mSettingView = findViewById(R.id.setting_view_display);
		mSettingView.setOnSettingViewItemSwitchListener(new SettingView.onSettingViewItemSwitchListener() {

			@Override
			public void onSwitchChanged(int index, boolean isChecked) {
				Preferences.putBoolean(mKeyMap.get(index), isChecked);
			}
		});

		initView(Setting.SETTING_DISPLAY_NET, R.string.setting_display_net);
		initView(Setting.SETTING_DISPLAY_CANDLE, R.string.setting_display_candle);
		initView(Setting.SETTING_DISPLAY_DRAW, R.string.setting_display_draw);
		initView(Setting.SETTING_DISPLAY_STROKE, R.string.setting_display_stroke);
		initView(Setting.SETTING_DISPLAY_SEGMENT, R.string.setting_display_segment);
		initView(Setting.SETTING_DISPLAY_LINE, R.string.setting_display_line);
		initView(Setting.SETTING_DISPLAY_OVERLAP, R.string.setting_display_overlap);
		initView(Setting.SETTING_DISPLAY_LATEST, R.string.setting_display_latest);
		initView(Setting.SETTING_DISPLAY_COST, R.string.setting_display_cost);
		initView(Setting.SETTING_DISPLAY_DEAL, R.string.setting_display_deal);
		initView(Setting.SETTING_DISPLAY_BONUS, R.string.setting_display_bonus);
		initView(Setting.SETTING_DISPLAY_BPS, R.string.setting_display_bps);
		initView(Setting.SETTING_DISPLAY_NPS, R.string.setting_display_nps);
		initView(Setting.SETTING_DISPLAY_ROE, R.string.setting_display_roe);
		initView(Setting.SETTING_DISPLAY_ROI, R.string.setting_display_roi);

		initView(Setting.SETTING_DISPLAY_THRESHOLD, R.string.setting_display_threshold);
		initView(Setting.SETTING_DISPLAY_QUANT, R.string.setting_display_quant);

		mSettingView.setAdapter(mListData);
	}

	void initView(String settingKey, int titleResId) {
		mKeyMap.put(mListData.size(), settingKey);
		mItemData = new SettingData();
		mItemData.setTitle(getString(titleResId));
		mItemData.setChecked(Preferences.getBoolean(settingKey, false));
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingDisplayActivity.this));
		mListData.add(mItemViewData);
	}
}
