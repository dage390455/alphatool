package com.sensoro.lora.setting.server.bean;

import java.util.List;

public class DeviceTyps extends ResponseBase {

    /**
     * data : {"stations":["ai_camera","station","gateway","taike_fire_hydrant","scgateway"],"devices":["cayman_smoke_pir","lite_smoke","bg_beacon_scanner","taike_level","baymax_ch4","baymax_lpg","enzo_smoke","acrel_alpha","chip_e","cayman_smoke","n16w_smoke","ai_camera","acrel_fires","concox_tracker","chip_l","acrel_single","zcrd_artificial","zcrd_door","fhsj_elec_fires","siter_ch4","tk","zcrd_magnetic","taike_hydrant","zcrd_water","node","no2","tracker","sos","ch4","leak","gps","pm","alarm","bigbang_tracker","waterPressure","depre_co2","t1","winsen_ch4","chip","zcrd_ammeter","fhsj_lpg","tester","temp_humi","zcrd_manual","depre_co","depre_pm","op_node","depre_ch4","depre_leak","jf_connection","chip_s","winsen_gas","taike_water","co","co2","angle","smoke","bhenergy_water","lpg","zcrd_sound_light","fire_sprinkler","cover","winsen_lpg","fhsj_smoke","module","zcrd_infrared","flame","mantun_fires","zcrd_arc","netvox","temp_humi_one","zcrd_rfid","fhsj_ch4"]}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<String> stations;
        private List<String> devices;

        public List<String> getStations() {
            return stations;
        }

        public void setStations(List<String> stations) {
            this.stations = stations;
        }

        public List<String> getDevices() {
            return devices;
        }

        public void setDevices(List<String> devices) {
            this.devices = devices;
        }
    }
}
