package com.sensoro.loratool.presenter;

import android.content.Context;
import android.os.Parcelable;

import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.loratool.activity.DeviceDetailActivity;
import com.sensoro.loratool.base.BasePresenter;
import com.sensoro.loratool.imainview.IDeviceDetailAcView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;

public class DeviceDetailAcPresenter extends BasePresenter<IDeviceDetailAcView> {
    @Override
    public void initData(Context context) {
        DeviceInfo deviceInfo = ((DeviceDetailActivity) context).getIntent().getParcelableExtra("deviceInfo");
        getView().initWidget(deviceInfo);
    }

    @Override
    public void onDestroy() {

    }

    public ArrayList<String> initRCValueList(DeviceInfo deviceInfo){
        ArrayList<String> list = new ArrayList<>();
        list.add(String.format(Locale.CHINA,"%ddBm",deviceInfo.getLoraTxp()));
        list.add(String.format(Locale.CHINA,"%ds",deviceInfo.getInterval()));
        list.add(String.format(Locale.CHINA,"%sMHz",deviceInfo.getBand()));
        list.add(String.format(Locale.CHINA,"%d",(int)deviceInfo.getSf()));
        DecimalFormat decimalFormat = new DecimalFormat("##0.0");
        list.add(String.format(Locale.CHINA,"%s℃",decimalFormat.format(deviceInfo.getTemperature())));
        list.add(String.format(Locale.CHINA,"%s%%",decimalFormat.format(deviceInfo.getHumidity())));

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
}
