package com.sensoro.loratool.activity;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
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
import com.sensoro.loratool.fragment.SettingsInputDialogFragment;
import com.sensoro.loratool.fragment.SettingsMajorMinorDialogFragment;
import com.sensoro.loratool.fragment.SettingsSingleChoiceItemsFragment;
import com.sensoro.loratool.fragment.SettingsUUIDDialogFragment;
import com.sensoro.libbleserver.ble.BLEDevice;
import com.sensoro.libbleserver.ble.CmdType;
import com.sensoro.libbleserver.ble.SensoroConnectionCallback;
import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.libbleserver.ble.SensoroDeviceConfiguration;
import com.sensoro.libbleserver.ble.SensoroDeviceConnection;
import com.sensoro.libbleserver.ble.SensoroSensorTest;
import com.sensoro.libbleserver.ble.SensoroSlot;
import com.sensoro.libbleserver.ble.SensoroUtils;
import com.sensoro.libbleserver.ble.SensoroWriteCallback;
import com.sensoro.libbleserver.ble.scanner.SensoroUUID;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.event.OnPositiveButtonClickListener;
import com.sensoro.libbleserver.ble.proto.MsgNode1V1M5;
import com.sensoro.libbleserver.ble.proto.ProtoMsgCfgV1U1;
import com.sensoro.libbleserver.ble.proto.ProtoStd1U1;
import com.sensoro.loratool.store.DeviceDataDao;
import com.sensoro.loratool.utils.ParamUtil;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.sensoro.libbleserver.ble.CmdType.CMD_ELEC_AIR_SWITCH;
import static com.sensoro.libbleserver.ble.CmdType.CMD_ELEC_RESET;
import static com.sensoro.libbleserver.ble.CmdType.CMD_ELEC_RESTORE;
import static com.sensoro.libbleserver.ble.CmdType.CMD_ELEC_SELF_TEST;
import static com.sensoro.libbleserver.ble.CmdType.CMD_ELEC_SILENCE;
import static com.sensoro.libbleserver.ble.CmdType.CMD_ELEC_ZERO_POWER;

