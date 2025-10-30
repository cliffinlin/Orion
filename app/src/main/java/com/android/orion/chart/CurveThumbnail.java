package com.android.orion.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CurveThumbnail extends Drawable {
	private final int width;
	private final int height;
	private final int backgroundColor;
	private final List<LineConfig> lines;
	private final List<ScatterConfig> scatterPoints; // 改为ScatterConfig列表
	private final List<CircleConfig> circlePoints; // 新增圆圈配置列表
	private final CrossMarkerConfig markerConfig;
	private final Paint bgPaint;
	private final List<DrawnLine> drawnLines;
	private final List<DrawnScatterPoint> drawnScatterPoints;
	private final List<DrawnCirclePoint> drawnCirclePoints; // 新增绘制的圆圈列表
	private final DrawnMarker drawnMarker;

	// 保持原有构造函数兼容性
	public CurveThumbnail(int size, int backgroundColor,
						  List<LineConfig> lines, CrossMarkerConfig markerConfig) {
		this(size, size, backgroundColor, lines, markerConfig);
	}

	// 新的构造函数，支持分别设置宽度和高度
	public CurveThumbnail(int width, int height, int backgroundColor,
						  List<LineConfig> lines, CrossMarkerConfig markerConfig) {
		this(width, height, backgroundColor, lines, null, null, markerConfig);
	}

	// 新增构造函数，支持散点配置
	public CurveThumbnail(int width, int height, int backgroundColor,
						  List<LineConfig> lines, List<ScatterConfig> scatterPoints,
						  CrossMarkerConfig markerConfig) {
		this(width, height, backgroundColor, lines, scatterPoints, null, markerConfig);
	}

	// 新增构造函数，支持散点和圆圈配置
	public CurveThumbnail(int width, int height, int backgroundColor,
						  List<LineConfig> lines, List<ScatterConfig> scatterPoints,
						  List<CircleConfig> circlePoints, CrossMarkerConfig markerConfig) {
		this.width = width;
		this.height = height;
		this.backgroundColor = backgroundColor;
		this.lines = lines != null ? lines : new ArrayList<LineConfig>();
		this.scatterPoints = scatterPoints != null ? scatterPoints : new ArrayList<ScatterConfig>();
		this.circlePoints = circlePoints != null ? circlePoints : new ArrayList<CircleConfig>();
		this.markerConfig = markerConfig;

		bgPaint = new Paint();
		bgPaint.setColor(backgroundColor);
		bgPaint.setStyle(Paint.Style.FILL);

		// 分别计算折线、散点和圆圈的数据范围
		DataRange lineRange = calculateLineDataRange();
		DataRange scatterRange = calculateScatterDataRange();
		DataRange circleRange = calculateCircleDataRange();

		// 合并数据范围
		DataRange combinedRange = combineDataRanges(lineRange, scatterRange, circleRange);

		this.drawnLines = buildAllLines(combinedRange);
		this.drawnScatterPoints = buildAllScatterPoints(combinedRange);
		this.drawnCirclePoints = buildAllCirclePoints(combinedRange);
		this.drawnMarker = buildMarker(combinedRange);
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

	// 构建所有散点
	private List<DrawnScatterPoint> buildAllScatterPoints(DataRange range) {
		List<DrawnScatterPoint> result = new ArrayList<>();
		for (ScatterConfig scatterConfig : scatterPoints) {
			DrawnScatterPoint point = buildScatterPoint(scatterConfig, range);
			result.add(point);
		}
		return result;
	}

	// 构建所有圆圈
	private List<DrawnCirclePoint> buildAllCirclePoints(DataRange range) {
		List<DrawnCirclePoint> result = new ArrayList<>();
		for (CircleConfig circleConfig : circlePoints) {
			DrawnCirclePoint circle = buildCirclePoint(circleConfig, range);
			result.add(circle);
		}
		return result;
	}

	// 构建单个散点
	private DrawnScatterPoint buildScatterPoint(ScatterConfig scatterConfig, DataRange range) {
		float x = preciseMapToX(scatterConfig.xValue, range.minX, range.maxX);
		float y = preciseMapToY(scatterConfig.yValue, range.minY, range.maxY);
		Paint paint = createScatterPaint(scatterConfig.color);
		return new DrawnScatterPoint(x, y, paint, scatterConfig.radius);
	}

	// 构建单个圆圈
	private DrawnCirclePoint buildCirclePoint(CircleConfig circleConfig, DataRange range) {
		float x = preciseMapToX(circleConfig.xValue, range.minX, range.maxX);
		float y = preciseMapToY(circleConfig.yValue, range.minY, range.maxY);
		Paint paint = createCirclePaint(circleConfig.color, circleConfig.strokeWidth);
		return new DrawnCirclePoint(x, y, paint, circleConfig.radius);
	}

	// 创建散点画笔
	private Paint createScatterPaint(int color) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(color);
		return paint;
	}

	// 创建圆圈画笔
	private Paint createCirclePaint(int color, float strokeWidth) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(color);
		paint.setStrokeWidth(strokeWidth);
		return paint;
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

	// 计算折线的数据范围
	private DataRange calculateLineDataRange() {
		if (lines.isEmpty()) {
			return new DataRange(0, 1, 0, 1); // 默认范围
		}

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

	// 计算散点的数据范围
	private DataRange calculateScatterDataRange() {
		if (scatterPoints.isEmpty()) {
			return null; // 没有散点数据
		}

		float minX = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = Float.MIN_VALUE;

		for (ScatterConfig scatter : scatterPoints) {
			minX = Math.min(minX, scatter.xValue);
			maxX = Math.max(maxX, scatter.xValue);
			minY = Math.min(minY, scatter.yValue);
			maxY = Math.max(maxY, scatter.yValue);
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

	// 计算圆圈的数据范围
	private DataRange calculateCircleDataRange() {
		if (circlePoints.isEmpty()) {
			return null; // 没有圆圈数据
		}

		float minX = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE;
		float maxY = Float.MIN_VALUE;

		for (CircleConfig circle : circlePoints) {
			minX = Math.min(minX, circle.xValue);
			maxX = Math.max(maxX, circle.xValue);
			minY = Math.min(minY, circle.yValue);
			maxY = Math.max(maxY, circle.yValue);
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

	// 合并数据范围
	private DataRange combineDataRanges(DataRange lineRange, DataRange scatterRange, DataRange circleRange) {
		DataRange result = lineRange;

		if (scatterRange != null) {
			if (result == null) {
				result = scatterRange;
			} else {
				result = mergeDataRanges(result, scatterRange);
			}
		}

		if (circleRange != null) {
			if (result == null) {
				result = circleRange;
			} else {
				result = mergeDataRanges(result, circleRange);
			}
		}

		// 如果所有数据范围都是null，返回默认范围
		if (result == null) {
			return new DataRange(0, 1, 0, 1);
		}

		return result;
	}

	// 合并两个数据范围
	private DataRange mergeDataRanges(DataRange range1, DataRange range2) {
		float minX = Math.min(range1.minX, range2.minX);
		float maxX = Math.max(range1.maxX, range2.maxX);
		float minY = Math.min(range1.minY, range2.minY);
		float maxY = Math.max(range1.maxY, range2.maxY);

		return new DataRange(minX, maxX, minY, maxY);
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

	// 高精度坐标映射方法 - 使用宽度和高度
	private float preciseMapToX(float value, float minX, float maxX) {
		if (maxX == minX) {
			return width / 2f;
		}
		float normalized = (value - minX) / (maxX - minX);
		return Math.round(normalized * width * 100) / 100f;
	}

	private float preciseMapToY(float value, float minY, float maxY) {
		if (maxY == minY) {
			// 所有y值相同时，显示在中间位置
			return height / 2f;
		}
		float normalized = (value - minY) / (maxY - minY);
		return height - Math.round(normalized * height * 100) / 100f;
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		// 使用实际绘制区域的宽度和高度
		Rect bounds = getBounds();
		int drawWidth = bounds.width() > 0 ? bounds.width() : width;
		int drawHeight = bounds.height() > 0 ? bounds.height() : height;

		canvas.drawRect(0, 0, drawWidth, drawHeight, bgPaint);

		// 保存画布状态，进行缩放以适应绘制区域
		canvas.save();
		if (drawWidth != width || drawHeight != height) {
			float scaleX = (float) drawWidth / width;
			float scaleY = (float) drawHeight / height;
			canvas.scale(scaleX, scaleY);
		}

		// 先绘制折线
		for (DrawnLine line : drawnLines) {
			canvas.drawPath(line.path, line.paint);
		}

		// 然后绘制散点（在折线之上）
		for (DrawnScatterPoint point : drawnScatterPoints) {
			canvas.drawCircle(point.x, point.y, point.radius, point.paint);
		}

		// 然后绘制圆圈（在散点之上）
		for (DrawnCirclePoint circle : drawnCirclePoints) {
			canvas.drawCircle(circle.x, circle.y, circle.radius, circle.paint);
		}

		// 最后绘制十字标记（在最上层）
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

		canvas.restore();
	}

	@Override
	public void setAlpha(int alpha) {
		bgPaint.setAlpha(alpha);
		for (DrawnLine line : drawnLines) {
			line.paint.setAlpha(alpha);
		}
		for (DrawnScatterPoint point : drawnScatterPoints) {
			point.paint.setAlpha(alpha);
		}
		for (DrawnCirclePoint circle : drawnCirclePoints) {
			circle.paint.setAlpha(alpha);
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
		for (DrawnScatterPoint point : drawnScatterPoints) {
			point.paint.setColorFilter(colorFilter);
		}
		for (DrawnCirclePoint circle : drawnCirclePoints) {
			circle.paint.setColorFilter(colorFilter);
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
		return width;
	}

	@Override
	public int getIntrinsicHeight() {
		return height;
	}

	// 折线配置类
	public static class LineConfig {
		public List<Float> xValues;
		public List<Float> yValues;
		public int color;
		public float strokeWidth;

		public LineConfig() {
		}

		public LineConfig(List<Float> xValues, List<Float> yValues,
						  int color, float strokeWidth) {
			this.xValues = xValues;
			this.yValues = yValues;
			this.color = color;
			this.strokeWidth = strokeWidth;
		}
	}

	// 散点配置类
	public static class ScatterConfig {
		public final float xValue;
		public final float yValue;
		public final int color;
		public final float radius;

		public ScatterConfig(float xValue, float yValue, int color, float radius) {
			this.xValue = xValue;
			this.yValue = yValue;
			this.color = color;
			this.radius = radius;
		}
	}

	// 圆圈配置类（新增）
	public static class CircleConfig {
		public final float xValue;
		public final float yValue;
		public final int color;
		public final float radius;
		public final float strokeWidth;

		public CircleConfig(float xValue, float yValue, int color, float radius, float strokeWidth) {
			this.xValue = xValue;
			this.yValue = yValue;
			this.color = color;
			this.radius = radius;
			this.strokeWidth = strokeWidth;
		}
	}

	// 十字标记配置类
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

	// 散点类
	private static class DrawnScatterPoint {
		final float x;
		final float y;
		final Paint paint;
		final float radius;

		DrawnScatterPoint(float x, float y, Paint paint, float radius) {
			this.x = x;
			this.y = y;
			this.paint = paint;
			this.radius = radius;
		}
	}

	// 圆圈类（新增）
	private static class DrawnCirclePoint {
		final float x;
		final float y;
		final Paint paint;
		final float radius;

		DrawnCirclePoint(float x, float y, Paint paint, float radius) {
			this.x = x;
			this.y = y;
			this.paint = paint;
			this.radius = radius;
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
}