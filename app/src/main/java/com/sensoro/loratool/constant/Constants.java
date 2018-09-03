package com.sensoro.loratool.constant;

/**
 * Created by tangrisheng on 2016/5/13.
 * All Constant
 */
public interface Constants {
    String EXTRA_NAME_DEVICE = "extra_name_device";
    String EXTRA_NAME_DEVICE_INFO = "extra_name_device_info";
    String EXTRA_NAME_DEVICE_LIST = "extra_name_device_list";
    String EXTRA_NAME_STATION = "extra_name_station";
    String EXTRA_NAME_STATION_LIST = "extra_name_station_list";
    String EXTRA_URL = "extra_url";
    String EXTRA_UPGRADE_INDEX = "extra_upgrade_index";
    String EXTRA_NAME_BAND = "extra_name_band";
    String EXTRA_NAME_DEVICE_TYPE = "extra_name_deviceType";
    String EXTRA_NAME_DEVICE_FIRMWARE_VERSION = "extra_name_firmware_version";
    String EXTRA_NAME_DEVICE_HARDWARE_VERSION = "extra_name_hardware_version";
    String EXTRA_NAME_STATION_TYPE = "extra_name_station_type";
    String BASE_KEY = "LYWpuL8AGpJwJgw9Miwf6Z9YmJN8XY";
    String TYPE_DEV = "A0";
    String TYPE_BUS = "B0";
    String DEFAULT_PASSWORD = "0000000000000000";
    String ENCODE = "HmacSHA512";

    // REQUEST CODE
    int ZXING_REQUEST_CODE_RESULT = 102;
    int ZXING_REQUEST_CODE_SCAN_BEACON = 100;
     String ZXING_REQUEST_CODE = "ZXING_REQUEST_CODE";

    String PREFERENCE_FILTER = "alpha_filter_data";
    String PREFERENCE_LOGIN = "alpha_tool_login";
    String PREFERENCE_SCOPE = "alpha_tool_scope";
    String PREFERENCE_DEVICE_HISTORY = "alpha_tool_device_history";
    String PREFERENCE_STATION_HISTORY = "alpha_tool_station_history";
    String PREFERENCE_KEY_SERVER_NAME = "server_name";
    String PREFERENCE_KEY_NAME = "name";
    String PREFERENCE_KEY_URL = "url";
    String PREFERENCE_KEY_PWD = "pwd";
    String PREFERENCE_KEY_EXPIRES = "expires";
    String PREFERENCE_KEY_SESSION_ID = "sessionId";
    String PREFERENCE_KEY_PERMISSION_0 = "permission0";
    String PREFERENCE_KEY_PERMISSION_1 = "permission1";
    String PREFERENCE_KEY_PERMISSION_2 = "permission2";
    String PREFERENCE_KEY_PERMISSION_3 = "permission3";
    String PREFERENCE_KEY_PERMISSION_4 = "permission4";
    String PREFERENCE_KEY_PERMISSION_5 = "permission5";
    String PREFERENCE_KEY_PERMISSION_6 = "permission6";
    String PREFERENCE_KEY_HISTORY_KEYWORD = "key_search_history_keyword";
    String LORA_BAND_US915 = "US915";
    String LORA_BAND_EU433 = "EU433";
    String LORA_BAND_EU868 = "EU868";
    String LORA_BAND_AU915 = "AU915";
    String LORA_BAND_AS923 = "AS923";
    String LORA_BAND_SE433 = "SE433";
    String LORA_BAND_SE470 = "SE470";
    String LORA_BAND_SE915 = "SE915";
    String LORA_BAND_SE780 = "SE780";
    String LORA_BAND_CN470 = "CN470";
    String DEVICE_HARDWARE_TYPE[] = {"##","chip", "module", "node", "co2", "co", "no2", "so2", "nh3",
            "tvoc", "o3", "pm", "leak", "temp_humi", "ch4", "lpg", "cover", "smoke", "angle", "gps", "op_node",
            "flame", "op_chip", "winsen_ch4", "winsen_lpg", "winsen_gas", "bhenergy_water", "chip_s", "chip_e",
            "tester", "temp_humi_one", "fhsj_ch4", "fhsj_lpg", "concox_tracker", "tk","fhsj_smoke","fhsj_elec_fires",
            "siter_ch4","siter_lpg","jf_connection","mantun_fires"};

