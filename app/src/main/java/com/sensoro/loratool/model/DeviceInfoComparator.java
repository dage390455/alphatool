package com.sensoro.loratool.model;

import com.sensoro.lora.setting.server.bean.DeviceInfo;

import java.util.Comparator;

/**
 * Created by sensoro on 17/2/14.
 */

public class DeviceInfoComparator implements Comparator<DeviceInfo> {


    @Override
    public int compare(DeviceInfo lhs, DeviceInfo rhs) {
        if (lhs.getSort() < rhs.getSort()) {
            return 1;
        } else if (lhs.getSort() == rhs.getSort()) {
            return 0;
        } else {
            return -1;
        }
    }
}
