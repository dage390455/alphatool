package com.sensoro.loratool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sensoro.loratool.R;

import java.util.List;


/**
 * Created by fangping on 2016/7/7.
 */

public class TagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<String> mList;

    public TagAdapter(Context context, List<String> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_tag, null);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (mList == null) {
            return;
        }
        ((TagViewHolder) holder).tagTextView.setText( mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tagTextView;

        public TagViewHolder(View itemView) {
            super(itemView);
            tagTextView = (TextView)itemView.findViewById(R.id.item_tag_name);
        }
    }

}
