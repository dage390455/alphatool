package com.sensoro.loratool.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.print.PageRange;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.protobuf.ByteString;
import com.sensoro.libbleserver.ble.BLEDevice;
import com.sensoro.libbleserver.ble.SensoroConnectionCallback;
import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.libbleserver.ble.SensoroDeviceConfiguration;
import com.sensoro.libbleserver.ble.SensoroDeviceConnection;
import com.sensoro.libbleserver.ble.SensoroUtils;
import com.sensoro.libbleserver.ble.SensoroWriteCallback;
import com.sensoro.libbleserver.ble.proto.MsgNode1V1M5;
import com.sensoro.libbleserver.ble.proto.ProtoMsgCfgV1U1;
import com.sensoro.libbleserver.ble.proto.ProtoStd1U1;
import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.event.OnPositiveButtonClickListener;
import com.sensoro.loratool.fragment.SettingsInputDialogFragment;
import com.sensoro.loratool.fragment.SettingsMultiChoiceItemsFragment;
import com.sensoro.loratool.fragment.SettingsSingleChoiceItemsFragment;
import com.sensoro.loratool.model.ChannelData;
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
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.sensoro.loratool.constant.Constants.SETTINGS_DEVICE_SGL_DATA_RATE;

/**
 * Created by sensoro on 16/10/11.
 */

public class AdvanceSettingDeviceActivity extends BaseActivity implements Constants, OnPositiveButtonClickListener, SensoroConnectionCallback, SensoroWriteCallback {
    @BindView(R.id.settings_device_tv_sgl_status)
    TextView settingsDeviceTvSglStatus;
    @BindView(R.id.settings_device_tv_sgl_status_content)
    TextView settingsDeviceTvSglStatusContent;
    @BindView(R.id.settings_device_rl_sgl_status)
    RelativeLayout settingsDeviceRlSglStatus;
    @BindView(R.id.settings_device_tv_sgl_data_rate)
    TextView settingsDeviceTvSglDataRate;
    @BindView(R.id.settings_device_tv_sgl_data_rate_content)
    TextView settingsDeviceTvSglDataRateContent;
    @BindView(R.id.settings_device_rl_sgl_data_rate)
    RelativeLayout settingsDeviceRlSglDataRate;
    @BindView(R.id.settings_device_tv_sgl_frequency)
    TextView settingsDeviceTvSglFrequency;
    @BindView(R.id.settings_device_tv_sgl_frequency_content)
    TextView settingsDeviceTvSglFrequencyContent;
    @BindView(R.id.settings_device_rl_sgl_frequency)
    RelativeLayout settingsDeviceRlSglFrequency;
    @BindView(R.id.settings_device_rl_channel)
    RelativeLayout settingsDeviceRlWorkChannel;
    private SensoroDeviceConnection sensoroDeviceConnection;
    private SensoroDeviceConfiguration deviceConfiguration;
    private SensoroDevice sensoroDevice = null;
    private DeviceInfo deviceInfo = null;
    private String band = null;
    private String deviceType = null;
    private ProgressDialog progressDialog;
    private String sfItems[];
    //DEV EUI
    @BindView(R.id.settings_device_rl_dev_eui)
    RelativeLayout devEuiRelativeLayout;
    @BindView(R.id.settings_device_tv_dev_eui_value)
    TextView devEuiTextView;
    //APP EUI
    @BindView(R.id.settings_device_rl_app_eui)
    RelativeLayout appEuiRelativeLayout;
    @BindView(R.id.settings_device_tv_eui_value)
    TextView appEuiTextView;
    //app key
    @BindView(R.id.settings_device_rl_app_key)
    RelativeLayout appKeyRelativeLayout;
    @BindView(R.id.settings_device_tv_app_key)
    TextView appKeyTextView;
    //app session key
    @BindView(R.id.settings_device_rl_app_session_key)
    RelativeLayout appSessionKeyRelativeLayout;
    @BindView(R.id.settings_device_tv_app_session_key)
    TextView appSessionKeyTextView;
    //network session key
    @BindView(R.id.settings_device_rl_nwk_session_key)
    RelativeLayout nwkSessionKeyRelativeLayout;
    @BindView(R.id.settings_device_tv_nwk_session_key)
    TextView nwkSessionKeyTextView;
    //network address
    @BindView(R.id.settings_device_rl_nwk_address)
    RelativeLayout devAddrRelativeLayout;
    @BindView(R.id.settings_device_tv_nwk_address)
    TextView devAddrTextView;
    //sf
    @BindView(R.id.settings_device_rl_sf)
    RelativeLayout loraSfRelativeLayout;
    @BindView(R.id.settings_device_tv_sf)
    TextView loraSfTextView;
    //auto data rate
    @BindView(R.id.settings_device_rl_data_rate)
    RelativeLayout dataRateRelativeLayout;
    @BindView(R.id.settings_device_sc_datarate)
    SwitchCompat dataRateSwitchCompat;
    @BindView(R.id.settings_device_tv_data_rate)
    TextView dataRateTextView;

