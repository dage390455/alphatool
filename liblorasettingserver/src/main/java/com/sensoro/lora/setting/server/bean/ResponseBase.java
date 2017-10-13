package com.sensoro.lora.setting.server.bean;

/**
 * Created by tangrisheng on 2016/5/5.
 * Response Base Class
 */

public class ResponseBase {
    int err_code;
    String errmsg;

    public int getErr_code() {
        return err_code;
    }

    public void setErr_code(int err_code) {
        this.err_code = err_code;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    @Override
    public String toString() {
        return "ResponseBase{" +
                "err_code=" + err_code +
                ", errmsg='" + errmsg + '\'' +
                '}';
    }
}
