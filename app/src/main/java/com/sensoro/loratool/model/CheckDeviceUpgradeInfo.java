package com.sensoro.loratool.model;

public class CheckDeviceUpgradeInfo {
    public String sn;
    public String firmVersion;
    //为了复用对象，添加该字段
    public boolean isNeedCheck = false;

    public CheckDeviceUpgradeInfo(String sn, String firmVersion, boolean isNeedCheck) {
        this.sn = sn;
        this.firmVersion = firmVersion;
        this.isNeedCheck = isNeedCheck;
    }

    public CheckDeviceUpgradeInfo() {
    }
}
