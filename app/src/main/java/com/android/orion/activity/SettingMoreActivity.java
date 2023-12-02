package com.android.orion.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.Menu;

import com.android.orion.R;
import com.dtr.settingview.lib.SettingView;
import com.dtr.settingview.lib.entity.SettingData;
import com.dtr.settingview.lib.entity.SettingViewItemData;
import com.dtr.settingview.lib.item.BasicItemViewH;

import java.util.ArrayList;
import java.util.List;

public class SettingMoreActivity extends BaseActivity {

	SettingData mItemData = null;
	SettingViewItemData mItemViewData = null;
	List<SettingViewItemData> mListData = new ArrayList<SettingViewItemData>();
	SettingView mSettingView = null;
	ArrayMap<Integer, Class<?>> mActivityMap = new ArrayMap();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_setting_more);

		initView();
	}

	void initView() {
		mSettingView = (SettingView) findViewById(R.id.more_setting_view);
		mSettingView.setOnSettingViewItemClickListener(new SettingView.onSettingViewItemClickListener() {

			@Override
			public void onItemClick(int index) {
				startActivity(new Intent(SettingMoreActivity.this, mActivityMap.get(index)));
			}
		});

		initView(SettingGeneralActivity.class, R.string.activity_title_setting_general);
		initView(SettingPeriodActivity.class, R.string.activity_title_setting_period);
		initView(SettingDisplayActivity.class, R.string.activity_title_setting_display);
		initView(SettingDebugActivity.class, R.string.activity_title_setting_debug);

		mSettingView.setAdapter(mListData);
	}

	void initView(Class<?> cls, int titleResId) {
		mActivityMap.put(mListData.size(), cls);
		mItemData = new SettingData();
		mItemData.setTitle(getString(titleResId));
		mItemViewData = new SettingViewItemData();
		mItemViewData.setData(mItemData);
		mItemViewData.setItemView(new BasicItemViewH(SettingMoreActivity.this));
		mListData.add(mItemViewData);
	}
}
