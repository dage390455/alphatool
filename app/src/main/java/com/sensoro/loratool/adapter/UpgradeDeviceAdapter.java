package com.sensoro.loratool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.sensoro.loratool.R;
import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.loratool.utils.RcItemTouchHelperCallback;
import com.sensoro.loratool.widget.AlphaToast;
import com.sensoro.loratool.widget.RecycleViewItemClickListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by fangping on 2016/7/7.
 */

public class UpgradeDeviceAdapter extends RecyclerView.Adapter<UpgradeDeviceAdapter.UpgradeInfoViewHolder>implements RcItemTouchHelperCallback.SwipedListener {

    private RecycleViewItemClickListener itemClickListener;
    private Context mContext;
    private ArrayList<SensoroDevice> mList = new ArrayList<>();

    public UpgradeDeviceAdapter(Context context, RecycleViewItemClickListener itemClickListener) {
        this.mContext = context;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public UpgradeInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_upgrade, null);
        return new UpgradeInfoViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(final UpgradeInfoViewHolder holder, int position) {
        if (mList == null) {
            return;
        }
        SensoroDevice device = mList.get(position);
        holder.item_more_name.setText(device.getSn());
        holder.item_more_value.setHint(R.string.dfu_waiting);
        if (device.getDfuProgress() <= 0||device.getDfuProgress()>100) {
            holder.item_more_value.setText(device.getDfuInfo());
        } else {
            holder.item_more_value.setText(device.getDfuProgress() + "%");

        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setData(ArrayList<SensoroDevice> list) {
      mList = list;
    }

    public ArrayList<SensoroDevice> getData() {
        return mList;
    }

    public SensoroDevice getData(int position) {
        return mList.get(position);
    }

    @Override
    public void onSwipedStart(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
        AlphaToast.INSTANCE.makeText(mContext,"移出升级设备列表",Toast.LENGTH_SHORT).show();
    }




    class UpgradeInfoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_upgrade_name)
        TextView item_more_name;
        @BindView(R.id.item_upgrade_value)
        TextView item_more_value;

        RecycleViewItemClickListener itemClickListener;

         UpgradeInfoViewHolder(View itemView, RecycleViewItemClickListener itemClickListener) {
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