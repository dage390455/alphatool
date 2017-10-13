package com.sensoro.station.communication.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tangrisheng on 2016/5/3.
 * used for station interface response.
 */
public class ResponseBase implements Parcelable {
    protected int errcode;
    protected String errmsg;
    protected String ctime;

    protected ResponseBase(Parcel in) {
        errcode = in.readInt();
        errmsg = in.readString();
        ctime = in.readString();
    }

    public static final Creator<ResponseBase> CREATOR = new Creator<ResponseBase>() {
        @Override
        public ResponseBase createFromParcel(Parcel in) {
            return new ResponseBase(in);
        }

        @Override
        public ResponseBase[] newArray(int size) {
            return new ResponseBase[size];
        }
    };

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    @Override
    public String toString() {
        return "ResponseBase{" +
                "errcode=" + errcode +
                ", errmsg='" + errmsg + '\'' +
                ", ctime='" + ctime + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(errcode);
        dest.writeString(errmsg);
        dest.writeString(ctime);
    }
}
