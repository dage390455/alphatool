package com.sensoro.station.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tangrisheng on 2016/5/3.
 * NetworkBase
 */
/*
            "ip": "192.168.0.199",//基站IP地址
            "mode": "dhcp",//基站IP地址分配方式，"dhcp"-DHCP分配，"static"-"静态指定"
            "gw": "192.168.0.1",//网关地址
            "nmask": "255.255.255.0",//子网掩码
            "pdns": "223.5.5.5",//主DNS服务器地址
            "adns": "223.6.6.6",//辅DNS服务器地址
            "traffic": "1000M",//当日流量统计
 */
public class NetworkBase implements Parcelable{
    protected String ip;
    protected String mode;
    protected String gw;
    protected String nmask;
    protected String pdns;
    protected String adns;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getGw() {
        return gw;
    }

    public void setGw(String gw) {
        this.gw = gw;
    }

    public String getNmask() {
        return nmask;
    }

    public void setNmask(String nmask) {
        this.nmask = nmask;
    }

    public String getPdns() {
        return pdns;
    }

    public void setPdns(String pdns) {
        this.pdns = pdns;
    }

    public String getAdns() {
        return adns;
    }

    public void setAdns(String adns) {
        this.adns = adns;
    }

    public NetworkBase() {

    }

    public NetworkBase(Parcel in) {
        ip = in.readString();
        mode = in.readString();
        gw = in.readString();
        nmask = in.readString();
        pdns = in.readString();
        adns = in.readString();
    }

    public static final Creator<NetworkBase> CREATOR = new Creator<NetworkBase>() {
        @Override
        public NetworkBase createFromParcel(Parcel in) {
            return new NetworkBase(in);
        }

        @Override
        public NetworkBase[] newArray(int size) {
            return new NetworkBase[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(ip);
        dest.writeString(mode);
        dest.writeString(gw);
        dest.writeString(nmask);
        dest.writeString(pdns);
        dest.writeString(adns);
    }
}
