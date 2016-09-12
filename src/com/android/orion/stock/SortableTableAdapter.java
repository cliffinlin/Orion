package com.android.orion.stock;

import java.util.Arrays;
import java.util.List;

import com.android.orion.R;
import com.android.orion.database.Stock;
import com.miguelbcr.tablefixheaders.TableFixHeaderAdapter;

import android.content.Context;

public class SortableTableAdapter
		extends
		TableFixHeaderAdapter<String, FirstHeaderCellViewGroup, List<String>, HeaderCellViewGroup, List<Stock>, FirstBodyCellViewGroup, BodyCellViewGroup, SectionCellViewGroup> {

	private Context mContext;

	public SortableTableAdapter(Context context) {
		super(context);

		mContext = context;
	}

	@Override
	protected FirstHeaderCellViewGroup inflateFirstHeader() {
		return new FirstHeaderCellViewGroup(mContext);
	}

	@Override
	protected HeaderCellViewGroup inflateHeader() {
		return new HeaderCellViewGroup(mContext);
	}

	@Override
	protected FirstBodyCellViewGroup inflateFirstBody() {
		return new FirstBodyCellViewGroup(mContext);
	}

	@Override
	protected BodyCellViewGroup inflateBody() {
		return new BodyCellViewGroup(mContext);
	}

	@Override
	protected SectionCellViewGroup inflateSection() {
		return new SectionCellViewGroup(mContext);
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
		return (int) mContext.getResources().getDimension(R.dimen._100dp);
	}

	@Override
	protected int getSectionHeight() {
		return (int) mContext.getResources().getDimension(R.dimen._60dp);
	}

	@Override
	protected int getBodyHeight() {
		return (int) mContext.getResources().getDimension(R.dimen._45dp);
	}

	@Override
	protected boolean isSection(List<List<Stock>> items, int row) {
		return false;
	}
}
