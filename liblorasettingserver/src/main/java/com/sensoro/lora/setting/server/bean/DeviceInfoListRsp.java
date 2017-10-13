package com.sensoro.lora.setting.server.bean;

import java.util.List;

/**
 * Created by tangrisheng on 2016/5/5.
 * station list
 */
public class DeviceInfoListRsp extends ResponseBase {


    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    protected Data data;

    @Override
    public String toString() {
        return "DeviceInfoListRsp{" +
                "data=" + data +
                '}';
    }

    public class Data {

        public List<DeviceInfo> getItems() {
            return items;
        }

        public void setItems(List<DeviceInfo> items) {
            this.items = items;
        }

        protected List<DeviceInfo> items;

        public PageInfo getPage_info() {
          return page_info;
        }

        public void setPage_info(PageInfo page_info) {
            this.page_info = page_info;
        }

        protected PageInfo page_info;

    }
}
