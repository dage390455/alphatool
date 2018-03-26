package com.sensoro.loratool.activity;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.fragment.SettingsSingleChoiceItemsFragment;
import com.sensoro.loratool.ble.BLEDevice;
import com.sensoro.loratool.ble.SensoroConnectionCallback;
import com.sensoro.loratool.ble.SensoroDeviceConnection;
import com.sensoro.loratool.ble.SensoroDevice;
import com.sensoro.loratool.ble.SensoroDeviceConfiguration;
import com.sensoro.loratool.ble.SensoroWriteCallback;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.event.OnPositiveButtonClickListener;
import com.sensoro.loratool.proto.ProtoMsgCfgV1U1;
import com.sensoro.loratool.store.DeviceDataDao;
import com.sensoro.loratool.utils.ParamUtil;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sensoro on 17/3/31.
 */

public class SettingMultiModuleActivity extends BaseActivity implements Constants, View.OnClickListener,SensoroConnectionCallback, SensoroWriteCallback, OnPositiveButtonClickListener {

    //transimit power
    private RelativeLayout loraIntRelativeLayout;
    private TextView loraTpIntervalTextView;
    private ImageView backImageView;
    private TextView saveTextView;

    private SensoroDeviceConnection sensoroDeviceConnection;
    private ArrayList<SensoroDevice> targetDeviceList = new ArrayList<>();
    private SensoroDevice sensoroDevice = null;
    private HashMap<String, Object> changeMap = new HashMap<>();

    private LoRaSettingApplication application;
    private SensoroDeviceConfiguration deviceConfiguration;
    private ProgressDialog progressDialog;

