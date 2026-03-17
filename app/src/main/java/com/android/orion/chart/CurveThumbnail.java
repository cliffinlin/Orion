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
	private final List<ScatterConfig> scatterPoints;
	private final List<CircleConfig> circlePoints;
	private final List<SectorConfig> sectorConfigs;
	private final CrossMarkerConfig markerConfig;
	private final AxisConfig axisConfig;
	private final Paint bgPaint;
	private final List<DrawnLine> drawnLines;
	private final List<DrawnScatterPoint> drawnScatterPoints;
	private final List<DrawnCirclePoint> drawnCirclePoints;
	private final List<DrawnSector> drawnSectors;
	private final DrawnMarker drawnMarker;

	// 标记是否是雷达图模式
	private final boolean isRadarMode;

	// 包含AxisConfig的主构造函数
	public CurveThumbnail(int width, int height, int backgroundColor,
	                      List<LineConfig> lines, List<ScatterConfig> scatterPoints,
	                      List<CircleConfig> circlePoints, List<SectorConfig> sectorConfigs,
	                      CrossMarkerConfig markerConfig, AxisConfig axisConfig) {
		this.width = width;
		this.height = height;
		this.backgroundColor = backgroundColor;
		this.lines = lines != null ? lines : new ArrayList<LineConfig>();
		this.scatterPoints = scatterPoints != null ? scatterPoints : new ArrayList<ScatterConfig>();
		this.circlePoints = circlePoints != null ? circlePoints : new ArrayList<CircleConfig>();
		this.sectorConfigs = sectorConfigs != null ? sectorConfigs : new ArrayList<SectorConfig>();
		this.markerConfig = markerConfig;
		this.axisConfig = axisConfig;

		// 判断是否是雷达图模式（有扇形或圆圈）
		this.isRadarMode = (sectorConfigs != null && !sectorConfigs.isEmpty()) ||
				(circlePoints != null && !circlePoints.isEmpty());

		bgPaint = new Paint();
		bgPaint.setColor(backgroundColor);
		bgPaint.setStyle(Paint.Style.FILL);

		if (isRadarMode) {
			// 雷达图模式：所有元素使用统一的坐标系
			// 扇形需要角度转换
			this.drawnSectors = buildAllSectors();

			// 散点、折线、标记线：TrendAnalyzer已经计算了像素坐标，但使用的是数学坐标系
			// 需要在Canvas坐标系中正确显示：Y轴翻转
			this.drawnLines = buildAllLinesRadar();
			this.drawnScatterPoints = buildAllScatterPointsRadar();
			this.drawnCirclePoints = buildAllCirclePoints(); // 圆圈是对称的，不需要翻转
			this.drawnMarker = buildMarkerRadar();
		} else {
			// 非雷达图模式（如柱状图）：需要数据范围映射
			DataRange lineRange = calculateLineDataRange();
			DataRange scatterRange = calculateScatterDataRange();
			DataRange combinedRange = combineDataRanges(lineRange, scatterRange);

			this.drawnSectors = new ArrayList<>();  // 非雷达图模式没有扇形
			this.drawnLines = buildAllLinesMapped(combinedRange);
			this.drawnScatterPoints = buildAllScatterPointsMapped(combinedRange);
			this.drawnCirclePoints = buildAllCirclePoints();
			this.drawnMarker = buildMarkerMapped(combinedRange);
		}
	}

	// 保持原有构造函数兼容性
	public CurveThumbnail(int size, int backgroundColor,
	                      List<LineConfig> lines, CrossMarkerConfig markerConfig) {
		this(size, size, backgroundColor, lines, null, null, null, markerConfig, null);
	}

	public CurveThumbnail(int width, int height, int backgroundColor,
	                      List<LineConfig> lines, CrossMarkerConfig markerConfig) {
		this(width, height, backgroundColor, lines, null, null, null, markerConfig, null);
	}

	public CurveThumbnail(int width, int height, int backgroundColor,
	                      List<LineConfig> lines, List<ScatterConfig> scatterPoints,
	                      CrossMarkerConfig markerConfig) {
		this(width, height, backgroundColor, lines, scatterPoints, null, null, markerConfig, null);
	}

	public CurveThumbnail(int width, int height, int backgroundColor,
	                      List<LineConfig> lines, List<ScatterConfig> scatterPoints,
	                      List<CircleConfig> circlePoints, CrossMarkerConfig markerConfig) {
		this(width, height, backgroundColor, lines, scatterPoints, circlePoints, null, markerConfig, null);
	}

	public CurveThumbnail(int width, int height, int backgroundColor,
	                      List<LineConfig> lines, List<ScatterConfig> scatterPoints,
	                      List<CircleConfig> circlePoints, List<SectorConfig> sectorConfigs,
	                      CrossMarkerConfig markerConfig) {
		this(width, height, backgroundColor, lines, scatterPoints, circlePoints, sectorConfigs, markerConfig, null);
	}

	// 坐标轴配置类
	public static class AxisConfig {
		public final int color;
		public final float strokeWidth;
		public final boolean drawHorizontal;
		public final boolean drawVertical;
		public final boolean drawFromCenter;

		public AxisConfig(int color, float strokeWidth, boolean drawHorizontal,
		                  boolean drawVertical, boolean drawFromCenter) {
			this.color = color;
			this.strokeWidth = strokeWidth;
			this.drawHorizontal = drawHorizontal;
			this.drawVertical = drawVertical;
			this.drawFromCenter = drawFromCenter;
		}

		public AxisConfig(int color, float strokeWidth) {
			this(color, strokeWidth, true, true, false);
		}

		public static AxisConfig createRadarAxis(int color, float strokeWidth) {
			return new AxisConfig(color, strokeWidth, true, true, true);
		}
	}

	// 扇形配置类
	public static class SectorConfig {
		public final float centerX;
		public final float centerY;
		public final float radius;
		public final float startAngleMath;
		public final float sweepAngleMath;
		public final int color;
		public final boolean useCenter;

		public SectorConfig(float centerX, float centerY, float radius,
		                    float startAngleMath, float sweepAngleMath,
		                    int color, boolean useCenter) {
			this.centerX = centerX;
			this.centerY = centerY;
			this.radius = radius;
			this.startAngleMath = startAngleMath;
			this.sweepAngleMath = sweepAngleMath;
			this.color = color;
			this.useCenter = useCenter;
		}

		public float getCanvasStartAngle() {
			// 数学坐标系：0°指向右（东），90°指向上（北），正角度逆时针
			// Canvas坐标系：0°指向右（东），正角度顺时针

			// 修正角度偏移：当前显示顺时针旋转了90度，所以需要逆时针旋转90度
			// 公式：canvasAngle = -startAngleMath - 90
			float canvasAngle = -startAngleMath - 90;
			// 确保角度在0-360范围内
			canvasAngle = (canvasAngle + 360) % 360;
			return canvasAngle;
		}

		public float getCanvasSweepAngle() {
			// 扫过角度保持不变（正角度还是逆时针）
			return sweepAngleMath;
		}
	}

	// 构建所有扇形
	private List<DrawnSector> buildAllSectors() {
		List<DrawnSector> result = new ArrayList<>();
		for (int i = 0; i < sectorConfigs.size(); i++) {
			SectorConfig config = sectorConfigs.get(i);
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(config.color);

			float canvasStartAngle = config.getCanvasStartAngle();
			float canvasSweepAngle = config.getCanvasSweepAngle();

			DrawnSector sector = new DrawnSector(config.centerX, config.centerY,
					config.radius, canvasStartAngle,
					canvasSweepAngle, config.useCenter, paint);
			result.add(sector);
		}
		return result;
	}

	// 构建所有圆圈
	private List<DrawnCirclePoint> buildAllCirclePoints() {
		List<DrawnCirclePoint> result = new ArrayList<>();
		for (int i = 0; i < circlePoints.size(); i++) {
			CircleConfig circleConfig = circlePoints.get(i);
			float x = circleConfig.xValue;
			float y = circleConfig.yValue;
			Paint paint = createCirclePaint(circleConfig.color, circleConfig.strokeWidth);
			result.add(new DrawnCirclePoint(x, y, paint, circleConfig.radius));
		}
		return result;
	}

	// 雷达图模式：构建折线（Y轴翻转）
	private List<DrawnLine> buildAllLinesRadar() {
		List<DrawnLine> result = new ArrayList<>();
		for (LineConfig lineConfig : lines) {
			Path path = buildPathRadar(lineConfig.xValues, lineConfig.yValues);
			Paint paint = createLinePaint(lineConfig);
			result.add(new DrawnLine(path, paint));
		}
		return result;
	}

	// 雷达图模式：构建散点（Y轴翻转）
	private List<DrawnScatterPoint> buildAllScatterPointsRadar() {
		List<DrawnScatterPoint> result = new ArrayList<>();
		for (ScatterConfig scatterConfig : scatterPoints) {
			// Y轴翻转：y = height - y
			float x = scatterConfig.xValue;
			float y = height - scatterConfig.yValue;
			Paint paint = createScatterPaint(scatterConfig.color);
			result.add(new DrawnScatterPoint(x, y, paint, scatterConfig.radius));
		}
		return result;
	}

	// 雷达图模式：构建标记线（Y轴翻转）
	private DrawnMarker buildMarkerRadar() {
		if (markerConfig == null) return null;
		// Y轴翻转：y = height - y
		float centerX = markerConfig.xValue;
		float centerY = height - markerConfig.yValue;
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(markerConfig.color);
		paint.setStrokeWidth(markerConfig.strokeWidth);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeCap(Paint.Cap.SQUARE);
		return new DrawnMarker(centerX, centerY, paint, markerConfig.size / 2f);
	}

	// 非雷达图模式：需要映射的折线构建
	private List<DrawnLine> buildAllLinesMapped(DataRange range) {
		List<DrawnLine> result = new ArrayList<>();
		for (LineConfig lineConfig : lines) {
			Path path = buildPathMapped(lineConfig.xValues, lineConfig.yValues, range);
			Paint paint = createLinePaint(lineConfig);
			result.add(new DrawnLine(path, paint));
		}
		return result;
	}

	// 非雷达图模式：需要映射的散点构建
	private List<DrawnScatterPoint> buildAllScatterPointsMapped(DataRange range) {
		List<DrawnScatterPoint> result = new ArrayList<>();
		for (ScatterConfig scatterConfig : scatterPoints) {
			float x = mapToX(scatterConfig.xValue, range.minX, range.maxX);
			float y = mapToY(scatterConfig.yValue, range.minY, range.maxY);
			Paint paint = createScatterPaint(scatterConfig.color);
			result.add(new DrawnScatterPoint(x, y, paint, scatterConfig.radius));
		}
		return result;
	}

	// 非雷达图模式：需要映射的标记线构建
	private DrawnMarker buildMarkerMapped(DataRange range) {
		if (markerConfig == null) return null;
		float centerX = mapToX(markerConfig.xValue, range.minX, range.maxX);
		float centerY = mapToY(markerConfig.yValue, range.minY, range.maxY);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(markerConfig.color);
		paint.setStrokeWidth(markerConfig.strokeWidth);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeCap(Paint.Cap.SQUARE);
		return new DrawnMarker(centerX, centerY, paint, markerConfig.size / 2f);
	}

	private Paint createScatterPaint(int color) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(color);
		return paint;
	}

	private Paint createCirclePaint(int color, float strokeWidth) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(color);
		paint.setStrokeWidth(strokeWidth);
		return paint;
	}

	private Paint createLinePaint(LineConfig lineConfig) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(lineConfig.strokeWidth);
		paint.setColor(lineConfig.color);
		paint.setStrokeCap(Paint.Cap.ROUND);
		return paint;
	}

	// 雷达图模式：构建路径（Y轴翻转）
	private Path buildPathRadar(List<Float> xValues, List<Float> yValues) {
		Path path = new Path();
		if (xValues == null || yValues == null || xValues.isEmpty() || yValues.isEmpty() || xValues.size() != yValues.size()) {
			return path;
		}

		// Y轴翻转：y = height - y
		path.moveTo(xValues.get(0), height - yValues.get(0));
		for (int i = 1; i < xValues.size(); i++) {
			path.lineTo(xValues.get(i), height - yValues.get(i));
		}
		return path;
	}

	// 需要映射的路径构建
	private Path buildPathMapped(List<Float> xValues, List<Float> yValues, DataRange range) {
		Path path = new Path();
		if (xValues == null || yValues == null || xValues.isEmpty() || yValues.isEmpty() || xValues.size() != yValues.size()) {
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

	// 映射X坐标
	private float mapToX(float value, float minX, float maxX) {
		if (maxX == minX) {
			return width / 2f;
		}
		float normalized = (value - minX) / (maxX - minX);
		return normalized * width;
	}

	// 映射Y坐标（注意Y轴翻转）
	private float mapToY(float value, float minY, float maxY) {
		if (maxY == minY) {
			return height / 2f;
		}
		float normalized = (value - minY) / (maxY - minY);
		return height - normalized * height;
	}

	private DataRange calculateLineDataRange() {
		if (lines.isEmpty()) {
			return new DataRange(0, 1, 0, 1);
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
		return new DataRange(minX - xMargin, maxX + xMargin, minY - yMargin, maxY + yMargin);
	}

	private DataRange calculateScatterDataRange() {
		if (scatterPoints.isEmpty()) {
			return null;
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
		return new DataRange(minX - xMargin, maxX + xMargin, minY - yMargin, maxY + yMargin);
	}

	private DataRange combineDataRanges(DataRange lineRange, DataRange scatterRange) {
		DataRange result = lineRange;
		if (scatterRange != null) {
			if (result == null) {
				result = scatterRange;
			} else {
				result = mergeDataRanges(result, scatterRange);
			}
		}
		if (result == null) {
			return new DataRange(0, 1, 0, 1);
		}
		return result;
	}

	private DataRange mergeDataRanges(DataRange range1, DataRange range2) {
		float minX = Math.min(range1.minX, range2.minX);
		float maxX = Math.max(range1.maxX, range2.maxX);
		float minY = Math.min(range1.minY, range2.minY);
		float maxY = Math.max(range1.maxY, range2.maxY);
		return new DataRange(minX, maxX, minY, maxY);
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		Rect bounds = getBounds();
		int drawWidth = bounds.width() > 0 ? bounds.width() : width;
		int drawHeight = bounds.height() > 0 ? bounds.height() : height;

		// 绘制背景
		canvas.drawRect(0, 0, drawWidth, drawHeight, bgPaint);

		// 保存画布状态
		canvas.save();

		// 应用缩放（如果需要）
		if (drawWidth != width || drawHeight != height) {
			float scaleX = (float) drawWidth / width;
			float scaleY = (float) drawHeight / height;
			canvas.scale(scaleX, scaleY);
		}

		// 绘制扇形
		for (int i = 0; i < drawnSectors.size(); i++) {
			DrawnSector sector = drawnSectors.get(i);
			canvas.drawArc(sector.centerX - sector.radius,
					sector.centerY - sector.radius,
					sector.centerX + sector.radius,
					sector.centerY + sector.radius,
					sector.canvasStartAngle, sector.canvasSweepAngle,
					sector.useCenter, sector.paint);
		}

		// 绘制坐标轴
		drawAxis(canvas);

		// 绘制折线
		for (DrawnLine line : drawnLines) {
			canvas.drawPath(line.path, line.paint);
		}

		// 绘制散点
		for (DrawnScatterPoint point : drawnScatterPoints) {
			canvas.drawCircle(point.x, point.y, point.radius, point.paint);
		}

		// 绘制圆圈
		for (int i = 0; i < drawnCirclePoints.size(); i++) {
			DrawnCirclePoint circle = drawnCirclePoints.get(i);
			canvas.drawCircle(circle.x, circle.y, circle.radius, circle.paint);
		}

		// 绘制十字标记
		if (drawnMarker != null) {
			float left = drawnMarker.centerX - drawnMarker.halfSize;
			float right = drawnMarker.centerX + drawnMarker.halfSize;
			float top = drawnMarker.centerY - drawnMarker.halfSize;
			float bottom = drawnMarker.centerY + drawnMarker.halfSize;

			canvas.drawLine(left, drawnMarker.centerY, right, drawnMarker.centerY, drawnMarker.paint);
			canvas.drawLine(drawnMarker.centerX, top, drawnMarker.centerX, bottom, drawnMarker.paint);
		}

		// 恢复画布状态
		canvas.restore();
	}

	// 坐标轴绘制方法
	private void drawAxis(Canvas canvas) {
		if (axisConfig == null) {
			return;
		}

		float centerX = width / 2f;
		float centerY = height / 2f;

		Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		axisPaint.setColor(axisConfig.color);
		axisPaint.setStrokeWidth(axisConfig.strokeWidth);
		axisPaint.setStyle(Paint.Style.STROKE);

		if (axisConfig.drawHorizontal) {
			if (axisConfig.drawFromCenter) {
				canvas.drawLine(centerX, centerY, 0, centerY, axisPaint);
				canvas.drawLine(centerX, centerY, width, centerY, axisPaint);
			} else {
				canvas.drawLine(0, centerY, width, centerY, axisPaint);
			}
		}

		if (axisConfig.drawVertical) {
			if (axisConfig.drawFromCenter) {
				canvas.drawLine(centerX, centerY, centerX, 0, axisPaint);
				canvas.drawLine(centerX, centerY, centerX, height, axisPaint);
			} else {
				canvas.drawLine(centerX, 0, centerX, height, axisPaint);
			}
		}
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
		for (DrawnSector sector : drawnSectors) {
			sector.paint.setAlpha(alpha);
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
		for (DrawnSector sector : drawnSectors) {
			sector.paint.setColorFilter(colorFilter);
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

	// 配置类
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

	// 内部类
	private static class DrawnLine {
		final Path path;
		final Paint paint;
		DrawnLine(Path path, Paint paint) {
			this.path = path;
			this.paint = paint;
		}
	}

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

	private static class DrawnSector {
		final float centerX;
		final float centerY;
		final float radius;
		final float canvasStartAngle;
		final float canvasSweepAngle;
		final boolean useCenter;
		final Paint paint;
		DrawnSector(float centerX, float centerY, float radius,
		            float canvasStartAngle, float canvasSweepAngle,
		            boolean useCenter, Paint paint) {
			this.centerX = centerX;
			this.centerY = centerY;
			this.radius = radius;
			this.canvasStartAngle = canvasStartAngle;
			this.canvasSweepAngle = canvasSweepAngle;
			this.useCenter = useCenter;
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
}