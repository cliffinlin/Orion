package com.android.orion.chart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import java.util.ArrayList;

public class CurveThumbnail extends Drawable {
	// 折线配置类
	public static class LineConfig {
		public final List<Float> xValues;
		public final List<Float> yValues;
		public final int color;
		public final float strokeWidth;

		public LineConfig(List<Float> xValues, List<Float> yValues,
						  int color, float strokeWidth) {
			this.xValues = xValues;
			this.yValues = yValues;
			this.color = color;
			this.strokeWidth = strokeWidth;
		}
	}

	// 十字标记配置类（最终修复版）
	public static class CrossMarkerConfig {
		public final float xValue;
		public final float yValue;
		public final int color;
		public final float strokeWidth;
		public final float size;

		public CrossMarkerConfig(float xValue, float yValue,
								 int color, float strokeWidth, float size) {
			this.xValue = xValue;
			this.yValue = yValue;
			this.color = color;
			this.strokeWidth = strokeWidth;
			this.size = size;
		}
	}

	private static class DrawnLine {
		final Path path;
		final Paint paint;

		DrawnLine(Path path, Paint paint) {
			this.path = path;
			this.paint = paint;
		}
	}

	private static class DrawnMarker {
		final float centerX;
		final float centerY;
		final Paint paint;
		final float halfSize;

		DrawnMarker(float centerX, float centerY, Paint paint, float halfSize) {
			this.centerX = centerX;
			this.centerY = centerY;
			this.paint = paint;
			this.halfSize = halfSize;
		}
	}

	private static class DataRange {
		final float minX, maxX, minY, maxY;

		DataRange(float minX, float maxX, float minY, float maxY) {
			this.minX = minX;
			this.maxX = maxX;
			this.minY = minY;
			this.maxY = maxY;
		}
	}

	private final int size;
	private final int backgroundColor;
	private final List<LineConfig> lines;
	private final CrossMarkerConfig markerConfig;
	private final Paint bgPaint;
	private final List<DrawnLine> drawnLines;
	private final DrawnMarker drawnMarker;

	public CurveThumbnail(int size, int backgroundColor,
						  List<LineConfig> lines, CrossMarkerConfig markerConfig) {
		this.size = size;
		this.backgroundColor = backgroundColor;
		this.lines = lines;
		this.markerConfig = markerConfig;

		bgPaint = new Paint();
		bgPaint.setColor(backgroundColor);
		bgPaint.setStyle(Paint.Style.FILL);

		DataRange range = calculateDataRange();
		this.drawnLines = buildAllLines(range);
		this.drawnMarker = buildMarker(range);
	}

	private List<DrawnLine> buildAllLines(DataRange range) {
		List<DrawnLine> result = new ArrayList<>();
		for (LineConfig lineConfig : lines) {
			Path path = buildPath(lineConfig.xValues, lineConfig.yValues, range);
			Paint paint = createLinePaint(lineConfig);
			result.add(new DrawnLine(path, paint));
		}
		return result;
	}

	private DrawnMarker buildMarker(DataRange range) {
		if (markerConfig == null) return null;

		float centerX = preciseMapToX(markerConfig.xValue, range.minX, range.maxX);
		float centerY = preciseMapToY(markerConfig.yValue, range.minY, range.maxY);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(markerConfig.color);
		paint.setStrokeWidth(markerConfig.strokeWidth);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeCap(Paint.Cap.SQUARE);

		return new DrawnMarker(centerX, centerY, paint, markerConfig.size / 2f);
	}

