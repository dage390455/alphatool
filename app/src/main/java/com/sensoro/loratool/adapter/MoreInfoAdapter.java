package com.sensoro.loratool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sensoro.loratool.R;
import com.sensoro.loratool.widget.RecycleViewItemClickListener;
import com.sensoro.station.communication.bean.StationInfo;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by fangping on 2016/7/7.
 */

public class MoreInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private RecycleViewItemClickListener itemClickListener;
    private Context mContext;
    private String mMoreInfoArray[] = null;
    private StationInfo mStationInfo = null;

    public MoreInfoAdapter(Context context, StationInfo stationInfo, RecycleViewItemClickListener itemClickListener) {
        this.mContext = context;
        this.itemClickListener = itemClickListener;
        this.mStationInfo = stationInfo;
        mMoreInfoArray = context.getResources().getStringArray(R.array.station_moreinfo_array);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_station_more, null);
        return new MoreInfoViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (mMoreInfoArray == null) {
            return;
        }
        ((MoreInfoViewHolder) holder).item_more_name.setText(mMoreInfoArray[position]);
        parseStationInfo(((MoreInfoViewHolder) holder).item_more_value, position);
    }

    @Override
    public int getItemCount() {
        return mMoreInfoArray.length;
    }

    public void setData(StationInfo stationInfo) {
        this.mStationInfo = stationInfo;
    }

    class MoreInfoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_station_more_name)
        TextView item_more_name;
        @BindView(R.id.item_station_more_value)
        TextView item_more_value;

        RecycleViewItemClickListener itemClickListener;

        public MoreInfoViewHolder(View itemView, RecycleViewItemClickListener itemClickListener) {
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


    private void parseStationInfo(TextView textView, int position) {
        if (mStationInfo != null) {
            switch (position) {
                case 0:
                    textView.setText(mStationInfo.getStation_ssid());
                    break;
                case 1: {
                    String acm = mStationInfo.getNwk().getType();
                    if (acm.equals("ethernet")) {
                        String ip = mStationInfo.getNwk().getEth().getIp();
                        textView.setText(ip == null ? "" : ip);
                    } else if (acm.equals("3g")) {
                        String ip = mStationInfo.getNwk().get_3g().getIp();
                        textView.setText(ip == null ? "" : ip);
                    } else {
                        String ip = mStationInfo.getNwk().getWifi().getIp();
                        textView.setText(ip == null ? "" : ip);
                    }
                }
                break;
                case 2: {
                    String acm = mStationInfo.getNwk().getType();
                    if (acm.equals("ethernet")) {
                        String nmask = mStationInfo.getNwk().getEth().getNmask();
                        textView.setText(nmask == null ? "" : nmask);
                    } else if (acm.equals("3g")) {
                        String nmask = mStationInfo.getNwk().get_3g().getNmask();
                        textView.setText(nmask == null ? "" : nmask);
                    } else {
                        String nmask = mStationInfo.getNwk().getWifi().getNmask();
                        textView.setText(nmask == null ? "" : nmask);
                    }
                }
                break;
                case 3: {
                    try {
                        String startTime = mStationInfo.getSys().getStarttime();
                        if(!startTime.trim().equals("")) {
                            Long longtime = Long.parseLong(String.valueOf(startTime));
                            String datetime = DateFormat.format("yyyy:MM:dd kk:mm:ss", longtime).toString();
                            textView.setText(longtime == 0 ? "" : datetime);
                        } else {
                            textView.setText("");
                        }

                    }catch (Exception e) {

                    }
                }
                break;
                case 4:
                    String pm = mStationInfo.getPwr().getPm();
                    double pv = mStationInfo.getPwr().getPv();
                    String pwr = pm + " " + pv + "v";
                    textView.setText(pwr);
                    break;
                case 5: {
                    String acm = mStationInfo.getNwk().getType();
                    if (acm.equals("3g")) {
                        String sim = mStationInfo.getNwk().get_3g().getSim();
                        if (sim != null) {
                            textView.setText(mStationInfo.getNwk().get_3g().getSim());
                        }
                    }
                }
                break;
                case 6: {
                    String acm = mStationInfo.getNwk().getType();
                    if (acm.equals("ethernet")) {
                        String gw = mStationInfo.getNwk().getEth().getGw();
                        textView.setText(gw == null ? "" : gw);
                    } else if (acm.equals("3g")) {
                        String gw = mStationInfo.getNwk().get_3g().getGw();
                        textView.setText(gw == null ? "" : gw);
                    } else {
                        String gw = mStationInfo.getNwk().getWifi().getGw();
                        textView.setText(gw == null ? "" : gw);
                    }
                }

                break;
                case 7: {
                    String acm = mStationInfo.getNwk().getType();
                    if (acm.equals("ethernet")) {
                        String pdns = mStationInfo.getNwk().getEth().getPdns();
                        textView.setText(pdns == null ? "" : pdns);
                    } else if (acm.equals("3g")) {
                        String pdns = mStationInfo.getNwk().get_3g().getPdns();
                        textView.setText(pdns == null ? "" : pdns);
                    } else {
                        String pdns = mStationInfo.getNwk().getWifi().getPdns();
                        textView.setText(pdns == null ? "" : pdns);
                    }
                }
                break;
                case 8:
                    textView.setText(mStationInfo.getSys().getSw_ver());
                    break;
                case 9:
                    textView.setText(mStationInfo.getSys().getHw_ver());
                    break;
            }
        }
    }
}