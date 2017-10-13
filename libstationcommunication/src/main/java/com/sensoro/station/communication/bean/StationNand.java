package com.sensoro.station.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tangrisheng on 2016/5/3.
 * Station nand
 */
/*
        "total":"445",//总FLASH存储大小，以"MB"为单位，如下依次内推
        "used": "202",//已使用FLASH大小
        "available": "226"//剩余FLASH大小
 */
public class StationNand implements Parcelable {
    protected double total;
    protected double used;
    protected double available;

    protected StationNand(Parcel in) {
        total = in.readDouble();
        used = in.readDouble();
        available = in.readDouble();
    }

    public static final Creator<StationNand> CREATOR = new Creator<StationNand>() {
        @Override
        public StationNand createFromParcel(Parcel in) {
            return new StationNand(in);
        }

        @Override
        public StationNand[] newArray(int size) {
            return new StationNand[size];
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

    public double getAvailable() {
        return available;
    }

    public void setAvailable(double available) {
        this.available = available;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(total);
        dest.writeDouble(used);
        dest.writeDouble(available);
    }
}

