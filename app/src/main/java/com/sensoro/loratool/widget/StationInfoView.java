package com.sensoro.loratool.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.text.DecimalFormat;

/**
 * Created by sensoro on 16/6/17.
 */

public class StationInfoView extends ImageView {

    public static final int LIGHT = 0;
    public static final int TEMPTURE_INNER = 1;
    public static final int TEMPTURE_OUT = 2;
    private static final float CURRENT_X = 150;
    private static final float CURRENT_Y = 123;
    private Context mContext;
    private Paint paint = new Paint();
    private double mTempture;
    private double mLightValue;
    private float mDpiWidth;
    private float mDpiHeight;
    private int mDrawableId;
    private int mType;

    public StationInfoView(Context context) {
        super(context);
        this.mContext = context;
    }

    public StationInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public StationInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //去锯齿
        paint.setAntiAlias(true);
        switch (mType) {
            case 0:
                drawLight(canvas, paint);
                break;
            case 1:
            case 2:
                drawTempture(canvas, paint);
                break;
        }

    }

    protected void drawTempture(Canvas canvas, Paint paint) {
        if (mTempture > 0) {
            paint.setColor(Color.RED);

        } else {
            paint.setColor(Color.BLUE);
        }
        //绘制刻度
        float temp_x = getX() > CURRENT_X ?getX() - CURRENT_X : CURRENT_X - getX();
        float temp_y = getY() > CURRENT_Y ?getY() - CURRENT_Y : CURRENT_Y - getY();
        canvas.save();
        paint.setStyle(Paint.Style.FILL);

        int tep = (int) (getTempture() * 1.55);
        RectF oval3 = new RectF(getX() - 150, getY() + 30 - tep, getX() + 80, getY() + 100);// 设置个新的长方形
        canvas.drawRoundRect(oval3, 0, 0, paint);// 第二个参数是x半径，第三个参数是y半
        canvas.restore();

        //画背景
        Bitmap tmpBitmap = ((BitmapDrawable) mContext.getResources().getDrawable(getDrawableId())) != null ? ((BitmapDrawable) mContext.getResources().getDrawable(getDrawableId())).getBitmap() : null;

        if (tmpBitmap != null) {
            canvas.drawBitmap(tmpBitmap, 0, 0, paint);
        }
        //绘制文字
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);

        DecimalFormat df = new DecimalFormat("#.#");
        canvas.drawText(df.format(mTempture) + "℃", getX() - 10, getY() - 75, paint);
    }

    protected void drawLight(Canvas canvas, Paint paint) {
        paint.setColor(Color.RED);
        //绘制刻度
        canvas.save();
        paint.setStyle(Paint.Style.FILL);
        int light = (int) (getLightValue() / 100 * 90);
        RectF oval3 = new RectF(getX() - 80, getY() + 29 - light, getX() + 80, getY() + 100);// 设置个新的长方形
        canvas.drawRoundRect(oval3, 0, 0, paint);// 第二个参数是x半径，第三个参数是y半
        canvas.restore();
        //画背景
        Bitmap tmpBitmap = ((BitmapDrawable) mContext.getResources().getDrawable(getDrawableId())) != null ? ((BitmapDrawable) mContext.getResources().getDrawable(getDrawableId())).getBitmap() : null;

        if (tmpBitmap != null) {
            canvas.drawBitmap(tmpBitmap, 0, 0, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }


    public double getTempture() {
        return mTempture;
    }

    public void setTempture(double tempture) {
        this.mTempture = tempture;
    }

    public int getDrawableId() {
        return mDrawableId;
    }

    public void setDrawableId(int drawableId) {
        this.mDrawableId = drawableId;
    }

    public double getLightValue() {
        return mLightValue;
    }

    public void setLightValue(double lightValue) {
        this.mLightValue = lightValue;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public float getDpiWidth() {
        return mDpiWidth;
    }

    public void setDpiWidth(float dpiWidth) {
        this.mDpiWidth = dpiWidth;
    }

    public float getDpiHeight() {
        return mDpiHeight;
    }

    public void setDpiHeight(float dpiHeight) {
        this.mDpiHeight = dpiHeight;
    }
}
