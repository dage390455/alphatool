package com.sensoro.loratool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sensoro.loratool.R;

import java.util.ArrayList;

public class DeviceDetailACRecylerAdaper extends RecyclerView.Adapter<DeviceDetailACRecylerAdaper.MyViewHolder>{
    private final Context mContext;
    private ArrayList<String> keyList;
    private ArrayList<String> valueList;

    public DeviceDetailACRecylerAdaper(Context context) {
        mContext = context;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_rc_device_detail, null);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mTvKey.setText(keyList.get(position));
        holder.mTvValue.setText(valueList.get(position));
    }

    public void setKeyList(ArrayList<String> keyList){
        this.keyList = keyList;
    }

    public void setValueList (ArrayList<String> valueList){
        this.valueList = valueList;
    }

    @Override
    public int getItemCount() {
        return keyList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        private final TextView mTvKey;
        private final TextView mTvValue;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTvKey = itemView.findViewById(R.id.item_device_detail_key);
            mTvValue = itemView.findViewById(R.id.item_device_detail_value);
        }
    }

}
