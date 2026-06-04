/*
 * WorkoutChart is a custom View that draws a dynamic, curved Bezier-style line graph for calorie-burn data.
 */
package com.fitai.gym;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class WorkoutChart extends View {

    private Paint gridPaint;
    private Paint textPaint;
    private Paint boldTextPaint;
    private Paint line1Paint;
    private Paint line2Paint;
    private Paint highlightPaint;
    private Paint dotPaint;
    private Paint dotBgPaint;

    private String[] xLabels = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private String[] yLabels = {"0%", "20%", "40%", "60%", "80%", "100%"};

    private float[] data1 = {40, 15, 85, 20, 65, 75, 45};
    private float[] data2 = {20, 55, 30, 80, 45, 70, 85};
    
    private int highlightIndex = 5;

    public WorkoutChart(Context context) {
        super(context);
        init();
    }

    public WorkoutChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WorkoutChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(0x33FFFFFF);
        gridPaint.setStrokeWidth(2f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xCCFFFFFF);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.RIGHT);

        boldTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boldTextPaint.setColor(0xFFFFFFFF);
        boldTextPaint.setTextSize(32f);
        boldTextPaint.setFakeBoldText(true);
        boldTextPaint.setTextAlign(Paint.Align.RIGHT);

        line1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        line1Paint.setColor(0xFFFFFFFF);
        line1Paint.setStyle(Paint.Style.STROKE);
        line1Paint.setStrokeWidth(6f);

        line2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        line2Paint.setColor(0x66FFFFFF);
        line2Paint.setStyle(Paint.Style.STROKE);
        line2Paint.setStrokeWidth(3f);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(0xFFAECBFA);
        dotPaint.setStyle(Paint.Style.FILL);

        dotBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotBgPaint.setColor(0xFFFFFFFF);
        dotBgPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(float[] data1, float[] data2, String[] xLabels, String[] yLabels, int highlightIndex) {
        if (data1 != null) this.data1 = data1;
        if (data2 != null) this.data2 = data2;
        if (xLabels != null) this.xLabels = xLabels;
        if (yLabels != null) this.yLabels = yLabels;
        this.highlightIndex = highlightIndex;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float paddingLeft = 20f;
        float paddingRight = 120f;
        float paddingTop = 40f;
        float paddingBottom = 80f;

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        if (xLabels == null || xLabels.length == 0 || data1 == null || data1.length == 0) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setColor(Color.WHITE);
            canvas.drawText("No workout activity data yet", width / 2f, height / 2f, textPaint);
            return;
        }

        textPaint.setTextAlign(Paint.Align.LEFT);
        boldTextPaint.setTextAlign(Paint.Align.LEFT);
        for (int i = 0; i < yLabels.length; i++) {
            float y = height - paddingBottom - (i * chartHeight / (yLabels.length - 1));
            canvas.drawLine(paddingLeft, y, width - paddingRight + 20, y, gridPaint);
            
            Paint tp = (i == 2) ? boldTextPaint : textPaint;
            canvas.drawText(yLabels[i], width - paddingRight + 40, y + 10, tp);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
        boldTextPaint.setTextAlign(Paint.Align.CENTER);
        float stepX = xLabels.length > 1 ? chartWidth / (xLabels.length - 1) : chartWidth;
        int safeHighlightIndex = Math.max(0, Math.min(highlightIndex, xLabels.length - 1));
        for (int i = 0; i < xLabels.length; i++) {
            float x = paddingLeft + (i * stepX);
            Paint tp = (i == safeHighlightIndex) ? boldTextPaint : textPaint;
            canvas.drawText(xLabels[i], x, height - 20f, tp);
        }

        float highlightX = paddingLeft + (safeHighlightIndex * stepX);
        float highlightY1 = height - paddingBottom - (data1[Math.min(safeHighlightIndex, data1.length - 1)] / 100f * chartHeight);
        float highlightY2 = height - paddingBottom - (data2[Math.min(safeHighlightIndex, data2.length - 1)] / 100f * chartHeight);
        float topY = Math.min(highlightY1, highlightY2);
        
        highlightPaint.setShader(new LinearGradient(
                0, topY, 0, height - paddingBottom,
                0x88FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
        canvas.drawRect(highlightX - 30f, topY, highlightX + 30f, height - paddingBottom, highlightPaint);

        Path path2 = createBezierPath(data2, paddingLeft, stepX, chartHeight, height - paddingBottom);
        canvas.drawPath(path2, line2Paint);

        Path path1 = createBezierPath(data1, paddingLeft, stepX, chartHeight, height - paddingBottom);
        canvas.drawPath(path1, line1Paint);

        float dotX = highlightX;
        float dotY1 = height - paddingBottom - (data1[Math.min(safeHighlightIndex, data1.length - 1)] / 100f * chartHeight);
        canvas.drawCircle(dotX, dotY1, 10f, dotBgPaint);
        canvas.drawCircle(dotX, dotY1, 6f, dotPaint);
    }

    private Path createBezierPath(float[] data, float startX, float stepX, float chartHeight, float bottomY) {
        Path path = new Path();
        if (data.length == 0) return path;

        float currentX = startX;
        float currentY = bottomY - (data[0] / 100f * chartHeight);
        path.moveTo(currentX, currentY);

        for (int i = 0; i < data.length - 1; i++) {
            float nextX = startX + ((i + 1) * stepX);
            float nextY = bottomY - (data[i + 1] / 100f * chartHeight);

            float controlX1 = currentX + (nextX - currentX) / 2f;
            float controlY1 = currentY;
            float controlX2 = nextX - (nextX - currentX) / 2f;
            float controlY2 = nextY;

            path.cubicTo(controlX1, controlY1, controlX2, controlY2, nextX, nextY);

            currentX = nextX;
            currentY = nextY;
        }

        return path;
    }
}
