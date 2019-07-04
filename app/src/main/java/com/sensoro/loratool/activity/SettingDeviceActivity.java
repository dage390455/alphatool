package com.sensoro.loratool.activity;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnDismissListener;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.sensoro.libbleserver.ble.callback.SensoroConnectionCallback;
import com.sensoro.libbleserver.ble.callback.SensoroWriteCallback;
import com.sensoro.libbleserver.ble.connection.SensoroDeviceConfiguration;
import com.sensoro.libbleserver.ble.connection.SensoroDeviceConnection;
import com.sensoro.libbleserver.ble.constants.CmdType;
import com.sensoro.libbleserver.ble.entity.BLEDevice;
import com.sensoro.libbleserver.ble.entity.SensoroDevice;
import com.sensoro.libbleserver.ble.entity.SensoroMantunData;
import com.sensoro.libbleserver.ble.entity.SensoroSensor;
import com.sensoro.libbleserver.ble.entity.SensoroSlot;
import com.sensoro.libbleserver.ble.proto.MsgNode1V1M5;
import com.sensoro.libbleserver.ble.proto.ProtoMsgCfgV1U1;
import com.sensoro.libbleserver.ble.proto.ProtoStd1U1;
import com.sensoro.libbleserver.ble.scanner.SensoroUUID;
import com.sensoro.libbleserver.ble.utils.SensoroUtils;
import com.sensoro.lora.setting.server.bean.EidInfo;
import com.sensoro.lora.setting.server.bean.EidInfoListRsp;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.DeviceAdapter;
import com.sensoro.loratool.adapter.RecyclerItemClickListener;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.event.OnPositiveButtonClickListener;
import com.sensoro.loratool.fragment.SettingsInputDialogFragment;
import com.sensoro.loratool.fragment.SettingsMajorMinorDialogFragment;
import com.sensoro.loratool.fragment.SettingsMultiChoiceItemsDialogFragment;
import com.sensoro.loratool.fragment.SettingsSingleChoiceItemsFragment;
import com.sensoro.loratool.fragment.SettingsUUIDDialogFragment;
import com.sensoro.loratool.model.ChannelData;
import com.sensoro.loratool.model.SettingDeviceModel;
import com.sensoro.loratool.store.DeviceDataDao;
import com.sensoro.loratool.utils.ParamUtil;
import com.sensoro.loratool.utils.Utils;
import com.sensoro.loratool.widget.AlphaToast;
import com.sensoro.loratool.widget.SettingEnterDialogUtils;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.sensoro.libbleserver.ble.constants.CmdType.CMD_ELEC_AIR_SWITCH;
import static com.sensoro.libbleserver.ble.constants.CmdType.CMD_ELEC_RESET;
import static com.sensoro.libbleserver.ble.constants.CmdType.CMD_ELEC_RESTORE;
import static com.sensoro.libbleserver.ble.constants.CmdType.CMD_ELEC_SELF_TEST;
import static com.sensoro.libbleserver.ble.constants.CmdType.CMD_ELEC_SILENCE;
import static com.sensoro.libbleserver.ble.constants.CmdType.CMD_ELEC_ZERO_POWER;


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
    @BindView(R.id.settings_device_rl_mrssi)
    RelativeLayout mrssiRelativeLayout;
    @BindView(R.id.settings_device_tv_mrssi)
    TextView mrssiTextView;

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
    @BindView(R.id.settings_device_rl_app_demo)
    RelativeLayout demoLayout;
    @BindView(R.id.settings_device_tv_demo)
    TextView demoTextView;
    @BindView(R.id.settings_device_rl_app_battery_beep)
    RelativeLayout batteryBeepLayout;
    @BindView(R.id.settings_device_tv_app_battery_beep)
    TextView batteryBeepTextView;
    @BindView(R.id.settings_device_rl_app_beep_mute_time)
    RelativeLayout beepMuteTimeLayout;
    @BindView(R.id.settings_device_rl_app_led_status)
    RelativeLayout ledStatusLayout;
    @BindView(R.id.settings_device_tv_app_led_status)
    TextView ledStatusTextView;
    @BindView(R.id.view_cayman_value_of_batb)
    View viewCaymanValueOfBatb;
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
    /**
     * 曼顿电气火灾器
     */
