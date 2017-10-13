package com.sensoro.station.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tangrisheng on 2016/5/3.
 * Station Dram
 */
/*
        "total": "13",//总内存大小，以"MB"为单位，如下依次内推
        "used": "11",//已使用内存大小
        "free": "486",//空闲内存大小
        "shared": "0",//共享内存大小
        "buffered": "0"//磁盘缓存大小
 */
public class StationDram implements Parcelable {
    protected double total;
    protected double used;
    protected double free;
    protected double shared;
    protected double buffered;

    protected StationDram(Parcel in) {
        total = in.readDouble();
        used = in.readDouble();
        free = in.readDouble();
        shared = in.readDouble();
        buffered = in.readDouble();
    }

    public static final Creator<StationDram> CREATOR = new Creator<StationDram>() {
        @Override
        public StationDram createFromParcel(Parcel in) {
            return new StationDram(in);
        }

        @Override
        public StationDram[] newArray(int size) {
            return new StationDram[size];
        }
    };

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getUsed() {
        return used;
    }

    public void setUsed(double used) {
        this.used = used;
    }

    public double getFree() {
        return free;
    }

    public void setFree(double free) {
        this.free = free;
    }

    public double getShared() {
        return shared;
    }

    public void setShared(double shared) {
        this.shared = shared;
    }

    public double getBuffered() {
        return buffered;
    }

    public void setBuffered(double buffered) {
        this.buffered = buffered;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(total);
        dest.writeDouble(used);
        dest.writeDouble(free);
        dest.writeDouble(shared);
        dest.writeDouble(buffered);
    }
}
