package com.android.orion.chart;

import android.graphics.Matrix;
import android.util.ArrayMap;
import android.view.MotionEvent;

import com.android.orion.data.Period;
import com.android.orion.utility.Logger;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.ArrayList;

public class ChartSyncHelper {
	private final ArrayList<OnChartGestureListener> mOnChartGestureListenerList = new ArrayList<>();
	Logger Log = Logger.getLogger();

	public void registerOnChartGestureListener(OnChartGestureListener listener) {
		if (listener == null) {
			return;
		}
		if (!mOnChartGestureListenerList.contains(listener)) {
			mOnChartGestureListenerList.add(listener);
		}
	}

	public void unregisterOnChartGestureListener(OnChartGestureListener listener) {
		mOnChartGestureListenerList.remove(listener);
	}

	public void syncCharts(ArrayMap<Integer, CombinedChart> combinedChartArrayMap) {
		if (combinedChartArrayMap == null) {
			return;
		}
		for (int i = 0; i < Period.PERIODS.length; i++) {
			int mainPosition = 2 * i;
			int subPosition = 2 * i + 1;
			if (combinedChartArrayMap.containsKey(mainPosition) && combinedChartArrayMap.containsKey(subPosition)) {
				syncCharts(combinedChartArrayMap.get(mainPosition), combinedChartArrayMap.get(subPosition));
			}
		}
	}

	public void syncCharts(CombinedChart chart1, CombinedChart chart2) {
		if (chart1 == null || chart2 == null) {
			return;
		}
		// 为第一个图表设置手势监听器
		chart1.setOnChartGestureListener(new OnChartGestureListener() {
			@Override
			public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
			}

			@Override
			public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
			}

			@Override
			public void onChartLongPressed(MotionEvent me) {
				for (OnChartGestureListener listener : mOnChartGestureListenerList) {
					if (listener != null) {
						listener.onChartLongPressed(me);
					}
				}
			}

			@Override
			public void onChartDoubleTapped(MotionEvent me) {
				for (OnChartGestureListener listener : mOnChartGestureListenerList) {
					if (listener != null) {
						listener.onChartDoubleTapped(me);
					}
				}
			}

			@Override
			public void onChartSingleTapped(MotionEvent me) {
			}

			@Override
			public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
			}

			@Override
			public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
				if (scaleX == 1.0) {
					return;
				}
				syncZoom(chart1, chart2);
			}

			@Override
			public void onChartTranslate(MotionEvent me, float dX, float dY) {
				syncTranslation(chart1, chart2);
			}
		});

		// 为第二个图表设置手势监听器
		chart2.setOnChartGestureListener(new OnChartGestureListener() {
			@Override
			public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
			}

			@Override
			public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
			}

			@Override
			public void onChartLongPressed(MotionEvent me) {
			}

			@Override
			public void onChartDoubleTapped(MotionEvent me) {
			}

			@Override
			public void onChartSingleTapped(MotionEvent me) {
			}

			@Override
			public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
			}

			@Override
			public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
				if (scaleX == 1.0) {
					return;
				}
				syncZoom(chart2, chart1);
			}

			@Override
			public void onChartTranslate(MotionEvent me, float dX, float dY) {
				syncTranslation(chart2, chart1);
			}
		});
	}

	private void syncZoom(CombinedChart source, CombinedChart target) {
		if (source == null || target == null) {
			return;
		}
		Matrix srcMatrix = source.getViewPortHandler().getMatrixTouch();
		Matrix dstMatrix = target.getViewPortHandler().getMatrixTouch();
		dstMatrix.set(srcMatrix);  // 将缩放信息复制到目标图表
		target.getViewPortHandler().refresh(dstMatrix, target, true);  // 刷新目标图表
	}

	private void syncTranslation(CombinedChart source, CombinedChart target) {
		if (source == null || target == null) {
			return;
		}
		Matrix srcMatrix = source.getViewPortHandler().getMatrixTouch();
		Matrix dstMatrix = target.getViewPortHandler().getMatrixTouch();
		dstMatrix.set(srcMatrix);  // 将平移信息复制到目标图表
		target.getViewPortHandler().refresh(dstMatrix, target, true);  // 刷新目标图表
	}
}
