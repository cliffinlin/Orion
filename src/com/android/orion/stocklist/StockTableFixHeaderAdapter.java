package com.android.orion.stocklist;

import java.util.Arrays;
import java.util.List;

import android.content.Context;

import com.android.orion.R;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

public class StockTableFixHeaderAdapter
		extends
		TableFixHeaderAdapter<String, StockCellViewGroup, String, StockCellViewGroup, Nexus, StockCellViewGroup, StockCellViewGroup, StockCellViewGroup> {
	Context mContext;

	public StockTableFixHeaderAdapter(Context context) {
		super(context);
		
		mContext = context;
	}

	@Override
	protected StockCellViewGroup inflateFirstHeader() {
		return new StockCellViewGroup(mContext);
	}

	@Override
	protected StockCellViewGroup inflateHeader() {
		return new StockCellViewGroup(mContext);
	}

	@Override
	protected StockCellViewGroup inflateFirstBody() {
		return new StockCellViewGroup(mContext);
	}

	@Override
	protected StockCellViewGroup inflateBody() {
		return new StockCellViewGroup(mContext);
	}

	@Override
	protected StockCellViewGroup inflateSection() {
		return new StockCellViewGroup(mContext);
	}

	@Override
	protected List<Integer> getHeaderWidths() {
		Integer[] witdhs = {
				(int) mContext.getResources().getDimension(R.dimen._150dp),
				(int) mContext.getResources().getDimension(R.dimen._120dp),
				(int) mContext.getResources().getDimension(R.dimen._170dp),
				(int) mContext.getResources().getDimension(R.dimen._80dp),
				(int) mContext.getResources().getDimension(R.dimen._110dp),
				(int) mContext.getResources().getDimension(R.dimen._80dp),
				(int) mContext.getResources().getDimension(R.dimen._80dp) };

		return Arrays.asList(witdhs);
	}

	@Override
	protected int getHeaderHeight() {
		return (int) mContext.getResources().getDimension(R.dimen._35dp);
	}

	@Override
	protected int getSectionHeight() {
		return (int) mContext.getResources().getDimension(R.dimen._25dp);
	}

	@Override
	protected int getBodyHeight() {
		return (int) mContext.getResources().getDimension(R.dimen._45dp);
	}

	@Override
	protected boolean isSection(List<Nexus> items, int row) {
		return items.get(row).isSection();
	}
}
