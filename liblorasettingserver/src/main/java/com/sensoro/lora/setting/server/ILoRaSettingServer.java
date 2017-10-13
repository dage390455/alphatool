package com.sensoro.lora.setting.server;

import com.android.volley.Response;
import com.sensoro.lora.setting.server.bean.DeviceInfoListRsp;
import com.sensoro.lora.setting.server.bean.EidInfo;
import com.sensoro.lora.setting.server.bean.EidInfoListRsp;
import com.sensoro.lora.setting.server.bean.LoginRsp;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.lora.setting.server.bean.StationListRsp;
import com.sensoro.lora.setting.server.bean.UpgradeListRsp;

/**
 * Created by tangrisheng on 2016/5/5.
 * used for lora setting app server interface
 */
public interface ILoRaSettingServer {


    void setSessionId(String sessionId);
    
    boolean login(String id, String pwd, Response.Listener<LoginRsp> listener, Response.ErrorListener errorListener);

    boolean stationList(Response.Listener<StationListRsp> listener, Response.ErrorListener errorListener);

    boolean deviceList(int page, Response.Listener<DeviceInfoListRsp> listener, Response.ErrorListener errorListener);

    boolean deviceUpgradeList(int page, String deviceType, String band, String hv, String fv, Response.Listener<UpgradeListRsp> listener, Response.ErrorListener errorListener);

    boolean deviceList(String searchText, String searchType, Response.Listener<DeviceInfoListRsp> listener, Response.ErrorListener errorListener);

    boolean deviceAll(String sn, Response.Listener<DeviceInfoListRsp> listener, Response.ErrorListener errorListener);

    void eidList(Response.Listener<EidInfoListRsp> listener, Response.ErrorListener errorListener);

    void getStationInfo(Response.Listener<StationListRsp> listener, Response.ErrorListener errorListener, String sn);

    void updateDevices(String dataJson, Response.Listener<ResponseBase> listener, Response.ErrorListener errorListener);

    void updateDeviceUpgradeInfo(String dataJson, Response.Listener<ResponseBase> listener, Response.ErrorListener errorListener);

    void stopAllRequest();

    void secondAuth(String pinCode, Response.Listener<ResponseBase> listener, Response.ErrorListener errorListener);
}
