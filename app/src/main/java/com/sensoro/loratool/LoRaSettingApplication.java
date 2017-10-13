package com.sensoro.loratool;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.widget.Toast;

import com.sensoro.lora.setting.server.ILoRaSettingServer;
import com.sensoro.lora.setting.server.LoRaSettingServerImpl;
import com.sensoro.loratool.ble.BLEDevice;
import com.sensoro.loratool.ble.scanner.BLEDeviceListener;
import com.sensoro.loratool.ble.scanner.BLEDeviceManager;
import com.sensoro.loratool.store.LoraDbHelper;
import com.sensoro.loratool.utils.IPUtil;
import com.sensoro.station.communication.IStation;
import com.sensoro.station.communication.StationImpl;
import com.sensoro.station.communication.bean.StationInfo;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

//
//import com.facebook.stetho.Stetho;
//import com.facebook.stetho.okhttp3.StethoInterceptor;


/**
 * Created by tangrisheng on 2016/5/9.
 * LoRaSetting Application
 */
public class LoRaSettingApplication extends Application implements BLEDeviceListener<BLEDevice> {

    public IStation station;
    public ILoRaSettingServer loRaSettingServer;
    public LocationService locationService;
    private List<StationInfo> stationInfoList = new ArrayList<>();
    private BLEDeviceManager bleDeviceManager;
    private ArrayList<SensoroDeviceListener> sensoroDeviceListeners = new ArrayList<>();
    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                ex.printStackTrace();
                System.out.println("ex====>" + ex.getMessage());
            }
        });

    }

    public static RefWatcher getRefWatcher(Context context) {
        LoRaSettingApplication application = (LoRaSettingApplication) context.getApplicationContext();
        return application.refWatcher;
    }


    private void init() {
//        refWatcher = LeakCanary.install(this);
        loRaSettingServer = LoRaSettingServerImpl.getInstance(getApplicationContext());
        locationService = new LocationService(getApplicationContext());
        initStationHandler();
        LoraDbHelper.init(this);
        initSensoroSDK();
    }

    private void initSensoroSDK() {
        try {
            bleDeviceManager = BLEDeviceManager.getInstance(this);
            boolean isEnable = bleDeviceManager.startService();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                bleDeviceManager.setForegroundScanPeriod(7000);
                bleDeviceManager.setOutOfRangeDelay(15000);
            }
//            else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                bleDeviceManager.setForegroundScanPeriod(5000);
//                bleDeviceManager.setOutOfRangeDelay(15000);
//            }
            else {
                bleDeviceManager.setOutOfRangeDelay(10000);
            }

            if (!isEnable) {
                Toast.makeText(this, R.string.tips_open_ble_service, Toast.LENGTH_SHORT).show();
            } else {
                bleDeviceManager.setBLEDeviceListener(this);
                bleDeviceManager.setBackgroundMode(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }//yangzhiqiang@sensoro.com 123456
    }

    @Override
    public void onTerminate() {
        locationService.stop();
        stationInfoList.clear();
        LoraDbHelper.instance.close();
        bleDeviceManager.stopService();
        super.onTerminate();
    }
//zhangchao asd.123456
    public void initStationHandler() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        String localIP = IPUtil.getWifiIP(wifiManager.getConnectionInfo());
        String gateWayIP = IPUtil.getGateWayIP(localIP);
        station = StationImpl.getInstance(getApplicationContext(), gateWayIP);
    }

    public List<StationInfo> getStationInfoList() {
        return stationInfoList;
    }

    public void registersSensoroDeviceListener(SensoroDeviceListener sensoroDeviceListener) {
        if (sensoroDeviceListeners != null) {
            synchronized (sensoroDeviceListeners) {
                if (!sensoroDeviceListeners.contains(sensoroDeviceListener)) {
                    sensoroDeviceListeners.add(sensoroDeviceListener);
                }
            }
        }
    }

    public void unRegistersSensoroDeviceListener(SensoroDeviceListener sensoroDeviceListener) {
        if (sensoroDeviceListener != null) {
            synchronized (sensoroDeviceListeners) {
                if (sensoroDeviceListeners.contains(sensoroDeviceListener)) {
                    sensoroDeviceListeners.remove(sensoroDeviceListener);
                }
            }
        }
    }

    @Override
    public void onNewDevice(BLEDevice bleDevice) {

        if (sensoroDeviceListeners != null) {
            synchronized (sensoroDeviceListeners) {
                if (sensoroDeviceListeners.size() != 0) {
                    for (SensoroDeviceListener listener : sensoroDeviceListeners) {
                        listener.onNewDevice(bleDevice);
                    }
                }
            }
        }
    }

    @Override
    public void onGoneDevice(BLEDevice bleDevice) {
        if (sensoroDeviceListeners != null) {
            synchronized (sensoroDeviceListeners) {
                if (sensoroDeviceListeners.size() != 0) {
                    for (SensoroDeviceListener listener : sensoroDeviceListeners) {
                        listener.onGoneDevice(bleDevice);
                    }
                }
            }
        }
    }

    @Override
    public void onUpdateDevices(ArrayList<BLEDevice> deviceList) {
        if (sensoroDeviceListeners != null) {
            synchronized (sensoroDeviceListeners) {
                if (sensoroDeviceListeners.size() != 0) {
                    for (SensoroDeviceListener listener : sensoroDeviceListeners) {
                        listener.onUpdateDevices(deviceList);
                    }
                }
            }
        }
    }

    public interface SensoroDeviceListener {
        void onNewDevice(BLEDevice bleDevice);
        void onGoneDevice(BLEDevice bleDevice);
        void onUpdateDevices(ArrayList<BLEDevice> deviceList);
    }

}