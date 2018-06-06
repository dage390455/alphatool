package com.sensoro.loratool.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sensoro.loratool.R;
import com.sensoro.loratool.ble.SensoroStation;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.model.StationInfoComparator;
import com.sensoro.loratool.utils.DateUtil;
import com.sensoro.station.communication.bean.StationInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sensoro on 16/8/17.
 */

public class StationInfoAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<StationInfo> mStationInfoList = Collections.synchronizedList(new ArrayList<StationInfo>());
    private ConcurrentHashMap<String, SensoroStation> mNearByStationMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, StationInfo> mCacheStationInfoMap = new ConcurrentHashMap<>();

    public StationInfoAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public List<StationInfo> getShowData() {
        return mStationInfoList;
    }

    public ConcurrentHashMap<String, SensoroStation> getNearByStationMap() {
        return mNearByStationMap;
    }

    public ConcurrentHashMap<String, StationInfo> getCacheData() {
        return mCacheStationInfoMap;
    }

    public List<StationInfo> getFilterData() {
        filter();
        return mStationInfoList;
    }

    public void appendSearchData(List<StationInfo> list) {

        HashMap<String, String> tempMap = new HashMap<>();
        for (int i = 0; i < mStationInfoList.size(); i++) {
            tempMap.put(mStationInfoList.get(i).getSys().getSn(), mStationInfoList.get(i).getSys().getSn());
        }
        for (int j = 0; j < list.size(); j++) {
            StationInfo stationInfo = list.get(j);
            if (mNearByStationMap.containsKey(stationInfo.getSys().getSn())) {
                stationInfo.setSort(2);
            } else {
                stationInfo.setSort(0);
            }
            if (!tempMap.containsKey(stationInfo.getSys().getSn())) {
                mStationInfoList.add(stationInfo);
            }

        }
        StationInfoComparator comparator = new StationInfoComparator();
        Collections.sort(mStationInfoList, comparator);
        notifyDataSetChanged();
    }

    public void appendData(List<StationInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            StationInfo stationInfo = list.get(i);
            if (!mCacheStationInfoMap.containsKey(stationInfo.getSys().getSn())) {
                mCacheStationInfoMap.put(stationInfo.getSys().getSn(), list.get(i));
            }
        }
        filter();
    }

    public void filter() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFERENCE_FILTER, Context
                .MODE_PRIVATE);
        HashSet<String> firmwareSet = (HashSet) sharedPreferences.getStringSet("station_firmware", null);
        HashSet<String> hardwareSet = (HashSet) sharedPreferences.getStringSet("station_hardware", null);
        HashSet<String> signalSet = (HashSet) sharedPreferences.getStringSet("station_signal", null);
        HashSet<String> nearSet = (HashSet) sharedPreferences.getStringSet("station_near", null);
        HashSet<String> enableFilterSet = (HashSet) sharedPreferences.getStringSet("station_enable_filter", null);
        boolean isClose = false;
        if (enableFilterSet != null) {
            for (String switchString : enableFilterSet) {
                if (switchString.equals("1")) {//close
                    isClose = true;
                    break;
                }
            }
        }
        if (firmwareSet != null && hardwareSet != null && signalSet != null && nearSet != null && !isClose) {
            List<StationInfo> tempFirmwareList = new ArrayList<>();
            for (String firmWare : firmwareSet) {
                for (String key : mCacheStationInfoMap.keySet()) {
                    StationInfo stationInfo = mCacheStationInfoMap.get(key);
                    if (stationInfo != null) {
                        String firmwareVersion = stationInfo.getSys().getSw_ver();
                        boolean isOther = false;
                        if (firmwareVersion.contains("0.")) {
                            isOther = true;
                        }
                        if ((firmWare.equals(firmwareVersion) || firmwareVersion.contains(firmWare) || isOther) &&
                                !tempFirmwareList.contains(stationInfo)) {

                            tempFirmwareList.add(stationInfo);
                        }
                    }
                }
            }

            List<StationInfo> tempHardwareList = new ArrayList<>();
            for (String hardware : hardwareSet) {
                for (int i = 0; i < tempFirmwareList.size(); i++) {
                    String hardwareVersion = tempFirmwareList.get(i).getDeviceType();
                    if (hardware.equals(hardwareVersion)) {
                        tempHardwareList.add(tempFirmwareList.get(i));
                    }
                }
            }
            tempFirmwareList.clear();
            List<StationInfo> tempSignalList = new ArrayList<>();
            for (String signal : signalSet) {
                for (int i = 0; i < tempHardwareList.size(); i++) {
                    if (tempHardwareList.get(i).getRssi() >= Integer.parseInt(signal)) {
                        tempSignalList.add(tempHardwareList.get(i));
                    }
                }
            }
            tempHardwareList.clear();
            List<StationInfo> stationInfoList = new ArrayList<>();
            for (String near : nearSet) {
                if (near.equals("1")) {//near
                    for (int i = 0; i < tempSignalList.size(); i++) {
                        if (mNearByStationMap.containsKey(tempSignalList.get(i).getSys().getSn())) {
                            tempSignalList.get(i).setSort(2);
                            stationInfoList.add(tempSignalList.get(i));
                        } else {
                            tempSignalList.get(i).setSort(0);
                        }
                    }
                } else {//all
                    for (int i = 0; i < tempSignalList.size(); i++) {
                        if (mNearByStationMap.containsKey(tempSignalList.get(i).getSys().getSn())) {
                            tempSignalList.get(i).setSort(2);
                        } else {
                            tempSignalList.get(i).setSort(0);
                        }
                        stationInfoList.add(tempSignalList.get(i));
                    }
                }
            }
            tempSignalList.clear();
            mStationInfoList.clear();
            mStationInfoList.addAll(stationInfoList);
            stationInfoList.clear();
            StationInfoComparator comparator = new StationInfoComparator();
            Collections.sort(mStationInfoList, comparator);
//            notifyDataSetChanged();
        } else {
            mStationInfoList.clear();
            mStationInfoList.addAll(mCacheStationInfoMap.values());
            StationInfoComparator comparator = new StationInfoComparator();
            Collections.sort(mStationInfoList, comparator);
//            notifyDataSetChanged();
        }
        notifyDataSetChanged();
    }

    public boolean isFilterNearby() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFERENCE_FILTER, Context
                .MODE_PRIVATE);
        HashSet<String> nearSet = (HashSet) sharedPreferences.getStringSet("station_near", null);
        HashSet<String> enableFilterSet = (HashSet) sharedPreferences.getStringSet("station_enable_filter", null);
        if (enableFilterSet != null) {
            for (String switchString : enableFilterSet) {
                if (switchString.equals("1")) {//close
                    return true;
                }
            }
        }

        boolean isFilterNearby = false;
        if (nearSet != null) {
            for (String nearKey : nearSet) {
                if (nearKey.equals("1")) {// nearby
                    isFilterNearby = true;
                }
            }
        }
        return isFilterNearby;
    }

    public boolean isFitable(SensoroStation sensoroStation) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFERENCE_FILTER, Context
                .MODE_PRIVATE);
        HashSet<String> firmwareSet = (HashSet) sharedPreferences.getStringSet("station_firmware", null);
        HashSet<String> hardwareSet = (HashSet) sharedPreferences.getStringSet("station_hardware", null);
        HashSet<String> signalSet = (HashSet) sharedPreferences.getStringSet("station_signal", null);
        HashSet<String> nearSet = (HashSet) sharedPreferences.getStringSet("station_near", null);
        HashSet<String> enableFilterSet = (HashSet) sharedPreferences.getStringSet("station_enable_filter", null);
        if (enableFilterSet != null) {
            for (String switchString : enableFilterSet) {
                if (switchString.equals("1")) {//close
                    return true;
                }
            }
        }

        boolean isFitFirmwareable = false;
        if (firmwareSet != null) {
            for (String key : firmwareSet) {
                if (sensoroStation.getFirmwareVersion().equals(key)) {
                    isFitFirmwareable = true;
                }
            }
        } else {
            isFitFirmwareable = true;
        }

        boolean isFitHardwareable = false;
        if (hardwareSet != null) {
            for (String key : hardwareSet) {
                StationInfo stationInfo = mCacheStationInfoMap.get(sensoroStation.getSn());
                if (stationInfo != null) {
                    if (stationInfo.getDeviceType().equals(key)) {
                        isFitHardwareable = true;
                    }
                }

            }
        } else {
            isFitHardwareable = true;
        }

        boolean isFitSignalable = false;
        if (nearSet != null) {
            for (String nearKey : nearSet) {
                if (nearKey.equals("1")) {
                    for (String key : signalSet) {
                        if ((sensoroStation.getRssi() >= Integer.parseInt(key))) {
                            isFitSignalable = true;
                        }
                    }
                } else {
                    isFitSignalable = true;
                }
            }
        } else {
            isFitSignalable = true;
        }

        return isFitSignalable && isFitFirmwareable && isFitHardwareable;
    }


    @Override
    public int getCount() {
        return mStationInfoList.size();
    }

    @Override
    public StationInfo getItem(int i) {
        return mStationInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        StationItemViewHolder itemViewHolder = null;
        if (view == null) {
            view = mInflater.inflate(R.layout.item_station, null);
            itemViewHolder = new StationItemViewHolder(view);
            view.setTag(itemViewHolder);
        } else {
            itemViewHolder = (StationItemViewHolder) view.getTag();
        }
        if (mStationInfoList.size() > 0) {
            StationInfo stationInfo = mStationInfoList.get(position);
            String sn = stationInfo.getSys().getSn();
            itemViewHolder.snTextView.setText(sn);
            itemViewHolder.nameTextView.setText(stationInfo.getName());
            String status_str = mContext.getString(R.string.unknow);

            switch (stationInfo.getSys().getNormalStatus()) {
                case 0:
                    Drawable drawable0 = mContext.getResources().getDrawable(R.drawable.shape_oval);
                    drawable0.setBounds(0, 0, drawable0 != null ? drawable0.getMinimumWidth() : 0,
                            drawable0.getMinimumHeight());
                    drawable0.setColorFilter(mContext.getResources().getColor(R.color.status_normal), PorterDuff.Mode
                            .MULTIPLY);
                    itemViewHolder.statusTextView.setCompoundDrawables(drawable0, null, null, null);
                    status_str = mContext.getString(R.string.status_normal);
                    break;
                case 1:
                    Drawable drawable1 = mContext.getResources().getDrawable(R.drawable.shape_status_fault);
                    drawable1.setBounds(0, 0, drawable1 != null ? drawable1.getMinimumWidth() : 0,
                            drawable1.getMinimumHeight());
                    drawable1.setColorFilter(mContext.getResources().getColor(R.color.status_fault), PorterDuff.Mode
                            .MULTIPLY);
                    itemViewHolder.statusTextView.setCompoundDrawables(drawable1, null, null, null);
                    status_str = mContext.getString(R.string.status_fault);
                    break;
                case 2:
                    Drawable drawable2 = mContext.getResources().getDrawable(R.drawable.shape_status_serious);
                    drawable2.setBounds(0, 0, drawable2 != null ? drawable2.getMinimumWidth() : 0,
                            drawable2.getMinimumHeight());
                    drawable2.setColorFilter(mContext.getResources().getColor(R.color.status_serious), PorterDuff
                            .Mode.MULTIPLY);
                    itemViewHolder.statusTextView.setCompoundDrawables(drawable2, null, null, null);
                    status_str = mContext.getString(R.string.status_serious);
                    break;
                case 3:
                    Drawable drawable3 = mContext.getResources().getDrawable(R.drawable.shape_status_timeout);
                    drawable3.setBounds(0, 0, drawable3 != null ? drawable3.getMinimumWidth() : 0,
                            drawable3.getMinimumHeight());
                    drawable3.setColorFilter(mContext.getResources().getColor(R.color.status_timeout), PorterDuff
                            .Mode.MULTIPLY);
                    itemViewHolder.statusTextView.setCompoundDrawables(drawable3, null, null, null);
                    status_str = mContext.getString(R.string.status_timeout);
                    break;
                case -1:
                    Drawable drawable4 = mContext.getResources().getDrawable(R.drawable.shape_status_inactive);
                    drawable4.setBounds(0, 0, drawable4 != null ? drawable4.getMinimumWidth() : 0,
                            drawable4.getMinimumHeight());
                    drawable4.setColorFilter(mContext.getResources().getColor(R.color.status_inactive), PorterDuff
                            .Mode.MULTIPLY);
                    itemViewHolder.statusTextView.setCompoundDrawables(drawable4, null, null, null);
                    status_str = mContext.getString(R.string.status_inactive);
                    break;
                case 4:
                    Drawable drawable6 = mContext.getResources().getDrawable(R.drawable.shape_status_offline);
                    drawable6.setBounds(0, 0, drawable6 != null ? drawable6.getMinimumWidth() : 0,
                            drawable6.getMinimumHeight());
                    drawable6.setColorFilter(mContext.getResources().getColor(R.color.status_offline), PorterDuff
                            .Mode.MULTIPLY);
                    itemViewHolder.statusTextView.setCompoundDrawables(drawable6, null, null, null);
                    status_str = mContext.getString(R.string.status_offline);
                default:
                    Drawable drawable5 = mContext.getResources().getDrawable(R.drawable.shape_status_inactive);
                    drawable5.setBounds(0, 0, drawable5 != null ? drawable5.getMinimumWidth() : 0,
                            drawable5.getMinimumHeight());
                    drawable5.setColorFilter(mContext.getResources().getColor(R.color.status_inactive), PorterDuff
                            .Mode.MULTIPLY);
                    itemViewHolder.statusTextView.setCompoundDrawables(drawable5, null, null, null);
                    break;
            }
            itemViewHolder.statusTextView.setText(status_str);
            itemViewHolder.statusTimeTextView.setText(DateUtil.getDateDiffWithFormat(mContext, Long.parseLong
                    (stationInfo.getSys().getPrevUpTime()), "MM-dd"));

            Drawable okDrawable = mContext.getResources().getDrawable(R.mipmap.ic_ok);
            okDrawable.setBounds(0, 0, okDrawable != null ? okDrawable.getMinimumWidth() : 0, okDrawable
                    .getMinimumHeight());

            Drawable notDrawable = mContext.getResources().getDrawable(R.mipmap.ic_not);
            notDrawable.setBounds(0, 0, notDrawable != null ? notDrawable.getMinimumWidth() : 0, notDrawable
                    .getMinimumHeight());

            Drawable usingDrawable = mContext.getResources().getDrawable(R.mipmap.ic_using);
            usingDrawable.setBounds(0, 0, usingDrawable != null ? usingDrawable.getMinimumWidth() : 0, usingDrawable
                    .getMinimumHeight());
            boolean isNearBy = mNearByStationMap.containsKey(sn);
            if (isNearBy) {
                SensoroStation sensoroStation = mNearByStationMap.get(sn);

                stationInfo.setRssi(mNearByStationMap.get(sn).getRssi());
                itemViewHolder.nearbyTextView.setText(mContext.getString(R.string.nearby));
                itemViewHolder.nearbyTextView.setVisibility(View.VISIBLE);
                if (sensoroStation.getWifiStatus() == 0) {
                    itemViewHolder.wifiTextView.setCompoundDrawables(okDrawable, null, null, null);
                } else {
                    itemViewHolder.wifiTextView.setCompoundDrawables(notDrawable, null, null, null);
                }
                if (sensoroStation.getEthStatus() == 0) {
                    itemViewHolder.ethTextView.setCompoundDrawables(okDrawable, null, null, null);
                } else {
                    itemViewHolder.ethTextView.setCompoundDrawables(notDrawable, null, null, null);
                }
                if (sensoroStation.getCellularStatus() == 0) {
                    itemViewHolder.cellularTextView.setCompoundDrawables(okDrawable, null, null, null);
                } else {
                    itemViewHolder.cellularTextView.setCompoundDrawables(notDrawable, null, null, null);
                }

            } else {
                String type = stationInfo.getNwk().getType();
                if (type.equals("wifi")) {
                    itemViewHolder.wifiTextView.setCompoundDrawables(okDrawable, null, null, null);
                    itemViewHolder.ethTextView.setCompoundDrawables(notDrawable, null, null, null);
                    itemViewHolder.cellularTextView.setCompoundDrawables(notDrawable, null, null, null);
                } else if (type.equals("ethernet")) {
                    itemViewHolder.wifiTextView.setCompoundDrawables(notDrawable, null, null, null);
                    itemViewHolder.ethTextView.setCompoundDrawables(okDrawable, null, null, null);
                    itemViewHolder.cellularTextView.setCompoundDrawables(notDrawable, null, null, null);
                } else if (type.equals("cellular")) {
                    itemViewHolder.wifiTextView.setCompoundDrawables(notDrawable, null, null, null);
                    itemViewHolder.ethTextView.setCompoundDrawables(notDrawable, null, null, null);
                    itemViewHolder.cellularTextView.setCompoundDrawables(okDrawable, null, null, null);
                } else {
                    itemViewHolder.wifiTextView.setCompoundDrawables(notDrawable, null, null, null);
                    itemViewHolder.ethTextView.setCompoundDrawables(notDrawable, null, null, null);
                    itemViewHolder.cellularTextView.setCompoundDrawables(notDrawable, null, null, null);
                }
                itemViewHolder.nearbyTextView.setVisibility(View.GONE);
            }
            if (stationInfo.getNwk().getType().equalsIgnoreCase("ethernet")) {
                itemViewHolder.ethTextView.setCompoundDrawables(usingDrawable, null, null, null);
            } else if (stationInfo.getNwk().getType().equalsIgnoreCase("cellular")) {
                itemViewHolder.cellularTextView.setCompoundDrawables(usingDrawable, null, null, null);
            } else {
                itemViewHolder.wifiTextView.setCompoundDrawables(usingDrawable, null, null, null);
            }
            int y50 = mContext.getResources().getDimensionPixelSize(R.dimen.y50);
            if (stationInfo.getDeviceType().equals(StationInfo.TYPE_GATEWAY)) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                        .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(350, 0, 0, 0);
                itemViewHolder.ethTextView.setLayoutParams(layoutParams);
                itemViewHolder.wifiTextView.setVisibility(View.VISIBLE);
            } else {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                        .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(50, 0, 0, 0);
                itemViewHolder.ethTextView.setLayoutParams(layoutParams);
                itemViewHolder.wifiTextView.setVisibility(View.GONE);
            }

            List<String> list = stationInfo.getTags();
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            itemViewHolder.tagRecyclerView.setLayoutManager(layoutManager);
            itemViewHolder.tagRecyclerView.setAdapter(new TagAdapter(mContext, list));

        }
        return view;
    }

    public void refreshNew(SensoroStation sensoroStation, boolean isSearchStatus) {
        String sn = sensoroStation.getSn();
        if (sn.endsWith("DBC9")) {
            Log.e("", "refreshNew: ------");
        }
        if (!mNearByStationMap.containsKey(sn)) {
            mNearByStationMap.put(sn, sensoroStation);
            notifyDataSetChanged();
        }
        if (isSearchStatus) {
            return;
        }
        boolean isContains = false;
        for (int j = 0; j < mStationInfoList.size(); j++) {
            StationInfo stationInfo = mStationInfoList.get(j);
            if (sn.equalsIgnoreCase(stationInfo.getSys().getSn())) {
                stationInfo.setSort(2);
                isContains = true;
                break;
            }
        }

        if (!isContains) {
            if (isFitable(sensoroStation) && mCacheStationInfoMap.containsKey(sn)) {
                StationInfo stationInfo = mCacheStationInfoMap.get(sn);
                stationInfo.setSort(2);
                mStationInfoList.add(stationInfo);

            }
        }
        StationInfoComparator comparator = new StationInfoComparator();
        Collections.sort(mStationInfoList, comparator);
        notifyDataSetChanged();

    }

    public void refreshGone(SensoroStation sensoroStation, boolean isSearchStatus) {
        //TODO 修改删除方式
        String sn = sensoroStation.getSn();
        if (mNearByStationMap.containsKey(sn)) {
            mNearByStationMap.remove(sn);
            for (int j = 0; j < mStationInfoList.size(); j++) {
                StationInfo stationInfo = mStationInfoList.get(j);
                if (sn.equalsIgnoreCase(stationInfo.getSys().getSn()) && isFilterNearby() &&
                        !isSearchStatus) {
                    mStationInfoList.remove(j);
//                    notifyDataSetChanged();
                    break;
                }
            }
            notifyDataSetChanged();
        }

    }


    public void clear() {
        mStationInfoList.clear();
    }

    public void clearCache() {
        mCacheStationInfoMap.clear();
    }

    class StationItemViewHolder {

        @BindView(R.id.station_item_sn)
        TextView snTextView;
        @BindView(R.id.station_item_name)
        TextView nameTextView;
        @BindView(R.id.station_item_status)
        TextView statusTextView;
        @BindView(R.id.station_item_status_time)
        TextView statusTimeTextView;
        @BindView(R.id.station_item_nearby)
        TextView nearbyTextView;
        @BindView(R.id.station_item_enable_3g)
        TextView cellularTextView;
        @BindView(R.id.station_item_enable_eth)
        TextView ethTextView;
        @BindView(R.id.station_item_enable_wifi)
        TextView wifiTextView;
        @BindView(R.id.station_item_tag_rv)
        RecyclerView tagRecyclerView;

        public StationItemViewHolder(final View itemView) {
            ButterKnife.bind(this, itemView);

        }
    }

    @Override
    public void notifyDataSetChanged() {
        final HashSet<StationInfo> stationInfoList = new HashSet<>(mStationInfoList);
        mStationInfoList.clear();
        mStationInfoList.addAll(stationInfoList);
//        Collections.sort(mStationInfoList);
        stationInfoList.clear();
        super.notifyDataSetChanged();
    }
}
