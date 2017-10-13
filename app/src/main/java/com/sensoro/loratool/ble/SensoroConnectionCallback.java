package com.sensoro.loratool.ble;

/**
 * Created by sensoro on 17/5/4.
 */

public interface SensoroConnectionCallback {

    void onConnectedSuccess(BLEDevice bleDevice, int cmd);

    void onConnectedFailure(int errorCode);

    void onDisconnected();
}