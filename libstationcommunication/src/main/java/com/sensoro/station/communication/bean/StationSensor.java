package com.sensoro.station.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tangrisheng on 2016/5/3.
 * Station sensor
 */

/*
"tpd": "25.9",//机壳内温度，精确到小数点后一位，单位"℃"
"tps": "23.4",//机壳温度，精确到小数点后一位，单位"℃"
"lt": "123",//机壳内光线强度，单位"lux"
"acc": "0",//基站跌落加速度，单位"m/s2"
"ang": "0"//基站倾斜角度，单位"°"
 */
public class StationSensor implements Parcelable {
    double tpd;
    double tps;
    double lt;
    double acc;
    double ang;

    protected StationSensor(Parcel in) {
        tpd = in.readDouble();
        tps = in.readDouble();
        lt = in.readDouble();
        acc = in.readDouble();
        ang = in.readDouble();
    }

    public static final Creator<StationSensor> CREATOR = new Creator<StationSensor>() {
        @Override
        public StationSensor createFromParcel(Parcel in) {
            return new StationSensor(in);
        }

        @Override
        public StationSensor[] newArray(int size) {
            return new StationSensor[size];
        }
    };

    public double getTpd() {
        return tpd;
    }

    public void setTpd(double tpd) {
        this.tpd = tpd;
    }

    public double getTps() {
        return tps;
    }

    public void setTps(double tps) {
        this.tps = tps;
    }

    public double getLt() {
        return lt;
    }

    public void setLt(double lt) {
        this.lt = lt;
    }

    public double getAcc() {
        return acc;
    }

    public void setAcc(double acc) {
        this.acc = acc;
    }

    public double getAng() {
        return ang;
    }

    public void setAng(double ang) {
        this.ang = ang;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(tpd);
        dest.writeDouble(tps);
        dest.writeDouble(lt);
        dest.writeDouble(acc);
        dest.writeDouble(ang);
    }
}
