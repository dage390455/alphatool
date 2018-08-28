package com.sensoro.loratool.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.lora.setting.server.ILoRaSettingServer;
import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.lora.setting.server.bean.DeviceInfoListRsp;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.CaptureActivity;
import com.sensoro.loratool.activity.UpgradeDeviceListActivity;
import com.sensoro.loratool.base.BasePresenter;
import com.sensoro.loratool.imainview.ICaptureActivityView;
import com.sensoro.loratool.utils.LogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.sensoro.loratool.constant.Constants.EXTRA_NAME_DEVICE_LIST;
import static com.sensoro.loratool.constant.Constants.ZXING_REQUEST_CODE_RESULT;

public class CaptureActivityPresenter extends BasePresenter<ICaptureActivityView>implements MediaPlayer.OnErrorListener{

    private Context mContext;
    private Map<String,SensoroDevice> deviceMap = new HashMap<>();
    private boolean isMulti = false;
    private MediaPlayer mediaPlayer;

    @Override
    public void initData(Context context) {
        mContext = context;
        mediaPlayer = buildMediaPlayer(mContext);
        ArrayList<SensoroDevice> dataList = ((CaptureActivity)mContext).getIntent().getParcelableArrayListExtra(EXTRA_NAME_DEVICE_LIST);
        for (int i = 0; i < dataList.size(); i ++) {
            deviceMap.put(dataList.get(i).getSn(), dataList.get(i));
        }
    }

    @Override
    public void onDestroy() {
        deviceMap.clear();
    }

    public void processScanResult(String result) {
        playVoice();
        if (TextUtils.isEmpty(result)) {
            getView().toastShort(mContext.getResources().getString(R.string.scan_failed));
            return;
        }
        String scanSerialNumber = parseResultMac(result);
        if (scanSerialNumber == null) {
            getView().toastShort(mContext.getResources().getString(R.string.qr_not_lora_station));
            getView().startScan();
        } else {
            processResult(scanSerialNumber, true);
        }
    }

    private String parseResultMac(String result) {
        String serialNumber = null;
        if (result != null) {
            String[] data = null;
            data = result.split("\\|");
            // if length is 2, it is fault-tolerant hardware.
            serialNumber = data[0];

        }
        return serialNumber;
    }

