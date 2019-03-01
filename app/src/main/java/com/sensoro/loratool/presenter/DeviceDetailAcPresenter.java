package com.sensoro.loratool.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.sensoro.libbleserver.ble.entity.SensoroDevice;
import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.AdvanceSettingDeviceActivity;
import com.sensoro.loratool.activity.DeviceDetailActivity;
import com.sensoro.loratool.activity.SettingDeviceActivity;
import com.sensoro.loratool.activity.SettingModuleActivity;
import com.sensoro.loratool.activity.SignalDetectionActivity;
import com.sensoro.loratool.activity.UpgradeFirmwareListActivity;
import com.sensoro.loratool.base.BasePresenter;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.imainview.IDeviceDetailAcView;
import com.sensoro.loratool.utils.DateUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import static com.sensoro.loratool.constant.Constants.DEVICE_HARDWARE_TYPE;
import static com.sensoro.loratool.constant.Constants.EXTRA_NAME_DEVICE_INFO;

public class DeviceDetailAcPresenter extends BasePresenter<IDeviceDetailAcView> implements LoRaSettingApplication
        .INearDeviceListener {
    private Activity mContext;
    private DeviceInfo mDeviceInfo;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        ((LoRaSettingApplication) mContext.getApplicationContext()).registerNearDeviceListener(this);
        mDeviceInfo = ((DeviceDetailActivity) context).getIntent().getParcelableExtra("deviceInfo");
        initWidget();
    }

    @Override
    public void onDestroy() {
        ((LoRaSettingApplication) mContext.getApplicationContext()).unregisterNearDeviceListener(this);
    }

    private void initWidget() {
        for (int i = 0; i < DEVICE_HARDWARE_TYPE.length; i++) {
            if (DEVICE_HARDWARE_TYPE[i].contains(mDeviceInfo.getDeviceType())) {
                String s = mContext.getResources().getStringArray(R.array.filter_device_hardware_array)[i];
                getView().setTvNameVisible(true);
                getView().setTvNameContent(s);
                break;
            }
        }

        ConcurrentHashMap<String, SensoroDevice> nearDeviceMap = ((LoRaSettingApplication) mContext
                .getApplicationContext()).getmNearDeviceMap();
        getView().setTvNearVisible(nearDeviceMap.containsKey(mDeviceInfo.getSn()));

        getView().setTvVersionContent(String.format(Locale.CHINA, "V %s", mDeviceInfo.getFirmwareVersion()));

        getView().setTvLocationContent(mDeviceInfo.getName());

        getView().setTvElectricQuantityContent(String.format(Locale.CHINA, "%d%%", mDeviceInfo.getBattery()));

        getView().setTvReportTimeContent(String.format(Locale.CHINA, "数据上报时间：%s", formatResportTime(mDeviceInfo)));

        initTvState();
        initTvTest();
        initRcContent();

    }

    private void initRcContent() {
        ArrayList<String> valueList = new ArrayList<>();
        valueList.add(String.format(Locale.CHINA, "%ddBm", mDeviceInfo.getLoraTxp()));
        valueList.add(String.format(Locale.CHINA, "%ds", mDeviceInfo.getInterval()));
        valueList.add(String.format(Locale.CHINA, "%sMHz", mDeviceInfo.getBand()));
        valueList.add(String.format(Locale.CHINA, "%d", (int) mDeviceInfo.getSf()));
        DecimalFormat decimalFormat = new DecimalFormat("##0.0");
        valueList.add(String.format(Locale.CHINA, "%s℃", decimalFormat.format(mDeviceInfo.getTemperature())));
        valueList.add(String.format(Locale.CHINA, "%s%%", decimalFormat.format(mDeviceInfo.getHumidity())));

        ArrayList<String> keyList = new ArrayList<>();
        keyList.add("功率");
        keyList.add("周期");
        keyList.add("频段");
        keyList.add("扩频方式");
        keyList.add("温度");
        keyList.add("湿度");
        getView().setRcKeyList(keyList);
        getView().updateRcValueList(valueList);
        getView().setRcAdapter();
    }

    private void initTvTest() {
        String tags = mContext.getIntent().getStringExtra("tags");
        if (tags != null && tags.length() > 0) {
            getView().setTvTestContent(tags);
        } else {
            getView().setTvTestVisible(false);
        }

    }

    private void initTvState() {
        String defStr = mContext.getString(R.string.unknow);
        Drawable drawable;
        switch (mDeviceInfo.getNormalStatus()) {
            case 0:
                drawable = mContext.getResources().getDrawable(R.drawable.shape_oval);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(mContext.getResources().getColor(R.color.status_normal), PorterDuff.Mode
                        .MULTIPLY);

                defStr = mContext.getString(R.string.status_normal);
                break;
            case 1:
                drawable = mContext.getResources().getDrawable(R.drawable.shape_status_fault);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(mContext.getResources().getColor(R.color.status_fault), PorterDuff.Mode
                        .MULTIPLY);
                defStr = mContext.getString(R.string.status_fault);
                break;
            case 2:
                drawable = mContext.getResources().getDrawable(R.drawable.shape_status_fault);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(mContext.getResources().getColor(R.color.status_serious), PorterDuff
                        .Mode.MULTIPLY);
                defStr = mContext.getString(R.string.status_serious);
                break;
            case 3:
                drawable = mContext.getResources().getDrawable(R.drawable.shape_status_timeout);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(mContext.getResources().getColor(R.color.status_timeout), PorterDuff
                        .Mode.MULTIPLY);
                defStr = mContext.getString(R.string.status_timeout);
                break;
            case -1:
                drawable = mContext.getResources().getDrawable(R.drawable.shape_status_inactive);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(mContext.getResources().getColor(R.color.status_inactive), PorterDuff
                        .Mode.MULTIPLY);
                defStr = mContext.getString(R.string.status_inactive);
                break;
            case 4:
                drawable = mContext.getResources().getDrawable(R.drawable.shape_status_offline);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(mContext.getResources().getColor(R.color.status_offline), PorterDuff
                        .Mode.MULTIPLY);
                defStr = mContext.getString(R.string.status_offline);
            default:
                drawable = mContext.getResources().getDrawable(R.drawable.shape_status_inactive);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(mContext.getResources().getColor(R.color.status_inactive), PorterDuff
                        .Mode.MULTIPLY);
                break;
        }

        getView().setTvStateCompoundDrawables(drawable);
        getView().setTvStateContent(defStr);
        getView().setTvStateTime(DateUtil.getDateDiffWithFormat(mContext, mDeviceInfo.getLastUpTime(), "MM-DD"));

    }


    public ArrayList<String> initRCValueList(DeviceInfo deviceInfo) {
        ArrayList<String> list = new ArrayList<>();
        list.add(String.format(Locale.CHINA, "%ddBm", deviceInfo.getLoraTxp()));
        list.add(String.format(Locale.CHINA, "%ds", deviceInfo.getInterval()));
        list.add(String.format(Locale.CHINA, "%sMHz", deviceInfo.getBand()));
        list.add(String.format(Locale.CHINA, "%d", (int) deviceInfo.getSf()));
        DecimalFormat decimalFormat = new DecimalFormat("##0.0");
        list.add(String.format(Locale.CHINA, "%s℃", decimalFormat.format(deviceInfo.getTemperature())));
        list.add(String.format(Locale.CHINA, "%s%%", decimalFormat.format(deviceInfo.getHumidity())));

        return list;
    }

    public ArrayList<String> initRCKeyList() {
        ArrayList<String> list = new ArrayList<>();
        list.add("功率");
        list.add("周期");
        list.add("频段");
        list.add("扩频方式");
        list.add("温度");
        list.add("湿度");
        return list;
    }

    public void config() {
        ConcurrentHashMap<String, SensoroDevice> nearDeviceMap = ((LoRaSettingApplication) mContext.getApplication())
                .getmNearDeviceMap();
        SensoroDevice sensoroDevice = nearDeviceMap.get(mDeviceInfo.getSn());
        if (sensoroDevice != null) {
            sensoroDevice.setPassword(mDeviceInfo.getPassword());
            sensoroDevice.setFirmwareVersion(mDeviceInfo.getFirmwareVersion());
            sensoroDevice.setBand(mDeviceInfo.getBand());
            sensoroDevice.setHardwareVersion(mDeviceInfo.getDeviceType());
            Intent intent = new Intent();
            if (mDeviceInfo.toSensoroDeviceType() == DeviceInfo.TYPE_MODULE) {
                intent.setClass(mContext, SettingModuleActivity.class);
            } else {
                intent.setClass(mContext, SettingDeviceActivity.class);
            }

            intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, mDeviceInfo.getDeviceType());
            intent.putExtra(Constants.EXTRA_NAME_BAND, mDeviceInfo.getBand());
            intent.putExtra(Constants.EXTRA_NAME_DEVICE, sensoroDevice);
            getView().startAc(intent);
        } else {
            getView().showShortToast(mContext.getResources().getString(R.string.tips_closeto_device));
        }

    }

    public void cloud() {
        ConcurrentHashMap<String, SensoroDevice> nearDeviceMap = ((LoRaSettingApplication) mContext.getApplication())
                .getmNearDeviceMap();
        SensoroDevice sensoroDevice = nearDeviceMap.get(mDeviceInfo.getSn());
        if (sensoroDevice != null) {
            sensoroDevice.setPassword(mDeviceInfo.getPassword());
            sensoroDevice.setFirmwareVersion(mDeviceInfo.getFirmwareVersion());
            sensoroDevice.setBand(mDeviceInfo.getBand());
            sensoroDevice.setHardwareVersion(mDeviceInfo.getDeviceType());
            Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_NAME_DEVICE, sensoroDevice);
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, mDeviceInfo.getDeviceType());
            intent.putExtra(Constants.EXTRA_NAME_BAND, mDeviceInfo.getBand());
            intent.putExtra(EXTRA_NAME_DEVICE_INFO, mDeviceInfo);
            intent.setClass(mContext, AdvanceSettingDeviceActivity.class);
            getView().startAc(intent);
        } else {
            getView().showShortToast(mContext.getResources().getString(R.string.tips_closeto_device));
        }
    }

    public void upgrade() {
        ConcurrentHashMap<String, SensoroDevice> nearDeviceMap = ((LoRaSettingApplication) mContext.getApplication())
                .getmNearDeviceMap();
        SensoroDevice sensoroDevice = nearDeviceMap.get(mDeviceInfo.getSn());
        if (sensoroDevice != null) {
            sensoroDevice.setPassword(mDeviceInfo.getPassword());
            sensoroDevice.setFirmwareVersion(mDeviceInfo.getFirmwareVersion());
            sensoroDevice.setBand(mDeviceInfo.getBand());
            sensoroDevice.setHardwareVersion(mDeviceInfo.getDeviceType());
            Intent intent = new Intent(mContext, UpgradeFirmwareListActivity.class);
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, mDeviceInfo.getDeviceType());
            intent.putExtra(Constants.EXTRA_NAME_BAND, mDeviceInfo.getBand());
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_HARDWARE_VERSION, mDeviceInfo.getHardwareVersion());
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_FIRMWARE_VERSION, sensoroDevice.getFirmwareVersion());
            ArrayList<SensoroDevice> tempArrayList = new ArrayList<>();
            tempArrayList.add(sensoroDevice);
            intent.putParcelableArrayListExtra(Constants.EXTRA_NAME_DEVICE_LIST, tempArrayList);
            getView().startAc(intent);
        } else {
            getView().showShortToast(mContext.getResources().getString(R.string.tips_closeto_device));
        }

    }

    public void signal() {
        ConcurrentHashMap<String, SensoroDevice> nearDeviceMap = ((LoRaSettingApplication) mContext.getApplication())
                .getmNearDeviceMap();
        SensoroDevice sensoroDevice = nearDeviceMap.get(mDeviceInfo.getSn());
        if (sensoroDevice != null) {
            sensoroDevice.setPassword(mDeviceInfo.getPassword());
            sensoroDevice.setFirmwareVersion(mDeviceInfo.getFirmwareVersion());
            sensoroDevice.setBand(mDeviceInfo.getBand());
            sensoroDevice.setHardwareVersion(mDeviceInfo.getDeviceType());
            Intent intent = new Intent(mContext, SignalDetectionActivity.class);
            intent.putExtra(Constants.EXTRA_NAME_DEVICE, sensoroDevice);
            intent.putExtra(Constants.EXTRA_NAME_BAND, mDeviceInfo.getBand());
            getView().startAc(intent);
        } else {
            getView().showShortToast(mContext.getResources().getString(R.string.tips_closeto_device));
        }

    }

    private String formatResportTime(DeviceInfo deviceInfo) {
        long lastUpTime = deviceInfo.getLastUpTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = new Date();
        date.setTime(lastUpTime);
        return simpleDateFormat.format(date);
    }

    @Override
    public void updateListener() {
        ConcurrentHashMap<String, SensoroDevice> nearDeviceMap =
                ((LoRaSettingApplication) mContext.getApplicationContext()).getmNearDeviceMap();
        getView().setTvNearVisible(nearDeviceMap.containsKey(mDeviceInfo.getSn()));
    }

    public void showPopupWindow() {
        //order
//        configLayout.setVisibility(VISIBLE);
//        cloudLayout.setVisibility(VISIBLE);
//        upgradeLayout.setVisibility(VISIBLE);
//        signalLayout.setVisibility(VISIBLE);
        boolean[] booleans = new boolean[4];
        booleans[0] = Constants.permission[4];
        booleans[1] = Constants.permission[5];
        booleans[2] = Constants.permission[6];
        booleans[3] = mDeviceInfo.isCanSignal();
        boolean support = mDeviceInfo.getDeviceType().equals("op_chip");
        if (support) {
            booleans[0] = false;
            booleans[1] = false;
        }
        getView().showPopSettingItem(booleans);
    }

    public void setBatteryLevel() {
        getView().setBatteryLevel(mDeviceInfo.getBattery());
    }
}
