package com.android.orion.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.widget.Toast;

import com.android.orion.R;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.android.orion.utility.Preferences;
import com.dtr.settingview.lib.SettingButton;
import com.dtr.settingview.lib.SettingView;
import com.dtr.settingview.lib.entity.SettingData;
import com.dtr.settingview.lib.entity.SettingViewItemData;
import com.dtr.settingview.lib.item.SwitchItemView;

import java.util.ArrayList;
import java.util.List;

public class SettingDebugActivity extends BaseActivity {

	SettingData mItemData;
	SettingViewItemData mItemViewData;
	List<SettingViewItemData> mListData = new ArrayList<>();
	SettingView mSettingView;
	ArrayMap<Integer, String> mKeyMap = new ArrayMap();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_setting_debug);
		initView();
	}

	private void initView() {

		mSettingView = findViewById(R.id.setting_view_debug);
		mSettingView.setOnSettingViewItemSwitchListener(new SettingView.onSettingViewItemSwitchListener() {

			@Override
			public void onSwitchChanged(int index, boolean isChecked) {
				Preferences.putBoolean(mKeyMap.get(index), isChecked);

				if (mKeyMap.get(index).equals(Setting.SETTING_DEBUG_LOG)) {
					Logger.setDebug(isChecked);
					if (isChecked) {
						Toast.makeText(SettingDebugActivity.this, getString(R.string.log_is_on), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(SettingDebugActivity.this, getString(R.string.log_is_off), Toast.LENGTH_SHORT).show();
					}
				} else if (mKeyMap.get(index).equals(Setting.SETTING_DEBUG_LOOPBACK)) {
					if (isChecked) {
						Intent intent = new Intent(mContext, SettingLoopbackActivity.class);
						mContext.startActivity(intent);
					}
				}
			}
		});

		initView(Setting.SETTING_DEBUG_LOG, R.string.setting_debug_log);
		initView(Setting.SETTING_DEBUG_DIRECT, R.string.setting_debug_direct);
		initView(Setting.SETTING_DEBUG_LOOPBACK, R.string.setting_debug_loopback);

		mSettingView.setAdapter(mListData);
	}

	void initView(String settingKey, int titleResId) {
		mKeyMap.put(mListData.size(), settingKey);
		mItemData = new SettingData();
		mItemData.setTitle(getString(titleResId));
		mItemData.setChecked(Preferences.getBoolean(settingKey, false));
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingDebugActivity.this));
		mListData.add(mItemViewData);
	}
}