package com.sensoro.loratool.model;

/**
 * Created by sensoro on 16/9/5.
 */

public class UpgradeData {

    private int id;
    private String firmwareVersion;
    private String data;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String version) {
        this.firmwareVersion = version;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