//    @BindView(R.id.iv_mantun_leak)
//    ImageView ivMantunLeak;
//    @BindView(R.id.settings_device_mantun_leak)
//    TextView settingsDeviceMantunLeak;
//    @BindView(R.id.settings_device_rl_mantun_leak)
//    RelativeLayout settingsDeviceRlMantunLeak;
//    @BindView(R.id.iv_maunton_temp)
//    ImageView ivMauntonTemp;
//    @BindView(R.id.settings_device_tv_maunton_temp)
//    TextView settingsDeviceTvMauntonTemp;
//    @BindView(R.id.settings_device_rl_mauton_temp)
//    RelativeLayout settingsDeviceRlMautonTemp;
//    @BindView(R.id.iv_maunton_current)
//    ImageView ivMauntonCurrent;
//    @BindView(R.id.settings_device_tv_maunton_current)
//    TextView settingsDeviceTvMauntonCurrent;
//    @BindView(R.id.settings_device_rl_maunton_current)
//    RelativeLayout settingsDeviceRlMauntonCurrent;
//    @BindView(R.id.iv_maunton_overpressure)
//    ImageView ivMauntonOverpressure;
//    @BindView(R.id.settings_device_tv_maunton_overpressure)
//    TextView settingsDeviceTvMauntonOverpressure;
//    @BindView(R.id.settings_device_rl_maunton_overpressure)
//    RelativeLayout settingsDeviceRlMauntonOverpressure;
//    @BindView(R.id.iv_maunton_undervoltage)
//    ImageView ivMauntonUndervoltage;
//    @BindView(R.id.settings_device_tv_maunton_undervoltage)
//    TextView settingsDeviceTvMauntonUndervoltage;
//    @BindView(R.id.settings_device_rl_maunton_undervoltage)
//    RelativeLayout settingsDeviceRlMauntonUndervoltage;
//    @BindView(R.id.iv_maunton_overload)
//    ImageView ivMauntonOverload;
//    @BindView(R.id.settings_device_tv_maunton_overload)
//    TextView settingsDeviceTvMauntonOverload;
//    @BindView(R.id.settings_device_rl_maunton_overload)
//    RelativeLayout settingsDeviceRlMauntonOverload;
//    @BindView(R.id.iv_maunton_mantun_out_side)
//    ImageView ivMauntonMantunOutSide;
//    @BindView(R.id.settings_device_tv_mantun_out_side)
//    TextView settingsDeviceTvMantunOutSide;
//    @BindView(R.id.settings_device_rl_mantun_out_side)
//    RelativeLayout settingsDeviceRlMantunOutSide;
//    @BindView(R.id.iv_maunton_mantun_contact)
//    ImageView ivMauntonMantunContact;
//    @BindView(R.id.settings_device_tv_mantun_contact)
//    TextView settingsDeviceTvMantunContact;
//    @BindView(R.id.settings_device_rl_mantun_contact)
//    RelativeLayout settingsDeviceRlMantunContact;
//    @BindView(R.id.settings_device_ll_matun_root)
//    LinearLayout settingsDeviceLlMatunRoot;
//    @BindView(R.id.settings_device_tv_maunton_control_switch_in)
//    TextView settingsDeviceTvMauntonControlSwitchIn;
//    @BindView(R.id.settings_device_rl_maunton_control_switch_in)
//    RelativeLayout settingsDeviceRlMauntonControlSwitchIn;
//    @BindView(R.id.settings_device_tv_maunton_control_switch_on)
//    TextView settingsDeviceTvMauntonControlSwitchOn;
//    @BindView(R.id.settings_device_rl_maunton_control_switch_on)
//    RelativeLayout settingsDeviceRlMauntonControlSwitchOn;
//    @BindView(R.id.settings_device_tv_maunton_control_self_chick)
//    TextView settingsDeviceTvMauntonControlSelfChick;
//    @BindView(R.id.settings_device_rl_maunton_control_self_chick)
//    RelativeLayout settingsDeviceRlMauntonControlSelfChick;
//    @BindView(R.id.settings_device_tv_maunton_control_elec_clear_zero)
//    TextView settingsDeviceTvMauntonControlElecClearZero;
//    @BindView(R.id.settings_device_rl_maunton_control_elec_clear_zero)
//    RelativeLayout settingsDeviceRlMauntonControlElecClearZero;
//    @BindView(R.id.settings_device_tv_maunton_control_restore)
//    TextView settingsDeviceTvMauntonControlRestore;
//    @BindView(R.id.settings_device_rl_maunton_control_restore)
//    RelativeLayout settingsDeviceRlMauntonControlRestore;
//    @BindView(R.id.settings_device_ll_maunton_control)
//    LinearLayout settingsDeviceLlMauntonControl;
    @BindView(R.id.settings_device_rl_ibeacon)
    RelativeLayout settingsDeviceRlIbeacon;
    @BindView(R.id.iv_uuid_value)
    ImageView ivUuidValue;
    @BindView(R.id.iv_major_value)
    ImageView ivMajorValue;
    @BindView(R.id.iv_minor_value)
    ImageView ivMinorValue;
    @BindView(R.id.layout_umm)
    LinearLayout layoutUmm;
    @BindView(R.id.settings_device_ll_umm)
    LinearLayout settingsDeviceLlUmm;
    @BindView(R.id.iv_power_value)
    ImageView ivPowerValue;
    @BindView(R.id.iv_rate_value)
    ImageView ivRateValue;
    @BindView(R.id.iv_turnon_time)
    ImageView ivTurnonTime;
    @BindView(R.id.iv_turnoff_time)
    ImageView ivTurnoffTime;
    @BindView(R.id.iv_transmit_power)
    ImageView ivTransmitPower;
    @BindView(R.id.iv_eirp)
    ImageView ivEirp;
    @BindView(R.id.iv_ad_interval_top)
    ImageView ivAdIntervalTop;
    @BindView(R.id.iv_ad_interval)
    ImageView ivAdInterval;
    @BindView(R.id.slot1_sep_iv1)
    ImageView slot1SepIv1;
    @BindView(R.id.slot1_item1_iv)
    ImageView slot1Item1Iv;
    @BindView(R.id.slot1_item2_iv)
    ImageView slot1Item2Iv;
    @BindView(R.id.slot1_sep_iv3)
    ImageView slot1SepIv3;
    @BindView(R.id.slot1_item3_tv)
    TextView slot1Item3Tv;
    @BindView(R.id.slot1_item3_iv)
    ImageView slot1Item3Iv;
    @BindView(R.id.slot1_item3_tv_v)
    TextView slot1Item3TvV;
    @BindView(R.id.settings_device_rl_slot1_item3)
    RelativeLayout settingsDeviceRlSlot1Item3;
    @BindView(R.id.slot1_sep_iv4)
    ImageView slot1SepIv4;
    @BindView(R.id.slot1_item4_tv)
    TextView slot1Item4Tv;
    @BindView(R.id.slot1_item4_iv)
    ImageView slot1Item4Iv;
    @BindView(R.id.slot1_item4_tv_v)
    TextView slot1Item4TvV;
    @BindView(R.id.settings_device_rl_slot1_item4)
    RelativeLayout settingsDeviceRlSlot1Item4;
    @BindView(R.id.slot2_sep_iv1)
    ImageView slot2SepIv1;
    @BindView(R.id.slot2_item1_iv)
    ImageView slot2Item1Iv;
    @BindView(R.id.slot2_item2_iv)
    ImageView slot2Item2Iv;
    @BindView(R.id.slot2_sep_iv3)
    ImageView slot2SepIv3;
    @BindView(R.id.slot2_item3_tv)
    TextView slot2Item3Tv;
    @BindView(R.id.slot2_item3_iv)
    ImageView slot2Item3Iv;
    @BindView(R.id.slot2_item3_tv_v)
    TextView slot2Item3TvV;
    @BindView(R.id.settings_device_rl_slot2_item3)
    RelativeLayout settingsDeviceRlSlot2Item3;
    @BindView(R.id.slot2_sep_iv4)
    ImageView slot2SepIv4;
    @BindView(R.id.slot2_item4_tv)
    TextView slot2Item4Tv;
    @BindView(R.id.slot2_item4_iv)
    ImageView slot2Item4Iv;
    @BindView(R.id.slot2_item4_tv_v)
    TextView slot2Item4TvV;
    @BindView(R.id.settings_device_rl_slot2_item4)
    RelativeLayout settingsDeviceRlSlot2Item4;
    @BindView(R.id.slot3_sep_iv1)
    ImageView slot3SepIv1;
    @BindView(R.id.slot3_item1_iv)
    ImageView slot3Item1Iv;
    @BindView(R.id.slot3_item2_iv)
    ImageView slot3Item2Iv;
    @BindView(R.id.slot3_sep_iv3)
    ImageView slot3SepIv3;
    @BindView(R.id.slot3_item3_tv)
    TextView slot3Item3Tv;
    @BindView(R.id.slot3_item3_iv)
    ImageView slot3Item3Iv;
    @BindView(R.id.slot3_item3_tv_v)
    TextView slot3Item3TvV;
    @BindView(R.id.settings_device_rl_slot3_item3)
    RelativeLayout settingsDeviceRlSlot3Item3;
    @BindView(R.id.slot3_sep_iv4)
    ImageView slot3SepIv4;
    @BindView(R.id.slot3_item4_tv)
    TextView slot3Item4Tv;
    @BindView(R.id.slot3_item4_iv)
    ImageView slot3Item4Iv;
    @BindView(R.id.slot3_item4_tv_v)
    TextView slot3Item4TvV;
    @BindView(R.id.settings_device_rl_slot3_item4)
    RelativeLayout settingsDeviceRlSlot3Item4;
    @BindView(R.id.slot4_sep_iv1)
    ImageView slot4SepIv1;
    @BindView(R.id.slot4_item1_iv)
    ImageView slot4Item1Iv;
    @BindView(R.id.slot4_item2_iv)
    ImageView slot4Item2Iv;
    @BindView(R.id.slot4_sep_iv3)
    ImageView slot4SepIv3;
    @BindView(R.id.slot4_item3_tv)
    TextView slot4Item3Tv;
    @BindView(R.id.slot4_item3_iv)
    ImageView slot4Item3Iv;
    @BindView(R.id.slot4_item3_tv_v)
    TextView slot4Item3TvV;
    @BindView(R.id.settings_device_rl_slot4_item3)
    RelativeLayout settingsDeviceRlSlot4Item3;
    @BindView(R.id.slot4_sep_iv4)
    ImageView slot4SepIv4;
    @BindView(R.id.slot4_item4_tv)
    TextView slot4Item4Tv;
    @BindView(R.id.slot4_item4_iv)
    ImageView slot4Item4Iv;
    @BindView(R.id.slot4_item4_tv_v)
    TextView slot4Item4TvV;
    @BindView(R.id.settings_device_rl_slot4_item4)
    RelativeLayout settingsDeviceRlSlot4Item4;
    @BindView(R.id.settings_device_rl_custom_package1_state)
    RelativeLayout settingsDeviceRlCustomPackage1State;
    @BindView(R.id.pack1_icon)
    ImageView pack1Icon;
    @BindView(R.id.settings_device_rl_custom_package2_state)
    RelativeLayout settingsDeviceRlCustomPackage2State;
    @BindView(R.id.pack2_icon)
    ImageView pack2Icon;
    @BindView(R.id.settings_device_rl_custom_package3_state)
    RelativeLayout settingsDeviceRlCustomPackage3State;
    @BindView(R.id.pack3_icon)
    ImageView pack3Icon;
    @BindView(R.id.settings_device_ll_custome_package3)
    LinearLayout settingsDeviceLlCustomePackage3;
    @BindView(R.id.iv_confirm_package)
    ImageView ivConfirmPackage;
    @BindView(R.id.iv_upload_upper_limit)
    ImageView ivUploadUpperLimit;
    @BindView(R.id.iv_co_upper_limit)
    ImageView ivCoUpperLimit;
    @BindView(R.id.iv_co2_upper_limit)
    ImageView ivCo2UpperLimit;
    @BindView(R.id.iv_no2_upper_limit)
    ImageView ivNo2UpperLimit;
    @BindView(R.id.iv_ch4_upper_limit)
    ImageView ivCh4UpperLimit;
    @BindView(R.id.iv_lpg_upper_limit)
    ImageView ivLpgUpperLimit;
    @BindView(R.id.iv_pm10_upper_limit)
    ImageView ivPm10UpperLimit;
    @BindView(R.id.iv_pm25_upper_limit)
    ImageView ivPm25UpperLimit;
    @BindView(R.id.iv_temp_upper_limit)
    ImageView ivTempUpperLimit;
    @BindView(R.id.iv_temp_lower_limit)
    ImageView ivTempLowerLimit;
    @BindView(R.id.iv_humidity_upper_limit)
    ImageView ivHumidityUpperLimit;
    @BindView(R.id.iv_humidity_lower_limit)
    ImageView ivHumidityLowerLimit;
    @BindView(R.id.iv_pitch_angle_upper_limit)
    ImageView ivPitchAngleUpperLimit;
    @BindView(R.id.iv_pitch_angle_lower_limit)
    ImageView ivPitchAngleLowerLimit;
    @BindView(R.id.iv_yaw_angle_upper_limit)
    ImageView ivYawAngleUpperLimit;
    @BindView(R.id.iv_yaw_angle_lower_limit)
    ImageView ivYawAngleLowerLimit;
    @BindView(R.id.iv_roll_angle_upper_limit)
    ImageView ivRollAngleUpperLimit;
    @BindView(R.id.iv_roll_angle_lower_limit)
    ImageView ivRollAngleLowerLimit;
    @BindView(R.id.settings_device_rl_angle_zero)
    RelativeLayout settingsDeviceRlAngleZero;
    @BindView(R.id.iv_water_pressure_upper_limit)
    ImageView ivWaterPressureUpperLimit;
    @BindView(R.id.iv_water_pressure_lower_limit)
    ImageView ivWaterPressureLowerLimit;
    @BindView(R.id.iv_temperature_pressure_upper_limit)
    ImageView ivTemperaturePressureUpperLimit;
    @BindView(R.id.iv_temperature_pressure_lower_limit)
    ImageView ivTemperaturePressureLowerLimit;
    @BindView(R.id.iv_temperature_pressure_upper_step_limit)
    ImageView ivTemperaturePressureUpperStepLimit;
    @BindView(R.id.iv_temperature_pressure_lower_step_limit)
    ImageView ivTemperaturePressureLowerStepLimit;
    @BindView(R.id.iv_fhsj_elec_pwd)
    ImageView ivFhsjElecPwd;
    @BindView(R.id.iv_fhsj_elec_leak)
    ImageView ivFhsjElecLeak;
    @BindView(R.id.iv_fhsj_elec_temp)
    ImageView ivFhsjElecTemp;
    @BindView(R.id.iv_fhsj_elec_current)
    ImageView ivFhsjElecCurrent;
    @BindView(R.id.iv_fhsj_elec_overload)
    ImageView ivFhsjElecOverload;
    @BindView(R.id.iv_fhsj_elec_overpressure)
    ImageView ivFhsjElecOverpressure;
    @BindView(R.id.iv_fhsj_elec_undervoltage)
    ImageView ivFhsjElecUndervoltage;
    @BindView(R.id.settings_device_tv_fhsj_elec_control_reset)
    TextView settingsDeviceTvFhsjElecControlReset;
    @BindView(R.id.settings_device_tv_fhsj_elec_control_restore)
    TextView settingsDeviceTvFhsjElecControlRestore;
    @BindView(R.id.settings_device_tv_fhsj_elec_control_air_switch)
    TextView settingsDeviceTvFhsjElecControlAirSwitch;
    @BindView(R.id.settings_device_tv_fhsj_elec_control_self_test)
    TextView settingsDeviceTvFhsjElecControlSelfTest;
    @BindView(R.id.settings_device_tv_fhsj_elec_control_silence)
    TextView settingsDeviceTvFhsjElecControlSilence;
    @BindView(R.id.settings_device_tv_fhsj_elec_control_zero_power)
    TextView settingsDeviceTvFhsjElecControlZeroPower;
    @BindView(R.id.settings_device_rl_smoke)
    RelativeLayout settingsDeviceRlSmoke;
    @BindView(R.id.settings_device_rl_smoke_start)
    RelativeLayout settingsDeviceRlSmokeStart;
    @BindView(R.id.settings_device_rl_smoke_stop)
    RelativeLayout settingsDeviceRlSmokeStop;
    @BindView(R.id.settings_device_rl_smoke_silence)
    RelativeLayout settingsDeviceRlSmokeSilence;
    @BindView(R.id.acrel_leakage_th_content)
    TextView acrelLeakageThContent;
    @BindView(R.id.acrel_leakage_th)
    LinearLayout acrelLeakageTh;
    @BindView(R.id.acrel_connect_sw_content)
    TextView acrelConnectSwContent;
    @BindView(R.id.acrel_connect_sw)
    LinearLayout acrelConnectSw;
    @BindView(R.id.acrel_ch_enable_content)
    TextView acrelChEnableContent;
    @BindView(R.id.acrel_ch_enable)
    LinearLayout acrelChEnable;
    @BindView(R.id.acrel_t1_th_content)
    TextView acrelT1ThContent;
    @BindView(R.id.acrel_t1_th)
    LinearLayout acrelT1Th;
    @BindView(R.id.acrel_t2_th_content)
    TextView acrelT2ThContent;
    @BindView(R.id.acrel_t2_th)
    LinearLayout acrelT2Th;
    @BindView(R.id.acrel_t3_th_content)
    TextView acrelT3ThContent;
    @BindView(R.id.acrel_t3_th)
    LinearLayout acrelT3Th;
    @BindView(R.id.acrel_t4_th_content)
    TextView acrelT4ThContent;
    @BindView(R.id.acrel_t4_th)
    LinearLayout acrelT4Th;
    @BindView(R.id.acrel_curr_high_set_content)
    TextView acrelCurrHighSetContent;
    @BindView(R.id.acrel_curr_high_set)
    LinearLayout acrelCurrHighSet;
    @BindView(R.id.acrel_val_high_set_content)
    TextView acrelValHighSetContent;
    @BindView(R.id.acrel_val_high_set)
    LinearLayout acrelValHighSet;
    @BindView(R.id.acrel_val_low_set_content)
    TextView acrelValLowSetContent;
    @BindView(R.id.acrel_val_low_set)
    LinearLayout acrelValLowSet;
    @BindView(R.id.acrel_val_high_type_content)
    TextView acrelValHighTypeContent;
    @BindView(R.id.acrel_val_high_type)
    LinearLayout acrelValHighType;
    @BindView(R.id.acrel_val_low_type_content)
    TextView acrelValLowTypeContent;
    @BindView(R.id.acrel_val_low_type)
    LinearLayout acrelValLowType;
    @BindView(R.id.acrel_curr_high_type_content)
    TextView acrelCurrHighTypeContent;
    @BindView(R.id.acrel_curr_high_type)
    LinearLayout acrelCurrHighType;
    @BindView(R.id.acrel_cmd_reset)
    LinearLayout acrelCmdReset;
    @BindView(R.id.acrel_cmd_self_check)
    LinearLayout acrelCmdSelfCheck;
    @BindView(R.id.view_acrel_line_query)
    View acrelLineQuery;
    @BindView(R.id.acrel_cmd_query)
    LinearLayout acrelCmdQuery;
    @BindView(R.id.view_acrel_line_mute)
    View acrelLineMute;
    @BindView(R.id.acrel_cmd_mute)
    LinearLayout acrelCmdMute;
    @BindView(R.id.view_acrel_line_zero_claring)
    View acrelLineZeroClearing;
    @BindView(R.id.acrel_cmd_zero_clearing)
    LinearLayout acrelCmdZeroClearing;
    @BindView(R.id.acrel_root)
    LinearLayout acrelRoot;
    @BindView(R.id.layout_root)
    LinearLayout layoutRoot;
    @BindView(R.id.acrel_psd_content)
    TextView acrelPsdContent;
    @BindView(R.id.acrel_psd)
    LinearLayout acrelPsd;
    @BindView(R.id.cayman_is_smoke_content)
    TextView caymanIsSmokeContent;
    @BindView(R.id.cayman_is_smoke)
    LinearLayout caymanIsSmoke;
    @BindView(R.id.cayman_is_moved_content)
    TextView caymanIsMovedContent;
    @BindView(R.id.cayman_is_moved)
    LinearLayout caymanIsMoved;
    @BindView(R.id.cayman_value_of_tem_content)
    TextView caymanValueOfTemContent;
    @BindView(R.id.cayman_value_of_tem)
    LinearLayout caymanValueOfTem;
    @BindView(R.id.cayman_value_of_photor_content)
    TextView caymanValueOfPhotorContent;
    @BindView(R.id.cayman_value_of_photor)
    LinearLayout caymanValueOfPhotor;
    @BindView(R.id.cayman_ble_adv_type_content)
    TextView caymanBleAdvTypeContent;
    @BindView(R.id.cayman_ble_adv_type)
    LinearLayout caymanBleAdvType;
    @BindView(R.id.cayman_ble_adv_start_time_content)
    TextView caymanBleAdvStartTimeContent;
    @BindView(R.id.cayman_ble_adv_start_time)
    LinearLayout caymanBleAdvStartTime;
    @BindView(R.id.cayman_ble_adv_end_time_content)
    TextView caymanBleAdvEndTimeContent;
    @BindView(R.id.cayman_ble_adv_end_time_hum)
    LinearLayout caymanBleAdvEndTime;
    @BindView(R.id.cayman_value_of_batb_content)
    TextView caymanValueOfBatbContent;
    @BindView(R.id.cayman_value_of_batb)
    LinearLayout caymanValueOfBatb;
    @BindView(R.id.cayman_human_detection_time_content)
    TextView caymanHumanDetctionTimeContent;
    @BindView(R.id.cayman_human_detection_time)
    LinearLayout caymanHumanDetectionTime;
    @BindView(R.id.cayman_defense_mode_content)
    TextView caymanDefenseModeContent;
    @BindView(R.id.cayman_defense_mode)
    LinearLayout caymanDefenseMode;
    @BindView(R.id.cayman_defense_timer_mode_content)
    TextView caymanDefenseTimerModeContent;
    @BindView(R.id.cayman_defense_timer_mode)
    LinearLayout caymanDefenseTimerMode;
    @BindView(R.id.cayman_defense_mode_start_time_content)
    TextView caymanDefenseModeStartTimeContent;
    @BindView(R.id.cayman_defense_mode_start_time)
    LinearLayout caymanDefenseModeStartTime;
    @BindView(R.id.cayman_defense_mode_stop_time_content)
    TextView caymanDefenseModeStopTimeContent;
    @BindView(R.id.cayman_defense_mode_stop_time)
    LinearLayout caymanDefenseModeStopTime;
    @BindView(R.id.cayman_invade_alarm_content)
    TextView caymanInvadeAlarmContent;
    @BindView(R.id.cayman_invade_alarm)
    LinearLayout caymanInvadeAlarm;
    @BindView(R.id.cayman_value_of_hum_content)
    TextView caymanValueOfHumContent;
    @BindView(R.id.cayman_value_of_hum)
    LinearLayout caymanValueOfHum;
    @BindView(R.id.cayman_alarm_of_high_tem_content)
    TextView caymanAlarmOfHighTemContent;
    @BindView(R.id.cayman_alarm_of_high_tem)
    LinearLayout caymanAlarmOfHighTem;
    @BindView(R.id.cayman_alarm_of_low_tem_content)
    TextView caymanAlarmOfLowTemContent;
    @BindView(R.id.cayman_alarm_of_low_tem)
    LinearLayout caymanAlarmOfLowTem;
    @BindView(R.id.cayman_alarm_of_high_hum_content)
    TextView caymanAlarmOfHighHumContent;
    @BindView(R.id.cayman_alarm_of_high_hum)
    LinearLayout caymanAlarmOfHighHum;
    @BindView(R.id.cayman_alarm_of_low_hum_content)
    TextView caymanAlarmOfLowHumContent;
    @BindView(R.id.cayman_alarm_of_low_hum)
    LinearLayout caymanAlarmOfLowHum;
    @BindView(R.id.cayman_cmd_self_check)
    LinearLayout caymanCmdSelfCheck;
    @BindView(R.id.cayman_cmd_reset)
    LinearLayout caymanCmdReset;
    @BindView(R.id.cayman_cmd_clear_sound)
    LinearLayout caymanCmdClearSound;
    @BindView(R.id.cayman_root)
    LinearLayout caymanRoot;
    @BindView(R.id.baymax_density_content)
    TextView baymaxDensityContent;
    @BindView(R.id.baymax_density)
    LinearLayout baymaxDensity;
    @BindView(R.id.baymax_density_l1_content)
    TextView baymaxDensityL1Content;
    @BindView(R.id.baymax_density_l1)
    LinearLayout baymaxDensityL1;
    @BindView(R.id.baymax_density_l2_content)
    TextView baymaxDensityL2Content;
    @BindView(R.id.baymax_density_l2)
    LinearLayout baymaxDensityL2;
    @BindView(R.id.baymax_density_l3_content)
    TextView baymaxDensityL3Content;
    @BindView(R.id.baymax_density_l3)
    LinearLayout baymaxDensityL3;
    @BindView(R.id.baymax_disassembly_content)
    TextView baymaxDisassemblyContent;
    @BindView(R.id.baymax_disassembly)
    LinearLayout baymaxDisassembly;
    @BindView(R.id.baymax_lose_pwr_content)
    TextView baymaxLosePwrContent;
    @BindView(R.id.baymax_lose_pwr)
    LinearLayout baymaxLosePwr;
    @BindView(R.id.baymax_em_valve_content)
    TextView baymaxEmValveContent;
    @BindView(R.id.baymax_em_valve)
    LinearLayout baymaxEmValve;
    @BindView(R.id.baymax_coms_down_content)
    TextView baymaxComsDownContent;
    @BindView(R.id.baymax_coms_down)
    LinearLayout baymaxComsDown;
    @BindView(R.id.baymax_cmd_self_check)
    LinearLayout baymaxCmdSelfCheck;
    @BindView(R.id.baymax_cmd_reset)
    LinearLayout baymaxCmdReset;
    @BindView(R.id.baymax_cmd_mute)
    LinearLayout baymaxCmdMute;
    @BindView(R.id.baymax_cmd_close_electronic_valve)
    LinearLayout baymaxCmdCloseElectronicValve;
    @BindView(R.id.baymax_root)
    LinearLayout baymaxRoot;
    @BindView(R.id.settings_device_rc_matun_fire)
    RecyclerView rcMatunFire;
    @BindView(R.id.settings_device_rc_ibeacon)
    RecyclerView rcIbeacon;
    @BindView(R.id.view_cayman_is_smoke)
    View viewCaymanIsSmoke;
    @BindView(R.id.view_cayman_is_move)
    View viewCaymanIsMove;
    @BindView(R.id.view_cayman_value_of_tem)
    View viewCaymanValueOfTem;
    @BindView(R.id.view_cayman_value_of_hum)
    View viewCaymanValueOfHum;
    @BindView(R.id.view_cayman_alarm_of_high_tem)
    View viewCaymanAlarmOfHighTem;
    @BindView(R.id.view_cayman_alarm_of_low_tem)
    View viewCaymanAlarmOfLowTem;
    @BindView(R.id.view_cayman_alarm_of_high_hum)
    View viewCaymanAlarmOfHighHum;
    @BindView(R.id.view_cayman_alarm_of_low_hum)
    View viewCaymanAlarmOfLowHum;
    @BindView(R.id.view_cayman_value_of_photor)
    View viewCaymanValueOfPhotor;
    @BindView(R.id.view_cayman_ble_adv_type)
    View viewCaymanBleAdvType;
    @BindView(R.id.view_ble_adv_start_time)
    View viewBleAdvStartTime;
    @BindView(R.id.view_ble_adv_end_time_hum)
    View viewBleAdvEndTimeHum;
    @BindView(R.id.view_cayman_human_detection_time)
    View viewCaymanHumanDetectionTime;
    @BindView(R.id.view_cayman_defense_mode)
    View viewCaymanDefenseMode;
    @BindView(R.id.view_cayman_defense_timer_mode)
    View viewCaymanDefenseTimerMode;
    @BindView(R.id.view_cayman_defense_mode_start_time)
    View viewCaymanDefenseModeStartTime;
    @BindView(R.id.view_cayman_defense_mode_stop_time)
    View viewCaymanDefenseModeStopTime;
    @BindView(R.id.view_cayman_invade_alarm)
    TextView viewCaymanInvadeAlarm;
    @BindView(R.id.view_cayman_cmd_self_check)
    View viewCaymanCmdSelfCheck;
    @BindView(R.id.view_cayman_cmd_reset)
    View viewCaymanCmdReset;

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
    private SensoroSensor sensoroSensor;
    private int[] txp_array;
    private SettingEnterDialogUtils mSettingEnterDialogUtils;
    private int isCaymanReset = -1;
    private OptionsPickerView<Integer> pvCustomOptions;
    private int picekerViewStatus = -1;
    private ArrayList<Integer> pickerHours;
    private ArrayList<Integer> pickerMinutes;

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
        txp_array = Constants.LORA_SE433_TXP;
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
                loraEirpItems = obtainEirpItems();
                loraEirpValues = Constants.LORA_AS923_MAX_EIRP_VALUE;
                break;
            case Constants.LORA_BAND_EU433:
                txp_array = Constants.LORA_EU433_TXP;
                loraEirpItems = obtainEirpItems();
                loraEirpValues = Constants.LORA_EU433_MAX_EIRP_VALUE;
                break;
            case Constants.LORA_BAND_EU868:
                txp_array = Constants.LORA_EU868_TXP;
                loraEirpItems = obtainEirpItems();
                loraEirpValues = Constants.LORA_EU868_MAX_EIRP_VALUE;
                break;
            case Constants.LORA_BAND_CN470:
                txp_array = Constants.LORA_CN470_TXP;
                loraEirpItems = obtainEirpItems();
                loraEirpValues = Constants.LORA_CN470_MAX_EIRP_VALUE;
            case Constants.LORA_BAND_SE800:
                txp_array = Constants.LORA_SE800_TXP;
                loraEirpItems = obtainEirpItems();
                loraEirpValues = Constants.LORA_SE800_MAX_EIRP_VALUE;

        }
        loraTxpItems = new String[txp_array.length];
        for (int i = 0; i < txp_array.length; i++) {
            int txp = txp_array[i];
            loraTxpItems[i] = txp + " dBm";
        }
    }

    private String[] obtainEirpItems() {
        String[] loraEirpItems = new String[txp_array.length];
        if (!sensoroDevice.hasMaxEirp()) {
            return loraEirpItems;
        }
        int maxEirp = sensoroDevice.getMaxEirp();

        for (int i = 0; i < txp_array.length; i++) {
            String format = String.format(Locale.CHINA, "MaxEIRP - %d \n%d dBm", i*2,
                    maxEirp - txp_array[txp_array.length - 1 - i] * 2);
            loraEirpItems[i] = format;
        }
        return loraEirpItems;

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
                        String firmwareVersion = sensoroDevice.getFirmwareVersion();
                        if (firmwareVersion.compareTo(SensoroDevice.FV_1_2) > 0) { // 1.3以后没有custom package 3
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

                    //ibeacon
                    loadIbeacon();

                    //曼顿电气火灾
                    loadMantunData();

                    boolean hasAcrelFires = sensoroSensor.hasAcrelFires;
                    acrelRoot.setVisibility(hasAcrelFires ? VISIBLE : GONE);
                    if (hasAcrelFires) {
                        if (sensoroSensor.acrelFires.hasPasswd) {
                            acrelPsdContent.setText(sensoroSensor.acrelFires.passwd + "");
                        } else {
                            acrelPsd.setVisibility(GONE);
                        }
                        if (sensoroSensor.acrelFires.hasLeakageTh) {
                            acrelLeakageThContent.setText(sensoroSensor.acrelFires.leakageTh + "");
                        } else {
                            acrelLeakageTh.setVisibility(GONE);
                        }

                        if (sensoroSensor.acrelFires.hasConnectSw) {
                            int connectSw = sensoroSensor.acrelFires.connectSw;
                            byte[] bytes = SensoroUUID.intToBits(connectSw, 5);
                            StringBuilder content = new StringBuilder();
                            for (int i = 0; i < bytes.length; i++) {
                                if (bytes[i] == 1) {
                                    content.append("通道 " + (i + 1) + " 关联继电器，");
                                }
                            }
                            if (content.length() > 1) {
                                content.deleteCharAt(content.length() - 1);
                            }
                            acrelConnectSwContent.setText(content);
                        } else {
                            acrelConnectSw.setVisibility(GONE);
                        }

                        if (sensoroSensor.acrelFires.hasChEnable) {
                            int chEnable = sensoroSensor.acrelFires.chEnable;
                            byte[] bytes = SensoroUUID.intToBits(chEnable, 5);
                            StringBuilder content = new StringBuilder();
                            for (int i = 0; i < bytes.length; i++) {
                                if (bytes[i] == 1) {
                                    content.append("通道 " + (i + 1) + " 使能，");
                                }
                            }
                            if (content.length() > 1) {
                                content.deleteCharAt(content.length() - 1);
                            }
                            acrelChEnableContent.setText(content);
                        } else {
                            acrelChEnable.setVisibility(GONE);
                        }

                        if (sensoroSensor.acrelFires.hasT1Th) {
                            acrelT1ThContent.setText(sensoroSensor.acrelFires.t1Th + "");
                        } else {
                            acrelT1Th.setVisibility(GONE);
                        }

                        if (sensoroSensor.acrelFires.hasT2Th) {
                            acrelT2ThContent.setText(sensoroSensor.acrelFires.t2Th + "");
                        } else {
                            acrelT2Th.setVisibility(GONE);
                        }

                        if (sensoroSensor.acrelFires.hasT3Th) {
                            acrelT3ThContent.setText(sensoroSensor.acrelFires.t3Th + "");
                        } else {
                            acrelT3Th.setVisibility(GONE);
                        }

                        if (sensoroSensor.acrelFires.hasT4Th) {
                            acrelT4ThContent.setText(sensoroSensor.acrelFires.t4Th + "");
                        } else {
                            acrelT1Th.setVisibility(GONE);
                        }

                        if (sensoroSensor.acrelFires.hasCurrHighSet) {
                            String format = String.format(Locale.CHINA, "%.1f %%", sensoroSensor.acrelFires.currHighSet / 10f);
                            acrelCurrHighSetContent.setText(format);
                        } else {
                            acrelCurrHighSet.setVisibility(GONE);
                        }

                        if (sensoroSensor.acrelFires.hasValHighSet) {
                            String format = String.format(Locale.CHINA, "%.1f %%", sensoroSensor.acrelFires.valHighSet / 10f);
                            acrelValHighSetContent.setText(format);
                        } else {
                            acrelValHighSet.setVisibility(GONE);
                        }
                        if (sensoroSensor.acrelFires.hasValLowSet) {
                            String format = String.format(Locale.CHINA, "%.1f %%", sensoroSensor.acrelFires.valLowSet / 10f);
                            acrelValLowSetContent.setText(format);
                        } else {
                            acrelValLowSet.setVisibility(GONE);
                        }

                        if (sensoroSensor.acrelFires.hasValHighType) {
                            int valHighType = sensoroSensor.acrelFires.valHighType;
                            byte[] bytes = SensoroUUID.intToBits(valHighType, 2);
                            StringBuilder content = new StringBuilder();

                            if (bytes[0] == 1) {
                                content.append("保护开关：开");
                            }

                            if (bytes[1] == 1) {
                                content.append("保护关联DO1：开");
                            }
                            acrelValHighTypeContent.setText(content.toString());
                        } else {
                            acrelValHighType.setVisibility(GONE);
                        }

                        if (sensoroSensor.acrelFires.hasValLowType) {
                            int valLowType = sensoroSensor.acrelFires.valLowType;
                            byte[] bytes = SensoroUUID.intToBits(valLowType, 2);
                            StringBuilder content = new StringBuilder();

                            if (bytes[0] == 1) {
                                content.append("保护开关：开");
                            }

                            if (bytes[1] == 1) {
                                content.append("保护关联DO1：开");
                            }
                            acrelValLowTypeContent.setText(content.toString());
                        } else {
                            acrelValLowType.setVisibility(GONE);
                        }

                        if (sensoroSensor.acrelFires.hasCurrHighType) {
                            int currHighType = sensoroSensor.acrelFires.currHighType;
                            byte[] bytes = SensoroUUID.intToBits(currHighType, 2);
                            StringBuilder content = new StringBuilder();

                            if (bytes[0] == 1) {
                                content.append("保护开关：开");
                            }

                            if (bytes[1] == 1) {
                                content.append("保护关联DO1：开");
                            }
                            acrelCurrHighTypeContent.setText(content.toString());
                        } else {
                            acrelCurrHighType.setVisibility(GONE);
                        }

                    }

                    boolean hasCayMan = sensoroSensor.hasCayMan;
                    caymanRoot.setVisibility(hasCayMan ? VISIBLE : GONE);
                    if (hasCayMan) {
                        if (sensoroSensor.cayManData.hasIsSmoke) {
                            if (sensoroSensor.cayManData.isSmoke == 0) {
                                caymanIsSmokeContent.setText("无烟");
                            } else {
                                caymanIsSmokeContent.setText("有烟");
                            }
                        } else {
                            caymanIsSmoke.setVisibility(GONE);
                            viewCaymanIsSmoke.setVisibility(GONE);
                        }

                        if (sensoroSensor.cayManData.hasIsMoved) {
                            if (sensoroSensor.cayManData.isMoved == 0) {
                                caymanIsMovedContent.setText("未拆卸");
                            } else {
                                caymanIsMovedContent.setText("拆卸");
                            }
                        } else {
                            caymanIsMoved.setVisibility(GONE);
                            viewCaymanIsMove.setVisibility(GONE);
                        }

                        if (sensoroSensor.cayManData.hasValueOfTem) {
                            caymanValueOfTemContent.setText(sensoroSensor.cayManData.valueOfTem / 10f + "℃");
                        } else {
                            caymanValueOfTem.setVisibility(GONE);
                            viewCaymanValueOfTem.setVisibility(GONE);
                        }
                        if (sensoroSensor.cayManData.hasValueOfHum) {
                            caymanValueOfHumContent.setText(sensoroSensor.cayManData.valueOfHum / 10f + "%");
                        } else {
                            caymanValueOfHum.setVisibility(GONE);
                            viewCaymanValueOfHum.setVisibility(GONE);
                        }
                        if (sensoroSensor.cayManData.hasAlarmOfHighTem) {
                            caymanAlarmOfHighTemContent.setText(sensoroSensor.cayManData.alarmOfHighTem / 10f + "℃");
                        } else {
                            caymanAlarmOfHighTem.setVisibility(GONE);
                            viewCaymanAlarmOfHighTem.setVisibility(GONE);
                        }
                        if (sensoroSensor.cayManData.hasAlarmOfLowTem) {
                            caymanAlarmOfLowTemContent.setText(sensoroSensor.cayManData.alarmOfLowTem / 10f + "℃");
                        } else {
                            caymanAlarmOfLowTem.setVisibility(GONE);
                            viewCaymanAlarmOfLowTem.setVisibility(GONE);
                        }
                        if (sensoroSensor.cayManData.hasAlarmOfHighHum) {
                            caymanAlarmOfHighHumContent.setText(sensoroSensor.cayManData.alarmOfHighHum / 10f + "%");
                        } else {
                            caymanAlarmOfHighHum.setVisibility(GONE);
                            viewCaymanAlarmOfHighHum.setVisibility(GONE);
                        }
                        if (sensoroSensor.cayManData.hasAlarmOfLowHum) {
                            caymanAlarmOfLowHumContent.setText(sensoroSensor.cayManData.alarmOfLowHum / 10f + "%");
                        } else {
                            caymanAlarmOfLowHum.setVisibility(GONE);
                            viewCaymanAlarmOfLowHum.setVisibility(GONE);
                        }
                        if (sensoroSensor.cayManData.hasValueOfphotor) {
                            caymanValueOfPhotorContent.setText(String.valueOf(sensoroSensor.cayManData.valueOfphotor));
                        } else {
                            caymanValueOfPhotor.setVisibility(GONE);
                            viewCaymanValueOfPhotor.setVisibility(GONE);
                        }
                        if (sensoroSensor.cayManData.hasBleAdvType) {
                            caymanBleAdvTypeContent.setText(sensoroSensor.cayManData.bleAdvType == 0 ? "持续广播" : "间断广播");
                        } else {
                            caymanBleAdvType.setVisibility(GONE);
                            viewCaymanBleAdvType.setVisibility(GONE);
                        }
                        if (sensoroSensor.cayManData.hasValueOfBatb && sensoroSensor.cayManData.valueOfBatb > -1) {
                            caymanValueOfBatbContent.setText(sensoroSensor.cayManData.valueOfBatb / 1000f + "v");
                        } else {
                            caymanValueOfBatb.setVisibility(GONE);
                            viewCaymanValueOfBatb.setVisibility(GONE);
                        }

                        if (sensoroSensor.cayManData.hasHumanDetectionTime) {
                            caymanHumanDetctionTimeContent.setText(sensoroSensor.cayManData.humanDetectionTime + "s");
                        } else {
                            caymanHumanDetectionTime.setVisibility(GONE);
                            viewCaymanHumanDetectionTime.setVisibility(GONE);
                        }

                        if (sensoroSensor.cayManData.hasDefenseMode) {
                            caymanDefenseModeContent.setText(sensoroSensor.cayManData.defenseMode == 0 ? R.string.close : R.string.open);
                        } else {
                            caymanDefenseMode.setVisibility(GONE);
                            viewCaymanDefenseMode.setVisibility(GONE);
                        }

                        if (sensoroSensor.cayManData.hasDefenseTimerMode) {
                            caymanDefenseTimerModeContent.setText(sensoroSensor.cayManData.defenseTimerMode == 0 ? R.string.close : R.string.open);
                        } else {
                            caymanDefenseTimerMode.setVisibility(GONE);
                            viewCaymanDefenseTimerMode.setVisibility(GONE);
                        }

                        if (sensoroSensor.cayManData.hasDefenseModeStartTime) {
                            String s = timeZoneConvert(sensoroSensor.cayManData.defenseModeStartTime);
                            caymanDefenseModeStartTimeContent.setText(s);
                        } else {
                            caymanDefenseModeStartTime.setVisibility(GONE);
                            viewCaymanDefenseModeStartTime.setVisibility(GONE);
                        }

                        if (sensoroSensor.cayManData.hasDefenseModeStopTime) {
                            String s = timeZoneConvert(sensoroSensor.cayManData.defenseModeStopTime);
                            caymanDefenseModeStopTimeContent.setText(s);
                        } else {
                            caymanDefenseModeStopTime.setVisibility(GONE);
                            viewCaymanDefenseModeStopTime.setVisibility(GONE);
                        }

                        if (sensoroSensor.cayManData.hasInvadeAlarm) {
                            caymanInvadeAlarmContent.setText(sensoroSensor.cayManData.invadeAlarm == 0 ? R.string.close : R.string.open);
                        } else {
                            caymanInvadeAlarm.setVisibility(GONE);
                            viewCaymanInvadeAlarm.setVisibility(GONE);
                        }


                    }

                    boolean hasBaymax = sensoroSensor.hasBaymax;
                    baymaxRoot.setVisibility(hasBaymax ? VISIBLE : GONE);
                    if (hasBaymax) {
                        baymaxDensity.setVisibility(sensoroSensor.baymax.hasGasDensity ? VISIBLE : GONE);
                        if (sensoroSensor.baymax.hasGasDensity) {
                            if ("baymax_lpg".equals(deviceType)) {
                                baymaxDensityContent.setText(String.format(Locale.CHINESE, "%.2f%%", (float) (sensoroSensor.baymax.gasDensity / 210)));
                            } else if ("baymax_ch4".equals(deviceType)) {
                                baymaxDensityContent.setText(String.format(Locale.CHINESE, "%.2f%%", (float) (sensoroSensor.baymax.gasDensity / 500)));
                            }

                        }
                        baymaxDensity.setVisibility(sensoroSensor.baymax.hasGasDensityL1 ? VISIBLE : GONE);
                        if (sensoroSensor.baymax.hasGasDensityL1) {
                            baymaxDensityL1Content.setText(sensoroSensor.baymax.gasDensityL1 + "%");
                        }
                        baymaxDensity.setVisibility(sensoroSensor.baymax.hasGasDensityL2 ? VISIBLE : GONE);
                        if (sensoroSensor.baymax.hasGasDensityL2) {
                            baymaxDensityL2Content.setText(sensoroSensor.baymax.gasDensityL2 + "%");
                        }
                        baymaxDensity.setVisibility(sensoroSensor.baymax.hasGasDensityL3 ? VISIBLE : GONE);
                        if (sensoroSensor.baymax.hasGasDensityL3) {
                            baymaxDensityL3Content.setText(sensoroSensor.baymax.gasDensityL3 + "%");
                        }
                        baymaxDensity.setVisibility(sensoroSensor.baymax.hasGasDisassembly ? VISIBLE : GONE);
                        if (sensoroSensor.baymax.hasGasDisassembly) {
                            if (sensoroSensor.baymax.gasDisassembly == 1) {
                                baymaxDisassemblyContent.setText(getString(R.string.disassembled));
                            } else if (sensoroSensor.baymax.gasDisassembly == 0) {
                                baymaxDisassemblyContent.setText(getString(R.string.installed));
                            }

                        }
                        baymaxDensity.setVisibility(sensoroSensor.baymax.hasGasLosePwr ? VISIBLE : GONE);
                        if (sensoroSensor.baymax.hasGasLosePwr) {
                            if (sensoroSensor.baymax.gasLosePwr == 1) {
                                baymaxLosePwrContent.setText(getString(R.string.disconnected));
                            } else if (sensoroSensor.baymax.gasLosePwr == 0) {
                                baymaxLosePwrContent.setText(getString(R.string.connected));
                            }
                        }
                        baymaxDensity.setVisibility(sensoroSensor.baymax.hasGasEMValve ? VISIBLE : GONE);
                        if (sensoroSensor.baymax.hasGasEMValve) {
                            if (sensoroSensor.baymax.gasEMValve == 1) {
                                baymaxEmValveContent.setText(getString(R.string.baymax_closed));
                            } else if (sensoroSensor.baymax.gasEMValve == 0) {
                                baymaxEmValveContent.setText(getString(R.string.baymax_opened));
                            }
                        }
                        baymaxDensity.setVisibility(sensoroSensor.baymax.hasGasDeviceComsDown ? VISIBLE : GONE);
                        if (sensoroSensor.baymax.hasGasDeviceComsDown) {
                            if (sensoroSensor.baymax.gasDeviceComsDown == 1) {
                                baymaxComsDownContent.setText(getString(R.string.baymax_malfunction));
                            } else if (sensoroSensor.baymax.gasDeviceComsDown == 0) {
                                baymaxEmValveContent.setText(getString(R.string.baymax_normal));
                            }
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

                    if (sensoroDevice.hasDemoMode()) {
                        demoLayout.setVisibility(VISIBLE);

                        if (sensoroDevice.getDemoMode() == 0) {
                            demoTextView.setText(getString(R.string.close));
                        } else if (sensoroDevice.getDemoMode() == 1) {
                            demoTextView.setText(getString(R.string.open));
                        }
                    } else {
                        demoLayout.setVisibility(GONE);
                    }
                } else {
                    appParamLayout.setVisibility(GONE);
                }

                if (sensoroDevice.hasBatteryBeep()) {
                    batteryBeepLayout.setVisibility(VISIBLE);
                    if (sensoroDevice.getBatteryBeep() == 0) {
                        batteryBeepTextView.setText(getString(R.string.close));
                    } else if (sensoroDevice.getBatteryBeep() == 1) {
                        batteryBeepTextView.setText(getString(R.string.open));
                    }
                } else {
                    batteryBeepLayout.setVisibility(GONE);
                }

                if (sensoroDevice.hasBeepMuteTime()) {
                    beepMuteTimeLayout.setVisibility(VISIBLE);
                } else {
                    beepMuteTimeLayout.setVisibility(GONE);
                }

                if (sensoroDevice.hasLedStatus()) {
                    ledStatusTextView.setVisibility(VISIBLE);
                    if (sensoroDevice.getLedStatus() == 0) {
                        ledStatusTextView.setText(getString(R.string.close));
                    } else if (sensoroDevice.getLedStatus() == 1) {
                        ledStatusTextView.setText(getString(R.string.open));
                    }
                } else {
                    ledStatusLayout.setVisibility(GONE);
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

    private void loadIbeacon() {
        if (sensoroSensor.hasIbeacon) {
            rcIbeacon.setVisibility(VISIBLE);
            DeviceAdapter ibeaconAdapter = new DeviceAdapter(this);
            LinearLayoutManager manager = new LinearLayoutManager(this);
            manager.setOrientation(LinearLayoutManager.VERTICAL);
            rcIbeacon.setLayoutManager(manager);
            rcIbeacon.setNestedScrollingEnabled(false);
            rcIbeacon.setAdapter(ibeaconAdapter);
            ArrayList<SettingDeviceModel> datas = new ArrayList<>();
            SettingDeviceModel model = new SettingDeviceModel();
            model.title = getString(R.string.ibeacon_function);
            model.viewType = 2;
            datas.add(model);

            if (sensoroSensor.ibeacon.hasUuid) {
                SettingDeviceModel model1 = new SettingDeviceModel();
                model1.name = "UUID";
                String uuid = Utils.byteString2String(sensoroSensor.ibeacon.uuid);
                ByteString uuid1 = sensoroSensor.ibeacon.uuid;
                if (uuid != null) {
                    StringBuilder uuidString = new StringBuilder(uuid.toUpperCase());
                    uuidString.insert(8, "-");
                    uuidString.insert(13, "-");
                    uuidString.insert(18, "-");
                    uuidString.insert(23, "-");
                    uuidTextView.setText(uuidString);
                }
                model1.content = uuid;
                model1.tag = 1;
                datas.add(model1);
            }

            if (sensoroSensor.ibeacon.hasMajor) {
                SettingDeviceModel model2 = new SettingDeviceModel();
                model2.name = "MAJOR";
                model2.content = String.format(Locale.ROOT, "0x%04X", sensoroSensor.ibeacon.major);
                model2.tag = 2;
                model2.originContent = sensoroSensor.ibeacon.major;
                datas.add(model2);
            }

            if (sensoroSensor.ibeacon.hasMinor) {
                SettingDeviceModel model3 = new SettingDeviceModel();
                model3.name = "MINOR";
                model3.content = String.format(Locale.ROOT, "0x%04X", sensoroSensor.ibeacon.minor);
                model3.tag = 3;
                model3.originContent = sensoroSensor.ibeacon.minor;
                datas.add(model3);
            }
            if (sensoroSensor.ibeacon.hasMrssi) {
                SettingDeviceModel model4 = new SettingDeviceModel();
                model4.name = "MRSSI";
                model4.content = String.valueOf(sensoroSensor.ibeacon.mrssi);
                model4.tag = 4;
                model4.originContent = sensoroSensor.ibeacon.mrssi;
                model4.isDivider = false;
                datas.add(model4);
            }

            ibeaconAdapter.updateData(datas);
            ibeaconAdapter.setOnItemClickListener(new RecyclerItemClickListener() {
                @Override
                public void onItemClick(SettingDeviceModel model, int position) {
                    switch ((int) model.tag) {
                        case 1:
                            //uuid
                            showUUIDDialog(model, ibeaconAdapter);
                            break;
                        case 2:
                            //major
                            showMajorDialog(model, ibeaconAdapter);
                            break;
                        case 3:
                            showMinorDialog(model, ibeaconAdapter);
                            break;
                        case 4:
                            showMrssiDialog(model, ibeaconAdapter);
                            break;
                    }
                }
            });

        } else {
            rcIbeacon.setVisibility(GONE);
        }
    }

    private void showMrssiDialog(SettingDeviceModel model, DeviceAdapter ibeaconAdapter) {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(String.valueOf(model.originContent));
        dialogFragment.show(getFragmentManager(), SETTINGS_MINOR);
        dialogFragment.setOnPositiveClickListener(new OnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(String tag, Bundle bundle) {
                String str = bundle.getString(SettingsInputDialogFragment.INPUT);
                try {
                    int i = Integer.parseInt(str);
                    model.originContent = sensoroSensor.ibeacon.mrssi = i;
                    model.content = String.valueOf(i);
                    ibeaconAdapter.notifyDataSetChanged();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    AlphaToast.INSTANCE.makeText(SettingDeviceActivity.this, getString(R.string.please_enter_correct_value), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void showMinorDialog(SettingDeviceModel model, DeviceAdapter ibeaconAdapter) {
        SettingsMajorMinorDialogFragment minorDialog = SettingsMajorMinorDialogFragment.newInstance(model.originContent);
        minorDialog.show(getFragmentManager(), SETTINGS_MINOR);
        minorDialog.setOnPositiveClickListener(new OnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(String tag, Bundle bundle) {
                int minor = bundle.getInt(SettingsMajorMinorDialogFragment.VALUE);
                model.originContent = sensoroSensor.ibeacon.minor = minor;
                model.content = String.format("0x%04X", minor);
                ibeaconAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showMajorDialog(SettingDeviceModel model, DeviceAdapter ibeaconAdapter) {
        SettingsMajorMinorDialogFragment majordialog = SettingsMajorMinorDialogFragment.newInstance(model.originContent);
        majordialog.show(getFragmentManager(), SETTINGS_MAJOR);
        majordialog.setOnPositiveClickListener(new OnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(String tag, Bundle bundle) {
                int major = bundle.getInt(SettingsMajorMinorDialogFragment.VALUE);
                model.originContent = sensoroSensor.ibeacon.major = major;
                model.content = String.format("0x%04X", major);
                ibeaconAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showUUIDDialog(SettingDeviceModel model, DeviceAdapter ibeaconAdapter) {
        String str = TextUtils.isEmpty(model.content) ? null : model.content.replaceAll("-", "");
        SettingsUUIDDialogFragment dialogFragment = SettingsUUIDDialogFragment.newInstance(str);
        dialogFragment.show(getFragmentManager(), SETTINGS_UUID);
        dialogFragment.setOnClickLisenter(new OnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(String tag, Bundle bundle) {
                String uuid = bundle.getString(SettingsUUIDDialogFragment.UUID);
                if (TextUtils.isEmpty(uuid)) {
                    model.content = "";
                } else {
                    StringBuilder uuidString = new StringBuilder(uuid);
                    uuidString.insert(8, "-");
                    uuidString.insert(13, "-");
                    uuidString.insert(18, "-");
                    uuidString.insert(23, "-");
                    model.content = uuidString.toString();
                    sensoroSensor.ibeacon.uuid = Utils.string2ByteString(uuid);
                }
                ibeaconAdapter.notifyDataSetChanged();
            }
        });

    }

    private void loadMantunData() {
        if (sensoroSensor.mantunDatas != null && sensoroSensor.mantunDatas.size() > 0) {
            rcMatunFire.setVisibility(VISIBLE);
            DeviceAdapter matunFireAdapter = new DeviceAdapter(this);
            LinearLayoutManager manager = new LinearLayoutManager(this);
            manager.setOrientation(LinearLayoutManager.VERTICAL);
            rcMatunFire.setLayoutManager(manager);
            rcMatunFire.setNestedScrollingEnabled(false);
            ArrayList<SettingDeviceModel> datas = new ArrayList<>();
            for (SensoroMantunData mantunData : sensoroSensor.mantunDatas) {
                int attribute = mantunData.attribute;
                int i = attribute & 0x00ff;
                int current = attribute >> 8;

                SettingDeviceModel settingDeviceModel = new SettingDeviceModel();
                settingDeviceModel.title = "Id：" + mantunData.id;
                settingDeviceModel.viewType = 2;
                datas.add(settingDeviceModel);

                SettingDeviceModel settingDeviceModel3 = new SettingDeviceModel();
                settingDeviceModel3.name = getString(R.string.attribute);
                if (i == 8) {
                    settingDeviceModel3.content = getString(R.string.elect_split_road);
                } else if (i == 7) {
                    settingDeviceModel3.content = getString(R.string.elect_main_road);
                }
                settingDeviceModel3.isArrow = false;
                datas.add(settingDeviceModel3);

                SettingDeviceModel settingDeviceModel1 = new SettingDeviceModel(getString(R.string.Overcurrent_threshold), String.valueOf(mantunData.currentTh), 1);
                settingDeviceModel1.min = 0f;
                settingDeviceModel1.max = current * 1.5f;
                settingDeviceModel1.hint = getString(R.string.current_threshold_range) + settingDeviceModel1.min + "-" + settingDeviceModel1.max;
                settingDeviceModel1.errMsg = getString(R.string.over_current_Threshold_range);
                settingDeviceModel1.tag = mantunData.id + "currentTh";
                settingDeviceModel1.canClick = false;
                datas.add(settingDeviceModel1);

                SettingDeviceModel settingDeviceModel2 = new SettingDeviceModel(getString(R.string.power_threshold), String.valueOf(mantunData.powerTh), 1);
                settingDeviceModel2.min = 0f;
                settingDeviceModel2.max = current * 220 * 1.5f;
                settingDeviceModel2.hint = getString(R.string.power_threshold_range) + settingDeviceModel1.min + "-" + settingDeviceModel1.max;
                settingDeviceModel2.errMsg = getString(R.string.over_power_Threshold_range);
                settingDeviceModel2.tag = mantunData.id + "powerTh";
                settingDeviceModel2.canClick = false;
                settingDeviceModel2.isDivider = false;
                datas.add(settingDeviceModel2);


            }

            SettingDeviceModel settingDeviceModel4 = new SettingDeviceModel();
            settingDeviceModel4.viewType = 2;
            settingDeviceModel4.title = getString(R.string.elect_control);
            datas.add(settingDeviceModel4);
            SettingDeviceModel settingDeviceModel5 = new SettingDeviceModel();
            settingDeviceModel5.viewType = 1;
            settingDeviceModel5.eventType = 2;
            settingDeviceModel5.cmd = 0;
            settingDeviceModel5.tag = 0;
            settingDeviceModel5.name = getString(R.string.query);
            datas.add(settingDeviceModel5);
            for (SensoroMantunData mantunData : sensoroSensor.mantunDatas) {
                SettingDeviceModel settingDeviceModel6 = new SettingDeviceModel();
                settingDeviceModel6.eventType = 2;
                settingDeviceModel6.cmd = 2;
                settingDeviceModel6.name = String.format(Locale.CHINESE, "%s(id:%d)", getString(R.string.close_brake), (int) mantunData.id);
                settingDeviceModel6.tag = mantunData.id;
                datas.add(settingDeviceModel6);

                SettingDeviceModel settingDeviceModel7 = new SettingDeviceModel();
                settingDeviceModel7.eventType = 2;
                settingDeviceModel7.cmd = 4;
                settingDeviceModel7.name = String.format(Locale.CHINESE, "%s(id:%d)", getString(R.string.spearate_brake), (int) mantunData.id);
                settingDeviceModel7.tag = mantunData.id;
                datas.add(settingDeviceModel7);

                SettingDeviceModel settingDeviceModel8 = new SettingDeviceModel();
                settingDeviceModel8.eventType = 2;
                settingDeviceModel8.cmd = 8;
                settingDeviceModel8.name = String.format(Locale.CHINESE, "%s(id:%d)", getString(R.string.smoke_silence), (int) mantunData.id);
                settingDeviceModel8.tag = mantunData.id;
                datas.add(settingDeviceModel8);
            }
            rcMatunFire.setAdapter(matunFireAdapter);
            matunFireAdapter.updateData(datas);
            matunFireAdapter.setOnItemClickListener(new RecyclerItemClickListener() {
                @Override
                public void onItemClick(SettingDeviceModel model, int position) {
                    switch (model.eventType) {
                        case 1:
                            mSettingEnterDialogUtils.show(model.content, model.hint, model.errMsg, model.max, model.min, new SettingEnterDialogUtils.SettingEnterUtilsClickListener() {
                                @Override
                                public void onCancelClick() {
                                    mSettingEnterDialogUtils.dismiss();
                                }

                                @Override
                                public void onConfirmClick(double value) {
                                    for (SensoroMantunData mantunData : sensoroSensor.mantunDatas) {
                                        String mantunCurrentTag = mantunData.id + "currentTh";
                                        String mantunPowerTag = mantunData.id + "powerTh";
                                        model.content = String.valueOf(value);
                                        if (model.tag instanceof String) {
                                            String tag = (String) model.tag;
                                            if (mantunCurrentTag.equals(tag)) {
                                                mantunData.currentTh = (int) (value * 100);
                                            } else if (tag.equals(mantunPowerTag)) {
                                                mantunData.powerTh = (int) value;
                                            }
                                        }
                                    }
                                    matunFireAdapter.notifyDataSetChanged();
                                }
                            });
                            break;
                        case 2:
                            MsgNode1V1M5.MantunData.Builder builder = MsgNode1V1M5.MantunData.newBuilder();
                            if (model.cmd == 0 || model.cmd == 2 || model.cmd == 4 || model.cmd == 8) {
                                if (model.tag instanceof Integer) {
                                    builder.setId((Integer) model.tag);
                                    builder.setCmd(model.cmd);
                                    sensoroDeviceConnection.writeMantunCmd(builder, new SensoroWriteCallback() {
                                        @Override
                                        public void onWriteSuccess(Object o, int cmd) {
                                            showSendCmdSuccessProgressDialog();
                                        }

                                        @Override
                                        public void onWriteFailure(int errorCode, int cmd) {
                                            showSendCmdFailedProgressDialog(errorCode);
                                        }
                                    });
                                    showSendCmdProgressDialog();
                                }
                            }

                            break;
                    }
                }
            });

        } else {
            rcMatunFire.setVisibility(GONE);
        }
    }

    private void showSendCmdSuccessProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), getString(R.string.send_success), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSendCmdFailedProgressDialog(int errorCode) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), getString(R.string.send_failed) + errorCode, Toast.LENGTH_SHORT).show();
        }
    }

    private void showSendCmdProgressDialog() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage(getString(R.string.send_cmd));
            progressDialog.show();
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
                                eddyStoneSlot1Item2Layout.setVisibility(VISIBLE);
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
                                eddyStoneSlot2Item2Layout.setVisibility(VISIBLE);

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
                                eddyStoneSlot3Item2Layout.setVisibility(VISIBLE);
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
                                eddyStoneSlot4Item2Layout.setVisibility(VISIBLE);
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
                                eddyStoneSlot1Item2Layout.setVisibility(VISIBLE);
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
                                eddyStoneSlot2Item2Layout.setVisibility(VISIBLE);
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
                                eddyStoneSlot3Item2Layout.setVisibility(VISIBLE);
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
                                eddyStoneSlot4Item2Layout.setVisibility(VISIBLE);
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
                                eddyStoneSlot1Item2Layout.setVisibility(VISIBLE);
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
                                eddyStoneSlot2Item2Layout.setVisibility(VISIBLE);
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
                                eddyStoneSlot3Item2Layout.setVisibility(VISIBLE);
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
                                eddyStoneSlot4Item2Layout.setVisibility(VISIBLE);
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

            if (firmwareVersion.compareTo(SensoroDevice.FV_1_2) > 0) {
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
        } catch (Exception e) {
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
                    case CmdType.CMD_SET_CAYMAN_CMD:
                        break;
                    case CmdType.CMD_SET_ZERO:
                        Toast.makeText(SettingDeviceActivity.this, R.string.zero_calibrate_success, Toast
                                .LENGTH_SHORT).show();
                        break;
                    case CmdType.CMD_SET_BAYMAX_CMD:
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), getString(R.string.send_success), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case CmdType.CMD_CAYMAN_RESET:
                        isCaymanReset = 2; //没别的意思，只是为了区分状态，值是随便给的，不想给0，就随意给了个2
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
                    case CmdType.CMD_SET_BAYMAX_CMD:
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), getString(R.string.send_failed) + errorCode, Toast.LENGTH_SHORT).show();
                        }
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
        String password = sensoroDevice.getPassword();
        String firmwareVersion = sensoroDevice.getFirmwareVersion();
        String macAddress = sensoroDevice.getMacAddress();
        sensoroDevice = (SensoroDevice) bleDevice;
        sensoroSensor = sensoroDevice.getSensoroSensorTest();
        sensoroDevice.setPassword(password);
        sensoroDevice.setFirmwareVersion(firmwareVersion);
        sensoroDevice.setSn(sn);
        sensoroDevice.setMacAddress(macAddress);
        sensoroDevice.setHasMaxEirp(((SensoroDevice) bleDevice).hasMaxEirp());
        if (((SensoroDevice) bleDevice).hasMaxEirp()) {
            sensoroDevice.setMaxEirp(((SensoroDevice) bleDevice).getMaxEirp());
            loraEirpItems = obtainEirpItems();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                registerUiEvent();
                refresh();
                initEnterDialog();
            }
        });


    }

    private void initEnterDialog() {
        mSettingEnterDialogUtils = new SettingEnterDialogUtils(this);
    }

    @Override
    public void onConnectedFailure(int errorCode) {
        Log.v(TAG, "onConnectedFailure:" + errorCode);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isCaymanReset == 2 || isCaymanReset == 3) {
                    if (isCaymanReset == 3) {
                        //测试过程中发现，会多次调用onConnectedFailure，这里做只要是恢复出厂设置，再回调onConnectedFailure
                        //就不做处理了
                        return;
                    }
                    //恢复出厂设置后重新连接，然后更新
                    progressDialog.setMessage("正在恢复出厂设置，请稍后...");
                    try {
                        progressDialog.show();
                        sensoroDeviceConnection = new SensoroDeviceConnection(SettingDeviceActivity.this, sensoroDevice.getMacAddress());
                        sensoroDeviceConnection.connect(sensoroDevice.getPassword(), mCaymanConnectCallBack);
                        isCaymanReset = 3;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.connect_failed), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    SettingDeviceActivity.this.finish();
                }
            }
        });
    }

    private SensoroConnectionCallback mCaymanConnectCallBack = new SensoroConnectionCallback() {
        @Override
        public void onConnectedSuccess(BLEDevice bleDevice, int cmd) {
            postUpdateData05();
            Toast.makeText(getApplicationContext(), "恢复出厂设置成功", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            SettingDeviceActivity.this.finish();
        }

        @Override
        public void onConnectedFailure(int errorCode) {
            progressDialog.dismiss();
            SettingDeviceActivity.this.finish();
        }

        @Override
        public void onDisconnected() {

        }
    };

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
            MsgNode1V1M5.LpwanParam.Builder loraParamBuilder = MsgNode1V1M5.LpwanParam.newBuilder();
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
            if(sensoroDevice.hasMaxEirp()){
                loraParamBuilder.setMaxEIRP(sensoroDevice.getMaxEirp());
            }
            msgCfgBuilder.setLpwanParam(loraParamBuilder);
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
            }
            if (sensoroDevice.hasConfirm()) {
                appParamBuilder.setConfirm(sensoroDevice.getConfirm());
            }

            if (sensoroDevice.hasDemoMode()) {
                appParamBuilder.setDemoMode(sensoroDevice.getDemoMode());
            }

            if (sensoroDevice.hasBatteryBeep()) {
                appParamBuilder.setLowBatteryBeep(sensoroDevice.getBatteryBeep());
            }

            if (sensoroDevice.hasBeepMuteTime()) {
                appParamBuilder.setBeepMuteTime(sensoroDevice.getBeepMuteTime());
            }

            if (sensoroDevice.hasLedStatus()) {
                appParamBuilder.setLedStatus(sensoroDevice.getLedStatus());
            }

            msgCfgBuilder.setAppParam(appParamBuilder);


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
            if (sensoroSensor.mantunDatas != null && sensoroSensor.mantunDatas.size() > 0) {
                for (SensoroMantunData mantunData : sensoroSensor.mantunDatas) {
                    MsgNode1V1M5.MantunData.Builder builder = MsgNode1V1M5.MantunData.newBuilder();
                    if (mantunData.hasCurrentTh) {
                        builder.setCurrentTh(mantunData.currentTh);
                    }
                    if (mantunData.hasPowerTh) {
                        builder.setPowerTh(mantunData.powerTh);
                    }
                    msgCfgBuilder.addMtunData(builder);
                }
            }

            if (sensoroSensor.hasAcrelFires) {
                MsgNode1V1M5.AcrelData.Builder builder = MsgNode1V1M5.AcrelData.newBuilder();
                if (sensoroSensor.acrelFires.hasConnectSw) {
                    builder.setConnectSw(sensoroSensor.acrelFires.connectSw);
                }

                if (sensoroSensor.acrelFires.hasChEnable) {
                    builder.setChEnable(sensoroSensor.acrelFires.chEnable);
                }
                if (sensoroSensor.acrelFires.hasLeakageTh) {
                    builder.setLeakageTh(sensoroSensor.acrelFires.leakageTh);
                }
                if (sensoroSensor.acrelFires.hasPasswd) {
                    builder.setPasswd(sensoroSensor.acrelFires.passwd);
                }
                if (sensoroSensor.acrelFires.hasT1Th) {
                    int t1Th = sensoroSensor.acrelFires.t1Th;
                    builder.setT1Th(sensoroSensor.acrelFires.t1Th);
                }
                if (sensoroSensor.acrelFires.hasT2Th) {
                    builder.setT2Th(sensoroSensor.acrelFires.t2Th);
                }
                if (sensoroSensor.acrelFires.hasT3Th) {
                    builder.setT3Th(sensoroSensor.acrelFires.t3Th);
                }
                if (sensoroSensor.acrelFires.hasT4Th) {
                    builder.setT4Th(sensoroSensor.acrelFires.t4Th);
                }
                if (sensoroSensor.acrelFires.hasPasswd) {
                    builder.setPasswd(sensoroSensor.acrelFires.passwd);
                }
                if (sensoroSensor.acrelFires.hasValHighSet) {
                    builder.setValHighSet(sensoroSensor.acrelFires.valHighSet);
                }
                if (sensoroSensor.acrelFires.hasValLowSet) {
                    builder.setValLowSet(sensoroSensor.acrelFires.valLowSet);
                }
                if (sensoroSensor.acrelFires.hasCurrHighSet) {
                    builder.setCurrHighSet(sensoroSensor.acrelFires.currHighSet);
                }

                if (sensoroSensor.acrelFires.hasValHighType) {
                    builder.setValHighType(sensoroSensor.acrelFires.valHighType);
                }
                if (sensoroSensor.acrelFires.hasValLowType) {
                    builder.setValLowType(sensoroSensor.acrelFires.valLowType);
                }
                if (sensoroSensor.acrelFires.hasCurrHighType) {
                    builder.setCurrHighType(sensoroSensor.acrelFires.currHighType);
                }
                if (sensoroSensor.acrelFires.hasIct) {
                    builder.setIct(sensoroSensor.acrelFires.ict);
                }
                if (sensoroSensor.acrelFires.hasCt) {
                    builder.setCt(sensoroSensor.acrelFires.ct);
                }
//            if (sensoroSensor.acrelFires.hasCmd) {
                builder.setCmd(sensoroSensor.acrelFires.cmd);
//            }
                msgCfgBuilder.setAcrelData(builder);

            }

            //嘉德 自研烟感
            if (sensoroSensor.hasCayMan) {
                MsgNode1V1M5.Cayman.Builder builder = MsgNode1V1M5.Cayman.newBuilder();
                if (sensoroSensor.cayManData.hasIsSmoke) {
                    builder.setIsSmoke(sensoroSensor.cayManData.isSmoke);
                }
                if (sensoroSensor.cayManData.hasIsMoved) {
                    builder.setIsMoved(sensoroSensor.cayManData.isMoved);
                }
                if (sensoroSensor.cayManData.hasValueOfTem) {
                    builder.setValueOfTem(sensoroSensor.cayManData.valueOfTem);
                }
                if (sensoroSensor.cayManData.hasValueOfHum) {
                    builder.setValueOfHum(sensoroSensor.cayManData.valueOfHum);
                }
                if (sensoroSensor.cayManData.hasAlarmOfHighTem) {
                    builder.setAlarmOfHighTem(sensoroSensor.cayManData.alarmOfHighTem);
                }
                if (sensoroSensor.cayManData.hasAlarmOfLowTem) {
                    builder.setAlarmOfLowTem(sensoroSensor.cayManData.alarmOfLowTem);
                }
                if (sensoroSensor.cayManData.hasAlarmOfHighHum) {
                    builder.setAlarmOfHighHum(sensoroSensor.cayManData.alarmOfHighHum);
                }
                if (sensoroSensor.cayManData.hasAlarmOfLowHum) {
                    builder.setAlarmOfLowHum(sensoroSensor.cayManData.alarmOfLowHum);
                }
                if (sensoroSensor.cayManData.hasCmd) {
                    builder.setCmd(sensoroSensor.cayManData.cmd);
                }
                if (sensoroSensor.cayManData.hasHumanDetectionTime) {
                    builder.setHumanDetectionTime(sensoroSensor.cayManData.humanDetectionTime);
                }
                if (sensoroSensor.cayManData.hasDefenseMode) {
                    builder.setDefenseMode(sensoroSensor.cayManData.defenseMode);
                }
                if (sensoroSensor.cayManData.hasDefenseTimerMode) {
                    builder.setDefenseTimerMode(sensoroSensor.cayManData.defenseTimerMode);
                }
                if (sensoroSensor.cayManData.hasDefenseModeStartTime) {
                    builder.setDefenseModeStartTime(sensoroSensor.cayManData.defenseModeStartTime);
                }
                if (sensoroSensor.cayManData.hasDefenseModeStopTime) {
                    builder.setDefenseModeStopTime(sensoroSensor.cayManData.defenseModeStopTime);
                }
                if (sensoroSensor.cayManData.hasInvadeAlarm) {
                    builder.setInvadeAlarm(sensoroSensor.cayManData.invadeAlarm);
                }
                msgCfgBuilder.setCaymanData(builder);

            }

            //baymax ch4 lpg
            if (sensoroSensor.hasBaymax) {
                MsgNode1V1M5.Baymax.Builder builder = MsgNode1V1M5.Baymax.newBuilder();
                if (sensoroSensor.baymax.hasGasDevClass) {
                    builder.setGasDevClass(sensoroSensor.baymax.gasDevClass);
                }
                if (sensoroSensor.baymax.hasGasDensity) {
                    builder.setGasDensity(sensoroSensor.baymax.gasDensity);
                }
                if (sensoroSensor.baymax.hasGasDensityL1) {
                    builder.setGasDensityL1(sensoroSensor.baymax.gasDensityL1);
                }
                if (sensoroSensor.baymax.hasGasDensityL2) {
                    builder.setGasDensityL2(sensoroSensor.baymax.gasDensityL2);
                }
                if (sensoroSensor.baymax.hasGasDensityL3) {
                    builder.setGasDensityL3(sensoroSensor.baymax.gasDensityL3);
                }
                if (sensoroSensor.baymax.hasGasDisassembly) {
                    builder.setGasDisassembly(sensoroSensor.baymax.gasDisassembly);
                }
                if (sensoroSensor.baymax.hasGasLosePwr) {
                    builder.setGasLosePwr(sensoroSensor.baymax.gasLosePwr);
                }
                if (sensoroSensor.baymax.hasGasEMValve) {
                    builder.setGasEMValve(sensoroSensor.baymax.gasEMValve);
                }
                if (sensoroSensor.baymax.hasGasDeviceStatus) {
                    builder.setGasDeviceStatus(sensoroSensor.baymax.gasDeviceStatus);
                }
                if (sensoroSensor.baymax.hasGasDeviceOpState) {
                    builder.setGasDeviceOpState(sensoroSensor.baymax.gasDeviceOpState);
                }
                if (sensoroSensor.baymax.hasGasDeviceComsDown) {
                    builder.setGasDeviceComsDown(sensoroSensor.baymax.gasDeviceComsDown);
                }
                if (sensoroSensor.baymax.hasGasDeviceCMD) {
                    builder.setGasDeviceCMD(sensoroSensor.baymax.gasDeviceCMD);
                }
                if (sensoroSensor.baymax.hasGasDeviceSilentMode) {
                    builder.setGasDeviceSilentMode(sensoroSensor.baymax.gasDeviceSilentMode);
                }
                msgCfgBuilder.setBaymaxData(builder);
            }
            if (sensoroSensor.hasIbeacon) {
                MsgNode1V1M5.iBeacon.Builder builder = MsgNode1V1M5.iBeacon.newBuilder();
                if (sensoroSensor.ibeacon.hasUuid) {
                    builder.setUuid(sensoroSensor.ibeacon.uuid);
                }
                if (sensoroSensor.ibeacon.hasMajor) {
                    builder.setMajor(sensoroSensor.ibeacon.major);
                }
                if (sensoroSensor.ibeacon.hasMinor) {
                    builder.setMinor(sensoroSensor.ibeacon.minor);
                }
                if (sensoroSensor.ibeacon.hasMrssi) {
                    builder.setMrssi(sensoroSensor.ibeacon.mrssi);
                }
                msgCfgBuilder.setIbeacon(builder);
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


    public static String timeZoneConvert(int defenseModeStartTime) {
        String displayName = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
        if (displayName.contains("+")) {
            String[] split = displayName.split("\\+");
            if (split.length > 1) {
                String[] split1 = split[1].split(":");
                if (split1.length > 1) {
                    try {
                        int hour = Integer.parseInt(split1[0]);
                        int minute = Integer.parseInt(split1[1]);

                        int time = defenseModeStartTime + hour * 60 + minute;

                        int realHour = time / 60;

                        if (realHour > 24) {
                            realHour = realHour - 24;
                        }
                        int realMinute = time % 60;
                        return String.format(Locale.ROOT, "%02d:%02d", realHour, realMinute);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return timeZoneBeijing(defenseModeStartTime);
                    }
                } else {
                    timeZoneBeijing(defenseModeStartTime);
                }
            } else {
                //默认按东八区计算
                return timeZoneBeijing(defenseModeStartTime);
            }

        } else if (displayName.contains("-")) {
            String[] split = displayName.split("-");
            if (split.length > 1) {
                String[] split1 = split[1].split(":");
                if (split1.length > 1) {
                    try {
                        int hour = Integer.parseInt(split1[0]);
                        int minute = Integer.parseInt(split1[1]);

                        int time = defenseModeStartTime - hour * 60 - minute;
                        if (time < 0) {
                            time = time + 24 * 60;
                        }
                        int realHour = time / 60;

                        if (realHour > 24) {
                            realHour = realHour - 24;
                        }
                        int realMinute = time % 60;
                        return String.format(Locale.ROOT, "%02d:%02d", realHour, realMinute);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return timeZoneBeijing(defenseModeStartTime);
                    }
                } else {
                    timeZoneBeijing(defenseModeStartTime);
                }
            } else {
                //默认按东八区计算
                return timeZoneBeijing(defenseModeStartTime);
            }
        } else {
            return timeZoneBeijing(defenseModeStartTime);
        }
        return timeZoneBeijing(defenseModeStartTime);
    }

    private static String timeZoneBeijing(int defenseModeStartTime) {

        int realHour = (defenseModeStartTime + 8 * 60) / 60;

        if (realHour > 24) {
            realHour = realHour - 24;
        }
        int realMinute = defenseModeStartTime % 60;
        StringBuffer sb = new StringBuffer(realHour).append(":").append(realMinute);
        return String.format(Locale.ROOT, "%02d:%02d", realHour, realMinute);
    }

    private void initCustomOptionPicker() {//条件选择器初始化，自定义布局
        pvCustomOptions = new OptionsPickerBuilder(SettingDeviceActivity.this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
//                mPresenter.doSelectComplete(options1, options2, options3 + 1);
                switch (picekerViewStatus) {
                    case 1:
                        //嘉德自研烟感定时设防开始时间
                        doCaymanDefenseModeStartTime(options1, options2, options3);
                        break;
                    case 2:
                        //嘉德自研烟感定时设防开始时间
                        doCaymanDefenseModeStopTime(options1, options2, options3);

                        break;
                    default:
                        break;
                }
            }
        }).setTitleText("选择时间")
                .setContentTextSize(20)//设置滚轮文字大小
                .setDividerColor(getResources().getColor(R.color.c_dfdfdf))//设置分割线的颜色
                .setBgColor(getResources().getColor(R.color.white))
                .setTitleBgColor(getResources().getColor(R.color.c_f4f4f4))
                .setTitleColor(getResources().getColor(R.color.c_252525))
                .setTextColorCenter(getResources().getColor(R.color.c_252525))
                .setTextColorOut(Color.parseColor("#A6A6A6"))
                .setOutSideColor(Color.parseColor("#B3000000"))
                .setCancelColor(getResources().getColor(R.color.actionbar_bg))
                .setSubmitColor(getResources().getColor(R.color.actionbar_bg))
                .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
                .isCenterLabel(false)//是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setOutSideCancelable(true)
                .setCyclic(true, true, false)
                .setLineSpacingMultiplier(2.0f)
                .build();
