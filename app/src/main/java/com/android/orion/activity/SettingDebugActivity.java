package com.android.orion.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.widget.Toast;

import com.android.orion.R;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.dtr.settingview.lib.SettingButton;
import com.dtr.settingview.lib.SettingView;
import com.dtr.settingview.lib.entity.SettingData;
import com.dtr.settingview.lib.entity.SettingViewItemData;
import com.dtr.settingview.lib.item.BasicItemViewH;
import com.dtr.settingview.lib.item.CheckItemViewV;
import com.dtr.settingview.lib.item.ImageItemView;
import com.dtr.settingview.lib.item.SwitchItemView;

import java.util.ArrayList;
import java.util.List;

public class SettingDebugActivity extends BaseActivity {

	private SettingButton mSettingButtonLog = null;

	private SettingData mItemData = null;
	private SettingViewItemData mItemViewData = null;
	private List<SettingViewItemData> mListData = new ArrayList<SettingViewItemData>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_debug);

		mSettingButtonLog = (SettingButton) findViewById(R.id.setting_button_log);
		mSettingButtonLog.setOnSettingButtonSwitchListener(new SettingButton.onSettingButtonSwitchListener() {

			@Override
			public void onSwitchChanged(boolean isChecked) {
				Setting.setDebugLog(isChecked);
				Logger.setDebug(isChecked);
				if (isChecked) {
					Toast.makeText(SettingDebugActivity.this, getString(R.string.log_is_on), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(SettingDebugActivity.this, getString(R.string.log_is_off), Toast.LENGTH_SHORT).show();
				}
			}
		});

		initView();
	}

	private void initView() {
		mItemData = new SettingData();
		mItemData.setTitle(getString(R.string.setting_debug_log));
		mItemData.setChecked(Setting.getDebugLog());
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingDebugActivity.this));
		mSettingButtonLog.setAdapter(mItemViewData);
	}
}
