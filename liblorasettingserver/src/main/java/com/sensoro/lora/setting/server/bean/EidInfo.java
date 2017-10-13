package com.sensoro.lora.setting.server.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sensoro on 16/8/25.
 */

public class EidInfo implements Parcelable {

    private String id;
    private String advertisedId;
    private int initialClockValue;
    private int rotationPeriodExponent;
    private String beaconIdentityKey;
    private String status;
    private long createdTime;
    private int currentClock;
    private String currentEID;

    public EidInfo() {

    }

    public EidInfo(Parcel in) {
        id = in.readString();
        advertisedId = in.readString();
        initialClockValue = in.readInt();
        rotationPeriodExponent = in.readInt();
        beaconIdentityKey = in.readString();
        status = in.readString();
        createdTime = in.readLong();
        currentClock = in.readInt();
        currentEID = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(advertisedId);
        dest.writeInt(initialClockValue);
        dest.writeInt(rotationPeriodExponent);
        dest.writeString(beaconIdentityKey);
        dest.writeString(status);
        dest.writeLong(createdTime);
        dest.writeInt(currentClock);
        dest.writeString(currentEID);
    }

    public static final Creator<EidInfo> CREATOR = new Creator<EidInfo>() {
        @Override
        public EidInfo createFromParcel(Parcel in) {
            return new EidInfo(in);
        }

        @Override
        public EidInfo[] newArray(int size) {
            return new EidInfo[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdvertisedId() {
        return advertisedId;
    }

    public void setAdvertisedId(String advertisedId) {
        this.advertisedId = advertisedId;
    }

    public int getInitialClockValue() {
        return initialClockValue;
    }

    public void setInitialClockValue(int initialClockValue) {
        this.initialClockValue = initialClockValue;
    }

    public int getRotationPeriodExponent() {
        return rotationPeriodExponent;
    }

    public void setRotationPeriodExponent(int rotationPeriodExponent) {
        this.rotationPeriodExponent = rotationPeriodExponent;
    }

    public String getBeaconIdentityKey() {
        return beaconIdentityKey;
    }

    public void setBeaconIdentityKey(String beaconIdentityKey) {
        this.beaconIdentityKey = beaconIdentityKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public int getCurrentClock() {
        return currentClock;
    }

    public void setCurrentClock(int currentClock) {
        this.currentClock = currentClock;
    }

    public String getCurrentEID() {
        return currentEID;
    }

    public void setCurrentEID(String currentEID) {
        this.currentEID = currentEID;
    }
}
