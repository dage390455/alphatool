package com.sensoro.lora.setting.server.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.sensoro.station.communication.bean.StationInfo;

/**
 * Created by fangping on 2016/7/21.
 */

public class LocationInfo implements Parcelable {

    private String country;
    private String province;
    private String state;
    private String district;
    private String address;


    public LocationInfo(Parcel in) {
        country = in.readString();
        province = in.readString();
        state = in.readString();
        district = in.readString();
        address = in.readString();
    }

    public static final Creator<LocationInfo> CREATOR = new Creator<LocationInfo>() {
        @Override
        public LocationInfo createFromParcel(Parcel in) {
            return new LocationInfo(in);
        }

        @Override
        public LocationInfo[] newArray(int size) {
            return new LocationInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(country);
        dest.writeString(province);
        dest.writeString(state);
        dest.writeString(district);
        dest.writeString(address);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
