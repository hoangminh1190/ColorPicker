package com.aidangrabe.materialcolorpicker.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by aidan on 23/04/15.
 * A View that shows a given color as circle
 */
public class ColorCircleView extends View {

    // the color for this view
    private int mPadding;
    private Paint mPaint;

    // the thickness of the circle
    private int mThickness;

    public ColorCircleView(Context context) {
        this(context, null);
    }

    public ColorCircleView(Context context, int color) {

        this(context, null);
        setColor(color);

    }

    public ColorCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mThickness = 10;
        mPadding = mThickness * 2;

        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
//        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mThickness);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // clear the canvas
        canvas.drawColor(Color.TRANSPARENT);

        float radius = getWidth() / 2;
        canvas.drawCircle(radius, radius, radius - mPadding/2, mPaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // ensure the view maintains a 1:1 ratio
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    public int getColor() {
        return mPaint.getColor();
    }

}
