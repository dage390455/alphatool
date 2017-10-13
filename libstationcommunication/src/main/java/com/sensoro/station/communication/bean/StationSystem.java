package com.sensoro.station.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tangrisheng on 2016/5/3.
 */
/*
        "sn":"02023920392039",//基站串号
        "hw_ver":"10A-HW-100",
        "sw_ver":"10A-SW-100-220",
        "starttime": "2008-11-09 11:50:31",//基站启动时间
        "inode":{//系统inode节点信息
            "total":893289,
            "free"：772637
        }，
        "cpu_util": {//实时CPU利用率
            "usr": "0.4%",//用户空间CPU利用率
            "sys": "0.6%",//系统CPU利用率
            "idl": "99%"//空闲CPU利用率
        },
        "gps":{
            "lat":"",
            "lon":"",
        }
 */
public class StationSystem implements Parcelable {
    protected String sn;
    protected String hw_ver;
    protected String sw_ver;
    protected String starttime;
    protected String prevUpTime;
    protected int normalStatus;
    protected INode inode;
    protected StationCpu cpu_util;
    protected StationGps gps;

    protected StationSystem(Parcel in) {
        sn = in.readString();
        hw_ver = in.readString();
        sw_ver = in.readString();
        starttime = in.readString();
        prevUpTime = in.readString();
        normalStatus = in.readInt();
        gps = in.readParcelable(StationGps.class.getClassLoader());
    }

    public static final Creator<StationSystem> CREATOR = new Creator<StationSystem>() {
        @Override
        public StationSystem createFromParcel(Parcel in) {
            return new StationSystem(in);
        }

        @Override
        public StationSystem[] newArray(int size) {
            return new StationSystem[size];
        }
    };

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getHw_ver() {
        return hw_ver;
    }

    public void setHw_ver(String hw_ver) {
        this.hw_ver = hw_ver;
    }

    public String getSw_ver() {
        return sw_ver;
    }

    public void setSw_ver(String sw_ver) {
        this.sw_ver = sw_ver;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public INode getInode() {
        return inode;
    }

    public void setInode(INode inode) {
        this.inode = inode;
    }

    public StationCpu getCpu_util() {
        return cpu_util;
    }

    public void setCpu_util(StationCpu cpu_util) {
        this.cpu_util = cpu_util;
    }

    public StationGps getGps() {
        return gps;
    }

    public void setGps(StationGps gps) {
        this.gps = gps;
    }

    public int getNormalStatus() {
        return normalStatus;
    }

    public void setNormalStatus(int normalStatus) {
        this.normalStatus = normalStatus;
    }

    public String getPrevUpTime() {
        return prevUpTime;
    }

    public void setPrevUpTime(String prevUpTime) {
        this.prevUpTime = prevUpTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sn);
        dest.writeString(hw_ver);
        dest.writeString(sw_ver);
        dest.writeString(starttime);
        dest.writeString(prevUpTime);
        dest.writeInt(normalStatus);
        dest.writeParcelable(gps, flags);
    }
}
