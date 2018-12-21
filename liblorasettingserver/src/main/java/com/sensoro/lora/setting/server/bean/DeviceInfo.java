package com.sensoro.lora.setting.server.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fangping on 2016/7/21.
 */

public class DeviceInfo implements Parcelable, Comparable {
    public static final int TYPE_NODE = 0;
    public static final int TYPE_MODULE = 1;
    public static final int TYPE_SENSOR = 2;
    public static final int TYPE_TRACKER = 3;
    public static final int TYPE_COVER = 4;
    public static final int TYPE_OP_NODE = 5;
    public static final String NODE_FIRMWARE_BETA_V = "1.0";//最先的Beta版本
    public static final String NODE_FIRMWARE_NORMAL_V = "1.1";//发布的正式版本。
    public static final String NODE_FIRMWARE_SIGNAL_TEST_V = "1.2";//支持信号测试
    public static final String NODE_FIRMWARE_SENSOR_CLASSB_V = "1.3";//支持Sensor广播和ClassB

    public static final String CHIP_FIRMWARE_V1 = "1.1";//支持信号测试

    public static final String SENSOR_FIRMWARE_V1 = "1.0";
    public static final String OPENLORA_FIRMWARE_V1 = "1.0";

    public static final String SENSOR_FIRMWARE_SIGNAL_TEST_V = "1.1";
    private String _id;
    private String sn;
    private int bleTxp;
    private int bleInt;
    private int bleOnTime;
    private int bleOffTime;
    private String appKey;
    private String appSKey;
    private String nwkSkey;
    private String appEui;
    private int tempInt;
    private int lightInt;
    private int humidityInt;
    private int devAddr;
    private int loraAdr;
    private int loraSf;
    private int loraInt;
    private int loraTxPower;
    private int ibcnState;
    private int ibcnMajor;
    private int ibcnMinor;
    private String ibcnUuid;
    private String eddyUid;
    private String eddyUrl;
    private int eddyUidState;
    private int eddyUrlState;
    private int eddyTlmState;
    private String cusPtk1;
    private String cusPtk2;
    private String cusPtk3;
    private int cusPkt1State;
    private int cusPkt2State;
    private int cusPkt3State;
    private int cusPkt4State;

    private List<String> tags;
    private String ownerId;
    private String owner;
    private int normalStatus;
    private double lonlat[];
    private int battery;
    private LocationInfo location;
    private double sf;
    private String deviceType;
    private long lastUpTime;
    private double humidity;
    private double temperature;
    private double light;
    private String name;
    private String password;
    private int interval;
    private String band;
    private int sort;
    private String firmwareVersion;
    private String hardwareVersion;
    private boolean isConnectable;
    private boolean isSelected;
    private int rssi;//附近才有

    public DeviceInfo() {

    }

    public DeviceInfo(Parcel in) {
        _id = in.readString();
        sn = in.readString();
//        if (getTags() != null) {
//            in.readStringList(tags);
//            this.tags = new ArrayList<>();
//            in.readStringList(this.tags);
//
////        }
        ownerId = in.readString();
        owner = in.readString();
        normalStatus = in.readInt();
//        if (lonlat != null) {
//            in.readDoubleArray(lonlat);
//        }

        battery = in.readInt();
        location = in.readParcelable(LocationInfo.class.getClassLoader());
        sf = in.readDouble();
        deviceType = in.readString();
        lastUpTime = in.readLong();
        humidity = in.readDouble();
        temperature = in.readDouble();
        light = in.readDouble();
        name = in.readString();
        password = in.readString();
        interval = in.readInt();
        bleTxp = in.readInt();
        bleInt = in.readInt();
        bleOnTime = in.readInt();
        bleOffTime = in.readInt();
        appKey = in.readString();
        appSKey = in.readString();
        nwkSkey = in.readString();
        appEui = in.readString();
        tempInt = in.readInt();
        lightInt = in.readInt();
        humidityInt = in.readInt();
        devAddr = in.readInt();
        loraAdr = in.readInt();
        loraSf = in.readInt();
        loraInt = in.readInt();
        loraTxPower = in.readInt();
        ibcnState = in.readInt();
        ibcnMajor = in.readInt();
        ibcnMinor = in.readInt();
        ibcnUuid = in.readString();
        eddyUid = in.readString();
        eddyUrl = in.readString();
        eddyUidState = in.readInt();
        eddyUrlState = in.readInt();
        eddyTlmState = in.readInt();
        cusPtk1 = in.readString();
        cusPtk2 = in.readString();
        cusPtk3 = in.readString();
        cusPkt1State = in.readInt();
        cusPkt2State = in.readInt();
        cusPkt3State = in.readInt();
        cusPkt4State = in.readInt();
        sort = in.readInt();
        firmwareVersion = in.readString();
        hardwareVersion = in.readString();
        isConnectable = in.readByte() == 1 ? true : false;
        band = in.readString();
        isSelected = in.readByte() == 1 ? true : false;
        rssi = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_id);
        dest.writeString(sn);
//        if (tags != null) {
////            dest.writeStringList(tags);
//            dest.writeList(tags);
//        }

