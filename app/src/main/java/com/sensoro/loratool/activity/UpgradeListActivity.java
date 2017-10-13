package com.sensoro.loratool.activity;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.lora.setting.server.bean.UpgradeInfo;
import com.sensoro.lora.setting.server.bean.UpgradeListRsp;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.UpgradeInfoAdapter;
import com.sensoro.loratool.ble.BLEDevice;
import com.sensoro.loratool.ble.SensoroConnectionCallback;
import com.sensoro.loratool.ble.SensoroDeviceConnection;
import com.sensoro.loratool.ble.SensoroDevice;
import com.sensoro.loratool.ble.SensoroWriteCallback;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.service.DfuService;
import com.sensoro.loratool.store.DeviceDataDao;
import com.sensoro.loratool.widget.RecycleViewDivider;
import com.sensoro.loratool.widget.RecycleViewItemClickListener;
import com.sensoro.loratool.widget.SensoroProgressDialog;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

/**
 * Created by sensoro on 16/9/12.
 */

public class UpgradeListActivity extends BaseActivity implements DfuProgressListener,RecycleViewItemClickListener, View.OnClickListener, SensoroConnectionCallback, SensoroWriteCallback {

    public static final String EXTERN_DIRECTORY_NAME = "sensoro_dfu";
    private static final int DFU_MAX_COUNT = 5;
    @BindView(R.id.upgrade_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.upgrade_title_btn1)
    ImageView refreshIv;
    private RecycleViewDivider mDivider = null;
    private UpgradeInfoAdapter mUpgradeInfoAdapter = null;
    private ProgressDialog progressDialog = null;
    private SensoroProgressDialog sensoroProgressDialog = null;
    private SensoroDeviceConnection sensoroConnection = null;
    private SensoroDevice targetDevice = null;
    private ArrayList<SensoroDevice> targetDeviceList = new ArrayList<>();
    private BroadcastReceiver downLoadReceiver = null;
    private String zipFileString = null;
    private String firmwareVersionString = null;
    private JSONArray snJSonArray = null;
    private int dfu_count = 0;
    private int deviceIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        resetRootLayout();
        ButterKnife.bind(this);
        init();
        StatService.trackBeginPage(this, "设备升级");
        MobclickAgent.onPageStart("设备升级");
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_upgrade;
    }

    private void init() {
        refreshIv.setOnClickListener(this);
        targetDeviceList = getIntent().getParcelableArrayListExtra(Constants.EXTRA_NAME_DEVICE_LIST);
        int size = targetDeviceList.size();
        if (size > 0) {
            Collections.sort(targetDeviceList, new Comparator<SensoroDevice>() {
                @Override
                public int compare(SensoroDevice lhs, SensoroDevice rhs) {
                    return lhs.getFirmwareVersion().compareTo(rhs.getFirmwareVersion());
                }
            });
            mUpgradeInfoAdapter = new UpgradeInfoAdapter(this,targetDeviceList.get(0), this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setAdapter(mUpgradeInfoAdapter);
            int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.y1);
            mDivider = new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, spacingInPixels, R.color.station_item_more_line, false);
            mRecyclerView.addItemDecoration(mDivider);
            requestUpgradeInfo();
        } else {
            this.finish();
            Toast.makeText(this, R.string.target_error, Toast.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.settings_v4_upgrade_back)
    public void back() {
        this.finish();
    }

    private void requestUpgradeInfo() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();
        LoRaSettingApplication application = (LoRaSettingApplication) getApplication();
        String deviceType = this.getIntent().getStringExtra(Constants.EXTRA_NAME_DEVICE_TYPE);
        String band = this.getIntent().getStringExtra(Constants.EXTRA_NAME_BAND);
        String hv = this.getIntent().getStringExtra(Constants.EXTRA_NAME_DEVICE_HARDWARE_VERSION);
        String fv = this.getIntent().getStringExtra(Constants.EXTRA_NAME_DEVICE_FIRMWARE_VERSION);
        application.loRaSettingServer.deviceUpgradeList(1,deviceType, band, hv, fv, new Response.Listener<UpgradeListRsp>() {
            @Override
            public void onResponse(UpgradeListRsp response) {
                ArrayList<UpgradeInfo> list = (ArrayList) response.getData().getItems();
                if (list != null) {
                    if (list.size() ==0) {
                        Toast.makeText(UpgradeListActivity.this, R.string.tips_no_upgrade, Toast.LENGTH_SHORT).show();
                    }
                    mUpgradeInfoAdapter.setData(list);
                    mUpgradeInfoAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
            }
        });
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

    private void requestDownLoadZip(String zipUrl) {

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
            String serviceString = Context.DOWNLOAD_SERVICE;
            DownloadManager downloadManager;
            downloadManager = (DownloadManager) getSystemService(serviceString);

            Uri uri = Uri.parse(zipUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);

            if (!directoryFile.exists()) {
                directoryFile.mkdir();
            }
            File file = new File(path + "/" + fileName);
            request.setDestinationUri(Uri.fromFile(file));
            final long downloadReference = downloadManager.enqueue(request);

            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            downLoadReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (downloadReference == reference) {
                        targetDevice = targetDeviceList.get(0);
                        showProgressDialog();
                        connectDevice();
                    }
                }
            };
            registerReceiver(downLoadReceiver, filter);
        } else {
            targetDevice = targetDeviceList.get(0);
            showProgressDialog();
            connectDevice();
        }
    }

    public void dfuStart() {
        DfuServiceInitiator initiator = new DfuServiceInitiator(targetDevice.getMacAddress())
                .setDisableNotification(true)
                .setZip(zipFileString);
//                .setBinOrHex(DfuService.TYPE_APPLICATION, hexFileString)
//                .setInitFile(datFileString);
        initiator.start(this, DfuService.class);

    }

    private void showProgressDialog() {
        sensoroProgressDialog = new SensoroProgressDialog(this);
        sensoroProgressDialog.show();
        int progress = (int) (1.0 / targetDeviceList.size() * 100);
        sensoroProgressDialog.getFirstProgress().setProgress(progress);
        String title = getString(R.string.total_device) + "1" + "/" + targetDeviceList.size();
        sensoroProgressDialog.getFirstProgressTitle().setText(title);

    }

    private void connectDevice() {
        try {
            sensoroProgressDialog.getTitle().setText(getString(R.string.device) + targetDevice.getSn() + getString(R.string.connecting));
            sensoroConnection = new SensoroDeviceConnection(this, targetDevice.getMacAddress());
            sensoroConnection.connect(targetDevice.getPassword(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenDfu(boolean isSuccess) {
        if (deviceIndex == (targetDeviceList.size() - 1)) {
            requestUpdateDeviceUpgradeInfo();
            mHandler.postDelayed(mRunnable, 3000);
        } else {
            deviceIndex ++;
            targetDevice = targetDeviceList.get(deviceIndex);
            connectDevice();
        }
        int progress = (int) ((float)(deviceIndex + 1) / targetDeviceList.size() * 100);
        sensoroProgressDialog.getFirstProgress().setProgress(progress);
        String title = getString(R.string.total_device) + (deviceIndex + 1) + "/" + targetDeviceList.size();
        sensoroProgressDialog.getFirstProgressTitle().setText(title);
    }


    @Override
    public void onDeviceConnecting(String deviceAddress) {

    }

    @Override
    public void onDeviceConnected(String deviceAddress) {

    }

    @Override
    public void onDfuProcessStarting(String deviceAddress) {

    }

    @Override
    public void onDfuProcessStarted(String deviceAddress) {
       sensoroProgressDialog.getTitle().setText(getString(R.string.device) + targetDevice.getSn() + getString(R.string.upgrading));
    }

    @Override
    public void onEnablingDfuMode(String deviceAddress) {

    }

    @Override
    public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
        sensoroProgressDialog.getSecondProgress().setProgress(percent);

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
        sensoroProgressDialog.getTitle().setText(getString(R.string.device) + targetDevice.getSn() + getString(R.string.upgrade_finish));
        listenDfu(true);
    }

    @Override
    public void onDfuAborted(String deviceAddress) {
        sensoroProgressDialog.getSecondProgress().setIndeterminate(true);
    }


    @Override
    public void onError(String deviceAddress, int error, int errorType, String message) {
        if (error == 4102 && dfu_count <= DFU_MAX_COUNT) {// dfu service not found
            dfuStart();
            dfu_count++;
        } else {
            Toast.makeText(this, message , Toast.LENGTH_SHORT).show();
            sensoroProgressDialog.getSecondProgress().setProgress(0);
            listenDfu(false);
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
                sensoroProgressDialog.getTitle().setText(getString(R.string.device) + targetDevice.getSn() + getString(R.string.connect_success));
                sensoroConnection.writeCmd(UpgradeListActivity.this);
            }
        });
    }

    @Override
    public void onConnectedFailure(int errorCode) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sensoroProgressDialog.getTitle().setText(getString(R.string.device) + targetDevice.getSn() + getString(R.string.connect_failed));
                listenDfu(false);
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
                sensoroProgressDialog.getTitle().setText(getString(R.string.device) + targetDevice.getSn() + getString(R.string.write_success));
                dfuStart();

            }
        });
    }

    @Override
    public void onWriteFailure(int errorCode, final int cmd) {
        targetDevice.setDfu(false);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sensoroProgressDialog.getTitle().setText(getString(R.string.device) + targetDevice.getSn() + getString(R.string.write_failed));
                listenDfu(false);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        StatService.onResume(this);
        MobclickAgent.onResume(this);
        DfuServiceListenerHelper.registerProgressListener(this, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        StatService.onPause(this);
        MobclickAgent.onPause(this);
        DfuServiceListenerHelper.unregisterProgressListener(this, this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downLoadReceiver != null) {
            unregisterReceiver(downLoadReceiver);
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.upgrade_title_btn1:
                requestUpgradeInfo();
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        UpgradeInfo upgradeInfo = mUpgradeInfoAdapter.getData(position);
        firmwareVersionString = upgradeInfo.getVersion();
        snJSonArray = new JSONArray();
        requestDownLoadZip(upgradeInfo.getUrl());
    }

    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            deviceIndex = 0;
            sensoroProgressDialog.getTitle().setText(R.string.upgrade_finish);
            sensoroProgressDialog.dismiss();
        }
    };
}
