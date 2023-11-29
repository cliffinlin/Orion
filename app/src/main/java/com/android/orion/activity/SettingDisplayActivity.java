package com.android.orion.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.android.orion.R;
import com.android.orion.setting.Setting;
import com.android.orion.utility.Logger;
import com.dtr.settingview.lib.SettingButton;
import com.dtr.settingview.lib.SettingView;
import com.dtr.settingview.lib.entity.SettingData;
import com.dtr.settingview.lib.entity.SettingViewItemData;
import com.dtr.settingview.lib.item.BasicItemViewH;
import com.dtr.settingview.lib.item.SwitchItemView;

import java.util.ArrayList;
import java.util.List;

public class SettingDisplayActivity extends BaseActivity {

	SettingData mItemData;
	SettingViewItemData mItemViewData;
	List<SettingViewItemData> mListData = new ArrayList<>();
	SettingView mSettingView;

	int mDisplayNetIndex;
	int mDisplayCandleIndex;
	int mDisplayDrawIndex;
	int mDisplayStrokeIndex;
	int mDisplaySegmentIndex;
	int mDisplayLineIndex;

	public SettingDisplayActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_setting_display);
		initView();
	}

	private void initView() {
		mSettingView = findViewById(R.id.display_setting_view);
		mSettingView.setOnSettingViewItemSwitchListener(new SettingView.onSettingViewItemSwitchListener() {
			@Override
			public void onSwitchChanged(int index, boolean isChecked) {
				if (index == mDisplayNetIndex) {
					Setting.setDisplayNet(isChecked);
				} else if (index == mDisplayCandleIndex) {
					Setting.setDisplayCandle(isChecked);
				} else if (index == mDisplayDrawIndex) {
					Setting.setDisplayDraw(isChecked);
				} else if (index == mDisplayStrokeIndex) {
					Setting.setDisplayStroke(isChecked);
				} else if (index == mDisplaySegmentIndex) {
					Setting.setDisplaySegment(isChecked);
				} else if (index == mDisplayLineIndex) {
					Setting.setDisplayLine(isChecked);
				}
			}
		});

		mDisplayNetIndex = mListData.size();
		mItemData = new SettingData();
		mItemData.setTitle(getString(R.string.setting_display_net));
		mItemData.setChecked(Setting.getDisplayNet());
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingDisplayActivity.this));
		mListData.add(mItemViewData);

		mDisplayCandleIndex = mListData.size();
		mItemData = new SettingData();
		mItemData.setTitle(getString(R.string.setting_display_candle));
		mItemData.setChecked(Setting.getDisplayCandle());
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingDisplayActivity.this));
		mListData.add(mItemViewData);

		mDisplayDrawIndex = mListData.size();
		mItemData = new SettingData();
		mItemData.setTitle(getString(R.string.setting_display_draw));
		mItemData.setChecked(Setting.getDisplayDraw());
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingDisplayActivity.this));
		mListData.add(mItemViewData);

		mDisplayStrokeIndex = mListData.size();
		mItemData = new SettingData();
		mItemData.setTitle(getString(R.string.setting_display_stroke));
		mItemData.setChecked(Setting.getDisplayStroke());
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingDisplayActivity.this));
		mListData.add(mItemViewData);

		mDisplaySegmentIndex = mListData.size();
		mItemData = new SettingData();
		mItemData.setTitle(getString(R.string.setting_display_segment));
		mItemData.setChecked(Setting.getDisplaySegment());
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingDisplayActivity.this));
		mListData.add(mItemViewData);

		mDisplayLineIndex = mListData.size();
		mItemData = new SettingData();
		mItemData.setTitle(getString(R.string.setting_display_line));
		mItemData.setChecked(Setting.getDisplayLine());
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new SwitchItemView(SettingDisplayActivity.this));
		mListData.add(mItemViewData);

		mSettingView.setAdapter(mListData);
	}
}
