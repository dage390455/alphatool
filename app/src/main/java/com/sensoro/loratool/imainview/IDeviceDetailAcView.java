package com.sensoro.loratool.imainview;

import android.content.Intent;

import com.sensoro.lora.setting.server.bean.DeviceInfo;

public interface IDeviceDetailAcView {
    void initWidget(DeviceInfo deviceInfo);
    void startAc(Intent intent);
    void showShortToast(String msg);
}
