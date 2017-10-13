package com.sensoro.loratool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sensoro.loratool.R;
import com.sensoro.loratool.model.SignalData;
import com.sensoro.loratool.widget.RecycleViewItemClickListener;
import com.sensoro.station.communication.bean.StationInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by fangping on 2016/7/7.
 */

public class SignalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private RecycleViewItemClickListener itemClickListener;
    private Context mContext;
    private ArrayList<SignalData> mSignalData = new ArrayList<SignalData>();

    public SignalAdapter(Context context, RecycleViewItemClickListener itemClickListener) {
        this.mContext = context;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_signal, null);
        return new SignalInfoAdapter(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        SignalInfoAdapter signalInfoAdapter = ((SignalInfoAdapter) holder);
        SignalData signalData = mSignalData.get(position);
        signalInfoAdapter.item_signal_time.setText(signalData.getDate());
        int down_sf = 12 - signalData.getDownlinkDR();
        int up_sf = 12 - signalData.getUplinkDR();
        float up_freq = (float)signalData.getUplinkFreq() / 1000000;
        signalInfoAdapter.item_up_rssi.setText("RSSI:" + (signalData.getUplinkRSSI() == 0 ? "-" : signalData.getUplinkRSSI()));
        signalInfoAdapter.item_up_snr.setText("SNR:" + (signalData.getUplinkSNR() == 0 ? "-" : signalData.getUplinkSNR()));
        signalInfoAdapter.item_up_txp.setText((signalData.getUplinkTxPower() == 0 ? "-" : signalData.getUplinkTxPower()) + "dBm SF" + up_sf);
        signalInfoAdapter.item_up_interval.setText("@" + (up_freq == 0 ? "-" : up_freq) + "MHz");

        signalInfoAdapter.item_down_rssi.setText("RSSI:" + (signalData.getDownlinkRSSI() == 0 ? "-" : signalData.getDownlinkRSSI()));
        signalInfoAdapter.item_down_snr.setText("SNR:" + (signalData.getDownlinkSNR() == 0 ? "-" : signalData.getDownlinkSNR()));
        signalInfoAdapter.item_down_txp.setText((signalData.getDownlinkTxPower() == 0 ? "-" : signalData.getDownlinkTxPower()) + "dBm SF" + down_sf);
        float down_freq = (float)signalData.getDownlinkFreq() / 1000000;
        signalInfoAdapter.item_down_interval.setText("@" + (down_freq == 0 ? "-" : down_freq) + "MHz");
    }

    @Override
    public int getItemCount() {
        return mSignalData.size();
    }

    public void setData(ArrayList<SignalData> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        mSignalData.clear();
        for (int i = 0; i < list.size(); i++) {
            mSignalData.add(list.get(i));
        }
    }

    public void appendData(SignalData signalData) {
        mSignalData.add(signalData);
        if (mSignalData.size() > 3) {
            mSignalData.remove(0);
        }
    }

    public void appendData(ArrayList<SignalData> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        if (mSignalData == null || mSignalData.size() == 0) {
            setData(list);
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            mSignalData.add(list.get(i));
        }
        if (mSignalData.size() > 3) {
            mSignalData.remove(list.size());
        }
    }


    class SignalInfoAdapter extends RecyclerView.ViewHolder {

        @BindView(R.id.signal_time)
        TextView item_signal_time;
        @BindView(R.id.signal_up_rssi)
        TextView item_up_rssi;
        @BindView(R.id.signal_up_snr)
        TextView item_up_snr;
        @BindView(R.id.signal_down_rssi)
        TextView item_down_rssi;
        @BindView(R.id.signal_down_snr)
        TextView item_down_snr;

        @BindView(R.id.signal_up_txp)
        TextView item_up_txp;
        @BindView(R.id.signal_up_interval)
        TextView item_up_interval;
        @BindView(R.id.signal_down_txp)
        TextView item_down_txp;
        @BindView(R.id.signal_down_interval)
        TextView item_down_interval;
        RecycleViewItemClickListener itemClickListener;

        public SignalInfoAdapter(View itemView, RecycleViewItemClickListener itemClickListener) {
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