//        pickerHours = new ArrayList<String>(24){
//            {
//                add("00");add("01");add("02");add("03");add("04");add("05");add("06");add("07");add("08");add("09");
//                add("12");add("11");add("13");add("14");add("15");add("16");add("17");add("18");add("19");add("20");
//                add("21");add("22");add("23");
//            }
//        };

        pickerHours = new ArrayList<Integer>(24) {
            {
                add(0);
                add(1);
                add(2);
                add(3);
                add(4);
                add(5);
                add(6);
                add(7);
                add(8);
                add(9);
                add(12);
                add(11);
                add(13);
                add(14);
                add(15);
                add(16);
                add(17);
                add(18);
                add(19);
                add(20);
                add(21);
                add(22);
                add(23);
            }
        };
        pickerMinutes = new ArrayList<Integer>(60) {
            {
                add(0);
                add(1);
                add(2);
                add(3);
                add(4);
                add(5);
                add(6);
                add(7);
                add(8);
                add(9);
                add(12);
                add(11);
                add(13);
                add(14);
                add(15);
                add(16);
                add(17);
                add(18);
                add(19);
                add(20);
                add(21);
                add(22);
                add(23);
                add(25);
                add(26);
                add(27);
                add(28);
                add(29);
                add(30);
                add(31);
                add(32);
                add(33);
                add(34);
                add(35);
                add(36);
                add(37);
                add(38);
                add(39);
                add(40);
                add(41);
                add(42);
                add(43);
                add(44);
                add(45);
                add(46);
                add(47);
                add(48);
                add(49);
                add(50);
                add(51);
                add(52);
                add(53);
                add(54);
                add(55);
                add(56);
                add(57);
                add(58);
                add(59);
            }
        };

