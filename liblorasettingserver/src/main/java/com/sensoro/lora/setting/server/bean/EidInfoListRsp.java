package com.sensoro.lora.setting.server.bean;

import java.util.List;

/**
 * Created by tangrisheng on 2016/5/5.
 * station list
 */
public class EidInfoListRsp extends ResponseBase {


    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    protected Data data;

    @Override
    public String toString() {
        return "EidInfoListRsp{" +
                "data=" + data +
                '}';
    }

    public class Data {

        public List<EidInfo> getItems() {
            return items;
        }

        public void setItems(List<EidInfo> items) {
            this.items = items;
        }

        protected List<EidInfo> items;

    }
}