    boolean permission[] = {
            false,//sCfgByBle
            false,//sCfgByWifi
            false,//sCfgToPrivateCloud
            false,//sUpgrade
            false,//dCfgByBle
            false,//dCfgToPrivateCloud
            false,//dUpgrade
    };
    int RESULT_FILTER = 1001;
    int REQUEST_FILTER = 2001;
    int FILTER_DEVICE_SWITCH = 0;
    int FILTER_DEVICE_NEARBY = 1;
    int FILTER_DEVICE_FIRMWARE = 2;
    int FILTER_DEVICE_HARDWARE = 3;
    int FILTER_DEVICE_BAND = 4;
    int FILTER_DEVICE_SIGNAL = 5;

    int FILTER_STATION_SWITCH = 0;
    int FILTER_STATION_NEARBY = 1;
    int FILTER_STATION_FIRMWARE = 2;
    int FILTER_STATION_HARDWARE = 3;
    int FILTER_STATION_SIGNAL = 4;

    int LORA_SE433_TXP[] = {20, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 2};
    String LORA_SE433_SF[] = {"SF7 / 125 kHz", "SF8 / 125 kHz", "SF9 / 125 kHz", "SF10 / 125 kHz", "SF11 / 125 kHz", "SF12 / 125 kHz"};
    int LORA_SE433_DR[] = {5, 4, 3, 2, 1, 0};

    int LORA_SE470_TXP[] = {20, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 2};
    String LORA_SE470_SF[] = {"SF7 / 125 kHz", "SF8 / 125 kHz", "SF9 / 125 kHz", "SF10 / 125 kHz", "SF11 / 125 kHz", "SF12 / 125 kHz"};
    int LORA_SE470_DR[] = {5, 4, 3, 2, 1, 0};

    int LORA_SE780_TXP[] = {20, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 2};
    String LORA_SE780_SF[] = {"SF7 / 125 kHz", "SF8 / 125 kHz", "SF9 / 125 kHz", "SF10 / 125 kHz", "SF11 / 125 kHz", "SF12 / 125 kHz"};
    int LORA_SE780_DR[] = {5, 4, 3, 2, 1, 0};

    int LORA_SE915_TXP[] = {20, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 2};
    String LORA_SE915_SF[] = {"SF7 / 125 kHz", "SF8 / 125 kHz", "SF9 / 125 kHz", "SF10 / 125 kHz", "SF11 / 125 kHz", "SF12 / 125 kHz"};
    int LORA_SE915_DR[] = {5, 4, 3, 2, 1, 0};

    int LORA_EU433_TXP[] = {10, 7, 4, 1};
    String LORA_EU433_SF[] = {"SF7 / 125 kHz", "SF8 / 125 kHz", "SF9 / 125 kHz", "SF10 / 125 kHz", "SF11 / 125 kHz", "SF12 / 125 kHz"};
    int LORA_EU433_DR[] = {5, 4, 3, 2, 1, 0};
    String LORA_EU433_MAX_EIRP[] = {"MaxEIRP - 0 \n12 dBm", "MaxEIRP - 2 \n10 dBm", "MaxEIRP - 4 \n8 dBm", "MaxEIRP -6 \n6 dBm", "MaxEIRP -8 \n4 dBm", "MaxEIRP -10 \n2 dBm"};
    String LORA_EU433_MAX_EIRP_VALUE[] = {"MaxEIRP - 0", "MaxEIRP - 2", "MaxEIRP -4", "MaxEIRP -6", "MaxEIRP -8", "MaxEIRP -10"};

    int LORA_US915_TXP[] = {20, 18, 16, 14, 12, 10};
    String LORA_US915_SF[] = {"SF7 / 125 kHz", "SF8 / 125 kHz", "SF9 / 125 kHz", "SF10 / 125 kHz"};
    int LORA_US915_DR[] = {3, 2, 1, 0};