//        pickerMinutes = new ArrayList<Integer>(60){
//            {
//                add("00");add("01");add("02");add("03");add("04");add("05");add("06");add("07");add("08");add("09");
//                add("12");add("11");add("13");add("14");add("15");add("16");add("17");add("18");add("19");add("20");
//                add("21");add("22");add("23");add("25");add("26");add("27");add("28");add("29");add("30");add("31");
//                add("32");add("33");add("34");
//                add("35");add("36");add("37");add("38");add("39");add("40");add("41");add("42");add("43");add("44");
//                add("45");add("46");add("47");add("48");add("49");add("50");add("51");add("52");add("53");add("54");
//                add("55");add("56");add("57");
//                add("58");add("59");
//            }
//        };

        pvCustomOptions.setNPicker(pickerHours, pickerMinutes, null);
        pvCustomOptions.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(Object o) {
//                mPresenter.onPickerViewDismiss();
            }
        });

    }

    private void doCaymanDefenseModeStopTime(int options1, int options2, int options3) {
        Integer hour = pickerHours.get(options1);
        Integer minute = pickerMinutes.get(options2);

        int i = caymanTimeConvertMinute(hour, minute);
        if (i == -1) {
            Toast.makeText(this, "输入非法，请重新输入", Toast.LENGTH_SHORT).show();
            return;

        }
        if (i < 0 || i > 1439) {
            Toast.makeText(this, "定时设防结束时间范围为1-1439", Toast.LENGTH_SHORT).show();
            return;
        }
        sensoroSensor.cayManData.defenseModeStopTime = i;
        caymanDefenseModeStopTimeContent.setText(String.format(Locale.ROOT, "%02d:%02d", hour, minute));
    }

    private void doCaymanDefenseModeStartTime(int options1, int options2, int options3) {
        Integer hour = pickerHours.get(options1);
        Integer minute = pickerMinutes.get(options2);

        int i = caymanTimeConvertMinute(hour, minute);
        if (i == -1) {
            Toast.makeText(this, "输入非法，请重新输入", Toast.LENGTH_SHORT).show();
            return;

        }
        if (i < 0 || i > 1439) {
            Toast.makeText(this, "定时设防结束时间范围为1-1439", Toast.LENGTH_SHORT).show();
            return;
        }
        sensoroSensor.cayManData.defenseModeStartTime = i;
        caymanDefenseModeStartTimeContent.setText(String.format(Locale.ROOT, "%02d:%02d", hour, minute));
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

    protected void doMantunControl(int cmd) {
        int sendCmd = 1;
        switch (cmd) {
            case CmdType.CMD_MANTUN_SWITCH_IN:
                Toast.makeText(application, "合闸", Toast.LENGTH_SHORT).show();
                sendCmd = 1;
                break;
            case CmdType.CMD_MANTUN_SWITCH_ON:
                Toast.makeText(application, "分闸", Toast.LENGTH_SHORT).show();
                sendCmd = 2;
                break;
            case CmdType.CMD_MANTUN_SELF_CHICK:
                Toast.makeText(application, "自检", Toast.LENGTH_SHORT).show();
                sendCmd = 4;
                break;
            case CmdType.CMD_MANTUN_ZERO_POWER:
                Toast.makeText(application, "电量清零", Toast.LENGTH_SHORT).show();
                sendCmd = 8;
                break;
            case CmdType.CMD_MANTUN_RESTORE:
                Toast.makeText(application, "恢复出厂", Toast.LENGTH_SHORT).show();
                sendCmd = 16;
                break;
        }
        MsgNode1V1M5.MantunData.Builder builder = MsgNode1V1M5.MantunData.newBuilder();
        builder.setCmd(sendCmd);
        sensoroDeviceConnection.writeMantunCmd(builder, this);
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
        } else if (SETTINGS_DEVICE_RL_APP_LOW_BATTERY_BEEP.equals(tag)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            String item = bundle.getString(SettingsSingleChoiceItemsFragment.ITEM);
            sensoroDevice.setBatteryBeep(index);
            batteryBeepTextView.setText(item);
        } else if (SETTINGS_DEVICE_RL_APP_DEMO.equals(tag)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            String item = bundle.getString(SettingsSingleChoiceItemsFragment.ITEM);
            sensoroDevice.setDemoMode(index);
            demoTextView.setText(item);
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
        } else if (SETTINGS_DEVICE_RL_ACREL_LEAKAGE.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 20 || i > 1000) {
                    Toast.makeText(this, "漏电阈值范围为20-1000", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.acrelFires.leakageTh = i;
                acrelLeakageThContent.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_ACREL_CONNECT_SW.equals(tag)) {
            ArrayList<ChannelData> channelDataArrayList = (ArrayList) bundle.getSerializable(SettingsMultiChoiceItemsDialogFragment.RESULT);
            ArrayList<ChannelData> channelOpenList = channelDataArrayList;
            StringBuffer stringBuffer = new StringBuffer();
            byte[] bytes = new byte[channelOpenList.size()];
            for (int i = 0; i < channelOpenList.size(); i++) {
                ChannelData channelData = channelOpenList.get(i);
                if (channelData.isOpen()) {
                    stringBuffer.append(getString(R.string.setting_text_channel) + channelData.getIndex() + "关联继电器，");
                    bytes[i] = 1;
                } else {
                    bytes[i] = 0;
                }
            }
            sensoroSensor.acrelFires.connectSw = SensoroUUID.bitsToInt(bytes);
            acrelConnectSwContent.setText(stringBuffer.toString());
        } else if (SETTINGS_DEVICE_RL_ACREL_CH_ENABLE.equals(tag)) {
            ArrayList<ChannelData> channelDataArrayList = (ArrayList) bundle.getSerializable(SettingsMultiChoiceItemsDialogFragment.RESULT);
            ArrayList<ChannelData> channelOpenList = channelDataArrayList;
            StringBuffer stringBuffer = new StringBuffer();
            byte[] bytes = new byte[channelOpenList.size()];
            for (int i = 0; i < channelOpenList.size(); i++) {
                ChannelData channelData = channelOpenList.get(i);
                if (channelData.isOpen()) {
                    stringBuffer.append(getString(R.string.setting_text_channel) + channelData.getIndex() + "关联继电器，");
                    bytes[i] = 1;
                } else {
                    bytes[i] = 0;
                }
            }
            sensoroSensor.acrelFires.chEnable = SensoroUUID.bitsToInt(bytes);
            acrelChEnableContent.setText(stringBuffer.toString());
        } else if (SETTINGS_DEVICE_RL_ACREL_T1_TH.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 45 || i > 140) {
                    Toast.makeText(this, "温度阈值范围为45-140", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.acrelFires.t1Th = i;
                acrelT1ThContent.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_ACREL_T2_TH.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 45 || i > 140) {
                    Toast.makeText(this, "温度阈值范围为45-140", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.acrelFires.t2Th = i;
                acrelT2ThContent.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_ACREL_T3_TH.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 45 || i > 140) {
                    Toast.makeText(this, "温度阈值范围为45-140", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.acrelFires.t3Th = i;
                acrelT3ThContent.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_ACREL_PSD.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 1 || i > 10000) {
                    Toast.makeText(this, "密码范围为1-9999", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.acrelFires.passwd = i;
                acrelPsdContent.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "密码为数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_CAYMAN_HUMAN_DETECTION_TIME.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 1 || i > 30) {
                    Toast.makeText(this, "时间范围为1-30", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.cayManData.humanDetectionTime = i;
                caymanHumanDetctionTimeContent.setText(i + "s");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "范围为数字格式", Toast.LENGTH_SHORT).show();
            }
        }  else if (SETTINGS_DEVICE_RL_APP_BEEP_MUTE_TIME.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 0) {
                    Toast.makeText(this, "消音时间范围1-30分钟", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroDeviceConnection.writeAppBeepMuteTime(i, new SensoroWriteCallback() {
                    @Override
                    public void onWriteSuccess(Object o, int cmd) {
                        sensoroDevice.setBeepMuteTime(i);
                        Toast.makeText(SettingDeviceActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onWriteFailure(int errorCode, int cmd) {
                        Toast.makeText(SettingDeviceActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入数字", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_ACREL_T4_TH.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 45 || i > 140) {
                    Toast.makeText(this, "温度阈值范围为45-140", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.acrelFires.t4Th = i;
                acrelT4ThContent.setText(i + "");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_ACREL_CURR_HIGH_SET.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 0.1 || i > 150.0) {
                    Toast.makeText(this, "过流阈值范围为0.1%-150%", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.acrelFires.currHighSet = i * 10;
                acrelCurrHighSetContent.setText(i + "%");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_ACREL_VAL_HIGH_SET.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 100.0 || i > 140.0) {
                    Toast.makeText(this, "过压阈值范围为100.0-140.0", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.acrelFires.valHighSet = i * 10;
                acrelValHighSetContent.setText(i + "%");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_ACREL_VAL_LOW_SET.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 60.0 || i > 100.0) {
                    Toast.makeText(this, "欠压阈值范围为60.0-100.0", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.acrelFires.valLowSet = i * 10;
                acrelValLowSetContent.setText(i + "%");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_ACREL_VAL_HIGH_TYPE.equals(tag)) {
            ArrayList<ChannelData> channelDataArrayList = (ArrayList) bundle.getSerializable(SettingsMultiChoiceItemsDialogFragment.RESULT);
            ArrayList<ChannelData> channelOpenList = channelDataArrayList;
            StringBuffer stringBuffer = new StringBuffer();
            byte[] bytes = new byte[channelOpenList.size()];
            for (int i = 0; i < channelOpenList.size(); i++) {
                ChannelData channelData = channelOpenList.get(i);
                if (channelData.isOpen()) {
                    bytes[i] = 1;
                    switch (i) {
                        case 0:
                            stringBuffer.append("保护开关 开，");
                            break;
                        case 1:
                            stringBuffer.append("保护关联 DO1 开，");
                            break;
                    }
                } else {
                    bytes[i] = 0;
                }
            }

            sensoroSensor.acrelFires.valHighType = SensoroUUID.bitsToInt(bytes);
            acrelValHighTypeContent.setText(stringBuffer.toString());
        } else if (SETTINGS_DEVICE_RL_ACREL_VAL_Low_TYPE.equals(tag)) {
            ArrayList<ChannelData> channelDataArrayList = (ArrayList) bundle.getSerializable(SettingsMultiChoiceItemsDialogFragment.RESULT);
            ArrayList<ChannelData> channelOpenList = channelDataArrayList;
            StringBuffer stringBuffer = new StringBuffer();
            byte[] bytes = new byte[channelOpenList.size()];
            for (int i = 0; i < channelOpenList.size(); i++) {
                ChannelData channelData = channelOpenList.get(i);
                if (channelData.isOpen()) {
                    bytes[i] = 1;
                    switch (i) {
                        case 0:
                            stringBuffer.append("保护开关 开，");
                            break;
                        case 1:
                            stringBuffer.append("保护关联 DO1 开，");
                            break;
                    }
                } else {
                    bytes[i] = 0;
                }
            }

            sensoroSensor.acrelFires.valLowType = SensoroUUID.bitsToInt(bytes);
            acrelValLowTypeContent.setText(stringBuffer.toString());
        } else if (SETTINGS_DEVICE_RL_ACREL_CURR_HIGH_TYPE.equals(tag)) {
            ArrayList<ChannelData> channelDataArrayList = (ArrayList) bundle.getSerializable(SettingsMultiChoiceItemsDialogFragment.RESULT);
            ArrayList<ChannelData> channelOpenList = channelDataArrayList;
            StringBuffer stringBuffer = new StringBuffer();
            byte[] bytes = new byte[channelOpenList.size()];
            for (int i = 0; i < channelOpenList.size(); i++) {
                ChannelData channelData = channelOpenList.get(i);
                if (channelData.isOpen()) {
                    bytes[i] = 1;
                    switch (i) {
                        case 0:
                            stringBuffer.append("保护开关 开，");
                            break;
                        case 1:
                            stringBuffer.append("保护关联 DO1 开，");
                            break;
                    }
                } else {
                    bytes[i] = 0;
                }
            }

            sensoroSensor.acrelFires.currHighType = SensoroUUID.bitsToInt(bytes);
            acrelCurrHighTypeContent.setText(stringBuffer.toString());
        } else if (SETTINGS_DEVICE_RL_CAYMAN_ALARM_OF_HIGH_TEM.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                float i = Float.parseFloat(temp);
                if (i < -40 || i > 140) {
                    Toast.makeText(this, "高温度阈值范围为-40-140", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.cayManData.alarmOfHighTem = (int) (i * 10);
                caymanAlarmOfHighTemContent.setText(i + "℃");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_CAYMAN_ALARM_OF_LOW_TEM.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                float i = Float.parseFloat(temp);
                if (i < -40 || i > 140) {
                    Toast.makeText(this, "低温度阈值范围为-40-140", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.cayManData.alarmOfLowTem = (int) (i * 10);
                caymanAlarmOfLowTemContent.setText(i + "℃");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_CAYMAN_ALARM_OF_HIGH_HUM.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                float i = Float.parseFloat(temp);
                if (i < 0 || i > 100) {
                    Toast.makeText(this, "湿度阈值范围为0-100", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.cayManData.alarmOfHighHum = (int) (i * 10);
                caymanAlarmOfHighHumContent.setText(i + "%");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_CAYMAN_ADV_TYPE.equals(tag)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            sensoroSensor.cayManData.bleAdvType = index;
            caymanBleAdvTypeContent.setText(index == 0 ? "持续广播" : "间断广播");
        } else if (SETTINGS_DEVICE_RL_CAYMAN_DEFENSE_MODE.equals(tag)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            sensoroSensor.cayManData.defenseMode = index;
            caymanDefenseModeContent.setText(index == 0 ? "关闭" : "开启");
        } else if (SETTINGS_DEVICE_RL_CAYMAN_DEFENSE_TIMER_MODE.equals(tag)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            sensoroSensor.cayManData.defenseTimerMode = index;
            caymanDefenseTimerModeContent.setText(index == 0 ? "关闭" : "开启");
        } else if (SETTINGS_DEVICE_RL_CAYMAN_INVADE_ALARM.equals(tag)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            sensoroSensor.cayManData.invadeAlarm = index;
            caymanInvadeAlarmContent.setText(index == 0 ? "关闭" : "开启");
        } else if (SETTINGS_DEVICE_APP_LED_STATUS.equals(tag)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            sensoroDevice.setLedStatus(index);
            ledStatusTextView.setText(index == 0 ? "关闭" : "开启");
        } else if (SETTINGS_DEVICE_RL_CAYMAN_ALARM_OF_LOW_HUM.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                float i = Float.parseFloat(temp);
                if (i < 0 || i > 100) {
                    Toast.makeText(this, "湿度阈值范围为0-100", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.cayManData.alarmOfLowHum = (int) (i * 10);
                caymanAlarmOfLowHumContent.setText(i + "%");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_BAYMAX_GAS_DENSITY_L1.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 0 || i > 20) {
                    Toast.makeText(this, "一级浓度阈值范围为4-18", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.baymax.gasDensityL1 = i;
                baymaxDensityL1Content.setText(i + "%");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_BAYMAX_GAS_DENSITY_L2.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 0 || i > 20) {
                    Toast.makeText(this, "二级浓度阈值范围为4-18", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.baymax.gasDensityL2 = i;
                baymaxDensityL2Content.setText(i + "%");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        } else if (SETTINGS_DEVICE_RL_BAYMAX_GAS_DENSITY_L3.equals(tag)) {
            String temp = bundle.getString(SettingsInputDialogFragment.INPUT);
            try {
                int i = Integer.parseInt(temp);
                if (i < 0 || i > 20) {
                    Toast.makeText(this, "三级浓度阈值范围为4-18", Toast.LENGTH_SHORT).show();
                    return;
                }
                sensoroSensor.baymax.gasDensityL3 = i;
                baymaxDensityL3Content.setText(i + "%");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private int caymanTimeConvertMinute(int hour, int minute) {
        try {

            String displayName = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
            if (displayName.contains("+")) {
                String[] split1 = displayName.split("\\+");
                if (split1.length > 1) {
                    String[] split2 = split1[1].split(":");
                    if (split2.length > 1) {
                        int zoneHour = Integer.parseInt(split2[0]);
                        int zoneMinute = Integer.parseInt(split2[1]);

                        int time = hour * 60 + minute - zoneHour * 60 - zoneMinute;

                        if (time < 0) {
                            time = time + 1440;
                        }
                        return time;
                    }
                }
            } else if (displayName.contains("-")) {
                String[] split1 = displayName.split("-");
                if (split1.length > 1) {
                    String[] split2 = split1[1].split(":");
                    if (split2.length > 1) {
                        int zoneHour = Integer.parseInt(split2[0]);
                        int zoneMinute = Integer.parseInt(split2[1]);

                        int time = hour * 60 + minute + zoneHour * 60 + zoneMinute;

                        if (time > 1440) {
                            time = time - 1440;
                        }
                        return time;
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();

        }
        return -1;
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (sensoroDeviceConnection != null) {
            sensoroDeviceConnection.disconnect();
        }

        if (mSettingEnterDialogUtils != null) {
            mSettingEnterDialogUtils.destroy();
        }
        super.onDestroy();
    }

    @OnClick({R.id.acrel_leakage_th,
            R.id.acrel_connect_sw, R.id.acrel_ch_enable, R.id.acrel_t1_th, R.id.acrel_t2_th,
            R.id.acrel_t3_th, R.id.acrel_t4_th, R.id.acrel_curr_high_set, R.id.acrel_val_high_set,
            R.id.acrel_val_low_set, R.id.acrel_val_high_type, R.id.acrel_val_low_type,
            R.id.acrel_curr_high_type, R.id.acrel_cmd_reset, R.id.acrel_cmd_self_check, R.id.acrel_cmd_query,
            R.id.acrel_cmd_mute, R.id.acrel_cmd_zero_clearing, R.id.acrel_root,
            R.id.acrel_psd, R.id.cayman_is_smoke, R.id.cayman_is_moved, R.id.cayman_value_of_tem, R.id.cayman_value_of_photor,
            R.id.cayman_ble_adv_type, R.id.cayman_ble_adv_start_time, R.id.cayman_ble_adv_end_time_hum, R.id.cayman_value_of_batb,
            R.id.cayman_human_detection_time, R.id.cayman_defense_mode, R.id.cayman_defense_timer_mode,
            R.id.cayman_defense_mode_start_time, R.id.cayman_defense_mode_stop_time, R.id.cayman_invade_alarm,
            R.id.cayman_value_of_hum, R.id.cayman_alarm_of_high_tem, R.id.cayman_alarm_of_low_tem,
            R.id.cayman_alarm_of_high_hum, R.id.settings_device_rl_app_led_status, R.id.cayman_alarm_of_low_hum, R.id.cayman_cmd_self_check,
            R.id.cayman_cmd_reset, R.id.cayman_cmd_clear_sound, R.id.settings_device_rl_app_beep_mute_time, R.id.baymax_density_l1, R.id.baymax_density_l2, R.id.baymax_density_l3,
            R.id.baymax_cmd_close_electronic_valve, R.id.baymax_cmd_self_check, R.id.baymax_cmd_reset, R.id.baymax_cmd_mute,
            R.id.settings_device_rl_app_battery_beep, R.id.settings_device_rl_app_demo})
    public void onViewClicked(View view) {
        DialogFragment dialogFragment;
        switch (view.getId()) {
            case R.id.acrel_leakage_th:
                int acrelLeakageTh = sensoroSensor.acrelFires.leakageTh;
                dialogFragment = SettingsInputDialogFragment.newInstance(acrelLeakageTh + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_LEAKAGE);
                break;
            case R.id.acrel_connect_sw:
                int connectSw = sensoroSensor.acrelFires.connectSw;
                byte[] bytes1 = SensoroUUID.intToBits(connectSw, 5);
                ArrayList<ChannelData> channelOpenList = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    ChannelData channelData = new ChannelData();
                    channelData.setIndex(i + 1);
                    try {
                        if (bytes1[i] == 1) {
                            channelData.setOpen(true);
                        } else {
                            channelData.setOpen(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        channelData.setOpen(false);
                    }
                    channelOpenList.add(channelData);
                }

                dialogFragment = SettingsMultiChoiceItemsDialogFragment.newInstance(channelOpenList);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_CONNECT_SW);
                break;
            case R.id.acrel_ch_enable:
                int chEnable = sensoroSensor.acrelFires.chEnable;
                byte[] chEnableBytes = SensoroUUID.intToBits(chEnable, 5);
                ArrayList<ChannelData> chEableList = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    ChannelData channelData = new ChannelData();
                    channelData.setIndex(i + 1);
                    try {
                        if (chEnableBytes[i] == 1) {
                            channelData.setOpen(true);
                        } else {
                            channelData.setOpen(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        channelData.setOpen(false);
                    }
                    chEableList.add(channelData);
                }
                dialogFragment = SettingsMultiChoiceItemsDialogFragment.newInstance(chEableList);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_CH_ENABLE);
                break;
            case R.id.acrel_t1_th:
                int t1Th = sensoroSensor.acrelFires.t1Th;
                dialogFragment = SettingsInputDialogFragment.newInstance(t1Th + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_T1_TH);
                break;
            case R.id.acrel_t2_th:
                int t2Th = sensoroSensor.acrelFires.t2Th;
                dialogFragment = SettingsInputDialogFragment.newInstance(t2Th + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_T2_TH);
                break;
            case R.id.acrel_t3_th:
                int t3Th = sensoroSensor.acrelFires.t3Th;
                dialogFragment = SettingsInputDialogFragment.newInstance(t3Th + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_T3_TH);
                break;
            case R.id.acrel_t4_th:
                int t4Th = sensoroSensor.acrelFires.t4Th;
                dialogFragment = SettingsInputDialogFragment.newInstance(t4Th + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_T4_TH);
                break;
            case R.id.acrel_curr_high_set:
                float currHighSet = sensoroSensor.acrelFires.currHighSet / 10;
                String format = String.format(Locale.CHINA, "%.1f", currHighSet);
                dialogFragment = SettingsInputDialogFragment.newInstance(format);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_CURR_HIGH_SET);
                break;
            case R.id.acrel_val_high_set:
                float valHighSet = sensoroSensor.acrelFires.valHighSet / 10;
                String valHighSetFormat = String.format(Locale.CHINA, "%.1f", valHighSet);
                dialogFragment = SettingsInputDialogFragment.newInstance(valHighSetFormat);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_VAL_HIGH_SET);
                break;
            case R.id.acrel_val_low_set:
                float valLowSet = sensoroSensor.acrelFires.valLowSet / 10;
                String valLowSetFormat = String.format(Locale.CHINA, "%.1f", valLowSet);
                dialogFragment = SettingsInputDialogFragment.newInstance(valLowSetFormat);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_VAL_LOW_SET);
                break;
            case R.id.acrel_val_high_type:
                int valHighType = sensoroSensor.acrelFires.valHighType;
                byte[] valHighTypeByte = SensoroUUID.intToBits(valHighType, 2);
                ArrayList<ChannelData> valHighTypeList = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    ChannelData channelData = new ChannelData();
                    channelData.setIndex(i);
                    try {
                        if (valHighTypeByte[i] == 1) {
                            channelData.setOpen(true);
                        } else {
                            channelData.setOpen(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        channelData.setOpen(false);
                    }
                    valHighTypeList.add(channelData);
                }
                dialogFragment = SettingsMultiChoiceItemsDialogFragment.newInstance(valHighTypeList);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_VAL_HIGH_TYPE);
                break;
            case R.id.acrel_val_low_type:
                int valLowType = sensoroSensor.acrelFires.valLowType;
                byte[] valLowTypeByte = SensoroUUID.intToBits(valLowType, 2);
                ArrayList<ChannelData> valLowTypeList = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    ChannelData channelData = new ChannelData();
                    channelData.setIndex(i);
                    try {
                        if (valLowTypeByte[i] == 1) {
                            channelData.setOpen(true);
                        } else {
                            channelData.setOpen(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        channelData.setOpen(false);
                    }
                    valLowTypeList.add(channelData);
                }
                dialogFragment = SettingsMultiChoiceItemsDialogFragment.newInstance(valLowTypeList);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_VAL_Low_TYPE);
                break;
            case R.id.acrel_curr_high_type:
                int currHighType = sensoroSensor.acrelFires.currHighType;
                byte[] currHighTypeByte = SensoroUUID.intToBits(currHighType, 2);
                ArrayList<ChannelData> currHighTypeList = new ArrayList<>();
                for (int i = 0; i < 2; i++) {
                    ChannelData channelData = new ChannelData();
                    channelData.setIndex(i);
                    try {
                        if (currHighTypeByte[i] == 1) {
                            channelData.setOpen(true);
                        } else {
                            channelData.setOpen(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        channelData.setOpen(false);
                    }
                    currHighTypeList.add(channelData);
                }
                dialogFragment = SettingsMultiChoiceItemsDialogFragment.newInstance(currHighTypeList);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_CURR_HIGH_TYPE);
                break;
            case R.id.acrel_cmd_reset:
                doAcrelControl(CmdType.CMD_ACREL_RESET);
                break;
            case R.id.acrel_cmd_self_check:
                doAcrelControl(CmdType.CMD_ACREL_SELF_CHECK);
                break;
            case R.id.acrel_cmd_query:
                doAcrelControl(CmdType.CMD_ACREL_QUERY);
                break;
            case R.id.acrel_cmd_mute:
                doAcrelControl(CmdType.CMD_ACREL_MUTE);
                break;
            case R.id.acrel_cmd_zero_clearing:
                doAcrelControl(CmdType.CMD_ACREL_ZERO_CLEARING);
                break;
            case R.id.acrel_psd:
                dialogFragment = SettingsInputDialogFragment.newInstance(sensoroSensor.acrelFires.passwd + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_ACREL_PSD);
                break;
//            case R.id.cayman_is_smoke:
//                int isSmoke = sensoroSensor.cayManData.isSmoke;
//                String[] smokeItems = {"无烟雾报警", "有烟雾报警"};
//                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(smokeItems, isSmoke);
//                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_IS_SMOKE);
//                break;
//            case R.id.cayman_is_moved:
//                int isMoved = sensoroSensor.cayManData.isSmoke;
//                String[] moveItems = {"无移动报警", "有移动报警"};
//                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(moveItems, isMoved);
//                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_IS_MOVED);
//                break;
//            case R.id.cayman_value_of_tem:
//                float valueOfTem = sensoroSensor.cayManData.valueOfTem / 10;
//                dialogFragment = SettingsInputDialogFragment.newInstance(valueOfTem + "");
//                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_VALUE_OF_TEM);
//                break;
//            case R.id.cayman_value_of_hum:
//                float valueOfHum = sensoroSensor.cayManData.valueOfHum;
//                dialogFragment = SettingsInputDialogFragment.newInstance(valueOfHum + "");
//                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_VALUE_OF_HUM);
//                break;
            case R.id.cayman_ble_adv_type:
                int advType = sensoroSensor.cayManData.bleAdvType;
                String[] moveItems = {"持续广播", "间断广播"};
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(moveItems, advType);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_ADV_TYPE);
                break;
            case R.id.cayman_ble_adv_start_time:
                break;
            case R.id.cayman_ble_adv_end_time_hum:
                break;
            case R.id.cayman_human_detection_time:
                dialogFragment = SettingsInputDialogFragment.newInstance(sensoroSensor.cayManData.humanDetectionTime + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_HUMAN_DETECTION_TIME);
                break;
            case R.id.cayman_defense_mode:
                String[] items = {"关闭", "开启"};
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(items, sensoroSensor.cayManData.defenseMode);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_DEFENSE_MODE);
                break;
            case R.id.cayman_defense_timer_mode:
                String[] timerItems = {"关闭", "开启"};
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(timerItems, sensoroSensor.cayManData.defenseTimerMode);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_DEFENSE_TIMER_MODE);
                break;
            case R.id.cayman_defense_mode_start_time:
//                dialogFragment = SettingsInputDialogFragment.newInstance(caymanDefenseModeStartTimeContent.getText().toString());
//                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_DEFENSE_MODE_START_TIME);
                showPickerView(1);
                break;
            case R.id.cayman_defense_mode_stop_time:
//                dialogFragment = SettingsInputDialogFragment.newInstance(caymanDefenseModeStopTimeContent.getText().toString());
//                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_DEFENSE_MODE_STOP_TIME);
                showPickerView(2);
                break;
            case R.id.cayman_invade_alarm:
                String[] invadeAlarmItems = {"关闭", "开启"};
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(invadeAlarmItems, sensoroSensor.cayManData.defenseTimerMode);
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_INVADE_ALARM);
                break;
            case R.id.settings_device_rl_app_led_status:
                String[] ledStatusItems = {"关闭", "开启"};
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(ledStatusItems, sensoroDevice.getLedStatus());
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_APP_LED_STATUS);
                break;
            case R.id.settings_device_rl_app_beep_mute_time:
                dialogFragment = SettingsInputDialogFragment.newInstance("");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_APP_BEEP_MUTE_TIME);
                break;
            case R.id.cayman_alarm_of_high_tem:
                float alarmOfHighTem = sensoroSensor.cayManData.alarmOfHighTem / 10f;
                dialogFragment = SettingsInputDialogFragment.newInstance(alarmOfHighTem + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_ALARM_OF_HIGH_TEM);
                break;
            case R.id.cayman_alarm_of_low_tem:
                float alarmOfLowTem = sensoroSensor.cayManData.alarmOfLowTem / 10f;
                dialogFragment = SettingsInputDialogFragment.newInstance(alarmOfLowTem + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_ALARM_OF_LOW_TEM);
                break;
            case R.id.cayman_alarm_of_high_hum:
                float alarmOfHighHum = sensoroSensor.cayManData.alarmOfHighHum / 10f;
                dialogFragment = SettingsInputDialogFragment.newInstance(alarmOfHighHum + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_ALARM_OF_HIGH_HUM);
                break;
            case R.id.cayman_alarm_of_low_hum:
                float alarmOfLowHum = sensoroSensor.cayManData.alarmOfLowHum / 10f;
                dialogFragment = SettingsInputDialogFragment.newInstance(alarmOfLowHum + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_CAYMAN_ALARM_OF_LOW_HUM);
                break;
            case R.id.cayman_cmd_self_check:
                doCaymanControl(CmdType.CMD_CAYMAN_SELEF_CHECK);
                break;
            case R.id.cayman_cmd_reset:

                //恢复出厂设置后，需要断开连接 再连接上 然后将新的配置文件推送到服务器
                MsgNode1V1M5.Cayman.Builder builder = MsgNode1V1M5.Cayman.newBuilder();
                builder.setCmd(2);
                sensoroDeviceConnection.writeCaymanCmd(builder, CmdType.CMD_CAYMAN_RESET, this);
                break;
            case R.id.cayman_cmd_clear_sound:
                doCaymanControl(CmdType.CMD_CAYMAN_CLEAR_SOUND);
                break;
            case R.id.baymax_density_l1:
                int gasDensityL1 = sensoroSensor.baymax.gasDensityL1;
                dialogFragment = SettingsInputDialogFragment.newInstance(gasDensityL1 + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_BAYMAX_GAS_DENSITY_L1);
                break;
            case R.id.baymax_density_l2:
                int gasDensityL2 = sensoroSensor.baymax.gasDensityL2;
                dialogFragment = SettingsInputDialogFragment.newInstance(gasDensityL2 + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_BAYMAX_GAS_DENSITY_L2);
                break;
            case R.id.baymax_density_l3:
                int gasDensityL3 = sensoroSensor.baymax.gasDensityL3;
                dialogFragment = SettingsInputDialogFragment.newInstance(gasDensityL3 + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_DEVICE_RL_BAYMAX_GAS_DENSITY_L3);
                break;
            case R.id.baymax_cmd_self_check:
                doBaymaxControl(1);
                break;
            case R.id.baymax_cmd_reset:
                doBaymaxControl(2);

                break;
            case R.id.baymax_cmd_mute:
                doBaymaxControl(4);
                break;
            case R.id.baymax_cmd_close_electronic_valve:
                doBaymaxControl(8);
                break;
            case R.id.settings_device_rl_app_battery_beep:
                String[] batteryItems = {"关闭", "开启"};
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(batteryItems, sensoroDevice.getBatteryBeep());
                dialogFragment.show(getFragmentManager(), Constants.SETTINGS_DEVICE_RL_APP_LOW_BATTERY_BEEP);
                break;
            case R.id.settings_device_rl_app_demo:
                String[] demoItems = {"关闭", "开启"};
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(demoItems, sensoroDevice.getDemoMode());
                dialogFragment.show(getFragmentManager(), Constants.SETTINGS_DEVICE_RL_APP_DEMO);
                break;

        }
    }

    private void showPickerView(int status) {

        if (pvCustomOptions == null) {
            initCustomOptionPicker();
        }
        picekerViewStatus = status;
        pvCustomOptions.setSelectOptions(8, 0);//默认选中项
        pvCustomOptions.show();

    }

    private void doBaymaxControl(int cmd) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage(getString(R.string.send_cmd));
            progressDialog.show();
        }
        MsgNode1V1M5.Baymax.Builder builder = MsgNode1V1M5.Baymax.newBuilder();
        builder.setGasDeviceCMD(cmd);
        sensoroDeviceConnection.writeBaymaxCmd(builder, this);
    }

    private void doCaymanControl(int cmd) {
        byte[] bytes = new byte[3];
        switch (cmd) {
            case CmdType.CMD_CAYMAN_SELEF_CHECK:
                bytes[0] = 1;
                bytes[1] = 0;
                bytes[2] = 0;
                break;
            case CmdType.CMD_CAYMAN_RESET:
                bytes[0] = 0;
                bytes[1] = 1;
                bytes[2] = 0;
                break;
            case CmdType.CMD_CAYMAN_CLEAR_SOUND:
                bytes[0] = 0;
                bytes[1] = 0;
                bytes[2] = 1;
                break;
        }

        MsgNode1V1M5.Cayman.Builder builder = MsgNode1V1M5.Cayman.newBuilder();
        int i = SensoroUUID.bitsToInt(bytes);
        builder.setCmd(i);
        sensoroDeviceConnection.writeCaymanCmd(builder, -1, this);
    }

    private void doAcrelControl(int cmd) {
        int acrelCmd = 0;
        switch (cmd) {
            case CmdType.CMD_ACREL_RESET:
                acrelCmd = 1;
                break;
            case CmdType.CMD_ACREL_SELF_CHECK:
                acrelCmd = 2;
                break;
            case CmdType.CMD_ACREL_QUERY:
                acrelCmd = 0;
                break;
            case CmdType.CMD_ACREL_MUTE:
                acrelCmd = 4;
                break;
            case CmdType.CMD_ACREL_ZERO_CLEARING:
                acrelCmd = 8;
                break;

        }

        MsgNode1V1M5.AcrelData.Builder builder = MsgNode1V1M5.AcrelData.newBuilder();
        builder.setCmd(acrelCmd);
        sensoroDeviceConnection.writeAcrelCmd(builder, this);
    }

}