    private String[] loraTxpItems;
    private String band;
    private int loraTxp;
    private int targetIndex;
    private int writeLoraPowerIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        MobclickAgent.onPageStart("传输模块配置");
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void init() {
        band = getIntent().getStringExtra(Constants.EXTRA_NAME_BAND);
        targetDeviceList = this.getIntent().getParcelableArrayListExtra(Constants.EXTRA_NAME_DEVICE_LIST);
        application = (LoRaSettingApplication) getApplication();
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setMessage(this.getResources().getString(R.string.connecting));
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                SettingMultiModuleActivity.this.finish();
            }
        });
        writeLoraPowerIndex = SETTING_STATUS_UNSET;
        initLoraParam();
        initWidget();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_setting_multi_module;
    }

    public void initWidget() {
        setContentView(R.layout.activity_setting_multi_module);
        resetRootLayout();
        backImageView = (ImageView) findViewById(R.id.module_multi_device_back);
        backImageView.setOnClickListener(this);
        saveTextView = (TextView) findViewById(R.id.module_multi_tv_save);
        saveTextView.setOnClickListener(this);
        loraIntRelativeLayout = (RelativeLayout) findViewById(R.id.module_multi_rl_lora_transmit_power);
        loraIntRelativeLayout.setOnClickListener(this);
        loraTpIntervalTextView = (TextView) findViewById(R.id.module_multi_tv_lora_transmit_power);
        targetIndex = 0;
    }

    private void initLoraParam() {
        int txp_array[] = Constants.LORA_SE433_TXP;
        switch (band) {
            case Constants.LORA_BAND_US915:
                txp_array = Constants.LORA_US915_TXP;
                break;
            case Constants.LORA_BAND_SE433:
                txp_array = Constants.LORA_SE433_TXP;
                break;
            case Constants.LORA_BAND_SE470:
                txp_array = Constants.LORA_SE470_TXP;
                break;
            case Constants.LORA_BAND_SE780:
                txp_array = Constants.LORA_SE780_TXP;
                break;
            case Constants.LORA_BAND_SE915:
                txp_array = Constants.LORA_SE915_TXP;
                break;
            case Constants.LORA_BAND_AU915:
                txp_array = Constants.LORA_AU915_TXP;
                break;
            case Constants.LORA_BAND_AS923:
                txp_array = Constants.LORA_AS923_TXP;
                break;
            case Constants.LORA_BAND_EU433:
                txp_array = Constants.LORA_EU433_TXP;
                break;
            case Constants.LORA_BAND_EU868:
                txp_array = Constants.LORA_EU868_TXP;
                break;
        }
        loraTxpItems = new String[txp_array.length + 1];
        for (int i = 0; i < txp_array.length; i++) {
            if (i == 0) {
                loraTxpItems[i] = getString(R.string.setting_unset);
            } else {
                int txp = txp_array[i - 1];
                loraTxpItems[i] = txp + " dBm";
            }
        }
    }

    public void refresh() {
        if (sensoroDevice != null) {
            loraTpIntervalTextView.setText(String.valueOf(sensoroDevice.getLoraTxp()) + " dBm");
        }
    }

    public void connectDevice() {
        try {
            progressDialog.show();
            sensoroDeviceConnection = new SensoroDeviceConnection(this, sensoroDevice.getMacAddress());
            sensoroDeviceConnection.connect(sensoroDevice.getPassword(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onConnectedSuccess(final BLEDevice bleDevice, int cmd) {
        String sn = sensoroDevice.getSn();
        String firmwareVersion = sensoroDevice.getFirmwareVersion();
        sensoroDevice = (SensoroDevice) bleDevice;
        sensoroDevice.setFirmwareVersion(firmwareVersion);
        sensoroDevice.setSn(sn);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                saveBeaconConfiguration();
                progressDialog.setMessage(getString(R.string.module) + sensoroDevice.getSn() + getString(R.string.connect_success));
//                Toast.makeText(getApplicationContext(), getString(R.string.connect_success), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectedFailure(int errorCode) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMessage(getString(R.string.module) + sensoroDevice.getSn() + getString(R.string.connect_failed));
//                Toast.makeText(getApplicationContext(), getString(R.string.device) + targetDevice.getSn() + getString(R.string.connect_failed), Toast.LENGTH_SHORT).show();
                save();
            }
        });
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onWriteSuccess(Object o, int cmd) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMessage(getString(R.string.module) + sensoroDevice.getSn() + getString(R.string.save_succ));
//                Toast.makeText(getApplicationContext(), getString(R.string.device) + targetDevice.getSn() + getString(R.string.save_succ), Toast.LENGTH_SHORT).show();
                postUpdateData();
                save();
            }
        });

    }

    @Override
    public void onWriteFailure(int errorCode, final int cmd) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMessage(getString(R.string.module) + sensoroDevice.getSn() + getString(R.string.save_fail));
