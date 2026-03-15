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
	private final Paint bgPaint;
	private final List<DrawnLine> drawnLines;
	private final List<DrawnScatterPoint> drawnScatterPoints;
	private final List<DrawnCirclePoint> drawnCirclePoints;
	private final List<DrawnSector> drawnSectors;
	private final DrawnMarker drawnMarker;

	// 保持原有构造函数兼容性
	public CurveThumbnail(int size, int backgroundColor,
	                      List<LineConfig> lines, CrossMarkerConfig markerConfig) {
		this(size, size, backgroundColor, lines, markerConfig);
	}

	// 支持宽度和高度的构造函数
	public CurveThumbnail(int width, int height, int backgroundColor,
	                      List<LineConfig> lines, CrossMarkerConfig markerConfig) {
		this(width, height, backgroundColor, lines, null, null, null, markerConfig);
	}

	// 支持散点配置的构造函数
	public CurveThumbnail(int width, int height, int backgroundColor,
	                      List<LineConfig> lines, List<ScatterConfig> scatterPoints,
	                      CrossMarkerConfig markerConfig) {
		this(width, height, backgroundColor, lines, scatterPoints, null, null, markerConfig);
	}

	// 支持散点和圆圈配置的构造函数
	public CurveThumbnail(int width, int height, int backgroundColor,
	                      List<LineConfig> lines, List<ScatterConfig> scatterPoints,
	                      List<CircleConfig> circlePoints, CrossMarkerConfig markerConfig) {
		this(width, height, backgroundColor, lines, scatterPoints, circlePoints, null, markerConfig);
	}

	// 支持扇形配置的构造函数
	public CurveThumbnail(int width, int height, int backgroundColor,
	                      List<LineConfig> lines, List<ScatterConfig> scatterPoints,
	                      List<CircleConfig> circlePoints, List<SectorConfig> sectorConfigs,
	                      CrossMarkerConfig markerConfig) {
		this.width = width;
		this.height = height;
		this.backgroundColor = backgroundColor;
		this.lines = lines != null ? lines : new ArrayList<LineConfig>();
		this.scatterPoints = scatterPoints != null ? scatterPoints : new ArrayList<ScatterConfig>();
		this.circlePoints = circlePoints != null ? circlePoints : new ArrayList<CircleConfig>();
		this.sectorConfigs = sectorConfigs != null ? sectorConfigs : new ArrayList<SectorConfig>();
		this.markerConfig = markerConfig;

		bgPaint = new Paint();
		bgPaint.setColor(backgroundColor);
		bgPaint.setStyle(Paint.Style.FILL);

		// 计算数据范围 - 圆圈不参与数据范围计算
		DataRange lineRange = calculateLineDataRange();
		DataRange scatterRange = calculateScatterDataRange();
		DataRange combinedRange = combineDataRanges(lineRange, scatterRange);

		// 构建所有图形元素
		this.drawnSectors = buildAllSectors();                 // 扇形使用固定坐标
		this.drawnLines = buildAllLines(combinedRange);        // 折线需要映射
		this.drawnScatterPoints = buildAllScatterPoints(combinedRange);  // 散点需要映射
		this.drawnCirclePoints = buildAllCirclePoints();       // 圆圈使用固定坐标，不映射
		this.drawnMarker = buildMarker(combinedRange);
	}

	// 扇形配置类 - 使用数学坐标系角度
	public static class SectorConfig {
		public final float centerX;           // 圆心X坐标
		public final float centerY;           // 圆心Y坐标
		public final float radius;             // 半径
		public final float startAngleMath;     // 起始角度（度），数学坐标系：0°指向右（东），90°指向上（北）
		public final float sweepAngleMath;     // 扫过的角度（度），正值为逆时针
		public final int color;                 // 颜色
		public final boolean useCenter;        // 是否连接到圆心（形成扇形）

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

		// 转换为 Canvas 坐标系角度
		public float getCanvasStartAngle() {
			// 第一步：Y轴镜像
			float mirroredAngle = (360 - startAngleMath) % 360;
			// 第二步：旋转-90度使象限对齐
			float canvasAngle = (mirroredAngle - 90 + 360) % 360;
			return canvasAngle;
		}

		// Canvas 中的 sweepAngle 需要保持方向一致
		public float getCanvasSweepAngle() {
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

			// 使用转换后的 Canvas 角度
			float canvasStartAngle = config.getCanvasStartAngle();
			float canvasSweepAngle = config.getCanvasSweepAngle();

			DrawnSector sector = new DrawnSector(config.centerX, config.centerY,
					config.radius, canvasStartAngle,
					canvasSweepAngle, config.useCenter, paint);

			result.add(sector);
		}
		return result;
	}

	// 构建所有圆圈 - 使用固定坐标，不经过映射
	private List<DrawnCirclePoint> buildAllCirclePoints() {
		List<DrawnCirclePoint> result = new ArrayList<>();

		for (int i = 0; i < circlePoints.size(); i++) {
			CircleConfig circleConfig = circlePoints.get(i);
			// 圆圈使用原始坐标，不经过映射
			float x = circleConfig.xValue;
			float y = circleConfig.yValue;

			Paint paint = createCirclePaint(circleConfig.color, circleConfig.strokeWidth);
			result.add(new DrawnCirclePoint(x, y, paint, circleConfig.radius));
		}
		return result;
	}

	// 构建所有折线
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
			float x = preciseMapToX(scatterConfig.xValue, range.minX, range.maxX);
			float y = preciseMapToY(scatterConfig.yValue, range.minY, range.maxY);
			Paint paint = createScatterPaint(scatterConfig.color);
			result.add(new DrawnScatterPoint(x, y, paint, scatterConfig.radius));
		}
		return result;
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

		return new DataRange(
				minX - xMargin,
				maxX + xMargin,
				minY - yMargin,
				maxY + yMargin
		);
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

		return new DataRange(
				minX - xMargin,
				maxX + xMargin,
				minY - yMargin,
				maxY + yMargin
		);
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

	private float preciseMapToX(float value, float minX, float maxX) {
		if (maxX == minX) {
			return width / 2f;
		}
		float normalized = (value - minX) / (maxX - minX);
		return Math.round(normalized * width * 100) / 100f;
	}

	private float preciseMapToY(float value, float minY, float maxY) {
		if (maxY == minY) {
			return height / 2f;
		}
		float normalized = (value - minY) / (maxY - minY);
		return height - Math.round(normalized * height * 100) / 100f;
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		Rect bounds = getBounds();
		int drawWidth = bounds.width() > 0 ? bounds.width() : width;
		int drawHeight = bounds.height() > 0 ? bounds.height() : height;

		canvas.drawRect(0, 0, drawWidth, drawHeight, bgPaint);

		canvas.save();
		if (drawWidth != width || drawHeight != height) {
			float scaleX = (float) drawWidth / width;
			float scaleY = (float) drawHeight / height;
			canvas.scale(scaleX, scaleY);
		}

		// 先绘制扇形（最底层）
		for (int i = 0; i < drawnSectors.size(); i++) {
			DrawnSector sector = drawnSectors.get(i);

			canvas.drawArc(sector.centerX - sector.radius,
					sector.centerY - sector.radius,
					sector.centerX + sector.radius,
					sector.centerY + sector.radius,
					sector.canvasStartAngle, sector.canvasSweepAngle,
					sector.useCenter, sector.paint);
		}

		// 然后绘制折线
		for (DrawnLine line : drawnLines) {
			canvas.drawPath(line.path, line.paint);
		}

		// 然后绘制散点
		for (DrawnScatterPoint point : drawnScatterPoints) {
			canvas.drawCircle(point.x, point.y, point.radius, point.paint);
		}

		// 然后绘制圆圈
		for (int i = 0; i < drawnCirclePoints.size(); i++) {
			DrawnCirclePoint circle = drawnCirclePoints.get(i);

			canvas.drawCircle(circle.x, circle.y, circle.radius, circle.paint);
		}

		// 最后绘制十字标记
		if (drawnMarker != null) {
			float left = Math.round((drawnMarker.centerX - drawnMarker.halfSize) * 100) / 100f;
			float right = Math.round((drawnMarker.centerX + drawnMarker.halfSize) * 100) / 100f;
			float top = Math.round((drawnMarker.centerY - drawnMarker.halfSize) * 100) / 100f;
			float bottom = Math.round((drawnMarker.centerY + drawnMarker.halfSize) * 100) / 100f;

			canvas.drawLine(left, drawnMarker.centerY, right, drawnMarker.centerY, drawnMarker.paint);
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

	// 圆圈配置类
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
		final float canvasStartAngle;   // Canvas 坐标系起始角度
		final float canvasSweepAngle;   // Canvas 坐标系扫过角度
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