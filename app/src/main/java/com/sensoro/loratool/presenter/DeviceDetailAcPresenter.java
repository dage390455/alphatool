package com.sensoro.loratool.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.widget.Toast;

import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.AdvanceSettingDeviceActivity;
import com.sensoro.loratool.activity.AdvanceSettingMultiDeviceActivity;
import com.sensoro.loratool.activity.DeviceDetailActivity;
import com.sensoro.loratool.activity.SettingDeviceActivity;
import com.sensoro.loratool.activity.SettingModuleActivity;
import com.sensoro.loratool.activity.SignalDetectionActivity;
import com.sensoro.loratool.activity.UpgradeFirmwareListActivity;
import com.sensoro.loratool.base.BasePresenter;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.imainview.IDeviceDetailAcView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import static com.sensoro.loratool.constant.Constants.EXTRA_NAME_DEVICE_INFO;

public class DeviceDetailAcPresenter extends BasePresenter<IDeviceDetailAcView> {
    private Context mContext;

    @Override
    public void initData(Context context) {
        mContext = context;

        DeviceInfo deviceInfo = ((DeviceDetailActivity) context).getIntent().getParcelableExtra("deviceInfo");
        getView().initWidget(deviceInfo);
    }

    @Override
    public void onDestroy() {

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

    public void config(DeviceInfo deviceInfo, ConcurrentHashMap<String, SensoroDevice> nearDeviceMap) {
        SensoroDevice sensoroDevice = nearDeviceMap.get(deviceInfo.getSn());
        if (sensoroDevice != null) {
            sensoroDevice.setPassword(deviceInfo.getPassword());
            sensoroDevice.setFirmwareVersion(deviceInfo.getFirmwareVersion());
            sensoroDevice.setBand(deviceInfo.getBand());
            sensoroDevice.setHardwareVersion(deviceInfo.getDeviceType());
            Intent intent = new Intent();
            if (deviceInfo.toSensoroDeviceType() == DeviceInfo.TYPE_MODULE) {
                intent.setClass(mContext, SettingModuleActivity.class);
            } else {
                intent.setClass(mContext, SettingDeviceActivity.class);
            }

            intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, deviceInfo.getDeviceType());
            intent.putExtra(Constants.EXTRA_NAME_BAND, deviceInfo.getBand());
            intent.putExtra(Constants.EXTRA_NAME_DEVICE, sensoroDevice);
            getView().startAc(intent);
        } else {
            getView().showShortToast(mContext.getResources().getString(R.string.tips_closeto_device));
        }

    }

    public void cloud(DeviceInfo deviceInfo, ConcurrentHashMap<String, SensoroDevice> nearDeviceMap) {
        SensoroDevice sensoroDevice = nearDeviceMap.get(deviceInfo.getSn());
        if (sensoroDevice != null) {
            sensoroDevice.setPassword(deviceInfo.getPassword());
            sensoroDevice.setFirmwareVersion(deviceInfo.getFirmwareVersion());
            sensoroDevice.setBand(deviceInfo.getBand());
            sensoroDevice.setHardwareVersion(deviceInfo.getDeviceType());
            Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_NAME_DEVICE, sensoroDevice);
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, deviceInfo.getDeviceType());
            intent.putExtra(Constants.EXTRA_NAME_BAND, deviceInfo.getBand());
            intent.putExtra(EXTRA_NAME_DEVICE_INFO, deviceInfo);
            intent.setClass(mContext, AdvanceSettingDeviceActivity.class);
            getView().startAc(intent);
        }else{
            getView().showShortToast(mContext.getResources().getString(R.string.tips_closeto_device));
        }
    }

    public void upgrade(DeviceInfo deviceInfo,ConcurrentHashMap<String, SensoroDevice> nearDeviceMap) {
        SensoroDevice sensoroDevice = nearDeviceMap.get(deviceInfo.getSn());
        if(sensoroDevice!=null){
            sensoroDevice.setPassword(deviceInfo.getPassword());
            sensoroDevice.setFirmwareVersion(deviceInfo.getFirmwareVersion());
            sensoroDevice.setBand(deviceInfo.getBand());
            sensoroDevice.setHardwareVersion(deviceInfo.getDeviceType());
            Intent intent = new Intent(mContext, UpgradeFirmwareListActivity.class);
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, deviceInfo.getDeviceType());
            intent.putExtra(Constants.EXTRA_NAME_BAND, deviceInfo.getBand());
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_HARDWARE_VERSION, deviceInfo.getDeviceType());
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_FIRMWARE_VERSION, sensoroDevice.getFirmwareVersion());
            ArrayList<SensoroDevice> tempArrayList = new ArrayList<>();
            tempArrayList.add(sensoroDevice);
            intent.putParcelableArrayListExtra(Constants.EXTRA_NAME_DEVICE_LIST, tempArrayList);
            getView().startAc(intent);
        }else{
            getView().showShortToast(mContext.getResources().getString(R.string.tips_closeto_device));
        }

    }

    public void singal(DeviceInfo deviceInfo, ConcurrentHashMap<String, SensoroDevice> nearDeviceMap) {
        SensoroDevice sensoroDevice = nearDeviceMap.get(deviceInfo.getSn());
        if (sensoroDevice!=null) {
            sensoroDevice.setPassword(deviceInfo.getPassword());
            sensoroDevice.setFirmwareVersion(deviceInfo.getFirmwareVersion());
            sensoroDevice.setBand(deviceInfo.getBand());
            sensoroDevice.setHardwareVersion(deviceInfo.getDeviceType());
            Intent intent = new Intent(mContext, SignalDetectionActivity.class);
            intent.putExtra(Constants.EXTRA_NAME_DEVICE, sensoroDevice);
            intent.putExtra(Constants.EXTRA_NAME_BAND, deviceInfo.getBand());
            getView().startAc(intent);
        }else{
            getView().showShortToast(mContext.getResources().getString(R.string.tips_closeto_device));
        }

    }

    public String formatResportTime(DeviceInfo deviceInfo) {
        long lastUpTime = deviceInfo.getLastUpTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = new Date();
        date.setTime(lastUpTime);
        return simpleDateFormat.format(date);
    }
}
