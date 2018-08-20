package com.sensoro.loratool.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.zxing.lib.CaptureActivity;
import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.UpgradeDeviceAdapter;
import com.sensoro.libbleserver.ble.BLEDevice;
import com.sensoro.libbleserver.ble.SensoroConnectionCallback;
import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.libbleserver.ble.SensoroDeviceConnection;
import com.sensoro.libbleserver.ble.SensoroWriteCallback;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.service.DfuService;
import com.sensoro.loratool.store.DeviceDataDao;
import com.sensoro.loratool.utils.DownloadUtil;
import com.sensoro.loratool.widget.RecycleViewDivider;
import com.sensoro.loratool.widget.RecycleViewItemClickListener;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

import static com.google.zxing.lib.CaptureActivity.ZXING_REQUEST_CODE;
import static com.google.zxing.lib.CaptureActivity.ZXING_REQUEST_CODE_RESULT;
import static com.google.zxing.lib.CaptureActivity.ZXING_REQUEST_CODE_SCAN_BEACON;

/**
 * Created by sensoro on 18/1/8.
 */

public class UpgradeDeviceListActivity extends BaseActivity implements Constants, DfuProgressListener,RecycleViewItemClickListener,LoRaSettingApplication.SensoroDeviceListener, SensoroConnectionCallback, SensoroWriteCallback {

