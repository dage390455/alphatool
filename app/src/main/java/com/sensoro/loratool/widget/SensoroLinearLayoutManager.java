package com.sensoro.loratool.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by sensoro on 17/5/2.
 */

public class SensoroLinearLayoutManager extends LinearLayoutManager {


    public SensoroLinearLayoutManager(Context context) {
        super(context);
    }

    public SensoroLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public SensoroLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);

        int sumWidth = getWidth();

        int curLineWidth = 0, curLineTop = 0;
        int lastLineMaxHeight = 0;
        int rowIndex = 0;
        for (int i = 0; i < getItemCount(); i++) {
            View view = recycler.getViewForPosition(i);

            addView(view);
            measureChildWithMargins(view, 0, 0);
            int width = getDecoratedMeasuredWidth(view);
            int height = getDecoratedMeasuredHeight(view);

            curLineWidth += width;
            if (curLineWidth <= sumWidth) {//不需要换行
                if (rowIndex == 0 && i > 0) {
                    layoutDecorated(view, curLineWidth - width, curLineTop + 15, curLineWidth, curLineTop + height + 15);
                    //比较当前行多有item的最大高度
                    lastLineMaxHeight = Math.max(lastLineMaxHeight, height);
                } else {
                    layoutDecorated(view, curLineWidth - width, curLineTop, curLineWidth, curLineTop + height);
                    //比较当前行多有item的最大高度
                    lastLineMaxHeight = Math.max(lastLineMaxHeight, height);
                }


            } else {//换行
                rowIndex++;
                curLineWidth = width;
                if (lastLineMaxHeight == 0) {
                    lastLineMaxHeight = height;
                }
                //记录当前行top
                curLineTop += lastLineMaxHeight;

                layoutDecorated(view, 0, curLineTop, width, curLineTop + height);
                lastLineMaxHeight = height;
            }
        }

    }

}
