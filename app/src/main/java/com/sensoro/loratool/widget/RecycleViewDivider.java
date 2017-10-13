package com.sensoro.loratool.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by tangrisheng on 2016/5/11.
 * RecycleView Divider
 */
public class RecycleViewDivider extends RecyclerView.ItemDecoration {

    private Paint mPaint;
    private Drawable mDivider;
    private int mDividerHeight = 2;//divider height，default 2px
    private int mOrientation;//orientation：LinearLayoutManager.VERTICAL or LinearLayoutManager.HORIZONTAL
    private boolean isDrawEnd;
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    /**
     * default divider：height 2px，color gray
     *
     * @param context     context
     * @param orientation orientation
     */
    public RecycleViewDivider(Context context, int orientation, boolean isDrawEnd) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("arguments error");
        }
        mOrientation = orientation;
        this.isDrawEnd = isDrawEnd;
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    /**
     * define divider by self
     *
     * @param context     context
     * @param orientation orientation
     * @param drawableId  divider drawableId
     */
    public RecycleViewDivider(Context context, int orientation, int drawableId, boolean isDrawEnd) {
        this(context, orientation, isDrawEnd);
        mDivider = ContextCompat.getDrawable(context, drawableId);
        mDividerHeight = mDivider.getIntrinsicHeight();
    }

    /**
     * define divider by self
     *
     * @param context       context
     * @param orientation   orientation
     * @param dividerHeight divider height
     * @param dividerColor  divider color
     */
    public RecycleViewDivider(Context context, int orientation, int dividerHeight, int dividerColor, boolean isDrawEnd) {
        this(context, orientation, isDrawEnd);
        mDividerHeight = dividerHeight;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(dividerColor);
        mPaint.setStyle(Paint.Style.FILL);
    }


    //get size
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0, 0, 0, mDividerHeight);
    }

    //draw divider
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    //draw divider horizontally
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mDividerHeight;

            if (isDrawEnd){
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            } else {
                if (i != childSize -1) {
                    if (mDivider != null) {
                        mDivider.setBounds(left, top, right, bottom);
                        mDivider.draw(canvas);
                    }
                    if (mPaint != null) {
                        canvas.drawRect(left, top, right, bottom, mPaint);
                    }
                }
            }
        }
    }

    //draw divider vertically
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mDividerHeight;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }
}
