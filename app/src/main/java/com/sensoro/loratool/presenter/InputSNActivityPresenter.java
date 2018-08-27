package com.sensoro.loratool.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.lora.setting.server.bean.DeviceInfoListRsp;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.DeviceDetailActivity;
import com.sensoro.loratool.base.BasePresenter;
import com.sensoro.loratool.imainview.IInputSNActivityView;

import java.util.List;

public class InputSNActivityPresenter extends BasePresenter<IInputSNActivityView> {
    private Activity mContext;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
    }

    @Override
    public void onDestroy() {

    }

    public void clickBtn(String s) {
        if (!TextUtils.isEmpty(s) && s.length() == 16) {
            scanFinish(s.toUpperCase());
        } else {
            getView().toastShort(mContext.getResources().getString(R.string.input_correct_sn));
        }
    }

    private void scanFinish(final String scanSerialNumber) {
        getView().showProgressDialog();
        final Intent intent = new Intent(mContext, DeviceDetailActivity.class);
        LoRaSettingApplication application = (LoRaSettingApplication) mContext.getApplicationContext();
        final List<DeviceInfo> deviceInfoList = application.getDeviceInfoList();
        for (DeviceInfo deviceInfo : deviceInfoList) {
            if (deviceInfo.getSn().equals(scanSerialNumber)) {
                intent.putExtra("deviceInfo", deviceInfo);
                addTags(intent, deviceInfo);
                getView().dismissProgressDialog();
                getView().startAc(intent);
                return;
            }
        }
        //
        application.loRaSettingServer.deviceAll(scanSerialNumber, new Response
                .Listener<DeviceInfoListRsp>() {
            @Override
            public void onResponse(DeviceInfoListRsp response) {
                List<DeviceInfo> infoArrayList = response.getData().getItems();
                if (infoArrayList != null) {
                    for (DeviceInfo deviceInfo : infoArrayList) {
                        if (deviceInfo.getSn().equals(scanSerialNumber)) {
                            intent.putExtra("deviceInfo", deviceInfo);
                            addTags(intent, deviceInfo);
                            getView().dismissProgressDialog();
                            getView().startAc(intent);
                            return;
                        }
                    }
                    getView().dismissProgressDialog();
                    getView().toastShort("设备未在账户下");
                } else {
                    getView().dismissProgressDialog();
                    getView().toastShort(mContext.getResources().getString(R.string.ac_scan_obtain_fail));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getView().dismissProgressDialog();
                getView().toastShort(mContext.getResources().getString(R.string.ac_scan_obtain_fail));
            }
        });
        //
//        getView().showProgressDialog();
//        final Intent intent = new Intent(mContext, DeviceDetailActivity.class);
//        LoRaSettingApplication application = (LoRaSettingApplication) mContext.getApplicationContext();
//        final boolean[] isCache = {false};
//        final List<DeviceInfo> deviceInfoList = application.getDeviceInfoList();
//        if (deviceInfoList.size() > 0) {
//            for (DeviceInfo deviceInfo : deviceInfoList) {
//                if (deviceInfo.getSn().equals(scanSerialNumber)) {
//                    intent.putExtra("deviceInfo", deviceInfo);
//                    addTags(intent, deviceInfo);
//                    isCache[0] = true;
//                    break;
//                }
//            }
//        }
//
//        if (!isCache[0]) {
//            application.loRaSettingServer.deviceAll(scanSerialNumber, new Response.Listener<DeviceInfoListRsp>() {
//                @Override
//                public void onResponse(DeviceInfoListRsp response) {
//                    ArrayList<DeviceInfo> infoArrayList = (ArrayList) response.getData().getItems();
//                    if (infoArrayList.size() != 0) {
//                        for (DeviceInfo deviceInfo : infoArrayList) {
//                            if (deviceInfo.getSn().equals(scanSerialNumber)) {
//                                intent.putExtra("deviceInfo", deviceInfo);
//                                addTags(intent, deviceInfo);
//                                isCache[0] = true;
//                                break;
//                            }
//                        }
//                    }
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//
//                }
//            });
//        }
//
//        if (!isCache[0]) {
//            getView().toastShort(mContext.getString(R.string.ac_scan_obtain_fail));
//            getView().dismissProgressDialog();
//
//        } else {
//            getView().dismissProgressDialog();
//            getView().startAc(intent);
//        }
    }

    private void addTags(Intent intent, DeviceInfo deviceInfo) {
        List<String> tags = deviceInfo.getTags();
        if (tags != null && tags.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String tag : tags) {
                sb.append(tag);
            }
            intent.putExtra("tags", sb.toString());
        }
    }
}
