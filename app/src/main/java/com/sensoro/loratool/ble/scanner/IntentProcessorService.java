package com.sensoro.loratool.ble.scanner;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.sensoro.loratool.ble.BLEDevice;

import java.util.ArrayList;

/**
 * Created by Sensoro on 12/18/14.
 */
public class IntentProcessorService extends IntentService {
    private MonitoredBLEDevice monitoredBLEDevice;
    private ArrayList<BLEDevice> updateDevices;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public IntentProcessorService() {
        super("IntentProcessor");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            handlerData(intent);
        } else {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    handlerData(intent);
                }
            });
        }

    }

    private void handlerData(Intent intent) {
        try {
            if (intent != null && intent.getExtras() != null) {
                monitoredBLEDevice = (MonitoredBLEDevice) intent.getExtras().get(BLEDeviceManager.MONITORED_DEVICE);
                updateDevices = (ArrayList<BLEDevice>) intent.getExtras().get(BLEDeviceManager.UPDATE_DEVICES);
            }
            if (monitoredBLEDevice != null) {
                BLEDeviceListener deviceManagerListener = BLEDeviceManager.getInstance(getApplication())
                        .getBLEDeviceListener();
                if (deviceManagerListener != null) {
                    if (monitoredBLEDevice.inSide) {
                        deviceManagerListener.onNewDevice(monitoredBLEDevice.bleDevice);
                    } else {
                        deviceManagerListener.onGoneDevice(monitoredBLEDevice.bleDevice);
                    }
                }
            }
            if (updateDevices != null) {
                BLEDeviceListener beaconManagerListener = BLEDeviceManager.getInstance(getApplication())
                        .getBLEDeviceListener();
                if (beaconManagerListener != null) {
                    beaconManagerListener.onUpdateDevices(updateDevices);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }
}