    int LORA_AU915_TXP[] = {20, 18, 16, 14, 12, 10};
    String LORA_AU915_SF[] = {"SF7 / 125 kHz", "SF8 / 125 kHz", "SF9 / 125 kHz", "SF10 / 125 kHz"};
    int LORA_AU915_DR[] = {3, 2, 1, 0};

    int LORA_EU868_TXP[] = {20, 14, 11, 8, 5, 2};
    String LORA_EU868_SF[] = {"FSK 50 kbps", "SF7 / 250 kHz", "SF7 / 125 kHz", "SF8 / 125 kHz", "SF9 / 125 kHz", "SF10 / 125 kHz", "SF11 / 125 kHz", "SF12 / 125 kHz"};
    int LORA_EU868_DR[] = {7, 6, 5, 4, 3, 2, 1, 0};
    String LORA_EU868_MAX_EIRP[] = {"MaxEIRP - 0 \n12 dBm", "MaxEIRP - 2 \n10 dBm", "MaxEIRP -4 \n8 dBm", "MaxEIRP -6 \n6 dBm", "MaxEIRP -4 \n4 dBm", "MaxEIRP -8 \n2 dBm", "MaxEIRP -10 \n0 dBm","MaxEIRP -12 \n-2 dBm"};
    String LORA_EU868_MAX_EIRP_VALUE[] = {"MaxEIRP - 0", "MaxEIRP - 2", "MaxEIRP -4", "MaxEIRP -6", "MaxEIRP -8", "MaxEIRP -10", "MaxEIRP -12","MaxEIRP -14"};

    int LORA_AS923_TXP[] = {14, 12, 10, 8, 6, 4};
    String LORA_AS923_SF[] = {"FSK 50 kbps", "SF7 / 250 kHz", "SF7 / 125 kHz", "SF8 / 125 kHz", "SF9 / 125 kHz", "SF10 / 125 kHz", "SF11 / 125 kHz", "SF12 / 125 kHz"};
    int LORA_AS923_DR[] = {7, 6, 5, 4, 3, 2, 1, 0};
    String LORA_AS923_MAX_EIRP[] = {"MaxEIRP - 0 \n12 dBm", "MaxEIRP - 2 \n10 dBm", "MaxEIRP -4 \n8 dBm", "MaxEIRP -6 \n6 dBm", "MaxEIRP -4 \n4 dBm", "MaxEIRP -8 \n2 dBm", "MaxEIRP -10 \n0 dBm","MaxEIRP -12 \n-2 dBm"};
    String LORA_AS923_MAX_EIRP_VALUE[] = {"MaxEIRP - 0", "MaxEIRP - 2", "MaxEIRP -4", "MaxEIRP -6", "MaxEIRP -8", "MaxEIRP -10", "MaxEIRP -12","MaxEIRP -14"};

    int LORA_CN470_TXP[] = {7, 6, 5, 4, 3, 2, 1, 0};
    String LORA_CN470_SF[] = {"SF7 / 125 kHz", "SF8 / 125 kHz", "SF9 / 125 kHz", "SF10 / 125 kHz", "SF11 / 125 kHz", "SF12 / 125 kHz"};
    int LORA_CN470_DR[] = {5, 4, 3, 2, 1, 0};
    String LORA_CN470_MAX_EIRP[] = {"MaxEIRP - 0 \n12 dBm", "MaxEIRP - 2 \n10 dBm", "MaxEIRP -4 \n8 dBm", "MaxEIRP -6 \n6 dBm", "MaxEIRP -4 \n4 dBm", "MaxEIRP -8 \n2 dBm", "MaxEIRP -10 \n0 dBm","MaxEIRP -12 \n-2 dBm"};
    String LORA_CN470_MAX_EIRP_VALUE[] = {"MaxEIRP - 0", "MaxEIRP - 2", "MaxEIRP -4", "MaxEIRP -6", "MaxEIRP -8", "MaxEIRP -10", "MaxEIRP -12","MaxEIRP -14"};


