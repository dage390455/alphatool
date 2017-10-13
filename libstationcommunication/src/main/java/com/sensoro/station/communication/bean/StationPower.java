package com.sensoro.station.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tangrisheng on 2016/5/3.
 * Station power info.
 */
public class StationPower implements Parcelable {
    protected String pm;
    protected double pv;


    protected StationPower(Parcel in) {
        pm = in.readString();
        pv = in.readDouble();
    }

    public static final Creator<StationPower> CREATOR = new Creator<StationPower>() {
        @Override
        public StationPower createFromParcel(Parcel in) {
            return new StationPower(in);
        }

        @Override
        public StationPower[] newArray(int size) {
            return new StationPower[size];
        }
    };

    public String getPm() {
        return pm;
    }

    public void setPm(String pm) {
        this.pm = pm;
    }

    public double getPv() {
        return pv;
    }

    public void setPv(double pv) {
        this.pv = pv;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pm);
        dest.writeDouble(pv);
    }
}