//                Toast.makeText(getApplicationContext(), getString(R.string.device) + targetDevice.getSn() + getString(R.string.save_fail), Toast.LENGTH_SHORT).show();
                save();
            }
        });

    }


    @Override
    public void onPositiveButtonClick(String tag, Bundle bundle) {
        if (tag.equals(SETTINGS_LORA_TXP)) {
            writeLoraPowerIndex = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            if (writeLoraPowerIndex == SETTING_STATUS_UNSET) {
                loraTpIntervalTextView.setText(R.string.setting_unset);
            } else {
                int index = writeLoraPowerIndex - 1;
                loraTxp = ParamUtil.getLoraTxp(band, index);
                loraTpIntervalTextView.setText(loraTxpItems[index]);
                changeMap.put(tag, loraTxp);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.module_multi_device_back:
                if (sensoroDeviceConnection != null) {
                    sensoroDeviceConnection.disconnect();
                    finish();
                }
                break;
            case R.id.module_multi_rl_lora_transmit_power:
                DialogFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(loraTxpItems, 0);
                dialogFragment.show(getFragmentManager(), SETTINGS_LORA_TXP);
                break;
            case R.id.module_multi_tv_save:
//                saveBeaconConfiguration();
                save();
                break;
        }
    }

    private void save() {
        try {
            if (sensoroDeviceConnection != null) {
                sensoroDeviceConnection.disconnect();
            }
            if (targetIndex < targetDeviceList.size()) {
                sensoroDevice = targetDeviceList.get(targetIndex);
                progressDialog.setTitle(getString(R.string.settings));
                progressDialog.setMessage(getString(R.string.device) + sensoroDevice.getSn() + getString(R.string.saving));
                progressDialog.show();
                connectDevice();
                targetIndex++;
            } else {
                progressDialog.dismiss();
                this.finish();
                Toast.makeText(getApplicationContext(), getString(R.string.save_finish), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void saveBeaconConfiguration() {
        progressDialog.setTitle(getString(R.string.settings));
        progressDialog.setMessage(getString(R.string.saving));
        progressDialog.show();
        if (writeLoraPowerIndex == SETTING_STATUS_UNSET) {
            loraTxp = sensoroDevice.getLoraTxp();
        }
        SensoroDeviceConfiguration.Builder builder = new SensoroDeviceConfiguration.Builder();
        builder.setIBeaconEnabled(sensoroDevice.isIBeaconEnabled())
                .setProximityUUID(sensoroDevice.getProximityUUID())
                .setMajor(sensoroDevice.getMajor())
                .setMinor(sensoroDevice.getMinor())
                .setBleTurnOnTime(sensoroDevice.getBleOnTime())
                .setBleTurnOffTime(sensoroDevice.getBleOffTime())
                .setBleInt(sensoroDevice.getBleInt())
                .setBleTxp(sensoroDevice.getBleTxp())
                .setLoraTxp(loraTxp)
                .setLoraInt(sensoroDevice.getLoraInt())
                .setAppEui(sensoroDevice.getAppEui())
                .setAppKey(sensoroDevice.getAppKey())
                .setAppSkey(sensoroDevice.getAppSkey())
                .setNwkSkey(sensoroDevice.getNwkSkey())
                .setDevAdr(sensoroDevice.getDevAdr())
                .setLoraAdr(sensoroDevice.getLoraAdr())
                .setLoraDr(sensoroDevice.getLoraDr())
                .setSensoroSlotArray(sensoroDevice.getSlotArray());
        if (sensoroDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_04) {
            builder.setClassBEnabled(sensoroDevice.getClassBEnabled())
                    .setClassBPeriodicity(sensoroDevice.getClassBPeriodicity())
                    .setClassBDataRate(sensoroDevice.getClassBDataRate());
        }
        if (sensoroDevice.getPassword() == null || (sensoroDevice.getPassword() != null && !sensoroDevice.getPassword().equals(""))) {
            builder.setPassword(sensoroDevice.getPassword());
        }
        deviceConfiguration = builder.build();
        try {
            sensoroDeviceConnection.writeModuleConfiguration(deviceConfiguration, this);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    protected void postUpdateData() {
        if (deviceConfiguration == null) {
            return;
        }
        String dataString = null;
        String version = null;
        ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();

        msgCfgBuilder.setLoraTxp(deviceConfiguration.getLoraTxp());
        ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();
        byte[] data = msgCfg.toByteArray();
        dataString = new String(Base64.encode(data, Base64.DEFAULT));
        version = "04";
        final String baseString = dataString;
        final String versionString = version;
        JSONObject jsonData = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try {
            jsonObject.put("SN", sensoroDevice.getSn());
            jsonObject.put("version", versionString);
            jsonObject.put("data", baseString);
            jsonArray.put(jsonObject);
            jsonData.put("devices", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        application.loRaSettingServer.updateDevices(jsonData.toString(), new Response.Listener<ResponseBase>() {
            @Override
            public void onResponse(ResponseBase responseBase) {
                if (responseBase.getErr_code() == 0) {
                    Log.i("SettingDeviceActivity", "====>update success");
                } else {
                    Log.i("SettingDeviceActivity", "====>update falied");
                    DeviceDataDao.addDeviceItem(sensoroDevice.getSn(), baseString, versionString);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                DeviceDataDao.addDeviceItem(sensoroDevice.getSn(), baseString, versionString);
            }
        });
    }
}