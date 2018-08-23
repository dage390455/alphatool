package com.sensoro.lora.setting.server;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.Gson;
import com.sensoro.lora.setting.server.bean.DeviceInfoListRsp;
import com.sensoro.lora.setting.server.bean.EidInfoListRsp;
import com.sensoro.lora.setting.server.bean.LoginReq;
import com.sensoro.lora.setting.server.bean.LoginRsp;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.lora.setting.server.bean.StationListRsp;
import com.sensoro.lora.setting.server.bean.UpgradeListRsp;
import com.sensoro.volleymanager.VolleyManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangrisheng on 2016/5/5.
 * LoRa Setting server interface implement.
 */
public class LoRaSettingServerImpl implements ILoRaSettingServer {
    public static final String SCOPE_MOCHA = "http://iot-mocha-api.sensoro.com";
    public static final String SCOPE_TEST = "http://iot-test-api.sensoro.com";
    public static final String SCOPE_IOT = "https://iot-api.sensoro.com";
    public static String SCOPE = SCOPE_MOCHA;//http://mocha-iot-api.sensoro.com-----http://iot-api.sensoro.com
    public static final String LOGIN = "/signin";
    public static final String STATION_LIST = "/manage/operator/get_station_info/v1";
    public static final String DEVICE_LIST = "/device/data";
    public static final String DEVICE_ALL = "/device/all";
    public static final String EID_LIST = "/eid/data";
    public static final String UPDATE = "/device/update";
    public static final String UPDATE_LIST = "/upgrade/device/data";
    public static final String UPDATE_DEVICE_UPGRADE_INFO = "/device/base/config";
    public static final String TWO_AUTHENTICATION = "/twofactor/authentication/verify";
    public static final String HEADER_SESSION_ID = "x-session-id";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String TAG = "Lora";

    Context context;

    protected String sessionId;
    private static LoRaSettingServerImpl singleton;
    Gson gson;

    VolleyManager volleyManager;

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    private LoRaSettingServerImpl(Context context) {
        this.context = context;
        gson = new Gson();
        volleyManager = VolleyManager.getInstance(context);
    }

    public static LoRaSettingServerImpl getInstance(Context context) {
        if (context == null) {
            return null;
        }
        if (singleton == null) {
            synchronized (LoRaSettingServerImpl.class) {
                if (singleton == null) {
                    singleton = new LoRaSettingServerImpl(context);
                }
            }
        }

        return singleton;
    }

    public void stopAllRequest() {
        if (volleyManager != null) {
            volleyManager.cancel(TAG);
        }
    }

    @Override
    public boolean login(String id, String pwd, final Response.Listener<LoginRsp> listener, Response.ErrorListener
            errorListener) {

        if (id == null || pwd == null) {
            return false;
        }
        LoginReq loginReq = new LoginReq(id, pwd);
        String body = gson.toJson(loginReq);
        Map<String, String> headers = new HashMap<>();

        // add sessionId for authorization.
        headers.put(HEADER_USER_AGENT, "android");
        Response.Listener<LoginRsp> interceptListener = new Response.Listener<LoginRsp>() {
            @Override
            public void onResponse(LoginRsp response) {
                // set sessionid;
                sessionId = response.getSessionId();
//                String expires = response.getExpires();
//                String s = response.toString();
                listener.onResponse(response);
            }
        };
        volleyManager.gsonRequest(TAG, Request.Method.POST, headers, body, SCOPE + LOGIN, LoginRsp.class,
                interceptListener,
                errorListener);

        return true;
    }

    @Override
    public boolean stationList(Response.Listener<StationListRsp> listener, Response.ErrorListener errorListener) {

        Map<String, String> headers = new HashMap<>();

        if (sessionId != null) {
            // add sessionId for authorization.
            headers.put(HEADER_SESSION_ID, sessionId);
        }
        volleyManager.gsonRequest(TAG, Request.Method.GET, headers, (String) null, SCOPE + STATION_LIST,
                StationListRsp.class, listener, errorListener);
        return false;
    }

