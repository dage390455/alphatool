package com.sensoro.loratool.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by sensoro on 17/1/20.
 */

public class MainPager extends ViewPager {


    public MainPager(Context context) {
        super(context);
    }

    public MainPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }
}


