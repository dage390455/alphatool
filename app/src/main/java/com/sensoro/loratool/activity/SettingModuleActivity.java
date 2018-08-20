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
import com.sensoro.loratool.fragment.SettingsSingleChoiceItemsFragment;
import com.sensoro.libbleserver.ble.BLEDevice;
import com.sensoro.libbleserver.ble.SensoroConnectionCallback;
import com.sensoro.libbleserver.ble.SensoroDeviceConnection;
import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.libbleserver.ble.SensoroDeviceConfiguration;
import com.sensoro.libbleserver.ble.SensoroWriteCallback;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.event.OnPositiveButtonClickListener;
import com.sensoro.libbleserver.ble.proto.ProtoMsgCfgV1U1;
import com.sensoro.loratool.store.DeviceDataDao;
import com.sensoro.loratool.utils.ParamUtil;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sensoro on 17/3/31.
 */

public class SettingModuleActivity extends BaseActivity implements Constants, View.OnClickListener,
        SensoroConnectionCallback, SensoroWriteCallback, OnPositiveButtonClickListener {
    //transimit power
    @BindView(R.id.module_rl_lora_transmit_power)
    RelativeLayout loraTxpRelativeLayout;
    @BindView(R.id.module_tv_lora_txp)
    TextView loraTxpTextView;

    @BindView(R.id.module_device_back)
    ImageView backImageView;
    @BindView(R.id.module_tv_save)
    TextView saveTextView;

    private SensoroDeviceConnection sensoroDeviceConnection;
    private SensoroDevice sensoroDevice = null;
    private String band = null;
    private HashMap<String, Object> changeMap = new HashMap<>();

    private LoRaSettingApplication application;
    private SensoroDeviceConfiguration deviceConfiguration;
    private ProgressDialog progressDialog;
    private String[] loraTxpItems;
    private int loraTxp;

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
        sensoroDevice = getIntent().getParcelableExtra(Constants.EXTRA_NAME_DEVICE);
        band = getIntent().getStringExtra(Constants.EXTRA_NAME_BAND);
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
                SettingModuleActivity.this.finish();
            }
        });
        initLoraParam();
        connectDevice();
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
        loraTxpItems = new String[txp_array.length];
        for (int i = 0; i < txp_array.length; i++) {
            int txp = txp_array[i];
            loraTxpItems[i] = txp + " dBm";
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_setting_module;
    }

    public void registerUiEvent() {
        setContentView(R.layout.activity_setting_module);
        ButterKnife.bind(this);
        resetRootLayout();
        backImageView.setOnClickListener(this);
        saveTextView.setOnClickListener(this);
        loraTxpRelativeLayout.setOnClickListener(this);
    }

    public void refresh() {
        if (sensoroDevice != null) {
            loraTxp = sensoroDevice.getLoraTxp();
            loraTxpTextView.setText(loraTxp + " dBm");
        }
        Toast.makeText(getApplicationContext(), getString(R.string.connect_success), Toast.LENGTH_SHORT).show();
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
    public void onConnectedSuccess(BLEDevice bleDevice, int cmd) {
        String sn = sensoroDevice.getSn();
        String firmwareVersion = sensoroDevice.getFirmwareVersion();
        sensoroDevice = (SensoroDevice) bleDevice;
        sensoroDevice.setFirmwareVersion(firmwareVersion);
        sensoroDevice.setSn(sn);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                registerUiEvent();
                refresh();
            }
        });
    }

    @Override
    public void onConnectedFailure(int errorCode) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getString(R.string.connect_failed), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                SettingModuleActivity.this.finish();
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
                postUpdateData();
                Toast.makeText(getApplicationContext(), getString(R.string.save_succ), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                SettingModuleActivity.this.finish();
            }
        });

    }


    @Override
    public void onWriteFailure(int errorCode, final int cmd) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getString(R.string.save_fail), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

    }


    @Override
    public void onPositiveButtonClick(String tag, Bundle bundle) {
        if (tag.equals(SETTINGS_LORA_TXP)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
//            loraTxp = getLoraTxp(index);
            loraTxp = ParamUtil.getLoraTxp(band, index);
            loraTxpTextView.setText(loraTxpItems[index]);
            changeMap.put(tag, loraTxp);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.module_device_back:
                if (sensoroDeviceConnection != null) {
                    sensoroDeviceConnection.disconnect();
                    finish();
                }
                break;
            case R.id.module_rl_lora_transmit_power:
//                DialogFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(loraTxpItems,
// getLoraTxpIndex(sensoroDevice.getLoraTxp()));
                DialogFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(loraTxpItems, ParamUtil
                        .getLoraTxpIndex(band, loraTxp));
                dialogFragment.show(getFragmentManager(), SETTINGS_LORA_TXP);
                break;
            case R.id.module_tv_save:
                saveBeaconConfiguration();
                break;
        }
    }

    private void saveBeaconConfiguration() {
        progressDialog.setTitle(getString(R.string.settings));
        progressDialog.setMessage(getString(R.string.saving));
        progressDialog.show();
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
        if (sensoroDevice.getPassword() == null || (sensoroDevice.getPassword() != null && !sensoroDevice.getPassword
                ().equals(""))) {
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
//        msgCfgBuilder.setBleTxp(deviceConfiguration.getBleTxp());
//        msgCfgBuilder.setBleInt(deviceConfiguration.getBleInt().intValue());
//        msgCfgBuilder.setBleOnTime(deviceConfiguration.getBleTurnOnTime());
//        msgCfgBuilder.setBleOffTime(deviceConfiguration.getBleTurnOffTime());
//
//        msgCfgBuilder.setHumiInt(deviceConfiguration.getHumidityInterval());
//        msgCfgBuilder.setTempInt(deviceConfiguration.getTemperatureInterval());
//        msgCfgBuilder.setLightInt(deviceConfiguration.getLightInterval());
//        msgCfgBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getAppEui()))));
//        msgCfgBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppKey())));
//        msgCfgBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppSkey())));
//        msgCfgBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getNwkSkey())));
//        msgCfgBuilder.setDevAddr(deviceConfiguration.getDevAdr());
//        msgCfgBuilder.setLoraDr((12 - Integer.valueOf(deviceConfiguration.getLoraSf())));
//        msgCfgBuilder.setLoraAdr(deviceConfiguration.getLoraAdr());
//
//        SensoroSlot[] sensoroSlots = deviceConfiguration.getSensoroSlots();
//        for (int i = 0; i < sensoroSlots.length; i++) {
//            ProtoMsgCfgV1U1.Slot.Builder builder = ProtoMsgCfgV1U1.Slot.newBuilder();
//            SensoroSlot sensoroSlot = sensoroSlots[i];
//            if (sensoroSlot.isActived() == 1) {
//                if (i == 4) {
//                    byte uuid_data[] = SensoroUtils.HexString2Bytes(deviceConfiguration.getProximityUUID());
//                    byte major_data[] = SensoroUUID.intToByteArray(deviceConfiguration.getMajor(), 2);
//                    byte minor_data[] = SensoroUUID.intToByteArray(deviceConfiguration.getMinor(), 2);
//                    byte ibeacon_data[] = new byte[20];
//                    System.arraycopy(uuid_data, 0, ibeacon_data, 0, 16);
//                    System.arraycopy(major_data, 0, ibeacon_data, 16, 2);
//                    System.arraycopy(minor_data, 0, ibeacon_data, 18, 2);
//                    builder.setFrame(ByteString.copyFrom(ibeacon_data));
//                } else if (i == 5 || i == 6 || i == 7) {
//                    String frameString = sensoroSlot.getFrame();
//                    if (frameString != null) {
//                        builder.setFrame(ByteString.copyFrom(SensoroUtils.HexString2Bytes(frameString)));
//                    }
//
//                } else {
//                    switch (sensoroSlot.getType()) {
//                        case ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_URL_VALUE:
//                            builder.setFrame(ByteString.copyFrom(SensoroUtils.encodeUrl(sensoroSlot.getFrame())));
//                            break;
//                        default:
//                            builder.setFrame(ByteString.copyFrom(SensoroUtils.HexString2Bytes(sensoroSlot.getFrame
// ())));
//                            break;
//                    }
//                }
//                builder.setIndex(i);
//                builder.setType(ProtoMsgCfgV1U1.SlotType.valueOf(sensoroSlot.getType()));
//            }
//            builder.setActived(sensoroSlot.isActived());
//            msgCfgBuilder.addSlot(i, builder.build());
//        }
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