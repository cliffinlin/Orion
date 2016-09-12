package com.android.orion.stock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;

import com.android.orion.database.Stock;
import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;

public class SortableTable {
	private Context mContext;

	public SortableTable() {
	}

	public SortableTable(Context context) {
		mContext = context;
	}

	 public BaseTableAdapter getInstance() {
		SortableTableAdapter adapter = new SortableTableAdapter(mContext);

		List<String> header = getHeader();
		List<Stock> body = getBody();

		 adapter.setFirstHeader(getHeader().get(0));
//		 adapter.setHeader(header);
//		 adapter.setFirstBody(body);
//		 adapter.setBody(body);
//		 adapter.setSection(body);
		//
		// setListeners(adapter);

		return adapter;
	}

	List<String> getHeader() {
		final String headers[] = { "Name", "Price", "Net", "5M", "15M", "30M",
				"60M", "Day", "Week", "Month", };

		return Arrays.asList(headers);
	}

	List<Stock> getBody() {
		List<Stock> stockList = new ArrayList<Stock>();

		return stockList;
	}
}