    int LORA_BAND_SE_433[] = {0, 433300000, 433500000, 433700000, 433900000, 434300000, 434500000, 434700000, 434900000};

    int LORA_BAND_SE_470[] = {0, 486300000, 486500000, 486700000, 486900000, 487100000, 487300000, 487500000, 487700000};

    int LORA_BAND_CN_470[] = {0, 486300000, 486500000, 486700000, 486900000, 487100000, 487300000, 487500000, 487700000};

    int LORA_BAND_SE_780[] = {0, 779500000, 779700000, 779700000, 780100000, 780300000, 780500000, 780700000, 780900000};

    int LORA_BAND_SE_915[] = {0, 915300000, 915500000, 915700000, 915900000, 916300000, 916500000, 916700000, 916900000};

    int LORA_BAND_EU_433[] = {0, 43317500, 43337500, 43357500, 43377500, 43397500, 43417500, 43437500, 43457500};

    int LORA_BAND_EU_868[] = {0, 868100000, 868300000, 868500000, 867100000, 867300000, 867500000, 867700000, 867900000};

    int LORA_BAND_US_915[] = {0, 902300000, 902500000, 902700000, 902900000, 903100000, 903300000, 903500000, 903700000};

    int LORA_BAND_AU_915[] = {0, 915200000, 915400000, 915600000, 915800000, 916000000, 916200000, 916400000, 916600000};

    int LORA_BAND_AS_923[] = {0, 923200000, 923400000, 923600000, 923800000, 924000000, 924200000, 924400000, 924600000};

