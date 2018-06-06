package com.sensoro.loratool.activity;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.sensoro.lora.setting.server.bean.EidInfo;
import com.sensoro.lora.setting.server.bean.EidInfoListRsp;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.fragment.SettingsInputDialogFragment;
import com.sensoro.loratool.activity.fragment.SettingsMajorMinorDialogFragment;
import com.sensoro.loratool.activity.fragment.SettingsSingleChoiceItemsFragment;
import com.sensoro.loratool.activity.fragment.SettingsUUIDDialogFragment;
import com.sensoro.loratool.ble.BLEDevice;
import com.sensoro.loratool.ble.CmdType;
import com.sensoro.loratool.ble.SensoroConnectionCallback;
import com.sensoro.loratool.ble.SensoroDevice;
import com.sensoro.loratool.ble.SensoroDeviceConfiguration;
import com.sensoro.loratool.ble.SensoroDeviceConnection;
import com.sensoro.loratool.ble.SensoroSensorConfiguration;
import com.sensoro.loratool.ble.SensoroSensorTest;
import com.sensoro.loratool.ble.SensoroSlot;
import com.sensoro.loratool.ble.SensoroUtils;
import com.sensoro.loratool.ble.SensoroWriteCallback;
import com.sensoro.loratool.ble.scanner.SensoroUUID;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.event.OnPositiveButtonClickListener;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class SettingMultiDeviceActivity extends BaseActivity implements Constants, View.OnClickListener,
        OnPositiveButtonClickListener, SensoroWriteCallback, SensoroConnectionCallback {

    private static final String TAG = SettingMultiDeviceActivity.class.getSimpleName();
    @BindView(R.id.settings_multi_device_back)
    ImageView backImageView;
    @BindView(R.id.settings_multi_tv_save)
    TextView saveTextView;
    @BindView(R.id.settings_multi_ll_ibeacon)
    LinearLayout ibeaconLayout;
    @BindView(R.id.settings_multi_tv_ibeacon)
    TextView iBeaconStatusTextView;
    @BindView(R.id.settings_multi_rl_ibeacon)
    RelativeLayout iBeaconRelativeLayout;
    @BindView(R.id.settings_multi_rl_uuid)
    RelativeLayout uuidRelativeLayout;
    @BindView(R.id.settings_multi_tv_uuid)
    TextView uuidTextView;
    @BindView(R.id.settings_multi_rl_major)
    RelativeLayout majorRelativeLayout;
    @BindView(R.id.settings_multi_tv_major)
    TextView majorTextView;
    @BindView(R.id.settings_multi_rl_minor)
    RelativeLayout minorRelativeLayout;
    @BindView(R.id.settings_multi_tv_minor)
    TextView minorTextView;

    /**
     * broadcast function
     **/
    @BindView(R.id.settings_multi_ll_ble)
    LinearLayout bleLayout;
    @BindView(R.id.settings_multi_rl_power)
    RelativeLayout powerRelativeLayout;
    @BindView(R.id.settings_multi_tv_power)
    TextView powerTextView;
    // advertise interval
    @BindView(R.id.settings_multi_rl_adv_interval)
    RelativeLayout advIntervalRelativeLayout;
    @BindView(R.id.settings_multi_tv_adv_interval)
    TextView advIntervalTextView;
    //ble turn on time
    @BindView(R.id.setting_multi_ll_turnon_time)
    LinearLayout bleTurnOnTimeLinearLayout;
    @BindView(R.id.settings_multi_ble_turnon_time)
    TextView turnOnTexView;
    //ble turn off time
    @BindView(R.id.setting_multi_ll_turnoff_time)
    LinearLayout bleTurnOffTimeLinearLayout;
    @BindView(R.id.settings_multi_ble_turnoff_time)
    TextView turnOffTextView;

    /**
     * Sensor Parameter
     **/
    @BindView(R.id.settings_multi_ll_lora)
    LinearLayout loraLayout;
    //transimit power
    @BindView(R.id.settings_multi_rl_lora_transmit_power)
    RelativeLayout loraTxpRelativeLayout;
    @BindView(R.id.settings_multi_tv_lora_transmit_power)
    TextView loraTxpTextView;
    //advertisingInterval
    @BindView(R.id.settings_multi_rl_lora_ad_interval)
    RelativeLayout loraAdIntervalRelativeLayout;
    @BindView(R.id.settings_multi_tv_lora_ad_interval)
    TextView loraAdIntervalTextView;
    @BindView(R.id.settings_multi_rl_lora_eirp)
    RelativeLayout loraEirpRelativeLayout;
    @BindView(R.id.settings_multi_tv_lora_eirp)
    TextView loraEirpTextView;

    /**
     * Eddystone Function
     **/
    @BindView(R.id.settings_multi_ll_eddystone)
    LinearLayout eddystoneLayout;
    //slot 1
    @BindView(R.id.settings_multi_rl_slot1_item1)
    RelativeLayout eddyStoneSlot1Item1Layout;
    @BindView(R.id.multi_slot1_item1_tv)
    TextView eddyStoneSlot1Item1;
    @BindView(R.id.multi_slot1_item1_tv_v)
    TextView eddyStoneSlot1Item1Value;
    @BindView(R.id.settings_multi_rl_slot1_item2)
    RelativeLayout eddyStoneSlot1Item2Layout;
    @BindView(R.id.multi_slot1_item2_tv)
    TextView eddyStoneSlot1Item2;
    @BindView(R.id.multi_slot1_item2_iv)
    ImageView eddyStoneSlot1Iv2;
    @BindView(R.id.multi_slot1_item2_tv_v)
    TextView eddyStoneSlot1Item2Value;
    //slot 2
    @BindView(R.id.settings_multi_rl_slot2_item1)
    RelativeLayout eddyStoneSlot2Item1Layout;
    @BindView(R.id.multi_slot2_item1_tv)
    TextView eddyStoneSlot2Item1;
    @BindView(R.id.multi_slot2_item1_tv_v)
    TextView eddyStoneSlot2Item1Value;
    @BindView(R.id.settings_multi_rl_slot2_item2)
    RelativeLayout eddyStoneSlot2Item2Layout;
    @BindView(R.id.multi_slot2_item2_tv)
    TextView eddyStoneSlot2Item2;
    @BindView(R.id.multi_slot2_sep_iv2)
    ImageView eddyStoneSlot2Iv2;
    @BindView(R.id.multi_slot2_item2_tv_v)
    TextView eddyStoneSlot2Item2Value;

    //slot 3
    @BindView(R.id.settings_multi_rl_slot3_item1)
    RelativeLayout eddyStoneSlot3Item1Layout;
    @BindView(R.id.multi_slot3_item1_tv)
    TextView eddyStoneSlot3Item1;
    @BindView(R.id.multi_slot3_item1_tv_v)
    TextView eddyStoneSlot3Item1Value;
    @BindView(R.id.settings_multi_rl_slot3_item2)
    RelativeLayout eddyStoneSlot3Item2Layout;
    @BindView(R.id.multi_slot3_item2_tv)
    TextView eddyStoneSlot3Item2;
    @BindView(R.id.multi_slot3_sep_iv2)
    ImageView eddyStoneSlot3Iv2;
    @BindView(R.id.multi_slot3_item2_tv_v)
    TextView eddyStoneSlot3Item2Value;


    //slot 3
    @BindView(R.id.settings_multi_rl_slot4_item1)
    RelativeLayout eddyStoneSlot4Item1Layout;
    @BindView(R.id.multi_slot4_item1_tv)
    TextView eddyStoneSlot4Item1;
    @BindView(R.id.multi_slot4_sep_iv1)
    ImageView eddyStoneSlot4Iv1;
    @BindView(R.id.multi_slot4_item1_tv_v)
    TextView eddyStoneSlot4Item1Value;
    @BindView(R.id.settings_multi_rl_slot4_item2)
    RelativeLayout eddyStoneSlot4Item2Layout;
    @BindView(R.id.multi_slot4_item2_tv)
    TextView eddyStoneSlot4Item2;
    @BindView(R.id.multi_slot4_sep_iv2)
    ImageView eddyStoneSlot4Iv2;
    @BindView(R.id.multi_slot4_item2_tv_v)
    TextView eddyStoneSlot4Item2Value;
    @BindView(R.id.settings_multi_ll_sensor_enable)
    LinearLayout sensorBroadcastEnableLayout;
    @BindView(R.id.multi_sensor_adv_tv)
    TextView sensorStatusTextView;
    @BindView(R.id.settings_multi_rl_custom_package1_state)
    RelativeLayout customPackage1RelativeLayout;
    @BindView(R.id.multi_custom_package1_tv)
    TextView customPackage1TextView;
    @BindView(R.id.settings_multi_rl_custom_package2_state)
    RelativeLayout customPackage2RelativeLayout;
    @BindView(R.id.multi_custom_package2_tv)
    TextView customPackage2TextView;
    @BindView(R.id.settings_multi_rl_custom_package3_state)
    RelativeLayout customPackage3RelativeLayout;
    @BindView(R.id.multi_custom_package3_tv)
    TextView customPackage3TextView;
    @BindView(R.id.settings_multi_ll_sensor_param)
    LinearLayout sensorParamLayout;
    @BindView(R.id.settings_multi_ll_co)
    LinearLayout coLinearLayout;
    @BindView(R.id.settings_multi_tv_co_upper_limit)
    TextView coTextView;
    @BindView(R.id.settings_multi_ll_co2)
    LinearLayout co2LinearLayout;
    @BindView(R.id.settings_multi_tv_co2_upper_limit)
    TextView co2TextView;
    @BindView(R.id.settings_multi_ll_no2)
    LinearLayout no2LinearLayout;
    @BindView(R.id.settings_multi_tv_no2_upper_limit)
    TextView no2TextView;
    @BindView(R.id.settings_multi_ll_ch4)
    LinearLayout ch4LinearLayout;
    @BindView(R.id.settings_multi_tv_ch4_upper_limit)
    TextView ch4TextView;
    @BindView(R.id.settings_multi_ll_lpg)
    LinearLayout lpgLinearLayout;
    @BindView(R.id.settings_multi_tv_lpg_upper_limit)
    TextView lpgTextView;
    @BindView(R.id.settings_multi_ll_pm25)
    LinearLayout pm25LinearLayout;
    @BindView(R.id.settings_multi_tv_pm25_upper_limit)
    TextView pm25TextView;
    @BindView(R.id.settings_multi_ll_pm10)
    LinearLayout pm10LinearLayout;
    @BindView(R.id.settings_multi_tv_pm10_upper_limit)
    TextView pm10TextView;
    @BindView(R.id.settings_multi_ll_app_param)
    LinearLayout appParamLayout;
    @BindView(R.id.settings_multi_rl_app_param_upload)
    RelativeLayout uploadIntervalLayout;
    @BindView(R.id.settings_multi_tv_upload_upper_limit)
    TextView uploadIntervalTextView;
    @BindView(R.id.settings_multi_ll_temp)
    LinearLayout tempLinearLayout;
    @BindView(R.id.settings_multi_tv_temp_upper_limit)
    TextView tempUpperTextView;
    @BindView(R.id.settings_multi_tv_temp_lower_limit)
    TextView tempLowerTextView;
    @BindView(R.id.settings_multi_rl_temp_upper)
    RelativeLayout tempUpperRelativeLayout;
    @BindView(R.id.settings_multi_rl_temp_lower)
    RelativeLayout tempLowerRelativeLayout;
    @BindView(R.id.settings_multi_ll_humidity)
    LinearLayout humidityLinearLayout;
    @BindView(R.id.settings_multi_tv_humidity_upper_limit)
    TextView humidityUpperTextView;
    @BindView(R.id.settings_multi_tv_humidity_lower_limit)
    TextView humidityLowerTextView;
    @BindView(R.id.settings_multi_rl_humidity_upper)
    RelativeLayout humidityUpperRelativeLayout;
    @BindView(R.id.settings_multi_rl_humidity_lower)
    RelativeLayout humidityLowerRelativeLayout;
    @BindView(R.id.settings_multi_ll_pitch_angle)
    LinearLayout pitchAngleLinearLayout;
    @BindView(R.id.settings_multi_tv_pitch_angle_upper_limit)
    TextView pitchAngleUpperTextView;
    @BindView(R.id.settings_multi_tv_pitch_angle_lower_limit)
    TextView pitchAngleLowerTextView;
    @BindView(R.id.settings_multi_rl_pitch_angle_upper)
    RelativeLayout pitchAngleRelativeLayout;
    @BindView(R.id.settings_multi_rl_pitch_angle_lower)
    RelativeLayout pitchAngleLowerRelativeLayout;
    @BindView(R.id.settings_multi_ll_roll_angle)
    LinearLayout rollAngleLinearLayout;
    @BindView(R.id.settings_multi_tv_roll_angle_upper_limit)
    TextView rollAngleUpperTextView;
    @BindView(R.id.settings_multi_tv_roll_angle_lower_limit)
    TextView rollAngleLowerTextView;
    @BindView(R.id.settings_multi_rl_roll_angle_upper)
    RelativeLayout rollAngleUpperRelativeLayout;
    @BindView(R.id.settings_multi_rl_roll_angle_lower)
    RelativeLayout rollAngleLowerRelativeLayout;
    @BindView(R.id.settings_multi_ll_yaw_angle)
    LinearLayout yawAngleLinearLayout;
    @BindView(R.id.settings_multi_tv_yaw_angle_upper_limit)
    TextView yawAngleUpperTextView;
    @BindView(R.id.settings_multi_tv_yaw_angle_lower_limit)
    TextView yawAngleLowerTextView;
    @BindView(R.id.settings_multi_rl_yaw_angle_upper)
    RelativeLayout yawAngleUpperRelativeLayout;
    @BindView(R.id.settings_multi_rl_yaw_angle_lower)
    RelativeLayout yawAngleLowerRelativeLayout;

    @BindView(R.id.settings_multi_ll_water_pressure)
    LinearLayout waterPressureLinearLayout;
    @BindView(R.id.settings_multi_tv_water_pressure_upper_limit)
    TextView waterPressureUpperTextView;
    @BindView(R.id.settings_multi_tv_water_pressure_lower_limit)
    TextView waterPressureLowerTextView;
    @BindView(R.id.settings_multi_rl_water_pressure_upper)
    RelativeLayout waterPressureUpperRelativeLayout;
    @BindView(R.id.settings_multi_rl_water_pressure_lower)
    RelativeLayout waterPressureLowerRelativeLayout;

    @BindView(R.id.settings_device_ll_smoke)
    LinearLayout smokeLinearLayout;
    @BindView(R.id.settings_device_tv_smoke_status)
    TextView smokeStatusTextView;
    @BindView(R.id.settings_device_tv_smoke_start)
    TextView smokeStartTextView;
    @BindView(R.id.settings_device_tv_smoke_stop)
    TextView smokeStopTextView;
    @BindView(R.id.settings_device_tv_smoke_silence)
    TextView smokeSilenceTextView;
    @BindView(R.id.settings_multi_rl_app_param_confirm)
    RelativeLayout confirmLayout;
    @BindView(R.id.settings_multi_tv_confirm)
    TextView confirmTextView;
    @BindView(R.id.settings_multi_ll_custom_package)
    LinearLayout customLayout;
    @BindView(R.id.settings_multi_rl_custom_package1)
    RelativeLayout customPackage1ValueRelativeLayout;
    @BindView(R.id.settings_multi_rl_custom_package2)
    RelativeLayout customPackage2ValueRelativeLayout;
    @BindView(R.id.settings_multi_rl_custom_package3)
    RelativeLayout customPackage3ValueRelativeLayout;
    @BindView(R.id.settings_multi_tv_package1_v)
    TextView customPackage1ValueTextView;
    @BindView(R.id.settings_multi_tv_package2_v)
    TextView customPackage2ValueTextView;
    @BindView(R.id.settings_multi_tv_package3_v)
    TextView customPackage3ValueTextView;

    private String[] blePowerItems;
    private String[] bleTimeItems;
    private String[] loraTxpItems;
    private String[] loraEirpItems;
    private String[] loraEirpValues;
    private String uuid;
    private int major;
    private int minor;
    private float bleInt;
    private int bleTxp;
    private float loraInt;
    private int loraTxp;

    private int bleTurnOnTime;
    private int bleTurnOffTime;

    private float coAlarmHigh;
    private float co2AlarmHigh;
    private float no2AlarmHigh;
    private float ch4AlarmHigh;
    private float lpgAlarmHigh;
    private float pm25AlarmHigh;
    private float pm10AlarmHigh;
    private float tempAlarmHigh;
    private float tempAlarmLow;
    private float humidityAlarmHigh;
    private float humidityAlarmLow;
    private float pitchAngleAlarmHigh;
    private float pitchAngleAlarmLow;
    private float rollAngleAlarmHigh;
    private float rollAngleAlarmLow;
    private float yawAngleAlarmHigh;
    private float yawAngleAlarmLow;
    private float waterPressureAlarmHigh;
    private float waterPressureAlarmLow;
    private int uploadInterval;
    private int appParamConfirm;
    private String[] slotItems;
    private String[] statusItems;
    private String slot1_frame;
    private String slot2_frame;
    private String slot3_frame;
    private String slot4_frame;
    private int slotItemSelectIndex[] = new int[4];
    private boolean isIBeaconEnabled;
    private String custom_package1;
    private String custom_package2;
    private String custom_package3;
    private boolean isCustomPackage1Enabled;
    private boolean isCustomPackage2Enabled;
    private boolean isCustomPackage3Enabled;
    private boolean isSensorBroadcastEnabled;
    private SensoroDeviceConnection sensoroDeviceConnection;
    private ArrayList<SensoroDevice> targetDeviceList = new ArrayList<>();
    private SensoroDevice targetDevice = null;
    private SensoroSlot sensoroSlotArray[];

    private LoRaSettingApplication application;
    private SensoroDeviceConfiguration deviceConfiguration;
    private ProgressDialog progressDialog;
    private String band;
    private String deviceType;

    private int targetDeviceIndex;
    private int smokeActionIndex = SMOKE_ACTION_START;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        MobclickAgent.onPageStart("设备配置");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void init() {
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
                SettingMultiDeviceActivity.this.finish();
            }
        });
        initData();
    }

    private void initData() {
        targetDeviceList = this.getIntent().getParcelableArrayListExtra(Constants.EXTRA_NAME_DEVICE_LIST);
        targetDeviceIndex = 0;
        targetDevice = targetDeviceList.get(targetDeviceIndex);
        band = getIntent().getStringExtra(Constants.EXTRA_NAME_BAND);
        deviceType = getIntent().getStringExtra(EXTRA_NAME_DEVICE_TYPE);
        String tempItems[] = null;
        if (deviceType.equals("node")) {
            tempItems = BLE_NODE_TXP_ARRAY;
        } else {
            tempItems = BLE_NOT_NODE_TXP_ARRAY;
        }
        blePowerItems = new String[tempItems.length + 1];
        for (int i = 0; i < blePowerItems.length; i++) {
            if (i == 0) {
                blePowerItems[i] = getString(R.string.setting_unset);
            } else {
                blePowerItems[i] = tempItems[i - 1];
            }
        }
        this.bleTimeItems = this.getResources().getStringArray(R.array.multi_time_array);
        this.statusItems = this.getResources().getStringArray(R.array.multi_status_array);
        this.slotItems = this.getResources().getStringArray(R.array.multi_eddystone_slot_array);
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
                loraEirpItems = Constants.LORA_AS923_MAX_EIRP;
                loraEirpValues = Constants.LORA_AS923_MAX_EIRP_VALUE;
                break;
            case Constants.LORA_BAND_EU433:
                txp_array = Constants.LORA_EU433_TXP;
                loraEirpItems = Constants.LORA_EU433_MAX_EIRP;
                loraEirpValues = Constants.LORA_EU433_MAX_EIRP_VALUE;
                break;
            case Constants.LORA_BAND_EU868:
                txp_array = Constants.LORA_EU868_TXP;
                loraEirpItems = Constants.LORA_EU868_MAX_EIRP;
                loraEirpValues = Constants.LORA_EU868_MAX_EIRP_VALUE;
                break;
            case Constants.LORA_BAND_CN470:
                txp_array = Constants.LORA_CN470_TXP;
                loraEirpItems = Constants.LORA_CN470_MAX_EIRP;
                loraEirpValues = Constants.LORA_CN470_MAX_EIRP_VALUE;
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

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_setting_multi_device;
    }

    private void registerUiEvent() {
        setContentView(R.layout.activity_setting_multi_device);
        ButterKnife.bind(this);
        resetRootLayout();
        backImageView.setOnClickListener(this);
        saveTextView.setOnClickListener(this);
        iBeaconRelativeLayout.setOnClickListener(this);
        uuidRelativeLayout.setOnClickListener(this);
        majorRelativeLayout.setOnClickListener(this);
        minorRelativeLayout.setOnClickListener(this);
        powerRelativeLayout.setOnClickListener(this);
        advIntervalRelativeLayout.setOnClickListener(this);
        bleTurnOnTimeLinearLayout.setOnClickListener(this);
        bleTurnOffTimeLinearLayout.setOnClickListener(this);
        loraTxpRelativeLayout.setOnClickListener(this);
        loraAdIntervalRelativeLayout.setOnClickListener(this);
        eddyStoneSlot1Item1Layout.setOnClickListener(this);
        eddyStoneSlot1Item2Layout.setOnClickListener(this);
        eddyStoneSlot2Item1Layout.setOnClickListener(this);
        eddyStoneSlot2Item2Layout.setOnClickListener(this);
        eddyStoneSlot3Item1Layout.setOnClickListener(this);
        eddyStoneSlot3Item2Layout.setOnClickListener(this);
        eddyStoneSlot4Item1Layout.setOnClickListener(this);
        eddyStoneSlot4Item2Layout.setOnClickListener(this);
        eddyStoneSlot1Item1.setText(this.getString(R.string.slot1_name));
        eddyStoneSlot2Item1.setText(this.getString(R.string.slot1_name));
        eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
        eddyStoneSlot4Item1.setText(this.getString(R.string.slot1_name));

        sensorBroadcastEnableLayout.setOnClickListener(this);
        customPackage1RelativeLayout.setOnClickListener(this);
        customPackage2RelativeLayout.setOnClickListener(this);
        customPackage3RelativeLayout.setOnClickListener(this);
        coLinearLayout.setOnClickListener(this);
        co2LinearLayout.setOnClickListener(this);
        no2LinearLayout.setOnClickListener(this);
        ch4LinearLayout.setOnClickListener(this);
        lpgLinearLayout.setOnClickListener(this);
        pm25LinearLayout.setOnClickListener(this);
        pm10LinearLayout.setOnClickListener(this);
        tempUpperRelativeLayout.setOnClickListener(this);
        tempLowerRelativeLayout.setOnClickListener(this);
        humidityUpperRelativeLayout.setOnClickListener(this);
        humidityLowerRelativeLayout.setOnClickListener(this);
        smokeStartTextView.setOnClickListener(this);
        smokeStopTextView.setOnClickListener(this);
        smokeSilenceTextView.setOnClickListener(this);
        uploadIntervalLayout.setOnClickListener(this);
        confirmLayout.setOnClickListener(this);
        pitchAngleRelativeLayout.setOnClickListener(this);
        pitchAngleLowerRelativeLayout.setOnClickListener(this);
        rollAngleUpperRelativeLayout.setOnClickListener(this);
        rollAngleLowerRelativeLayout.setOnClickListener(this);
        yawAngleUpperRelativeLayout.setOnClickListener(this);
        yawAngleLowerRelativeLayout.setOnClickListener(this);
        waterPressureUpperRelativeLayout.setOnClickListener(this);
        waterPressureLowerRelativeLayout.setOnClickListener(this);
    }

    private void refresh() {

        if (targetDeviceIndex == 0) {
            registerUiEvent();
            progressDialog.dismiss();
            if (targetDevice.hasIbeacon()) {
                ibeaconLayout.setVisibility(VISIBLE);
            } else {
                ibeaconLayout.setVisibility(GONE);
            }
            if (targetDevice.hasBleParam()) {
                bleLayout.setVisibility(VISIBLE);
            } else {
                bleLayout.setVisibility(GONE);
            }
            if (targetDevice.hasLoraParam()) {
                loraLayout.setVisibility(VISIBLE);
                if (targetDevice.hasMaxEirp()) {
                    loraEirpRelativeLayout.setVisibility(VISIBLE);
                    loraTxpRelativeLayout.setVisibility(GONE);
                } else {
                    loraEirpRelativeLayout.setVisibility(GONE);
                    loraTxpRelativeLayout.setVisibility(VISIBLE);
                }
                if (targetDevice.hasLoraInterval()) {
                    loraAdIntervalRelativeLayout.setVisibility(VISIBLE);
                } else {
                    loraAdIntervalRelativeLayout.setVisibility(GONE);
                }
            } else {
                loraLayout.setVisibility(GONE);
            }
            if (targetDevice.hasSensorBroadcast()) {
                sensorBroadcastEnableLayout.setVisibility(VISIBLE);
            } else {
                sensorBroadcastEnableLayout.setVisibility(GONE);
            }
            if (targetDevice.hasEddyStone()) {
                eddystoneLayout.setVisibility(VISIBLE);
            } else {
                eddystoneLayout.setVisibility(GONE);
            }
            if (targetDevice.hasCustomPackage()) {
                customLayout.setVisibility(VISIBLE);
            } else {
                customLayout.setVisibility(GONE);
            }
            if (targetDevice.hasSensorParam()) {
                sensorParamLayout.setVisibility(VISIBLE);
                SensoroSensorTest sensoroSensorTest = targetDevice.getSensoroSensorTest();
                if (sensoroSensorTest.hasCo) {
                    coLinearLayout.setVisibility(VISIBLE);
                } else {
                    coLinearLayout.setVisibility(GONE);
                }
                if (sensoroSensorTest.hasCo2) {
                    co2LinearLayout.setVisibility(VISIBLE);
                } else {
                    co2LinearLayout.setVisibility(GONE);
                }
                if (sensoroSensorTest.hasNo2) {
                    no2LinearLayout.setVisibility(VISIBLE);
                } else {
                    no2LinearLayout.setVisibility(GONE);
                }
                if (sensoroSensorTest.hasCh4) {
                    ch4LinearLayout.setVisibility(VISIBLE);
                } else {
                    ch4LinearLayout.setVisibility(GONE);
                }
                if (sensoroSensorTest.hasLpg) {
                    lpgLinearLayout.setVisibility(VISIBLE);
                } else {
                    lpgLinearLayout.setVisibility(GONE);
                }

                if (sensoroSensorTest.hasPm25) {
                    pm25LinearLayout.setVisibility(VISIBLE);
                } else {
                    pm25LinearLayout.setVisibility(GONE);
                }
                if (sensoroSensorTest.hasPm10) {
                    pm10LinearLayout.setVisibility(VISIBLE);
                } else {
                    pm10LinearLayout.setVisibility(GONE);
                }
                if (sensoroSensorTest.hasTemperature) {
                    tempUpperTextView.setText(sensoroSensorTest.temperature.alarmHigh_float + "");
                    tempLowerTextView.setText(sensoroSensorTest.temperature.alarmLow_float + "");
                    tempLinearLayout.setVisibility(VISIBLE);
                } else {
                    tempLinearLayout.setVisibility(GONE);
                }
                if (sensoroSensorTest.hasHumidity) {
                    humidityUpperTextView.setText(sensoroSensorTest.humidity.alarmHigh_float + "");
                    humidityLowerTextView.setText(sensoroSensorTest.humidity.alarmLow_float + "");
                    humidityLinearLayout.setVisibility(VISIBLE);
                } else {
                    humidityLinearLayout.setVisibility(GONE);
                }
                if (sensoroSensorTest.hasSmoke) {
                    smokeLinearLayout.setVisibility(VISIBLE);
                    if (sensoroSensorTest.smoke.status == 0) {
                        smokeStatusTextView.setText(getResources().getStringArray(R.array.smoke_status_array)[0]);
                    } else {
                        smokeStatusTextView.setText(getResources().getStringArray(R.array.smoke_status_array)[1]);
                    }

                } else {
                    smokeLinearLayout.setVisibility(GONE);
                }
                if (sensoroSensorTest.hasPitch) {
                    pitchAngleLinearLayout.setVisibility(VISIBLE);
                    pitchAngleUpperTextView.setText(sensoroSensorTest.pitch.alarmHigh_float + "");
                    pitchAngleLowerTextView.setText(sensoroSensorTest.pitch.alarmLow_float + "");
                } else {
                    pitchAngleLinearLayout.setVisibility(GONE);
                }
                if (sensoroSensorTest.hasRoll) {
                    rollAngleLinearLayout.setVisibility(VISIBLE);
                    rollAngleUpperTextView.setText(sensoroSensorTest.roll.alarmHigh_float + "");
                    rollAngleLowerTextView.setText(sensoroSensorTest.roll.alarmLow_float + "");
                } else {
                    rollAngleLinearLayout.setVisibility(GONE);
                }
                if (sensoroSensorTest.hasYaw) {
                    yawAngleLinearLayout.setVisibility(VISIBLE);
                    yawAngleUpperTextView.setText(sensoroSensorTest.yaw.alarmHigh_float + "");
                    yawAngleLowerTextView.setText(sensoroSensorTest.yaw.alarmLow_float + "");
                } else {
                    yawAngleLinearLayout.setVisibility(GONE);
                }
                if (sensoroSensorTest.hasWaterPressure) {
                    waterPressureLinearLayout.setVisibility(VISIBLE);
                    waterPressureUpperTextView.setText(sensoroSensorTest.waterPressure.alarmHigh_float + "");
                    waterPressureLowerTextView.setText(sensoroSensorTest.waterPressure.alarmLow_float + "");
                } else {
                    waterPressureLinearLayout.setVisibility(GONE);
                }
            } else {
                sensorParamLayout.setVisibility(GONE);
            }
            if (targetDevice.hasAppParam()) {
                appParamLayout.setVisibility(VISIBLE);
                if (targetDevice.hasUploadInterval()) {
                    uploadIntervalLayout.setVisibility(VISIBLE);
                } else {
                    uploadIntervalLayout.setVisibility(GONE);
                }
                if (targetDevice.hasConfirm()) {
                    confirmLayout.setVisibility(VISIBLE);
                } else {
                    confirmLayout.setVisibility(GONE);
                }
            } else {
                appParamLayout.setVisibility(GONE);
            }

        } else {
            progressDialog.show();
            progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string
                    .connect_success));
            saveConfiguration();
        }
        refreshDevice();
    }

    private void refreshDevice() {
        if (targetDevice.hasIbeacon()) {
            uuid = targetDevice.getProximityUUID();
            major = targetDevice.getMajor();
            minor = targetDevice.getMinor();
        }

        if (targetDevice.hasBleParam()) {
            bleInt = targetDevice.getBleInt();
            bleTxp = targetDevice.getBleTxp();
            bleTurnOffTime = targetDevice.getBleOffTime();
            bleTurnOnTime = targetDevice.getBleOnTime();
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
            int offset = zoneOffset / 60 / 60 / 1000;
            bleTurnOnTime += offset;
            bleTurnOffTime += offset;
        }

        if (targetDevice.hasLoraParam()) {
            loraInt = targetDevice.getLoraInt();
            loraTxp = targetDevice.getLoraTxp();
        }

        if (targetDevice.hasEddyStone()) {
            sensoroSlotArray = targetDevice.getSlotArray();
            float firmware_version = Float.valueOf(targetDevice.getFirmwareVersion());
            if (firmware_version > SensoroDevice.FV_1_2) { // 1.3以后没有custom package 3
                findViewById(R.id.settings_multi_ll_custome_package3).setVisibility(GONE);
            } else {
                findViewById(R.id.settings_multi_ll_sensor_enable).setVisibility(GONE);
            }
            if (sensoroSlotArray != null) {
                refreshSlot();
            }
        }

        if (targetDevice.hasSensorParam()) {
            sensorParamLayout.setVisibility(VISIBLE);
            SensoroSensorTest sensoroSensorTest = targetDevice.getSensoroSensorTest();
            if (sensoroSensorTest.hasCo) {
                coAlarmHigh = sensoroSensorTest.co.alarmHigh_float;
            }
            if (sensoroSensorTest.hasCo2) {
                co2AlarmHigh = sensoroSensorTest.co2.alarmHigh_float;
            }
            if (sensoroSensorTest.hasNo2) {
                no2AlarmHigh = sensoroSensorTest.no2.alarmHigh_float;
            }
            if (sensoroSensorTest.hasCh4) {
                ch4AlarmHigh = sensoroSensorTest.ch4.alarmHigh_float;
            }
            if (sensoroSensorTest.hasLpg) {
                lpgAlarmHigh = sensoroSensorTest.lpg.alarmHigh_float;
            }
            if (sensoroSensorTest.hasPm25) {
                pm25AlarmHigh = sensoroSensorTest.pm25.alarmHigh_float;
            }
            if (sensoroSensorTest.hasPm10) {
                pm10AlarmHigh = sensoroSensorTest.pm10.alarmHigh_float;
            }
            if (sensoroSensorTest.hasTemperature) {
                tempAlarmHigh = sensoroSensorTest.temperature.alarmHigh_float;
                tempAlarmLow = sensoroSensorTest.temperature.alarmLow_float;
            }
            if (sensoroSensorTest.hasHumidity) {
                humidityAlarmHigh = sensoroSensorTest.humidity.alarmHigh_float;
                humidityAlarmLow = sensoroSensorTest.humidity.alarmLow_float;
            }
            if (sensoroSensorTest.hasSmoke) {
                //TODO ?????
            }
            if (sensoroSensorTest.hasPitch) {
                pitchAngleAlarmHigh = sensoroSensorTest.pitch.alarmHigh_float;
                pitchAngleAlarmLow = sensoroSensorTest.pitch.alarmLow_float;
            }
            if (sensoroSensorTest.hasRoll) {
                rollAngleAlarmHigh = sensoroSensorTest.roll.alarmHigh_float;
                rollAngleAlarmLow = sensoroSensorTest.roll.alarmLow_float;
            }
            if (sensoroSensorTest.hasYaw) {
                yawAngleAlarmHigh = sensoroSensorTest.yaw.alarmHigh_float;
                yawAngleAlarmLow = sensoroSensorTest.yaw.alarmLow_float;

            }
            if (sensoroSensorTest.hasWaterPressure) {
                waterPressureAlarmHigh = sensoroSensorTest.waterPressure.alarmHigh_float;
                waterPressureAlarmLow = sensoroSensorTest.waterPressure.alarmLow_float;

            }
        }
        if (targetDevice.hasAppParam()) {
            if (targetDevice.hasUploadInterval()) {
                uploadInterval = targetDevice.getUploadInterval();
            }
            if (targetDevice.hasConfirm()) {
                appParamConfirm = targetDevice.getConfirm();
            }
        }

    }

    public void refreshSlot() {
        for (int i = 0; i < sensoroSlotArray.length; i++) {
            SensoroSlot slot = sensoroSlotArray[i];
            switch (slot.getType()) {
                case ProtoMsgCfgV1U1.SlotType.SLOT_SENSOR_VALUE:

                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_NONE_VALUE:
                    switch (slot.getIndex()) {
                        case EDDYSTONE_SLOT_CUSTOM1:
                            isCustomPackage1Enabled = false;
                            break;
                        case EDDYSTONE_SLOT_CUSTOM2:
                            isCustomPackage2Enabled = false;
                            break;
                        case EDDYSTONE_SLOT_CUSTOM3:
                            isCustomPackage3Enabled = false;
                            break;
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_UID_VALUE:
                    slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_UID;
                    switch (slot.getIndex()) {
                        case 0:
                            if (slot.isActived() == 1) {
                                slot1_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                        case 1:
                            if (slot.isActived() == 1) {
                                slot2_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                        case 2:
                            if (slot.isActived() == 1) {
                                slot3_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                        case 3:
                            if (slot.isActived() == 1) {
                                slot4_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_URL_VALUE:
                    slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_URL;
                    switch (slot.getIndex()) {
                        case 0:
                            if (slot.isActived() == 1) {
                                slot1_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                        case 1:
                            if (slot.isActived() == 1) {
                                slot2_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                        case 2:
                            if (slot.isActived() == 1) {
                                slot3_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                        case 3:
                            if (slot.isActived() == 1) {
                                slot4_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_EID_VALUE:
                    slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_EID;
                    switch (slot.getIndex()) {
                        case 0:
                            if (slot.isActived() == 1) {
                                slot1_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                        case 1:
                            if (slot.isActived() == 1) {
                                slot2_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                        case 2:
                            if (slot.isActived() == 1) {
                                slot3_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                        case 3:
                            if (slot.isActived() == 1) {
                                slot4_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_TLM_VALUE:
                    slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_TLM;
                    switch (slot.getIndex()) {
                        case 0:
                            if (slot.isActived() == 1) {
                                slot1_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                        case 1:
                            if (slot.isActived() == 1) {
                                slot2_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                        case 2:
                            if (slot.isActived() == 1) {
                                slot3_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                        case 3:
                            if (slot.isActived() == 1) {
                                slot4_frame = slot.getFrame();
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                            }
                            break;
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_CUSTOME_VALUE:
                    switch (slot.getIndex()) {
                        case 5:
                            custom_package1 = slot.getFrame();
                            isCustomPackage1Enabled = slot.isActived() == 1 ? true : false;
                            break;
                        case 6:
                            custom_package2 = slot.getFrame();
                            isCustomPackage2Enabled = slot.isActived() == 1 ? true : false;
                            break;
                        case 7:
                            custom_package3 = slot.getFrame();
                            isCustomPackage3Enabled = slot.isActived() == 1 ? true : false;
                            break;
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_IBEACON_VALUE:
                    isIBeaconEnabled = sensoroSlotArray[i].isActived() == 1 ? true : false;
                    break;
            }
        }
    }


    private void save() {
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
                SettingMultiDeviceActivity.this.finish();
                Toast.makeText(getApplicationContext(), getString(R.string.save_finish), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveConfiguration() {
        if (targetDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_05) {
            saveConfigurationWithHighVersion();
        } else {
            saveConfigurationWithLowVersion();
        }
    }

    private void saveConfigurationWithLowVersion() {
        try {
            sensoroSlotArray = targetDevice.getSlotArray();
            int slot1_type = 0;
            boolean slot1_status = false;

            switch (slotItemSelectIndex[0]) {
                case STATUS_SLOT_DISABLED:
                    slot1_status = false;
                    break;
                case STATUS_SLOT_UID:
                    slot1_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_UID_VALUE;
                    slot1_status = true;
                    break;
                case STATUS_SLOT_URL:
                    slot1_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_URL_VALUE;
                    slot1_status = true;
                    break;
                case STATUS_SLOT_EID:
                    slot1_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_EID_VALUE;
                    slot1_status = true;
                    break;
                case STATUS_SLOT_TLM:
                    slot1_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_TLM_VALUE;
                    slot1_status = true;
                    break;
            }

            if (slot1_status) {
                sensoroSlotArray[0].setType(slot1_type);
                sensoroSlotArray[0].setFrame(slot1_frame);
            }
            sensoroSlotArray[0].setActived(slot1_status ? 1 : 0);

            int slot2_type = 0;
            boolean slot2_status = false;
            switch (slotItemSelectIndex[1]) {
                case STATUS_SLOT_DISABLED:
                    slot2_status = false;
                    break;
                case STATUS_SLOT_UID:
                    slot2_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_UID_VALUE;
                    slot2_status = true;
                    break;
                case STATUS_SLOT_URL:
                    slot2_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_URL_VALUE;
                    slot2_status = true;
                    break;
                case STATUS_SLOT_EID:
                    slot2_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_EID_VALUE;
                    slot2_status = true;
                    break;
                case STATUS_SLOT_TLM:
                    slot2_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_TLM_VALUE;
                    slot2_status = true;
                    break;
            }
            if (slot2_status) {
                sensoroSlotArray[1].setType(slot2_type);
                sensoroSlotArray[1].setFrame(slot2_frame);
            }
            sensoroSlotArray[1].setActived(slot2_status ? 1 : 0);

            int slot3_type = 0;
            boolean slot3_status = false;
            switch (slotItemSelectIndex[2]) {
                case STATUS_SLOT_DISABLED:
                    slot3_status = false;
                    break;
                case STATUS_SLOT_UID:
                    slot3_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_UID_VALUE;
                    slot3_status = true;
                    break;
                case STATUS_SLOT_URL:
                    slot3_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_URL_VALUE;
                    slot3_status = true;
                    break;
                case STATUS_SLOT_EID:
                    slot3_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_EID_VALUE;
                    slot3_status = true;
                    break;
                case STATUS_SLOT_TLM:
                    slot3_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_TLM_VALUE;
                    slot3_status = true;
                    break;
            }
            if (slot3_status) {
                sensoroSlotArray[2].setType(slot3_type);
                sensoroSlotArray[2].setFrame(slot3_frame);
            }
            sensoroSlotArray[2].setActived(slot3_status ? 1 : 0);

            int slot4_type = 0;
            boolean slot4_status = false;
            switch (slotItemSelectIndex[3]) {
                case STATUS_SLOT_DISABLED:
                    slot4_status = false;
                    break;
                case STATUS_SLOT_UID:
                    slot4_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_UID_VALUE;
                    slot4_status = true;
                    break;
                case STATUS_SLOT_URL:
                    slot4_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_URL_VALUE;
                    slot4_status = true;
                    break;
                case STATUS_SLOT_EID:
                    slot4_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_EID_VALUE;
                    slot4_status = true;
                    break;
                case STATUS_SLOT_TLM:
                    slot4_type = ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_TLM_VALUE;
                    slot4_status = true;
                    break;
            }
            if (slot4_status) {
                sensoroSlotArray[3].setType(slot4_type);
                sensoroSlotArray[3].setFrame(slot4_frame);
            }
            sensoroSlotArray[3].setActived(slot4_status ? 1 : 0);

            if (isIBeaconEnabled) {
                sensoroSlotArray[4].setActived(1);
                sensoroSlotArray[4].setType(ProtoMsgCfgV1U1.SlotType.SLOT_IBEACON_VALUE);
            } else {
                sensoroSlotArray[4].setActived(0);
                sensoroSlotArray[4].setType(ProtoMsgCfgV1U1.SlotType.SLOT_IBEACON_VALUE);
            }

            if (isCustomPackage1Enabled) {
                sensoroSlotArray[5].setFrame(custom_package1);
                sensoroSlotArray[5].setType(ProtoMsgCfgV1U1.SlotType.SLOT_CUSTOME_VALUE);
            }
            sensoroSlotArray[5].setActived(isCustomPackage1Enabled ? 1 : 0);
            if (isCustomPackage2Enabled) {
                sensoroSlotArray[6].setFrame(custom_package2);
                sensoroSlotArray[6].setType(ProtoMsgCfgV1U1.SlotType.SLOT_CUSTOME_VALUE);
            }
            sensoroSlotArray[6].setActived(isCustomPackage2Enabled ? 1 : 0);
            float firmware_version = Float.valueOf(targetDevice.getFirmwareVersion());
            if (firmware_version > SensoroDevice.FV_1_2) {
                sensoroSlotArray[7].setType(ProtoMsgCfgV1U1.SlotType.SLOT_SENSOR_VALUE);
                sensoroSlotArray[7].setActived(isSensorBroadcastEnabled ? 1 : 0);
            } else {
                if (isCustomPackage3Enabled) {
                    sensoroSlotArray[7].setFrame(custom_package3);
                    sensoroSlotArray[7].setType(ProtoMsgCfgV1U1.SlotType.SLOT_CUSTOME_VALUE);
                }
                sensoroSlotArray[7].setActived(isCustomPackage3Enabled ? 1 : 0);
            }


            SensoroDeviceConfiguration.Builder builder = new SensoroDeviceConfiguration.Builder();
            builder.setIBeaconEnabled(isIBeaconEnabled)
                    .setProximityUUID(uuid)
                    .setMajor(major)
                    .setMinor(minor)
                    .setBleTurnOnTime(bleTurnOnTime)
                    .setBleTurnOffTime(bleTurnOffTime)
                    .setBleInt(bleInt)
                    .setBleTxp(bleTxp)
                    .setLoraTxp(loraTxp)
                    .setLoraInt(loraInt)
                    .setAppEui(targetDevice.getAppEui())
                    .setAppKey(targetDevice.getAppKey())
                    .setAppSkey(targetDevice.getAppSkey())
                    .setNwkSkey(targetDevice.getNwkSkey())
                    .setDevAdr(targetDevice.getDevAdr())
                    .setLoraAdr(targetDevice.getLoraAdr())
                    .setLoraDr(targetDevice.getLoraDr())
                    .setSensoroSlotArray(sensoroSlotArray);
            if (targetDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_04) {
                builder.setClassBEnabled(targetDevice.getClassBEnabled())
                        .setClassBPeriodicity(targetDevice.getClassBPeriodicity())
                        .setClassBDataRate(targetDevice.getClassBDataRate());
            }
            String password = targetDevice.getPassword();
            if (password == null || (password != null && !password.equals(""))) {
                builder.setPassword(password);
            }
            deviceConfiguration = builder.build();
            sensoroDeviceConnection.writeMultiDataConfiguration(deviceConfiguration, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveConfigurationWithHighVersion() {
        try {
            SensoroDeviceConfiguration.Builder builder = new SensoroDeviceConfiguration.Builder();
            SensoroSensorConfiguration.Builder sensorBuilder = new SensoroSensorConfiguration.Builder();
            if (targetDevice.hasSensorParam()) {
                SensoroSensorTest sensoroSensorTest = targetDevice.getSensoroSensorTest();
                if (sensoroSensorTest.hasCo) {
                    sensorBuilder.setCoAlarmHigh(coAlarmHigh);
                    sensorBuilder.setCoData(sensoroSensorTest.co.data_float);
                    sensorBuilder.setHasCo(sensoroSensorTest.hasCo);
                }
                if (sensoroSensorTest.hasCo2) {
                    sensorBuilder.setCo2AlarmHigh(co2AlarmHigh);
                    sensorBuilder.setCo2Data(sensoroSensorTest.co.data_float);
                    sensorBuilder.setHasCo2(sensoroSensorTest.hasCo2);
                }
                if (sensoroSensorTest.hasNo2) {
                    sensorBuilder.setNo2AlarmHigh(no2AlarmHigh);
                    sensorBuilder.setNo2Data(sensoroSensorTest.no2.data_float);
                    sensorBuilder.setHasNo2(sensoroSensorTest.hasNo2);
                }
                if (sensoroSensorTest.hasCh4) {
                    sensorBuilder.setCh4AlarmHigh(ch4AlarmHigh);
                    sensorBuilder.setCh4Data(sensoroSensorTest.ch4.data_float);
                    sensorBuilder.setHasCh4(sensoroSensorTest.hasCh4);
                }
                if (sensoroSensorTest.hasLpg) {
                    sensorBuilder.setLpgAlarmHigh(lpgAlarmHigh);
                    sensorBuilder.setLpgData(sensoroSensorTest.lpg.data_float);
                    sensorBuilder.setHasLpg(sensoroSensorTest.hasLpg);
                }
                if (sensoroSensorTest.hasPm10) {
                    sensorBuilder.setPm10AlarmHigh(pm10AlarmHigh);
                    sensorBuilder.setPm10Data(sensoroSensorTest.pm10.data_float);
                    sensorBuilder.setHasPm10(sensoroSensorTest.hasPm10);
                }
                if (sensoroSensorTest.hasPm25) {
                    sensorBuilder.setPm25AlarmHigh(pm25AlarmHigh);
                    sensorBuilder.setPm25Data(sensoroSensorTest.pm25.data_float);
                    sensorBuilder.setHasPm25(sensoroSensorTest.hasPm25);
                }
                if (sensoroSensorTest.hasTemperature) {
                    sensorBuilder.setTempAlarmHigh(tempAlarmHigh);
                    sensorBuilder.setTempAlarmLow(tempAlarmLow);
                    sensorBuilder.setHasTemperature(sensoroSensorTest.hasTemperature);
                }
                if (sensoroSensorTest.hasHumidity) {
                    sensorBuilder.setHumidityHigh(humidityAlarmHigh);
                    sensorBuilder.setHumidityLow(humidityAlarmLow);
                    sensorBuilder.setHasHumidity(sensoroSensorTest.hasHumidity);
                }
                if (sensoroSensorTest.hasPitch) {
                    sensorBuilder.setPitchAngleAlarmHigh(pitchAngleAlarmHigh);
                    sensorBuilder.setPitchAngleAlarmLow(pitchAngleAlarmLow);
                    sensorBuilder.setHasPitchAngle(sensoroSensorTest.hasPitch);
                }
                if (sensoroSensorTest.hasRoll) {
                    sensorBuilder.setRollAngleAlarmHigh(rollAngleAlarmHigh);
                    sensorBuilder.setRollAngleAlarmLow(rollAngleAlarmLow);
                    sensorBuilder.setHasRollAngle(sensoroSensorTest.hasRoll);
                }
                if (sensoroSensorTest.hasYaw) {
                    sensorBuilder.setYawAngleAlarmHigh(yawAngleAlarmHigh);
                    sensorBuilder.setYawAngleAlarmLow(yawAngleAlarmLow);
                    sensorBuilder.setHasYawAngle(sensoroSensorTest.hasYaw);
                }
                if (sensoroSensorTest.hasWaterPressure) {
                    sensorBuilder.setWaterPressureAlarmHigh(waterPressureAlarmHigh);
                    sensorBuilder.setWaterPressureAlarmLow(waterPressureAlarmLow);
                    sensorBuilder.setHasWaterPressure(sensoroSensorTest.hasWaterPressure);
                }
            }

            if (targetDevice.hasAppParam()) {
                if (targetDevice.hasUploadInterval()) {
                    builder.setHasUploadInterval(targetDevice.hasUploadInterval());
                    builder.setUploadIntervalData(uploadInterval);
                }
                if (targetDevice.hasConfirm()) {
                    builder.setConfirmData(appParamConfirm);
                    builder.setHasConfirm(targetDevice.hasConfirm());

                }
                builder.setHasAppParam(targetDevice.hasAppParam());
            }
            builder.setHasBleParam(targetDevice.hasBleParam());
            builder.setHasLoraParam(targetDevice.hasLoraParam());
            builder.setBleTurnOnTime(bleTurnOnTime)
                    .setBleTurnOffTime(bleTurnOffTime)
                    .setBleInt(bleInt)
                    .setBleTxp(bleTxp)
                    .setLoraTxp(loraTxp)
                    .setAppEui(targetDevice.getAppEui())
                    .setAppKey(targetDevice.getAppKey())
                    .setAppSkey(targetDevice.getAppSkey())
                    .setNwkSkey(targetDevice.getNwkSkey())
                    .setDevAdr(targetDevice.getDevAdr())
                    .setLoraDr(targetDevice.getLoraDr())
                    .setLoraAdr(targetDevice.getLoraAdr());
            if (targetDevice.getPassword() == null || (targetDevice.getPassword() != null && !targetDevice
                    .getPassword().equals(""))) {
                builder.setPassword(targetDevice.getPassword());
            }
            SensoroSensorConfiguration sensorConfiguration = sensorBuilder.build();
            builder.setSensorConfiguration(sensorConfiguration);
            deviceConfiguration = builder.build();
            sensoroDeviceConnection.writeMultiData05Configuration(deviceConfiguration, this);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    protected void doSmokeStart() {
        progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string.smoke_test));
        MsgNode1V1M5.AppParam.Builder appParamBuilder = MsgNode1V1M5.AppParam.newBuilder();
        appParamBuilder.setSmokeCtrl(MsgNode1V1M5.SmokeCtrl.SMOKE_INSPECTION_TEST);
        sensoroDeviceConnection.writeSmokeCmd(appParamBuilder, this);
    }

    protected void doSmokeStop() {
        progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string.smoke_test));
        MsgNode1V1M5.AppParam.Builder appParamBuilder = MsgNode1V1M5.AppParam.newBuilder();
        appParamBuilder.setSmokeCtrl(MsgNode1V1M5.SmokeCtrl.SMOKE_INSPECTION_OVER);
        sensoroDeviceConnection.writeSmokeCmd(appParamBuilder, this);

    }

    protected void doSmokeSilence() {
        progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string.smoke_test));
        MsgNode1V1M5.AppParam.Builder appParamBuilder = MsgNode1V1M5.AppParam.newBuilder();
        appParamBuilder.setSmokeCtrl(MsgNode1V1M5.SmokeCtrl.SMOKE_ERASURE);
        sensoroDeviceConnection.writeSmokeCmd(appParamBuilder, this);
    }


    @Override
    public void onWriteSuccess(Object o, final int cmd) {
        System.out.println("写入成功=====>");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (cmd == CmdType.CMD_SET_SMOKE) {
                    connectDeviceWithCommand();
                } else {
                    progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string
                            .save_succ));
//                Toast.makeText(getApplicationContext(), getString(R.string.device) + targetDevice.getSn() +
// getString(R.string.save_succ), Toast.LENGTH_SHORT).show();
                    switch (targetDevice.getDataVersion()) {
                        case SensoroDeviceConnection.DATA_VERSION_05:
                            postUpdateData05();
                            break;
                        default:
                            postUpdateData();
                            break;
                    }
                    save();
                }

            }
        });

    }

    @Override
    public void onWriteFailure(final int errorCode, final int cmd) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("写入失败,错误码=====>" + errorCode);
                progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string
                        .save_fail) + " 错误码" + errorCode);
//                Toast.makeText(getApplicationContext(), getString(R.string.device) + targetDevice.getSn() +
// getString(R.string.save_fail), Toast.LENGTH_SHORT).show();
                save();
            }
        });

    }


    @Override
    public void onConnectedSuccess(BLEDevice bleDevice, int cmd) {
        System.out.println(bleDevice.getSn() + "连接成功=====>");
        if (cmd == CmdType.CMD_SET_SMOKE) {
            if (targetDeviceIndex < (targetDeviceList.size() - 1)) {
                targetDeviceIndex++;
                targetDevice = targetDeviceList.get(targetDeviceIndex);
                switch (smokeActionIndex) {
                    case SMOKE_ACTION_SILENCE:
                        doSmokeSilence();
                        break;
                    case SMOKE_ACTION_START:
                        doSmokeStart();
                        break;
                    case SMOKE_ACTION_STOP:
                        doSmokeStop();
                        break;
                }
            } else {
                progressDialog.dismiss();
            }
        } else {
            String sn = targetDevice.getSn();
            String firmwareVersion = targetDevice.getFirmwareVersion();
            targetDevice = (SensoroDevice) bleDevice;
            targetDevice.setFirmwareVersion(firmwareVersion);
            targetDevice.setSn(sn);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            });
        }

    }


    @Override
    public void onConnectedFailure(final int errorCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("连接错误,错误码=====>" + errorCode);
//                Toast.makeText(getApplicationContext(), getString(R.string.device) + targetDevice.getSn() +
// getString(R.string.connect_failed), Toast.LENGTH_SHORT).show();
                if (targetDeviceIndex != 0) {
                    progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string
                            .connect_failed));
                    save();
                } else {
                    Toast.makeText(application, R.string.connect_failed, Toast.LENGTH_SHORT).show();
                    SettingMultiDeviceActivity.this.finish();
                }
            }
        });
    }

    @Override
    public void onDisconnected() {
        Log.v(TAG, "onDisconnected");
    }


    public void connectDevice() {
        try {
            if (targetDeviceIndex != 0) {
                progressDialog.setTitle(getString(R.string.settings));
                progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R.string
                        .saving));
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

    protected void connectDeviceWithCommand() {
        try {
            if (sensoroDeviceConnection != null) {
                sensoroDeviceConnection.disconnect();
            }
            progressDialog.setMessage(getString(R.string.connecting));
            progressDialog.show();
            sensoroDeviceConnection = new SensoroDeviceConnection(this, targetDevice.getMacAddress());
            sensoroDeviceConnection.connect(targetDevice.getPassword(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSlot1Dialog(DialogFragment dialogFragment) {
        switch (slotItemSelectIndex[0]) {
            case STATUS_SLOT_DISABLED:
                break;
            case STATUS_SLOT_EID:
                requestEidData(LAYOUT_EDDYSTONE_SLOT1);
                break;
            case STATUS_SLOT_TLM:
                break;
            case STATUS_SLOT_UID:
                dialogFragment = SettingsInputDialogFragment.newInstance(eddyStoneSlot1Item2Value.getText().toString());
                dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE1_UID);
                break;
            case STATUS_SLOT_URL:
                dialogFragment = SettingsInputDialogFragment.newInstance(eddyStoneSlot1Item2Value.getText().toString());
                dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE1_URL);
                break;
        }
    }

    private void showSlot2Dialog(DialogFragment dialogFragment) {
        switch (slotItemSelectIndex[1]) {
            case STATUS_SLOT_DISABLED:
                break;
            case STATUS_SLOT_EID:
                requestEidData(LAYOUT_EDDYSTONE_SLOT2);
                break;
            case STATUS_SLOT_TLM:
                break;
            case STATUS_SLOT_UID:
                dialogFragment = SettingsInputDialogFragment.newInstance(eddyStoneSlot2Item2Value.getText().toString());
                dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE2_UID);
                break;
            case STATUS_SLOT_URL:
                dialogFragment = SettingsInputDialogFragment.newInstance(eddyStoneSlot2Item2Value.getText().toString());
                dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE2_URL);
                break;
        }
    }

    private void showSlot3Dialog(DialogFragment dialogFragment) {
        switch (slotItemSelectIndex[2]) {
            case STATUS_SLOT_DISABLED:
                break;
            case STATUS_SLOT_EID:
                requestEidData(LAYOUT_EDDYSTONE_SLOT3);
                break;
            case STATUS_SLOT_TLM:
                break;
            case STATUS_SLOT_UID:
                dialogFragment = SettingsInputDialogFragment.newInstance(eddyStoneSlot3Item2Value.getText().toString());
                dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE3_UID);
                break;
            case STATUS_SLOT_URL:
                dialogFragment = SettingsInputDialogFragment.newInstance(eddyStoneSlot3Item2Value.getText().toString());
                dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE3_URL);
                break;
        }
    }

    private void showSlot4Dialog(DialogFragment dialogFragment) {
        switch (slotItemSelectIndex[3]) {
            case STATUS_SLOT_DISABLED:
                break;
            case STATUS_SLOT_EID:
                requestEidData(LAYOUT_EDDYSTONE_SLOT4);
                break;
            case STATUS_SLOT_TLM:
                break;
            case STATUS_SLOT_UID:
                dialogFragment = SettingsInputDialogFragment.newInstance(eddyStoneSlot4Item2Value.getText().toString());
                dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE4_UID);
                break;
            case STATUS_SLOT_URL:
                dialogFragment = SettingsInputDialogFragment.newInstance(eddyStoneSlot4Item2Value.getText().toString());
                dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE4_URL);
                break;
        }
    }

    private void requestEidData(final int index) {
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();
        application.loRaSettingServer.eidList(new Response.Listener<EidInfoListRsp>() {
                                                  @Override
                                                  public void onResponse(EidInfoListRsp response) {
                                                      List<EidInfo> list = response.getData().getItems();
                                                      initEidChoiceDialog(list, index);
                                                  }
                                              }, new Response.ErrorListener() {
                                                  @Override
                                                  public void onErrorResponse(VolleyError error) {
                                                      showErrorInfo();
                                                  }
                                              }

        );
    }

    private void showErrorInfo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                Toast.makeText(SettingMultiDeviceActivity.this, R.string.tips_server_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initEidChoiceDialog(final List<EidInfo> list, final int index) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    if (list.size() > 0) {
                        String ik_items[] = new String[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            EidInfo eidInfo = list.get(i);
                            ik_items[i] = eidInfo.getBeaconIdentityKey();
                        }
                        String tag = SETTINGS_EDDYSTONE1_EID;
                        switch (index) {
                            case LAYOUT_EDDYSTONE_SLOT1:
                                tag = SETTINGS_EDDYSTONE1_EID;
                                break;
                            case LAYOUT_EDDYSTONE_SLOT2:
                                tag = SETTINGS_EDDYSTONE2_EID;
                                break;
                            case LAYOUT_EDDYSTONE_SLOT3:
                                tag = SETTINGS_EDDYSTONE3_EID;
                                break;
                            case LAYOUT_EDDYSTONE_SLOT4:
                                tag = SETTINGS_EDDYSTONE4_EID;
                                break;
                        }
                        DialogFragment dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(ik_items, 0);
                        dialogFragment.show(getFragmentManager(), tag);

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void postUpdateData05() {
        if (deviceConfiguration == null) {
            return;
        }
        String dataString = null;
        String version = null;
        MsgNode1V1M5.MsgNode.Builder msgCfgBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        if (targetDevice.hasLoraParam()) {
            MsgNode1V1M5.LoraParam.Builder loraParamBuilder = MsgNode1V1M5.LoraParam.newBuilder();
            loraParamBuilder.setTxPower(deviceConfiguration.getLoraTxp());
            msgCfgBuilder.setLoraParam(loraParamBuilder);
        }
        if (targetDevice.hasBleParam()) {
            MsgNode1V1M5.BleParam.Builder bleParamBuilder = MsgNode1V1M5.BleParam.newBuilder();
            bleParamBuilder.setBleOnTime(deviceConfiguration.getBleTurnOnTime());
            bleParamBuilder.setBleOffTime(deviceConfiguration.getBleTurnOffTime());
            bleParamBuilder.setBleTxp(deviceConfiguration.getBleTxp());
            bleParamBuilder.setBleInterval(deviceConfiguration.getBleInt());
            msgCfgBuilder.setBleParam(bleParamBuilder);
        }
        if (targetDevice.hasAppParam()) {
            MsgNode1V1M5.AppParam.Builder appParamBuilder = MsgNode1V1M5.AppParam.newBuilder();
            if (targetDevice.hasUploadInterval()) {
                appParamBuilder.setUploadInterval(deviceConfiguration.getUploadIntervalData());
                msgCfgBuilder.setAppParam(appParamBuilder);
            }
            if (targetDevice.hasConfirm()) {
                appParamBuilder.setConfirm(deviceConfiguration.getConfirmData());
                msgCfgBuilder.setAppParam(appParamBuilder);
            }
        }

        if (targetDevice.hasSensorParam()) {
            SensoroSensorTest sensoroSensorTest = targetDevice.getSensoroSensorTest();
            if (sensoroSensorTest.hasCo) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(coAlarmHigh);
                msgCfgBuilder.setCo(builder);
            }
            if (sensoroSensorTest.hasCo2) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(co2AlarmHigh);
                msgCfgBuilder.setCo2(builder);
            }
            if (sensoroSensorTest.hasNo2) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(no2AlarmHigh);
                msgCfgBuilder.setNo2(builder);
            }
            if (sensoroSensorTest.hasCh4) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(ch4AlarmHigh);
                msgCfgBuilder.setCh4(builder);
            }
            if (sensoroSensorTest.hasLpg) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(lpgAlarmHigh);
                msgCfgBuilder.setLpg(builder);
            }
            if (sensoroSensorTest.hasPm10) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(pm10AlarmHigh);
                msgCfgBuilder.setPm10(builder);
            }
            if (sensoroSensorTest.hasPm25) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(pm25AlarmHigh);
                msgCfgBuilder.setPm25(builder);
            }
            if (sensoroSensorTest.hasTemperature) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(tempAlarmHigh);
                builder.setAlarmLow(tempAlarmLow);
                msgCfgBuilder.setTemperature(builder);
            }
            if (sensoroSensorTest.hasHumidity) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(humidityAlarmHigh);
                builder.setAlarmLow(humidityAlarmLow);
                msgCfgBuilder.setHumidity(builder);
            }
            if (sensoroSensorTest.hasPitch) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(pitchAngleAlarmHigh);
                builder.setAlarmLow(pitchAngleAlarmLow);
                msgCfgBuilder.setPitch(builder);
            }
            if (sensoroSensorTest.hasRoll) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(rollAngleAlarmHigh);
                builder.setAlarmLow(rollAngleAlarmLow);
                msgCfgBuilder.setRoll(builder);
            }
            if (sensoroSensorTest.hasYaw) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(yawAngleAlarmHigh);
                builder.setAlarmLow(yawAngleAlarmLow);
                msgCfgBuilder.setYaw(builder);
            }
            if (sensoroSensorTest.hasWaterPressure) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(waterPressureAlarmHigh);
                builder.setAlarmLow(waterPressureAlarmLow);
                msgCfgBuilder.setWaterPressure(builder);
            }
        }

        MsgNode1V1M5.MsgNode msgCfg = msgCfgBuilder.build();
        byte[] data = msgCfg.toByteArray();
        dataString = new String(Base64.encode(data, Base64.DEFAULT));
        version = "05";
        final String baseString = dataString;
        final String versionString = version;
        JSONObject jsonData = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try {
            jsonObject.put("SN", targetDevice.getSn());
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
                    DeviceDataDao.addDeviceItem(targetDevice.getSn(), baseString, versionString);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                DeviceDataDao.addDeviceItem(targetDevice.getSn(), baseString, versionString);
            }
        });
    }


    protected void postUpdateData() {
        if (deviceConfiguration == null) {
            return;
        }
        String dataString = null;
        String version = null;
        float firmware_version = Float.valueOf(targetDevice.getFirmwareVersion());
        ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();

        msgCfgBuilder.setLoraInt(deviceConfiguration.getLoraInt().intValue());
        msgCfgBuilder.setLoraTxp(deviceConfiguration.getLoraTxp());
        msgCfgBuilder.setBleTxp(deviceConfiguration.getBleTxp());
        msgCfgBuilder.setBleInt(deviceConfiguration.getBleInt().intValue());
        msgCfgBuilder.setBleOnTime(deviceConfiguration.getBleTurnOnTime());
        msgCfgBuilder.setBleOffTime(deviceConfiguration.getBleTurnOffTime());
        msgCfgBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getAppEui()))));
        msgCfgBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppKey())));
        msgCfgBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppSkey())));
        msgCfgBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getNwkSkey())));
        msgCfgBuilder.setDevAddr(deviceConfiguration.getDevAdr());
        msgCfgBuilder.setLoraDr(deviceConfiguration.getLoraDr());
        msgCfgBuilder.setLoraAdr(deviceConfiguration.getLoraAdr());

        SensoroSlot[] sensoroSlots = deviceConfiguration.getSensoroSlots();
        for (int i = 0; i < sensoroSlots.length; i++) {
            ProtoMsgCfgV1U1.Slot.Builder builder = ProtoMsgCfgV1U1.Slot.newBuilder();
            SensoroSlot sensoroSlot = sensoroSlots[i];
            if (sensoroSlot.isActived() == 1) {
                if (i == 4) {
                    byte uuid_data[] = SensoroUtils.HexString2Bytes(deviceConfiguration.getProximityUUID());
                    byte major_data[] = SensoroUUID.intToByteArray(deviceConfiguration.getMajor(), 2);
                    byte minor_data[] = SensoroUUID.intToByteArray(deviceConfiguration.getMinor(), 2);
                    byte ibeacon_data[] = new byte[20];
                    System.arraycopy(uuid_data, 0, ibeacon_data, 0, 16);
                    System.arraycopy(major_data, 0, ibeacon_data, 16, 2);
                    System.arraycopy(minor_data, 0, ibeacon_data, 18, 2);
                    builder.setFrame(ByteString.copyFrom(ibeacon_data));
                } else if (i == 5 || i == 6 || i == 7) {
                    String frameString = sensoroSlot.getFrame();
                    if (frameString != null) {
                        builder.setFrame(ByteString.copyFrom(SensoroUtils.HexString2Bytes(frameString)));
                    }

                } else {
                    switch (sensoroSlot.getType()) {
                        case ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_URL_VALUE:
                            builder.setFrame(ByteString.copyFrom(SensoroUtils.encodeUrl(sensoroSlot.getFrame())));
                            break;
                        default:
                            builder.setFrame(ByteString.copyFrom(SensoroUtils.HexString2Bytes(sensoroSlot.getFrame())));
                            break;
                    }
                }
                builder.setIndex(i);
                builder.setType(ProtoMsgCfgV1U1.SlotType.valueOf(sensoroSlot.getType()));
            }
            builder.setActived(sensoroSlot.isActived());
            msgCfgBuilder.addSlot(i, builder.build());
        }


        if (firmware_version <= SensoroDevice.FV_1_2) {//03
            ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();
            byte[] data = msgCfg.toByteArray();
            dataString = new String(Base64.encode(data, Base64.DEFAULT));
            version = "03";
        } else {//04
            ProtoMsgCfgV1U1.MsgCfgV1u1 msgCfg = msgCfgBuilder.build();
            ProtoStd1U1.MsgStd.Builder msgStdBuilder = ProtoStd1U1.MsgStd.newBuilder();
            msgStdBuilder.setCustomData(msgCfg.toByteString());
            msgStdBuilder.setEnableClassB(deviceConfiguration.getClassBEnabled());
            msgStdBuilder.setClassBDataRate(deviceConfiguration.getClassDateRate());
            msgStdBuilder.setClassBPeriodicity(deviceConfiguration.getClassPeriodicity());
            ProtoStd1U1.MsgStd msgStd = msgStdBuilder.build();
            byte[] data = msgStd.toByteArray();
            dataString = new String(Base64.encode(data, Base64.DEFAULT));
            version = "04";
        }
        final String baseString = dataString;
        final String versionString = version;
        JSONObject jsonData = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try {
            jsonObject.put("SN", targetDevice.getSn());
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
                    DeviceDataDao.addDeviceItem(targetDevice.getSn(), baseString, versionString);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                DeviceDataDao.addDeviceItem(targetDevice.getSn(), baseString, versionString);
            }
        });
    }


    public void setSettingsEddystone1(int index) {
        slotItemSelectIndex[0] = index;
        switch (index) {
            case STATUS_SLOT_DISABLED:
                eddyStoneSlot1Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot1Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot1Item2Layout.setVisibility(GONE);
                eddyStoneSlot1Iv2.setVisibility(GONE);
                break;
            case STATUS_SLOT_UID:
                eddyStoneSlot1Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot1Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot1Item2.setText(this.getString(R.string.slot5_name));
                eddyStoneSlot1Item2Value.setText(this.getString(R.string.text_null));
                eddyStoneSlot1Item2Layout.setVisibility(VISIBLE);
                break;
            case STATUS_SLOT_URL:
                eddyStoneSlot1Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot1Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot1Item2.setText(this.getString(R.string.slot6_name));
                eddyStoneSlot1Item2Value.setText(this.getString(R.string.text_null));
                eddyStoneSlot1Item2Layout.setVisibility(VISIBLE);
                break;
            case STATUS_SLOT_EID:
                eddyStoneSlot1Item2Layout.setVisibility(VISIBLE);
                eddyStoneSlot1Iv2.setVisibility(VISIBLE);
                eddyStoneSlot1Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot1Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot1Item2.setText(this.getString(R.string.eid_list));
                eddyStoneSlot1Item2Value.setText(this.getString(R.string.text_null));
                break;
            case STATUS_SLOT_TLM:
                eddyStoneSlot1Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot1Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot1Iv2.setVisibility(GONE);
                eddyStoneSlot1Item2Layout.setVisibility(GONE);
                break;
            case STATUS_SLOT_UNSET:
                eddyStoneSlot1Item1Value.setText(R.string.setting_unset);
                eddyStoneSlot1Item2Layout.setVisibility(GONE);
                eddyStoneSlot1Iv2.setVisibility(GONE);
                break;
        }
    }

    public void setSettingsEddystone2(int index) {
        slotItemSelectIndex[1] = index;
        switch (index) {
            case STATUS_SLOT_DISABLED:
                eddyStoneSlot2Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot2Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot2Iv2.setVisibility(GONE);
                eddyStoneSlot2Item2Value.setText(this.getString(R.string.text_null));
                eddyStoneSlot2Item2Layout.setVisibility(GONE);
                break;
            case STATUS_SLOT_UID:
                eddyStoneSlot2Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot2Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot2Item2.setText(this.getString(R.string.slot5_name));
                eddyStoneSlot2Item2Layout.setVisibility(VISIBLE);

                break;
            case STATUS_SLOT_URL:
                eddyStoneSlot2Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot2Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot2Item2.setText(this.getString(R.string.slot6_name));
                eddyStoneSlot2Item2Value.setText(this.getString(R.string.text_null));
                eddyStoneSlot2Item2Layout.setVisibility(VISIBLE);

                break;
            case STATUS_SLOT_EID:
                eddyStoneSlot2Item2Layout.setVisibility(VISIBLE);
                eddyStoneSlot2Iv2.setVisibility(VISIBLE);
                eddyStoneSlot2Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot2Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot2Item2.setText(this.getString(R.string.eid_list));
                eddyStoneSlot2Item2Value.setText(this.getString(R.string.text_null));
                break;
            case STATUS_SLOT_TLM:
                eddyStoneSlot2Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot2Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot2Item2Layout.setVisibility(GONE);
                eddyStoneSlot2Iv2.setVisibility(GONE);
                break;
            case STATUS_SLOT_UNSET:
                eddyStoneSlot2Item1Value.setText(R.string.setting_unset);
                eddyStoneSlot2Item2Layout.setVisibility(GONE);
                eddyStoneSlot2Iv2.setVisibility(GONE);
                break;
        }
    }

    public void setSettingsEddystone3(int index) {
        slotItemSelectIndex[2] = index;
        switch (index) {
            case STATUS_SLOT_DISABLED:
                eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot3Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot3Item2Layout.setVisibility(GONE);
                eddyStoneSlot2Iv2.setVisibility(GONE);
                break;
            case STATUS_SLOT_UID:
                eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot3Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot3Item2.setText(this.getString(R.string.slot5_name));
                eddyStoneSlot3Item2Value.setText(this.getString(R.string.text_null));
                eddyStoneSlot3Item2Layout.setVisibility(VISIBLE);
                break;
            case STATUS_SLOT_URL:
                eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot3Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot3Item2.setText(this.getString(R.string.slot6_name));
                eddyStoneSlot3Item2Layout.setVisibility(VISIBLE);
                break;
            case STATUS_SLOT_EID:
                eddyStoneSlot3Item2Layout.setVisibility(VISIBLE);
                eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot3Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot3Item2.setText(this.getString(R.string.eid_list));
                eddyStoneSlot3Iv2.setVisibility(VISIBLE);
                break;
            case STATUS_SLOT_TLM:
                eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot3Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot3Item2Layout.setVisibility(GONE);
                eddyStoneSlot3Iv2.setVisibility(GONE);
                break;
            case STATUS_SLOT_UNSET:
                eddyStoneSlot3Item1Value.setText(R.string.setting_unset);
                eddyStoneSlot3Item2Layout.setVisibility(GONE);
                eddyStoneSlot3Iv2.setVisibility(GONE);
                break;
        }
    }

    public void setSettingsEddystone4(int index) {
        slotItemSelectIndex[3] = index;
        switch (index) {
            case STATUS_SLOT_DISABLED:
                eddyStoneSlot4Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot4Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot4Item2Layout.setVisibility(GONE);
                eddyStoneSlot4Iv2.setVisibility(GONE);
                break;
            case STATUS_SLOT_UID:
                eddyStoneSlot4Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot4Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot4Item2.setText(this.getString(R.string.slot5_name));
                eddyStoneSlot4Item2Value.setText(this.getString(R.string.text_null));
                eddyStoneSlot4Item2Layout.setVisibility(VISIBLE);
                break;
            case STATUS_SLOT_URL:
                eddyStoneSlot4Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot4Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot4Item2.setText(this.getString(R.string.slot6_name));
                eddyStoneSlot4Item2Layout.setVisibility(VISIBLE);

                break;
            case STATUS_SLOT_EID:
                eddyStoneSlot4Item2Layout.setVisibility(VISIBLE);
                eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot4Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot4Item2.setText(this.getString(R.string.eid_list));
                eddyStoneSlot4Item2Value.setText(this.getString(R.string.text_null));
                eddyStoneSlot4Iv2.setVisibility(VISIBLE);
                break;
            case STATUS_SLOT_TLM:
                eddyStoneSlot4Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot4Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot4Item2Layout.setVisibility(GONE);
                eddyStoneSlot4Iv2.setVisibility(GONE);
                break;
            case STATUS_SLOT_UNSET:
                eddyStoneSlot4Item1Value.setText(R.string.setting_unset);
                eddyStoneSlot4Item2Layout.setVisibility(GONE);
                eddyStoneSlot4Iv2.setVisibility(GONE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        try {
            DialogFragment dialogFragment = null;
            switch (v.getId()) {
                case R.id.settings_multi_device_back:
                    finish();
                    break;
                case R.id.settings_multi_rl_ibeacon:
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(statusItems, 0);
                    dialogFragment.show(getFragmentManager(), SETTINGS_IBEACON);
                    break;
                case R.id.settings_multi_rl_uuid:
                    dialogFragment = SettingsUUIDDialogFragment.newInstance(uuid);
                    dialogFragment.show(getFragmentManager(), SETTINGS_UUID);
                    break;
                case R.id.settings_multi_rl_major:
                    dialogFragment = SettingsMajorMinorDialogFragment.newInstance(major);
                    dialogFragment.show(getFragmentManager(), SETTINGS_MAJOR);
                    break;
                case R.id.settings_multi_rl_minor:
                    dialogFragment = SettingsMajorMinorDialogFragment.newInstance(minor);
                    dialogFragment.show(getFragmentManager(), SETTINGS_MINOR);
                    break;
                case R.id.settings_multi_rl_power:
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(blePowerItems, 0);
                    dialogFragment.show(getFragmentManager(), SETTINGS_BLE_POWER);
                    break;
                case R.id.settings_multi_rl_adv_interval:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_ADV_INTERVAL);
                    break;
                case R.id.setting_multi_ll_turnon_time:
                    int showTurnOnTime = bleTurnOnTime;
                    if (bleTurnOnTime >= 24) {
                        showTurnOnTime -= 24;
                    }
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(bleTimeItems, showTurnOnTime);
                    dialogFragment.show(getFragmentManager(), SETTINGS_BLE_TURNON_TIME);
                    break;
                case R.id.setting_multi_ll_turnoff_time:
                    int showTurnOffTime = bleTurnOffTime;
                    if (bleTurnOffTime >= 24) {
                        showTurnOffTime -= 24;
                    }
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(bleTimeItems, showTurnOffTime);
                    dialogFragment.show(getFragmentManager(), SETTINGS_BLE_TURNOFF_TIME);
                    break;
                case R.id.settings_multi_rl_lora_transmit_power:
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(loraTxpItems, 0);
                    dialogFragment.show(getFragmentManager(), SETTINGS_LORA_TXP);
                    break;
                case R.id.settings_multi_rl_lora_ad_interval:
                    dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(loraInt));
                    dialogFragment.show(getFragmentManager(), SETTINGS_LORA_INT);
                    break;
                case R.id.settings_multi_rl_lora_eirp:
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(loraEirpItems, loraTxp);
                    dialogFragment.show(getFragmentManager(), SETTINGS_LORA_EIRP);
                    break;
                case R.id.settings_multi_rl_slot1_item1:
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(slotItems, slotItemSelectIndex[0]);
                    dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE1);
                    break;
                case R.id.settings_multi_rl_slot1_item2:
                    showSlot1Dialog(dialogFragment);
                    break;
                case R.id.settings_multi_rl_slot2_item1:
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(slotItems, slotItemSelectIndex[1]);
                    dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE2);
                    break;
                case R.id.settings_multi_rl_slot2_item2:
                    showSlot2Dialog(dialogFragment);
                    break;
                case R.id.settings_multi_rl_slot3_item1:
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(slotItems, slotItemSelectIndex[2]);
                    dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE3);
                    break;
                case R.id.settings_multi_rl_slot3_item2:
                    showSlot3Dialog(dialogFragment);
                    break;

                case R.id.settings_multi_rl_slot4_item1:
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(slotItems, slotItemSelectIndex[3]);
                    dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE4);
                    break;
                case R.id.settings_multi_rl_slot4_item2:
                    showSlot4Dialog(dialogFragment);
                    break;
                case R.id.settings_multi_ll_sensor_enable:
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(statusItems, 0);
                    dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR);
                    break;
                case R.id.settings_multi_rl_custom_package1_state:
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(statusItems, 0);
                    dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE1_STATUS);
                    break;
                case R.id.settings_multi_rl_custom_package2_state:
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(statusItems, 0);
                    dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE2_STATUS);
                    break;
                case R.id.settings_multi_rl_custom_package3_state:
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(statusItems, 0);
                    dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE3_STATUS);
                    break;
                case R.id.settings_multi_rl_custom_package1:
                    dialogFragment = SettingsInputDialogFragment.newInstance(custom_package1);
                    dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE1);
                    break;
                case R.id.settings_multi_rl_custom_package2:
                    dialogFragment = SettingsInputDialogFragment.newInstance(custom_package2);
                    dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE2);
                    break;
                case R.id.settings_multi_rl_custom_package3:
                    dialogFragment = SettingsInputDialogFragment.newInstance(custom_package3);
                    dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE3);
                    break;
                case R.id.settings_multi_ll_co:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_CO);
                    break;
                case R.id.settings_multi_ll_co2:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_CO2);
                    break;
                case R.id.settings_multi_ll_ch4:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_CH4);
                    break;
                case R.id.settings_multi_ll_no2:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_NO2);
                    break;
                case R.id.settings_multi_ll_lpg:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_LPG);
                    break;
                case R.id.settings_multi_ll_pm10:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_PM10);
                    break;
                case R.id.settings_multi_ll_pm25:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_PM25);
                    break;
                case R.id.settings_multi_rl_temp_upper:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_TEMP_UPPER);
                    break;
                case R.id.settings_multi_rl_temp_lower:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_TEMP_LOWER);
                    break;
                case R.id.settings_multi_rl_humidity_upper:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_HUMIDITY_UPPER);
                    break;
                case R.id.settings_multi_rl_humidity_lower:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_HUMIDITY_LOWER);
                    break;
                case R.id.settings_multi_rl_app_param_upload:
                    dialogFragment = SettingsInputDialogFragment.newInstance("");
                    dialogFragment.show(getFragmentManager(), SETTINGS_APP_PARAM_UPLOAD);
                    break;
                case R.id.settings_multi_rl_app_param_confirm:
                    String statusArray[] = this.getResources().getStringArray(R.array.multi_status_array);
                    dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(statusArray, 0);
                    dialogFragment.show(getFragmentManager(), SETTINGS_APP_PARAM_CONFIRM);
                    break;
                case R.id.settings_device_tv_smoke_start:
                    progressDialog.show();
                    smokeActionIndex = SMOKE_ACTION_START;
                    doSmokeStart();
                    break;
                case R.id.settings_device_tv_smoke_stop:
                    progressDialog.show();
                    smokeActionIndex = SMOKE_ACTION_STOP;
                    doSmokeStop();
                    break;
                case R.id.settings_device_tv_smoke_silence:
                    progressDialog.show();
                    smokeActionIndex = SMOKE_ACTION_SILENCE;
                    doSmokeSilence();
                    break;
                case R.id.settings_multi_tv_save:
                    if (targetDeviceIndex == 0) {
                        progressDialog.setMessage(getString(R.string.device) + targetDevice.getSn() + getString(R
                                .string.saving));
                        progressDialog.show();
                        saveConfiguration();
                    } else {
                        save();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPositiveButtonClick(String tag, Bundle bundle) {
        try {
            String unsetString = getString(R.string.setting_unset);
            if (tag.equals(SETTINGS_IBEACON)) {
                int writeIbeaconIndex = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                if (writeIbeaconIndex == 1) {
                    isIBeaconEnabled = true;
                    iBeaconStatusTextView.setText(R.string.open);
                } else if (writeIbeaconIndex == 2) {
                    isIBeaconEnabled = false;
                    iBeaconStatusTextView.setText(R.string.close);
                } else {
                    isIBeaconEnabled = targetDevice.isIBeaconEnabled();
                    iBeaconStatusTextView.setText(R.string.setting_unset);
                }
            } else if (tag.equals(SETTINGS_UUID)) {

                String uuidString = bundle.getString(SettingsUUIDDialogFragment.UUID);
                if (uuidString != null && !(uuidString.trim().equals("") && uuidString.equals(unsetString))) {
                    uuid = uuidString;
                    uuidTextView.setText(uuid);
                } else {
                    uuid = targetDevice.getProximityUUID();
                }

            } else if (tag.equals(SETTINGS_MAJOR)) {
                int major = bundle.getInt(SettingsMajorMinorDialogFragment.VALUE);
                this.major = major;
                majorTextView.setText(String.format("0x%04X", major));
            } else if (tag.equals(SETTINGS_MINOR)) {
                int minor = bundle.getInt(SettingsMajorMinorDialogFragment.VALUE);
                this.minor = minor;
                minorTextView.setText(String.format("0x%04X", minor));
            } else if (tag.equals(SETTINGS_BLE_POWER)) {
                int writeBlePowerIndex = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                if (writeBlePowerIndex == SETTING_STATUS_UNSET) {
                    powerTextView.setText(R.string.setting_unset);
                    bleTxp = targetDevice.getBleTxp();
                } else {
                    int index = writeBlePowerIndex - 1;
                    bleTxp = ParamUtil.getBleTxp(deviceType, index);
                    powerTextView.setText("" + bleTxp);
                }
            } else if (tag.equals(SETTINGS_ADV_INTERVAL)) {
                String bleInt = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (bleInt != null && (bleInt.trim().equals("") && bleInt.equals(unsetString))) {
                    this.bleInt = Double.valueOf(bleInt).intValue();
                    advIntervalTextView.setText(bleInt + " ms");
                } else {
                    this.bleInt = targetDevice.getBleInt();
                }

            } else if (tag.equals(SETTINGS_BLE_TURNON_TIME)) {
                int writeTurnOnTimeIndex = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                if (writeTurnOnTimeIndex == SETTING_STATUS_UNSET) {
                    turnOnTexView.setText(R.string.setting_unset);
                    bleTurnOnTime = targetDevice.getBleOnTime();
                } else {
                    int index = writeTurnOnTimeIndex - 1;
                    bleTurnOnTime = index;
                    int saveTurnOnTime = 0;
                    Calendar cal = Calendar.getInstance(Locale.getDefault());
                    int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
                    int offset = zoneOffset / 60 / 60 / 1000;
                    if ((bleTurnOnTime - offset) < 0) {
                        saveTurnOnTime = 24 + (bleTurnOnTime - offset);
                    } else if ((bleTurnOnTime - offset) == 0) {
                        saveTurnOnTime = 0;
                    } else {
                        saveTurnOnTime = (bleTurnOnTime - offset);
                    }
                    bleTurnOnTime = saveTurnOnTime;
                    turnOnTexView.setText(bleTimeItems[writeTurnOnTimeIndex]);
                }
            } else if (tag.equals(SETTINGS_BLE_TURNOFF_TIME)) {
                int writeTurnOffTimeIndex = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                if (writeTurnOffTimeIndex == SETTING_STATUS_UNSET) {
                    turnOffTextView.setText(R.string.setting_unset);
                    bleTurnOffTime = targetDevice.getBleOffTime();
                } else {
                    int index = writeTurnOffTimeIndex - 1;
                    bleTurnOffTime = index;
                    int saveTurnOffTime = 0;
                    Calendar cal = Calendar.getInstance(Locale.getDefault());
                    int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
                    int offset = zoneOffset / 60 / 60 / 1000;

                    if ((bleTurnOffTime - offset) < 0) {
                        saveTurnOffTime = 24 + (bleTurnOffTime - offset);
                    } else if ((bleTurnOffTime - offset) == 0) {
                        saveTurnOffTime = 0;
                    } else {
                        saveTurnOffTime = (bleTurnOffTime - offset);
                    }
                    bleTurnOffTime = saveTurnOffTime;
                    turnOffTextView.setText(bleTimeItems[writeTurnOffTimeIndex]);
                }
            } else if (tag.equals(SETTINGS_LORA_TXP)) {
                int writeLoraPowerIndex = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                if (writeLoraPowerIndex == SETTING_STATUS_UNSET) {
                    loraTxpTextView.setText(R.string.setting_unset);
                    loraTxp = targetDevice.getLoraTxp();
                } else {
                    int index = writeLoraPowerIndex;
                    loraTxp = ParamUtil.getLoraTxp(band, index - 1);
                    loraTxpTextView.setText("" + loraTxp);
                }

            } else if (tag.equals(SETTINGS_LORA_EIRP)) {
                int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                loraTxp = index;
                loraEirpTextView.setText(loraEirpValues[loraTxp]);
            } else if (tag.equals(SETTINGS_LORA_INT)) {
                String loraIntString = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (loraIntString != null && (loraIntString.trim().equals("") && loraIntString.equals(unsetString))) {
                    loraAdIntervalTextView.setText(loraIntString);
                    loraInt = Float.valueOf(loraIntString);
                } else {
                    loraInt = targetDevice.getLoraInt();
                }
            } else if (tag.equals(SETTINGS_EDDYSTONE1)) {
                int writeSlot1Index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                int index = writeSlot1Index - 1;
                setSettingsEddystone1(index);
            } else if (tag.equals(SETTINGS_EDDYSTONE2)) {
                int writeSlot2Index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                int index = writeSlot2Index - 1;
                setSettingsEddystone2(index);
            } else if (tag.equals(SETTINGS_EDDYSTONE3)) {
                int writeSlot3Index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                int index = writeSlot3Index - 1;
                setSettingsEddystone3(index);
            } else if (tag.equals(SETTINGS_EDDYSTONE4)) {
                int writeSlot4Index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                int index = writeSlot4Index - 1;
                setSettingsEddystone4(index);
            } else if (tag.equals(SETTINGS_EDDYSTONE1_UID)) {
                String text = bundle.getString(SettingsInputDialogFragment.INPUT);
                String regString = "[a-f0-9A-F]{32}";
                if (!Pattern.matches(regString, text)) {
                    SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance
                            (eddyStoneSlot1Item2Value.getText().toString());
                    dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE1_UID);
                    Toast.makeText(this, R.string.invaild_uid, Toast.LENGTH_SHORT).show();
                } else {
                    eddyStoneSlot1Item2Value.setText(text);
                    slot1_frame = eddyStoneSlot1Item2Value.getText().toString();
                }
            } else if (tag.equals(SETTINGS_EDDYSTONE1_URL)) {
                String text = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (!text.equals("")) {
                    Pattern pattern2 = Pattern
                            .compile("(http|https):\\/\\/\\S*");
                    Matcher matcher2 = pattern2.matcher(text);
                    if (!matcher2.matches()) {
                        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance
                                (eddyStoneSlot1Item2Value.getText().toString());
                        dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE1_URL);
                        Toast.makeText(this, R.string.invaild_url, Toast.LENGTH_SHORT).show();
                    } else {
                        eddyStoneSlot1Item2Value.setText(text);
                        slot1_frame = eddyStoneSlot1Item2Value.getText().toString();
                    }
                }

            } else if (tag.equals(SETTINGS_EDDYSTONE2_UID)) {
                String text = bundle.getString(SettingsInputDialogFragment.INPUT);
                String regString = "[a-f0-9A-F]{32}";
                if (!Pattern.matches(regString, text)) {
                    SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance
                            (eddyStoneSlot2Item2Value.getText().toString());
                    dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE2_UID);
                    Toast.makeText(this, R.string.invaild_uid, Toast.LENGTH_SHORT).show();
                } else {
                    eddyStoneSlot2Item2Value.setText(text);
                    slot2_frame = eddyStoneSlot2Item2Value.getText().toString();
                }
            } else if (tag.equals(SETTINGS_EDDYSTONE2_URL)) {
                String text = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (!text.equals("")) {
                    Pattern pattern2 = Pattern
                            .compile("(http|https):\\/\\/\\S*");
                    Matcher matcher2 = pattern2.matcher(text);
                    if (!matcher2.matches()) {
                        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance
                                (eddyStoneSlot2Item2Value.getText().toString());
                        dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE2_URL);
                        Toast.makeText(this, R.string.invaild_url, Toast.LENGTH_SHORT).show();
                    } else {
                        eddyStoneSlot2Item2Value.setText(text);
                        slot2_frame = eddyStoneSlot2Item2Value.getText().toString();
                    }
                }

            } else if (tag.equals(SETTINGS_EDDYSTONE3_UID)) {
                String text = bundle.getString(SettingsInputDialogFragment.INPUT);
                String regString = "[a-f0-9A-F]{32}";
                if (!Pattern.matches(regString, text)) {
                    SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance
                            (eddyStoneSlot3Item2Value.getText().toString());
                    dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE3_UID);
                    Toast.makeText(this, R.string.invaild_uid, Toast.LENGTH_SHORT).show();
                } else {
                    eddyStoneSlot3Item2Value.setText(text);
                    slot3_frame = eddyStoneSlot3Item2Value.getText().toString();
                }
            } else if (tag.equals(SETTINGS_EDDYSTONE3_URL)) {
                String text = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (!text.equals("")) {
                    Pattern pattern2 = Pattern
                            .compile("(http|https):\\/\\/\\S*");
                    Matcher matcher2 = pattern2.matcher(text);
                    if (!matcher2.matches()) {
                        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance
                                (eddyStoneSlot3Item2Value.getText().toString());
                        dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE3_URL);
                        Toast.makeText(this, R.string.invaild_url, Toast.LENGTH_SHORT).show();
                    } else {
                        eddyStoneSlot3Item2Value.setText(text);
                        slot3_frame = eddyStoneSlot3Item2Value.getText().toString();
                    }
                }

            } else if (tag.equals(SETTINGS_EDDYSTONE4_UID)) {
                String text = bundle.getString(SettingsInputDialogFragment.INPUT);
                String regString = "[a-f0-9A-F]{32}";
                if (!Pattern.matches(regString, text)) {
                    SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance
                            (eddyStoneSlot4Item2Value.getText().toString());
                    dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE4_UID);
                    Toast.makeText(this, R.string.invaild_uid, Toast.LENGTH_SHORT).show();
                } else {
                    eddyStoneSlot4Item2Value.setText(text);
                    slot4_frame = eddyStoneSlot4Item2Value.getText().toString();
                }
            } else if (tag.equals(SETTINGS_EDDYSTONE4_URL)) {
                String text = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (!text.equals("")) {
                    Pattern pattern2 = Pattern
                            .compile("(http|https):\\/\\/\\S*");
                    Matcher matcher2 = pattern2.matcher(text);
                    if (!matcher2.matches()) {
                        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance
                                (eddyStoneSlot4Item2Value.getText().toString());
                        dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE4_URL);
                        Toast.makeText(this, R.string.invaild_url, Toast.LENGTH_SHORT).show();
                    } else {
                        eddyStoneSlot4Item2Value.setText(text);
                        slot4_frame = eddyStoneSlot4Item2Value.getText().toString();
                    }
                }

            } else if (tag.equals(SETTINGS_EDDYSTONE1_EID)) {
                int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                String item = bundle.getString(SettingsSingleChoiceItemsFragment.ITEM);
                eddyStoneSlot1Item2Value.setText(item);
            } else if (tag.equals(SETTINGS_EDDYSTONE2_EID)) {
                int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                String item = bundle.getString(SettingsSingleChoiceItemsFragment.ITEM);
                eddyStoneSlot2Item2Value.setText(item);
            } else if (tag.equals(SETTINGS_EDDYSTONE3_EID)) {
                int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                String item = bundle.getString(SettingsSingleChoiceItemsFragment.ITEM);
                eddyStoneSlot3Item2Value.setText(item);
            } else if (tag.equals(SETTINGS_EDDYSTONE4_EID)) {
                int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                String item = bundle.getString(SettingsSingleChoiceItemsFragment.ITEM);
                eddyStoneSlot4Item2Value.setText(item);
            } else if (tag.equals(SETTINGS_SENSOR)) {
                int writeSensorIndex = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                if (writeSensorIndex == 1) {
                    isSensorBroadcastEnabled = true;
                } else {
                    isSensorBroadcastEnabled = false;
                }
                sensorStatusTextView.setText(getResources().getStringArray(R.array.multi_status_array)
                        [writeSensorIndex]);
            } else if (tag.equals(SETTINGS_CUSTOM_PACKAGE1)) {
                String ctp1 = bundle.getString(SettingsInputDialogFragment.INPUT);
                String regString = "[a-f0-9A-F]{1,56}";
                if (!Pattern.matches(regString, ctp1)) {
                    SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance
                            (custom_package1);
                    dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE1);
                    Toast.makeText(this, R.string.invaild_custom, Toast.LENGTH_SHORT).show();
                } else {
                    custom_package1 = ctp1;
                    customPackage1ValueTextView.setText(custom_package1);
                }
            } else if (tag.equals(SETTINGS_CUSTOM_PACKAGE2)) {
                String ctp2 = bundle.getString(SettingsInputDialogFragment.INPUT);
                String regString = "[a-f0-9A-F]{1,56}";
                if (!Pattern.matches(regString, ctp2)) {
                    SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance
                            (custom_package2);
                    dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE2);
                    Toast.makeText(this, R.string.invaild_custom, Toast.LENGTH_SHORT).show();
                } else {
                    custom_package2 = ctp2;
                    customPackage2ValueTextView.setText(custom_package2);
                }
            } else if (tag.equals(SETTINGS_CUSTOM_PACKAGE3)) {
                String ctp3 = bundle.getString(SettingsInputDialogFragment.INPUT);
                String regString = "[a-f0-9A-F]{1,56}";
                if (!Pattern.matches(regString, ctp3)) {
                    SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance
                            (custom_package3);
                    dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE3);
                    Toast.makeText(this, R.string.invaild_custom, Toast.LENGTH_SHORT).show();
                } else {
                    custom_package3 = ctp3;
                    customPackage3ValueTextView.setText(custom_package3);
                }
            } else if (tag.equals(SETTINGS_CUSTOM_PACKAGE1_STATUS)) {
                int writeCustom1Index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                customPackage1TextView.setText(getResources().getStringArray(R.array.multi_status_array)
                        [writeCustom1Index]);
                if (writeCustom1Index == 1) {
                    isCustomPackage1Enabled = true;
                    customPackage1ValueRelativeLayout.setVisibility(VISIBLE);
                    customPackage1ValueRelativeLayout.setAlpha(1.0f);
                    customPackage1ValueRelativeLayout.setEnabled(true);
                } else if (writeCustom1Index == 2) {
                    isCustomPackage1Enabled = false;
                    customPackage1ValueRelativeLayout.setVisibility(VISIBLE);
                    customPackage1ValueRelativeLayout.setAlpha(0.5f);
                    customPackage1ValueRelativeLayout.setEnabled(false);
                } else {
                    isCustomPackage1Enabled = targetDevice.getSlotArray()[5].isActived() == 1 ? true : false;
                    customPackage1ValueRelativeLayout.setVisibility(View.GONE);
                    customPackage1TextView.setText(R.string.setting_unset);
                }
            } else if (tag.equals(SETTINGS_CUSTOM_PACKAGE2_STATUS)) {
                int writeCustom2Index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                customPackage2TextView.setText(getResources().getStringArray(R.array.multi_status_array)
                        [writeCustom2Index]);
                if (writeCustom2Index == 1) {
                    isCustomPackage2Enabled = true;
                    customPackage2ValueRelativeLayout.setVisibility(VISIBLE);
                    customPackage2ValueRelativeLayout.setAlpha(1.0f);
                    customPackage2ValueRelativeLayout.setEnabled(true);
                } else if (writeCustom2Index == 2) {
                    customPackage2ValueRelativeLayout.setVisibility(VISIBLE);
                    isCustomPackage2Enabled = false;
                    customPackage2ValueRelativeLayout.setAlpha(0.5f);
                    customPackage2ValueRelativeLayout.setEnabled(false);
                } else {
                    isCustomPackage2Enabled = targetDevice.getSlotArray()[6].isActived() == 1 ? true : false;
                    customPackage2ValueRelativeLayout.setVisibility(View.GONE);
                    customPackage2TextView.setText(R.string.setting_unset);
                }
            } else if (tag.equals(SETTINGS_CUSTOM_PACKAGE3_STATUS)) {
                int writeCustom3Index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                customPackage3TextView.setText(getResources().getStringArray(R.array.multi_status_array)
                        [writeCustom3Index]);
                if (writeCustom3Index == 1) {
                    isCustomPackage3Enabled = true;
                    customPackage3ValueRelativeLayout.setVisibility(VISIBLE);
                    customPackage3ValueRelativeLayout.setAlpha(1.0f);
                    customPackage3ValueRelativeLayout.setEnabled(true);
                } else if (writeCustom3Index == 2) {
                    isCustomPackage3Enabled = false;
                    customPackage3ValueRelativeLayout.setVisibility(VISIBLE);
                    customPackage3ValueRelativeLayout.setAlpha(0.5f);
                    customPackage3ValueRelativeLayout.setEnabled(false);
                } else {
                    isCustomPackage2Enabled = targetDevice.getSlotArray()[7].isActived() == 1 ? true : false;
                    customPackage3ValueRelativeLayout.setVisibility(View.GONE);
                    customPackage3TextView.setText(R.string.setting_unset);
                }
            } else if (tag.equals(SETTINGS_SENSOR_CO)) {
                String co = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (co != null && (co.trim().equals("") && co.equals(unsetString))) {
                    this.coAlarmHigh = Float.valueOf(co).intValue();
                    coTextView.setText(co + "");
                } else {
                    this.coAlarmHigh = targetDevice.getSensoroSensorTest().co.alarmHigh_float;
                }

            } else if (tag.equals(SETTINGS_SENSOR_CO2)) {
                String co2 = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (co2 != null && (co2.trim().equals("") && co2.equals(unsetString))) {
                    this.co2AlarmHigh = Float.valueOf(co2).intValue();
                    co2TextView.setText(co2 + "");
                } else {
                    this.co2AlarmHigh = targetDevice.getSensoroSensorTest().co2.alarmHigh_float;
                }

            } else if (tag.equals(SETTINGS_SENSOR_NO2)) {

                String no2 = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (no2 != null && (no2.trim().equals("") && no2.equals(unsetString))) {
                    this.no2AlarmHigh = Float.valueOf(no2).intValue();
                    no2TextView.setText(no2 + "");
                } else {
                    this.no2AlarmHigh = targetDevice.getSensoroSensorTest().no2.alarmHigh_float;
                }

            } else if (tag.equals(SETTINGS_SENSOR_CH4)) {

                String ch4 = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (ch4 != null && (ch4.trim().equals("") && ch4.equals(unsetString))) {
                    this.ch4AlarmHigh = Float.valueOf(ch4).intValue();
                    ch4TextView.setText(ch4 + "");
                } else {
                    this.ch4AlarmHigh = targetDevice.getSensoroSensorTest().ch4.alarmHigh_float;
                }

            } else if (tag.equals(SETTINGS_SENSOR_LPG)) {

                String lpg = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (lpg != null && (lpg.trim().equals("") && lpg.equals(unsetString))) {
                    this.lpgAlarmHigh = Float.valueOf(lpg).intValue();
                    lpgTextView.setText(lpg + " ");
                } else {
                    this.lpgAlarmHigh = targetDevice.getSensoroSensorTest().lpg.alarmHigh_float;
                }

            } else if (tag.equals(SETTINGS_SENSOR_PM25)) {
                String pm25 = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (pm25 != null && (pm25.trim().equals("") && pm25.equals(unsetString))) {
                    this.pm25AlarmHigh = Float.valueOf(pm25).intValue();
                    pm25TextView.setText(pm25 + "");
                } else {
                    this.pm25AlarmHigh = targetDevice.getSensoroSensorTest().pm25.alarmHigh_float;
                }

            } else if (tag.equals(SETTINGS_SENSOR_PM10)) {
                String pm10 = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (pm10 != null && (pm10.trim().equals("") && pm10.equals(unsetString))) {
                    this.pm10AlarmHigh = Float.valueOf(pm10).intValue();
                    pm10TextView.setText(pm10 + "");
                } else {
                    this.pm10AlarmHigh = targetDevice.getSensoroSensorTest().pm10.alarmHigh_float;
                }

            } else if (tag.equals(SETTINGS_SENSOR_TEMP_UPPER)) {
                String tempHigh = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (tempHigh != null && (tempHigh.trim().equals("") && tempHigh.equals(unsetString))) {
                    this.tempAlarmHigh = Float.valueOf(tempHigh).intValue();
                    tempUpperTextView.setText(tempHigh + "");
                } else {
                    this.tempAlarmHigh = targetDevice.getSensoroSensorTest().temperature.alarmHigh_float;
                }

            } else if (tag.equals(SETTINGS_SENSOR_TEMP_LOWER)) {
                String tempLow = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (tempLow != null && (tempLow.trim().equals("") && tempLow.equals(unsetString))) {
                    this.tempAlarmLow = Float.valueOf(tempLow).intValue();
                    tempLowerTextView.setText(tempLow + "");
                } else {
                    this.tempAlarmLow = targetDevice.getSensoroSensorTest().temperature.alarmLow_float;
                }

            } else if (tag.equals(SETTINGS_SENSOR_HUMIDITY_UPPER)) {
                String humidityHigh = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (humidityHigh != null && (humidityHigh.trim().equals("") && humidityHigh.equals(unsetString))) {
                    this.humidityAlarmHigh = Float.valueOf(humidityHigh).intValue();
                    humidityUpperTextView.setText(humidityHigh + "");
                } else {
                    this.humidityAlarmHigh = targetDevice.getSensoroSensorTest().humidity.alarmHigh_float;
                }

            } else if (tag.equals(SETTINGS_SENSOR_HUMIDITY_LOWER)) {
                String humidityLow = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (humidityLow != null && (humidityLow.trim().equals("") && humidityLow.equals(unsetString))) {
                    this.humidityAlarmLow = Float.valueOf(humidityLow).intValue();
                    humidityLowerTextView.setText(humidityLow + "");
                } else {
                    this.humidityAlarmLow = targetDevice.getSensoroSensorTest().humidity.alarmLow_float;
                }

            } else if (tag.equals(SETTINGS_SENSOR_PITCH_ANGLE_UPPER)) {
                String pitchHigh = bundle.getString(SettingsInputDialogFragment.INPUT);
                this.pitchAngleAlarmHigh = Float.valueOf(pitchHigh).intValue();
                pitchAngleUpperTextView.setText(pitchHigh + "");
            } else if (tag.equals(SETTINGS_SENSOR_PITCH_ANGLE_LOWER)) {
                String pitchLow = bundle.getString(SettingsInputDialogFragment.INPUT);
                this.pitchAngleAlarmLow = Float.valueOf(pitchLow).intValue();
                pitchAngleLowerTextView.setText(pitchLow + "");
            } else if (tag.equals(SETTINGS_SENSOR_ROLL_ANGLE_UPPER)) {
                String rollHigh = bundle.getString(SettingsInputDialogFragment.INPUT);
                this.rollAngleAlarmHigh = Float.valueOf(rollHigh).intValue();
                rollAngleUpperTextView.setText(rollHigh + "");
            } else if (tag.equals(SETTINGS_SENSOR_ROLL_ANGLE_LOWER)) {
                String rollLower = bundle.getString(SettingsInputDialogFragment.INPUT);
                this.rollAngleAlarmLow = Float.valueOf(rollLower).intValue();
                rollAngleLowerTextView.setText(rollLower + "");
            } else if (tag.equals(SETTINGS_SENSOR_YAW_ANGLE_UPPER)) {
                String yawHigh = bundle.getString(SettingsInputDialogFragment.INPUT);
                this.yawAngleAlarmHigh = Float.valueOf(yawHigh).intValue();
                yawAngleUpperTextView.setText(yawHigh + "");
            } else if (tag.equals(SETTINGS_SENSOR_YAW_ANGLE_LOWER)) {
                String yawLower = bundle.getString(SettingsInputDialogFragment.INPUT);
                this.yawAngleAlarmLow = Float.valueOf(yawLower).intValue();
                yawAngleLowerTextView.setText(yawLower + "");
            } else if (tag.equals(SETTINGS_SENSOR_WATER_PRESSURE_UPPER)) {
                String waterHigh = bundle.getString(SettingsInputDialogFragment.INPUT);
                this.waterPressureAlarmHigh = Float.valueOf(waterHigh).intValue();
                waterPressureUpperTextView.setText(waterHigh + "");
            } else if (tag.equals(SETTINGS_SENSOR_WATER_PRESSURE_LOWER)) {
                String waterLower = bundle.getString(SettingsInputDialogFragment.INPUT);
                this.waterPressureAlarmLow = Float.valueOf(waterLower).intValue();
                waterPressureLowerTextView.setText(waterLower + "");
            } else if (tag.equals(SETTINGS_APP_PARAM_UPLOAD)) {
                String upload = bundle.getString(SettingsInputDialogFragment.INPUT);
                if (upload != null && (upload.trim().equals("") && upload.equals(unsetString))) {
                    this.uploadInterval = Integer.valueOf(upload);
                    uploadIntervalTextView.setText(uploadInterval + "");
                } else {
                    this.uploadInterval = targetDevice.getUploadInterval();
                }

            } else if (tag.equals(SETTINGS_APP_PARAM_CONFIRM)) {

                int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
                if (index == 0) {
                    this.appParamConfirm = targetDevice.getConfirm();
                } else {
                    if (index == 1) {
                        appParamConfirm = 1;
                    } else {
                        appParamConfirm = 0;
                    }
                    confirmTextView.setText(getResources().getStringArray(R.array.multi_status_array)[index]);
                }

            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.tips_format_error, Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (sensoroDeviceConnection != null) {
            sensoroDeviceConnection.disconnect();
        }
    }
}
