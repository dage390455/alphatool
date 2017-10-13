package com.sensoro.station.communication.bean;

/**
 * Created by tangrisheng on 2016/5/3.
 * Station Cpu
 */
/*
            "usr": "0.4",//用户空间CPU利用率
            "sys": "0.6",//系统CPU利用率
            "idl": "0.99"//空闲CPU利用率
 */
public class StationCpu {
    protected double usr;
    protected double sys;
    protected double idl;

    public double getUsr() {
        return usr;
    }

    public void setUsr(double usr) {
        this.usr = usr;
    }

    public double getSys() {
        return sys;
    }

    public void setSys(double sys) {
        this.sys = sys;
    }

    public double getIdl() {
        return idl;
    }

    public void setIdl(double idl) {
        this.idl = idl;
    }
}
