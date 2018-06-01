package com.sensoro.loratool.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.protobuf.ByteString;
import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.fragment.SettingsInputDialogFragment;
import com.sensoro.loratool.activity.fragment.SettingsMultiChoiceItemsFragment;
import com.sensoro.loratool.activity.fragment.SettingsSingleChoiceItemsFragment;
import com.sensoro.loratool.ble.BLEDevice;
import com.sensoro.loratool.ble.SensoroConnectionCallback;
import com.sensoro.loratool.ble.SensoroDevice;
import com.sensoro.loratool.ble.SensoroDeviceConfiguration;
import com.sensoro.loratool.ble.SensoroDeviceConnection;
import com.sensoro.loratool.ble.SensoroUtils;
import com.sensoro.loratool.ble.SensoroWriteCallback;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.event.OnPositiveButtonClickListener;
import com.sensoro.loratool.model.ChannelData;
import com.sensoro.loratool.proto.MsgNode1V1M5;
import com.sensoro.loratool.proto.ProtoMsgCfgV1U1;
import com.sensoro.loratool.proto.ProtoStd1U1;
import com.sensoro.loratool.store.DeviceDataDao;
import com.sensoro.loratool.utils.ParamUtil;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by sensoro on 16/10/11.
 */

public class AdvanceSettingMultiDeviceActivity extends BaseActivity implements Constants, OnPositiveButtonClickListener,  SensoroConnectionCallback, SensoroWriteCallback {

    private SensoroDeviceConnection sensoroDeviceConnection;
    private SensoroDevice targetDevice = null;
    private DeviceInfo deviceInfo = null;
    private ArrayList<SensoroDevice> targetDeviceList = new ArrayList<>();
    private int targetDeviceIndex = 0;
    private ProgressDialog progressDialog;
    //DEV EUI
    @BindView(R.id.settings_multi_rl_dev_eui)
    RelativeLayout devEuiRelativeLayout;
    @BindView(R.id.settings_multi_tv_dev_eui_value)
    TextView devEuiTextView;
    //APP EUI
    @BindView(R.id.settings_multi_rl_app_eui)
    RelativeLayout appEuiRelativeLayout;
    @BindView(R.id.settings_multi_tv_eui_value)
    TextView appEuiTextView;
    //app key
    @BindView(R.id.settings_multi_rl_appkey)
    RelativeLayout appKeyRelativeLayout;
    @BindView(R.id.settings_multi_tv_appkey_value)
    TextView appKeyTextView;
    //app session key
    @BindView(R.id.settings_multi_rl_appsessionkey)
    RelativeLayout appSessionKeyRelativeLayout;
    @BindView(R.id.settings_multi_tv_appskey)
    TextView appSessionKeyTextView;
    //network session key
    @BindView(R.id.settings_multi_rl_nsk)
    RelativeLayout nwkSessionKeyRelativeLayout;
    @BindView(R.id.settings_multi_tv_nsk_value)
    TextView nwkSessionKeyTextView;
    //network address
    @BindView(R.id.settings_multi_rl_nwk_address)
    RelativeLayout nwkAddressRelativeLayout;
    @BindView(R.id.settings_multi_tv_devaddr)
    TextView nwkAddressTextView;
    //sf
    @BindView(R.id.settings_multi_rl_sf)
    RelativeLayout loraSfRelativeLayout;
    @BindView(R.id.settings_multi_tv_sf_value)
    TextView loraSfTextView;
    //auto data rate
    @BindView(R.id.settings_multi_rl_data_rate)
    RelativeLayout dataRateRelativeLayout;
    @BindView(R.id.settings_multi_tv_data_rate_state)
    TextView dataRateTextView;

    @BindView(R.id.settings_multi_tv_ad_device_save)
    TextView advSaveTextView;

