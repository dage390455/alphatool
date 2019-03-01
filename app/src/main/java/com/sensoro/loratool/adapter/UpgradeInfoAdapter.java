package com.sensoro.loratool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sensoro.libbleserver.ble.entity.SensoroDevice;
import com.sensoro.lora.setting.server.bean.UpgradeInfo;
import com.sensoro.loratool.R;
import com.sensoro.loratool.utils.DateUtil;
import com.sensoro.loratool.widget.RecycleViewItemClickListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by fangping on 2016/7/7.
 */

public class UpgradeInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private RecycleViewItemClickListener itemClickListener;
    private Context mContext;
    private SensoroDevice mSensoroDevice;
    private ArrayList<UpgradeInfo> mList = new ArrayList<>();

    public UpgradeInfoAdapter(Context context, SensoroDevice sensoroDevice, RecycleViewItemClickListener itemClickListener) {
        this.mContext = context;
        this.itemClickListener = itemClickListener;
        this.mSensoroDevice = sensoroDevice;
    }

//    public UpgradeInfoAdapter(Context context, RecycleViewItemClickListener itemClickListener) {
//        this.mContext = context;
//        this.itemClickListener = itemClickListener;
//
//    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_upgrade, null);
        return new UpgradeInfoViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (mList == null) {
            return;
        }
        UpgradeInfo upgradeInfo = mList.get(position);
        ((UpgradeInfoViewHolder) holder).item_more_name.setText(upgradeInfo.getVersion());
        ((UpgradeInfoViewHolder) holder).item_more_value.setText(DateUtil.getDate(upgradeInfo.getUpdatedTime()));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setData(ArrayList<UpgradeInfo> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        mList.clear();
        mList.addAll(list);

    }

    public UpgradeInfo getData(int position) {
        return mList.get(position);
    }


    class UpgradeInfoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_upgrade_name)
        TextView item_more_name;
        @BindView(R.id.item_upgrade_value)
        TextView item_more_value;

        RecycleViewItemClickListener itemClickListener;

        public UpgradeInfoViewHolder(View itemView, RecycleViewItemClickListener itemClickListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemClickListener = itemClickListener;
            itemView.setOnClickListener(onItemClickListener);
        }

        View.OnClickListener onItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, getAdapterPosition());
                }
            }
        };
    }
}