    @BindView(R.id.settings_device_tv_adv_device_save)
    TextView advSaveTextView;

    @BindView(R.id.settings_device_rl_activation)
    RelativeLayout activationRelativeLayout;
    @BindView(R.id.settings_device_tv_activation)
    TextView activationTextView;
    @BindView(R.id.settings_device_ll_delay)
    LinearLayout delayLinearLayout;
    @BindView(R.id.settings_device_rl_delay)
    RelativeLayout delayRelativeLayout;
    @BindView(R.id.settings_device_tv_delay)
    TextView delayTextView;

    @BindView(R.id.settings_device_ll_channel)
    LinearLayout channelMaskLinearLayout;
    @BindView(R.id.settings_device_tv_channel)
    TextView channelTextView;

    @BindView(R.id.settings_device_ll_classB)
    LinearLayout classBLinearLayout;
    @BindView(R.id.settings_device_sc_classB_enable)
    SwitchCompat classBEnableSwitchCompat;
    @BindView(R.id.settings_device_tv_classB_datarate)
    TextView classBDataRateTextView;
    @BindView(R.id.settings_device_tv_classB_periodicity)
    TextView classBPeriodicityTextView;

    private ArrayList<ChannelData> channelOpenList = new ArrayList<>();
    private int loraDr = 0;
    private int activation = 0;
    private int delay = 1;
    private String receivePeriod;
    private String classBSf;
    private String sglDataRate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onPageStart("设备私有云配置V1.1");
        initData();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_advcance_device;
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