    private void processResult(String sn,boolean isScan) {
        LoRaSettingApplication application = (LoRaSettingApplication) mContext.getApplicationContext();
        if (application.getSensoroDeviceMap().containsKey(sn)){
            List<DeviceInfo> deviceInfoList = application.getDeviceInfoList();
            boolean isFind = false;
            for (int i = 0 ; i <deviceInfoList.size(); i++) {
                DeviceInfo deviceInfo = deviceInfoList.get(i);
                if (deviceInfo.getSn().equalsIgnoreCase(sn)) {
                    isFind = true;
                    SensoroDevice sensoroDevice = application.getSensoroDeviceMap().get(sn);
                    sensoroDevice.setPassword(deviceInfo.getPassword());
                    sensoroDevice.setFirmwareVersion(deviceInfo.getFirmwareVersion());
                    sensoroDevice.setHardwareVersion(deviceInfo.getDeviceType());
                    sensoroDevice.setBand(deviceInfo.getBand());
                    if (isSupportDevice(sensoroDevice)) {
                        if (!deviceMap.containsKey(sn)) {
                            getView().toastShort("设备添加成功");
                            deviceMap.put(sn, sensoroDevice);
                        } else {
                            getView().toastShort("设备已存在升级队列中");

                        }
                        if (!isMulti) {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_NAME_DEVICE_LIST, new ArrayList<>(deviceMap.values()));
                            getView().setIntentResult(ZXING_REQUEST_CODE_RESULT,intent);
                        }else if(isScan){
                            getView().startScan();
                        }
                    }else{
                        getView().toastShort("不支持该设备");
                        if(isScan){
                            getView().startScan();
                        }
                    }

                    break;
                }
            }
            if (!isFind) {
                requestDeviceWithSearch(sn,isScan);
            }
        } else {
            if(isScan){
                getView().startScan();
            }
            getView().toastShort("未找到设备在附近");

        }

    }
    private MediaPlayer buildMediaPlayer(Context activity) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try (AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.beep)) {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(false);
            mediaPlayer.setVolume(0.1f, 0.1f);
            mediaPlayer.prepare();
            return mediaPlayer;
        } catch (IOException ioe) {
            LogUtils.loge(this, ioe.getMessage());
            mediaPlayer.release();
            return null;
        }
    }

    private void playVoice() {
        vibrate();
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }
    private void vibrate() {
        Vibrator vibrator = (Vibrator) mContext.getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(200);
        }
    }

    private boolean isSupportDevice(SensoroDevice device) {
        boolean isSame = false;
        for (String key : deviceMap.keySet()) {
            SensoroDevice sensoroDevice = deviceMap.get(key);
            System.out.println("sensorodevice.hw=>" + sensoroDevice.getHardwareVersion());
            System.out.println("device.hw=>" + device.getHardwareVersion());
            System.out.println("sensorodevice.fw=>" + sensoroDevice.getHardwareVersion());
            System.out.println("device.fw=>" + device.getHardwareVersion());
            if (sensoroDevice.getHardwareVersion().equalsIgnoreCase(device.getHardwareVersion())) {
                if (sensoroDevice.getFirmwareVersion().equalsIgnoreCase(device.getFirmwareVersion())) {
                    if (sensoroDevice.getBand().equalsIgnoreCase(device.getBand())) {
                        isSame = true;
                    } else {
                        getView().toastShort(mContext.getResources().getString(R.string.tips_same_band));
                        isSame = false;
                    }
                } else {
                    getView().toastShort(mContext.getResources().getString(R.string.tips_same_firmware));
                    isSame = false;
                }
            } else {
                getView().toastShort(mContext.getResources().getString(R.string.tips_same_hardware));
                isSame = false;
            }
        }
        return isSame;
    }

    public void setSwitchBtnState(boolean isChecked) {
        isMulti = isChecked;
    }

    private void requestDeviceWithSearch(final String sn, final boolean isScan) {
        final LoRaSettingApplication application = (LoRaSettingApplication) mContext.getApplicationContext();
        ILoRaSettingServer loRaSettingServer = application.loRaSettingServer;
        loRaSettingServer.deviceList(sn, "sn", new Response.Listener<DeviceInfoListRsp>() {
            @Override
            public void onResponse(final DeviceInfoListRsp response) {
                ArrayList<DeviceInfo> searchList = (ArrayList) response.getData().getItems();
                if (searchList.size() > 0) {
                    DeviceInfo deviceInfo = searchList.get(0);
                    if (application.getSensoroDeviceMap().containsKey(sn)) {
                        SensoroDevice sensoroDevice = application.getSensoroDeviceMap().get(sn);
                        sensoroDevice.setPassword(deviceInfo.getPassword());
                        sensoroDevice.setFirmwareVersion(deviceInfo.getFirmwareVersion());
                        sensoroDevice.setHardwareVersion(deviceInfo.getDeviceType());
                        sensoroDevice.setBand(deviceInfo.getBand());
                        if (isSupportDevice(sensoroDevice)) {
                            if (!deviceMap.containsKey(sn)) {
                                deviceMap.put(sn, sensoroDevice);
                                getView().toastShort("设备添加成功");
                            } else {
                                getView().toastShort("设备已存在升级队列中");

                            }
                            if (!isMulti) {
                                Intent intent = new Intent();
                                intent.putExtra(EXTRA_NAME_DEVICE_LIST, new ArrayList<>(deviceMap.values()));
                                getView().setIntentResult(ZXING_REQUEST_CODE_RESULT, intent);
                            }else if(isScan){
                                getView().startScan();
                            }
                        }else{
                            getView().toastShort("不支持该设备");
                            if (isScan) {
                                getView().startScan();
                            }
                        }
                    } else {
                        getView().toastShort("未找到设备在附近");
                        if (isScan) {
                            getView().startScan();
                        }

                    }

                } else {
                    getView().toastShort(mContext.getResources().getString(R.string.beacon_not_found));
                    if (isScan) {
                        getView().startScan();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                getView().toastShort(mContext.getResources().getString(R.string.beacon_not_found));
                if (isScan) {
                    getView().startScan();
                }

            }
        });

    }

    public void processEditResult(TextView v) {
        if (v.getText().length() == 16) {
            processResult(v.getText().toString().toUpperCase(),false);
        } else {
            getView().toastShort("SN 格式不正确");

        }
    }

    public void imvClose() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NAME_DEVICE_LIST, new ArrayList<>(deviceMap.values()));
        getView().setIntentResult(ZXING_REQUEST_CODE_RESULT,intent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }
}
