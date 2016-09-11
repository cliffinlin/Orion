package com.android.orion.stock;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;

import com.android.orion.R;
import com.android.orion.database.Stock;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

public class SectionCellViewGroup extends CellViewGroup implements
		TableFixHeaderAdapter.SectionBinder<List<Stock>> {

	public SectionCellViewGroup(Context context) {
		super(context);
		init();
	}

	public SectionCellViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	void init() {
		init(R.layout.text_view_group);
	}

	@Override
	public void bindSection(List<Stock> item, int row, int column) {
	}
}
