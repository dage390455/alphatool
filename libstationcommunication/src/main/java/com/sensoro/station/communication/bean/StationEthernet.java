package com.sensoro.station.communication.bean;

import android.os.Parcel;

/**
 * Created by tangrisheng on 2016/5/3.
 * Station Ethernet
 */
public class StationEthernet extends NetworkBase {

    public StationEthernet() {

    }

    public StationEthernet(Parcel in) {
       super(in);
    }

    public static final Creator<StationEthernet> CREATOR = new Creator<StationEthernet>() {
        @Override
        public StationEthernet createFromParcel(Parcel in) {
            return new StationEthernet(in);
        }

        @Override
        public StationEthernet[] newArray(int size) {
            return new StationEthernet[size];
        }
    };
}