    @BindView(R.id.settings_multi_ll_classB)
    LinearLayout classBLinearLayout;
    @BindView(R.id.settings_multi_sc_classB_enable)
    TextView classBEnableTextView;
    @BindView(R.id.settings_multi_tv_classB_datarate)
    TextView classBDatarateTextView;
    @BindView(R.id.settings_multi_tv_classB_periodicity)
    TextView classBPeriodicityTextView;
    @BindView(R.id.settings_multi_rl_activation)
    RelativeLayout activationRelativeLayout;
    @BindView(R.id.settings_multi_tv_activation)
    TextView activationTextView;

    @BindView(R.id.settings_multi_ll_delay)
    LinearLayout delayLinearLayout;
    @BindView(R.id.settings_multi_rl_delay)
    RelativeLayout delayRelativeLayout;
    @BindView(R.id.settings_multi_tv_delay)
    TextView delayTextView;

    @BindView(R.id.settings_multi_ll_channel)
    LinearLayout channelMaskLinearLayout;
    @BindView(R.id.settings_multi_tv_channel)
    TextView channelTextView;

    private ArrayList<ChannelData> channelOpenList = new ArrayList<>();
    private int writeActivationIndex = SETTING_STATUS_UNSET;
    private int writeDataRateIndex = SETTING_STATUS_UNSET;
    private int writeClassBEnableIndex = SETTING_STATUS_UNSET;
    private int activation = 0;
    private int loraDr = 0;
    private int delay = 1;
    private String band;
    private String deviceType;

    private SensoroDeviceConfiguration deviceConfiguration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onPageStart("设备私有云配置V1.1");
        initData();
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

