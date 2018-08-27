package com.sensoro.loratool.imainview;

import android.content.Intent;

import com.sensoro.loratool.iwidget.IProgressDialog;

public interface IScanDeviceAcView extends IProgressDialog {
    void startScan();
    void stopScan();
    void setFlashLightState(boolean isOn);
    void shortToast(String message);
    void startAC(Intent intent);
    void finishAc();
}
