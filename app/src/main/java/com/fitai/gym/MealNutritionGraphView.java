/*
 * MealNutritionGraphView is a custom UI View that renders target vs consumed nutrition progress graphs.
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

public class MealNutritionGraphView extends View {

    private float[] dataPoints = new float[]{0.3f, 0.5f, 0.4f, 0.7f, 0.6f, 0.8f, 0.5f}; 
    private Paint linePaint;
    private Paint fillPaint;
    private Paint dotPaint;

    public MealNutritionGraphView(Context context) {
        super(context);
        init();
    }

    public MealNutritionGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MealNutritionGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#C58BF2")); 
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(6f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.parseColor("#92A3FD")); 
        dotPaint.setStyle(Paint.Style.FILL);
    }

    public void setDataPoints(float[] points) {
        if (points == null || points.length == 0) {
            this.dataPoints = new float[]{0.1f, 0.1f};
        } else {
            this.dataPoints = points;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();

        if (w == 0 || h == 0 || dataPoints.length < 2) return;

        float stepX = (float) w / (dataPoints.length - 1);

        Path path = new Path();
        Path fillPath = new Path();

        
        float startX = 0;
        float startY = h - (dataPoints[0] * h);
        path.moveTo(startX, startY);
        fillPath.moveTo(startX, h);
        fillPath.lineTo(startX, startY);

        for (int i = 1; i < dataPoints.length; i++) {
            float x1 = (i - 1) * stepX;
            float y1 = h - (dataPoints[i - 1] * h);
            float x2 = i * stepX;
            float y2 = h - (dataPoints[i] * h);

            
            float conX1 = x1 + (stepX / 2f);
            float conY1 = y1;
            float conX2 = x1 + (stepX / 2f);
            float conY2 = y2;

            path.cubicTo(conX1, conY1, conX2, conY2, x2, y2);
            fillPath.cubicTo(conX1, conY1, conX2, conY2, x2, y2);
        }

        fillPath.lineTo(w, h);
        fillPath.close();

        
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, h,
                Color.parseColor("#30C58BF2"), 
                Color.parseColor("#00FFFFFF"), 
                Shader.TileMode.CLAMP
        );
        fillPaint.setShader(gradient);

        
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(path, linePaint);

        
        for (int i = 0; i < dataPoints.length; i++) {
            float x = i * stepX;
            float y = h - (dataPoints[i] * h);
            canvas.drawCircle(x, y, 10f, dotPaint);

            
            Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            whitePaint.setColor(Color.WHITE);
            whitePaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x, y, 5f, whitePaint);
        }
    }
}