public class SettingDeviceActivity extends BaseActivity implements Constants, CompoundButton.OnCheckedChangeListener,
        View.OnClickListener, OnPositiveButtonClickListener, SensoroWriteCallback, SensoroConnectionCallback {
    private static final String TAG = SettingDeviceActivity.class.getSimpleName();

    @BindView(R.id.settings_device_back)
    ImageView backImageView;
    @BindView(R.id.settings_device_tv_save)
    TextView saveTextView;
    /**
     * iBeacon
     **/
    @BindView(R.id.settings_device_ll_ibeacon)
    LinearLayout ibeaconLayout;
    @BindView(R.id.settings_device_sc_ibeacon)
    SwitchCompat iBeaconSwitchCompat;
    @BindView(R.id.settings_device_rl_uuid)
    RelativeLayout uuidRelativeLayout;
    @BindView(R.id.settings_device_tv_uuid)
    TextView uuidTextView;
    @BindView(R.id.settings_device_rl_major)
    RelativeLayout majorRelativeLayout;
    @BindView(R.id.settings_device_tv_major)
    TextView majorTextView;
    @BindView(R.id.settings_device_rl_minor)
    RelativeLayout minorRelativeLayout;
    @BindView(R.id.settings_device_tv_minor)
    TextView minorTextView;

    /**
     * ble function
     **/
    @BindView(R.id.settings_device_ll_ble)
    LinearLayout bleLayout;
    @BindView(R.id.settings_device_rl_power)
    RelativeLayout powerRelativeLayout;
    @BindView(R.id.settings_device_tv_power)
    TextView powerTextView;
    // advertise interval
    @BindView(R.id.settings_device_rl_adv_interval)
    RelativeLayout advIntervalRelativeLayout;
    @BindView(R.id.settings_device_tv_adv_interval)
    TextView advIntervalTextView;
    //ble turn on time
    @BindView(R.id.settings_device_ll_turnon_time)
    LinearLayout bleTurnOnTimeLinearLayout;
    @BindView(R.id.settings_device_tv_turnon_time)
    TextView turnOnTexView;
    //ble turn off time
    @BindView(R.id.settings_device_ll_turnoff_time)
    LinearLayout bleTurnOffTimeLinearLayout;
    @BindView(R.id.settings_device_tv_turnoff_time)
    TextView turnOffTextView;

    @BindView(R.id.settings_device_ll_lora)
    LinearLayout loraLayout;
    //transimit power
    @BindView(R.id.settings_device_rl_lora_txp)
    RelativeLayout loraTxpRelativeLayout;
    @BindView(R.id.settings_device_tv_lora_txp)
    TextView loraTxpTextView;
    //advertisingInterval
    @BindView(R.id.settings_device_rl_lora_ad_interval)
    RelativeLayout loraAdIntervalRelativeLayout;
    @BindView(R.id.settings_device_tv_lora_ad_interval)
    TextView loraAdIntervalTextView;
    @BindView(R.id.settings_device_rl_lora_eirp)
    RelativeLayout loraEirpRelativeLayout;
    @BindView(R.id.settings_device_tv_lora_eirp)
    TextView loraEirpTextView;

    /**
     * Eddystone Function
     **/
    @BindView(R.id.settings_device_ll_eddystone)
    LinearLayout eddystoneLayout;
    //slot 1
    @BindView(R.id.settings_device_rl_slot1_item1)
    RelativeLayout eddyStoneSlot1Item1Layout;
    @BindView(R.id.slot1_item1_tv)
    TextView eddyStoneSlot1Item1;
    @BindView(R.id.slot1_item1_tv_v)
    TextView eddyStoneSlot1Item1Value;
    @BindView(R.id.settings_device_rl_slot1_item2)
    RelativeLayout eddyStoneSlot1Item2Layout;
    @BindView(R.id.slot1_item2_tv)
    TextView eddyStoneSlot1Item2;
    @BindView(R.id.slot1_sep_iv2)
    ImageView eddyStoneSlot1Iv2;
    @BindView(R.id.slot1_item2_tv_v)
    TextView eddyStoneSlot1Item2Value;

    //slot 2
    @BindView(R.id.settings_device_rl_slot2_item1)
    RelativeLayout eddyStoneSlot2Item1Layout;
    @BindView(R.id.slot2_item1_tv)
    TextView eddyStoneSlot2Item1;
    @BindView(R.id.slot2_item1_tv_v)
    TextView eddyStoneSlot2Item1Value;
    @BindView(R.id.settings_device_rl_slot2_item2)
    RelativeLayout eddyStoneSlot2Item2Layout;
    @BindView(R.id.slot2_item2_tv)
    TextView eddyStoneSlot2Item2;
    @BindView(R.id.slot2_sep_iv2)
    ImageView eddyStoneSlot2Iv2;
    @BindView(R.id.slot2_item2_tv_v)
    TextView eddyStoneSlot2Item2Value;

    //slot 3
    @BindView(R.id.settings_device_rl_slot3_item1)
    RelativeLayout eddyStoneSlot3Item1Layout;
    @BindView(R.id.slot3_item1_tv)
    TextView eddyStoneSlot3Item1;
    @BindView(R.id.slot3_item1_tv_v)
    TextView eddyStoneSlot3Item1Value;
    @BindView(R.id.settings_device_rl_slot3_item2)
    RelativeLayout eddyStoneSlot3Item2Layout;
    @BindView(R.id.slot3_item2_tv)
    TextView eddyStoneSlot3Item2;
    @BindView(R.id.slot3_sep_iv2)
    ImageView eddyStoneSlot3Iv2;
    @BindView(R.id.slot3_item2_tv_v)
    TextView eddyStoneSlot3Item2Value;

    //slot 3
    @BindView(R.id.settings_device_rl_slot4_item1)
    RelativeLayout eddyStoneSlot4Item1Layout;
    @BindView(R.id.slot4_item1_tv)
    TextView eddyStoneSlot4Item1;
    @BindView(R.id.slot4_item1_tv_v)
    TextView eddyStoneSlot4Item1Value;
    @BindView(R.id.settings_device_rl_slot4_item2)
    RelativeLayout eddyStoneSlot4Item2Layout;
    @BindView(R.id.slot4_item2_tv)
    TextView eddyStoneSlot4Item2;
    @BindView(R.id.slot4_sep_iv2)
    ImageView eddyStoneSlot4Iv2;
    @BindView(R.id.slot4_item2_tv_v)
    TextView eddyStoneSlot4Item2Value;
    @BindView(R.id.settings_device_ll_sensor_enable)
    LinearLayout sensorBroadcastEnableLayout;
    @BindView(R.id.settings_device_sensor_adv_status)
    SwitchCompat sensorBroadcastSwitchCompat;
    @BindView(R.id.settings_device_ll_app_param)
    LinearLayout appParamLayout;
    @BindView(R.id.settings_device_rl_app_param_upload)
    RelativeLayout uploadIntervalLayout;
    @BindView(R.id.settings_device_tv_upload_upper_limit)
    TextView uploadIntervalTextView;
    @BindView(R.id.settings_device_rl_app_param_confirm)
    RelativeLayout confirmLayout;
    @BindView(R.id.settings_device_tv_confirm)
    TextView confirmTextView;
    @BindView(R.id.settings_device_ll_sensor_param)
    LinearLayout sensorParamLayout;
    @BindView(R.id.settings_device_ll_co)
    LinearLayout coLinearLayout;
    @BindView(R.id.settings_device_tv_co_upper_limit)
    TextView coTextView;
    @BindView(R.id.settings_device_ll_co2)
    LinearLayout co2LinearLayout;
    @BindView(R.id.settings_device_tv_co2_upper_limit)
    TextView co2TextView;
    @BindView(R.id.settings_device_ll_no2)
    LinearLayout no2LinearLayout;
    @BindView(R.id.settings_device_tv_no2_upper_limit)
    TextView no2TextView;
    @BindView(R.id.settings_device_ll_ch4)
    LinearLayout ch4LinearLayout;
    @BindView(R.id.settings_device_tv_ch4_upper_limit)
    TextView ch4TextView;
    @BindView(R.id.settings_device_ll_lpg)
    LinearLayout lpgLinearLayout;
    @BindView(R.id.settings_device_tv_lpg_upper_limit)
    TextView lpgTextView;
    @BindView(R.id.settings_device_ll_pm25)
    LinearLayout pm25LinearLayout;
    @BindView(R.id.settings_device_tv_pm25_upper_limit)
    TextView pm25TextView;
    @BindView(R.id.settings_device_ll_pm10)
    LinearLayout pm10LinearLayout;
    @BindView(R.id.settings_device_tv_pm10_upper_limit)
    TextView pm10TextView;
    @BindView(R.id.settings_device_ll_temp)
    LinearLayout tempLinearLayout;
    @BindView(R.id.settings_device_tv_temp_upper_limit)
    TextView tempUpperTextView;
    @BindView(R.id.settings_device_tv_temp_lower_limit)
    TextView tempLowerTextView;
    @BindView(R.id.settings_device_rl_temp_upper)
    RelativeLayout tempUpperRelativeLayout;
    @BindView(R.id.settings_device_rl_temp_lower)
    RelativeLayout tempLowerRelativeLayout;
    @BindView(R.id.settings_device_ll_humidity)
    LinearLayout humidityLinearLayout;
    @BindView(R.id.settings_device_tv_humidity_upper_limit)
    TextView humidityUpperTextView;
    @BindView(R.id.settings_device_tv_humidity_lower_limit)
    TextView humidityLowerTextView;
    @BindView(R.id.settings_device_rl_humidity_upper)
    RelativeLayout humidityUpperRelativeLayout;
    @BindView(R.id.settings_device_rl_humidity_lower)
    RelativeLayout humidityLowerRelativeLayout;
    @BindView(R.id.settings_device_ll_pitch_angle)
    LinearLayout pitchAngleLinearLayout;
    @BindView(R.id.settings_device_tv_pitch_angle_upper_limit)
    TextView pitchAngleUpperTextView;
    @BindView(R.id.settings_device_tv_pitch_angle_lower_limit)
    TextView pitchAngleLowerTextView;
    @BindView(R.id.settings_device_rl_pitch_angle_upper)
    RelativeLayout pitchAngleUpperRelativeLayout;
    @BindView(R.id.settings_device_rl_pitch_angle_lower)
    RelativeLayout pitchAngleLowerRelativeLayout;
    @BindView(R.id.settings_device_ll_roll_angle)
    LinearLayout rollAngleLinearLayout;
    @BindView(R.id.settings_device_tv_roll_angle_upper_limit)
    TextView rollAngleUpperTextView;
    @BindView(R.id.settings_device_tv_roll_angle_lower_limit)
    TextView rollAngleLowerTextView;
    @BindView(R.id.settings_device_rl_roll_angle_upper)
    RelativeLayout rollAngleUpperRelativeLayout;
    @BindView(R.id.settings_device_rl_roll_angle_lower)
    RelativeLayout rollAngleLowerRelativeLayout;
    @BindView(R.id.settings_device_ll_yaw_angle)
    LinearLayout yawAngleLinearLayout;
    @BindView(R.id.settings_device_tv_yaw_angle_upper_limit)
    TextView yawAngleUpperTextView;
    @BindView(R.id.settings_device_tv_yaw_angle_lower_limit)
    TextView yawAngleLowerTextView;
    @BindView(R.id.settings_device_rl_yaw_angle_upper)
    RelativeLayout yawAngleUpperRelativeLayout;
    @BindView(R.id.settings_device_rl_yaw_angle_lower)
    RelativeLayout yawAngleLowerRelativeLayout;

    @BindView(R.id.settings_device_ll_water_pressure)
    LinearLayout waterPressureLinearLayout;
    @BindView(R.id.settings_device_tv_water_pressure_upper_limit)
    TextView waterPressureUpperTextView;
    @BindView(R.id.settings_device_tv_water_pressure_lower_limit)
    TextView waterPressureLowerTextView;
    @BindView(R.id.settings_device_rl_water_pressure_upper)
    RelativeLayout waterPressureUpperRelativeLayout;
    @BindView(R.id.settings_device_rl_water_pressure_lower)
    RelativeLayout waterPressureLowerRelativeLayout;

    @BindView(R.id.settings_device_ll_angle_zero)
    LinearLayout zeroCommandLinearLayout;
    @BindView(R.id.settings_device_tv_angle_zero)
    TextView zeroCommandTextView;
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
    @BindView(R.id.settings_device_ll_custom_package)
    LinearLayout customLayout;
    @BindView(R.id.settings_device_rl_custom_package1)
    RelativeLayout customPackage1RelativeLayout;
    @BindView(R.id.settings_device_tv_package1)
    TextView customPackage1TextView;
    @BindView(R.id.settings_device_rl_custom_package2)
    RelativeLayout customPackage2RelativeLayout;
    @BindView(R.id.settings_device_tv_package2)
    TextView customPackage2TextView;
    @BindView(R.id.settings_device_rl_custom_package3)
    RelativeLayout customPackage3RelativeLayout;
    @BindView(R.id.settings_device_tv_package3)
    TextView customPackage3TextView;
    @BindView(R.id.settings_device_custom_package1_state)
    SwitchCompat customPackage1SwitchCompat;
    @BindView(R.id.settings_device_custom_package2_state)
    SwitchCompat customPackage2SwitchCompat;
    @BindView(R.id.settings_device_custom_package3_state)
    SwitchCompat customPackage3SwitchCompat;

    /**
     * 温度传感器
     */
    @BindView(R.id.settings_device_ll_temperature_pressure)
    LinearLayout settingsDeviceLlTemperaturePressure;

    @BindView(R.id.settings_device_rl_temperature_pressure_upper)
    RelativeLayout settingsDeviceRlTemperaturePressureUpper;

    @BindView(R.id.settings_device_tv_temperature_pressure_upper_limit)
    TextView settingsDeviceTvTemperaturePressureUpperLimit;

    @BindView(R.id.settings_device_rl_temperature_pressure_lower)
    RelativeLayout settingsDeviceRlTemperaturePressureLower;

    @BindView(R.id.settings_device_tv_temperature_pressure_lower_limit)
    TextView settingsDeviceTvTemperaturePressureLowerLimit;

    @BindView(R.id.settings_device_rl_temperature_pressure_step_upper)
    RelativeLayout settingsDeviceRlTemperaturePressureStepUpper;

    @BindView(R.id.settings_device_tv_temperature_pressure_upper_step_limit)
    TextView settingsDeviceTvTemperaturePressureUpperStepLimit;

    @BindView(R.id.settings_device_rl_temperature_pressure_step_lower)
    RelativeLayout settingsDeviceRlTemperaturePressureStepLower;

    @BindView(R.id.settings_device_tv_temperature_pressure_lower_step_limit)
    TextView settingsDeviceTvTemperaturePressureLowerStepLimit;

    /**
     * 泛海三江电表
     */
    @BindView(R.id.settings_device_ll_fhsj_elec)
    LinearLayout settingsDeviceLlFhsjElec;
    //密码
    @BindView(R.id.settings_device_rl_fhsj_elec_pwd)
    RelativeLayout settingsDeviceRlFhsjElecPwd;
    @BindView(R.id.settings_device_tv_fhsj_elec_pwd)
    TextView settingsDeviceTvFhsjElecPwd;
    //漏电阈值
    @BindView(R.id.settings_device_rl_fhsj_elec_leak)
    RelativeLayout settingsDeviceRlFhsjElecLeak;
    @BindView(R.id.settings_device_tv_fhsj_elec_leak)
    TextView settingsDeviceTvFhsjElecLeak;
    //温度阈值
    @BindView(R.id.settings_device_rl_fhsj_elec_temp)
    RelativeLayout settingsDeviceRlFhsjElecTemp;
    @BindView(R.id.settings_device_tv_fhsj_elec_temp)
    TextView settingsDeviceTvFhsjElecTemp;
    //电流阈值
    @BindView(R.id.settings_device_rl_fhsj_elec_current)
    RelativeLayout settingsDeviceRlFhsjElecCurrent;
    @BindView(R.id.settings_device_tv_fhsj_elec_current)
    TextView settingsDeviceTvFhsjElecCurrent;
    //过载阈值
    @BindView(R.id.settings_device_rl_fhsj_elec_overload)
    RelativeLayout settingsDeviceRlFhsjElecOverload;
    @BindView(R.id.settings_device_tv_fhsj_elec_overload)
    TextView settingsDeviceTvFhsjElecOverload;
    //过压阈值
    @BindView(R.id.settings_device_rl_fhsj_elec_overpressure)
    RelativeLayout settingsDeviceRlFhsjElecOverpressure;
    @BindView(R.id.settings_device_tv_fhsj_elec_overpressure)
    TextView settingsDeviceTvFhsjElecOverpressure;
    //欠压阈值
    @BindView(R.id.settings_device_rl_fhsj_elec_undervoltage)
    RelativeLayout settingsDeviceRlFhsjElecUndervoltage;
    @BindView(R.id.settings_device_tv_fhsj_elec_undervoltage)
    TextView settingsDeviceTvFhsjElecUndervoltage;

    /**
     * 电表控制
     */
    @BindView(R.id.settings_device_ll_fhsj_elec_control)
    LinearLayout settingsDeviceLlFhsjElecControl;
    //系统复位
    @BindView(R.id.settings_device_rl_fhsj_elec_control_reset)
    RelativeLayout settingsDeviceRlFhsjElecControlReset;
    //回复出厂
    @BindView(R.id.settings_device_rl_fhsj_elec_control_restore)
    RelativeLayout settingsDeviceRlFhsjElecControlRestore;
    //断开空气开关
    @BindView(R.id.settings_device_rl_fhsj_elec_control_air_switch)
    RelativeLayout settingsDeviceRlFhsjElecControlAirSwitch;
    //自检
    @BindView(R.id.settings_device_rl_fhsj_elec_control_self_test)
    RelativeLayout settingsDeviceRlFhsjElecControlSelfTest;
    //消音
    @BindView(R.id.settings_device_rl_fhsj_elec_control_silence)
    RelativeLayout settingsDeviceRlFhsjElecControlSilence;
    //电量清零
    @BindView(R.id.settings_device_rl_fhsj_elec_control_zero_power)
    RelativeLayout settingsDeviceRlFhsjElecControlZeroPower;

    private String[] blePowerItems;
    private String[] bleTimeItems;
    private String[] loraTxpItems;
    private String[] loraEirpItems;
    private String[] loraEirpValues;

    private boolean isIBeaconEnabled;
    private String uuid;

    private SensoroSlot sensoroSlotArray[];
    private String[] slotItems;
    private int slotItemSelectIndex[] = new int[4];

    private String custom_package1;
    private String custom_package2;
    private String custom_package3;
    private boolean isCustomPackage1Enabled;
    private boolean isCustomPackage2Enabled;
    private boolean isCustomPackage3Enabled;
    private boolean isSensorBroadcastEnabled;

    private SensoroDeviceConnection sensoroDeviceConnection;
    private SensoroDevice sensoroDevice = null;
    private String band = null;
    private String deviceType = null;

    private LoRaSettingApplication application;
    private SensoroDeviceConfiguration deviceConfiguration;
    private ProgressDialog progressDialog;
    private SensoroSensorTest sensoroSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        MobclickAgent.onPageStart("设备配置");
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
                SettingDeviceActivity.this.finish();
            }
        });
        initData();
        connectDevice();
    }

    private void initData() {
        sensoroDevice = getIntent().getParcelableExtra(EXTRA_NAME_DEVICE);
        band = getIntent().getStringExtra(EXTRA_NAME_BAND);
        deviceType = getIntent().getStringExtra(EXTRA_NAME_DEVICE_TYPE);
        if (deviceType.equals("node")) {
            this.blePowerItems = BLE_NODE_TXP_ARRAY;
        } else {
            this.blePowerItems = BLE_NOT_NODE_TXP_ARRAY;
        }

        this.bleTimeItems = BLE_TIME_ARRAY;
        initLoraParam();
        this.slotItems = this.getResources().getStringArray(R.array.eddystone_slot_array);
        isIBeaconEnabled = true;
        uuid = "7274242C-B265-4F59-9F51-F70FB4B42150";
//        major = 10001;
//        minor = 10002;
//        bleTurnOffTime = 0;
//        bleTurnOnTime = 0;
        sensoroDevice.setBleOnTime(0);
        sensoroDevice.setBleOffTime(0);
        sensoroDevice.setMinor(10002);
        sensoroDevice.setMajor(10001);
        //
        custom_package1 = "";
        custom_package2 = "";
        custom_package3 = "";
        isCustomPackage1Enabled = false;
        isCustomPackage2Enabled = false;
        isCustomPackage3Enabled = false;
        isSensorBroadcastEnabled = false;

        slotItemSelectIndex[0] = STATUS_SLOT_DISABLED;
        slotItemSelectIndex[1] = STATUS_SLOT_DISABLED;
        slotItemSelectIndex[2] = STATUS_SLOT_DISABLED;
        slotItemSelectIndex[3] = STATUS_SLOT_DISABLED;
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
                loraEirpValues = Constants.LORA_EU868_MAX_EIRP_VALUE;
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
        loraTxpItems = new String[txp_array.length];
        for (int i = 0; i < txp_array.length; i++) {
            int txp = txp_array[i];
            loraTxpItems[i] = txp + " dBm";
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_setting_device;
    }

    private void registerUiEvent() {
        //TODO????
        setContentView(R.layout.activity_setting_device);
        ButterKnife.bind(this);
        resetRootLayout();
        backImageView.setOnClickListener(this);
        saveTextView.setOnClickListener(this);
        iBeaconSwitchCompat.setOnCheckedChangeListener(this);
        uuidRelativeLayout.setOnClickListener(this);
        majorRelativeLayout.setOnClickListener(this);
        minorRelativeLayout.setOnClickListener(this);
        powerRelativeLayout.setOnClickListener(this);
        advIntervalRelativeLayout.setOnClickListener(this);
        bleTurnOnTimeLinearLayout.setOnClickListener(this);
        bleTurnOffTimeLinearLayout.setOnClickListener(this);
        loraTxpRelativeLayout.setOnClickListener(this);
        loraEirpRelativeLayout.setOnClickListener(this);
        loraAdIntervalRelativeLayout.setOnClickListener(this);
        eddyStoneSlot1Item1Layout.setOnClickListener(this);
        eddyStoneSlot1Item2Layout.setOnClickListener(this);
        eddyStoneSlot2Item1Layout.setOnClickListener(this);
        eddyStoneSlot2Item2Layout.setOnClickListener(this);
        eddyStoneSlot3Item1Layout.setOnClickListener(this);
        eddyStoneSlot3Item2Layout.setOnClickListener(this);
        eddyStoneSlot4Item1Layout.setOnClickListener(this);
        eddyStoneSlot4Item2Layout.setOnClickListener(this);
        sensorBroadcastSwitchCompat.setOnCheckedChangeListener(this);
        customPackage1RelativeLayout.setOnClickListener(this);
        customPackage2RelativeLayout.setOnClickListener(this);
        customPackage3RelativeLayout.setOnClickListener(this);
        customPackage1SwitchCompat.setOnCheckedChangeListener(this);
        customPackage2SwitchCompat.setOnCheckedChangeListener(this);
        customPackage3SwitchCompat.setOnCheckedChangeListener(this);
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
        appParamLayout.setOnClickListener(this);
        uploadIntervalLayout.setOnClickListener(this);
        confirmLayout.setOnClickListener(this);
        zeroCommandTextView.setOnClickListener(this);
        pitchAngleUpperRelativeLayout.setOnClickListener(this);
        pitchAngleLowerRelativeLayout.setOnClickListener(this);
        rollAngleUpperRelativeLayout.setOnClickListener(this);
        rollAngleLowerRelativeLayout.setOnClickListener(this);
        yawAngleUpperRelativeLayout.setOnClickListener(this);
        yawAngleLowerRelativeLayout.setOnClickListener(this);
        waterPressureUpperRelativeLayout.setOnClickListener(this);
        waterPressureLowerRelativeLayout.setOnClickListener(this);
        settingsDeviceRlTemperaturePressureUpper.setOnClickListener(this);
        settingsDeviceRlTemperaturePressureLower.setOnClickListener(this);
        settingsDeviceRlTemperaturePressureStepUpper.setOnClickListener(this);
        settingsDeviceRlTemperaturePressureStepLower.setOnClickListener(this);
        //电表
        settingsDeviceRlFhsjElecPwd.setOnClickListener(this);
        settingsDeviceRlFhsjElecLeak.setOnClickListener(this);
        settingsDeviceRlFhsjElecTemp.setOnClickListener(this);
        settingsDeviceRlFhsjElecCurrent.setOnClickListener(this);
        settingsDeviceRlFhsjElecOverload.setOnClickListener(this);
        settingsDeviceRlFhsjElecOverpressure.setOnClickListener(this);
        settingsDeviceRlFhsjElecUndervoltage.setOnClickListener(this);
        //电表控制
        settingsDeviceRlFhsjElecControlReset.setOnClickListener(this);
        settingsDeviceRlFhsjElecControlRestore.setOnClickListener(this);
        settingsDeviceRlFhsjElecControlAirSwitch.setOnClickListener(this);
        settingsDeviceRlFhsjElecControlSelfTest.setOnClickListener(this);
        settingsDeviceRlFhsjElecControlSilence.setOnClickListener(this);
        settingsDeviceRlFhsjElecControlZeroPower.setOnClickListener(this);
    }

    private void refresh() {
        if (sensoroDevice != null) {
            try {
                eddyStoneSlot1Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot2Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot4Item1.setText(this.getString(R.string.slot1_name));
                if (sensoroDevice.hasIbeacon()) {
                    eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
                    uuid = sensoroDevice.getProximityUUID();
                    eddyStoneSlot4Item1.setText(this.getString(R.string.slot1_name));
                    int major = sensoroDevice.getMajor();
                    int minor = sensoroDevice.getMinor();
                    if (uuid != null) {
                        StringBuilder uuidString = new StringBuilder(uuid.toUpperCase());
                        uuidString.insert(8, "-");
                        uuidString.insert(13, "-");
                        uuidString.insert(18, "-");
                        uuidString.insert(23, "-");
                        uuidTextView.setText(uuidString);
                    }
                    majorTextView.setText(String.format("0x%04X", major));
                    minorTextView.setText(String.format("0x%04X", minor));
                    ibeaconLayout.setVisibility(VISIBLE);
                } else {
                    ibeaconLayout.setVisibility(GONE);
                }

                if (sensoroDevice.hasBleParam()) {
                    Calendar cal = Calendar.getInstance(Locale.getDefault());
                    int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
                    int offset = zoneOffset / 60 / 60 / 1000;
                    if (sensoroDevice.hasBleOffTime()) {
                        int bleTurnOffTime = sensoroDevice.getBleOffTime();
                        bleTurnOffTime += offset;
                        int showTurnOffTime = bleTurnOffTime;
                        if (bleTurnOffTime >= 24) {
                            showTurnOffTime -= 24;
                        }
                        turnOffTextView.setText(showTurnOffTime + ":00");
                    } else {
                        bleTurnOffTimeLinearLayout.setVisibility(GONE);
                    }
                    if (sensoroDevice.hasBleOnTime()) {
                        int bleTurnOnTime = sensoroDevice.getBleOnTime();
                        bleTurnOnTime += offset;
                        int showTurnOnTime = bleTurnOnTime;
                        if (bleTurnOnTime >= 24) {
                            showTurnOnTime -= 24;
                        }
                        turnOnTexView.setText(showTurnOnTime + ":00");
                    } else {
                        bleTurnOnTimeLinearLayout.setVisibility(GONE);
                    }
                    if (sensoroDevice.hasBleInterval()) {
                        float bleInt = sensoroDevice.getBleInt();
                        advIntervalTextView.setText(bleInt + " ms");
                    } else {
                        advIntervalRelativeLayout.setVisibility(GONE);
                    }
                    if (sensoroDevice.hasBleTxp()) {
                        int bleTxp = sensoroDevice.getBleTxp();
                        powerTextView.setText(bleTxp + " dBm");
                    } else {
                        powerRelativeLayout.setVisibility(GONE);
                    }
                    bleLayout.setVisibility(VISIBLE);
                } else {
                    bleLayout.setVisibility(GONE);
                }
                if (sensoroDevice.hasLoraParam()) {
                    if (sensoroDevice.hasMaxEirp()) {
                        loraEirpRelativeLayout.setVisibility(VISIBLE);
                        loraTxpRelativeLayout.setVisibility(GONE);
                        int loraTxp = sensoroDevice.getLoraTxp();
                        String loraEirpValue = loraEirpValues[loraTxp];
                        loraEirpTextView.setText("" + loraEirpValue);
                    } else {
                        loraEirpRelativeLayout.setVisibility(GONE);
                        loraTxpRelativeLayout.setVisibility(VISIBLE);
                        int loraTxp = sensoroDevice.getLoraTxp();
                        loraTxpTextView.setText(loraTxp + " dBm");
                    }
                    if (sensoroDevice.hasLoraInterval()) {
                        float loraInt = sensoroDevice.getLoraInt();
                        loraAdIntervalTextView.setText(loraInt + "s");
                        loraAdIntervalRelativeLayout.setVisibility(VISIBLE);
                    } else {
                        loraAdIntervalRelativeLayout.setVisibility(GONE);
                    }
                    loraLayout.setVisibility(VISIBLE);
                } else {
                    loraLayout.setVisibility(GONE);
                }

                if (sensoroDevice.hasSensorBroadcast()) {
                    sensorBroadcastEnableLayout.setVisibility(VISIBLE);
                } else {
                    sensorBroadcastEnableLayout.setVisibility(GONE);
                }

                if (sensoroDevice.hasCustomPackage()) {
                    customLayout.setVisibility(VISIBLE);
                } else {
                    customLayout.setVisibility(GONE);
                }

                if (sensoroDevice.hasEddyStone()) {
                    sensoroSlotArray = sensoroDevice.getSlotArray();
                    try {
                        //todo 还有这里啦
                        String firmwareVersion = sensoroDevice.getFirmwareVersion();
                        if (firmwareVersion.compareTo(SensoroDevice.FV_1_2) >0 ) { // 1.3以后没有custom package 3
                            findViewById(R.id.settings_device_ll_custome_package3).setVisibility(GONE);
                        } else {
                            findViewById(R.id.settings_device_ll_sensor_enable).setVisibility(GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (sensoroSlotArray != null) {
                        refreshSlot();
                    }
                    eddystoneLayout.setVisibility(VISIBLE);
                } else {
                    eddystoneLayout.setVisibility(GONE);
                }

                if (sensoroDevice.hasSensorParam()) {
                    sensorParamLayout.setVisibility(VISIBLE);
                    if (sensoroSensor.hasCo) {
                        coLinearLayout.setVisibility(VISIBLE);
                        coLinearLayout.setOnClickListener(this);
                        if (sensoroSensor.co.has_alarmStepHigh) {
                            Float coAlarmHigh = sensoroSensor.co.alarmHigh_float;
                            coTextView.setText(coAlarmHigh + "");
                        }

                    } else {
                        coLinearLayout.setVisibility(GONE);
                    }
                    if (sensoroSensor.hasCo2) {
                        co2LinearLayout.setVisibility(VISIBLE);
                        co2LinearLayout.setOnClickListener(this);
                        if (sensoroSensor.co2.has_alarmHigh) {
                            Float co2AlarmHigh = sensoroSensor.co2.alarmHigh_float;
                            co2TextView.setText(co2AlarmHigh + "");
                        }

                    } else {
                        co2LinearLayout.setVisibility(GONE);
                    }
                    if (sensoroSensor.hasNo2) {
                        no2LinearLayout.setVisibility(VISIBLE);
                        no2LinearLayout.setOnClickListener(this);
                        if (sensoroSensor.no2.has_alarmHigh) {
                            Float no2AlarmHigh = sensoroSensor.no2.alarmHigh_float;
                            no2TextView.setText(no2AlarmHigh + "");
                        }

                    } else {
                        no2LinearLayout.setVisibility(GONE);
                    }
                    if (sensoroSensor.hasCh4) {
                        ch4LinearLayout.setVisibility(VISIBLE);
                        ch4LinearLayout.setOnClickListener(this);
                        if (sensoroSensor.ch4.has_alarmHigh) {
                            Float ch4AlarmHigh = sensoroSensor.ch4.alarmHigh_float;
                            ch4TextView.setText(ch4AlarmHigh + "");
                        }

                    } else {
                        ch4LinearLayout.setVisibility(GONE);
                    }
                    if (sensoroSensor.hasLpg) {
                        lpgLinearLayout.setVisibility(VISIBLE);
                        lpgLinearLayout.setOnClickListener(this);
                        if (sensoroSensor.lpg.has_alarmHigh) {
                            Float lpgAlarmHigh = sensoroSensor.lpg.alarmHigh_float;
                            lpgTextView.setText(lpgAlarmHigh + "");
                        }
                    } else {
                        lpgLinearLayout.setVisibility(GONE);
                    }
                    if (sensoroSensor.hasPm25) {
                        pm25LinearLayout.setVisibility(VISIBLE);
                        pm25LinearLayout.setOnClickListener(this);
                        if (sensoroSensor.pm25.has_alarmHigh) {
                            Float pm25AlarmHigh = sensoroSensor.pm25.alarmHigh_float;
                            pm25TextView.setText(pm25AlarmHigh + "");
                        }

                    } else {
                        pm25LinearLayout.setVisibility(GONE);
                    }
                    if (sensoroSensor.hasPm10) {
                        pm10LinearLayout.setVisibility(VISIBLE);
                        pm10LinearLayout.setOnClickListener(this);
                        if (sensoroSensor.pm10.has_alarmHigh) {
                            Float pm10AlarmHigh = sensoroSensor.pm10.alarmHigh_float;
                            pm10TextView.setText(pm10AlarmHigh + "");
                        }

                    } else {
                        pm10LinearLayout.setVisibility(GONE);
                    }
                    if (sensoroSensor.hasTemperature) {
                        boolean has_alarmHigh = sensoroSensor.temperature.has_alarmHigh;
                        if (has_alarmHigh) {
                            Float tempAlarmHigh = sensoroSensor.temperature.alarmHigh_float;
                            tempUpperTextView.setText(tempAlarmHigh + "");
                        }
                        boolean has_alarmLow = sensoroSensor.temperature.has_alarmLow;
                        if (has_alarmLow) {
                            Float tempAlarmLow = sensoroSensor.temperature.alarmLow_float;
                            tempLowerTextView.setText(tempAlarmLow + "");
                        }

                        tempLinearLayout.setVisibility(has_alarmHigh || has_alarmLow ? VISIBLE : GONE);
                    } else {
                        tempLinearLayout.setVisibility(GONE);
                    }
                    if (sensoroSensor.hasHumidity) {
                        boolean has_alarmHigh = sensoroSensor.humidity.has_alarmHigh;
                        if (has_alarmHigh) {
                            Float humidityAlarmHigh = sensoroSensor.humidity.alarmHigh_float;
                            humidityUpperTextView.setText(humidityAlarmHigh + "");
                        }
                        boolean has_alarmLow = sensoroSensor.humidity.has_alarmLow;
                        if (has_alarmLow) {
                            Float humidityAlarmLow = sensoroSensor.humidity.alarmLow_float;
                            humidityLowerTextView.setText(humidityAlarmLow + "");
                        }
                        humidityLinearLayout.setVisibility(has_alarmHigh || has_alarmLow ? VISIBLE : GONE);
                    } else {
                        humidityLinearLayout.setVisibility(GONE);
                    }

                    if (sensoroSensor.hasSmoke) {
                        smokeLinearLayout.setVisibility(VISIBLE);
                        if (sensoroSensor.smoke.status == 0) {
                            smokeStatusTextView.setText(getResources().getStringArray(R.array.smoke_status_array)[0]);
                        } else {
                            smokeStatusTextView.setText(getResources().getStringArray(R.array.smoke_status_array)[1]);
                        }

                    } else {
                        smokeLinearLayout.setVisibility(GONE);
                    }

                    if (sensoroSensor.hasPitch) {
                        boolean has_alarmHigh = sensoroSensor.pitch.has_alarmHigh;
                        if (has_alarmHigh) {
                            Float pitchAngleAlarmHigh = sensoroSensor.pitch.alarmHigh_float;
                            pitchAngleUpperTextView.setText(pitchAngleAlarmHigh + "");
                        }
                        boolean has_alarmLow = sensoroSensor.pitch.has_alarmLow;
                        if (has_alarmLow) {
                            Float pitchAngleAlarmLow = sensoroSensor.pitch.alarmLow_float;
                            pitchAngleLowerTextView.setText(pitchAngleAlarmLow + "");
                        }
                        pitchAngleLinearLayout.setVisibility(has_alarmHigh || has_alarmLow ? VISIBLE : GONE);

                    } else {
                        pitchAngleLinearLayout.setVisibility(GONE);
                    }
                    if (sensoroSensor.hasRoll) {
                        boolean has_alarmHigh = sensoroSensor.roll.has_alarmHigh;
                        if (has_alarmHigh) {
                            Float rollAngleAlarmHigh = sensoroSensor.roll.alarmHigh_float;
                            rollAngleUpperTextView.setText(rollAngleAlarmHigh + "");
                        }
                        boolean has_alarmLow = sensoroSensor.roll.has_alarmLow;
                        if (has_alarmLow) {
                            Float rollAngleAlarmLow = sensoroSensor.roll.alarmLow_float;
                            rollAngleLowerTextView.setText(rollAngleAlarmLow + "");
                        }
                        rollAngleLinearLayout.setVisibility(has_alarmHigh || has_alarmLow ? VISIBLE : GONE);

                    } else {
                        rollAngleLinearLayout.setVisibility(GONE);
                    }
                    if (sensoroSensor.hasYaw) {
                        boolean has_alarmHigh = sensoroSensor.yaw.has_alarmHigh;
                        if (has_alarmHigh) {
                            Float yawAngleAlarmHigh = sensoroSensor.yaw.alarmHigh_float;
                            yawAngleUpperTextView.setText(yawAngleAlarmHigh + "");
                        }
                        boolean has_alarmLow = sensoroSensor.yaw.has_alarmLow;
                        if (has_alarmLow) {
                            Float yawAngleAlarmLow = sensoroSensor.yaw.alarmLow_float;
                            yawAngleLowerTextView.setText(yawAngleAlarmLow + "");
                        }
                        yawAngleLinearLayout.setVisibility(has_alarmHigh || has_alarmLow ? VISIBLE : GONE);

                    } else {
                        yawAngleLinearLayout.setVisibility(GONE);
                    }

                    if (sensoroSensor.hasWaterPressure) {
                        boolean has_alarmHigh = sensoroSensor.waterPressure.has_alarmHigh;
                        if (has_alarmHigh) {
                            Float waterPressureAlarmHigh = sensoroSensor.waterPressure.alarmHigh_float;
                            waterPressureUpperTextView.setText(waterPressureAlarmHigh + "");
                        }
                        boolean has_alarmLow = sensoroSensor.waterPressure.has_alarmLow;
                        if (has_alarmLow) {
                            Float waterPressureAlarmLow = sensoroSensor.waterPressure.alarmLow_float;
                            waterPressureLowerTextView.setText(waterPressureAlarmLow + "");
                        }
                        waterPressureLinearLayout.setVisibility(has_alarmHigh || has_alarmLow ? VISIBLE : GONE);

                    } else {
                        waterPressureLinearLayout.setVisibility(GONE);
                    }
                    /**
                     * 设置单通道温度值
                     */
                    boolean hasMultiTemperature = sensoroSensor.hasMultiTemp;
                    settingsDeviceLlTemperaturePressure.setVisibility(hasMultiTemperature ? VISIBLE : GONE);
                    if (hasMultiTemperature) {
                        if (sensoroSensor.multiTemperature.has_alarmHigh) {
                            settingsDeviceRlTemperaturePressureUpper.setVisibility(VISIBLE);
                            Integer alarmHigh = sensoroSensor.multiTemperature.alarmHigh_int;
                            settingsDeviceTvTemperaturePressureUpperLimit.setText(alarmHigh / 100f + "");
                        } else {
                            settingsDeviceRlTemperaturePressureUpper.setVisibility(GONE);
                        }
                        if (sensoroSensor.multiTemperature.has_alarmLow) {
                            settingsDeviceRlTemperaturePressureLower.setVisibility(VISIBLE);
                            Integer alarmLow = sensoroSensor.multiTemperature.alarmLow_int;
                            settingsDeviceTvTemperaturePressureLowerLimit.setText(alarmLow / 100f + "");
                        } else {
                            settingsDeviceRlTemperaturePressureLower.setVisibility(GONE);
                        }
                        if (sensoroSensor.multiTemperature.has_alarmStepHigh) {
                            settingsDeviceRlTemperaturePressureStepUpper.setVisibility(VISIBLE);
                            Integer alarmStepHigh = sensoroSensor.multiTemperature.alarmStepHigh_int;
                            settingsDeviceTvTemperaturePressureUpperStepLimit.setText(alarmStepHigh / 100f + "");
                        } else {
                            settingsDeviceRlTemperaturePressureStepUpper.setVisibility(GONE);
                        }
                        if (sensoroSensor.multiTemperature.has_alarmStepLow) {
                            settingsDeviceRlTemperaturePressureStepLower.setVisibility(VISIBLE);
                            Integer alarmStepLow = sensoroSensor.multiTemperature.alarmStepLow_int;
                            settingsDeviceTvTemperaturePressureUpperStepLimit.setText(alarmStepLow / 100f + "");
                        } else {
                            settingsDeviceRlTemperaturePressureStepLower.setVisibility(GONE);
                        }
                    }
                    boolean hasFireData = sensoroSensor.hasFireData;
                    settingsDeviceLlFhsjElec.setVisibility(hasFireData ? VISIBLE : GONE);
                    settingsDeviceLlFhsjElecControl.setVisibility(hasFireData ? VISIBLE : GONE);
                    if (hasFireData) {
                        if (sensoroSensor.elecFireData.hasSensorPwd) {
                            settingsDeviceTvFhsjElecPwd.setText(sensoroSensor.elecFireData.sensorPwd + "");
                        } else {
                            settingsDeviceRlFhsjElecPwd.setVisibility(GONE);
                        }
                        if (sensoroSensor.elecFireData.hasLeakageTh) {
                            settingsDeviceTvFhsjElecLeak.setText(sensoroSensor.elecFireData.leakageTh + "");
                        } else {
                            settingsDeviceRlFhsjElecLeak.setVisibility(GONE);
                        }
                        if (sensoroSensor.elecFireData.hasTempTh) {
                            settingsDeviceTvFhsjElecTemp.setText(sensoroSensor.elecFireData.tempTh + "");
                        } else {
                            settingsDeviceRlFhsjElecTemp.setVisibility(GONE);
                        }
                        if (sensoroSensor.elecFireData.hasCurrentTh) {
                            settingsDeviceTvFhsjElecCurrent.setText(sensoroSensor.elecFireData.currentTh + "");
                        } else {
                            settingsDeviceRlFhsjElecCurrent.setVisibility(GONE);
                        }
                        if (sensoroSensor.elecFireData.hasLoadTh) {
                            settingsDeviceTvFhsjElecOverload.setText(sensoroSensor.elecFireData.loadTh + "");
                        } else {
                            settingsDeviceRlFhsjElecOverload.setVisibility(GONE);
                        }
                        if (sensoroSensor.elecFireData.hasVolHighTh) {
                            settingsDeviceTvFhsjElecOverpressure.setText(sensoroSensor.elecFireData.volHighTh + "");
                        } else {
                            settingsDeviceRlFhsjElecOverpressure.setVisibility(GONE);
                        }
                        if (sensoroSensor.elecFireData.hasVolLowTh) {
                            settingsDeviceTvFhsjElecUndervoltage.setText(sensoroSensor.elecFireData.volLowTh + "");
                        } else {
                            settingsDeviceRlFhsjElecUndervoltage.setVisibility(GONE);
                        }

                    }
                    if (sensoroSensor.hasPitch || sensoroSensor
                            .hasRoll || sensoroSensor.hasYaw) {
                        zeroCommandLinearLayout.setVisibility(VISIBLE);
                    } else {
                        zeroCommandLinearLayout.setVisibility(GONE);
                    }

                } else {
                    sensorParamLayout.setVisibility(GONE);
                }

                if (sensoroDevice.hasAppParam()) {
                    appParamLayout.setVisibility(VISIBLE);
                    if (sensoroDevice.hasUploadInterval()) {
                        uploadIntervalLayout.setVisibility(VISIBLE);
                        uploadIntervalLayout.setOnClickListener(this);
                        Integer uploadInterval = sensoroDevice.getUploadInterval();
                        uploadIntervalTextView.setText(sensoroDevice.getUploadInterval() + "s");
                    } else {
                        uploadIntervalLayout.setVisibility(GONE);
                    }
                    if (sensoroDevice.hasConfirm()) {
                        confirmLayout.setVisibility(VISIBLE);
                        confirmLayout.setOnClickListener(this);
                        Integer appParamConfirm = sensoroDevice.getConfirm();

                        confirmTextView.setText(this.getResources().getStringArray(R.array.status_array)
                                [sensoroDevice.getConfirm() == 0 ? 1 : 0]);
                    } else {
                        confirmLayout.setVisibility(GONE);
                    }
                } else {
                    appParamLayout.setVisibility(GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.connect_failed, Toast.LENGTH_SHORT).show();
                this.finish();
                return;
            }
            Toast.makeText(getApplicationContext(), getString(R.string.connect_success), Toast.LENGTH_SHORT).show();
        }
    }

    public void refreshSlot() {
        for (int i = 0; i < sensoroSlotArray.length; i++) {
            SensoroSlot slot = sensoroSlotArray[i];
            switch (slot.getType()) {
                case ProtoMsgCfgV1U1.SlotType.SLOT_SENSOR_VALUE:
                    String firmwareVersion = sensoroDevice.getFirmwareVersion();
                    if (firmwareVersion.compareTo(SensoroDevice.FV_1_2) > 0) { // 1.3以后没有custom package 3
                        isSensorBroadcastEnabled = slot.isActived() == 1 ? true : false;
                        sensorBroadcastSwitchCompat.setChecked(slot.isActived() == 1 ? true : false);
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_NONE_VALUE:

                    switch (slot.getIndex()) {
                        case EDDYSTONE_SLOT_UID:
                            eddyStoneSlot1Item1Value.setText(getResources().getStringArray(R.array
                                    .eddystone_slot_array)[0]);
                            break;
                        case EDDYSTONE_SLOT_URL:
                            eddyStoneSlot2Item1Value.setText(getResources().getStringArray(R.array
                                    .eddystone_slot_array)[0]);
                            break;
                        case EDDYSTONE_SLOT_EID:
                            eddyStoneSlot3Item1Value.setText(getResources().getStringArray(R.array
                                    .eddystone_slot_array)[0]);
                            break;
                        case EDDYSTONE_SLOT_TLM:
                            eddyStoneSlot4Item1Value.setText(getResources().getStringArray(R.array
                                    .eddystone_slot_array)[0]);
                            break;
                        case EDDYSTONE_SLOT_CUSTOM1:
                            isCustomPackage1Enabled = false;
                            customPackage1RelativeLayout.setAlpha(0.5f);
                            customPackage1RelativeLayout.setEnabled(false);
                            break;
                        case EDDYSTONE_SLOT_CUSTOM2:
                            isCustomPackage2Enabled = false;
                            customPackage2RelativeLayout.setAlpha(0.5f);
                            customPackage2RelativeLayout.setEnabled(false);
                            break;
                        case EDDYSTONE_SLOT_CUSTOM3:
                            isCustomPackage3Enabled = false;
                            customPackage3RelativeLayout.setAlpha(0.5f);
                            customPackage3RelativeLayout.setEnabled(false);
                            break;
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_UID_VALUE:
                    slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_UID;
                    switch (slot.getIndex()) {
                        case 0:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot1Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[1]);
                                eddyStoneSlot1Item2.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[1]);
                                eddyStoneSlot1Item2Value.setText(slot.getFrame());
                                eddyStoneSlot1Item2Layout.setVisibility(View.VISIBLE);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot1Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                        case 1:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot2Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[1]);
                                eddyStoneSlot2Item2.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[1]);
                                eddyStoneSlot2Item2Value.setText(slot.getFrame());
                                eddyStoneSlot2Item2Layout.setVisibility(View.VISIBLE);

                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot2Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                        case 2:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot3Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[1]);
                                eddyStoneSlot3Item2.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[1]);
                                eddyStoneSlot3Item2Value.setText(slot.getFrame());
                                eddyStoneSlot3Item2Layout.setVisibility(View.VISIBLE);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot3Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                        case 3:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot4Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[1]);
                                eddyStoneSlot4Item2.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[1]);
                                eddyStoneSlot4Item2Value.setText(slot.getFrame());
                                eddyStoneSlot4Item2Layout.setVisibility(View.VISIBLE);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot4Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_URL_VALUE:
                    slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_URL;
                    switch (slot.getIndex()) {
                        case 0:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot1Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[2]);
                                eddyStoneSlot1Item2.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[2]);
                                eddyStoneSlot1Item2Value.setText(slot.getFrame());
                                eddyStoneSlot1Item2Layout.setVisibility(View.VISIBLE);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot1Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                        case 1:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot2Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[2]);
                                eddyStoneSlot2Item2.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[2]);
                                eddyStoneSlot2Item2Value.setText(slot.getFrame());
                                eddyStoneSlot2Item2Layout.setVisibility(View.VISIBLE);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot2Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                        case 2:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot3Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[1]);
                                eddyStoneSlot3Item2.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[1]);
                                eddyStoneSlot3Item2Value.setText(slot.getFrame());
                                eddyStoneSlot3Item2Layout.setVisibility(View.VISIBLE);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot3Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                        case 3:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot4Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[2]);
                                eddyStoneSlot4Item2.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[2]);
                                eddyStoneSlot4Item2Value.setText(slot.getFrame());
                                eddyStoneSlot4Item2Layout.setVisibility(View.VISIBLE);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot4Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_EID_VALUE:
                    slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_EID;
                    switch (slot.getIndex()) {
                        case 0:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot1Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[3]);
                                eddyStoneSlot1Item2.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[3]);
                                String value = slot.getFrame();
                                byte[] value_data = SensoroUtils.HexString2Bytes(value);
                                byte[] ik_data = new byte[16];
                                System.arraycopy(value_data, 9, ik_data, 0, ik_data.length);
                                String ik = SensoroUtils.bytesToHex(ik_data);
                                eddyStoneSlot1Item2.setText(getString(R.string.eid_list));
                                eddyStoneSlot1Item2Value.setText(ik);
                                eddyStoneSlot1Item2Layout.setVisibility(View.VISIBLE);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot1Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                        case 1:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot2Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[3]);
                                eddyStoneSlot2Item2.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[3]);
                                String value = slot.getFrame();
                                byte[] value_data = SensoroUtils.HexString2Bytes(value);
                                byte[] ik_data = new byte[16];
                                System.arraycopy(value_data, 9, ik_data, 0, ik_data.length);
                                String ik = SensoroUtils.bytesToHex(ik_data);
                                eddyStoneSlot2Item2.setText(getString(R.string.eid_list));
                                eddyStoneSlot2Item2Value.setText(ik);
                                eddyStoneSlot2Item2Layout.setVisibility(View.VISIBLE);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot2Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                        case 2:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot3Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[3]);
                                eddyStoneSlot3Item2.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[3]);
                                String value = slot.getFrame();
                                byte[] value_data = SensoroUtils.HexString2Bytes(value);
                                byte[] ik_data = new byte[16];
                                System.arraycopy(value_data, 9, ik_data, 0, ik_data.length);
                                String ik = SensoroUtils.bytesToHex(ik_data);
                                eddyStoneSlot3Item2.setText(getString(R.string.eid_list));
                                eddyStoneSlot3Item2Value.setText(ik);
                                eddyStoneSlot3Item2Layout.setVisibility(View.VISIBLE);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot3Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                        case 3:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot4Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[3]);
                                eddyStoneSlot4Item2.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[3]);
                                String value = slot.getFrame();
                                byte[] value_data = SensoroUtils.HexString2Bytes(value);
                                byte[] ik_data = new byte[16];
                                System.arraycopy(value_data, 9, ik_data, 0, ik_data.length);
                                String ik = SensoroUtils.bytesToHex(ik_data);
                                eddyStoneSlot4Item2.setText(getString(R.string.eid_list));
                                eddyStoneSlot4Item2Value.setText(ik);
                                eddyStoneSlot4Item2Layout.setVisibility(View.VISIBLE);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot4Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_EDDYSTONE_TLM_VALUE:
                    slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_TLM;
                    switch (slot.getIndex()) {
                        case 0:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot1Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[4]);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot1Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }

                            break;
                        case 1:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot2Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[4]);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot2Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                        case 2:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot3Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[4]);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot3Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                        case 3:
                            if (slot.isActived() == 1) {
                                eddyStoneSlot4Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[4]);
                            } else {
                                slotItemSelectIndex[slot.getIndex()] = STATUS_SLOT_DISABLED;
                                eddyStoneSlot4Item1Value.setText(getResources().getStringArray(R.array
                                        .eddystone_slot_array)[0]);
                            }
                            break;
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_CUSTOME_VALUE:
                    switch (slot.getIndex()) {
                        case 5:
                            custom_package1 = slot.getFrame();
                            isCustomPackage1Enabled = slot.isActived() == 1 ? true : false;
                            customPackage1TextView.setText(custom_package1);
                            customPackage1SwitchCompat.setChecked(isCustomPackage1Enabled);
                            if (isCustomPackage1Enabled) {
                                customPackage1RelativeLayout.setAlpha(1f);
                                customPackage1RelativeLayout.setEnabled(true);
                            } else {
                                customPackage1RelativeLayout.setAlpha(0.5f);
                                customPackage1RelativeLayout.setEnabled(false);
                            }
                            break;
                        case 6:
                            custom_package2 = slot.getFrame();
                            isCustomPackage2Enabled = slot.isActived() == 1 ? true : false;
                            customPackage2TextView.setText(custom_package2);
                            customPackage2SwitchCompat.setChecked(isCustomPackage2Enabled);
                            if (isCustomPackage2Enabled) {
                                customPackage2RelativeLayout.setAlpha(1f);
                                customPackage2RelativeLayout.setEnabled(true);
                            } else {
                                customPackage2RelativeLayout.setAlpha(0.5f);
                                customPackage2RelativeLayout.setEnabled(false);
                            }
                            break;
                        case 7:
                            custom_package3 = slot.getFrame();
                            isCustomPackage3Enabled = slot.isActived() == 1 ? true : false;
                            customPackage3TextView.setText(custom_package3);
                            customPackage3SwitchCompat.setChecked(isCustomPackage3Enabled);
                            if (isCustomPackage3Enabled) {
                                customPackage3RelativeLayout.setAlpha(1f);
                                customPackage3RelativeLayout.setEnabled(true);
                            } else {
                                customPackage3RelativeLayout.setAlpha(0.5f);
                                customPackage3RelativeLayout.setEnabled(false);
                            }
                            break;
                    }
                    break;
                case ProtoMsgCfgV1U1.SlotType.SLOT_IBEACON_VALUE:
                    isIBeaconEnabled = sensoroSlotArray[i].isActived() == 1 ? true : false;
                    iBeaconSwitchCompat.setChecked(isIBeaconEnabled);
                    break;
            }
        }
    }

    private void saveConfiguration() {
        if (sensoroDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_05) {
            saveConfigurationWithHighVersion();
        } else {
            saveConfigurationWithLowVersion();
        }
    }

    private void saveConfigurationWithLowVersion() {
        progressDialog.setTitle(getString(R.string.settings));
        progressDialog.setMessage(getString(R.string.saving));
        progressDialog.show();

        try {
            {
                int slot1_type = 0;
                boolean slot1_status = false;
                String slot1_frame = eddyStoneSlot1Item2Value.getText().toString();
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
            }
            {
                int slot2_type = 0;
                boolean slot2_status = false;
                String slot2_frame = eddyStoneSlot2Item2Value.getText().toString();
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
            }
            {
                int slot3_type = 0;
                boolean slot3_status = false;
                String slot3_frame = eddyStoneSlot3Item2Value.getText().toString();
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
            }
            {
                int slot4_type = 0;
                boolean slot4_status = false;
                String slot4_frame = eddyStoneSlot3Item2Value.getText().toString();
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
            }
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

            String firmwareVersion = sensoroDevice.getFirmwareVersion();

            if (firmwareVersion.compareTo(SensoroDevice.FV_1_2)>0) {
                sensoroSlotArray[7].setType(ProtoMsgCfgV1U1.SlotType.SLOT_SENSOR_VALUE);
                sensoroSlotArray[7].setActived(isSensorBroadcastEnabled ? 1 : 0);
            } else {
                if (isCustomPackage3Enabled) {
                    sensoroSlotArray[7].setFrame(custom_package3);
                    sensoroSlotArray[7].setType(ProtoMsgCfgV1U1.SlotType.SLOT_CUSTOME_VALUE);
                }
                sensoroSlotArray[7].setActived(isCustomPackage3Enabled ? 1 : 0);
            }
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
            int offset = zoneOffset / 60 / 60 / 1000;
            int saveTurnOnTime = 0;
            int saveTurnOffTime = 0;
            if ((sensoroDevice.getBleOnTime() - offset) < 0) {
                saveTurnOnTime = 24 + (sensoroDevice.getBleOnTime() - offset);
            } else if ((sensoroDevice.getBleOnTime() - offset) == 0) {
                saveTurnOnTime = 0;
            } else {
                saveTurnOnTime = (sensoroDevice.getBleOnTime() - offset);
            }
            if ((sensoroDevice.getBleOffTime() - offset) < 0) {
                saveTurnOffTime = 24 + (sensoroDevice.getBleOffTime() - offset);
            } else if ((sensoroDevice.getBleOffTime() - offset) == 0) {
                saveTurnOffTime = 0;
            } else {
                saveTurnOffTime = (sensoroDevice.getBleOffTime() - offset);
            }

            SensoroDeviceConfiguration.Builder builder = new SensoroDeviceConfiguration.Builder();

            builder.setIBeaconEnabled(isIBeaconEnabled)
                    .setProximityUUID(uuid)
                    .setMajor(sensoroDevice.getMajor())
                    .setMinor(sensoroDevice.getMinor())
                    .setBleTurnOnTime(saveTurnOnTime)
                    .setBleTurnOffTime(saveTurnOffTime)
                    .setBleInt(sensoroDevice.getBleInt())
                    .setBleTxp(sensoroDevice.getBleTxp())
                    .setLoraTxp(sensoroDevice.getLoraTxp())
                    .setLoraInt(sensoroDevice.getLoraInt())
                    .setAppEui(sensoroDevice.getAppEui())
                    .setAppKey(sensoroDevice.getAppKey())
                    .setAppSkey(sensoroDevice.getAppSkey())
                    .setNwkSkey(sensoroDevice.getNwkSkey())
                    .setDevAdr(sensoroDevice.getDevAdr())
                    .setLoraAdr(sensoroDevice.getLoraAdr())
                    .setLoraDr(sensoroDevice.getLoraDr())
                    .setSensoroSlotArray(sensoroSlotArray);
            if (sensoroDevice.getDataVersion() == SensoroDeviceConnection.DATA_VERSION_04) {
                builder.setClassBEnabled(sensoroDevice.getClassBEnabled())
                        .setClassBPeriodicity(sensoroDevice.getClassBPeriodicity())
                        .setClassBDataRate(sensoroDevice.getClassBDataRate());
            }
            if (sensoroDevice.getPassword() == null || (sensoroDevice.getPassword() != null && !sensoroDevice
                    .getPassword().equals(""))) {
                builder.setPassword(sensoroDevice.getPassword());
            }
            deviceConfiguration = builder.build();
            sensoroDeviceConnection.writeDataConfiguration(deviceConfiguration, this);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void saveConfigurationWithHighVersion() {
        progressDialog.setTitle(getString(R.string.settings));
        progressDialog.setMessage(getString(R.string.saving));
        progressDialog.show();

        try {
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
            int offset = zoneOffset / 60 / 60 / 1000;
            int saveTurnOnTime = 0;
            int saveTurnOffTime = 0;
            if ((sensoroDevice.getBleOnTime() - offset) < 0) {
                saveTurnOnTime = 24 + (sensoroDevice.getBleOffTime() - offset);
            } else if ((sensoroDevice.getBleOnTime() - offset) == 0) {
                saveTurnOnTime = 0;
            } else {
                saveTurnOnTime = (sensoroDevice.getBleOnTime() - offset);
            }
            if ((sensoroDevice.getBleOffTime() - offset) < 0) {
                saveTurnOffTime = 24 + (sensoroDevice.getBleOffTime() - offset);
            } else if ((sensoroDevice.getBleOffTime() - offset) == 0) {
                saveTurnOffTime = 0;
            } else {
                saveTurnOffTime = (sensoroDevice.getBleOffTime() - offset);
            }
//            SensoroDeviceConfiguration.Builder builder = new SensoroDeviceConfiguration.Builder();
//            SensoroSensorConfiguration.Builder sensorBuilder = new SensoroSensorConfiguration.Builder();
//            if (sensoroDevice.hasSensorParam()) {
//                SensoroSensorTest sensoroSensorTest = sensoroDevice.getSensoroSensorTest();
//                if (sensoroSensorTest.hasCo) {
//                    sensorBuilder.setCoAlarmHigh(coAlarmHigh);
//                    sensorBuilder.setCoData(sensoroSensorTest.co.data_float);
//                    sensorBuilder.setHasCo(sensoroSensorTest.hasCo);
//                }
//                if (sensoroSensorTest.hasCo2) {
//                    sensorBuilder.setCo2AlarmHigh(co2AlarmHigh);
//                    sensorBuilder.setCo2Data(sensoroSensorTest.co.data_float);
//                    sensorBuilder.setHasCo2(sensoroSensorTest.hasCo2);
//                }
//                if (sensoroSensorTest.hasNo2) {
//                    sensorBuilder.setNo2AlarmHigh(no2AlarmHigh);
//                    sensorBuilder.setNo2Data(sensoroSensorTest.no2.data_float);
//                    sensorBuilder.setHasNo2(sensoroSensorTest.hasNo2);
//                }
//                if (sensoroSensorTest.hasCh4) {
//                    sensorBuilder.setCh4AlarmHigh(ch4AlarmHigh);
//                    sensorBuilder.setCh4Data(sensoroSensorTest.ch4.data_float);
//                    sensorBuilder.setHasCh4(sensoroSensorTest.hasCh4);
//                }
//                if (sensoroSensorTest.hasLpg) {
//                    sensorBuilder.setLpgAlarmHigh(lpgAlarmHigh);
//                    sensorBuilder.setLpgData(sensoroSensorTest.lpg.data_float);
//                    sensorBuilder.setHasLpg(sensoroSensorTest.hasLpg);
//                }
//                if (sensoroSensorTest.hasPm10) {
//                    sensorBuilder.setPm10AlarmHigh(pm10AlarmHigh);
//                    sensorBuilder.setPm10Data(sensoroSensorTest.pm10.data_float);
//                    sensorBuilder.setHasPm10(sensoroSensorTest.hasPm10);
//                }
//                if (sensoroSensorTest.hasPm25) {
//                    sensorBuilder.setPm25AlarmHigh(pm25AlarmHigh);
//                    sensorBuilder.setPm25Data(sensoroSensorTest.pm25.data_float);
//                    sensorBuilder.setHasPm25(sensoroSensorTest.hasPm25);
//                }
//                if (sensoroSensorTest.hasTemperature) {
//                    if (sensoroSensorTest.temperature.has_data) {
//
//                    }
////                    if (sensoroSensorTest.temperature.has_alarmHigh) {
//                        sensorBuilder.setTempAlarmHigh(tempAlarmHigh);
////                    }
////                    if (sensoroSensorTest.temperature.has_alarmLow) {
//                        sensorBuilder.setTempAlarmLow(tempAlarmLow);
////                    }
//                    sensorBuilder.setHasTemperature(sensoroSensorTest.hasTemperature);
//                }
//                if (sensoroSensorTest.hasHumidity) {
//                    if (sensoroSensorTest.humidity.has_data) {
//
//                    }
////                    if (sensoroSensorTest.humidity.has_alarmHigh) {
//                        sensorBuilder.setHumidityHigh(humidityAlarmHigh);
////                    }
////                    if (sensoroSensorTest.humidity.has_alarmLow) {
//                        sensorBuilder.setHumidityLow(humidityAlarmLow);
////                    }
//                    sensorBuilder.setHasHumidity(sensoroSensorTest.hasHumidity);
//                }
//                if (sensoroDevice.hasAppParam()) {
//                    builder.setHasUploadInterval(sensoroDevice.hasUploadInterval());
//                    if (sensoroDevice.hasUploadInterval()) {
//                        builder.setUploadIntervalData(uploadInterval);
//                    }
//                    if (sensoroDevice.hasConfirm()) {
//                        builder.setConfirmData(appParamConfirm);
//                    }
//                    builder.setHasConfirm(sensoroDevice.hasConfirm());
//                    builder.setHasAppParam(sensoroDevice.hasAppParam());
//                }
//                if (sensoroSensorTest.hasPitch) {
//                    sensorBuilder.setHasPitchAngle(sensoroSensorTest.hasPitch);
//
//                    if (sensoroSensorTest.pitch.has_data) {
//
//                    }
////                    if (sensoroSensorTest.pitch.has_alarmHigh) {
//                        sensorBuilder.setPitchAngleAlarmHigh(pitchAngleAlarmHigh);
////                    }
////                    if (sensoroSensorTest.pitch.has_alarmLow) {
//                        sensorBuilder.setPitchAngleAlarmLow(pitchAngleAlarmLow);
////                    }
//                }
//                if (sensoroSensorTest.hasRoll) {
//                    sensorBuilder.setHasRollAngle(sensoroSensorTest.hasRoll);
//                    if (sensoroSensorTest.roll.has_data) {
//
//                    }
////                    if (sensoroSensorTest.roll.has_alarmHigh) {
//                        sensorBuilder.setRollAngleAlarmHigh(rollAngleAlarmHigh);
////                    }
////                    if (sensoroSensorTest.roll.has_alarmLow) {
//                        sensorBuilder.setRollAngleAlarmLow(rollAngleAlarmLow);
////                    }
//                }
//                if (sensoroSensorTest.hasYaw) {
//                    sensorBuilder.setHasYawAngle(sensoroSensorTest.hasYaw);
//                    if (sensoroSensorTest.yaw.has_data) {
//
//                    }
////                    if (sensoroSensorTest.yaw.has_alarmHigh) {
//                        sensorBuilder.setYawAngleAlarmHigh(yawAngleAlarmHigh);
////                    }
////                    if (sensoroSensorTest.yaw.has_alarmLow) {
//                        sensorBuilder.setYawAngleAlarmLow(yawAngleAlarmLow);
////                    }
//                }
//                if (sensoroSensorTest.hasWaterPressure) {
//                    sensorBuilder.setHasWaterPressure(sensoroSensorTest.hasWaterPressure);
//                    if (sensoroSensorTest.waterPressure.has_data) {
//
//                    }
////                    if (sensoroSensorTest.waterPressure.has_alarmHigh) {
//                        sensorBuilder.setWaterPressureAlarmHigh(waterPressureAlarmHigh);
////                    }
////                    if (sensoroSensorTest.waterPressure.has_alarmLow) {
//                        sensorBuilder.setWaterPressureAlarmLow(waterPressureAlarmLow);
////                    }
//                }
//                boolean hasMultiTemperature = sensoroSensorTest.hasMultiTemp;
//                builder.setHasMultiTemperature(hasMultiTemperature);
//                if (hasMultiTemperature) {
//                    boolean hasAlarmHigh = sensoroSensorTest.multiTemperature.has_alarmHigh;
//                    builder.setHasAlarmHigh(hasAlarmHigh);
//                    if (hasAlarmHigh) {
//                        builder.setAlarmHigh(alarmHigh);
//                    }
//                    boolean hasAlarmLow = sensoroSensorTest.multiTemperature.has_alarmLow;
//                    builder.setHasAlarmLow(hasAlarmLow);
//                    if (hasAlarmLow) {
//                        builder.setAlarmLow(alarmLow);
//                    }
//                    boolean hasAlarmStepHigh = sensoroSensorTest.multiTemperature.has_alarmStepHigh;
//                    builder.setHasAlarmStepHigh(hasAlarmStepHigh);
//                    if (hasAlarmStepHigh) {
//                        builder.setAlarmStepHigh(alarmStepHigh);
//                    }
//                    boolean hasAlarmStepLow = sensoroSensorTest.multiTemperature.has_alarmStepLow;
//                    builder.setHasAlarmStepLow(hasAlarmStepLow);
//                    if (hasAlarmStepLow) {
//                        builder.setAlarmStepLow(alarmStepLow);
//                    }
//                }
//            }
            sensoroDevice.setBleOnTime(saveTurnOnTime);
            sensoroDevice.setBleOffTime(saveTurnOffTime);
//            builder.setBleTurnOnTime(saveTurnOnTime)
//                    .setBleTurnOffTime(saveTurnOffTime)
//                    .setBleInt(bleInt)
//                    .setBleTxp(bleTxp)
//                    .setLoraTxp(loraTxp)
//                    .setDevEui(sensoroDevice.getDevEui())
//                    .setAppEui(sensoroDevice.getAppEui())
//                    .setAppKey(sensoroDevice.getAppKey())
//                    .setAppSkey(sensoroDevice.getAppSkey())
//                    .setNwkSkey(sensoroDevice.getNwkSkey())
//                    .setDevAdr(sensoroDevice.getDevAdr())
//                    .setLoraDr(sensoroDevice.getLoraDr())
//                    .setLoraAdr(sensoroDevice.getLoraAdr());
            if (sensoroDevice.getPassword() == null || (!TextUtils.isEmpty(sensoroDevice.getPassword()))) {
//                builder.setPassword(sensoroDevice.getPassword());
            } else {
//                sensoroDevice.
            }
//            SensoroSensorConfiguration sensorConfiguration = sensorBuilder.build();
//            builder.setSensorConfiguration(sensorConfiguration);
//            deviceConfiguration = builder.build();
            sensoroDeviceConnection.writeData05Configuration(sensoroDevice, this);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    protected void doSmokeStart() {
        Toast.makeText(application, R.string.smoke_start_test, Toast.LENGTH_SHORT).show();
        MsgNode1V1M5.AppParam.Builder appParamBuilder = MsgNode1V1M5.AppParam.newBuilder();
        appParamBuilder.setSmokeCtrl(MsgNode1V1M5.SmokeCtrl.SMOKE_INSPECTION_TEST);
        sensoroDeviceConnection.writeSmokeCmd(appParamBuilder, this);
    }

    protected void doSmokeStop() {
        Toast.makeText(application, R.string.smoke_stop_test, Toast.LENGTH_SHORT).show();
        MsgNode1V1M5.AppParam.Builder appParamBuilder = MsgNode1V1M5.AppParam.newBuilder();
        appParamBuilder.setSmokeCtrl(MsgNode1V1M5.SmokeCtrl.SMOKE_INSPECTION_OVER);
        sensoroDeviceConnection.writeSmokeCmd(appParamBuilder, this);
    }

    protected void doSmokeSilence() {
        Toast.makeText(application, R.string.smoke_silence, Toast.LENGTH_SHORT).show();
        MsgNode1V1M5.AppParam.Builder appParamBuilder = MsgNode1V1M5.AppParam.newBuilder();
        appParamBuilder.setSmokeCtrl(MsgNode1V1M5.SmokeCtrl.SMOKE_ERASURE);
        sensoroDeviceConnection.writeSmokeCmd(appParamBuilder, this);
    }

    protected void doZeroCommand() {
        progressDialog.show();
        sensoroDeviceConnection.writeZeroCmd(this);
    }


    @Override
    public void onWriteSuccess(Object o, final int cmd) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (cmd) {
                    case CmdType.CMD_SET_SMOKE:
                    case CmdType.CMD_SET_ELEC_CMD:
                        break;
                    case CmdType.CMD_SET_ZERO:
                        Toast.makeText(SettingDeviceActivity.this, R.string.zero_calibrate_success, Toast
                                .LENGTH_SHORT).show();
                        break;
                    default:
                        try {
                            switch (sensoroDevice.getDataVersion()) {
                                case SensoroDeviceConnection.DATA_VERSION_05:
                                    postUpdateData05();
                                    break;
                                default:
                                    postUpdateData();
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(getApplicationContext(), getString(R.string.save_succ), Toast.LENGTH_SHORT)
                                .show();
                        SettingDeviceActivity.this.finish();
                        break;
                }
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onWriteFailure(final int errorCode, final int cmd) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (cmd) {
                    case CmdType.CMD_SET_SMOKE:
                    case CmdType.CMD_SET_ELEC_CMD:
                        break;
                    case CmdType.CMD_SET_ZERO:
                        Toast.makeText(SettingDeviceActivity.this, R.string.zero_calibrate_failed, Toast
                                .LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), getString(R.string.save_fail) + " 错误码" + errorCode,
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                progressDialog.dismiss();
            }
        });

    }

    @Override
    public void onConnectedSuccess(BLEDevice bleDevice, int cmd) {
        Log.v(TAG, "onConnectedSuccess");
        String sn = sensoroDevice.getSn();
        String firmwareVersion = sensoroDevice.getFirmwareVersion();
        sensoroDevice = (SensoroDevice) bleDevice;
        sensoroSensor = sensoroDevice.getSensoroSensorTest();
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
        Log.v(TAG, "onConnectedFailure:" + errorCode);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getString(R.string.connect_failed), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                SettingDeviceActivity.this.finish();
            }
        });
    }

    @Override
    public void onDisconnected() {
        Log.v(TAG, "onDisconnected");
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
                Toast.makeText(SettingDeviceActivity.this, R.string.tips_server_error, Toast.LENGTH_SHORT).show();
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
//        if (deviceConfiguration == null) {
//            return;
//        }
        if (sensoroDevice == null) {
            return;
        }
        String dataString = null;
        String version = null;
        MsgNode1V1M5.MsgNode.Builder msgCfgBuilder = MsgNode1V1M5.MsgNode.newBuilder();
        if (sensoroDevice.hasLoraParam()) {
            MsgNode1V1M5.LoraParam.Builder loraParamBuilder = MsgNode1V1M5.LoraParam.newBuilder();
            loraParamBuilder.setTxPower(sensoroDevice.getLoraTxp());
            if (sensoroDevice.hasDevEui()) {
                loraParamBuilder.setDevEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((sensoroDevice
                        .getDevEui()))));
            }
            if (sensoroDevice.hasAppEui()) {
                loraParamBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((sensoroDevice
                        .getAppEui()))));
            }
            if (sensoroDevice.hasAppKey()) {
                loraParamBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(sensoroDevice
                        .getAppKey())));
            }
            if (sensoroDevice.hasAppSkey()) {
                loraParamBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(sensoroDevice
                        .getAppSkey())));
            }
            if (sensoroDevice.hasNwkSkey()) {
                loraParamBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(sensoroDevice
                        .getNwkSkey())));
            }
            if (sensoroDevice.hasDevAddr()) {
                loraParamBuilder.setDevAddr(sensoroDevice.getDevAdr());
            }
            msgCfgBuilder.setLoraParam(loraParamBuilder);
        }
        if (sensoroDevice.hasBleParam()) {
            MsgNode1V1M5.BleParam.Builder bleParamBuilder = MsgNode1V1M5.BleParam.newBuilder();
            bleParamBuilder.setBleOnTime(sensoroDevice.getBleOnTime());
            bleParamBuilder.setBleOffTime(sensoroDevice.getBleOffTime());
            bleParamBuilder.setBleTxp(sensoroDevice.getBleTxp());
            bleParamBuilder.setBleInterval(sensoroDevice.getBleInt());
            msgCfgBuilder.setBleParam(bleParamBuilder);
        }
        if (sensoroDevice.hasAppParam()) {
            MsgNode1V1M5.AppParam.Builder appParamBuilder = MsgNode1V1M5.AppParam.newBuilder();
            if (sensoroDevice.hasUploadInterval()) {
                appParamBuilder.setUploadInterval(sensoroDevice.getUploadInterval());
                msgCfgBuilder.setAppParam(appParamBuilder);
            }
            if (sensoroDevice.hasConfirm()) {
                appParamBuilder.setConfirm(sensoroDevice.getConfirm());
                msgCfgBuilder.setAppParam(appParamBuilder);
            }
        }
        if (sensoroDevice.hasSensorParam()) {
            if (sensoroSensor.hasCo) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(sensoroSensor.co.alarmHigh_float);
                msgCfgBuilder.setCo(builder);
            }
            if (sensoroSensor.hasCo2) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(sensoroSensor.co2.alarmHigh_float);
                msgCfgBuilder.setCo2(builder);
            }
            if (sensoroSensor.hasNo2) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(sensoroSensor.no2.alarmHigh_float);
                msgCfgBuilder.setNo2(builder);
            }
            if (sensoroSensor.hasCh4) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(sensoroSensor.ch4.alarmHigh_float);
                msgCfgBuilder.setCh4(builder);
            }
            if (sensoroSensor.hasLpg) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(sensoroSensor.lpg.alarmHigh_float);
                msgCfgBuilder.setLpg(builder);
            }
            if (sensoroSensor.hasPm10) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(sensoroSensor.pm10.alarmHigh_float);
                msgCfgBuilder.setPm10(builder);
            }
            if (sensoroSensor.hasPm25) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                builder.setAlarmHigh(sensoroSensor.pm25.alarmHigh_float);
                msgCfgBuilder.setPm25(builder);
            }

            if (sensoroSensor.hasTemperature) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                if (sensoroSensor.temperature.has_data) {
                    builder.setData(sensoroSensor.temperature.data_float);
                }
                if (sensoroSensor.temperature.has_alarmHigh) {
                    builder.setAlarmHigh(sensoroSensor.temperature.alarmHigh_float);
                }
                if (sensoroSensor.temperature.has_alarmLow) {
                    builder.setAlarmLow(sensoroSensor.temperature.alarmLow_float);
                }
                msgCfgBuilder.setTemperature(builder);
            }
            if (sensoroSensor.hasHumidity) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                if (sensoroSensor.humidity.has_data) {
                    builder.setData(sensoroSensor.humidity.data_float);
                }
                if (sensoroSensor.humidity.has_alarmHigh) {
                    builder.setAlarmHigh(sensoroSensor.humidity.alarmHigh_float);
                }
                if (sensoroSensor.humidity.has_alarmLow) {
                    builder.setAlarmLow(sensoroSensor.humidity.alarmLow_float);
                }
                msgCfgBuilder.setHumidity(builder);
            }
            if (sensoroSensor.hasPitch) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                if (sensoroSensor.pitch.has_data) {
                    builder.setData(sensoroSensor.pitch.data_float);
                }
                if (sensoroSensor.pitch.has_alarmHigh) {
                    builder.setAlarmHigh(sensoroSensor.pitch.alarmHigh_float);
                }
                if (sensoroSensor.pitch.has_alarmLow) {
                    builder.setAlarmLow(sensoroSensor.pitch.alarmLow_float);
                }
                msgCfgBuilder.setPitch(builder);
            }
            if (sensoroSensor.hasRoll) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                if (sensoroSensor.roll.has_data) {
                    builder.setData(sensoroSensor.roll.data_float);
                }
                if (sensoroSensor.roll.has_alarmHigh) {
                    builder.setAlarmHigh(sensoroSensor.roll.alarmHigh_float);
                }
                if (sensoroSensor.roll.has_alarmLow) {
                    builder.setAlarmLow(sensoroSensor.roll.alarmLow_float);
                }
                msgCfgBuilder.setRoll(builder);
            }
            if (sensoroSensor.hasYaw) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                if (sensoroSensor.yaw.has_data) {
                    builder.setData(sensoroSensor.yaw.data_float);
                }
                if (sensoroSensor.yaw.has_alarmHigh) {
                    builder.setAlarmHigh(sensoroSensor.yaw.alarmHigh_float);
                }
                if (sensoroSensor.yaw.has_alarmLow) {
                    builder.setAlarmLow(sensoroSensor.yaw.alarmLow_float);
                }
                msgCfgBuilder.setYaw(builder);
            }
            if (sensoroSensor.hasWaterPressure) {
                MsgNode1V1M5.SensorData.Builder builder = MsgNode1V1M5.SensorData.newBuilder();
                if (sensoroSensor.waterPressure.has_data) {
                    builder.setData(sensoroSensor.waterPressure.data_float);
                }
                if (sensoroSensor.waterPressure.has_alarmHigh) {
                    builder.setAlarmHigh(sensoroSensor.waterPressure.alarmHigh_float);
                }
                if (sensoroSensor.waterPressure.has_alarmLow) {
                    builder.setAlarmLow(sensoroSensor.waterPressure.alarmLow_float);
                }
                msgCfgBuilder.setWaterPressure(builder);
            }
            //添加单通道温度传感器支持
            if (sensoroSensor.hasMultiTemp) {
                MsgNode1V1M5.MultiSensorDataInt.Builder builder = MsgNode1V1M5.MultiSensorDataInt.newBuilder();
                if (sensoroSensor.multiTemperature.has_alarmHigh) {
                    builder.setAlarmHigh(sensoroSensor.multiTemperature.alarmHigh_int);
                }
                if (sensoroSensor.multiTemperature.has_alarmLow) {
                    builder.setAlarmLow(sensoroSensor.multiTemperature.alarmLow_int);
                }
                if (sensoroSensor.multiTemperature.has_alarmStepHigh) {
                    builder.setAlarmStepHigh(sensoroSensor.multiTemperature.alarmStepHigh_int);
                }
                if (sensoroSensor.multiTemperature.has_alarmStepLow) {
                    builder.setAlarmStepLow(sensoroSensor.multiTemperature.alarmStepLow_int);
                }
                msgCfgBuilder.setMultiTemp(builder);
            }
            if (sensoroSensor.hasFireData) {
                MsgNode1V1M5.ElecFireData.Builder builder = MsgNode1V1M5.ElecFireData.newBuilder();
                if (sensoroSensor.elecFireData.hasSensorPwd) {
                    builder.setSensorPwd(sensoroSensor.elecFireData.sensorPwd);
                }
                if (sensoroSensor.elecFireData.hasLeakageTh) {
                    builder.setLeakageTh(sensoroSensor.elecFireData.leakageTh);
                }
                if (sensoroSensor.elecFireData.hasTempTh) {
                    builder.setTempTh(sensoroSensor.elecFireData.tempTh);
                }
                if (sensoroSensor.elecFireData.hasCurrentTh) {
                    builder.setCurrentTh(sensoroSensor.elecFireData.currentTh);
                }
                if (sensoroSensor.elecFireData.hasLoadTh) {
                    builder.setLoadTh(sensoroSensor.elecFireData.loadTh);
                }
                if (sensoroSensor.elecFireData.hasVolHighTh) {
                    builder.setVolHighTh(sensoroSensor.elecFireData.volHighTh);
                }
                if (sensoroSensor.elecFireData.hasVolLowTh) {
                    builder.setVolLowTh(sensoroSensor.elecFireData.volLowTh);
                }
                msgCfgBuilder.setFireData(builder);
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
                    Log.i("SettingDeviceActivity", "====>update failed");
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


    protected void postUpdateData() {
        if (deviceConfiguration == null) {
            return;
        }
        String dataString = null;
        String version = null;
        ProtoMsgCfgV1U1.MsgCfgV1u1.Builder msgCfgBuilder = ProtoMsgCfgV1U1.MsgCfgV1u1.newBuilder();

        msgCfgBuilder.setLoraInt(deviceConfiguration.getLoraInt().intValue());
        msgCfgBuilder.setLoraTxp(deviceConfiguration.getLoraTxp());
        msgCfgBuilder.setBleTxp(deviceConfiguration.getBleTxp());
        msgCfgBuilder.setBleInt(deviceConfiguration.getBleInt().intValue());
        msgCfgBuilder.setBleOnTime(deviceConfiguration.getBleTurnOnTime());
        msgCfgBuilder.setBleOffTime(deviceConfiguration.getBleTurnOffTime());
        if (deviceConfiguration.getDevEui() != null) {
            msgCfgBuilder.setDevEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getDevEui()
            ))));
        }
        if (deviceConfiguration.getAppEui() != null) {
            msgCfgBuilder.setAppEui(ByteString.copyFrom(SensoroUtils.HexString2Bytes((deviceConfiguration.getAppEui()
            ))));
        }
        if (deviceConfiguration.getAppKey() != null) {
            msgCfgBuilder.setAppKey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppKey())));
        }
        if (deviceConfiguration.getAppSkey() != null) {
            msgCfgBuilder.setAppSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getAppSkey
                    ())));
        }
        if (deviceConfiguration.getNwkSkey() != null) {
            msgCfgBuilder.setNwkSkey(ByteString.copyFrom(SensoroUtils.HexString2Bytes(deviceConfiguration.getNwkSkey
                    ())));
        }

        msgCfgBuilder.setDevAddr(deviceConfiguration.getDevAdr());
        msgCfgBuilder.setLoraAdr(deviceConfiguration.getLoraAdr());
        msgCfgBuilder.setLoraDr(deviceConfiguration.getLoraDr());

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

        String firmwareVersion = sensoroDevice.getFirmwareVersion();
        if (firmwareVersion.compareTo(SensoroDevice.FV_1_2) <= 0) {//03
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


    @Override
    public void onClick(View v) {
        DialogFragment dialogFragment = null;
        switch (v.getId()) {
            case R.id.settings_device_back:
                if (sensoroDeviceConnection != null) {
                    sensoroDeviceConnection.disconnect();
                    finish();
                }
                break;
            case R.id.settings_device_rl_uuid:
                dialogFragment = SettingsUUIDDialogFragment.newInstance(uuid);
                dialogFragment.show(getFragmentManager(), SETTINGS_UUID);
                break;
            case R.id.settings_device_rl_major:
                dialogFragment = SettingsMajorMinorDialogFragment.newInstance(sensoroDevice.getMajor());
                dialogFragment.show(getFragmentManager(), SETTINGS_MAJOR);
                break;
            case R.id.settings_device_rl_minor:
                dialogFragment = SettingsMajorMinorDialogFragment.newInstance(sensoroDevice.getMinor());
                dialogFragment.show(getFragmentManager(), SETTINGS_MINOR);
                break;
            case R.id.settings_device_rl_power:
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(blePowerItems, ParamUtil
                        .getBleTxpIndex(deviceType, sensoroDevice.getBleTxp()));
                dialogFragment.show(getFragmentManager(), SETTINGS_BLE_POWER);
                break;
            case R.id.settings_device_rl_adv_interval:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroDevice.getBleInt()));
                dialogFragment.show(getFragmentManager(), SETTINGS_ADV_INTERVAL);
                break;
            case R.id.settings_device_ll_turnon_time:
                int showTurnOnTime = sensoroDevice.getBleOnTime();
                if (sensoroDevice.getBleOnTime() >= 24) {
                    showTurnOnTime -= 24;
                }
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(bleTimeItems, showTurnOnTime);
                dialogFragment.show(getFragmentManager(), SETTINGS_BLE_TURNON_TIME);
                break;
            case R.id.settings_device_ll_turnoff_time:
                int showTurnOffTime = sensoroDevice.getBleOffTime();
                if (sensoroDevice.getBleOffTime() >= 24) {
                    showTurnOffTime -= 24;
                }
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(bleTimeItems, showTurnOffTime);
                dialogFragment.show(getFragmentManager(), SETTINGS_BLE_TURNOFF_TIME);
                break;
            case R.id.settings_device_rl_lora_txp:
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(loraTxpItems, ParamUtil
                        .getLoraTxpIndex(band, sensoroDevice.getLoraTxp()));
                dialogFragment.show(getFragmentManager(), SETTINGS_LORA_TXP);
                break;
            case R.id.settings_device_rl_lora_ad_interval:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroDevice.getLoraInt()));
                dialogFragment.show(getFragmentManager(), SETTINGS_LORA_INT);
                break;
            case R.id.settings_device_rl_lora_eirp:
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(loraEirpItems, sensoroDevice
                        .getLoraTxp());
                dialogFragment.show(getFragmentManager(), SETTINGS_LORA_EIRP);
                break;
            case R.id.settings_device_rl_slot1_item1:
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(slotItems, slotItemSelectIndex[0]);
                dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE1);
                break;
            case R.id.settings_device_rl_slot1_item2:
                showSlot1Dialog(dialogFragment);
                break;
            case R.id.settings_device_rl_slot2_item1:
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(slotItems, slotItemSelectIndex[1]);
                dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE2);
                break;
            case R.id.settings_device_rl_slot2_item2:
                showSlot2Dialog(dialogFragment);
                break;
            case R.id.settings_device_rl_slot3_item1:
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(slotItems, slotItemSelectIndex[2]);
                dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE3);
                break;
            case R.id.settings_device_rl_slot3_item2:
                showSlot3Dialog(dialogFragment);
                break;
            case R.id.settings_device_rl_slot4_item1:
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(slotItems, slotItemSelectIndex[3]);
                dialogFragment.show(getFragmentManager(), SETTINGS_EDDYSTONE4);
                break;
            case R.id.settings_device_rl_slot4_item2:
                showSlot4Dialog(dialogFragment);
                break;

            case R.id.settings_device_rl_custom_package1:
                dialogFragment = SettingsInputDialogFragment.newInstance(custom_package1);
                dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE1);
                break;
            case R.id.settings_device_rl_custom_package2:
                dialogFragment = SettingsInputDialogFragment.newInstance(custom_package2);
                dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE2);
                break;
            case R.id.settings_device_rl_custom_package3:
                dialogFragment = SettingsInputDialogFragment.newInstance(custom_package3);
                dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE3);
                break;
            case R.id.settings_device_ll_co:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.co
                        .alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_CO);
                break;
            case R.id.settings_device_ll_co2:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor
                        .co2.alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_CO2);
                break;
            case R.id.settings_device_ll_ch4:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor
                        .ch4.alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_CH4);
                break;
            case R.id.settings_device_ll_no2:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor
                        .no2.alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_NO2);
                break;
            case R.id.settings_device_ll_lpg:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.lpg
                        .alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_LPG);
                break;
            case R.id.settings_device_ll_pm10:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor
                        .pm10.alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_PM10);
                break;
            case R.id.settings_device_ll_pm25:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor
                        .pm25.alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_PM25);
                break;
            case R.id.settings_device_rl_temp_upper:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.temperature
                        .alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_TEMP_UPPER);
                break;
            case R.id.settings_device_rl_temp_lower:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.temperature
                        .alarmLow_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_TEMP_LOWER);
                break;
            case R.id.settings_device_rl_humidity_upper:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.humidity
                        .alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_HUMIDITY_UPPER);
                break;
            case R.id.settings_device_rl_humidity_lower:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.humidity
                        .alarmLow_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_HUMIDITY_LOWER);
                break;
            case R.id.settings_device_rl_pitch_angle_upper:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.pitch
                        .alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_PITCH_ANGLE_UPPER);
                break;
            case R.id.settings_device_rl_pitch_angle_lower:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.pitch
                        .alarmLow_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_PITCH_ANGLE_LOWER);
                break;
            case R.id.settings_device_rl_roll_angle_upper:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.roll
                        .alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_ROLL_ANGLE_UPPER);
                break;
            case R.id.settings_device_rl_roll_angle_lower:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.roll
                        .alarmLow_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_ROLL_ANGLE_LOWER);
                break;
            case R.id.settings_device_rl_yaw_angle_upper:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.yaw
                        .alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_YAW_ANGLE_UPPER);
                break;
            case R.id.settings_device_rl_yaw_angle_lower:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.yaw
                        .alarmLow_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_YAW_ANGLE_LOWER);
                break;
            case R.id.settings_device_rl_water_pressure_upper:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.waterPressure
                        .alarmHigh_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_WATER_PRESSURE_UPPER);
                break;
            case R.id.settings_device_rl_water_pressure_lower:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor.waterPressure
                        .alarmLow_float));
                dialogFragment.show(getFragmentManager(), SETTINGS_SENSOR_WATER_PRESSURE_LOWER);
                break;
            case R.id.settings_device_rl_app_param_upload:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroDevice
                        .getUploadInterval()));
                dialogFragment.show(getFragmentManager(), SETTINGS_APP_PARAM_UPLOAD);
                break;
            case R.id.settings_device_rl_app_param_confirm:
                String statusArray[] = this.getResources().getStringArray(R.array.status_array);
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(statusArray, sensoroDevice.getConfirm
                        () == 0 ? 1 : 0);
                dialogFragment.show(getFragmentManager(), SETTINGS_APP_PARAM_CONFIRM);
                break;
            case R.id.settings_device_tv_smoke_start:
                doSmokeStart();
                break;
            case R.id.settings_device_tv_smoke_stop:
                doSmokeStop();
                break;
            case R.id.settings_device_tv_smoke_silence:
                doSmokeSilence();
                break;
            case R.id.settings_device_tv_angle_zero:
                doZeroCommand();
                break;
            case R.id.settings_device_tv_save:
                saveConfiguration();
                break;
            //TODO 单通道温度传感器
            case R.id.settings_device_rl_temperature_pressure_upper:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor
                        .multiTemperature.alarmHigh_int / 100f));
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_TEMPERATURE_PRESSURE_UPPER);
                break;
            case R.id.settings_device_rl_temperature_pressure_lower:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor
                        .multiTemperature.alarmLow_int / 100f));
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_TEMPERATURE_PRESSURE_LOWER);
                break;
            case R.id.settings_device_rl_temperature_pressure_step_upper:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor
                        .multiTemperature.alarmStepHigh_int / 100f));
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_TEMPERATURE_PRESSURE_STEP_UPPER);
                break;
            case R.id.settings_device_rl_temperature_pressure_step_lower:
                dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(sensoroSensor
                        .multiTemperature.alarmStepLow_int / 100f));
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_TEMPERATURE_PRESSURE_STEP_LOWER);
                break;
            //TODO 电表
            case R.id.settings_device_rl_fhsj_elec_pwd:
                int sensorPwd = sensoroSensor.elecFireData.sensorPwd;
                dialogFragment = SettingsInputDialogFragment.newInstance(sensorPwd + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_FHSJ_ELEC_PWD);
                break;
            case R.id.settings_device_rl_fhsj_elec_leak:
                int leakageTh = sensoroSensor.elecFireData.leakageTh;
                dialogFragment = SettingsInputDialogFragment.newInstance(leakageTh + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_FHSJ_ELEC_LEAK);
                break;
            case R.id.settings_device_rl_fhsj_elec_temp:
                int tempTh = sensoroSensor.elecFireData.tempTh;
                dialogFragment = SettingsInputDialogFragment.newInstance(tempTh + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_FHSJ_ELEC_TEMP);
                break;
            case R.id.settings_device_rl_fhsj_elec_current:
                int currentTh = sensoroSensor.elecFireData.currentTh;
                dialogFragment = SettingsInputDialogFragment.newInstance(currentTh + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_FHSJ_ELEC_CURRENT);
                break;
            case R.id.settings_device_rl_fhsj_elec_overload:
                int loadTh = sensoroSensor.elecFireData.loadTh;
                dialogFragment = SettingsInputDialogFragment.newInstance(loadTh + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_FHSJ_ELEC_OVERLOAD);
                break;
            case R.id.settings_device_rl_fhsj_elec_overpressure:
                int volHighTh = sensoroSensor.elecFireData.volHighTh;
                dialogFragment = SettingsInputDialogFragment.newInstance(volHighTh + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_FHSJ_ELEC_OVERPRESSURE);
                break;
            case R.id.settings_device_rl_fhsj_elec_undervoltage:
                int volLowTh = sensoroSensor.elecFireData.volLowTh;
                dialogFragment = SettingsInputDialogFragment.newInstance(volLowTh + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_FHSJ_ELEC_UNDERVOLTAGE);
                break;
            case R.id.settings_device_rl_fhsj_elec_control_reset:
                doElecControl(CMD_ELEC_RESET);
                break;
            case R.id.settings_device_rl_fhsj_elec_control_restore:
                doElecControl(CMD_ELEC_RESTORE);
                break;
            case R.id.settings_device_rl_fhsj_elec_control_air_switch:
                doElecControl(CMD_ELEC_AIR_SWITCH);
                break;
            case R.id.settings_device_rl_fhsj_elec_control_self_test:
                doElecControl(CMD_ELEC_SELF_TEST);
                break;
            case R.id.settings_device_rl_fhsj_elec_control_silence:
                doElecControl(CMD_ELEC_SILENCE);
                break;
            case R.id.settings_device_rl_fhsj_elec_control_zero_power:
                doElecControl(CMD_ELEC_ZERO_POWER);
                break;


            default:
                break;
        }
    }

    /**
     * 电表系统复位
     */
    protected void doElecControl(int cmd) {
        switch (cmd) {
            case CMD_ELEC_RESET:
                Toast.makeText(application, "系统复位", Toast.LENGTH_SHORT).show();
                break;
            case CMD_ELEC_RESTORE:
                Toast.makeText(application, "回复出厂", Toast.LENGTH_SHORT).show();
                break;
            case CMD_ELEC_AIR_SWITCH:
                Toast.makeText(application, "断开空气开关", Toast.LENGTH_SHORT).show();
                break;
            case CMD_ELEC_SELF_TEST:
                Toast.makeText(application, "自检", Toast.LENGTH_SHORT).show();
                break;
            case CMD_ELEC_SILENCE:
                Toast.makeText(application, "消音", Toast.LENGTH_SHORT).show();
                break;
            case CMD_ELEC_ZERO_POWER:
                Toast.makeText(application, "电量清零", Toast.LENGTH_SHORT).show();
                break;
        }
        MsgNode1V1M5.ElecFireData.Builder builder = MsgNode1V1M5.ElecFireData.newBuilder();
        builder.setCmd(cmd);
        sensoroDeviceConnection.writeElecCmd(builder, this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.settings_device_sc_ibeacon:
                if (isChecked) {
                    isIBeaconEnabled = true;
                } else {
                    isIBeaconEnabled = false;
                }
                break;
            case R.id.settings_device_custom_package1_state:
                if (isChecked) {
                    isCustomPackage1Enabled = true;
                    customPackage1RelativeLayout.setAlpha(1.0f);
                    customPackage1RelativeLayout.setEnabled(true);
                } else {
                    isCustomPackage1Enabled = false;
                    customPackage1RelativeLayout.setAlpha(0.5f);
                    customPackage1RelativeLayout.setEnabled(false);
                }
                break;
            case R.id.settings_device_custom_package2_state:
                if (isChecked) {
                    isCustomPackage2Enabled = true;
                    customPackage2RelativeLayout.setAlpha(1f);
                    customPackage2RelativeLayout.setEnabled(true);
                } else {
                    isCustomPackage2Enabled = false;
                    customPackage2RelativeLayout.setAlpha(0.5f);
                    customPackage2RelativeLayout.setEnabled(false);
                }
                break;
            case R.id.settings_device_custom_package3_state:
                if (isChecked) {
                    isCustomPackage3Enabled = true;
                    customPackage3RelativeLayout.setAlpha(1f);
                    customPackage3RelativeLayout.setEnabled(true);
                } else {
                    isCustomPackage3Enabled = false;
                    customPackage3RelativeLayout.setAlpha(0.5f);
                    customPackage3RelativeLayout.setEnabled(false);
                }
                break;
            case R.id.settings_device_sensor_adv_status:
                if (isChecked) {
                    isSensorBroadcastEnabled = true;
                } else {
                    isSensorBroadcastEnabled = false;
                }
                break;
            default:
                break;
        }
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
                eddyStoneSlot1Item2Layout.setVisibility(View.VISIBLE);
                break;
            case STATUS_SLOT_URL:
                eddyStoneSlot1Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot1Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot1Item2.setText(this.getString(R.string.slot6_name));
                eddyStoneSlot1Item2Value.setText(this.getString(R.string.text_null));
                eddyStoneSlot1Item2Layout.setVisibility(View.VISIBLE);
                break;
            case STATUS_SLOT_EID:
                eddyStoneSlot1Item2Layout.setVisibility(View.VISIBLE);
                eddyStoneSlot1Iv2.setVisibility(View.VISIBLE);
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
                eddyStoneSlot2Item2Layout.setVisibility(View.VISIBLE);
                break;
            case STATUS_SLOT_URL:
                eddyStoneSlot2Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot2Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot2Item2.setText(this.getString(R.string.slot6_name));
                eddyStoneSlot2Item2Value.setText(this.getString(R.string.text_null));
                eddyStoneSlot2Item2Layout.setVisibility(View.VISIBLE);
                break;
            case STATUS_SLOT_EID:
                eddyStoneSlot2Item2Layout.setVisibility(View.VISIBLE);
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
                eddyStoneSlot3Item2Layout.setVisibility(View.VISIBLE);
                break;
            case STATUS_SLOT_URL:
                eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot3Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot3Item2.setText(this.getString(R.string.slot6_name));
                eddyStoneSlot3Item2Layout.setVisibility(View.VISIBLE);
                break;
            case STATUS_SLOT_EID:
                eddyStoneSlot3Item2Layout.setVisibility(View.VISIBLE);
                eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot3Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot3Item2.setText(this.getString(R.string.eid_list));
                eddyStoneSlot3Iv2.setVisibility(View.VISIBLE);
                break;
            case STATUS_SLOT_TLM:
                eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot3Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
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
                eddyStoneSlot4Item2Layout.setVisibility(View.VISIBLE);
                break;
            case STATUS_SLOT_URL:
                eddyStoneSlot4Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot4Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot4Item2.setText(this.getString(R.string.slot6_name));
                eddyStoneSlot4Item2Layout.setVisibility(View.VISIBLE);
                break;
            case STATUS_SLOT_EID:
                eddyStoneSlot4Item2Layout.setVisibility(View.VISIBLE);
                eddyStoneSlot3Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot4Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot4Item2.setText(this.getString(R.string.eid_list));
                eddyStoneSlot4Item2Value.setText(this.getString(R.string.text_null));
                eddyStoneSlot4Iv2.setVisibility(View.VISIBLE);
                break;
            case STATUS_SLOT_TLM:
                eddyStoneSlot4Item1.setText(this.getString(R.string.slot1_name));
                eddyStoneSlot4Item1Value.setText(this.getResources().getStringArray(R.array.eddystone_slot_array)
                        [index]);
                eddyStoneSlot4Item2Layout.setVisibility(GONE);
                eddyStoneSlot4Iv2.setVisibility(GONE);

                break;
        }
    }

    @Override
    public void onPositiveButtonClick(String tag, Bundle bundle) {
        if (tag.equals(SETTINGS_UUID)) {
            uuid = bundle.getString(SettingsUUIDDialogFragment.UUID);
            uuidTextView.setText(uuid);
        } else if (tag.equals(SETTINGS_MAJOR)) {
            int major = bundle.getInt(SettingsMajorMinorDialogFragment.VALUE);
            sensoroDevice.setMajor(major);
            majorTextView.setText(String.format("0x%04X", major));
        } else if (tag.equals(SETTINGS_MINOR)) {
            int minor = bundle.getInt(SettingsMajorMinorDialogFragment.VALUE);
            sensoroDevice.setMinor(minor);
            minorTextView.setText(String.format("0x%04X", minor));
        } else if (tag.equals(SETTINGS_BLE_POWER)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            int bleTxp = ParamUtil.getBleTxp(deviceType, index);
            sensoroDevice.setBleTxp(bleTxp);
            powerTextView.setText(blePowerItems[index]);
        } else if (tag.equals(SETTINGS_ADV_INTERVAL)) {
            String bleInt = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroDevice.setBleInt(Double.valueOf(bleInt).intValue());
            advIntervalTextView.setText(bleInt + " ms");
        } else if (tag.equals(SETTINGS_BLE_TURNON_TIME)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            sensoroDevice.setBleOnTime(index);
            turnOnTexView.setText(bleTimeItems[index]);
        } else if (tag.equals(SETTINGS_BLE_TURNOFF_TIME)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            sensoroDevice.setBleOffTime(index);
            turnOffTextView.setText(bleTimeItems[index]);
        } else if (tag.equals(SETTINGS_LORA_TXP)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            sensoroDevice.setLoraTxp(ParamUtil.getLoraTxp(band, index));
            loraTxpTextView.setText(loraTxpItems[index]);
        } else if (tag.equals(SETTINGS_LORA_EIRP)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            sensoroDevice.setLoraTxp(index);
            loraEirpTextView.setText(loraEirpValues[index]);
        } else if (tag.equals(SETTINGS_LORA_INT)) {
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
            Float loraInt = Float.valueOf(text);
            sensoroDevice.setLoraInt(loraInt);
            loraAdIntervalTextView.setText(loraInt + "s");
        } else if (tag.equals(SETTINGS_EDDYSTONE1)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            setSettingsEddystone1(index);
        } else if (tag.equals(SETTINGS_EDDYSTONE2)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            setSettingsEddystone2(index);
        } else if (tag.equals(SETTINGS_EDDYSTONE3)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            setSettingsEddystone3(index);
        } else if (tag.equals(SETTINGS_EDDYSTONE4)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
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
        } else if (tag.equals(SETTINGS_CUSTOM_PACKAGE1)) {
            String ctp1 = bundle.getString(SettingsInputDialogFragment.INPUT);
            String regString = "[a-f0-9A-F]{1,56}";
            if (!Pattern.matches(regString, ctp1)) {
                SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(custom_package1);
                dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE1);
                Toast.makeText(this, R.string.invaild_custom, Toast.LENGTH_SHORT).show();
            } else {
                custom_package1 = ctp1;
                customPackage1TextView.setText(custom_package1);
            }
        } else if (tag.equals(SETTINGS_CUSTOM_PACKAGE2)) {
            String ctp2 = bundle.getString(SettingsInputDialogFragment.INPUT);
            String regString = "[a-f0-9A-F]{1,56}";
            if (!Pattern.matches(regString, ctp2)) {
                SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(custom_package2);
                dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE2);
                Toast.makeText(this, R.string.invaild_custom, Toast.LENGTH_SHORT).show();
            } else {
                custom_package2 = ctp2;
                customPackage2TextView.setText(custom_package2);
            }
        } else if (tag.equals(SETTINGS_CUSTOM_PACKAGE3)) {
            String ctp3 = bundle.getString(SettingsInputDialogFragment.INPUT);
            String regString = "[a-f0-9A-F]{1,56}";
            if (!Pattern.matches(regString, ctp3)) {
                SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(custom_package3);
                dialogFragment.show(getFragmentManager(), SETTINGS_CUSTOM_PACKAGE3);
                Toast.makeText(this, R.string.invaild_custom, Toast.LENGTH_SHORT).show();
            } else {
                custom_package3 = ctp3;
                customPackage3TextView.setText(custom_package3);
            }
        } else if (tag.equals(SETTINGS_SENSOR_CO)) {
            String co = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.co.alarmHigh_float = Float.valueOf(co);
            coTextView.setText(co + "");
        } else if (tag.equals(SETTINGS_SENSOR_CO2)) {
            String co2 = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.co2.alarmHigh_float = Float.valueOf(co2);
            co2TextView.setText(co2 + "");
        } else if (tag.equals(SETTINGS_SENSOR_NO2)) {
            String no2 = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.no2.alarmHigh_float = Float.valueOf(no2);
            no2TextView.setText(no2 + "");
        } else if (tag.equals(SETTINGS_SENSOR_CH4)) {
            String ch4 = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.ch4.alarmHigh_float = Float.valueOf(ch4);
            ch4TextView.setText(ch4 + "");
        } else if (tag.equals(SETTINGS_SENSOR_LPG)) {
            String lpg = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.lpg.alarmHigh_float = Float.valueOf(lpg);
            lpgTextView.setText(lpg + " ");
        } else if (tag.equals(SETTINGS_SENSOR_PM25)) {
            String pm25 = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.pm25.alarmHigh_float = Float.valueOf(pm25);
            pm25TextView.setText(pm25 + "");
        } else if (tag.equals(SETTINGS_SENSOR_PM10)) {
            String pm10 = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.pm10.alarmHigh_float = Float.valueOf(pm10);
            pm10TextView.setText(pm10 + "");
        } else if (tag.equals(SETTINGS_SENSOR_TEMP_UPPER)) {
            String tempHigh = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.temperature.alarmHigh_float = Float.valueOf(tempHigh);
            tempUpperTextView.setText(tempHigh + "");
        } else if (tag.equals(SETTINGS_SENSOR_TEMP_LOWER)) {
            String tempLow = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.temperature.alarmLow_float = Float.valueOf(tempLow);
            tempLowerTextView.setText(tempLow + "");
        } else if (tag.equals(SETTINGS_SENSOR_HUMIDITY_UPPER)) {
            String humidityHigh = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.humidity.alarmHigh_float = Float.valueOf(humidityHigh);
            humidityUpperTextView.setText(humidityHigh + "");
        } else if (tag.equals(SETTINGS_SENSOR_HUMIDITY_LOWER)) {
            String humidityLow = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.humidity.alarmLow_float = Float.valueOf(humidityLow);
            humidityLowerTextView.setText(humidityLow + "");
        } else if (tag.equals(SETTINGS_SENSOR_PITCH_ANGLE_UPPER)) {
            String pitchHigh = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.pitch.alarmHigh_float = Float.valueOf(pitchHigh);
            pitchAngleUpperTextView.setText(pitchHigh + "");
        } else if (tag.equals(SETTINGS_SENSOR_PITCH_ANGLE_LOWER)) {
            String pitchLow = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.pitch.alarmLow_float = Float.valueOf(pitchLow);
            pitchAngleLowerTextView.setText(pitchLow + "");
        } else if (tag.equals(SETTINGS_SENSOR_ROLL_ANGLE_UPPER)) {
            String rollHigh = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.roll.alarmHigh_float = Float.valueOf(rollHigh);
            rollAngleUpperTextView.setText(rollHigh + "");
        } else if (tag.equals(SETTINGS_SENSOR_ROLL_ANGLE_LOWER)) {
            String rollLower = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.roll.alarmLow_float = Float.valueOf(rollLower);
            rollAngleLowerTextView.setText(rollLower + "");
        } else if (tag.equals(SETTINGS_SENSOR_YAW_ANGLE_UPPER)) {
            String yawHigh = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.yaw.alarmHigh_float = Float.valueOf(yawHigh);
            yawAngleUpperTextView.setText(yawHigh + "");
        } else if (tag.equals(SETTINGS_SENSOR_YAW_ANGLE_LOWER)) {
            String yawLower = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.yaw.alarmLow_float = Float.valueOf(yawLower);
            yawAngleLowerTextView.setText(yawLower + "");
        } else if (tag.equals(SETTINGS_SENSOR_WATER_PRESSURE_UPPER)) {
            String waterHigh = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.waterPressure.alarmHigh_float = Float.valueOf(waterHigh);
            waterPressureUpperTextView.setText(waterHigh + "");
        } else if (tag.equals(SETTINGS_SENSOR_WATER_PRESSURE_LOWER)) {
            String waterLower = bundle.getString(SettingsInputDialogFragment.INPUT);
            sensoroSensor.waterPressure.alarmLow_float = Float.valueOf(waterLower);
            waterPressureLowerTextView.setText(waterLower + "");
        } else if (tag.equals(SETTINGS_APP_PARAM_UPLOAD)) {
            String upload = bundle.getString(SettingsInputDialogFragment.INPUT);
            Integer uploadInterval = Integer.valueOf(upload);
            sensoroDevice.setUploadInterval(uploadInterval);
            uploadIntervalTextView.setText(uploadInterval + "s");
        } else if (tag.equals(SETTINGS_APP_PARAM_CONFIRM)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            int appParamConfirm;
            if (index == 0) {
                appParamConfirm = 1;
            } else {
                appParamConfirm = 0;
            }
            sensoroDevice.setConfirm(appParamConfirm);
            confirmTextView.setText(getResources().getStringArray(R.array.status_array)[index]);
        } else if (SETTINGS_DEVICE_TEMPERATURE_PRESSURE_UPPER.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                float f = Float.parseFloat(temp);
                sensoroSensor.multiTemperature.alarmHigh_int = (int) (f * 100);
                settingsDeviceTvTemperaturePressureUpperLimit.setText(f + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_TEMPERATURE_PRESSURE_LOWER.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                float f = Float.parseFloat(temp);
                sensoroSensor.multiTemperature.alarmLow_int = (int) (f * 100);
                settingsDeviceTvTemperaturePressureLowerLimit.setText(f + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }

        } else if (SETTINGS_DEVICE_TEMPERATURE_PRESSURE_STEP_UPPER.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                float f = Float.parseFloat(temp);
                sensoroSensor.multiTemperature.alarmStepHigh_int = (int) (f * 100);
                settingsDeviceTvTemperaturePressureUpperStepLimit.setText(f + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }

        } else if (SETTINGS_DEVICE_TEMPERATURE_PRESSURE_STEP_LOWER.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                float f = Float.parseFloat(temp);
                sensoroSensor.multiTemperature.alarmStepLow_int = (int) (f * 100);
                settingsDeviceTvTemperaturePressureLowerStepLimit.setText(f + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }

        } else if (SETTINGS_DEVICE_RL_FHSJ_ELEC_PWD.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i > 9999 || i < 0) {
                    Toast.makeText(this, "密码设定为0-9999", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.elecFireData.sensorPwd = i;
                settingsDeviceTvFhsjElecPwd.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }

        } else if (SETTINGS_DEVICE_RL_FHSJ_ELEC_LEAK.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i <= 0 || i > 1000) {
                    Toast.makeText(this, "漏电阈值范围1-1000", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.elecFireData.leakageTh = i;
                settingsDeviceTvFhsjElecLeak.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }

        } else if (SETTINGS_DEVICE_RL_FHSJ_ELEC_TEMP.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 45 || i > 145) {
                    Toast.makeText(this, "温度阈值分为45-145", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.elecFireData.tempTh = i;
                settingsDeviceTvFhsjElecTemp.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }

        } else if (SETTINGS_DEVICE_RL_FHSJ_ELEC_CURRENT.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i <= 0 || i > 50) {
                    Toast.makeText(this, "电流阈值分为1-50", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.elecFireData.currentTh = i;
                settingsDeviceTvFhsjElecCurrent.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }

        } else if (SETTINGS_DEVICE_RL_FHSJ_ELEC_OVERLOAD.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i <= 0 || i > 11) {
                    Toast.makeText(this, "过载阈值分为1-11", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.elecFireData.loadTh = i;
                settingsDeviceTvFhsjElecOverload.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }

        } else if (SETTINGS_DEVICE_RL_FHSJ_ELEC_OVERPRESSURE.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 220 || i > 264) {
                    Toast.makeText(this, "过压阈值分为220-264", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.elecFireData.volHighTh = i;
                settingsDeviceTvFhsjElecOverpressure.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }

        } else if (SETTINGS_DEVICE_RL_FHSJ_ELEC_UNDERVOLTAGE.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 176 || i > 219) {
                    Toast.makeText(this, "欠压阈值分为176-219", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.elecFireData.volLowTh = i;
                settingsDeviceTvFhsjElecUndervoltage.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (sensoroDeviceConnection != null) {
            sensoroDeviceConnection.disconnect();
        }
    }
}
