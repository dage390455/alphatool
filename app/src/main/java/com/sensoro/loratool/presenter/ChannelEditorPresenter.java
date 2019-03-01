package com.sensoro.loratool.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sensoro.libbleserver.ble.entity.SensoroChannel;
import com.sensoro.libbleserver.ble.proto.MsgNode1V1M5;
import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.RecyclerItemClickListener;
import com.sensoro.loratool.base.BasePresenter;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.imainview.IChannelEditorActivityView;
import com.sensoro.loratool.model.SettingDeviceModel;
import com.sensoro.loratool.widget.SettingEnterDialogUtils;
import com.sensoro.station.communication.bean.CheckNetStatusReq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.LongFunction;

public class ChannelEditorPresenter extends BasePresenter<IChannelEditorActivityView>
        implements RecyclerItemClickListener, SettingEnterDialogUtils.SettingEnterUtilsClickListener {
    private Activity mActivity;
    private ArrayList<SensoroChannel> channelArrayList = new ArrayList<>();
    private SettingDeviceModel mModel;


    @Override
    public void initData(Context context) {
        mActivity = (Activity) context;
        Serializable list = mActivity.getIntent().getSerializableExtra(Constants.EXTRA_CHANNEL_EDITOR_DEVICE);

        channelArrayList = (ArrayList<SensoroChannel>) list;


        ArrayList<SettingDeviceModel> datas = new ArrayList<>();
        for (int i = 0; i < channelArrayList.size(); i++) {
            SettingDeviceModel settingDeviceModel = new SettingDeviceModel();
            settingDeviceModel.title = "通道" + i;
            settingDeviceModel.viewType = 2;
            datas.add(settingDeviceModel);

            SettingDeviceModel settingDeviceModel1 = new SettingDeviceModel();
            settingDeviceModel1.name = "上行/MHz";
            settingDeviceModel1.content = String.valueOf(channelArrayList.get(i).frequency/1000000d);
            settingDeviceModel1.viewType = 1;
            settingDeviceModel1.tag = String.format(Locale.ROOT, "%d,%d", i, 1);
            datas.add(settingDeviceModel1);

            SettingDeviceModel settingDeviceModel2 = new SettingDeviceModel();
            settingDeviceModel2.name = "下行/MHz";
            settingDeviceModel2.isDivider = false;
            settingDeviceModel2.content = String.valueOf(channelArrayList.get(i).rx1Frequency/1000000d);
            settingDeviceModel2.viewType = 1;
            settingDeviceModel2.tag = String.format(Locale.ROOT, "%d,%d", i, 2);
            datas.add(settingDeviceModel2);
        }
        getView().updateData(datas);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onItemClick(SettingDeviceModel model, int position) {
        mModel = model;
        SettingDeviceModel mPreItem = getView().getItem(position - 1);
        if (mPreItem != null && mPreItem.content != null) {
            if (Double.valueOf(mPreItem.content) <= 0) {
                getView().toastShort(mActivity.getString(R.string.up_channel_not_zero));
                getView().dismissDialog();
                return;
            }
        }
        getView().showDialog(model);
    }

    @Override
    public void onCancelClick() {
        getView().dismissDialog();
    }

    @Override
    public void onConfirmClick(double value) {
        mModel.content = String.valueOf(value);
        getView().notifyData();
        getView().dismissDialog();
    }

    public void doSave() {
        List<SettingDeviceModel> data = getView().getData();
        for (SettingDeviceModel datum : data) {
            if (datum.tag instanceof String) {
                String[] split = ((String)datum.tag).split(",");
                SensoroChannel sensoroChannel = channelArrayList.get(Integer.valueOf(split[0]));
                if ("1".equals(split[1])) {
                    sensoroChannel.frequency = (int) (Double.valueOf(datum.content) * 1000000);
                } else if ("2".equals(split[1])) {
                    sensoroChannel.rx1Frequency = (int) (Double.valueOf(datum.content) * 1000000);
                }
            }
        }

        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_CHANNEL_RESULT, channelArrayList);
        getView().setIntentResult(0, intent);
    }
}
