package com.sensoro.station.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by tangrisheng on 2016/5/3.
 * Station Info Rsp
 */
public class StationInfo extends ResponseBase implements Parcelable, Comparable {
    public static final String TYPE_STATION = "station";
    public static final String TYPE_GATEWAY = "gateway";
    protected String station_ssid;
    protected String station_pwd;
    protected String station_sec_type;
    protected String type;
    protected String name;
    protected List<String> tags;
    protected StationPower pwr;
    protected StationSensor sd;
    protected StationNetwork nwk;
    protected StationSystem sys;
    protected StationDram dram;
    protected StationNand nand;
    protected int sort;
    protected int rssi;

    protected StationInfo(Parcel in) {
        super(in);
        station_ssid = in.readString();
        station_pwd = in.readString();
        station_sec_type = in.readString();
        name = in.readString();
        tags = in.createStringArrayList();
        pwr = in.readParcelable(StationPower.class.getClassLoader());
        sd = in.readParcelable(StationSensor.class.getClassLoader());
        nwk = in.readParcelable(StationNetwork.class.getClassLoader());
        sys = in.readParcelable(StationSystem.class.getClassLoader());
        dram = in.readParcelable(StationDram.class.getClassLoader());
        nand = in.readParcelable(StationNand.class.getClassLoader());
        sort = in.readInt();
        rssi = in.readInt();
        type = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(station_ssid);
        dest.writeString(station_pwd);
        dest.writeString(station_sec_type);
        dest.writeString(name);
        dest.writeStringList(tags);
        dest.writeParcelable(pwr, flags);
        dest.writeParcelable(sd, flags);
        dest.writeParcelable(nwk, flags);
        dest.writeParcelable(sys, flags);
        dest.writeParcelable(dram, flags);
        dest.writeParcelable(nand, flags);
        dest.writeInt(sort);
        dest.writeInt(rssi);
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StationInfo> CREATOR = new Creator<StationInfo>() {
        @Override
        public StationInfo createFromParcel(Parcel in) {
            return new StationInfo(in);
        }

        @Override
        public StationInfo[] newArray(int size) {
            return new StationInfo[size];
        }
    };

    public String getStation_ssid() {
        return station_ssid;
    }

    public void setStation_ssid(String station_ssid) {
        this.station_ssid = station_ssid;
    }

    public String getStation_pwd() {
        return station_pwd;
    }

    public void setStation_pwd(String station_pwd) {
        this.station_pwd = station_pwd;
    }

    public StationPower getPwr() {
        return pwr;
    }

    public void setPwr(StationPower pwr) {
        this.pwr = pwr;
    }

    public StationSensor getSd() {
        return sd;
    }

    public void setSd(StationSensor sd) {
        this.sd = sd;
    }

    public StationNetwork getNwk() {
        return nwk;
    }

    public void setNwk(StationNetwork nwk) {
        this.nwk = nwk;
    }

    public StationSystem getSys() {
        return sys;
    }

    public void setSys(StationSystem sys) {
        this.sys = sys;
    }

    public StationDram getDram() {
        return dram;
    }

    public void setDram(StationDram dram) {
        this.dram = dram;
    }

    public StationNand getNand() {
        return nand;
    }

    public void setNand(StationNand nand) {
        this.nand = nand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getStation_sec_type() {
        return station_sec_type;
    }

    public void setStation_sec_type(String station_sec_type) {
        this.station_sec_type = station_sec_type;
    }

    public String getDeviceType() {
        return type;
    }

    public void setDeviceType(String deviceType) {
        this.type = deviceType;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "StationInfo{" +
                "station_ssid='" + station_ssid + '\'' +
                ", station_pwd='" + station_pwd + '\'' +
                ", station_sec_type='" + station_sec_type + '\'' +
                ", name='" + name + '\'' +
                ", tags=" + tags +
                ", pwr=" + pwr +
                ", sd=" + sd +
                ", nwk=" + nwk +
                ", sys=" + sys +
                ", dram=" + dram +
                ", nand=" + nand +
                '}';
    }

    @Override
    public int compareTo(Object another) {
        StationInfo anotherStationInfo = (StationInfo) another;
        if (this.sort > anotherStationInfo.sort) {
            return -1;
        }else if (this.sort == anotherStationInfo.sort) {
            return 0;
        }
        else {
            return 1;
        }
    }
}
