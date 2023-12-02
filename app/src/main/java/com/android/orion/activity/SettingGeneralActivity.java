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

public class SettingGeneralActivity extends BaseActivity {

	SettingData mItemData;
	SettingViewItemData mItemViewData;
	List<SettingViewItemData> mListData = new ArrayList<>();
	SettingView mSettingView;
	ArrayMap<Integer, String> mKeyMap = new ArrayMap();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_setting_general);
		initView();
	}

	private void initView() {
		mSettingView = findViewById(R.id.setting_view_general);
		mSettingView.setOnSettingViewItemSwitchListener(new SettingView.onSettingViewItemSwitchListener() {

			@Override
			public void onSwitchChanged(int index, boolean isChecked) {
				Preferences.putBoolean(mKeyMap.get(index), isChecked);
			}
		});

		initView(Setting.SETTING_NOTIFICATION, R.string.setting_notification);
		initView(Setting.SETTING_INDEXES_WEIGHT, R.string.setting_indexes_weight);

		mSettingView.setAdapter(mListData);
	}

	void initView(String settingKey, int titleResId) {
		mKeyMap.put(mListData.size(), settingKey);
		mItemData = new SettingData();
		mItemData.setTitle(getString(titleResId));
		mItemData.setChecked(Preferences.getBoolean(settingKey, false));
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingGeneralActivity.this));
		mListData.add(mItemViewData);
	}
}
