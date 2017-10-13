package com.sensoro.station.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tangrisheng on 2016/5/3.
 * station network.
 */
/*
        "acm": "access mode",//基站网络接入方式，支持"eth"、"wifi"、"3g"三种接入方式
        "eth":{
            "ip": "192.168.0.199",//基站IP地址
            "mode": "dhcp",//基站IP地址分配方式，"dhcp"-DHCP分配，"static"-"静态指定"
            "gw": "192.168.0.1",//网关地址
            "nmask": "255.255.255.0",//子网掩码
            "pdns": "223.5.5.5",//主DNS服务器地址
            "adns": "223.6.6.6",//辅DNS服务器地址
            "traffic": "1000M",//以太网当日流量统计
        },
        "wifi":{
            "ssid":"SENSORO-OFFICE", //基站连接的无线局域网SSID
            "encrypt":"加密方式",
            "ip": "192.168.0.199",//基站IP地址
            "mode": "dhcp",//基站IP地址分配方式，"dhcp"-DHCP分配，"static"-"静态指定"
            "gw": "192.168.0.1",//网关地址
            "nmask": "255.255.255.0",//子网掩码
            "pdns": "223.5.5.5",//主DNS服务器地址
            "adns": "223.6.6.6",//辅DNS服务器地址
            "traffic": "888M",//无线wifi当日流量统计
        },
        "3g":{
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
        }
 */
public class StationNetwork implements Parcelable {
    String acm;
    String type;
    StationEthernet eth;
    StationWifi wifi;
    @SerializedName("3g")
    Station3G _3g;

    public StationNetwork() {
    }

    protected StationNetwork(Parcel in) {

        acm = in.readString();
        type = in.readString();
        eth = in.readParcelable(StationEthernet.class.getClassLoader());
        wifi = in.readParcelable(StationWifi.class.getClassLoader());
        _3g = in.readParcelable(Station3G.class.getClassLoader());
    }

    public static final Creator<StationNetwork> CREATOR = new Creator<StationNetwork>() {
        @Override
        public StationNetwork createFromParcel(Parcel in) {
            return new StationNetwork(in);
        }

        @Override
        public StationNetwork[] newArray(int size) {
            return new StationNetwork[size];
        }
    };

    public String getAcm() {
        return acm;
    }

    public void setAcm(String acm) {
        this.acm = acm;
    }

    public StationEthernet getEth() {
        return eth;
    }

    public void setEth(StationEthernet eth) {
        this.eth = eth;
    }

    public StationWifi getWifi() {
        return wifi;
    }

    public void setWifi(StationWifi wifi) {
        this.wifi = wifi;
    }

    public Station3G get_3g() {
        return _3g;
    }

    public void set_3g(Station3G _3g) {
        this._3g = _3g;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(acm);
        dest.writeString(type);
        dest.writeParcelable(eth, flags);
        dest.writeParcelable(wifi, flags);
        dest.writeParcelable(_3g, flags);
    }
}
