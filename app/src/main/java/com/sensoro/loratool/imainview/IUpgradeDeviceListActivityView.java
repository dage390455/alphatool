package com.sensoro.loratool.imainview;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.sensoro.libbleserver.ble.entity.SensoroDevice;
import com.sensoro.loratool.iwidget.IProgressDialog;
import com.sensoro.loratool.iwidget.IToast;

import java.util.ArrayList;

public interface IUpgradeDeviceListActivityView extends IProgressDialog,IToast{
     void setAddIvVisible(boolean isVisible);

    void updateRcData(ArrayList<SensoroDevice> targetDeviceList);

    ArrayList<SensoroDevice> getAdapterData();

    void updateRc(int deviceIndex, @StringRes int string);

    void updateRcProgressChanged(int deviceIndex,int percent);

    void updateRcPercentAndTip(int deviceIndex,int percent,String dfuInfo);

    SensoroDevice getItemData(int deviceIndex);

    void setStartButtonBg(@DrawableRes int drawableId);

    void notigyAdapter();
}
