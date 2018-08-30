package com.sensoro.loratool.utils;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class RcItemTouchHelperCallback extends ItemTouchHelper.Callback {


    private final SwipedListener mAdapter;

    public RcItemTouchHelperCallback(SwipedListener adapter) {
        mAdapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START;
        return makeMovementFlags(dragFlags,swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if(direction == ItemTouchHelper.START){
            mAdapter.onSwipedStart(viewHolder.getAdapterPosition());
        }
    }

    public interface SwipedListener{
        void onSwipedStart(int position);
    }
}
