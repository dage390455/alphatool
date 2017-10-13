package com.sensoro.loratool.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.model.DeviceData;
import com.sensoro.loratool.model.UpgradeData;
import com.sensoro.loratool.receiver.AlarmReceiver;
import com.sensoro.loratool.store.DeviceDataDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sensoro on 16/9/5.
 */

public class PollingService extends Service {
    private static final String TAG = "LongRunningService";
    private ExecutorService executorService ;

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                checkData();
            }
        });
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int alarmTime = 60 * 1000; // 定时10s
        long trigerAtTime = SystemClock.elapsedRealtime() + alarmTime;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void checkData() {

        final List<DeviceData> list = DeviceDataDao.getDeviceDataList();
        final List<UpgradeData> upgradeDataList = DeviceDataDao.getUpgradeDataList();
        if (list != null && list.size() > 0) {
            try {
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonDataObject = new JSONObject();
                for (int i = 0; i < list.size(); i++) {
                    DeviceData deviceData = list.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("SN", deviceData.getSn());
                    jsonObject.put("version", deviceData.getVersion());
                    jsonObject.put("data", deviceData.getData());
                    jsonArray.put(jsonObject);
                }
                jsonDataObject.put("devices", jsonArray.toString());
                String decodeString = "{\"devices\":"+jsonArray.toString()+"}";
                LoRaSettingApplication loRaSettingApplication = (LoRaSettingApplication) this.getApplication();
                Log.d("decodeString", "===>" + decodeString);
                loRaSettingApplication.loRaSettingServer.updateDevices(decodeString, new Response.Listener<ResponseBase>() {
                    @Override
                    public void onResponse(ResponseBase responseBase) {
                        if (responseBase.getErr_code() == 0) {
                            for (int i = 0; i < list.size(); i++) {
                                DeviceData deviceData = list.get(i);
                                DeviceDataDao.removeDeviceItem(deviceData);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (volleyError.networkResponse != null) {
                            Log.i("Error", new String(volleyError.networkResponse.data));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (upgradeDataList != null && upgradeDataList.size() > 0) {
            JSONObject jsonData = new JSONObject();
            try {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < upgradeDataList.size(); i ++) {
                    UpgradeData upgradeData = upgradeDataList.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sns", upgradeData.getData());
                    jsonObject.put("firmwareVersion", upgradeData.getFirmwareVersion());
                    jsonArray.put(jsonObject);
                }
                jsonData.put("data", jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String dataString = jsonData.toString();
            LoRaSettingApplication application = (LoRaSettingApplication) getApplication();
            application.loRaSettingServer.updateDeviceUpgradeInfo(dataString, new Response.Listener<ResponseBase>() {
                @Override
                public void onResponse(ResponseBase responseBase) {
                    if (responseBase.getErr_code() == 0) {
                        for (int i = 0; i < upgradeDataList.size(); i++) {
                            UpgradeData upgradeData = upgradeDataList.get(i);
                            DeviceDataDao.removeUpgradeInfoItem(upgradeData);
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                }
            });
        }
    }

}
