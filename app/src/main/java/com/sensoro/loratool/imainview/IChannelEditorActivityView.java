package com.sensoro.loratool.imainview;

import android.content.Intent;

import com.sensoro.loratool.model.SettingDeviceModel;

import java.util.ArrayList;
import java.util.List;

public interface IChannelEditorActivityView {
    void updateData(ArrayList<SettingDeviceModel> datas);

    void showDialog(SettingDeviceModel model);

    void dismissDialog();

    void notifyData();

    List<SettingDeviceModel> getData();

    void setIntentResult(int resultCode, Intent intent);
}
