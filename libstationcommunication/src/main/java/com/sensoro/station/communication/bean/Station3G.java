package com.sensoro.station.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tangrisheng on 2016/5/3.
 * Station 3G
 */
/*
            "ip": "192.168.0.199",//基站IP地址
            "gw": "192.168.0.1",//网关地址
            "nmask": "255.255.255.0",//子网掩码
            "pdns": "223.5.5.5",//主DNS服务器地址
            "adns": "223.6.6.6",//辅DNS服务器地址
            "traffic": "888M",//3G当日流量统计
            "rssi":"-90dBm",//3G信号强度
            "imei":"492048762883",//3G卡IMEI
            "imsi":"1213221323",//3G卡IMSI码
            "sim":"13789202201"//3G SIM卡卡号
 */
public class Station3G extends NetworkBase implements Parcelable {
    protected double rssi;
    protected String imei;
    protected String imsi;
    protected String sim;

    public double getRssi() {
        return rssi;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getSim() {
        return sim;
    }

    public void setSim(String sim) {
        this.sim = sim;
    }


    public Station3G(Parcel in) {
        super(in);
        rssi = in.readDouble();
        imei = in.readString();
        imsi = in.readString();
        sim = in.readString();
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(rssi);
        dest.writeString(imei);
        dest.writeString(imsi);
        dest.writeString(sim);
    }

    public static final Creator<Station3G> CREATOR = new Creator<Station3G>() {
        @Override
        public Station3G createFromParcel(Parcel in) {
            return new Station3G(in);
        }

        @Override
        public Station3G[] newArray(int size) {
            return new Station3G[size];
        }
    };
}
