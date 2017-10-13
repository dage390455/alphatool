package com.sensoro.lora.setting.server.bean;

import com.sensoro.station.communication.bean.StationInfo;

import java.util.List;

/**
 * Created by tangrisheng on 2016/5/5.
 * station list
 */
public class StationListRsp extends ResponseBase {


    public List<StationInfo> getResult() {
        return result;
    }

    public void setResult(List<StationInfo> result) {
        this.result = result;
    }

    protected List<StationInfo> result;

    @Override
    public String toString() {
        return "StationListRsp{" +
                "result=" + result +
                '}';
    }
}
