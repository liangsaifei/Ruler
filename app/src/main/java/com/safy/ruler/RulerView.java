package com.safy.ruler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import static com.safy.ruler.DisplayUtils.getScreenWidth;

/**
 * Created by saifei on 2017/10/16.
 * 尺子View
 */

public class RulerView extends View {

    Paint paint;
    float strokeWidthUnit;
    float strokeWidth;
    int screenWidth;
    int lineHeightUnit;
    int lineHeight;
    int lineSpace;
    //一屏幕分成40 刻度
    int screenCount = 40;
    //最大宽度
    private int maxWidth;

    public RulerView(Context context) {
        this(context, null);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        screenWidth = getScreenWidth(getContext());
        //四倍屏幕宽度，即160刻度 加上初始化100cm 一共161刻度
        maxWidth = screenWidth * 4;
        lineHeightUnit = DisplayUtils.dip2px(getContext(), 20);
        lineSpace = screenWidth / screenCount;
        strokeWidthUnit = 2.5f;
        strokeWidth = strokeWidthUnit * 2;
        lineHeight = lineHeightUnit * 2;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(maxWidth, DisplayUtils.dip2px(getContext(), 100));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int startX = screenWidth / 2;
        int startY = 0;
        int endX = startX;
        strokeWidth = strokeWidthUnit * 2;
        lineHeight = lineHeightUnit * 2;
        paint.setStrokeWidth(strokeWidth);
        int endY = lineHeight;
        int j = 1;

        for (int i = 0; i < 161; i++) {
            canvas.drawLine(startX, startY, endX, endY, paint);
            startX += lineSpace;
            endX = startX;
            j++;
            if (j == 11) {
                j = 1;
                strokeWidth = strokeWidthUnit * 2;
                lineHeight = lineHeightUnit * 2;
            } else {
                lineHeight = lineHeightUnit;
                strokeWidth = strokeWidthUnit;
            }
            paint.setStrokeWidth(strokeWidth);
            endY = lineHeight;
        }
    }


}
