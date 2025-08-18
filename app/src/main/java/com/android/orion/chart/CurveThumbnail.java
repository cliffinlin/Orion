package com.android.orion.chart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class CurveThumbnail extends Drawable {
	// 折线配置类
	public static class LineConfig {
		public final List<Float> xValues;
		public final List<Float> yValues;
		public final int color;
		public final float strokeWidth;
		public final boolean showLastPointMarker;

		public LineConfig(List<Float> xValues, List<Float> yValues,
		                  int color, float strokeWidth, boolean showLastPointMarker) {
			this.xValues = xValues;
			this.yValues = yValues;
			this.color = color;
			this.strokeWidth = strokeWidth;
			this.showLastPointMarker = showLastPointMarker;
		}
	}

	// 参考线配置类
	public static class ReferenceLineConfig {
		public final float value;
		public final int color;
		public final float strokeWidth;
		public final float[] dashPattern;

		public ReferenceLineConfig(float value, int color,
		                           float strokeWidth, float[] dashPattern) {
			this.value = value;
			this.color = color;
			this.strokeWidth = strokeWidth;
			this.dashPattern = dashPattern;
		}
	}

	// 内部绘制用类
	private static class DrawnLine {
		final Path path;
		final Paint linePaint;
		final Paint pointPaint;
		final float lastX;
		final float lastY;
		final boolean showMarker;

		DrawnLine(Path path, Paint linePaint, Paint pointPaint,
		          float lastX, float lastY, boolean showMarker) {
			this.path = path;
			this.linePaint = linePaint;
			this.pointPaint = pointPaint;
			this.lastX = lastX;
			this.lastY = lastY;
			this.showMarker = showMarker;
		}
	}

	private static class DrawnReferenceLine {
		final Path path;
		final Paint paint;

		DrawnReferenceLine(Path path, Paint paint) {
			this.path = path;
			this.paint = paint;
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
	private final ReferenceLineConfig referenceLine;

	private final Paint bgPaint;
	private final List<DrawnLine> drawnLines;
	private final DrawnReferenceLine drawnReferenceLine;

	public CurveThumbnail(int size, int backgroundColor,
	                      List<LineConfig> lines, ReferenceLineConfig referenceLine) {
		this.size = size;
		this.backgroundColor = backgroundColor;
		this.lines = lines;
		this.referenceLine = referenceLine;

		bgPaint = new Paint();
		bgPaint.setColor(backgroundColor);

		DataRange range = calculateDataRange();
		this.drawnLines = buildAllLines(range);
		this.drawnReferenceLine = buildReferenceLine(range);
	}

	private List<DrawnLine> buildAllLines(DataRange range) {
		List<DrawnLine> result = new java.util.ArrayList<>();
		for (LineConfig lineConfig : lines) {
			Path path = buildPath(lineConfig.xValues, lineConfig.yValues, range);
			Paint linePaint = createLinePaint(lineConfig);

			Paint pointPaint = null;
			float lastX = 0, lastY = 0;

			if (lineConfig.showLastPointMarker && !lineConfig.xValues.isEmpty() && !lineConfig.yValues.isEmpty()) {
				pointPaint = new Paint(linePaint);
				pointPaint.setStyle(Paint.Style.FILL);

				lastX = mapToX(
						lineConfig.xValues.get(lineConfig.xValues.size() - 1),
						range.minX, range.maxX
				);
				lastY = mapToY(
						lineConfig.yValues.get(lineConfig.yValues.size() - 1),
						range.minY, range.maxY
				);
			}

			result.add(new DrawnLine(
					path, linePaint, pointPaint,
					lastX, lastY, lineConfig.showLastPointMarker
			));
		}
		return result;
	}

	private DrawnReferenceLine buildReferenceLine(DataRange range) {
		float y = mapToY(referenceLine.value, range.minY, range.maxY);

		Path path = new Path();
		path.moveTo(0, y);
		path.lineTo(size, y);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(referenceLine.strokeWidth);
		paint.setColor(referenceLine.color);

		if (referenceLine.dashPattern != null) {
			paint.setPathEffect(new DashPathEffect(referenceLine.dashPattern, 0f));
		}

		return new DrawnReferenceLine(path, paint);
	}

	private Paint createLinePaint(LineConfig lineConfig) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(lineConfig.strokeWidth);
		paint.setColor(lineConfig.color);
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

		minY = Math.min(minY, referenceLine.value);
		maxY = Math.max(maxY, referenceLine.value);

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

		float firstX = mapToX(xValues.get(0), range.minX, range.maxX);
		float firstY = mapToY(yValues.get(0), range.minY, range.maxY);
		path.moveTo(firstX, firstY);

		for (int i = 1; i < xValues.size(); i++) {
			float x = mapToX(xValues.get(i), range.minX, range.maxX);
			float y = mapToY(yValues.get(i), range.minY, range.maxY);
			path.lineTo(x, y);
		}

		return path;
	}

	private float mapToX(float value, float minX, float maxX) {
		return (value - minX) / (maxX - minX) * size;
	}

	private float mapToY(float value, float minY, float maxY) {
		return size - ((value - minY) / (maxY - minY) * size);
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		canvas.drawRect(0, 0, size, size, bgPaint);

		for (DrawnLine line : drawnLines) {
			canvas.drawPath(line.path, line.linePaint);

			if (line.showMarker && line.pointPaint != null) {
				canvas.drawCircle(line.lastX, line.lastY, 3f, line.pointPaint);
			}
		}

		canvas.drawPath(drawnReferenceLine.path, drawnReferenceLine.paint);
	}

	@Override
	public void setAlpha(int alpha) {
		// 设置透明度，这里简单实现
		bgPaint.setAlpha(alpha);
		for (DrawnLine line : drawnLines) {
			line.linePaint.setAlpha(alpha);
			if (line.pointPaint != null) {
				line.pointPaint.setAlpha(alpha);
			}
		}
		drawnReferenceLine.paint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(@Nullable android.graphics.ColorFilter colorFilter) {
		// 设置颜色过滤器
		bgPaint.setColorFilter(colorFilter);
		for (DrawnLine line : drawnLines) {
			line.linePaint.setColorFilter(colorFilter);
			if (line.pointPaint != null) {
				line.pointPaint.setColorFilter(colorFilter);
			}
		}
		drawnReferenceLine.paint.setColorFilter(colorFilter);
	}

	@Override
	public int getOpacity() {
		// 返回不透明
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