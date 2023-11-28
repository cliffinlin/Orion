package com.android.orion.activity;

import android.os.Bundle;
import android.widget.Toast;

import com.android.orion.R;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.dtr.settingview.lib.SettingButton;
import com.dtr.settingview.lib.entity.SettingData;
import com.dtr.settingview.lib.entity.SettingViewItemData;
import com.dtr.settingview.lib.item.SwitchItemView;

import java.util.ArrayList;
import java.util.List;

public class SettingDisplayActivity extends BaseActivity {

	private SettingButton mSettingButtonDisplayNet;
	private SettingButton mSettingButtonDisplayCandle;
	private SettingButton mSettingButtonDisplayDraw;

	private SettingData mItemData = null;
	private SettingViewItemData mItemViewData = null;
	private List<SettingViewItemData> mListData = new ArrayList<SettingViewItemData>();

	public SettingDisplayActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_setting_display);

		mSettingButtonDisplayNet = findViewById(R.id.setting_button_display_net);
		mSettingButtonDisplayNet.setOnSettingButtonSwitchListener(new SettingButton.onSettingButtonSwitchListener() {

			@Override
			public void onSwitchChanged(boolean isChecked) {
				Setting.setDisplayNet(isChecked);
			}
		});

		mSettingButtonDisplayCandle = findViewById(R.id.setting_button_display_candle);
		mSettingButtonDisplayCandle.setOnSettingButtonSwitchListener(new SettingButton.onSettingButtonSwitchListener() {
			@Override
			public void onSwitchChanged(boolean isChecked) {
				Setting.setDisplayCandle(isChecked);
			}
		});

		mSettingButtonDisplayDraw = findViewById(R.id.setting_button_display_draw);
		mSettingButtonDisplayDraw.setOnSettingButtonSwitchListener(new SettingButton.onSettingButtonSwitchListener() {
			@Override
			public void onSwitchChanged(boolean isChecked) {
				Setting.setDisplayDraw(isChecked);
			}
		});

		initView();
	}

	private void initView() {
		mItemData = new SettingData();
		mItemData.setTitle(getString(R.string.setting_display_net));
		mItemData.setChecked(Setting.getDisplayNet());
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingDisplayActivity.this));
		mSettingButtonDisplayNet.setAdapter(mItemViewData);

		mItemData = new SettingData();
		mItemData.setTitle(getString(R.string.setting_display_candle));
		mItemData.setChecked(Setting.getDisplayCandle());
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingDisplayActivity.this));
		mSettingButtonDisplayCandle.setAdapter(mItemViewData);

		mItemData = new SettingData();
		mItemData.setTitle(getString(R.string.setting_display_draw));
		mItemData.setChecked(Setting.getDisplayDraw());
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingDisplayActivity.this));
		mSettingButtonDisplayDraw.setAdapter(mItemViewData);
	}
}
