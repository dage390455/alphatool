package com.sensoro.station.communication;

import com.android.volley.Response;
import com.sensoro.station.communication.bean.NetworkBase;
import com.sensoro.station.communication.bean.ResponseBase;
import com.sensoro.station.communication.bean.StationInfo;
import com.sensoro.station.communication.bean.StationNetwork;

import java.util.List;

/**
 * Created by tangrisheng on 2016/4/22.
 * used for defining interface communicating with station.
 */
public interface IStation {

    void resetScope(String ip);
    /**
     * check net status.
     *
     * @param listener      result listener
     * @param errorListener errorListener
     * @param type          net type
     * @return status
     */
    void checkNetStatus(Response.Listener<ResponseBase> listener, Response.ErrorListener errorListener, String type);

    /**
     * get station info
     *
     * @param listener      result listener
     * @param errorListener errorListener
     * @param sn         info types
     */
    void getStationInfo(Response.Listener<StationInfo> listener, Response.ErrorListener errorListener, String sn);

    /**
     * set Stationl Net
     *
     * @param listener       result listener
     * @param errorListener  errorListener
     * @param stationNetwork network info to set
     */
    void setStationNet(Response.Listener<ResponseBase> listener, Response.ErrorListener errorListener, StationNetwork stationNetwork);

    /**
     * station Self Check
     *
     * @param listener      result listener
     * @param errorListener errorListener
     */
    void stationSelfCheck(Response.Listener<ResponseBase> listener, Response.ErrorListener errorListener);
}
