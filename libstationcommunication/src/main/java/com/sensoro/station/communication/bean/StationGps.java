package com.sensoro.station.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tangrisheng on 2016/5/3.
 */
public class StationGps implements Parcelable{

    protected double lat;
    protected double lon;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public StationGps (Parcel in) {
        lat = in.readDouble();
        lon = in.readDouble();
    }
    public static final Creator<StationGps> CREATOR = new Creator<StationGps>() {
        @Override
        public StationGps createFromParcel(Parcel in) {
            return new StationGps(in);
        }

        @Override
        public StationGps[] newArray(int size) {
            return new StationGps[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeDouble(lat);
        dest.writeDouble(lon);
    }
}
