package com.sensoro.station.communication;

import android.content.Context;

import com.android.volley.Response;
import com.google.gson.Gson;
import com.sensoro.station.communication.bean.CheckNetStatusReq;
import com.sensoro.station.communication.bean.ResponseBase;
import com.sensoro.station.communication.bean.StationInfo;
import com.sensoro.station.communication.bean.StationNetwork;
import com.sensoro.volleymanager.GsonRequest;
import com.sensoro.volleymanager.VolleyManager;

import java.util.List;

/**
 * Created by tangrisheng on 2016/4/22.
 * implement Station Interface.
 */
public class StationImpl implements IStation {

    public static final int ERRCODE_SUC = 0;


    public static final String NAMESPACE_DEFAULT = "/cgi-bin/iot-bst";
    public static final int PORT_DEFAULT = 8018;

    public static final String CHECK_NET_STATIS = "/check_net_status";
    public static final String GET_STATION_INFO = "/get_station_info";
    public static final String SET_STATION_NET = "/set_station_net";
    public static final String STATION_CHECK_SELF = "/station_check_self";
    public static final String TAG = "Lora";

    VolleyManager volleyManager;
    Gson gson = new Gson();
    Context context;
    String ip = "http://192.168.123.1:8018";
    int port = 8018;

    String scope;

    private static StationImpl singleton;

    private StationImpl(Context context, String ip, int port, String namesapce) {
        if (context == null) {
            return;
        }
        this.context = context;
        if (ip.startsWith("http://") || ip.startsWith("https://")) {
            this.ip = ip;
        } else {
            this.ip = "http://" + ip;
        }
        this.scope = this.ip + ":" + port + namesapce;
        volleyManager = VolleyManager.getInstance(context);
    }

    public static StationImpl getInstance(Context context, String ip, int port, String namespace) {
        if (context == null) {
            return null;
        }
        if (singleton == null) {
            synchronized (StationImpl.class) {
                if (singleton == null) {
                    singleton = new StationImpl(context, ip, port, namespace);
                }
            }
        }

        return singleton;
    }

    public void resetScope(String ip) {
        if (ip.startsWith("http://") || ip.startsWith("https://")) {
            this.ip = ip;
        } else {
            this.ip = "http://" + ip;
        }
        this.scope = this.ip + ":" + port + NAMESPACE_DEFAULT;
    }

    public static StationImpl getInstance(Context context, String ip) {

        return getInstance(context, ip, PORT_DEFAULT, NAMESPACE_DEFAULT);
    }

    @Override
    public void checkNetStatus(Response.Listener<ResponseBase> listener, Response.ErrorListener errorListener, String type) {

        CheckNetStatusReq checkNetStatusReq = new CheckNetStatusReq();
        checkNetStatusReq.setType(type);
        String json = gson.toJson(checkNetStatusReq);
        volleyManager.gsonRequest(TAG, GsonRequest.Method.POST, json, scope + CHECK_NET_STATIS, ResponseBase.class, listener, errorListener);
//        volleyManager.gsonRequest(null, GsonRequest.Method.GET, json, "http://www.weather.com.cn/data/sk/101010100.html", ResponseBase.class, listener, errorListener);
    }

    @Override
    public void getStationInfo(Response.Listener<StationInfo> listener, Response.ErrorListener errorListener, String sn) {
        String url = ip + GET_STATION_INFO + "?sn="+sn;
        volleyManager.gsonGetRequest(TAG, url, StationInfo.class, listener, errorListener);
    }

    @Override
    public void setStationNet(Response.Listener<ResponseBase> listener, Response.ErrorListener errorListener, StationNetwork stationNetwork) {

        String json = gson.toJson(stationNetwork);
        volleyManager.gsonRequest(TAG, GsonRequest.Method.POST, json, scope + SET_STATION_NET, ResponseBase.class, listener, errorListener);
    }

    @Override
    public void stationSelfCheck(Response.Listener<ResponseBase> listener, Response.ErrorListener errorListener) {
        volleyManager.gsonRequest(TAG, GsonRequest.Method.POST, "", scope + STATION_CHECK_SELF, ResponseBase.class, listener, errorListener);
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