	private Paint createLinePaint(LineConfig lineConfig) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(lineConfig.strokeWidth);
		paint.setColor(lineConfig.color);
		paint.setStrokeCap(Paint.Cap.ROUND);
		return paint;
	}

	private DataRange calculateDataRange() {
		float minX = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = Float.MIN_VALUE;

		for (LineConfig line : lines) {
			for (float x : line.xValues) {
				minX = Math.min(minX, x);
				maxX = Math.max(maxX, x);
			}
			for (float y : line.yValues) {
				minY = Math.min(minY, y);
				maxY = Math.max(maxY, y);
			}
		}

		if (markerConfig != null) {
			minX = Math.min(minX, markerConfig.xValue);
			maxX = Math.max(maxX, markerConfig.xValue) + markerConfig.size / 2f;
			minY = Math.min(minY, markerConfig.yValue);
			maxY = Math.max(maxY, markerConfig.yValue);
		}

		if (minX == maxX) maxX = minX + 1;
		if (minY == maxY) maxY = minY + 1;

		float xMargin = (maxX - minX) * 0.05f;
		float yMargin = (maxY - minY) * 0.05f;

		return new DataRange(
				minX - xMargin,
				maxX + xMargin,
				minY - yMargin,
				maxY + yMargin
		);
	}

	private Path buildPath(List<Float> xValues, List<Float> yValues, DataRange range) {
		Path path = new Path();
		if (xValues.isEmpty() || yValues.isEmpty() || xValues.size() != yValues.size()) {
			return path;
		}

		float firstX = preciseMapToX(xValues.get(0), range.minX, range.maxX);
		float firstY = preciseMapToY(yValues.get(0), range.minY, range.maxY);
		path.moveTo(firstX, firstY);

		for (int i = 1; i < xValues.size(); i++) {
			float x = preciseMapToX(xValues.get(i), range.minX, range.maxX);
			float y = preciseMapToY(yValues.get(i), range.minY, range.maxY);
			path.lineTo(x, y);
		}

		return path;
	}

	// 高精度坐标映射方法
	private float preciseMapToX(float value, float minX, float maxX) {
		float normalized = (value - minX) / (maxX - minX);
		return Math.round(normalized * size * 100) / 100f;
	}

	private float preciseMapToY(float value, float minY, float maxY) {
		float normalized = (value - minY) / (maxY - minY);
		return size - Math.round(normalized * size * 100) / 100f;
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		canvas.drawRect(0, 0, size, size, bgPaint);

		for (DrawnLine line : drawnLines) {
			canvas.drawPath(line.path, line.paint);
		}

		if (drawnMarker != null) {
			// 确保完全对称的十字标记
			float left = Math.round((drawnMarker.centerX - drawnMarker.halfSize) * 100) / 100f;
			float right = Math.round((drawnMarker.centerX + drawnMarker.halfSize) * 100) / 100f;
			float top = Math.round((drawnMarker.centerY - drawnMarker.halfSize) * 100) / 100f;
			float bottom = Math.round((drawnMarker.centerY + drawnMarker.halfSize) * 100) / 100f;

			// 水平线（确保左右对称）
			canvas.drawLine(left, drawnMarker.centerY, right, drawnMarker.centerY, drawnMarker.paint);

			// 垂直线（确保上下对称）
			canvas.drawLine(drawnMarker.centerX, top, drawnMarker.centerX, bottom, drawnMarker.paint);
		}
	}

	@Override
	public void setAlpha(int alpha) {
		bgPaint.setAlpha(alpha);
		for (DrawnLine line : drawnLines) {
			line.paint.setAlpha(alpha);
		}
		if (drawnMarker != null) {
			drawnMarker.paint.setAlpha(alpha);
		}
	}

	@Override
	public void setColorFilter(@Nullable android.graphics.ColorFilter colorFilter) {
		bgPaint.setColorFilter(colorFilter);
		for (DrawnLine line : drawnLines) {
			line.paint.setColorFilter(colorFilter);
		}
		if (drawnMarker != null) {
			drawnMarker.paint.setColorFilter(colorFilter);
		}
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}

	@Override
	public int getIntrinsicWidth() {
		return size;
	}

	@Override
	public int getIntrinsicHeight() {
		return size;
	}
}