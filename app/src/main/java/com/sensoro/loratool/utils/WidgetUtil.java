package com.sensoro.loratool.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by sensoro on 17/7/12.
 */

public class WidgetUtil {

    public static void setSearchViewOnClickListener(View v, View.OnClickListener listener) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)v;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = group.getChildAt(i);
                if (child instanceof LinearLayout || child instanceof RelativeLayout) {
                    setSearchViewOnClickListener(child, listener);
                }

                if (child instanceof TextView) {
                    TextView text = (TextView)child;
                    text.setFocusable(false);
                }
                child.setOnClickListener(listener);
            }
        }
    }
}
