package com.sensoro.loratool.presenter;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.sensoro.libbleserver.ble.BLEDevice;
import com.sensoro.libbleserver.ble.SensoroConnectionCallback;
import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.libbleserver.ble.SensoroDeviceConnection;
import com.sensoro.libbleserver.ble.SensoroDirectWriteDfuCallBack;
import com.sensoro.libbleserver.ble.SensoroWriteCallback;
import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.base.BasePresenter;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.imainview.IUpgradeDeviceListActivityView;
import com.sensoro.loratool.service.DfuService;
import com.sensoro.loratool.store.DeviceDataDao;
import com.sensoro.loratool.utils.DownloadUtil;
import com.sensoro.loratool.utils.LogUtils;
import com.sensoro.loratool.widget.AlphaToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceInitiator;

public class UpgradeDeviceListActivityPresenter extends BasePresenter<IUpgradeDeviceListActivityView>
        implements Constants, LoRaSettingApplication.SensoroDeviceListener, DfuProgressListener, SensoroConnectionCallback, SensoroWriteCallback, SensoroDirectWriteDfuCallBack {
    private Activity mActivity;
    private String mDefBand;
    private String mDefHardwareVersion;
    private String mDefFirmwareVersion;
    private String firmwareVersionString = null;
    private int deviceIndex = 0;
    private JSONArray snJSonArray = new JSONArray();
    public boolean isStartUpgrade = false;
    private SensoroDevice targetDevice = null;
    private SensoroDeviceConnection sensoroConnection = null;
    public static final String EXTERN_DIRECTORY_NAME = "sensoro_dfu";
    private String zipFileString = null;
    private int dfu_count = 0;
    private static final int DFU_MAX_COUNT = 5;

    private Handler mHandler;
    private Runnable dfuUpgradeTimeout = new Runnable() {
        @Override
        public void run() {
            listenDfu(mActivity.getString(R.string.upgrade_failed));
        }
    };
    private boolean isChipE;


    @Override
    public void initData(Context context) {
        mActivity = (Activity) context;
        ArrayList<SensoroDevice> targetDeviceList = mActivity.getIntent().getParcelableArrayListExtra(EXTRA_NAME_DEVICE_LIST);
        SensoroDevice sensoroDevice = targetDeviceList.get(0);
        mDefBand = sensoroDevice.getBand();
        mDefHardwareVersion = sensoroDevice.getHardwareVersion();
        mDefFirmwareVersion = sensoroDevice.getFirmwareVersion();
        firmwareVersionString = mActivity.getIntent().getStringExtra(EXTRA_NAME_DEVICE_FIRMWARE_VERSION);
        String devicetype = mActivity.getIntent().getStringExtra(EXTRA_NAME_DEVICE_TYPE);
        isChipE = Constants.CHIP_E_UPGRADE_LIST.contains(devicetype);

        int upgrade_index = mActivity.getIntent().getIntExtra(EXTRA_UPGRADE_INDEX, 0);
        getView().setAddIvVisible(upgrade_index != 0);

        getView().updateRcData(targetDeviceList);

        initSensoroSDK();

        mHandler = new Handler(Looper.myLooper());
    }

    private void initSensoroSDK() {
        try {
            LoRaSettingApplication loRaSettingApplication = (LoRaSettingApplication) mActivity.getApplication();
            loRaSettingApplication.registersSensoroDeviceListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        LoRaSettingApplication loRaSettingApplication = (LoRaSettingApplication) mActivity.getApplication();
        loRaSettingApplication.unRegistersSensoroDeviceListener(this);
    }

    @Override
    public void onNewDevice(BLEDevice bleDevice) {
        if (bleDevice instanceof SensoroDevice) {
            System.out.println("found device =>" + bleDevice.getSn());
            final SensoroDevice newDevice = (SensoroDevice) bleDevice;
            if (isFit(newDevice)) {
                System.out.println(" device fit =>" + bleDevice.getSn());
                // todo 自己更新
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        mUpgradeDeviceAdapter.getData().add(newDevice);
////                        mUpgradeDeviceAdapter.notifyDataSetChanged();
//                    }
//                });

            }
        }
    }

    @Override
    public void onGoneDevice(BLEDevice bleDevice) {

    }

    @Override
    public void onUpdateDevices(ArrayList<BLEDevice> deviceList) {

    }

    private boolean isFit(SensoroDevice newDevice) {
        boolean isFit = false;
        if (!isExistDevice(newDevice)) {
            ConcurrentHashMap<String, DeviceInfo> deviceInfoList = ((LoRaSettingApplication) mActivity.getApplication()).getmCacheDeviceMap();
            for (String s : deviceInfoList.keySet()) {
                DeviceInfo deviceInfo = deviceInfoList.get(s);
                if (deviceInfo.getSn().equalsIgnoreCase(newDevice.getSn())) {
                    newDevice.setHardwareVersion(deviceInfo.getDeviceType());
                    newDevice.setBand(deviceInfo.getBand());
                    newDevice.setFirmwareVersion(deviceInfo.getFirmwareVersion());
                    newDevice.setPassword(deviceInfo.getPassword());
                    break;
                }
            }
            if (newDevice.getHardwareVersion().equalsIgnoreCase(mDefHardwareVersion)) {
                if (newDevice.getFirmwareVersion().equalsIgnoreCase(mDefFirmwareVersion)) {
                    if (newDevice.getBand().equalsIgnoreCase(mDefBand)) {
                        isFit = true;
                    }
                }
            }
        }
        return isFit;
    }

    private boolean isExistDevice(SensoroDevice newDevice) {
        boolean isExist = false;
        ArrayList<SensoroDevice> data = getView().getAdapterData();
        for (int i = 0; i < data.size(); i++) {
            SensoroDevice tempDevice = data.get(i);
            if (tempDevice.getSn().equalsIgnoreCase(newDevice.getSn())) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    private void listenDfu(String title) {
        if (getView() != null) {
            ArrayList<SensoroDevice> data = getView().getAdapterData();
            if (data.size() > 0) {
//            SensoroDevice itemData = getView().getItemData(deviceIndex);
                if (deviceIndex == (data.size() - 1)) {
                    getView().updateRcPercentAndTip(deviceIndex, 0, title);

                    requestUpdateDeviceUpgradeInfo();
                    isStartUpgrade = false;
                    System.out.println("设备已全部升级完毕===>");
                    getView().setStartButtonBg(R.drawable.shape_upgrade_enable);

                } else if(deviceIndex < data.size()){
                    getView().updateRcPercentAndTip(deviceIndex, 0, title);
                    deviceIndex++;
                    targetDevice = data.get(deviceIndex);
                    System.out.println("升级设备===>" + targetDevice.getSn());
                    connectDevice();

                }
            }
        }


    }

    private void requestUpdateDeviceUpgradeInfo() {
        JSONObject jsonData = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sns", snJSonArray);
            jsonObject.put("firmwareVersion", firmwareVersionString);
            jsonArray.put(jsonObject);
            jsonData.put("data", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String snString = snJSonArray.toString();
        String dataString = jsonData.toString();
        LoRaSettingApplication application = (LoRaSettingApplication) mActivity.getApplication();
        application.loRaSettingServer.updateDeviceUpgradeInfo(dataString, new Response.Listener<ResponseBase>() {
            @Override
            public void onResponse(ResponseBase responseBase) {
                if (responseBase.getErr_code() != 0) {
                    DeviceDataDao.addUpgradeInfoItem(firmwareVersionString, snString);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                DeviceDataDao.addUpgradeInfoItem(firmwareVersionString, snString);
            }
        });
    }

    private void connectDevice() {
        try {
            if (isChipE) {
                sensoroConnection = new SensoroDeviceConnection(mActivity, targetDevice.getMacAddress(), false, false, true);
            } else {
                sensoroConnection = new SensoroDeviceConnection(mActivity, targetDevice.getMacAddress(), targetDevice.isDfu());
                sensoroConnection.setOnSensoroDirectWriteDfuCallBack(this);
            }
            sensoroConnection.connect(targetDevice.getPassword(), this);
        } catch (Exception e) {
            e.printStackTrace();
            //就是给个提示，errcode 无意义
            onConnectedFailure(1);
        }
    }

    private void dfuStart() {
        DfuServiceInitiator initiator = new DfuServiceInitiator(targetDevice.getMacAddress())
                .setDisableNotification(true)
                .setZip(zipFileString);
        initiator.start(mActivity, DfuService.class);

    }


    public void requestDownLoadZip() {
        String zipUrl = mActivity.getIntent().getStringExtra(EXTRA_URL);
        String path = Environment.getExternalStorageDirectory().getPath() + "/" + EXTERN_DIRECTORY_NAME;
        String fileName = zipUrl.substring(zipUrl.lastIndexOf("/") + 1, zipUrl.length());
        File directoryFile = new File(path);
        File files[] = directoryFile.listFiles();
        boolean isContainFile = false;
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String tempFileName = files[i].getName();
                if (tempFileName.equals(fileName)) {
                    isContainFile = true;
                }
            }
        }
        zipFileString = path + "/" + fileName;
        LogUtils.loge("是否需要下载" + isContainFile);
        if (!isContainFile) {
            DownloadUtil.getInstance().download(zipUrl, EXTERN_DIRECTORY_NAME, new DownloadUtil.OnDownloadListener() {
                @Override
                public void onDownloadSuccess() {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getView().showProgressDialog();
                            startUpgrade();
                        }
                    });

                }

                @Override
                public void onDownloading(int progress) {

                }

                @Override
                public void onDownloadFailed() {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getView().dismissProgressDialog();
                            getView().toastShort("下载失败");
                        }
                    });
                }
            });
        } else {
            getView().dismissProgressDialog();
            startUpgrade();
        }
    }

    private void startUpgrade() {
        isStartUpgrade = true;
//        deviceIndex = 0;

        if (getView().getAdapterData().size() > 0) {
            targetDevice = getView().getItemData(deviceIndex);
            if (!isChipE) {
                while (targetDevice.getDfuInfo() != null && targetDevice.getDfuInfo().equals(mActivity.getString(R.string.upgrade_finish))) {
                    if (deviceIndex < getView().getAdapterData().size() - 1) {
                        deviceIndex++;
                        targetDevice = getView().getItemData(deviceIndex);
                    } else {
                        getView().toastShort("设备已全部升级完毕");
                        return;
                    }
                }
            }

            getView().updateRc(deviceIndex, R.string.dfu_connecting);
            getView().setStartButtonBg(R.drawable.shape_upgrade_disable);
            System.out.println("升级设备===>" + targetDevice.getSn());
            connectDevice();
        } else {
            getView().toastShort("未添加升级设备");
        }

    }

    @Override
    public void onDeviceConnecting(String deviceAddress) {
        LogUtils.loge("dfu连接中");
    }

    @Override
    public void onDeviceConnected(String deviceAddress) {
        LogUtils.loge("dfu连接成功了");
    }

    @Override
    public void onDfuProcessStarting(String deviceAddress) {
        LogUtils.loge("dfu开始升级");
        getView().updateRc(deviceIndex, R.string.dfu_ready);

    }

    @Override
    public void onDfuProcessStarted(String deviceAddress) {
        LogUtils.loge("等待传输控件");
        getView().updateRc(deviceIndex, R.string.dfu_trans);
    }

    @Override
    public void onEnablingDfuMode(String deviceAddress) {
        LogUtils.loge("不知道什么用的:onEnablingDfuMode::");
    }

    @Override
    public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
        LogUtils.loge("升级进度:::" + percent);
        getView().updateRcProgressChanged(deviceIndex, percent);
    }

    @Override
    public void onFirmwareValidating(String deviceAddress) {
        LogUtils.loge("dfu通过了验证");
    }

    @Override
    public void onDeviceDisconnecting(String deviceAddress) {
        LogUtils.loge("dfu断开连接中");
        getView().updateRcPercentAndTip(deviceIndex, 101, "升级中");

        mHandler.postDelayed(dfuUpgradeTimeout, 60000);
    }

    @Override
    public void onDeviceDisconnected(String deviceAddress) {
        LogUtils.loge("dfu断开连接了");
    }

    @Override
    public void onDfuCompleted(String deviceAddress) {
        LogUtils.loge("dfuComplete完成了");
        snJSonArray.put(targetDevice.getSn());
        listenDfu(mActivity.getString(R.string.upgrade_finish));
        mHandler.removeCallbacks(dfuUpgradeTimeout);
    }

    @Override
    public void onDfuAborted(String deviceAddress) {
        LogUtils.loge(":dfu::" + "中断了");
        listenDfu(mActivity.getString(R.string.upgrade_failed));
        mHandler.removeCallbacks(dfuUpgradeTimeout);
    }

    @Override
    public void onError(String deviceAddress, int error, int errorType, String message) {
        LogUtils.loge("错误发生了");
        if (error == 4102 && dfu_count <= DFU_MAX_COUNT) {// dfu service not found
            LogUtils.loge("重新开始了");
            dfuStart();
            dfu_count++;
        } else {
            if (dfu_count > DFU_MAX_COUNT) {
                listenDfu(mActivity.getString(R.string.upgrade_failed));
                sensoroConnection.disconnect();
                mHandler.removeCallbacksAndMessages(null);
            } else {
                LogUtils.loge("错误导致升级失败 失败码" + error + message);
                sensoroConnection.freshCache();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.loge("重新连接，重新开始");
                        connectDevice();
                    }
                }, 500);
            }
        }
        mHandler.removeCallbacks(dfuUpgradeTimeout);
    }

    @Override
    public void onConnectedSuccess(BLEDevice bleDevice, int cmd) {

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getView().updateRcPercentAndTip(deviceIndex, 0, mActivity.getString(R.string.dfu_connect_success));
                if (isChipE) {
                    startUpgradeChipE();
                } else {
                    stratUpgrade((SensoroDevice) bleDevice);
                }
            }
        });
    }

    private void stratUpgrade(SensoroDevice bleDevice) {
        String sn = targetDevice.getSn();
        String macAddress = targetDevice.getMacAddress();
        String firmwareVersion = targetDevice.getFirmwareVersion();
        targetDevice = bleDevice;
        targetDevice.setFirmwareVersion(firmwareVersion);
        targetDevice.setSn(sn);
        targetDevice.setMacAddress(macAddress);
        sensoroConnection.writeUpgradeCmd(UpgradeDeviceListActivityPresenter.this);
    }

    private void startUpgradeChipE() {
        isStartUpgrade = true;
        getView().dismissProgressDialog();
        ArrayList<SensoroDevice> data = getView().getAdapterData();
        if (data == null || deviceIndex >= data.size()) {
            AlphaToast.INSTANCE.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.all_upgrade_completed), Toast.LENGTH_SHORT).show();
            return;
        }
        sensoroConnection.writeUpgradeCmd(zipFileString, 1, new SensoroWriteCallback() {
            @Override
            public void onWriteSuccess(Object o, int cmd) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (o instanceof Integer) {
                            Integer i = (Integer) o;
                            LogUtils.loge("index =" + deviceIndex + "i = " + i);
                            if (i <= 100) {
                                getView().updateRcProgressChanged(deviceIndex, (Integer) o);
                            } else {
                                snJSonArray.put(targetDevice.getSn());
                                getView().updateRcPercentAndTip(deviceIndex, 0, mActivity.getString(R.string.upgrade_finish));

                                deviceIndex++;
                                Log.e("ddf", deviceIndex + ":ddf::" + data.size());
                                if (deviceIndex < data.size()) {
                                    targetDevice = data.get(deviceIndex);
                                    System.out.println("升级设备===>" + targetDevice.getSn());
                                    connectDevice();
                                } else if (deviceIndex == data.size()) {
                                    requestUpdateDeviceUpgradeInfo();
                                    isStartUpgrade = false;
                                    System.out.println("设备已全部升级完毕===>");
//                                    sensoroConnection.disconnect();
                                    getView().setStartButtonBg(R.drawable.shape_upgrade_enable);
                                }

                            }
                        }
                    }
                });


            }

            @Override
            public void onWriteFailure(int errorCode, int cmd) {

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String s = "异常";
                        switch (errorCode) {
                            case 0:
                                s = "文件为空";
                                break;
                            case 1:
                                s = "文件大小为空";
                                break;
                            case 2:
                                s = "packet包错误";
                                break;
                            case 3:
                                s = "发送包失败";
                                break;
                            case 4:
                                s = "发送数据错误";
                                break;
                            case 5:
                                s = "发送确认指令失败";
                            case 6:
                                s = "升级异常";
                                break;
                            case 7:
                                s = "升级过程错误";
                                break;
                        }
                        getView().updateRcPercentAndTip(deviceIndex, 0, s);
                        isStartUpgrade = false;
                        getView().setStartButtonBg(R.drawable.shape_upgrade_enable);
                        sensoroConnection.disconnect();
                    }
                });

            }
        });
    }

    @Override
    public void onConnectedFailure(int errorCode) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listenDfu(mActivity.getString(R.string.connect_failed));


            }
        });
    }

    @Override
    public void onDisconnected() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogUtils.loge("更新界面 连接失败");
                listenDfu(mActivity.getString(R.string.connect_failed));
            }
        });
    }

    @Override
    public void OnDirectWriteDfuCallBack() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sensoroConnection.disconnect();
                targetDevice.setDfu(true);
                dfuStart();
                getView().updateRc(deviceIndex, R.string.dfu_write_success);
            }
        });
    }

    @Override
    public void onWriteSuccess(Object o, int cmd) {
        LogUtils.loge("xiedfur");
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogUtils.loge("写入dfu命令成功");
                getView().dismissProgressDialog();
                sensoroConnection.disconnect();
                targetDevice.setDfu(true);
                dfuStart();
                getView().updateRc(deviceIndex, R.string.dfu_write_success);
            }
        });
    }

    @Override
    public void onWriteFailure(int errorCode, int cmd) {
        targetDevice.setDfu(false);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogUtils.loge("切换dfu失败，在UpgradeDeviceListActivity onWriteFailure");
                listenDfu(mActivity.getString(R.string.upgrade_failed));
            }
        });
    }

    public void addNearBy() {
        ConcurrentHashMap<String, SensoroDevice> deviceMap = ((LoRaSettingApplication) mActivity.getApplication()).getmNearDeviceMap();
        int count = 0;
        for (String key : deviceMap.keySet()) {
            SensoroDevice sensoroDevice = deviceMap.get(key);
            if (isFit(sensoroDevice)) {
                getView().getAdapterData().add(sensoroDevice);
                count++;
            }
        }
        getView().notigyAdapter();

        getView().toastShort("找到" + count + "个匹配设备");
    }

    public boolean isUpgradeAll() {
        if(deviceIndex == 0){
            return  getView().getAdapterData().get(0).getDfuInfo() != null &&mActivity.getString(R.string.upgrade_finish).equals(getView().getAdapterData().get(0).getDfuInfo());
        }
        return deviceIndex >= getView().getAdapterData().size()-1;
    }
}