        sensoroDevice = getIntent().getParcelableExtra(Constants.EXTRA_NAME_DEVICE);
        band = getIntent().getStringExtra(Constants.EXTRA_NAME_BAND);
        deviceType = getIntent().getStringExtra(Constants.EXTRA_NAME_DEVICE_TYPE);
        deviceInfo = getIntent().getParcelableExtra(Constants.EXTRA_NAME_DEVICE_INFO);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                AdvanceSettingDeviceActivity.this.finish();
            }
        });
        initLoraParam();
        connectDevice();
    }

    private void registerUi() {
        setContentView(R.layout.activity_advcance_device);
        ButterKnife.bind(this);
        resetRootLayout();
    }

    private void refresh() {
        try {
            if (sensoroDevice.hasDevEui()) {
                devEuiTextView.setText(sensoroDevice.getDevEui().toUpperCase());
                devEuiRelativeLayout.setVisibility(VISIBLE);
            } else {
                devEuiRelativeLayout.setVisibility(GONE);
            }
            if (sensoroDevice.hasAppEui()) {
                appEuiTextView.setText(String.valueOf(sensoroDevice.getAppEui()).toUpperCase());
                appEuiRelativeLayout.setVisibility(VISIBLE);
            } else {
                appEuiRelativeLayout.setVisibility(GONE);
            }
            if (sensoroDevice.hasAppKey()) {
                appKeyTextView.setText(sensoroDevice.getAppKey().toUpperCase());
                appKeyRelativeLayout.setVisibility(VISIBLE);
            } else {
                appKeyRelativeLayout.setVisibility(GONE);
            }
            if (sensoroDevice.hasAppSkey()) {
                appSessionKeyTextView.setText(sensoroDevice.getAppSkey() == null ? "" : sensoroDevice.getAppSkey().toUpperCase());
                appSessionKeyRelativeLayout.setVisibility(VISIBLE);
            } else {
                appSessionKeyTextView.setText(sensoroDevice.getAppSkey() == null ? "" : sensoroDevice.getAppSkey().toUpperCase());
                appSessionKeyRelativeLayout.setVisibility(GONE);
            }
            if (sensoroDevice.hasNwkSkey()) {
                nwkSessionKeyTextView.setText(sensoroDevice.getNwkSkey() == null ? "" : sensoroDevice.getNwkSkey().toUpperCase());
                nwkSessionKeyRelativeLayout.setVisibility(VISIBLE);
            } else {
                nwkSessionKeyTextView.setText(sensoroDevice.getNwkSkey() == null ? "" : sensoroDevice.getNwkSkey().toUpperCase());
                nwkSessionKeyRelativeLayout.setVisibility(GONE);
            }
            if (sensoroDevice.hasDevAddr()) {
                devAddrTextView.setText("0x0" + Integer.toHexString(sensoroDevice.getDevAdr()).toUpperCase());
                devAddrRelativeLayout.setVisibility(VISIBLE);
            } else {
                devAddrTextView.setText("0x0" + Integer.toHexString(sensoroDevice.getDevAdr()).toUpperCase());
                devAddrRelativeLayout.setVisibility(GONE);
            }
            dataRateSwitchCompat.setChecked(sensoroDevice.getLoraAdr() == 1 ? true : false);
            if (sensoroDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_04) {
                classBEnableSwitchCompat.setChecked(sensoroDevice.getClassBEnabled() == 1 ? true : false);
                classBSf = CLASSB_DATARATE[sensoroDevice.getClassBDataRate()];
                classBDataRateTextView.setText(classBSf);
                receivePeriod = CLASSB_PERIODICITY[sensoroDevice.getClassBPeriodicity()];
                classBPeriodicityTextView.setText(receivePeriod + "s");
            } else {
                classBLinearLayout.setVisibility(View.GONE);
            }

//            if(sensoroDevice.hasSglStatus()){
//                settingsDeviceRlSglStatus.setVisibility(VISIBLE);
//                int sglStatus = sensoroDevice.getSglStatus();
//                if (sglStatus == 1) {
//                    settingsDeviceTvSglStatusContent.setText("开启");
//                    if(sensoroDevice.hasSglDatarate()){
//                        settingsDeviceRlSglDataRate.setVisibility(VISIBLE);
//                        int loraTxpIndex = ParamUtil.getLoraTxpIndex(band, sensoroDevice.getSglDatarate());
//                        if(loraTxpIndex==0||loraTxpIndex==1||loraTxpIndex==2){
//                            settingsDeviceTvSglDataRateContent.setText(sfItems[loraTxpIndex]);
//                        }else{
//                            settingsDeviceTvSglDataRateContent.setText("未知");
//                        }
//
//                    }
//
//                    if(sensoroDevice.hasSglFrequency()){
//                        settingsDeviceRlSglFrequency.setVisibility(VISIBLE);
//                        int sglFrequency = sensoroDevice.getSglFrequency();
//                        int[] loraBandIntArray = ParamUtil.getLoraBandIntArray(band);
//                        for (int i = 0; i < loraBandIntArray.length; i++) {
//                            if (sglFrequency == loraBandIntArray[i]) {
//                                settingsDeviceTvSglFrequencyContent.setText(sensoroDevice.getSglFrequency()/1000000+"MHz");
//                                break;
//                            }
//                        }
//                    }
//                    settingsDeviceRlWorkChannel.setVisibility(GONE);
//
//                }else{
//                    settingsDeviceTvSglStatusContent.setText("关闭");
//                    settingsDeviceRlSglDataRate.setVisibility(GONE);
//                    settingsDeviceRlSglFrequency.setVisibility(GONE);
//                    settingsDeviceRlWorkChannel.setVisibility(VISIBLE);
//                    // todo 记得改这里
//                }
//
//
//            }
            if (deviceInfo.getDeviceType().equalsIgnoreCase("chip") && (deviceInfo.getFirmwareVersion().equalsIgnoreCase("2.0") || deviceInfo.getFirmwareVersion().equalsIgnoreCase("2.0.0"))) {
                delayLinearLayout.setVisibility(GONE);
            } else {
                delay = sensoroDevice.getDelay();
                if (sensoroDevice.hasDelay()) {
                    delayLinearLayout.setVisibility(VISIBLE);
                    delayTextView.setText(String.valueOf(sensoroDevice.getDelay()) + "s");
                } else {
                    delayLinearLayout.setVisibility(GONE);
                }
            }

            if (sensoroDevice.hasActivation()) {
                activationRelativeLayout.setVisibility(VISIBLE);
                activation = sensoroDevice.getActivation();
                String activationArray[] = this.getResources().getStringArray(R.array.activation_array);
                doActivationEvent(activation);
                activationTextView.setText(activationArray[sensoroDevice.getActivation()]);
            } else {
                activationRelativeLayout.setVisibility(GONE);
            }
            if (sensoroDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_05) {
                dataRateTextView.setText(R.string.adaptive_data_rate);
            } else {
                dataRateTextView.setText(R.string.data_rate);
            }
            loraDr = sensoroDevice.getLoraDr();
            int index = ParamUtil.getLoraDrIndex(band, loraDr);
            loraSfTextView.setText(String.valueOf(ParamUtil.getLoraSF(band, index)));
            if (deviceType != null) {
                if (deviceInfo.isOpenLoraDevice()) {
                    if (sensoroDevice.getLoraAdr() == 1) {
                        loraSfRelativeLayout.setVisibility(GONE);
                    } else {
                        loraSfRelativeLayout.setVisibility(VISIBLE);
                    }
                } else {
                    devEuiRelativeLayout.setVisibility(View.GONE);
                }
            }

            if (sensoroDevice.getChannelMaskList() == null) {
                channelMaskLinearLayout.setVisibility(GONE);
            } else {
                List<Integer> list = sensoroDevice.getChannelMaskList();
                for (int i = 0; i < list.size(); i++) {
                    for (int j = 0; j < 16; j++) {
                        ChannelData channelData = new ChannelData();
                        channelData.setIndex((i * 16 + j));
                        if ((list.get(i) & 1 << j) != 0) {
                            channelData.setOpen(true);
                            System.out.println("open==>" + (i * 16 + j));
                        } else {
                            channelData.setOpen(false);
                            System.out.println("close==>" + (i * 16 + j));
                        }
                        channelOpenList.add(channelData);
                    }
                }
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < channelOpenList.size(); i++) {
                    ChannelData channelData = channelOpenList.get(i);
                    if (channelData.isOpen()) {
                        stringBuffer.append(getString(R.string.setting_text_channel) + channelData.getIndex());
                    }
                }
                channelTextView.setText(stringBuffer.toString());
                if (channelOpenList.size() == 0) {
                    channelMaskLinearLayout.setVisibility(GONE);
                } else {
                    channelMaskLinearLayout.setVisibility(VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getString(R.string.connect_failed), Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        Toast.makeText(this, R.string.connect_success, Toast.LENGTH_SHORT).show();
    }

    private void initLoraParam() {
        switch (band) {
            case Constants.LORA_BAND_US915:
                sfItems = Constants.LORA_US915_SF;
                break;
            case Constants.LORA_BAND_SE433:
                sfItems = Constants.LORA_SE433_SF;
                break;
            case Constants.LORA_BAND_SE470:
                sfItems = Constants.LORA_SE470_SF;
                break;
            case Constants.LORA_BAND_SE780:
                sfItems = Constants.LORA_SE780_SF;
                break;
            case Constants.LORA_BAND_SE915:
                sfItems = Constants.LORA_SE915_SF;
                break;
            case Constants.LORA_BAND_AU915:
                sfItems = Constants.LORA_AU915_SF;
                break;
            case Constants.LORA_BAND_AS923:
                sfItems = Constants.LORA_AS923_SF;
                break;
            case Constants.LORA_BAND_EU433:
                sfItems = Constants.LORA_EU433_SF;
                break;
            case Constants.LORA_BAND_EU868:
                sfItems = Constants.LORA_EU868_SF;
                break;
            case Constants.LORA_BAND_CN470:
                sfItems = Constants.LORA_CN470_SF;
                break;
            default:
                sfItems = Constants.LORA_EU433_SF;
                break;
        }

    }

    public void doActivationEvent(int index) {
        if (index == 0) {
            appSessionKeyRelativeLayout.setVisibility(VISIBLE);
            nwkSessionKeyRelativeLayout.setVisibility(VISIBLE);
            devAddrRelativeLayout.setVisibility(VISIBLE);
        } else {
            appSessionKeyRelativeLayout.setVisibility(GONE);
            nwkSessionKeyRelativeLayout.setVisibility(GONE);
            devAddrRelativeLayout.setVisibility(GONE);
        }
    }

    public void connectDevice() {
        try {
            progressDialog.setMessage(getString(R.string.connecting));
            progressDialog.show();
            sensoroDeviceConnection = new SensoroDeviceConnection(this, sensoroDevice.getMacAddress());
            sensoroDeviceConnection.connect(sensoroDevice.getPassword(), this);
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
        msgCfgBuilder.setLoraInt((int) sensoroDevice.getLoraInt());
        msgCfgBuilder.setLoraTxp(sensoroDevice.getLoraTxp());
        msgCfgBuilder.setBleTxp(sensoroDevice.getBleTxp());
        msgCfgBuilder.setBleInt((int) sensoroDevice.getBleInt());
        msgCfgBuilder.setBleOnTime(sensoroDevice.getBleOnTime());
        msgCfgBuilder.setBleOffTime(sensoroDevice.getBleOffTime());

        if (sensoroDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_03) {
            msgCfgBuilder.setDevEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getDevEui()))));
            msgCfgBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getAppEui()))));
            msgCfgBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppKey())));
            msgCfgBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppSkey())));
            msgCfgBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getNwkSkey())));
            msgCfgBuilder.setDevAddr(deviceConfiguration.getDevAdr());
            msgCfgBuilder.setLoraDr(deviceConfiguration.getLoraDr());
            msgCfgBuilder.setLoraAdr(deviceConfiguration.getLoraAdr());
            ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();
            byte[] data = msgCfg.toByteArray();
            baseString = new String(Base64.encode(data, Base64.DEFAULT));

            version = "03";
        } else if (sensoroDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_04) {
            msgCfgBuilder.setDevEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getDevEui()))));
            msgCfgBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getAppEui()))));
            msgCfgBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppKey())));
            msgCfgBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppSkey())));
            msgCfgBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getNwkSkey())));
            msgCfgBuilder.setDevAddr(deviceConfiguration.getDevAdr());
            msgCfgBuilder.setLoraDr(deviceConfiguration.getLoraDr());
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
            if (sensoroDevice.hasLoraParam()) {
                MsgNode1V1M5.LpwanParam.Builder loraParamBuilder = MsgNode1V1M5.LpwanParam.newBuilder();
                loraParamBuilder.setTxPower(sensoroDevice.getLoraTxp());
                loraParamBuilder.setDevEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getDevEui()))));
                loraParamBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getAppEui()))));
                loraParamBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppKey())));
                loraParamBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppSkey())));
                loraParamBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getNwkSkey())));
                loraParamBuilder.setDevAddr(deviceConfiguration.getDevAdr());
                loraParamBuilder.setAdr(deviceConfiguration.getLoraAdr());
                loraParamBuilder.setDatarate(deviceConfiguration.getLoraDr());

                if (deviceInfo.getDeviceType().equalsIgnoreCase("chip") && (deviceInfo.getFirmwareVersion().equalsIgnoreCase("2.0") || deviceInfo.getFirmwareVersion().equalsIgnoreCase("2.0.0"))) {

                } else {
                    if (sensoroDevice.hasDelay()) {
                        loraParamBuilder.setDelay(deviceConfiguration.getDelay());
                    }
                }

                if (sensoroDevice.hasActivation()) {
                    loraParamBuilder.setActivition(MsgNode1V1M5.Activtion.valueOf(activation));
                }
                msgNodeBuilder.setLpwanParam(loraParamBuilder);
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
            jsonObject.put("SN", sensoroDevice.getSn());
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
                    DeviceDataDao.addDeviceItem(sensoroDevice.getSn(), dataString, versionString);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                DeviceDataDao.addDeviceItem(sensoroDevice.getSn(), dataString, versionString);
            }
        });
    }

    @OnClick(R.id.settings_device_ad_device_back)
    public void back() {
        this.finish();
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
                progressDialog.dismiss();
                if (bleDevice != null) {
                    registerUi();
                    refresh();
                }
            }
        });
    }

    @Override
    public void onConnectedFailure(int errorCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(AdvanceSettingDeviceActivity.this, R.string.connect_failed, Toast.LENGTH_SHORT).show();
                AdvanceSettingDeviceActivity.this.finish();
            }
        });
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onWriteSuccess(Object object, int cmd) {
        Log.e("hcs","写入成功:::");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    postUpdateData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                AdvanceSettingDeviceActivity.this.finish();
                Toast.makeText(AdvanceSettingDeviceActivity.this, R.string.save_succ, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onWriteFailure(final int errorCode, final int cmd) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(AdvanceSettingDeviceActivity.this, getString(R.string.save_fail) + " 错误码" + errorCode, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.settings_device_rl_dev_eui)
    public void doDevEui() {
        if (deviceInfo.isOpenLoraDevice()) {
            SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(devEuiTextView.getText().toString());
            dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_DEV_EUI);
        }
    }

    @OnClick(R.id.settings_device_rl_app_eui)
    public void doAppEui() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(appEuiTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_APP_EUI);

    }

    @OnClick(R.id.settings_device_rl_app_key)
    public void doAppKey() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(appKeyTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_APP_KEY);

    }

    @OnClick(R.id.settings_device_rl_app_session_key)
    public void doAppSessionKey() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(appSessionKeyTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_APP_SESSION_KEY);

    }

    @OnClick(R.id.settings_device_rl_nwk_session_key)
    public void doNwkSessionKey() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(nwkSessionKeyTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_NWK_SESSION_KEY);

    }

    @OnClick(R.id.settings_device_rl_nwk_address)
    public void doNwkAddress() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(devAddrTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_NWK_ADDRESS);

    }

    @OnClick(R.id.settings_device_rl_sf)
    public void doSf() {
        int index = ParamUtil.getLoraDrIndex(band, loraDr);
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(sfItems, index);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_SF);
    }

    @OnClick(R.id.settings_device_rl_delay)
    public void doDelay() {
        int index = ParamUtil.getIndexByValue(DELAY_VALUES, delay);
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(DELAY_ITEMS, index);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_DELAY);
    }

    @OnClick(R.id.settings_device_rl_channel)
    public void doChannel() {
        SettingsMultiChoiceItemsFragment dialogFragment = SettingsMultiChoiceItemsFragment.newInstance(channelOpenList);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_CHANNEL);
    }

    @OnClick(R.id.settings_device_rl_activation)
    public void doActivation() {
        String activationArray[] = this.getResources().getStringArray(R.array.activation_array);
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(activationArray, activation);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_ACTIVATION);
    }

    @OnCheckedChanged(R.id.settings_device_sc_classB_enable)
    public void doClassBEnable() {

    }

    @OnClick(R.id.settings_device_rl_classB_datarate)
    public void doClassBDataRate() {
        int datarate = Integer.parseInt(classBSf);
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(CLASSB_DATARATE, getClassBDataRate(datarate));
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_CLASSB_DATARATE);
    }

    @OnClick(R.id.settings_device_rl_classB_periodicity)
    public void doClassBPeriodicity() {
        int periodicity = Integer.parseInt(receivePeriod);
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(CLASSB_PERIODICITY, getClassBPeridicity(periodicity));
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_CLASSB_PERIODICITY);
    }

    @OnClick(R.id.settings_device_rl_sgl_status)
    public void doSglStatus() {
        int sglStatus = sensoroDevice.getSglStatus();
        SettingsSingleChoiceItemsFragment dialogFragment;
        if(sglStatus == 1){
            dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(ON_OFF_ITEMS, 0);
        }else{
            dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(ON_OFF_ITEMS, 1);
        }

        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_SGL_STATUS);
    }

    @OnClick(R.id.settings_device_rl_sgl_data_rate)
    public void doSglDataRate() {
        int index = ParamUtil.getLoraDrIndex(band, loraDr);
        if(index == 0|| index==1||index==2){

        }else{
            index = -1;
        }
        String[] items = {sfItems[0],sfItems[1],sfItems[2]};
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(items, index);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_SGL_DATA_RATE);
    }

    @OnClick(R.id.settings_device_rl_sgl_frequency)
    public void doSglFrequency() {
        String[] loraBandText = ParamUtil.getLoraBandText(this, band);
        int sglFrequency = sensoroDevice.getSglFrequency();
        int[] loraBandIntArray = ParamUtil.getLoraBandIntArray(band);
        for (int i = 0; i < loraBandIntArray.length; i++) {
            if (sglFrequency == loraBandIntArray[i]) {
                SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(loraBandText, i);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_SGL_FREQUENCY);
                return;
            }
        }
        SettingsSingleChoiceItemsFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(loraBandText, 0);
        dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_SGL_FREQUENCY);

    }

    @OnClick(R.id.settings_device_tv_adv_device_save)
    public void saveConfiguration() {
        try {
            progressDialog.setMessage(getString(R.string.saving));
            progressDialog.show();

            SensoroDeviceConfiguration.Builder builder = new SensoroDeviceConfiguration.Builder();
            builder.setLoraAdr(dataRateSwitchCompat.isChecked() ? 1 : 0);

            if (sensoroDevice.hasActivation()) {
                builder.setActivation(activation);
                builder.setHasActivation(true);
            } else {
                builder.setHasActivation(false);
            }
            if (sensoroDevice.hasDevEui()) {
                builder.setHasDevEui(true);
                builder.setDevEui(devEuiTextView.getText().toString());
            } else {
                builder.setHasDevEui(false);
            }
            if (sensoroDevice.hasAppEui()) {
                builder.setAppEui(appEuiTextView.getText().toString());
                builder.setHasAppEui(true);
            } else {
                builder.setHasAppEui(false);
            }
            if (sensoroDevice.hasAppKey()) {
                builder.setAppKey(appKeyTextView.getText().toString());
                builder.setHasAppKey(true);
            } else {
                builder.setHasAppKey(false);
            }
            if (sensoroDevice.hasAppSkey() || activation == 0) {
                builder.setAppSkey(appSessionKeyTextView.getText().toString());
                builder.setHasAppSkey(true);
            } else {
                builder.setHasAppSkey(false);
            }
            if (sensoroDevice.hasNwkSkey() || activation == 0) {
                builder.setNwkSkey(nwkSessionKeyTextView.getText().toString());
                builder.setHasNwkSkey(true);
            } else {
                builder.setHasNwkSkey(false);
            }
            if (sensoroDevice.hasDevAddr() || activation == 0) {
                builder.setHasDevAddr(true);
                builder.setDevAdr(Integer.parseInt(devAddrTextView.getText().toString().replace("0x0", ""), 16));
            } else {
                builder.setHasDevAddr(false);
            }
            if (sensoroDevice.hasDelay()) {
                builder.setHasDelay(true);
                builder.setDelay(delay);
            } else {
                builder.setHasDelay(false);
            }

            builder.setHasSglStatus(sensoroDevice.hasSglStatus());
            if(sensoroDevice.hasSglStatus()){
                builder.setSglStatus(sensoroDevice.getSglStatus());
            }

            builder.setHasSglDataRate(sensoroDevice.hasDataRate());
            if(sensoroDevice.hasSglDatarate()){
                builder.setSglStatus(sensoroDevice.getSglStatus());
            }

            builder.setHasSglFrequency(sensoroDevice.hasSglFrequency());
            if(sensoroDevice.hasSglFrequency()){
                builder.setSglFrequency(sensoroDevice.getSglFrequency());
            }
            List<Integer> list = sensoroDevice.getChannelMaskList();
            if (list != null&&list.size()>0) {
                int array[] = new int[list.size()];
                for (int i = 0; i < list.size() * 16; i++) {
                    ChannelData channelData = channelOpenList.get(i);
                    if (channelData.isOpen()) {
                        array[i / 16] |= 1 << i % 16;
                    } else {
                        array[i / 16] |= 0 << i % 16;
                    }
                }
                ArrayList<Integer> tempList = new ArrayList();
                for (int i = 0; i < array.length; i++) {
                    tempList.add(array[i]);
                }
                builder.setChannelList(tempList);
            }
            builder.setLoraDr(loraDr);
            if (sensoroDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_04) {
                builder.setClassBEnabled(classBEnableSwitchCompat.isChecked() ? 1 : 0)
                        .setClassBPeriodicity(getClassBPeridicity(Integer.parseInt(receivePeriod.replace("s", ""))))
                        .setClassBDataRate(getClassBDataRate(Integer.parseInt(classBSf.toString())));
            }
            deviceConfiguration = builder.build();

            sensoroDeviceConnection.writeDeviceAdvanceConfiguration(deviceConfiguration, this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @OnCheckedChanged(R.id.settings_device_sc_datarate)
    public void doDataRateState() {
        if (dataRateSwitchCompat.isChecked()) {
            loraSfRelativeLayout.setVisibility(GONE);
        } else {
            loraSfRelativeLayout.setVisibility(VISIBLE);
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
            String regString = "[a-f0-9A-F]{16}";
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
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
                devAddrTextView.setText(text);
            } else {
                Toast.makeText(this, R.string.data_invalid, Toast.LENGTH_SHORT).show();
            }

        } else if (tag.equals(SETTINGS_DEVICE_SF)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            loraDr = ParamUtil.getLoraDr(band, index);
            String item = sfItems[index];
            loraSfTextView.setText(item);
        } else if (tag.equals(SETTINGS_DEVICE_DELAY)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            delay = DELAY_VALUES[index];
            String item = DELAY_ITEMS[index];
            delayTextView.setText(item);
        } else if (tag.equals(SETTINGS_DEVICE_CHANNEL)) {
            ArrayList<ChannelData> channelDataArrayList = (ArrayList) bundle.getSerializable(SettingsSingleChoiceItemsFragment.INDEX);
            channelOpenList = channelDataArrayList;
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < channelOpenList.size(); i++) {
                ChannelData channelData = channelOpenList.get(i);
                if (channelData.isOpen()) {
                    stringBuffer.append(getString(R.string.setting_text_channel) + channelData.getIndex());
                }
            }
            channelTextView.setText(stringBuffer.toString());
        } else if (tag.equals(SETTINGS_DEVICE_CLASSB_DATARATE)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            classBSf = CLASSB_DATARATE[index];
            classBDataRateTextView.setText(classBSf);
        } else if (tag.equals(SETTINGS_DEVICE_CLASSB_PERIODICITY)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            receivePeriod = CLASSB_PERIODICITY[index];
            classBPeriodicityTextView.setText(receivePeriod + "s");
        } else if (tag.equals(SETTINGS_DEVICE_ACTIVATION)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            String activationArray[] = this.getResources().getStringArray(R.array.activation_array);
            String item = activationArray[index];
            activationTextView.setText(item);
            activation = index;
            doActivationEvent(index);
        }else if (tag.equals(SETTINGS_DEVICE_SGL_STATUS)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            if(index == 0){
                sensoroDevice.setSglStatus(1);
                settingsDeviceTvSglStatusContent.setText("开启");
                settingsDeviceRlSglFrequency.setVisibility(VISIBLE);
                settingsDeviceRlSglDataRate.setVisibility(VISIBLE);
                settingsDeviceRlWorkChannel.setVisibility(GONE);
                int bleTxpIndex = ParamUtil.getLoraTxpIndex(band,sensoroDevice.getSglDatarate());
                if(bleTxpIndex == 0||bleTxpIndex==1||bleTxpIndex==2){
                    settingsDeviceTvSglDataRateContent.setText(sfItems[bleTxpIndex]);
                }else{
                    settingsDeviceTvSglDataRateContent.setText("未知");
                }


                int sglFrequency = sensoroDevice.getSglFrequency();
                int[] loraBandIntArray = ParamUtil.getLoraBandIntArray(band);
                for (int i = 0; i < loraBandIntArray.length; i++) {
                    if (sglFrequency == loraBandIntArray[i]) {
                        settingsDeviceTvSglFrequencyContent.setText(sensoroDevice.getSglFrequency()/1000000+"MHz");
                        return;
                    }
                }
            }else{
                sensoroDevice.setSglStatus(0);
                settingsDeviceTvSglStatusContent.setText("关闭");
                settingsDeviceRlSglFrequency.setVisibility(GONE);
                settingsDeviceRlSglDataRate.setVisibility(GONE);
                settingsDeviceRlWorkChannel.setVisibility(GONE);
//                sensoroDevice.setSglDatarate(0);
//                sensoroDevice.setSglFrequency(0);
            }

        }else if (tag.equals(SETTINGS_DEVICE_SGL_DATA_RATE)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            int bleTxp = ParamUtil.getLoraTxp(band, index);
            sensoroDevice.setSglDatarate(bleTxp);
            settingsDeviceTvSglDataRateContent.setText(sfItems[index]);
        }else if (tag.equals(SETTINGS_DEVICE_SGL_FREQUENCY)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            int sglFrequency = ParamUtil.getLoraBandIntArray(band)[index];
            sensoroDevice.setSglFrequency(sglFrequency);
            settingsDeviceTvSglFrequencyContent.setText(ParamUtil.getLoraBandText(this,band)[index]);
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
