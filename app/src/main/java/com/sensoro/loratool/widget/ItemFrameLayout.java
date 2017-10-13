package com.sensoro.loratool.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;


public class ItemFrameLayout extends android.widget.LinearLayout {

    public ItemFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ItemFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemFrameLayout(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //核心就是下面这块代码块啦
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = width;
        setLayoutParams(lp);
    }
}