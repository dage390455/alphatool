package com.sensoro.loratool.presenter;

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
import com.sensoro.loratool.imainview.IScanDeviceAcView;

import java.util.ArrayList;
import java.util.List;

public class InputSNActivityPresenter extends BasePresenter<IInputSNActivityView>{
    private Context mContext;

    @Override
    public void initData(Context context) {
        mContext = context;
    }

    @Override
    public void onDestroy() {

    }

    public void clickBtn(String s) {
        if(!TextUtils.isEmpty(s)&&s.length()==16){
            scanFinish(s);
        }else{
            getView().toastShort(mContext.getResources().getString(R.string.input_correct_sn));
        }
    }

    private void scanFinish(final String scanSerialNumber) {
        getView().showProgressDialog();
        final Intent intent = new Intent(mContext,DeviceDetailActivity.class);
        LoRaSettingApplication application = (LoRaSettingApplication) mContext.getApplicationContext();
        final boolean[] isCache = {false};
        final List<DeviceInfo> deviceInfoList = application.getDeviceInfoList();
        if(deviceInfoList.size()>0){
            for (DeviceInfo deviceInfo : deviceInfoList) {
                if(deviceInfo.getSn().equals(scanSerialNumber)){
                    intent.putExtra("deviceInfo",deviceInfo);
                    addTags(intent, deviceInfo);
                    isCache[0] = true;
                    break;
                }
            }
        }

        if(!isCache[0]){
            application.loRaSettingServer.deviceAll(scanSerialNumber, new Response.Listener<DeviceInfoListRsp>() {
                @Override
                public void onResponse(DeviceInfoListRsp response) {
                    ArrayList<DeviceInfo> infoArrayList = (ArrayList) response.getData().getItems();
                    if (infoArrayList.size() != 0) {
                        for (DeviceInfo  deviceInfo: infoArrayList) {
                            if(deviceInfo.getSn().equals(scanSerialNumber)){
                                intent.putExtra("deviceInfo",deviceInfo);
                                addTags(intent,deviceInfo);
                                isCache[0] = true;
                                break;
                            }
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }

        if(!isCache[0]){
            getView().toastShort(mContext.getString(R.string.ac_scan_obtain_fail));
            getView().dismissProgressDialog();

        }else{
            getView().dismissProgressDialog();
            getView().startAc(intent);
            if (((LoRaSettingApplication) mContext.getApplicationContext()).scanDeviceView!=null) {
                ((LoRaSettingApplication) mContext.getApplicationContext()).scanDeviceView.finishAc();
                ((LoRaSettingApplication) mContext.getApplicationContext()).scanDeviceView = null;
            }
        }
    }

    private void addTags(Intent intent, DeviceInfo deviceInfo) {
        List<String> tags = deviceInfo.getTags();
        StringBuilder sb = new StringBuilder();
        if (tags!= null) {
            for (String tag : tags) {
                sb.append(tag);
            }
        }
        if(sb.length()>0){
            intent.putExtra("tags",sb.toString());
        }
    }
}