        dest.writeString(ownerId);
        dest.writeString(owner);
        dest.writeInt(normalStatus);
//        if (lonlat != null) {
//            dest.writeDoubleArray(lonlat);
//        }
        dest.writeInt(battery);
        dest.writeParcelable(location, flags);
        dest.writeDouble(sf);
        dest.writeString(deviceType);
        dest.writeLong(lastUpTime);
        dest.writeDouble(humidity);
        dest.writeDouble(temperature);
        dest.writeDouble(light);
        dest.writeString(name);
        dest.writeString(password);
        dest.writeInt(interval);
        dest.writeInt(bleTxp);
        dest.writeInt(bleInt);
        dest.writeInt(bleOnTime);
        dest.writeInt(bleOffTime);
        dest.writeString(appKey);
        dest.writeString(appSKey);
        dest.writeString(nwkSkey);
        dest.writeString(appEui);
        dest.writeInt(tempInt);
        dest.writeInt(lightInt);
        dest.writeInt(humidityInt);
        dest.writeInt(devAddr);
        dest.writeInt(loraAdr);
        dest.writeInt(loraSf);
        dest.writeInt(loraInt);
        dest.writeInt(loraTxPower);
        dest.writeInt(ibcnState);
        dest.writeInt(ibcnMajor);
        dest.writeInt(ibcnMinor);
        dest.writeString(ibcnUuid);
        dest.writeString(eddyUid);
        dest.writeString(eddyUrl);
        dest.writeInt(eddyUidState);
        dest.writeInt(eddyUrlState);
        dest.writeInt(eddyTlmState);
        dest.writeString(cusPtk1);
        dest.writeString(cusPtk2);
        dest.writeString(cusPtk3);
        dest.writeInt(cusPkt1State);
        dest.writeInt(cusPkt2State);
        dest.writeInt(cusPkt3State);
        dest.writeInt(cusPkt4State);
        dest.writeInt(sort);
        dest.writeString(firmwareVersion);
        dest.writeString(hardwareVersion);
        dest.writeByte((byte) (isConnectable ? 1 : 0));
        dest.writeString(band);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeInt(rssi);
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
        @Override
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        try {
            if (obj instanceof DeviceInfo) {
                return this.sn.equals(((DeviceInfo) obj).sn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String get_id() {
        return _id;
    }

    @Override
    public int hashCode() {
        return this.sn.hashCode();
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getNormalStatus() {
        return normalStatus;
    }

    public void setNormalStatus(int normalStatus) {
        this.normalStatus = normalStatus;
    }

    public double[] getLonlat() {
        return lonlat;
    }

    public void setLonlat(double[] lonlat) {
        this.lonlat = lonlat;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public LocationInfo getLocation() {
        return location;
    }

    public void setLocation(LocationInfo location) {
        this.location = location;
    }

    public double getSf() {
        return sf;
    }

    public void setSf(double sf) {
        this.sf = sf;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public long getLastUpTime() {
        return lastUpTime;
    }

    public void setLastUpTime(long lastUpTime) {
        this.lastUpTime = lastUpTime;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }


    public int getBleTxp() {
        return bleTxp;
    }

    public void setBleTxp(int bleTxp) {
        this.bleTxp = bleTxp;
    }

    public int getBleInt() {
        return bleInt;
    }

    public void setBleInt(int bleInt) {
        this.bleInt = bleInt;
    }

    public int getBleOnTime() {
        return bleOnTime;
    }

    public void setBleOnTime(int bleOnTime) {
        this.bleOnTime = bleOnTime;
    }

    public int getBleOffTime() {
        return bleOffTime;
    }

    public void setBleOffTime(int bleOffTime) {
        this.bleOffTime = bleOffTime;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppSKey() {
        return appSKey;
    }

    public void setAppSKey(String appSKey) {
        this.appSKey = appSKey;
    }

    public String getNwkSkey() {
        return nwkSkey;
    }

    public void setNwkSkey(String nwkSkey) {
        this.nwkSkey = nwkSkey;
    }

    public String getAppEui() {
        return appEui;
    }

    public void setAppEui(String appEui) {
        this.appEui = appEui;
    }

    public int getTempInt() {
        return tempInt;
    }

    public void setTempInt(int tempInt) {
        this.tempInt = tempInt;
    }

    public int getLightInt() {
        return lightInt;
    }

    public void setLightInt(int lightInt) {
        this.lightInt = lightInt;
    }

    public int getHumidityInt() {
        return humidityInt;
    }

    public void setHumidityInt(int humidityInt) {
        this.humidityInt = humidityInt;
    }

    public int getDevAddr() {
        return devAddr;
    }

    public void setDevAddr(int devAddr) {
        this.devAddr = devAddr;
    }

    public int getLoraAdr() {
        return loraAdr;
    }

    public void setLoraAdr(int loraAdr) {
        this.loraAdr = loraAdr;
    }

    public int getLoraSf() {
        return loraSf;
    }

    public void setLoraSf(int loraSf) {
        this.loraSf = loraSf;
    }

    public int getLoraInt() {
        return loraInt;
    }

    public void setLoraInt(int loraInt) {
        this.loraInt = loraInt;
    }

    public int getLoraTxp() {
        return loraTxPower;
    }

    public void setLoraTxp(int loraTxp) {
        this.loraTxPower = loraTxp;
    }

    public int getIbcnState() {
        return ibcnState;
    }

    public void setIbcnState(int ibcnState) {
        this.ibcnState = ibcnState;
    }

    public int getIbcnMajor() {
        return ibcnMajor;
    }

    public void setIbcnMajor(int ibcnMajor) {
        this.ibcnMajor = ibcnMajor;
    }

    public int getIbcnMinor() {
        return ibcnMinor;
    }

    public void setIbcnMinor(int ibcnMinor) {
        this.ibcnMinor = ibcnMinor;
    }

    public String getIbcnUuid() {
        return ibcnUuid;
    }

    public void setIbcnUuid(String ibcnUuid) {
        this.ibcnUuid = ibcnUuid;
    }

    public String getEddyUid() {
        return eddyUid;
    }

    public void setEddyUid(String eddyUid) {
        this.eddyUid = eddyUid;
    }

    public String getEddyUrl() {
        return eddyUrl;
    }

    public void setEddyUrl(String eddyUrl) {
        this.eddyUrl = eddyUrl;
    }

    public int getEddyUidState() {
        return eddyUidState;
    }

    public void setEddyUidState(int eddyUidState) {
        this.eddyUidState = eddyUidState;
    }

    public int getEddyUrlState() {
        return eddyUrlState;
    }

    public void setEddyUrlState(int eddyUrlState) {
        this.eddyUrlState = eddyUrlState;
    }

    public int getEddyTlmState() {
        return eddyTlmState;
    }

    public void setEddyTlmState(int eddyTlmState) {
        this.eddyTlmState = eddyTlmState;
    }

    public String getCusPtk1() {
        return cusPtk1;
    }

    public void setCusPtk1(String cusPtk1) {
        this.cusPtk1 = cusPtk1;
    }

    public String getCusPtk2() {
        return cusPtk2;
    }

    public void setCusPtk2(String cusPtk2) {
        this.cusPtk2 = cusPtk2;
    }

    public String getCusPtk3() {
        return cusPtk3;
    }

    public void setCusPtk3(String cusPtk3) {
        this.cusPtk3 = cusPtk3;
    }

    public int getCusPkt1State() {
        return cusPkt1State;
    }

    public void setCusPkt1State(int cusPkt1State) {
        this.cusPkt1State = cusPkt1State;
    }

    public int getCusPkt2State() {
        return cusPkt2State;
    }

    public void setCusPkt2State(int cusPkt2State) {
        this.cusPkt2State = cusPkt2State;
    }

    public int getCusPkt3State() {
        return cusPkt3State;
    }

    public void setCusPkt3State(int cusPkt3State) {
        this.cusPkt3State = cusPkt3State;
    }

    public int getCusPkt4State() {
        return cusPkt4State;
    }

    public void setCusPkt4State(int cusPkt4State) {
        this.cusPkt4State = cusPkt4State;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public int getLoraTxPower() {
        return loraTxPower;
    }

    public void setLoraTxPower(int loraTxPower) {
        this.loraTxPower = loraTxPower;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public boolean isConnectable() {
        return isConnectable;
    }

    public void setConnectable(boolean connectable) {
        isConnectable = connectable;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int toSensoroDeviceType() {
        if (deviceType.equals("module")) {
            return TYPE_MODULE;
        } else if (deviceType.equals("node")) {
            return TYPE_NODE;
        } else if (deviceType.equals("tracker")) {
            return TYPE_TRACKER;
        } else if (deviceType.equals("cover")) {
            return TYPE_COVER;
        } else if (deviceType.equals("op_node")) {
            return TYPE_OP_NODE;
        } else {
            return TYPE_SENSOR;
        }
    }

    public boolean isCanSignal() {
        String firmwareVersion = getFirmwareVersion();
        boolean isCan = false;
        String[] cnCanSignal = {"acrel_single","baymax_ch4","baymax_lpg","fhsj_smoke"};
        if("CN470".equals(band)&& Arrays.asList(cnCanSignal).contains(deviceType)){
            return true;
        }
        if (isOpenLoraDevice()) {
            return false;
        } else {
            switch (deviceType) {
                case "flame":
                case "op_chip":
                case "winsen_ch4":
                case "winsen_lpg":
                case "winsen_gas":
                case "bhenergy_water":
                case "fhsj_smoke":
                case "tester":
                    isCan = true;
                    break;
                case "chip":
                case "module":
                    isCan = firmwareVersion.compareTo(CHIP_FIRMWARE_V1) >= 0;
                    break;
                case "node":
                    isCan = firmwareVersion.compareTo(NODE_FIRMWARE_SIGNAL_TEST_V) >= 0;
                    break;
                case "co2":
                case "co":
                case "ch4":
                case "no2":
                case "pm":
                case "leak":
                case "temp_humi":
                case "lpg":
                case "smoke":
                    isCan = firmwareVersion.compareTo(SENSOR_FIRMWARE_SIGNAL_TEST_V) >= 0;
                    break;
                case "tracker":
                case "angle":
                case "so2":
                case "nh3":
                case "tvoc":
                case "o3":
                    isCan = firmwareVersion.compareTo(SENSOR_FIRMWARE_V1) >= 0;
                    break;
                default:
                    isCan = false;
            }
        }
        return isCan;
    }

    public boolean isOpenLoraDevice() {
        if ((band.startsWith("se") || band.startsWith("SE"))) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int compareTo(Object another) {
        DeviceInfo anotherDeviceInfo = (DeviceInfo) another;
        if (this.sort > anotherDeviceInfo.sort) {
            return -1;
        } else if (this.sort == anotherDeviceInfo.sort) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        String s ="appsky " +getAppSKey()+"getAppEui" +getAppEui()
                +"getLoraTxp" +getLoraTxp()+"getNormalStatus" +getNormalStatus()+"getAppKey" +getAppKey()+
                "getBand" +getBand()+"getDeviceType" +getDeviceType()+"getHumidity" +getHumidity()
                ;
        return s;
    }
}