    private void initData() {
        targetDeviceList = getIntent().getParcelableArrayListExtra(Constants.EXTRA_NAME_DEVICE_LIST);
        targetDevice = targetDeviceList.get(targetDeviceIndex);
        band = getIntent().getStringExtra(Constants.EXTRA_NAME_BAND);
        deviceType = getIntent().getStringExtra(Constants.EXTRA_NAME_DEVICE_TYPE);
        deviceInfo = getIntent().getParcelableExtra(EXTRA_NAME_DEVICE_INFO);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                AdvanceSettingMultiDeviceActivity.this.finish();
            }
        });

        connectDevice();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_advcance_multi_device;
    }

    private void refresh() {
        if (targetDeviceIndex == 0) {
            progressDialog.dismiss();
            setContentView(R.layout.activity_advcance_multi_device);
            ButterKnife.bind(this);
            resetRootLayout();
            if (targetDevice.hasActivation()) {
                doActivationEvent(targetDevice.getActivation());
                activationRelativeLayout.setVisibility(View.VISIBLE);
            } else {
                activationRelativeLayout.setVisibility(View.GONE);
            }
            if (targetDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_04) {
                classBLinearLayout.setVisibility(View.VISIBLE);
            } else {
                classBLinearLayout.setVisibility(View.GONE);
            }
            if (deviceInfo.getDeviceType().equalsIgnoreCase("chip") && (deviceInfo.getFirmwareVersion().equalsIgnoreCase("2.0")||deviceInfo.getFirmwareVersion().equalsIgnoreCase("2.0.0"))) {
                delayLinearLayout.setVisibility(GONE);
            } else {
                delay = targetDevice.getDelay();
                if (targetDevice.hasDelay()) {
                    delayLinearLayout.setVisibility(VISIBLE);
                    delayTextView.setText(String.valueOf(targetDevice.getDelay()));
                } else {
                    delayLinearLayout.setVisibility(GONE);
                }
            }

            if (deviceType != null) {
                if (deviceInfo.isOpenLoraDevice()) {
                    if (targetDevice.getLoraAdr() == 1 ) {
                        loraSfRelativeLayout.setVisibility(GONE);
                    } else {
                        loraSfRelativeLayout.setVisibility(VISIBLE);
                    }
                } else {
                    devEuiRelativeLayout.setVisibility(View.GONE);
                }
            }

            if (targetDevice.getChannelMaskList() == null) {
                channelMaskLinearLayout.setVisibility(GONE);
            } else {
                List<Integer> list = targetDevice.getChannelMaskList();
                for (int i = 0; i < list.size(); i++) {
                    for (int j =0 ; j < 16; j ++) {
                        ChannelData channelData = new ChannelData();
                        channelData.setIndex((i*16 + j));
                        if ((list.get(i) & 1 << j) !=0) {
                            channelData.setOpen(true);
                            System.out.println("open==>"+(i*16 + j));
                        } else {
                            channelData.setOpen(false);
                            System.out.println("close==>"+(i*16 + j));
                        }
                        channelOpenList.add(channelData);
                    }
                }
                if (channelOpenList.size() == 0) {
                    channelMaskLinearLayout.setVisibility(GONE);
                } else {
                    channelMaskLinearLayout.setVisibility(VISIBLE);
                }
            }
        } else {
            progressDialog.show();
            progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string.connect_success));
            saveConfiguration();
        }

    }

    public void doActivationEvent(int index) {
        if (index == 0) {
            appSessionKeyRelativeLayout.setVisibility(VISIBLE);
            nwkSessionKeyRelativeLayout.setVisibility(VISIBLE);
            nwkAddressRelativeLayout.setVisibility(VISIBLE);
        } else {
            appSessionKeyRelativeLayout.setVisibility(GONE);
            nwkSessionKeyRelativeLayout.setVisibility(GONE);
            nwkAddressRelativeLayout.setVisibility(GONE);
        }
    }


    public void connectDevice() {
        try {
            if (targetDeviceIndex != 0) {
                progressDialog.setTitle(getString(R.string.settings));
                progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string.saving));
            } else {
                progressDialog.setMessage(getString(R.string.connecting));
            }
            progressDialog.show();
            sensoroDeviceConnection = new SensoroDeviceConnection(this, targetDevice.getMacAddress());
            sensoroDeviceConnection.connect(targetDevice.getPassword(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int getClassBPeridicity(int value) {
        switch (value) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 4:
                return 2;
            case 8:
                return 3;
            case 16:
                return 4;
            case 32:
                return 5;
            case 64:
                return 6;
            case 128:
                return 7;
        }
        return 0;
    }

    public int getClassBDataRate(int value) {
        switch (value) {
            case 12:
                return 0;
            case 11:
                return 1;
            case 10:
                return 2;
            case 9:
                return 3;
            case 8:
                return 4;
            case 7:
                return 5;
        }
        return 0;
    }

    protected void postUpdateData() {
        if (deviceConfiguration == null) {
            return;
        }
        String baseString = null;
        String version = "03";
        ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();
        msgCfgBuilder.setLoraInt((int)targetDevice.getLoraInt());
        msgCfgBuilder.setLoraTxp(targetDevice.getLoraTxp());
        msgCfgBuilder.setBleTxp(targetDevice.getBleTxp());
        msgCfgBuilder.setBleInt((int)targetDevice.getBleInt());
        msgCfgBuilder.setBleOnTime(targetDevice.getBleOnTime());
        msgCfgBuilder.setBleOffTime(targetDevice.getBleOffTime());

        if (targetDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_03) {
            msgCfgBuilder.setDevEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getDevEui()))));
            msgCfgBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getAppEui()))));
            msgCfgBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppKey())));
            msgCfgBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppSkey())));
            msgCfgBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getNwkSkey())));
            msgCfgBuilder.setDevAddr(deviceConfiguration.getDevAdr());
            msgCfgBuilder.setLoraDr((deviceConfiguration.getLoraDr()));
            msgCfgBuilder.setLoraAdr(deviceConfiguration.getLoraAdr());
            ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();
            byte[] data = msgCfg.toByteArray();
            baseString = new String(Base64.encode(data, Base64.DEFAULT));
            version = "03";
        } else if (targetDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_04){
            msgCfgBuilder.setDevEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getDevEui()))));
            msgCfgBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getAppEui()))));
            msgCfgBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppKey())));
            msgCfgBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppSkey())));
            msgCfgBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getNwkSkey())));
            msgCfgBuilder.setDevAddr(deviceConfiguration.getDevAdr());
            msgCfgBuilder.setLoraDr((deviceConfiguration.getLoraDr()));
            msgCfgBuilder.setLoraAdr(deviceConfiguration.getLoraAdr());
            ProtoStd1U1.MsgStd.Builder msgStdBuilder = ProtoStd1U1.MsgStd.newBuilder();
            msgStdBuilder.setCustomData(msgCfgBuilder.build().toByteString());
            msgStdBuilder.setEnableClassB(deviceConfiguration.getClassBEnabled());
            msgStdBuilder.setClassBDataRate(deviceConfiguration.getClassDateRate());
            msgStdBuilder.setClassBPeriodicity(deviceConfiguration.getClassPeriodicity());
            ProtoStd1U1.MsgStd msgStd = msgStdBuilder.build();

            byte[] data = msgStd.toByteArray();
            baseString = new String(Base64.encode(data, Base64.DEFAULT));
            version = "04";
        } else {
            MsgNode1V1M5.MsgNode.Builder msgNodeBuilder = MsgNode1V1M5.MsgNode.newBuilder();
            if (targetDevice.hasLoraParam()) {
                MsgNode1V1M5.LoraParam.Builder loraParamBuilder = MsgNode1V1M5.LoraParam.newBuilder();
                loraParamBuilder.setTxPower(targetDevice.getLoraTxp());
                loraParamBuilder.setDevEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getDevEui()))));
                loraParamBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getAppEui()))));
                loraParamBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppKey())));
                loraParamBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppSkey())));
                loraParamBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getNwkSkey())));
                loraParamBuilder.setDevAddr(deviceConfiguration.getDevAdr());
                loraParamBuilder.setAdr(deviceConfiguration.getLoraAdr());
                loraParamBuilder.setDatarate(deviceConfiguration.getLoraDr());
                if (targetDevice.hasActivation()) {
                    loraParamBuilder.setActivition(MsgNode1V1M5.Activtion.valueOf(activation));
                }
                if (deviceInfo.getDeviceType().equalsIgnoreCase("chip") && (deviceInfo.getFirmwareVersion().equalsIgnoreCase("2.0")||deviceInfo.getFirmwareVersion().equalsIgnoreCase("2.0.0"))) {

                } else {
                    if (targetDevice.hasDelay()) {
                        loraParamBuilder.setDelay(deviceConfiguration.getDelay());
                    }
                }
                msgNodeBuilder.setLoraParam(loraParamBuilder);
            }

            byte[] data = msgNodeBuilder.build().toByteArray();
            baseString = new String(Base64.encode(data, Base64.DEFAULT));
            version = "05";
        }

        final String dataString = baseString;
        JSONObject jsonData = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        final String versionString = version;
        try {
            jsonObject.put("SN", targetDevice.getSn());
            jsonObject.put("version", version);
            jsonObject.put("data", baseString);
            jsonArray.put(jsonObject);
            jsonData.put("devices", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LoRaSettingApplication application = (LoRaSettingApplication) getApplication();
        application.loRaSettingServer.updateDevices(jsonData.toString(), new Response.Listener<ResponseBase>() {
            @Override
            public void onResponse(ResponseBase responseBase) {
                if (responseBase.getErr_code() == 0) {
                } else {
                    DeviceDataDao.addDeviceItem(targetDevice.getSn(), dataString, versionString);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                DeviceDataDao.addDeviceItem(targetDevice.getSn(), dataString, versionString);
            }
        });
    }

    @OnClick(R.id.settings_multi_ad_device_back)
    public void back() {
        this.finish();
    }

    @Override
    public void onConnectedSuccess(final BLEDevice bleDevice, int cmd) {
        String sn = targetDevice.getSn();
        String firmwareVersion = targetDevice.getFirmwareVersion();
        this.targetDevice = (SensoroDevice) bleDevice;
        targetDevice.setFirmwareVersion(firmwareVersion);
        targetDevice.setSn(sn);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refresh();
                progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string.connect_success));
//                Toast.makeText(getApplicationContext(), getString(R.string.device) + targetDevice.getSn() + getString(R.string.connect_success), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnectedFailure(int errorCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (targetDeviceIndex != 0) {
                    progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string.connect_failed));
                    save();
                } else {
                    Toast.makeText(AdvanceSettingMultiDeviceActivity.this, R.string.connect_failed, Toast.LENGTH_SHORT).show();
                    AdvanceSettingMultiDeviceActivity.this.finish();
                }
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
                progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string.save_succ));
