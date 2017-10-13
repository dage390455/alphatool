package com.sensoro.station.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tangrisheng on 2016/5/3.
 * Station Wifi
 */
public class StationWifi extends NetworkBase implements Parcelable {
    protected String ssid;
    protected String pwd;
    protected String encrypt;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }

    public StationWifi() {

    }

    public StationWifi(Parcel in) {
        super(in);
        ssid = in.readString();
        pwd = in.readString();
        encrypt = in.readString();
    }

    public static final Creator<StationWifi> CREATOR = new Creator<StationWifi>() {
        @Override
        public StationWifi createFromParcel(Parcel in) {
            return new StationWifi(in);
        }

        @Override
        public StationWifi[] newArray(int size) {
            return new StationWifi[size];
        }
    };

    @Override
    public int describeContents() {

        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(ssid);
        dest.writeString(pwd);
        dest.writeString(encrypt);
    }
}
