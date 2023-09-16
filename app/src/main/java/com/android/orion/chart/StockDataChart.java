package com.android.orion.chart;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.android.orion.database.StockQuant;
import com.android.orion.setting.Constants;
import com.android.orion.setting.Settings;
import com.android.orion.database.Stock;
import com.android.orion.database.StockDeal;
import com.android.orion.utility.Preferences;
import com.android.orion.utility.Utility;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleData;
import com.github.mikephil.charting.data.BubbleDataSet;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class StockDataChart {
	String mPeriod;
	public String mDescription;

	double mMainChartYMin = 0;
	double mMainChartYMax = 0;
    double mSubChartYMin = 0;
    double mSubChartYMax = 0;

	public ArrayList<String> mXValues = null;

	public ArrayList<BubbleEntry> mNaturalRallyList = null;
	public ArrayList<BubbleEntry> mUpwardTrendList = null;
	public ArrayList<BubbleEntry> mDownwardTrendList = null;
	public ArrayList<BubbleEntry> mNaturalReactionList = null;

	public ArrayList<CandleEntry> mCandleEntryList = null;
	public ArrayList<Entry> mAverage5EntryList = null;
	public ArrayList<Entry> mAverage10EntryList = null;
	public ArrayList<Entry> mDrawEntryList = null;
	public ArrayList<Entry> mStrokeEntryList = null;
	public ArrayList<Entry> mSegmentEntryList = null;
	public ArrayList<Entry> mLineEntryList = null;
	public ArrayList<Entry> mOutlineEntryList = null;
	public ArrayList<Entry> mOverlapHighEntryList = null;
	public ArrayList<Entry> mOverlapLowEntryList = null;
	public ArrayList<Entry> mBookValuePerShareList = null;
	public ArrayList<Entry> mNetProfitPerShareList = null;
	public ArrayList<Entry> mRoeList = null;
	public ArrayList<Entry> mRoiList = null;
	public ArrayList<BarEntry> mDividendEntryList = null;

	public ArrayList<Entry> mDIFEntryList = null;
	public ArrayList<Entry> mDEAEntryList = null;
	public ArrayList<BarEntry> mHistogramEntryList = null;

    ArrayList<Entry> mSubChartDrawEntryList = null;
    ArrayList<Entry> mSubChartStrokeEntryList = null;
    ArrayList<Entry> mSubChartSegmentEntryList = null;

	public ArrayList<LimitLine> mXLimitLineList = null;

	public CombinedData mCombinedDataMain = null;
	public CombinedData mCombinedDataSub = null;

	public StockDataChart() {
	}

	public StockDataChart(String period) {
		if (mXValues == null) {
			mXValues = new ArrayList<String>();
		}

		if (mNaturalRallyList == null) {
			mNaturalRallyList = new ArrayList<BubbleEntry>();
		}

		if (mUpwardTrendList == null) {
			mUpwardTrendList = new ArrayList<BubbleEntry>();
		}

		if (mDownwardTrendList == null) {
			mDownwardTrendList = new ArrayList<BubbleEntry>();
		}

		if (mNaturalReactionList == null) {
			mNaturalReactionList = new ArrayList<BubbleEntry>();
		}

		if (mCandleEntryList == null) {
			mCandleEntryList = new ArrayList<CandleEntry>();
		}

		if (mAverage5EntryList == null) {
			mAverage5EntryList = new ArrayList<Entry>();
		}

		if (mAverage10EntryList == null) {
			mAverage10EntryList = new ArrayList<Entry>();
		}

		if (mDrawEntryList == null) {
			mDrawEntryList = new ArrayList<Entry>();
		}

		if (mStrokeEntryList == null) {
			mStrokeEntryList = new ArrayList<Entry>();
		}

		if (mSegmentEntryList == null) {
			mSegmentEntryList = new ArrayList<Entry>();
		}

		if (mLineEntryList == null) {
			mLineEntryList = new ArrayList<Entry>();
		}

		if (mOutlineEntryList == null) {
			mOutlineEntryList = new ArrayList<Entry>();
		}

		if (mOverlapHighEntryList == null) {
			mOverlapHighEntryList = new ArrayList<Entry>();
		}

		if (mOverlapLowEntryList == null) {
			mOverlapLowEntryList = new ArrayList<Entry>();
		}

		if (mBookValuePerShareList == null) {
			mBookValuePerShareList = new ArrayList<Entry>();
		}

		if (mNetProfitPerShareList == null) {
			mNetProfitPerShareList = new ArrayList<Entry>();
		}

		if (mRoeList == null) {
			mRoeList = new ArrayList<Entry>();
		}

		if (mRoiList == null) {
			mRoiList = new ArrayList<Entry>();
		}

		if (mDividendEntryList == null) {
			mDividendEntryList = new ArrayList<BarEntry>();
		}

		if (mDIFEntryList == null) {
			mDIFEntryList = new ArrayList<Entry>();
		}

		if (mDEAEntryList == null) {
			mDEAEntryList = new ArrayList<Entry>();
		}

		if (mHistogramEntryList == null) {
			mHistogramEntryList = new ArrayList<BarEntry>();
		}

        if (mSubChartDrawEntryList == null) {
            mSubChartDrawEntryList = new ArrayList<Entry>();
        }

        if (mSubChartStrokeEntryList == null) {
            mSubChartStrokeEntryList = new ArrayList<Entry>();
        }

        if (mSubChartSegmentEntryList == null) {
            mSubChartSegmentEntryList = new ArrayList<Entry>();
        }

		if (mCombinedDataMain == null) {
			mCombinedDataMain = new CombinedData(mXValues);
		}

		if (mCombinedDataSub == null) {
			mCombinedDataSub = new CombinedData(mXValues);
		}

		if (mXLimitLineList == null) {
			mXLimitLineList = new ArrayList<LimitLine>();
		}

		mPeriod = period;
		mDescription = mPeriod;

//		setMainChartData();
//		setSubChartData();
	}

	public void setMainChartData(Context context) {
		mCombinedDataMain = new CombinedData(mXValues);

		if (Preferences.getBoolean(context, Settings.KEY_DISPLAY_MARKET_KEY,
				true)) {
			BubbleData bubbleData = new BubbleData(mXValues);
			if (mNaturalRallyList.size() > 0) {
				BubbleDataSet bubbleDataSet = new BubbleDataSet(mNaturalRallyList, "NUp");
				bubbleDataSet.setColor(Color.BLUE);
				bubbleData.addDataSet(bubbleDataSet);
			}

			if (mUpwardTrendList.size() > 0) {
				BubbleDataSet bubbleDataSet = new BubbleDataSet(mUpwardTrendList, "Up");
				bubbleDataSet.setColor(Color.RED);
				bubbleData.addDataSet(bubbleDataSet);
			}

			if (mDownwardTrendList.size() > 0) {
				BubbleDataSet bubbleDataSet = new BubbleDataSet(mDownwardTrendList, "Dn");
				bubbleDataSet.setColor(Color.GREEN);
				bubbleData.addDataSet(bubbleDataSet);
			}

			if (mNaturalReactionList.size() > 0) {
				BubbleDataSet bubbleDataSet = new BubbleDataSet(mNaturalReactionList, "NDn");
				bubbleDataSet.setColor(Color.YELLOW);
				bubbleData.addDataSet(bubbleDataSet);
			}
			mCombinedDataMain.setData(bubbleData);
		}

		if (mCandleEntryList.size() > 0) {
			CandleData candleData = new CandleData(mXValues);

			CandleDataSet candleDataSet = new CandleDataSet(mCandleEntryList,
					"K");
			candleDataSet.setDecreasingColor(Color.rgb(50, 128, 50));
			candleDataSet.setDecreasingPaintStyle(Paint.Style.FILL);
			candleDataSet.setIncreasingColor(Color.rgb(255, 50, 50));
			candleDataSet.setIncreasingPaintStyle(Paint.Style.FILL);
			candleDataSet.setShadowColorSameAsCandle(true);
			candleDataSet.setAxisDependency(AxisDependency.LEFT);
			candleDataSet.setColor(Color.RED);
			candleDataSet.setHighLightColor(Color.TRANSPARENT);
			candleDataSet.setDrawTags(true);

			candleData.addDataSet(candleDataSet);
			mCombinedDataMain.setData(candleData);
		}

		LineData lineData = new LineData(mXValues);

		if (mCandleEntryList.size() > 0) {
			{
				LineDataSet lineDataSet = new LineDataSet(mAverage5EntryList,
						"MA5");
				lineDataSet.setColor(Color.WHITE);
				lineDataSet.setDrawCircles(false);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}

			{
				LineDataSet lineDataSet = new LineDataSet(mAverage10EntryList,
						"MA10");
				lineDataSet.setColor(Color.CYAN);
				lineDataSet.setDrawCircles(false);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}
		}

		if (Preferences.getBoolean(context, Settings.KEY_DISPLAY_DRAW,
				true)) {
			if (mDrawEntryList.size() > 0) {
				LineDataSet lineDataSet = new LineDataSet(mDrawEntryList, "Draw");
				lineDataSet.setColor(Color.GRAY);
				lineDataSet.setCircleColor(Color.GRAY);
				lineDataSet.setCircleSize(0);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}
		}

		if (Preferences.getBoolean(context, Settings.KEY_DISPLAY_STROKE,
				false)) {
			if (mStrokeEntryList.size() > 0) {
				LineDataSet lineDataSet = new LineDataSet(mStrokeEntryList, "Stroke");
				lineDataSet.setColor(Color.YELLOW);
				lineDataSet.setCircleColor(Color.YELLOW);
				lineDataSet.setCircleSize(0);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}
		}

		if (Preferences.getBoolean(context, Settings.KEY_DISPLAY_SEGMENT,
				false)) {
			if (mSegmentEntryList.size() > 0) {
				LineDataSet lineDataSet = new LineDataSet(mSegmentEntryList,
						"Segment");
				lineDataSet.setColor(Color.BLACK);
				lineDataSet.setCircleColor(Color.BLACK);
				lineDataSet.setCircleSize(0);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}
		}

		if (Preferences.getBoolean(context, Settings.KEY_DISPLAY_LINE,
				false)) {
			if (mLineEntryList.size() > 0) {
				LineDataSet lineDataSet = new LineDataSet(mLineEntryList,
						"Line");
				lineDataSet.setColor(Color.RED);
				lineDataSet.setCircleColor(Color.RED);
				lineDataSet.setCircleSize(0);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}

//			if (mOutlineEntryList.size() > 0) {
//				LineDataSet lineDataSet = new LineDataSet(mOutlineEntryList,
//						"Outline");
//				lineDataSet.setColor(Color.RED);
//				lineDataSet.setCircleColor(Color.RED);
//				lineDataSet.setCircleSize(0);
//				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
//				lineData.addDataSet(lineDataSet);
//			}
		}

		if ((mOverlapHighEntryList.size() > 0)
				&& (mOverlapLowEntryList.size() > 0)) {
			{
				LineDataSet lineDataSet = new LineDataSet(
						mOverlapHighEntryList, "OverHigh");
				lineDataSet.setColor(Color.MAGENTA);
				lineDataSet.setDrawCircles(false);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}

			{
				LineDataSet lineDataSet = new LineDataSet(
						mOverlapLowEntryList, "OverLow");
				lineDataSet.setColor(Color.BLUE);
				lineDataSet.setDrawCircles(false);
				lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
				lineData.addDataSet(lineDataSet);
			}
		}

		if (mBookValuePerShareList.size() > 0) {
			LineDataSet lineDataSet = new LineDataSet(
					mBookValuePerShareList, "BPS");
			lineDataSet.setColor(Color.BLUE);
			lineDataSet.setDrawCircles(false);
			lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(lineDataSet);
		}

		if (mNetProfitPerShareList.size() > 0) {
			LineDataSet lineDataSet = new LineDataSet(
					mNetProfitPerShareList, "NPS");
			lineDataSet.setColor(Color.YELLOW);
			lineDataSet.setDrawCircles(false);
			lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(lineDataSet);
		}

		if (mRoeList.size() > 0) {
			LineDataSet lineDataSet = new LineDataSet(mRoeList, "ROE");
			lineDataSet.setColor(Color.DKGRAY);
			lineDataSet.setDrawCircles(false);
			lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(lineDataSet);
		}

		if (mRoiList.size() > 0) {
			LineDataSet lineDataSet = new LineDataSet(mRoiList, "ROI");
			lineDataSet.setColor(Color.RED);
			lineDataSet.setDrawCircles(false);
			lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			lineData.addDataSet(lineDataSet);
		}

		mCombinedDataMain.setData(lineData);

		if (mDividendEntryList.size() > 0) {
			BarData barData = new BarData(mXValues);
			BarDataSet dividendDataSet = new BarDataSet(mDividendEntryList,
					"Dividend");
			dividendDataSet.setBarSpacePercent(40f);
			dividendDataSet.setIncreasingColor(Color.rgb(255, 50, 50));
			dividendDataSet.setDecreasingColor(Color.rgb(50, 128, 50));
			dividendDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
			barData.addDataSet(dividendDataSet);

			mCombinedDataMain.setData(barData);
		}
	}

	public void setSubChartData(Context context) {
		mCombinedDataSub = new CombinedData(mXValues);

		BarData barData = new BarData(mXValues);
		BarDataSet histogramDataSet = new BarDataSet(mHistogramEntryList,
				"Histogram");
		histogramDataSet.setBarSpacePercent(40f);
		histogramDataSet.setIncreasingColor(Color.rgb(255, 50, 50));
		histogramDataSet.setDecreasingColor(Color.rgb(50, 128, 50));
		histogramDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		barData.addDataSet(histogramDataSet);

		LineData lineData = new LineData(mXValues);

		LineDataSet difDataSet = new LineDataSet(mDIFEntryList, "DIF");
		difDataSet.setColor(Color.YELLOW);
		// difDataSet.setCircleColor(Color.YELLOW);
		difDataSet.setDrawCircles(false);
		difDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(difDataSet);

		LineDataSet deaDataSet = new LineDataSet(mDEAEntryList, "DEA");
		deaDataSet.setColor(Color.WHITE);
		// deaDataSet.setCircleColor(Color.WHITE);
		deaDataSet.setDrawCircles(false);
		deaDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		lineData.addDataSet(deaDataSet);

        if (Preferences.getBoolean(context, Settings.KEY_DISPLAY_DRAW,
                true)) {
            transferMainChartDataToSubChartData(mDrawEntryList, mSubChartDrawEntryList);
            LineDataSet drawDataSet = new LineDataSet(mSubChartDrawEntryList, "Draw");
            drawDataSet.setColor(Color.GRAY);
            drawDataSet.setCircleColor(Color.GRAY);
            drawDataSet.setCircleSize(0);
            drawDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineData.addDataSet(drawDataSet);
        }

        if (Preferences.getBoolean(context, Settings.KEY_DISPLAY_STROKE,
                false)) {
            transferMainChartDataToSubChartData(mStrokeEntryList, mSubChartStrokeEntryList);
            LineDataSet strokeDataSet = new LineDataSet(mSubChartStrokeEntryList, "Stroke");
            strokeDataSet.setColor(Color.YELLOW);
            strokeDataSet.setCircleColor(Color.YELLOW);
            strokeDataSet.setCircleSize(0);
            strokeDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineData.addDataSet(strokeDataSet);
        }

        if (Preferences.getBoolean(context, Settings.KEY_DISPLAY_SEGMENT,
                false)) {
            transferMainChartDataToSubChartData(mSegmentEntryList, mSubChartSegmentEntryList);
            LineDataSet segmentDataSet = new LineDataSet(mSubChartSegmentEntryList,
                    "Segment");
            segmentDataSet.setColor(Color.BLACK);
            segmentDataSet.setCircleColor(Color.BLACK);
            segmentDataSet.setCircleSize(0);
            segmentDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineData.addDataSet(segmentDataSet);
        }

		mCombinedDataSub.setData(barData);
		mCombinedDataSub.setData(lineData);
	}

    public void setMainChartYMinMax(int index, ArrayList<Entry> drawEntryList, ArrayList<Entry> strokeEntryList, ArrayList<Entry> segmentEntryList) {
	    double draw = 0;
	    double stroke = 0;
	    double segment = 0;

	    if (drawEntryList == null || drawEntryList.size() == 0) {
	        return;
        }

	    if (strokeEntryList == null || strokeEntryList.size() == 0) {
            return;
        }

        if (segmentEntryList == null || segmentEntryList.size() == 0) {
            return;
        }

	    draw = drawEntryList.get(drawEntryList.size() - 1).getVal();
        stroke = strokeEntryList.get(strokeEntryList.size() - 1).getVal();
        segment = segmentEntryList.get(segmentEntryList.size() - 1).getVal();

        if (index == 0) {
            mMainChartYMin = Math.min(Math.min(draw, stroke), segment);
            mMainChartYMax = Math.max(Math.max(draw, stroke), segment);
        } else {
            mMainChartYMin = Math.min(Math.min(Math.min(draw, stroke), segment), mMainChartYMin);
            mMainChartYMax = Math.max(Math.max(Math.max(draw, stroke), segment), mMainChartYMax);
        }
    }

	public void setSubChartYMinMax(int index, ArrayList<Entry> difEntryList, ArrayList<Entry> deaEntryList) {
        double dif = 0;
        double dea = 0;

        if (difEntryList == null ||difEntryList.size() == 0) {
            return;
        }

        if (deaEntryList == null || deaEntryList.size() == 0) {
            return;
        }

        dif = difEntryList.get(difEntryList.size() - 1).getVal();
        dea = deaEntryList.get(deaEntryList.size() - 1).getVal();

        if (index == 0) {
	        mSubChartYMin = Math.min(dif, dea);
	        mSubChartYMax = Math.max(dif, dea);
        } else {
            mSubChartYMin = Math.min(Math.min(dif, dea), mSubChartYMin);
            mSubChartYMax = Math.max(Math.max(dif, dea), mSubChartYMax);
        }
    }

    void transferMainChartDataToSubChartData(ArrayList<Entry> mainChartEntryList, ArrayList<Entry> subChartEntryList) {
	    if ((mainChartEntryList == null) || (subChartEntryList == null)) {
	        return;
        }

        if (((mMainChartYMax - mMainChartYMin) == 0) || (mSubChartYMax - mSubChartYMin) == 0) {
            return;
        }

        subChartEntryList.clear();

        for (int i = 0; i < mainChartEntryList.size(); i++) {
           Entry mainChartEntry = mainChartEntryList.get(i);
           if (mainChartEntry != null) {
               Entry subChartEntry = new Entry((float) ((mainChartEntry.getVal() - mMainChartYMin) / (mMainChartYMax - mMainChartYMin)
                       * (mSubChartYMax - mSubChartYMin) + mSubChartYMin),
                       mainChartEntry.getXIndex());
               subChartEntryList.add(subChartEntry);
           }
        }
    }

	public void updateDescription(Stock stock) {
		mDescription = "";

		if (stock == null) {
			return;
		}

		mDescription += mPeriod;
		mDescription += " ";

		mDescription += stock.getName();
		mDescription += " ";

		mDescription += stock.getPrice() + "  ";

		if (stock.getNet() > 0) {
			mDescription += "+";
		} else if (stock.getNet() < 0) {
			mDescription += "-";
		}

		mDescription += stock.getNet() + "%" + "  ";

		mDescription += stock.getAction(mPeriod);
	}

	LimitLine createLimitLine(double limit, int color, String label) {
		LimitLine limitLine = new LimitLine(0);

		limitLine.enableDashedLine(10f, 10f, 0f);
		limitLine.setLineWidth(3);
		limitLine.setTextSize(10f);
		limitLine.setLabelPosition(LimitLabelPosition.LEFT_TOP);
		limitLine.setLimit((float) limit);
		limitLine.setLineColor(color);
		limitLine.setLabel(label);

		return limitLine;
	}

	public void updateLimitLines(@NonNull Stock stock, @NonNull ArrayList<StockDeal> stockDealList, @NonNull ArrayList<StockQuant> stockQuantList,
								 boolean keyDisplayLatest, boolean keyDisplayCost, boolean keyDisplayDeal, boolean keyDisplayQuant) {
		if (mXLimitLineList == null) {
			return;
		}

		mXLimitLineList.clear();

		updateLatestLimitLine(stock, stockDealList, keyDisplayLatest);
		updateCostLimitLine(stock, stockDealList, keyDisplayCost);
		updateDealLimitLine(stock, stockDealList, keyDisplayDeal);
		updateQuantLimitLine(stock, stockQuantList, keyDisplayQuant);
	}

	void updateLatestLimitLine(@NonNull Stock stock, @NonNull ArrayList<StockDeal> stockDealList, boolean keyDisplayLatest) {
		int color = Color.WHITE;
		String action = "";
		String label = "";
		LimitLine limitLine;

		if (mXLimitLineList == null) {
			return;
		}

		if (!keyDisplayLatest) {
			return;
		}

		action = stock.getAction(mPeriod);

		if (action.contains("L") || action.contains("B")) {
			color = Color.CYAN;
		} else if (action.contains("H") || action.contains("S")) {
			color = Color.MAGENTA;
		}

		label = "                                                     " + " "
				+ "Action" + " " + action + " ";
		limitLine = createLimitLine(stock.getPrice(), color, label);
		mXLimitLineList.add(limitLine);
	}

	void updateCostLimitLine(@NonNull Stock stock, @NonNull ArrayList<StockDeal> stockDealList, boolean keyDisplayCost) {
		int color = Color.WHITE;
		double cost = 0;
		double net = 0;
		String label = "";
		LimitLine limitLine;

		if (mXLimitLineList == null) {
			return;
		}

		if (!keyDisplayCost) {
			return;
		}

		cost = stock.getCost();

		if ((cost > 0) && (stock.getHold() > 0)) {
			net = Utility.Round(100 * (stock.getPrice() - cost) / cost,
					Constants.DOUBLE_FIXED_DECIMAL);
			color = Color.BLUE;
			label = "                                                     "
					+ " " + cost + " " + net + "%";
			limitLine = createLimitLine(cost, color, label);

			mXLimitLineList.add(limitLine);
		}
	}

	void updateDealLimitLine(@NonNull Stock stock, @NonNull ArrayList<StockDeal> stockDealList,
			boolean keyDisplayDeal) {
		double limit = 0;
		int color = Color.WHITE;
		String label = "";
		LimitLine limitLineDeal = new LimitLine(0);

		if (mXLimitLineList == null) {
			return;
		}

		if (!keyDisplayDeal) {
			return;
		}

		for (StockDeal stockDeal : stockDealList) {
			if ((stockDeal.getBuy() > 0) && (stockDeal.getSell() > 0)) {
				limit = stockDeal.getBuy();
			} else if (stockDeal.getBuy() > 0) {
				limit = stockDeal.getBuy();
			} else if (stockDeal.getSell() > 0) {
				limit = stockDeal.getSell();
			}

			if (stockDeal.getProfit() > 0) {
				color = Color.RED;
			} else {
				color = Color.GREEN;
			}

			if (stockDeal.getVolume() <= 0) {
				color = Color.YELLOW;
			}

			label = "               "
					+ "  " + limit
					+ "  " + stockDeal.getVolume()
					+ "  " + (int) stockDeal.getProfit()
					+ "  " + stockDeal.getNet() + "%"
					+ "  " + stockDeal.getAccount();

			limitLineDeal = createLimitLine(limit, color, label);

			mXLimitLineList.add(limitLineDeal);
		}
	}

	void updateQuantLimitLine(@NonNull Stock stock, @NonNull ArrayList<StockQuant> stockQuantList,
							 boolean keyDisplayDeal) {
		double limit = 0;
		int color = Color.WHITE;
		String label = "";
		LimitLine limitLineQuant = new LimitLine(0);

		if (mXLimitLineList == null) {
			return;
		}

		if (!keyDisplayDeal) {
			return;
		}

		for (StockQuant stockQuant : stockQuantList) {
			if ((stockQuant.getBuy() > 0) && (stockQuant.getSell() > 0)) {
				limit = stockQuant.getBuy();
			} else if (stockQuant.getBuy() > 0) {
				limit = stockQuant.getBuy();
			} else if (stockQuant.getSell() > 0) {
				limit = stockQuant.getSell();
			}

			if (stockQuant.getProfit() > 0) {
				color = Color.RED;
			} else {
				color = Color.GREEN;
			}

			if (stockQuant.getVolume() <= 0) {
				color = Color.YELLOW;
			}

			label = "               "
					+ "  " + limit
					+ "  " + stockQuant.getVolume()
					+ "  " + (int) stockQuant.getProfit()
					+ "  " + stockQuant.getNet() + "%"
					+ "  " + stockQuant.getAccount();

			limitLineQuant = createLimitLine(limit, color, label);

			mXLimitLineList.add(limitLineQuant);
		}
	}

	public void clear() {
		mXValues.clear();

		mNaturalRallyList.clear();
		mUpwardTrendList.clear();
		mDownwardTrendList.clear();
		mNaturalReactionList.clear();
		mCandleEntryList.clear();
		mAverage5EntryList.clear();
		mAverage10EntryList.clear();
		mDrawEntryList.clear();
		mStrokeEntryList.clear();
		mSegmentEntryList.clear();
		mLineEntryList.clear();
		mOutlineEntryList.clear();
		mOverlapHighEntryList.clear();
		mOverlapLowEntryList.clear();
		mBookValuePerShareList.clear();
		mNetProfitPerShareList.clear();
		mRoeList.clear();
		mRoiList.clear();
		mDividendEntryList.clear();
		mDIFEntryList.clear();
		mDEAEntryList.clear();
		mHistogramEntryList.clear();
        mSubChartDrawEntryList.clear();
        mSubChartStrokeEntryList.clear();
        mSubChartSegmentEntryList.clear();
	}
}
