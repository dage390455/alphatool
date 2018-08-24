package com.sensoro.loratool.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.libbleserver.ble.SensoroSensorTest;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.utils.DateUtil;
import com.sensoro.loratool.widget.BatteryView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by sensoro on 16/8/17.
 */

public class DeviceInfoAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private final List<DeviceInfo> mDeviceInfoList = Collections.synchronizedList(new ArrayList<DeviceInfo>());
    private final ConcurrentHashMap<String, DeviceInfo> mCacheDeviceInfoMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SensoroDevice> mNearByDeviceMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SensoroSensorTest> mSensorMap = new ConcurrentHashMap<>();

    public DeviceInfoAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        ((LoRaSettingApplication) context.getApplicationContext()).setmNearDeviceMap(mNearByDeviceMap);
    }

    public ConcurrentHashMap<String, SensoroDevice> getNearByDeviceMap() {
        return mNearByDeviceMap;
    }

    public boolean isNearBy(String sn) {
        return mNearByDeviceMap.containsKey(sn);
    }

    public ConcurrentHashMap<String, DeviceInfo> getCacheData() {
        return mCacheDeviceInfoMap;
    }

    public List<DeviceInfo> getFilterData() {
        filter();
        return mDeviceInfoList;
    }

    public void appendData(List<DeviceInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            DeviceInfo deviceInfo = list.get(i);
            if (!mCacheDeviceInfoMap.containsKey(deviceInfo.getSn())) {
                mCacheDeviceInfoMap.put(deviceInfo.getSn(), deviceInfo);
            }
        }
        filter();
    }


    public void appendDataWithPosition(ArrayList<DeviceInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            DeviceInfo deviceInfo = list.get(i);
            if (!mCacheDeviceInfoMap.containsKey(deviceInfo.getSn())) {
                mCacheDeviceInfoMap.put(deviceInfo.getSn(), deviceInfo);
            }
        }
        filter();
    }

    public void appendSearchData(List<DeviceInfo> list) {
        final HashSet<String> hashSet = new HashSet<>();
//        HashMap<String, String> tempMap = new HashMap<>();
        for (int i = 0; i < mDeviceInfoList.size(); i++) {
            hashSet.add(mDeviceInfoList.get(i).getSn());
//            tempMap.put(mDeviceInfoList.get(i).getSn(), mDeviceInfoList.get(i).getSn());
        }
        for (int j = 0; j < list.size(); j++) {
            DeviceInfo deviceInfo = list.get(j);
            if (mNearByDeviceMap.containsKey(deviceInfo.getSn())) {
                deviceInfo.setSort(2);
            } else {
                deviceInfo.setSort(0);
            }
            if (!hashSet.contains(deviceInfo.getSn())) {
                mDeviceInfoList.add(deviceInfo);
            }
        }
//        DeviceInfoComparator comparator = new DeviceInfoComparator();
//        Collections.sort(mDeviceInfoList, comparator);
        notifyDataSetChanged();
    }

    public void refreshNew(SensoroDevice sensoroDevice, boolean isSearchStatus) {
        String sn = sensoroDevice.getSn();
        if (sn.endsWith("A939")){
            Log.e("",sn);
        }
        if (!mNearByDeviceMap.containsKey(sn)) {
            mNearByDeviceMap.put(sn, sensoroDevice);
            ((LoRaSettingApplication) mContext.getApplicationContext()).updateNearDeviceMap();
//            notifyDataSetChanged();
        }
        final DeviceInfo cacheDeviceInfo = mCacheDeviceInfoMap.get(sn);
        if (isSearchStatus || cacheDeviceInfo == null) {
            return;
        }
        boolean isContains = false;
        for (int j = 0; j < mDeviceInfoList.size(); j++) {
            DeviceInfo deviceInfo = mDeviceInfoList.get(j);
            if (sn.equalsIgnoreCase(deviceInfo.getSn())) {
                deviceInfo.setSort(2);
                isContains = true;
                break;
            }
        }
        if (!isContains && isFitable(sensoroDevice, cacheDeviceInfo)) {
            mDeviceInfoList.add(cacheDeviceInfo);
        }
        notifyDataSetChanged();
    }

    public void refreshGone(SensoroDevice sensoroDevice, boolean isSearchStatus) {
        String sn = sensoroDevice.getSn();
        //TODO 修改删除方式
        if (mNearByDeviceMap.containsKey(sn)) {
            mNearByDeviceMap.remove(sn, sensoroDevice);
            ((LoRaSettingApplication) mContext.getApplicationContext()).updateNearDeviceMap();
            for (int j = 0; j < mDeviceInfoList.size(); j++) {
                final DeviceInfo deviceInfo = mDeviceInfoList.get(j);
                if (sn.equalsIgnoreCase(deviceInfo.getSn())) {
                    deviceInfo.setSort(0);
                    deviceInfo.setSelected(false);
                    if (isFilterNearby() && !isSearchStatus) {
//                        mNearByDeviceMap.remove(sn);
                        mDeviceInfoList.remove(deviceInfo);
//                    notifyDataSetChanged();
                        break;
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    public void refreshSensorNew(final SensoroSensorTest sensoroSensor) {
        if (!mSensorMap.containsKey(sensoroSensor.getSn())) {
            mSensorMap.put(sensoroSensor.getSn(), sensoroSensor);
            notifyDataSetChanged();
        }

    }

    public void refreshSensor(SensoroSensorTest sensoroSensor) {
        if (mSensorMap.containsKey(sensoroSensor.getSn())) {
            mSensorMap.replace(sensoroSensor.getSn(), mSensorMap.get(sensoroSensor.getSn()), sensoroSensor);
            notifyDataSetChanged();
        }
    }

    public void refreshSensorGone(final SensoroSensorTest sensoroSensor) {
        if (mSensorMap.containsKey(sensoroSensor.getSn())) {
            mSensorMap.remove(sensoroSensor.getSn());
            notifyDataSetChanged();
        }
    }


    public void clear() {
        mDeviceInfoList.clear();
    }

    public void clearCache() {
        mCacheDeviceInfoMap.clear();
    }


    public void filter() {

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFERENCE_FILTER, Context
                .MODE_PRIVATE);
        final HashSet<String> firmwareSet = (HashSet) sharedPreferences.getStringSet("device_firmware", null);
        final HashSet<String> hardwareSet = (HashSet) sharedPreferences.getStringSet("device_hardware", null);
        final HashSet<String> bandSet = (HashSet) sharedPreferences.getStringSet("device_band", null);
        final HashSet<String> signalSet = (HashSet) sharedPreferences.getStringSet("device_signal", null);
        final HashSet<String> nearSet = (HashSet) sharedPreferences.getStringSet("device_near", null);
        final HashSet<String> enableFilterSet = (HashSet) sharedPreferences.getStringSet("device_enable_filter", null);
        boolean isClose = false;
        if (enableFilterSet != null) {
            for (String switchString : enableFilterSet) {
                if (switchString.equals("1")) {//close
                    isClose = true;
                    break;
                }
            }
        }

        if ((firmwareSet != null && hardwareSet != null && signalSet != null && nearSet != null && bandSet != null)
                && !isClose) {
            final List<DeviceInfo> tempFirmwareList = new ArrayList<>();
            for (String firmWare : firmwareSet) {
                for (String key : mCacheDeviceInfoMap.keySet()) {
                    DeviceInfo deviceInfo = mCacheDeviceInfoMap.get(key);
                    if (deviceInfo != null) {
                        String firmwareVersion = deviceInfo.getFirmwareVersion();
                        if (firmwareVersion.contains(firmWare)) {
                            tempFirmwareList.add(mCacheDeviceInfoMap.get(key));
                        }
                    }
                }
            }

            final List<DeviceInfo> tempHardwareList = new ArrayList<>();
            for (String hardware : hardwareSet) {
                for (int i = 0; i < tempFirmwareList.size(); i++) {
                    String deviceType = tempFirmwareList.get(i).getDeviceType();
                    if (hardware == null || deviceType == null) {
                    } else {
                        if (deviceType.contains(hardware)) {
                            tempHardwareList.add(tempFirmwareList.get(i));
                        }
                    }

                }
            }
            tempFirmwareList.clear();
            final List<DeviceInfo> tempBandList = new ArrayList<>();
            for (String bandData : bandSet) {
                for (int i = 0; i < tempHardwareList.size(); i++) {
                    String band = tempHardwareList.get(i).getBand();
                    if (band == null) {
                    } else {
                        if (bandData.equalsIgnoreCase(band)) {
                            tempBandList.add(tempHardwareList.get(i));
                        }
                    }

                }
            }

            tempHardwareList.clear();
            final List<DeviceInfo> tempSignalList = new ArrayList<>();
            for (String signal : signalSet) {
                for (int i = 0; i < tempBandList.size(); i++) {
                    if (tempBandList.get(i).getRssi() >= Integer.parseInt(signal)) {
                        tempSignalList.add(tempBandList.get(i));
                    }
                }
            }
            tempBandList.clear();
            final List<DeviceInfo> deviceInfoList = new ArrayList<>();
            for (String near : nearSet) {
                if (near.equals("1")) {//near
                    for (int i = 0; i < tempSignalList.size(); i++) {
                        if (mNearByDeviceMap.containsKey(tempSignalList.get(i).getSn())) {
                            tempSignalList.get(i).setSort(2);
                            deviceInfoList.add(tempSignalList.get(i));
                        } else {
                            tempSignalList.get(i).setSort(0);
                        }
                    }
                } else {//all
                    for (int i = 0; i < tempSignalList.size(); i++) {
                        if (mNearByDeviceMap.containsKey(tempSignalList.get(i).getSn())) {
                            tempSignalList.get(i).setSort(2);
                        } else {
                            tempSignalList.get(i).setSort(0);
                        }
                        deviceInfoList.add(tempSignalList.get(i));
                    }
                }
            }
            tempSignalList.clear();
            mDeviceInfoList.clear();
            mDeviceInfoList.addAll(deviceInfoList);
            deviceInfoList.clear();

        } else {
            mDeviceInfoList.clear();
            mDeviceInfoList.addAll(mCacheDeviceInfoMap.values());
        }

//        DeviceInfoComparator comparator = new DeviceInfoComparator();
//        Collections.sort(mDeviceInfoList, comparator);
        notifyDataSetChanged();
    }

    public boolean isFilterNearby() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFERENCE_FILTER, Context
                .MODE_PRIVATE);
        final HashSet<String> nearSet = (HashSet) sharedPreferences.getStringSet("device_near", null);
        final HashSet<String> enableFilterSet = (HashSet) sharedPreferences.getStringSet("device_enable_filter", null);
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

    public boolean isFitable(SensoroDevice sensoroDevice, DeviceInfo deviceInfo) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(Constants.PREFERENCE_FILTER, Context
                .MODE_PRIVATE);
        final HashSet<String> firmwareSet = (HashSet) sharedPreferences.getStringSet("device_firmware", null);
        final HashSet<String> hardwareSet = (HashSet) sharedPreferences.getStringSet("device_hardware", null);
        final HashSet<String> bandSet = (HashSet) sharedPreferences.getStringSet("device_band", null);
        final HashSet<String> signalSet = (HashSet) sharedPreferences.getStringSet("device_signal", null);
        final HashSet<String> nearSet = (HashSet) sharedPreferences.getStringSet("device_near", null);
        final HashSet<String> enableFilterSet = (HashSet) sharedPreferences.getStringSet("device_enable_filter", null);
        if (enableFilterSet != null) {
            for (String switchString : enableFilterSet) {
                if (switchString.equals("1")) {//close
                    return true;
                }
            }
        }

        boolean isFitableFirmware = false;
        if (firmwareSet != null) {
            for (String key : firmwareSet) {
                if (sensoroDevice.getFirmwareVersion().equalsIgnoreCase(key)) {
                    isFitableFirmware = true;
                }
            }
        } else {
            isFitableFirmware = true;
        }

        boolean isFitHardwareable = false;
        if (hardwareSet != null) {
            for (String key : hardwareSet) {
                if (deviceInfo.getDeviceType().equalsIgnoreCase(key)) {
                    isFitHardwareable = true;
                }
            }
        } else {
            isFitHardwareable = true;
        }

        boolean isFitableBand = false;
        if (bandSet != null) {
            for (String key : bandSet) {
                if (deviceInfo.getBand().equalsIgnoreCase(key)) {
                    isFitableBand = true;
                }

            }
        } else {
            isFitableBand = true;
        }

        boolean isFitSignalable = false;
        if (nearSet != null) {
            for (String nearKey : nearSet) {
                if (nearKey.equals("1")) {
                    for (String key : signalSet) {
                        if ((sensoroDevice.getRssi() >= Integer.parseInt(key))) {
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

        return isFitSignalable && isFitableFirmware && isFitHardwareable && isFitableBand;
    }

    public LinearLayout getItemLayout(View view) {
        DeviceItemViewHolder viewHolder = (DeviceItemViewHolder) view.getTag();
        return viewHolder.itemLayout;
    }

    @Override
    public int getCount() {
        return mDeviceInfoList.size();
    }

    @Override
    public DeviceInfo getItem(int i) {
        return mDeviceInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        DeviceItemViewHolder itemViewHolder = null;
        if (view == null) {
            view = mInflater.inflate(R.layout.item_device, null);
            itemViewHolder = new DeviceItemViewHolder(view);
            view.setTag(itemViewHolder);
        } else {
            itemViewHolder = (DeviceItemViewHolder) view.getTag();
        }

        if (mDeviceInfoList.size() > 0) {
            DeviceInfo deviceInfo = mDeviceInfoList.get(position);
            String sn = deviceInfo.getSn();
            itemViewHolder.snTextView.setText(sn);
            itemViewHolder.nameTextView.setText(deviceInfo.getName());
            String status_str = mContext.getString(R.string.unknow);

            switch (deviceInfo.getNormalStatus()) {
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
                    Drawable drawable2 = mContext.getResources().getDrawable(R.drawable.shape_status_fault);
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
            itemViewHolder.statusTimeTextView.setText(DateUtil.getDateDiffWithFormat(mContext, deviceInfo
                    .getLastUpTime(), "MM-dd"));
            List<String> list = deviceInfo.getTags();
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            itemViewHolder.tagRecyclerView.setLayoutManager(layoutManager);
            itemViewHolder.tagRecyclerView.setAdapter(new TagAdapter(mContext, list));
            String default_value = mContext.getString(R.string.item_tv_default);
            itemViewHolder.powerTextView.setText(mContext.getString(R.string.item_tv_power) + (deviceInfo.getLoraTxp
                    () == 0 ? default_value : (deviceInfo.getLoraTxp()) + "dBm"));
            int interval = deviceInfo.getInterval();
            itemViewHolder.intervalTextView.setText(mContext.getString(R.string.item_tv_freq) + (interval == 0 ?
                    default_value : (interval + "s")));
            String band = deviceInfo.getBand();
            itemViewHolder.bandTextView.setText(mContext.getString(R.string.item_band) + band + "MHz");
            int sf = (int) deviceInfo.getSf();
            itemViewHolder.sfTextView.setText(mContext.getString(R.string.item_sf) + sf);
            if (deviceInfo.getBattery() < 0) {
                itemViewHolder.batteryView.setVisibility(GONE);
                itemViewHolder.pluginImageView.setVisibility(VISIBLE);
            } else {
                itemViewHolder.batteryView.setVisibility(VISIBLE);
                itemViewHolder.pluginImageView.setVisibility(GONE);
                itemViewHolder.batteryView.setBattery(deviceInfo.getBattery());
            }

            String version = deviceInfo.getFirmwareVersion();
//            boolean isNearBy = false;
//            if (deviceInfo.toSensoroDeviceType() == DeviceInfo.TYPE_SENSOR) {
//                isNearBy = mSensorMap.containsKey(sn);
//                if (isNearBy) {
//                    itemViewHolder.nearByTextView.setText(mContext.getString(R.string.nearby));
//                    itemViewHolder.nearByTextView.setVisibility(View.VISIBLE);
//                    version = mSensorMap.get(sn).getFirmwareVersion();
//                    deviceInfo.setConnectable(true);
//                    deviceInfo.setRssi(mSensorMap.get(sn).getRssi());
//                } else {
//                    itemViewHolder.nearByTextView.setVisibility(GONE);
//                    deviceInfo.setConnectable(false);
//                }
//            } else {
//                isNearBy = mNearByDeviceMap.containsKey(sn);
//                if (isNearBy) {
//                    itemViewHolder.nearByTextView.setText(mContext.getString(R.string.nearby));
//                    itemViewHolder.nearByTextView.setVisibility(View.VISIBLE);
//                    version = mNearByDeviceMap.get(sn).getFirmwareVersion();
//                    deviceInfo.setConnectable(true);
//                    deviceInfo.setRssi(mNearByDeviceMap.get(sn).getRssi());
//                } else {
//                    itemViewHolder.nearByTextView.setVisibility(GONE);
//                    deviceInfo.setConnectable(false);
//                }
//            }

            boolean isNearby = mNearByDeviceMap.containsKey(sn);
            if (isNearby) {
                itemViewHolder.nearByTextView.setText(mContext.getString(R.string.nearby));
                itemViewHolder.nearByTextView.setVisibility(VISIBLE);
                SensoroDevice sensoroDevice = mNearByDeviceMap.get(sn);
                if (sensoroDevice != null) {
                    deviceInfo.setRssi(mNearByDeviceMap.get(sn).getRssi());
                    if (deviceInfo.getFirmwareVersion().compareTo(sensoroDevice.getFirmwareVersion()) > 0) {
                        version = deviceInfo.getFirmwareVersion();
                    } else {
                        version = sensoroDevice.getFirmwareVersion();
                    }
                    deviceInfo.setFirmwareVersion(version);
                }

                deviceInfo.setConnectable(true);

            } else {
                itemViewHolder.nearByTextView.setVisibility(GONE);
                deviceInfo.setConnectable(false);
            }
            if (TextUtils.isEmpty(version)) {
                itemViewHolder.versionTextView.setText("-");
            } else {
                itemViewHolder.versionTextView.setText("V" + version);
            }
            if (deviceInfo.getDeviceType().equals("chip") || deviceInfo.getDeviceType().equals("op_chip") ||
                    deviceInfo.getDeviceType().equals("module") || deviceInfo.getDeviceType().equals("op_node")) {
                itemViewHolder.iconImageView.setImageResource(R.mipmap.ic_device_module);
            } else if (deviceInfo.getDeviceType().equals("angle")) {
                itemViewHolder.iconImageView.setImageResource(R.mipmap.ic_device_angle);
            } else if (deviceInfo.getDeviceType().equals("smoke")) {
                itemViewHolder.iconImageView.setImageResource(R.mipmap.ic_device_smoke);
            } else if (deviceInfo.getDeviceType().equals("cover")) {
                itemViewHolder.iconImageView.setImageResource(R.mipmap.ic_device_cover);
            } else if (deviceInfo.getDeviceType().equals("ch4") || deviceInfo.getDeviceType().equals("tvoc") ||
                    deviceInfo.getDeviceType().equals("pm") || deviceInfo.getDeviceType().equals("o3") ||
                    deviceInfo.getDeviceType().equals("lpg") || deviceInfo.getDeviceType().equals("co2") ||
                    deviceInfo.getDeviceType().equals("co") ||
                    deviceInfo.getDeviceType().equals("nh4") || deviceInfo.getDeviceType().equals("so2") ||
                    deviceInfo.getDeviceType().equals("no2")) {
                itemViewHolder.iconImageView.setImageResource(R.mipmap.ic_device_gas);
            } else if (deviceInfo.getDeviceType().equals("leak")) {
                itemViewHolder.iconImageView.setImageResource(R.mipmap.ic_device_leak);
            } else if (deviceInfo.getDeviceType().equals("temp_humi")) {
                itemViewHolder.iconImageView.setImageResource(R.mipmap.ic_device_temp_humi);
            } else if (deviceInfo.getDeviceType().equals("tracker")) {
                itemViewHolder.iconImageView.setImageResource(R.mipmap.ic_tracker);
            } else {
                itemViewHolder.iconImageView.setImageResource(R.mipmap.ic_device);
            }

            String deviceInfo_sn = deviceInfo.getSn();
            itemViewHolder.sensorLinearLayout.setVisibility(GONE);
            itemViewHolder.sensorSepView.setVisibility(GONE);
            itemViewHolder.sensorTempLayout.setVisibility(GONE);
            itemViewHolder.sensorHumidityLayout.setVisibility(GONE);
            itemViewHolder.sensorLightLayout.setVisibility(GONE);
            itemViewHolder.sensorCoLayout.setVisibility(GONE);
            itemViewHolder.sensorCo2Layout.setVisibility(GONE);
            itemViewHolder.sensorCh4Layout.setVisibility(GONE);
            itemViewHolder.sensorNo2Layout.setVisibility(GONE);
            itemViewHolder.sensorPm25Layout.setVisibility(GONE);
            itemViewHolder.sensorPm10Layout.setVisibility(GONE);
            itemViewHolder.sensorLpgLayout.setVisibility(GONE);
            itemViewHolder.sensorLeakLayout.setVisibility(GONE);
            itemViewHolder.sensorPitchAngleLayout.setVisibility(GONE);
            itemViewHolder.sensorRollAngleLayout.setVisibility(GONE);
            itemViewHolder.sensorYawAngleLayout.setVisibility(GONE);
            itemViewHolder.sensorWaterPressureLayout.setVisibility(GONE);
            SensoroSensorTest sensoroSensor = mSensorMap.get(deviceInfo_sn);
            if (sensoroSensor != null && isNearby) {
                int sensor_counter = 0;
                if (sensoroSensor.hasTemperature && sensoroSensor.temperature.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorTempLayout.setVisibility(VISIBLE);
                    itemViewHolder.sensorTempTextView.setText(String.format("%.1f", sensoroSensor.temperature
                            .data_float) +
                            " ℃");
                }
                if (sensoroSensor.hasHumidity && sensoroSensor.humidity.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorHumidityLayout.setVisibility(VISIBLE);
                    itemViewHolder.sensorHumidityTextView.setText(String.format("%.1f", sensoroSensor.humidity
                            .data_float)
                            + " RH%");
                }
                if (sensoroSensor.hasLight && sensoroSensor.light.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorLightLayout.setVisibility(VISIBLE);
                    itemViewHolder.sensorLightTextView.setText(String.format("%.1f", sensoroSensor.light.data_float)
                            + " LX");
                }

                if (sensoroSensor.hasCo && sensoroSensor.co.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorCoLayout.setVisibility(VISIBLE);
                    itemViewHolder.sensorCoTextView.setText("" + String.format("%.1f", sensoroSensor.co.data_float));
                }

                if (sensoroSensor.hasCo2 && sensoroSensor.co2.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorCo2TextView.setText("" + String.format("%.1f", sensoroSensor.co2.data_float));
                    itemViewHolder.sensorCo2Layout.setVisibility(VISIBLE);
                }
                if (sensoroSensor.hasNo2 && sensoroSensor.no2.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorNo2TextView.setText("" + String.format("%.1f", sensoroSensor.no2.data_float));
                    itemViewHolder.sensorNo2Layout.setVisibility(VISIBLE);
                }
                if (sensoroSensor.hasCh4 && sensoroSensor.ch4.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorCh4TextView.setText("" + String.format("%.1f", sensoroSensor.ch4.data_float));
                    itemViewHolder.sensorCh4Layout.setVisibility(VISIBLE);
                }
                if (sensoroSensor.hasLpg && sensoroSensor.lpg.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorLpgTextView.setText("" + String.format("%.1f", sensoroSensor.lpg.data_float));
                    itemViewHolder.sensorLpgLayout.setVisibility(VISIBLE);
                }
                if (sensoroSensor.hasPm25 && sensoroSensor.pm25.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorPm25TextView.setText("" + String.format("%.1f", sensoroSensor
                            .pm25.data_float));
                    itemViewHolder.sensorPm25Layout.setVisibility(VISIBLE);
                }
                if (sensoroSensor.hasPm10 && sensoroSensor.pm10.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorPm10TextView.setText("" + String.format("%.1f", sensoroSensor
                            .pm10.data_float));
                    itemViewHolder.sensorPm10Layout.setVisibility(VISIBLE);
                }
                if (sensoroSensor.hasPitch && sensoroSensor.pitch.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorPitchAngleTextView.setText("P " + String.format("%.1f", sensoroSensor
                            .pitch.data_float) + "°。");
                    itemViewHolder.sensorPitchAngleLayout.setVisibility(VISIBLE);
                }
                if (sensoroSensor.hasRoll && sensoroSensor.roll.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorRollAngleTextView.setText("R " + String.format("%.1f", sensoroSensor
                            .roll.data_float) + "°。");
                    itemViewHolder.sensorRollAngleLayout.setVisibility(VISIBLE);
                }
                if (sensoroSensor.hasYaw && sensoroSensor.yaw.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorYawAngleTextView.setText("Y " + String.format("%.1f", sensoroSensor
                            .yaw.data_float) + "°。");
                    itemViewHolder.sensorYawAngleLayout.setVisibility(VISIBLE);
                }
                if (sensoroSensor.hasWaterPressure && sensoroSensor.waterPressure.has_data) {
                    sensor_counter++;
                    itemViewHolder.sensorWaterPressureTextView.setText("WaterPressure " + String.format("%.1f",
                            sensoroSensor.waterPressure.data_float) + "");
                    itemViewHolder.sensorWaterPressureLayout.setVisibility(VISIBLE);
                }
                if (sensoroSensor.hasLeak && sensoroSensor.leak.has_data) {
                    sensor_counter++;
                    if (sensoroSensor.leak.data_int == 0) {
                        Drawable drawableNormal = mContext.getResources().getDrawable(R.mipmap.ic_leak_normal);
                        drawableNormal.setBounds(0, 0, drawableNormal != null ? drawableNormal.getMinimumWidth() : 0,
                                drawableNormal.getMinimumHeight());
                        itemViewHolder.sensorLeakTextView.setCompoundDrawables(drawableNormal, null, null, null);
                        itemViewHolder.sensorLeakTextView.setText(R.string.leak_normal);
                        itemViewHolder.sensorLeakLayout.setVisibility(VISIBLE);
                    } else {
                        Drawable drawableWarning = mContext.getResources().getDrawable(R.mipmap.ic_leak_warning);
                        drawableWarning.setBounds(0, 0, drawableWarning != null ? drawableWarning.getMinimumWidth() :
                                0, drawableWarning.getMinimumHeight());
                        itemViewHolder.sensorLeakTextView.setCompoundDrawables(drawableWarning, null, null, null);
                        itemViewHolder.sensorLeakTextView.setText(R.string.leak_warn);
                        itemViewHolder.sensorLeakLayout.setVisibility(VISIBLE);

                    }

                }
                if (sensor_counter > 0) {
                    itemViewHolder.sensorLinearLayout.setVisibility(VISIBLE);
                    itemViewHolder.sensorSepView.setVisibility(VISIBLE);
                }
            }
            if (!deviceInfo.isSelected()) {
                itemViewHolder.itemLayout.setBackgroundResource(R.drawable.shape_device_item);
            } else {
                itemViewHolder.itemLayout.setBackgroundResource(R.drawable.shape_shadow_layer);
            }
        }

        return view;
    }


    class DeviceItemViewHolder {
        @BindView(R.id.device_item_icon)
        ImageView iconImageView;
        @BindView(R.id.device_item_sn)
        TextView snTextView;
        @BindView(R.id.device_item_name)
        TextView nameTextView;
        @BindView(R.id.device_item_status)
        TextView statusTextView;
        @BindView(R.id.device_item_status_time)
        TextView statusTimeTextView;
        @BindView(R.id.device_item_nearby)
        TextView nearByTextView;
        @BindView(R.id.device_item_version)
        TextView versionTextView;
        @BindView(R.id.device_item_power)
        TextView powerTextView;
        @BindView(R.id.device_item_interval)
        TextView intervalTextView;
        @BindView(R.id.device_item_freq)
        TextView bandTextView;
        @BindView(R.id.device_item_sf)
        TextView sfTextView;
        @BindView(R.id.device_item_sensor_temp)
        TextView sensorTempTextView;
        @BindView(R.id.device_item_layout_sensor_temp)
        LinearLayout sensorTempLayout;
        @BindView(R.id.device_item_sensor_humity)
        TextView sensorHumidityTextView;
        @BindView(R.id.device_item_layout_sensor_humidity)
        LinearLayout sensorHumidityLayout;
        @BindView(R.id.device_item_sensor_light)
        TextView sensorLightTextView;
        @BindView(R.id.device_item_layout_sensor_light)
        LinearLayout sensorLightLayout;
        @BindView(R.id.device_item_sensor_co)
        TextView sensorCoTextView;
        @BindView(R.id.device_item_layout_sensor_co)
        LinearLayout sensorCoLayout;
        @BindView(R.id.device_item_sensor_co2)
        TextView sensorCo2TextView;
        @BindView(R.id.device_item_layout_sensor_co2)
        LinearLayout sensorCo2Layout;
        @BindView(R.id.device_item_sensor_no2)
        TextView sensorNo2TextView;
        @BindView(R.id.device_item_layout_sensor_no2)
        LinearLayout sensorNo2Layout;
        @BindView(R.id.device_item_sensor_ch4)
        TextView sensorCh4TextView;
        @BindView(R.id.device_item_layout_sensor_ch4)
        LinearLayout sensorCh4Layout;
        @BindView(R.id.device_item_sensor_lpg)
        TextView sensorLpgTextView;
        @BindView(R.id.device_item_layout_sensor_lpg)
        LinearLayout sensorLpgLayout;
        @BindView(R.id.device_item_sensor_pm25)
        TextView sensorPm25TextView;
        @BindView(R.id.device_item_layout_sensor_pm25)
        LinearLayout sensorPm25Layout;
        @BindView(R.id.device_item_sensor_pm10)
        TextView sensorPm10TextView;
        @BindView(R.id.device_item_layout_sensor_pm10)
        LinearLayout sensorPm10Layout;
        @BindView(R.id.device_item_sensor_leak)
        TextView sensorLeakTextView;
        @BindView(R.id.device_item_layout_sensor_leak)
        LinearLayout sensorLeakLayout;
        @BindView(R.id.device_item_layout_sensor_angle_pitch)
        LinearLayout sensorPitchAngleLayout;
        @BindView(R.id.device_item_layout_sensor_angle_roll)
        LinearLayout sensorRollAngleLayout;
        @BindView(R.id.device_item_layout_sensor_angle_yaw)
        LinearLayout sensorYawAngleLayout;
        @BindView(R.id.device_item_layout_sensor_water)
        LinearLayout sensorWaterPressureLayout;
        @BindView(R.id.device_item_sensor_angle_pitch)
        TextView sensorPitchAngleTextView;
        @BindView(R.id.device_item_sensor_angle_roll)
        TextView sensorRollAngleTextView;
        @BindView(R.id.device_item_sensor_angle_yaw)
        TextView sensorYawAngleTextView;
        @BindView(R.id.device_item_sensor_water)
        TextView sensorWaterPressureTextView;
        @BindView(R.id.device_item_tag_rv)
        RecyclerView tagRecyclerView;
        @BindView(R.id.device_item_battery)
        BatteryView batteryView;
        @BindView(R.id.device_item_plugin)
        ImageView pluginImageView;
        @BindView(R.id.item_device_ll_sensor)
        LinearLayout sensorLinearLayout;
        @BindView(R.id.item_sensor_sep)
        View sensorSepView;
        @BindView(R.id.item_device_ll)
        LinearLayout itemLayout;
        View.OnClickListener clickListener;

        public DeviceItemViewHolder(final View itemView, View.OnClickListener clickListener) {
            ButterKnife.bind(this, itemView);
//            this.clickListener = clickListener;
        }

        public DeviceItemViewHolder(final View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        //去重复并排序
        final HashSet<DeviceInfo> deviceInfos = new HashSet<>(mDeviceInfoList);
        mDeviceInfoList.clear();
        mDeviceInfoList.addAll(deviceInfos);
//        Collections.sort(mDeviceInfoList);
        deviceInfos.clear();
        super.notifyDataSetChanged();
    }
}