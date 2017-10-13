package com.sensoro.loratool.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by sensoro on 17/7/11.
 */

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public SpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;

        if (position == 0) {
            outRect.top = space;
        }

    }
}