    public static final String EXTERN_DIRECTORY_NAME = "sensoro_dfu";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 101;
    private static final int DFU_MAX_COUNT = 5;
    @BindView(R.id.upgrade_device_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.upgrade_device_add)
    ImageView addIv;
    @BindView(R.id.upgrade_device_start)
    Button mStartButton;
    private RecycleViewDivider mDivider = null;
    private UpgradeDeviceAdapter mUpgradeDeviceAdapter = null;
    private SensoroDeviceConnection sensoroConnection = null;
    private SensoroDevice targetDevice = null;
    private String zipFileString = null;
    private String firmwareVersionString = null;
    private JSONArray snJSonArray = new JSONArray();
    private int dfu_count = 0;
    private int deviceIndex = 0;
    private boolean isStartUpgrade = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_device);
        ButterKnife.bind(this);
        init();
        MobclickAgent.onPageStart("设备升级");
    }

    private void init() {
        ArrayList<SensoroDevice>  targetDeviceList = getIntent().getParcelableArrayListExtra(EXTRA_NAME_DEVICE_LIST);
        int upgrade_index = getIntent().getIntExtra(EXTRA_UPGRADE_INDEX, 0);
        if (upgrade_index == 0) {
            addIv.setVisibility(View.GONE);
        } else {
            addIv.setVisibility(View.VISIBLE);
        }
        firmwareVersionString = getIntent().getStringExtra(EXTRA_NAME_DEVICE_FIRMWARE_VERSION);
        progressDialog = new ProgressDialog(this);
        mUpgradeDeviceAdapter = new UpgradeDeviceAdapter(this, this);
        mUpgradeDeviceAdapter.setData(targetDeviceList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mUpgradeDeviceAdapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.y1);
        mDivider = new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, spacingInPixels, R.color.station_item_more_line, false);
        mRecyclerView.addItemDecoration(mDivider);
        initSensoroSDK();
    }

    private void initSensoroSDK() {
        try {
            LoRaSettingApplication  loRaSettingApplication = (LoRaSettingApplication) getApplication();
            loRaSettingApplication.registersSensoroDeviceListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_upgrade_device;
    }

    @OnClick(R.id.settings_upgrade_device_back)
    public void back() {
        this.finish();
    }

    @OnClick(R.id.upgrade_device_add)
    public void add() {
        if (requireCameraPermission()) {
            Intent intent = new Intent(this, CaptureActivity.class);
            intent.putExtra(ZXING_REQUEST_CODE, ZXING_REQUEST_CODE_SCAN_BEACON);
            intent.putParcelableArrayListExtra(EXTRA_NAME_DEVICE_LIST, mUpgradeDeviceAdapter.getData());
            startActivityForResult(intent, ZXING_REQUEST_CODE_SCAN_BEACON);
        } else {
            Toast.makeText(this, R.string.tips_open_ble_service, Toast.LENGTH_SHORT).show();
        }

    }

    private boolean isFit(SensoroDevice newDevice) {
        boolean isFit = false;
        if (!isExistDevice(newDevice)) {
            List<DeviceInfo> deviceInfoList = ((LoRaSettingApplication) getApplication()).getDeviceInfoList();
            for (int j = 0; j < deviceInfoList.size(); j ++ ) {
                DeviceInfo deviceInfo = deviceInfoList.get(j);
                if (deviceInfo.getSn().equalsIgnoreCase(newDevice.getSn())) {
                    newDevice.setHardwareVersion(deviceInfo.getDeviceType());
                    newDevice.setBand(deviceInfo.getBand());
                    newDevice.setFirmwareVersion(deviceInfo.getFirmwareVersion());
                    break;
                }
            }
            SensoroDevice device = mUpgradeDeviceAdapter.getData().get(0);
            if (newDevice.getHardwareVersion().equalsIgnoreCase(device.getHardwareVersion())) {
                if (newDevice.getFirmwareVersion().equalsIgnoreCase(device.getFirmwareVersion())) {
                    if (newDevice.getBand().equalsIgnoreCase(device.getBand())) {
                        isFit = true;
                    }
                }
            }
        }
        return isFit;
    }

    private boolean isExistDevice(SensoroDevice newDevice) {
        boolean isExist = false;
        for (int i = 0 ; i < mUpgradeDeviceAdapter.getData().size(); i ++) {
            SensoroDevice tempDevice = mUpgradeDeviceAdapter.getData().get(i);
            if (tempDevice.getSn().equalsIgnoreCase(newDevice.getSn())) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    @OnClick(R.id.upgrade_device_add_nearby)
    public void addNearBy() {
        ConcurrentHashMap<String, SensoroDevice> deviceMap = ((LoRaSettingApplication) getApplication()).getSensoroDeviceMap();
        int count = 0;
        for (String key : deviceMap.keySet()) {
            SensoroDevice sensoroDevice = deviceMap.get(key);
            if (isFit(sensoroDevice)) {
                mUpgradeDeviceAdapter.getData().add(sensoroDevice);
                count++;
            }
        }
        mUpgradeDeviceAdapter.notifyDataSetChanged();
        Toast.makeText(this, "找到" + count + "个匹配设备", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.upgrade_device_start)
    public void start() {
        if (!isStartUpgrade) {
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.show();
            isStartUpgrade = true;
            requestDownLoadZip();
        }
    }

    private boolean requireCameraPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {


            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ZXING_REQUEST_CODE_RESULT) {

            ArrayList<SensoroDevice> sensoroDeviceArrayList = data.getParcelableArrayListExtra(EXTRA_NAME_DEVICE_LIST);
            System.out.println("UpgradeDeviceList.size=>" + sensoroDeviceArrayList.size());
            mUpgradeDeviceAdapter.getData().clear();
            mUpgradeDeviceAdapter.getData().addAll(sensoroDeviceArrayList);
            mUpgradeDeviceAdapter.notifyDataSetChanged();
        }
    }

    public void dfuStart() {
        DfuServiceInitiator initiator = new DfuServiceInitiator(targetDevice.getMacAddress())
                .setDisableNotification(true)
                .setZip(zipFileString);
        initiator.start(this, DfuService.class);

    }


    private void connectDevice() {
        try {
            System.out.println("targetDevice.getMacAddress()===>" + targetDevice.getMacAddress());
            System.out.println("targetDevice.getPassword()===>" + targetDevice.getPassword());
            sensoroConnection = new SensoroDeviceConnection(this, targetDevice.getMacAddress());
            sensoroConnection.connect(targetDevice.getPassword(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenDfu(String title) {
        System.out.println("targetDeviceList.size===>" + mUpgradeDeviceAdapter.getData().size());
        System.out.println("deviceIndex===>" + deviceIndex);
        if (deviceIndex == (mUpgradeDeviceAdapter.getData().size() - 1)) {
            mUpgradeDeviceAdapter.getData(deviceIndex).setDfuProgress(0);
            mUpgradeDeviceAdapter.getData(deviceIndex).setDfuInfo(title);
            requestUpdateDeviceUpgradeInfo();
            isStartUpgrade = false;
            System.out.println("设备已全部升级完毕===>");
            deviceIndex = 0;
            mStartButton.setBackground(getResources().getDrawable(R.drawable.shape_upgrade_enable));
        } else {
            mUpgradeDeviceAdapter.getData(deviceIndex).setDfuProgress(0);
            mUpgradeDeviceAdapter.getData(deviceIndex).setDfuInfo(title);
            deviceIndex ++;
            targetDevice = mUpgradeDeviceAdapter.getData().get(deviceIndex);
            System.out.println("升级设备===>" + targetDevice.getSn());
            connectDevice();

        }
        mUpgradeDeviceAdapter.notifyDataSetChanged();
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
        System.out.println("dataString=>" +dataString);
        LoRaSettingApplication application = (LoRaSettingApplication) getApplication();
        application.loRaSettingServer.updateDeviceUpgradeInfo(dataString, new Response.Listener<ResponseBase>() {
            @Override
            public void onResponse(ResponseBase responseBase) {
                if (responseBase.getErr_code() == 0) {
                } else {
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

    @Override
    public void onDeviceConnecting(String deviceAddress) {

    }

    @Override
    public void onDeviceConnected(String deviceAddress) {

    }

    @Override
    public void onDfuProcessStarting(String deviceAddress) {
        mUpgradeDeviceAdapter.getData(deviceIndex).setDfuInfo(getString(R.string.dfu_ready));
        mUpgradeDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDfuProcessStarted(String deviceAddress) {
        mUpgradeDeviceAdapter.getData(deviceIndex).setDfuInfo(getString(R.string.dfu_trans));
        mUpgradeDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEnablingDfuMode(String deviceAddress) {

    }

    @Override
    public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
        mUpgradeDeviceAdapter.getData(deviceIndex).setDfuProgress(percent);
        mUpgradeDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFirmwareValidating(String deviceAddress) {

    }

    @Override
    public void onDeviceDisconnecting(String deviceAddress) {

    }

    @Override
    public void onDeviceDisconnected(String deviceAddress) {

    }

    @Override
    public void onDfuCompleted(String deviceAddress) {
        snJSonArray.put(targetDevice.getSn());
        listenDfu(getString(R.string.upgrade_finish));
    }

    @Override
    public void onDfuAborted(String deviceAddress) {
        listenDfu(getString(R.string.upgrade_failed));
    }


    @Override
    public void onError(String deviceAddress, int error, int errorType, String message) {
        if (error == 4102 && dfu_count <= DFU_MAX_COUNT) {// dfu service not found
            dfuStart();
            dfu_count++;
        } else {
            listenDfu(getString(R.string.upgrade_failed));
        }
    }


    @Override
    public void onConnectedSuccess(final BLEDevice bleDevice, int cmd) {
        String sn = targetDevice.getSn();
        String macAddress = targetDevice.getMacAddress();
        String firmwareVersion = targetDevice.getFirmwareVersion();
        targetDevice = (SensoroDevice) bleDevice;
        targetDevice.setFirmwareVersion(firmwareVersion);
        targetDevice.setSn(sn);
        targetDevice.setMacAddress(macAddress);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUpgradeDeviceAdapter.getData(deviceIndex).setDfuProgress(0);
                mUpgradeDeviceAdapter.getData(deviceIndex).setDfuInfo(getString(R.string.dfu_connect_success));
                sensoroConnection.writeCmd(UpgradeDeviceListActivity.this);
            }
        });
    }

    @Override
    public void onConnectedFailure(int errorCode) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listenDfu(getString(R.string.upgrade_failed));
            }
        });

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onWriteSuccess(Object o, int cmd) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sensoroConnection.disconnect();
                targetDevice.setDfu(true);
                dfuStart();
                mUpgradeDeviceAdapter.getData(deviceIndex).setDfuInfo(getString(R.string.dfu_write_success));
                mUpgradeDeviceAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onWriteFailure(int errorCode, final int cmd) {
        targetDevice.setDfu(false);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listenDfu(getString(R.string.upgrade_failed));
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        DfuServiceListenerHelper.registerProgressListener(this, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        DfuServiceListenerHelper.unregisterProgressListener(this, this);
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    public void startUpgrade() {
        isStartUpgrade = true;
        deviceIndex = 0;
        targetDevice = mUpgradeDeviceAdapter.getData(deviceIndex);
        targetDevice.setDfuInfo(getString(R.string.dfu_connecting));
        mUpgradeDeviceAdapter.notifyDataSetChanged();
        mStartButton.setBackground(getResources().getDrawable(R.drawable.shape_upgrade_disable));
        System.out.println("升级设备===>" + targetDevice.getSn());
        connectDevice();
    }

    private void requestDownLoadZip() {
        String zipUrl = getIntent().getStringExtra(EXTRA_URL);
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
        if (!isContainFile) {
            DownloadUtil.getInstance().download(zipUrl, EXTERN_DIRECTORY_NAME, new DownloadUtil.OnDownloadListener() {
                @Override
                public void onDownloadSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            startUpgrade();
                        }
                    });

                }

                @Override
                public void onDownloading(int progress) {

                }

                @Override
                public void onDownloadFailed() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(UpgradeDeviceListActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            progressDialog.dismiss();
            startUpgrade();
        }
    }

    @Override
    public void onNewDevice(BLEDevice bleDevice) {
        if (bleDevice instanceof SensoroDevice) {
            System.out.println("found device =>" + bleDevice.getSn());
            final SensoroDevice newDevice = (SensoroDevice) bleDevice;
            if (isFit(newDevice)) {
                System.out.println(" device fit =>" + bleDevice.getSn());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mUpgradeDeviceAdapter.getData().add(newDevice);
                        mUpgradeDeviceAdapter.notifyDataSetChanged();
                    }
                });

            }
        }
    }

    @Override
    public void onGoneDevice(BLEDevice bleDevice) {

    }

    @Override
    public void onUpdateDevices(ArrayList<BLEDevice> deviceList) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoRaSettingApplication loRaSettingApplication = (LoRaSettingApplication) getApplication();
        loRaSettingApplication.unRegistersSensoroDeviceListener(this);
    }
}
