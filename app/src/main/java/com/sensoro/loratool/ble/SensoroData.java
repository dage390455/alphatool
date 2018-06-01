package com.sensoro.loratool.ble;

import java.io.Serializable;

public class SensoroData implements Serializable {
    /**
     * 基本属性date
     */
    public int data_int;
    public float data_float;
    /**
     * 预警上限值
     */
    public int alarmHigh_int;
    public float alarmHigh_float;
    /**
     * 预警下限值
     */
    public int alarmLow_int;
    public float alarmLow_float;
    /**
     * 状态属性
     */
    public int status;

    /**
     * 步长上限值
     */
    public int alarmStepHigh_int;
    public float alarmStepHigh_float;
    /**
     * 步长下限值
     */
    public int alarmStepLow_int;
    public float alarmStepLow_float;

    //
    public boolean has_data_int;
    public boolean has_data_float;

    public boolean has_alarmHigh_int;
    public boolean has_alarmHigh_float;

    public boolean has_alarmLow_int;
    public boolean has_alarmLow_float;

    public boolean has_status;

    public boolean has_alarmStepHigh_int;
    public boolean has_alarmStepHigh_float;

    public boolean has_alarmStepLow_int;
    public boolean has_alarmStepLow_float;
}