    String BLE_TIME_ARRAY[] = {"0:00", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
    String BLE_NODE_TXP_ARRAY[] = {"-52 dBm", "-42 dBm", "-38 dBm", "-34 dBm", "-30 dBm", "-20 dBm", "-16 dBm", "-12 dBm", "-8 dBm", "-4 dBm", "0 dBm", "4 dBm"};
    String BLE_NOT_NODE_TXP_ARRAY[] = {"-30 dBm", "-20 dBm", "-16 dBm", "-12 dBm", "-8 dBm", "-4 dBm", "0 dBm", "4 dBm"};
    String[] SF_ITEMS = {"7", "8", "9", "10", "11", "12"};
    String[] CLASSB_DATARATE = {"12", "11", "10", "9", "8", "7"};
    String[] CLASSB_PERIODICITY = {"1", "2", "4", "8", "16", "32", "64", "128"};
    int[] DELAY_VALUES = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    String[] DELAY_ITEMS = {"1s", "2s", "3s", "4s", "5s", "6s", "7s", "8s", "9s", "10s", "11s", "12s", "13s", "14s", "15s"};

    int STATUS_SLOT_DISABLED = 0;
    int STATUS_SLOT_UID = 1;
    int STATUS_SLOT_URL = 2;
    int STATUS_SLOT_EID = 3;
    int STATUS_SLOT_TLM = 4;

    int LAYOUT_EDDYSTONE_SLOT1 = 1;
    int LAYOUT_EDDYSTONE_SLOT2 = 2;
    int LAYOUT_EDDYSTONE_SLOT3 = 3;
    int LAYOUT_EDDYSTONE_SLOT4 = 4;
    int EDDYSTONE_SLOT_UID = 0;
    int EDDYSTONE_SLOT_URL = 1;
    int EDDYSTONE_SLOT_EID = 2;
    int EDDYSTONE_SLOT_TLM = 3;
    int EDDYSTONE_SLOT_IBEACON = 4;
    int EDDYSTONE_SLOT_CUSTOM1 = 5;
    int EDDYSTONE_SLOT_CUSTOM2 = 6;
    int EDDYSTONE_SLOT_CUSTOM3 = 7;
    int SETTING_STATUS_UNSET = 0;
    int SETTING_STATUS_SETTED = 1;
    int SMOKE_ACTION_START = 0;
    int SMOKE_ACTION_STOP = 1;
    int SMOKE_ACTION_SILENCE = 2;
    int STATUS_SLOT_UNSET = -1;

    String SETTINGS_IBEACON = "SETTINGS_IBEACON";
    String SETTINGS_UUID = "SETTINGS_UUID";
    String SETTINGS_MAJOR = "SETTINGS_MAJOR";
    String SETTINGS_MINOR = "SETTINGS_MINOR";
    String SETTINGS_BLE_POWER = "SETTINGS_BLE_POWER";
    String SETTINGS_ADV_INTERVAL = "SETTINGS_ADV_INTERVAL";
    String SETTINGS_BROADCAST_KEY = "SETTINGS_BROADCAST_KEY";
    String SETTINGS_EDDYSTONE1 = "SETTINGS_EDDYSTONE1";
    String SETTINGS_EDDYSTONE1_UID = "SETTINGS_EDDYSTONE1_UID";
    String SETTINGS_EDDYSTONE1_URL = "SETTINGS_EDDYSTONE1_URL";
    String SETTINGS_EDDYSTONE1_EID = "SETTINGS_EDDYSTONE1_EID";
    String SETTINGS_EDDYSTONE2 = "SETTINGS_EDDYSTONE2";
    String SETTINGS_EDDYSTONE2_UID = "SETTINGS_EDDYSTONE2_UID";
    String SETTINGS_EDDYSTONE2_URL = "SETTINGS_EDDYSTONE2_URL";
    String SETTINGS_EDDYSTONE2_EID = "SETTINGS_EDDYSTONE2_EID";
    String SETTINGS_EDDYSTONE3 = "SETTINGS_EDDYSTONE3";
    String SETTINGS_EDDYSTONE3_UID = "SETTINGS_EDDYSTONE3_UID";
    String SETTINGS_EDDYSTONE3_URL = "SETTINGS_EDDYSTONE3_URL";
    String SETTINGS_EDDYSTONE3_EID = "SETTINGS_EDDYSTONE3_EID";
    String SETTINGS_EDDYSTONE4 = "SETTINGS_EDDYSTONE4";
    String SETTINGS_EDDYSTONE4_UID = "SETTINGS_EDDYSTONE4_UID";
    String SETTINGS_EDDYSTONE4_URL = "SETTINGS_EDDYSTONE4_URL";
    String SETTINGS_EDDYSTONE4_EID = "SETTINGS_EDDYSTONE4_EID";
    String SETTINGS_TLM_INTERVAL = "SETTINGS_TLM_INTERVAL";
    String SETTINGS_TEMP_INTERVAL = "SETTINGS_TEMP_INTERVAL";
    String SETTINGS_LIGHT_INTERVAL = "SETTINGS_LIGHT_INTERVAL";
    String SETTINGS_HUMIDITY_INTERVAL = "SETTINGS_HUMIDITY_INTERVAL";
    String SETTINGS_ACCELER = "SETTINGS_ACCELER";
    String SETTINGS_MEASURED_RSSI = "SETTINGS_MEASURED_RSSI";
    String SETTINGS_BLE_TURNON_TIME = "SETTINGS_TURNON_TIME";
    String SETTINGS_BLE_TURNOFF_TIME = "SETTINGS_TURNOFF_TIME";
    String SETTINGS_LORA_TXP = "SETTINGS_LORA_TXP";
    String SETTINGS_LORA_INT = "SETTINGS_LORA_INT";
    String SETTINGS_LORA_EIRP = "SETTINGS_LORA_EIRP";
    String SETTINGS_SENSOR_CO = "SETTINGS_SENSOR_CO";
    String SETTINGS_SENSOR_CO2 = "SETTINGS_SENSOR_CO2";
    String SETTINGS_SENSOR_NO2 = "SETTINGS_SENSOR_NO2";
    String SETTINGS_SENSOR_CH4 = "SETTINGS_SENSOR_CH4";
    String SETTINGS_SENSOR_LPG = "SETTINGS_SENSOR_LPG";
    String SETTINGS_SENSOR_PM25 = "SETTINGS_SENSOR_PM25";
    String SETTINGS_SENSOR_PM10 = "SETTINGS_SENSOR_PM10";
    String SETTINGS_SENSOR_TEMP_UPPER = "SETTINGS_SENSOR_TEMP_UPPER";
    String SETTINGS_SENSOR_TEMP_LOWER = "SETTINGS_SENSOR_TEMP_LOWER";
    String SETTINGS_SENSOR_HUMIDITY_UPPER = "SETTINGS_SENSOR_HUMIDITY_UPPER";
    String SETTINGS_SENSOR_HUMIDITY_LOWER = "SETTINGS_SENSOR_HUMIDITY_UPPER";
    String SETTINGS_SENSOR_PITCH_ANGLE_UPPER = "SETTINGS_SENSOR_PITCH_ANGLE_UPPER";
    String SETTINGS_SENSOR_PITCH_ANGLE_LOWER = "SETTINGS_SENSOR_PITCH_ANGLE_LOWER";
    String SETTINGS_SENSOR_ROLL_ANGLE_UPPER = "SETTINGS_SENSOR_ROLL_ANGLE_UPPER";
    String SETTINGS_SENSOR_ROLL_ANGLE_LOWER = "SETTINGS_SENSOR_ROLL_ANGLE_LOWER";
    String SETTINGS_SENSOR_YAW_ANGLE_UPPER = "SETTINGS_SENSOR_YAW_ANGLE_UPPER";
    String SETTINGS_SENSOR_YAW_ANGLE_LOWER = "SETTINGS_SENSOR_YAW_ANGLE_LOWER";
    String SETTINGS_SENSOR_WATER_PRESSURE_UPPER = "SETTINGS_SENSOR_WATER_PRESSURE_UPPER";
    String SETTINGS_SENSOR_WATER_PRESSURE_LOWER = "SETTINGS_SENSOR_WATER_PRESSURE_LOWER";
    String SETTINGS_APP_PARAM_UPLOAD = "SETTINGS_APP_PARAM_UPLOAD";
    String SETTINGS_APP_PARAM_CONFIRM = "SETTINGS_APP_PARAM_CONFIRM";
    String SETTINGS_CUSTOM_PACKAGE1 = "SETTINGS_CUSTOM_PACKAGE1";
    String SETTINGS_CUSTOM_PACKAGE2 = "SETTINGS_CUSTOM_PACKAGE2";
    String SETTINGS_CUSTOM_PACKAGE3 = "SETTINGS_CUSTOM_PACKAGE3";
    String SETTINGS_SENSOR = "SETTINGS_SENSOR";
    String SETTINGS_CUSTOM_PACKAGE1_STATUS = "SETTINGS_CUSTOM_PACKAGE1_STATUS";
    String SETTINGS_CUSTOM_PACKAGE2_STATUS = "SETTINGS_CUSTOM_PACKAGE2_STATUS";
    String SETTINGS_CUSTOM_PACKAGE3_STATUS = "SETTINGS_CUSTOM_PACKAGE3_STATUS";

    String SETTINGS_ACCESS_MODE = "SETTINGS_ACCESS_MODE";
    String SETTINGS_IP_ASSIGNMENT = "SETTINGS_IP_ASSIGNMENT";
    String SETTINGS_IP_ADDRESS = "SETTINGS_IP_ADDRESS";
    String SETTINGS_ROUTER = "SETTINGS_ROUTER";
    String SETTINGS_SUBNET_MASK = "SETTINGS_SUBNET_MASK";
    String SETTINGS_DNS = "SETTINGS_DNS";
    String SETTINGS_SEC_DNS = "SETTINGS_SEC_DNS";
    String SETTINGS_NAME = "SETTINGS_NAME";
    String SETTINGS_PASSWORD = "SETTINGS_PASSWORD";
    String SETTINGS_ENCRYPT = "SETTINGS_ENCRYPT";
    String SETTINGS_SGL_DR = "settings_sgl_dr";
    String SETTINGS_SGL_FREQ = "settings_sgl_freq";

    String SETTINGS_DEVICE_APP_EUI = "Settings_device_app_eui";
    String SETTINGS_DEVICE_DEV_EUI = "Settings_device_dev_eui";
    String SETTINGS_DEVICE_APP_KEY = "Settings_device_app_key";
    String SETTINGS_DEVICE_APP_SESSION_KEY = "Settings_device_app_session_key";
    String SETTINGS_DEVICE_NWK_SESSION_KEY = "Settings_device_nwk_session_key";
    String SETTINGS_DEVICE_NWK_ADDRESS = "Settings_device_nwk_address";
    String SETTINGS_DEVICE_SF = "Settings_device_sf";
    String SETTINGS_DEVICE_DELAY = "Settings_device_delay";
    String SETTINGS_DEVICE_CHANNEL = "Settings_device_channel";
    String SETTINGS_DEVICE_DATARATE = "Settings_device_datarate";
    String SETTINGS_DEVICE_CLASS_ENALBLE = "Settings_device_classB_enable";
    String SETTINGS_DEVICE_ACTIVATION = "Settings_device_activation";
    String SETTINGS_DEVICE_CLASSB_DATARATE = "Settings_device_classB_datarate";
    String SETTINGS_DEVICE_CLASSB_PERIODICITY = "Settings_device_classB_periodicity";
    String SETTINGS_STATION_NETID = "Settings_station_netid";
    String SETTINGS_STATION_CLOUD_ADDRESS = "Settings_station_cloud_address";
    String SETTINGS_STATION_CLOUD_PORT = "Settings_station_cloud_port";
    String SETTINGS_STATION_KEY = "Settings_station_key";

    //单通道传感器设置
    String SETTINGS_DEVICE_TEMPERATURE_PRESSURE_UPPER = "settings_device_temperature_pressure_upper";
    String SETTINGS_DEVICE_TEMPERATURE_PRESSURE_LOWER = "settings_device_temperature_pressure_lower";
    String SETTINGS_DEVICE_TEMPERATURE_PRESSURE_STEP_UPPER = "settings_device_temperature_pressure_step_upper";
    String SETTINGS_DEVICE_TEMPERATURE_PRESSURE_STEP_LOWER = "settings_device_temperature_pressure_step_lower";
    //电表
    String SETTINGS_DEVICE_RL_FHSJ_ELEC_PWD = "settings_device_rl_fhsj_elec_pwd";
    String SETTINGS_DEVICE_RL_FHSJ_ELEC_LEAK = "settings_device_rl_fhsj_elec_leak";
    String SETTINGS_DEVICE_RL_FHSJ_ELEC_TEMP = "settings_device_rl_fhsj_elec_temp";
    String SETTINGS_DEVICE_RL_FHSJ_ELEC_OVERLOAD = "settings_device_rl_fhsj_elec_overload";
    String SETTINGS_DEVICE_RL_FHSJ_ELEC_OVERPRESSURE = "settings_device_rl_fhsj_elec_overpressure";
    String SETTINGS_DEVICE_RL_FHSJ_ELEC_UNDERVOLTAGE = "settings_device_rl_fhsj_elec_undervoltage";
    String SETTINGS_DEVICE_RL_FHSJ_ELEC_CURRENT = "settings_device_rl_fhsj_elec_current";

    //曼顿火灾传感器
    String SETTINGS_DEVICE_RL_MANTUN_LEAKAGE = "settings_device_rl_mantun_leakage";
    String SETTINGS_DEVICE_RL_MANTUN_TEMP = "settings_device_rl_mantun_temp";
    String SETTINGS_DEVICE_RL_MANTUN_CURRENT = "settings_device_rl_mantun_current";
    String SETTINGS_DEVICE_RL_MANTUN_VOL_HIGH = "settings_device_rl_mantun_vol_high";
    String SETTINGS_DEVICE_RL_MANTUN_VOL_LOW = "settings_device_rl_mantun_vol_low";
    String SETTINGS_DEVICE_RL_MANTUN_POWER = "settings_device_rl_mantun_vol_power";
    String SETTINGS_DEVICE_RL_MANTUN_TEMP_OUTSIDE = "settings_device_rl_mantun_temp_outside";
    String SETTINGS_DEVICE_RL_MANTUN_TEMP_CONTACT = "settings_device_rl_mantun_temp_contact";





}