//                Toast.makeText(getApplicationContext(), getString(R.string.device) + targetDevice.getSn() + getString(R.string.save_succ), Toast.LENGTH_SHORT).show();
                save();
            }
        });
    }

    @Override
    public void onWriteFailure(final int errorCode, final int cmd) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string.save_fail) + " 错误码" + errorCode);
//                Toast.makeText(getApplicationContext(), getString(R.string.device) + targetDevice.getSn() + getString(R.string.save_fail), Toast.LENGTH_SHORT).show();
                save();
            }
        });
    }

    @OnClick(R.id.settings_device_rl_dev_eui)
    public void doDevEui() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(devEuiTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_DEV_EUI);
    }

    @OnClick(R.id.settings_multi_rl_app_eui)
    public void doAppEui() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(appEuiTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_APP_EUI);
    }

    @OnClick(R.id.settings_multi_rl_appkey)
    public void doAppKey() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(appKeyTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_APP_KEY);
    }

    @OnClick(R.id.settings_multi_rl_appsessionkey)
    public void doAppSessionKey() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(appSessionKeyTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_APP_SESSION_KEY);
    }

    @OnClick(R.id.settings_multi_rl_nsk)
    public void doNwkSessionKey() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(nwkSessionKeyTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_NWK_SESSION_KEY);
    }

    @OnClick(R.id.settings_multi_rl_nwk_address)
    public void doNwkAddress() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(nwkAddressTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_NWK_ADDRESS);
    }

    @OnClick(R.id.settings_multi_rl_sf)
    public void doSf() {
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(SF_ITEMS, 0);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_SF);
    }

    @OnClick(R.id.settings_multi_rl_delay)
    public void doDelay() {
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(DELAY_ITEMS, 0);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_DELAY);
    }

    @OnClick(R.id.settings_multi_rl_channel)
    public void doChannel() {
        SettingsMultiChoiceItemsFragment dialogFragment = SettingsMultiChoiceItemsFragment.newInstance(channelOpenList);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_CHANNEL);
    }

    @OnClick(R.id.settings_multi_rl_data_rate)
    public void doDataRate() {
        String array[] = getResources().getStringArray(R.array.multi_status_array);
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(array, 0);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_DATARATE);
    }

    @OnClick(R.id.settings_multi_rl_activation)
    public void doActivation() {
        String activationArray[] = this.getResources().getStringArray(R.array.multi_activation_array);
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(activationArray, 0);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_ACTIVATION);
    }

    @OnClick(R.id.settings_multi_rl_classB_enable)
    public void doClassBStatus() {
        String array[] = getResources().getStringArray(R.array.multi_status_array);
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(array, 0);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_CLASS_ENALBLE);
    }

    @OnClick(R.id.settings_multi_rl_classB_datarate)
    public void doClassBDataRate() {
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(CLASSB_DATARATE, 0);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_CLASSB_DATARATE);
    }

    @OnClick(R.id.settings_multi_rl_classB_periodicity)
    public void doClassBPeriodicity() {

        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(CLASSB_PERIODICITY, 0);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_CLASSB_PERIODICITY);
    }

    @OnClick(R.id.settings_multi_tv_ad_device_save)
    public void saveClick() {
        if (targetDeviceIndex == 0) {
            progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string.saving));
            progressDialog.show();
            saveConfiguration();
        } else {
            save();
        }
    }


    public void save() {
        try {
            if (sensoroDeviceConnection != null) {
                sensoroDeviceConnection.disconnect();
            }
            if (targetDeviceIndex < (targetDeviceList.size() - 1)) {
                targetDeviceIndex++;
                targetDevice = targetDeviceList.get(targetDeviceIndex);
                connectDevice();
            } else {
                progressDialog.dismiss();
                AdvanceSettingMultiDeviceActivity.this.finish();
                Toast.makeText(getApplicationContext(), getString(R.string.save_finish), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveConfiguration() {
        try {
            String unsetString = getString(R.string.setting_unset);
            String devEui = devEuiTextView.getText().toString();
            if (devEui.equals("") || devEui.equals(unsetString)) {
                devEui = targetDevice.getDevEui();
            }
            String appEui = appEuiTextView.getText().toString();
            if (appEui.equals("") || appEui.equals(unsetString)) {
                appEui = targetDevice.getAppEui();
            }
            String appKey = appKeyTextView.getText().toString();
            if (appKey.equals("") || appKey.equals(unsetString)) {
                appKey = targetDevice.getAppKey();
            }
            String appsKey = appSessionKeyTextView.getText().toString();
            if (appsKey.equals("") || appsKey.equals(unsetString)) {
                appsKey = targetDevice.getAppSkey();
            }
            String nwkSessionKey = nwkSessionKeyTextView.getText().toString();
            if (nwkSessionKey.equals("") || nwkSessionKey.equals(unsetString)) {
                nwkSessionKey = targetDevice.getNwkSkey();
            }
            int devAdr = targetDevice.getDevAdr();
            String devAdrString = nwkAddressTextView.getText().toString();
            if (!(devAdrString.equals("") || devAdrString.equals(unsetString))) {
                devAdr = Integer.parseInt(devAdrString.replace("0x0", ""), 16);
            }
            int dataRate = 1;
            if (writeDataRateIndex != SETTING_STATUS_UNSET) {
                dataRate = writeDataRateIndex == 1? 1: 0;
            }
            SensoroDeviceConfiguration.Builder builder = new SensoroDeviceConfiguration.Builder();
            builder.setLoraAdr(dataRate);

            if (targetDevice.hasActivation()) {
                builder.setActivation(activation);
                builder.setHasActivation(true);
            } else {
                builder.setHasActivation(false);
            }
            if (targetDevice.hasDevEui()) {
                builder.setHasDevEui(true);
                builder.setDevEui(devEui);
            } else {
                builder.setHasDevEui(false);
            }
            if (targetDevice.hasAppEui()) {
                builder.setAppEui(appEui);
                builder.setHasAppEui(true);
            } else {
                builder.setHasAppEui(false);
            }
            if (targetDevice.hasAppKey()) {
                builder.setAppKey(appKey);
                builder.setHasAppKey(true);
            } else {
                builder.setHasAppKey(false);
            }
            if (targetDevice.hasAppSkey()) {
                builder.setAppSkey(appsKey);
                builder.setHasAppSkey(true);
            } else {
                builder.setHasAppSkey(false);
            }
            if (targetDevice.hasNwkSkey() || activation == 1) {
                builder.setNwkSkey(nwkSessionKey);
                builder.setHasNwkSkey(true);
            } else {
                builder.setHasNwkSkey(false);
            }
            if (targetDevice.hasDevAddr() || activation == 1) {
                builder.setHasDevAddr(true);
                builder.setDevAdr(devAdr);
            } else {
                builder.setHasDevAddr(false);
            }
            if (targetDevice.hasActivation() || activation == 1) {
                builder.setActivation(activation);
                builder.setHasActivation(true);
            } else {
                builder.setHasActivation(false);
            }
            builder.setLoraDr(loraDr);
            if (targetDevice.hasDelay()) {
                builder.setDelay(delay);
                builder.setHasDelay(true);
            } else {
                builder.setHasDelay(false);
            }
            List<Integer> list = targetDevice.getChannelMaskList();
            int array[] = new int[list.size()];
            for (int i = 0; i < list.size() * 16; i++) {
                ChannelData channelData = channelOpenList.get(i);
                if (channelData.isOpen()) {
                    array[i/16] |= 1 << i%16;
                } else {
                    array[i/16] |= 0 << i%16;
                }
            }
            ArrayList<Integer> tempList = new ArrayList();
            for (int i = 0 ; i < array.length; i ++) {
                tempList.add(array[i]);
            }
            builder.setChannelList(tempList);
            if (targetDevice.getFirmwareVersion().compareTo("1.3") >= 0) {
                if (writeClassBEnableIndex != SETTING_STATUS_UNSET) {
                    String classBPeriodicityString = classBPeriodicityTextView.getText().toString();
                    int classBPeriodicity = 0;
                    if (classBPeriodicityString.equals("") || classBPeriodicityString.equals(unsetString)) {
                        classBPeriodicity = targetDevice.getClassBPeriodicity();
                    }else {
                        classBPeriodicity = Integer.parseInt(classBPeriodicityString.replace("s", ""));
                    }
                    String classBDatarateString  = classBDatarateTextView.getText().toString();
                    int classBDataRate = 0;
                    if (classBDatarateString.equals("") || classBDatarateString.equals(unsetString)) {
                        classBDataRate = targetDevice.getClassBDataRate();
                    } else {
                        classBDataRate = Integer.parseInt(classBDatarateString);
                    }
                    builder.setClassBEnabled(writeClassBEnableIndex == 1 ? 1 : 0)
                            .setClassBPeriodicity(getClassBPeridicity(classBPeriodicity))
                            .setClassBDataRate(getClassBDataRate(classBDataRate));
                }

            }
            deviceConfiguration = builder.build();
            sensoroDeviceConnection.writeDeviceAdvanceConfiguration(deviceConfiguration, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPositiveButtonClick(String tag, Bundle bundle) {
        if (tag.equals(SETTINGS_DEVICE_APP_EUI)) {
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
            String regString = "[a-f0-9A-F]{16}";
            if (text.length() == 16) {
                if (!Pattern.matches(regString, text)) {
                    SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(appEuiTextView.getText().toString());
                    dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_APP_EUI);
                    Toast.makeText(this, R.string.invaild_eui, Toast.LENGTH_SHORT).show();
                } else {
                    appEuiTextView.setText(text);
                }
            } else {
                Toast.makeText(this, R.string.data_invalid, Toast.LENGTH_SHORT).show();
            }


        } else if (tag.equals(SETTINGS_DEVICE_DEV_EUI)) {
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
            String regString = "[a-f0-9A-F]{16}";
            if (text.length() == 16) {
                if (!Pattern.matches(regString, text)) {
                    Toast.makeText(this, R.string.invaild_eui, Toast.LENGTH_SHORT).show();
                } else {
                    devEuiTextView.setText(text);
                }
            } else {
                Toast.makeText(this, R.string.data_invalid, Toast.LENGTH_SHORT).show();
            }

        } else if (tag.equals(SETTINGS_DEVICE_APP_KEY)) {
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
            String regString = "[a-f0-9A-F]{32}";
            if (text.length() == 32) {
                if (!Pattern.matches(regString, text)) {
                    SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(appKeyTextView.getText().toString());
                    dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_APP_KEY);
                    Toast.makeText(this, R.string.invaild_app_key, Toast.LENGTH_SHORT).show();
                } else {
                    appKeyTextView.setText(text);
                }
            } else {
                Toast.makeText(this, R.string.data_invalid, Toast.LENGTH_SHORT).show();
            }

        } else if (tag.equals(SETTINGS_DEVICE_APP_SESSION_KEY)) {
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
            if (text.length() == 32) {
                appSessionKeyTextView.setText(text);
            } else {
                Toast.makeText(this, R.string.data_invalid, Toast.LENGTH_SHORT).show();
            }

        } else if (tag.equals(SETTINGS_DEVICE_NWK_SESSION_KEY)) {
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
            if (text.length() == 32) {
                nwkSessionKeyTextView.setText(text);
            } else {
                Toast.makeText(this, R.string.data_invalid, Toast.LENGTH_SHORT).show();
            }

        } else if (tag.equals(SETTINGS_DEVICE_NWK_ADDRESS)) {
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
            if (text.length() == 8) {
                nwkAddressTextView.setText(text);
            } else {
                Toast.makeText(this, R.string.data_invalid, Toast.LENGTH_SHORT).show();
            }

        } else if (tag.equals(SETTINGS_DEVICE_SF)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            if (index == 0) {
                loraDr = targetDevice.getLoraDr();
            } else {
                loraDr = ParamUtil.getLoraDr(band, index);
            }
            String item = SF_ITEMS[index];
            loraSfTextView.setText(item);
        } else if (tag.equals(SETTINGS_DEVICE_DELAY)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            if (index == 0) {
                delay = targetDevice.getDelay();
            } else {
                delay = DELAY_VALUES[index];
            }
            String item = DELAY_ITEMS[index];
            delayTextView.setText(item);
        }  else if (tag.equals(SETTINGS_DEVICE_CHANNEL)) {
            ArrayList<ChannelData> channelDataArrayList = (ArrayList)bundle.getSerializable(SettingsSingleChoiceItemsFragment.INDEX);
            channelOpenList = channelDataArrayList;
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0 ; i < channelOpenList.size(); i ++) {
                ChannelData channelData = channelOpenList.get(i);
                if (channelData.isOpen()) {
                    stringBuffer.append(getString(R.string.setting_text_channel) + channelData.getIndex());
                }
            }
            channelTextView.setText(stringBuffer.toString());
        } else if (tag.equals(SETTINGS_DEVICE_CLASSB_DATARATE)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            String item = CLASSB_DATARATE[index];
            classBDatarateTextView.setText(item);
        } else if (tag.equals(SETTINGS_DEVICE_CLASSB_PERIODICITY)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            String item = CLASSB_PERIODICITY[index];
            classBPeriodicityTextView.setText(item + "s");
        } else if (tag.equals(SETTINGS_DEVICE_DATARATE)) {
            writeDataRateIndex = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            String item = getResources().getStringArray(R.array.multi_status_array)[writeDataRateIndex];
            dataRateTextView.setText(item);
            if (writeDataRateIndex == 1) {
                loraSfRelativeLayout.setVisibility(GONE);
            } else {
                loraSfRelativeLayout.setVisibility(VISIBLE);
            }
        } else if (tag.equals(SETTINGS_DEVICE_CLASS_ENALBLE)) {
            writeClassBEnableIndex = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            String item = getResources().getStringArray(R.array.multi_status_array)[writeClassBEnableIndex];
            classBEnableTextView.setText(item);
        } else if (tag.equals(SETTINGS_DEVICE_ACTIVATION)) {
            writeActivationIndex = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            String activationArray[] = this.getResources().getStringArray(R.array.multi_activation_array);
            String item = activationArray[writeActivationIndex];
            activationTextView.setText(item);
            if (writeActivationIndex != SETTING_STATUS_UNSET) {
                activation = writeActivationIndex;
                doActivationEvent(writeActivationIndex - 1);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensoroDeviceConnection != null) {
            sensoroDeviceConnection.disconnect();
        }
    }
}
