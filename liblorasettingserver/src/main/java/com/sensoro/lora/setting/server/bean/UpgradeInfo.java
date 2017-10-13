package com.sensoro.lora.setting.server.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sensoro on 16/9/12.
 */

public class UpgradeInfo implements Parcelable {

    private String version;
    private String fromVersion;
    private String fromHVersion;
    private String url;
    private String extraUrl;
    private String type;
    private boolean isDeleted;
    private long createdTime;
    private long updatedTime;

    public UpgradeInfo(Parcel in) {

        version = in.readString();
        fromVersion = in.readString();
        fromHVersion = in.readString();
        url = in.readString();
        extraUrl = in.readString();
        type = in.readString();
        isDeleted = in.readByte() == 1 ? true : false;
        createdTime = in.readLong();
        updatedTime = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(version);
        dest.writeString(fromVersion);
        dest.writeString(fromHVersion);
        dest.writeString(url);
        dest.writeString(extraUrl);
        dest.writeString(type);
        dest.writeByte((byte) (isDeleted ? 1 : 0));
        dest.writeLong(createdTime);
        dest.writeLong(updatedTime);
    }

    public static final Creator<UpgradeInfo> CREATOR = new Creator<UpgradeInfo>() {
        @Override
        public UpgradeInfo createFromParcel(Parcel in) {
            return new UpgradeInfo(in);
        }

        @Override
        public UpgradeInfo[] newArray(int size) {
            return new UpgradeInfo[size];
        }
    };

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFromVersion() {
        return fromVersion;
    }

    public void setFromVersion(String fromVersion) {
        this.fromVersion = fromVersion;
    }

    public String getFromHVersion() {
        return fromHVersion;
    }

    public void setFromHVersion(String fromHVersion) {
        this.fromHVersion = fromHVersion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getExtraUrl() {
        return extraUrl;
    }

    public void setExtraUrl(String extraUrl) {
        this.extraUrl = extraUrl;
    }
}