    @Override
    public boolean deviceList(int page, Response.Listener<DeviceInfoListRsp> listener, Response.ErrorListener
            errorListener) {

        Map<String, String> headers = new HashMap<>();

        if (sessionId != null) {
            // add sessionId for authorization.
            headers.put(HEADER_SESSION_ID, sessionId);
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("p", String.valueOf(page));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        volleyManager.gsonRequest(TAG, Request.Method.POST, headers, jsonObject.toString(), SCOPE + DEVICE_LIST,
                DeviceInfoListRsp.class, listener, errorListener);
//        volleyManager.gsonRequest(null, Request.Method.POST, headers, (String) null, SCOPE + DEVICE_LIST,
// DeviceInfoListRsp.class, listener, errorListener);
        return false;
    }

    @Override
    public boolean deviceList(String searchText, String searchType, Response.Listener<DeviceInfoListRsp> listener,
                              Response.ErrorListener errorListener) {
        Map<String, String> headers = new HashMap<>();

        if (sessionId != null) {
            // add sessionId for authorization.
            headers.put(HEADER_SESSION_ID, sessionId);
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("p", 1);
            jsonObject.put("count", 1000);
            jsonObject.put("searchText", searchText);
            jsonObject.put("searchType", searchType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        volleyManager.gsonRequest(TAG, Request.Method.POST, headers, jsonObject.toString(), SCOPE + DEVICE_LIST,
                DeviceInfoListRsp.class, listener, errorListener);

        return false;
    }

    @Override
    public boolean deviceAll(String sn, Response.Listener<DeviceInfoListRsp> listener, Response.ErrorListener
            errorListener) {
        Map<String, String> headers = new HashMap<>();

        if (sessionId != null) {
            // add sessionId for authorization.
            headers.put(HEADER_SESSION_ID, sessionId);
        }

        String url = SCOPE + DEVICE_ALL + "/" + sn;
        volleyManager.gsonRequest(TAG, Request.Method.GET, headers, (String) null, url, DeviceInfoListRsp.class,
                listener, errorListener);

        return false;
    }

    @Override
    public void eidList(Response.Listener<EidInfoListRsp> listener, Response.ErrorListener errorListener) {
        Map<String, String> headers = new HashMap<>();

        if (sessionId != null) {
            // add sessionId for authorization.
            headers.put(HEADER_SESSION_ID, sessionId);
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("p", 1);
            jsonObject.put("count", 1000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        volleyManager.gsonRequest(TAG, Request.Method.POST, headers, jsonObject.toString(), SCOPE + EID_LIST,
                EidInfoListRsp.class, listener, errorListener);

    }

    @Override
    public void getStationInfo(Response.Listener<StationListRsp> listener, Response.ErrorListener errorListener,
                               String sn) {
        String url = SCOPE + STATION_LIST + "?sn=" + sn;
        Map<String, String> headers = new HashMap<>();

        if (sessionId != null) {
            // add sessionId for authorization.
            headers.put(HEADER_SESSION_ID, sessionId);
        }
//        Map<String, String> params = new HashMap<>();
//        params.put("sn", sn);
        volleyManager.gsonRequest(TAG, Request.Method.GET, headers, (String) null, url, StationListRsp.class,
                listener, errorListener);

    }

    @Override
    public void updateDevices(String dataJson, Response.Listener<ResponseBase> listener, Response.ErrorListener
            errorListener) {
        String url = SCOPE + UPDATE;
        Map<String, String> headers = new HashMap<>();

        if (sessionId != null) {
            // add sessionId for authorization.
            headers.put(HEADER_SESSION_ID, sessionId);
        }
        volleyManager.gsonRequest(TAG, Request.Method.POST, headers, dataJson, url, ResponseBase.class, listener,
                errorListener);
    }

    @Override
    public boolean deviceUpgradeList(int page, String deviceType, String band, String hv, String fv, Response
            .Listener<UpgradeListRsp> listener, Response.ErrorListener errorListener) {
        Map<String, String> headers = new HashMap<>();

        if (sessionId != null) {
            // add sessionId for authorization.
            headers.put(HEADER_SESSION_ID, sessionId);
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("p", String.valueOf(page));
            jsonObject.put("deviceType", deviceType);
            jsonObject.put("band", band);
            jsonObject.put("hv", hv);
            jsonObject.put("fv", fv);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        volleyManager.gsonRequest(TAG, Request.Method.POST, headers, jsonObject.toString(), SCOPE + UPDATE_LIST,
                UpgradeListRsp.class, listener, errorListener);
        return false;
    }

    @Override
    public void updateDeviceUpgradeInfo(String dataJson, Response.Listener<ResponseBase> listener, Response
            .ErrorListener errorListener) {
        String url = SCOPE + UPDATE_DEVICE_UPGRADE_INFO;
        Map<String, String> headers = new HashMap<>();

        if (sessionId != null) {
            // add sessionId for authorization.
            headers.put(HEADER_SESSION_ID, sessionId);
        }
        volleyManager.gsonRequest(TAG, Request.Method.PUT, headers, dataJson, url, ResponseBase.class, listener,
                errorListener);
    }

    @Override
    public void secondAuth(String pinCode, Response.Listener<ResponseBase> listener, Response.ErrorListener
            errorListener) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("pincode", pinCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Map<String, String> headers = new HashMap<>();

        if (sessionId != null) {
            // add sessionId for authorization.
            headers.put(HEADER_SESSION_ID, sessionId);
        }
        volleyManager.gsonRequest(TAG, Request.Method.POST, headers, jsonObject.toString(), SCOPE +
                TWO_AUTHENTICATION, ResponseBase.class, listener, errorListener);

    }
}
