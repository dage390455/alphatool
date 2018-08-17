package com.sensoro.loratool.imainview;

import com.sensoro.loratool.iwidget.IActivityIntent;
import com.sensoro.loratool.iwidget.IProgressDialog;
import com.sensoro.loratool.iwidget.IToast;


public interface IPointDeployFragmentView extends IToast, IActivityIntent, IProgressDialog {
    void setFlashLightState(boolean isOn);

    void startScan();
    void stopScan();
}
