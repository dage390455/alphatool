package com.sensoro.loratool.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * WIFI admin class
 *
 * @author tangrisheng
 */
public class WifiAdmin {

    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_WPA_PSK = 2;
    public static final int SECURITY_EAP = 3;

    private static WifiAdmin wifiAdmin = null;

    private List<WifiConfiguration> wifiConfigurations; //无线网络配置信息类集合(网络连接列表)
    private List<ScanResult> scanResults; //检测到接入点信息类 集合

    //描述任何Wifi连接状态
    private WifiInfo wifiInfo;
    private Context mContext;

    WifiManager.WifiLock wifiLock; //能够阻止wifi进入睡眠状态，使wifi一直处于活跃状态
    public WifiManager wifiManager;

    /**
     * singleton
     *
     * @param context context
     * @return WifiAdmin
     */
    public static WifiAdmin getInstance(Context context) {

        if (wifiAdmin == null) {
            synchronized (WifiAdmin.class) {
                if (wifiAdmin == null) {
                    wifiAdmin = new WifiAdmin(context);
                }
            }
            return wifiAdmin;
        }
        return wifiAdmin;
    }

    private WifiAdmin(Context context) {
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //获取连接信息
        this.wifiInfo = this.wifiManager.getConnectionInfo();
        this.mContext = context;
        wifiConfigurations = wifiManager.getConfiguredNetworks();
    }

    /**
     * 是否存在网络信息
     *
     * @param str 热点名称
     * @return WifiConfiguration
     */
    public WifiConfiguration isExsits(String str) {
        Iterator localIterator = this.wifiManager.getConfiguredNetworks().iterator();
        WifiConfiguration localWifiConfiguration;
        do {
            if (!localIterator.hasNext()) return null;
            localWifiConfiguration = (WifiConfiguration) localIterator.next();
        } while (!localWifiConfiguration.SSID.equals("\"" + str + "\""));
        return localWifiConfiguration;
    }

    /**
     * 锁定WifiLock，当下载大文件时需要锁定
     **/
    public void acquireWifiLock() {
        this.wifiLock.acquire();
    }

    /**
     * 创建一个WifiLock
     **/
    public void createWifiLock() {
        this.wifiLock = this.wifiManager.createWifiLock("Test");
    }

    /**
     * 解锁WifiLock
     **/
    public void releaseWifilock() {
        if (wifiLock.isHeld()) { //判断时候锁定
            wifiLock.release();
            wifiLock = null;
        }
    }


    /**
     * get whether ic_wifi is enabled.
     *
     * @return whether ic_wifi is enabled.
     */
    public boolean isEnable() {
        return wifiManager.isWifiEnabled();
    }

    /**
     * 打开Wifi
     **/
    public void enableWifi() {
        if (!this.wifiManager.isWifiEnabled()) { //当前wifi不可用
            this.wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭Wifi
     **/
    public void disableWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 端口指定id的wifi
     **/
    public void disconnectWifi(int paramInt) {
        this.wifiManager.disableNetwork(paramInt);
    }

    /**
     * 添加指定网络
     **/
    public void addNetwork(WifiConfiguration paramWifiConfiguration) {
        int id = wifiManager.addNetwork(paramWifiConfiguration);
        if (id != -1) {
            wifiManager.enableNetwork(id, true);
        }
    }

    /**
     * 连接指定配置好的网络
     *
     * @param index 配置好网络的ID
     */
    public void connectConfiguration(int index) {
        // 索引大于配置好的网络索引返回
        if (index > wifiConfigurations.size()) {
            return;
        }
        //连接配置好的指定ID的网络
        wifiManager.enableNetwork(wifiConfigurations.get(index).networkId, true);
    }

    /**
     * connect network id.
     *
     * @param networkId network id
     */
    public void connecNetworkId(int networkId) {
        wifiManager.enableNetwork(networkId, true);
    }

    /**
     * 根据wifi信息创建或关闭一个热点
     *
     * @param paramWifiConfiguration WifiConfiguration
     * @param paramBoolean           关闭标志
     */
    public void createWifiAP(WifiConfiguration paramWifiConfiguration, boolean paramBoolean) {
        try {
            Class localClass = this.wifiManager.getClass();
            Class[] arrayOfClass = new Class[2];
            arrayOfClass[0] = WifiConfiguration.class;
            arrayOfClass[1] = Boolean.TYPE;
            Method localMethod = localClass.getMethod("setWifiApEnabled", arrayOfClass);
            WifiManager localWifiManager = this.wifiManager;
            Object[] arrayOfObject = new Object[2];
            arrayOfObject[0] = paramWifiConfiguration;
            arrayOfObject[1] = Boolean.valueOf(paramBoolean);
            localMethod.invoke(localWifiManager, arrayOfObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建一个wifi信息
     *
     * @param ssid          名称
     * @param passawrd      密码
     * @param encryptType   有3个参数，1是无密码，2是简单密码，3是wap加密
     * @param isConnectWifi 是"ap"还是"ic_wifi"
     * @return WifiConfiguration
     */
    public WifiConfiguration createWifiInfo(String ssid, String passawrd, int encryptType, boolean isConnectWifi, boolean isHiddenSSID) {
        //配置网络信息类
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        //设置配置网络属性
        wifiConfiguration.allowedAuthAlgorithms.clear();
        wifiConfiguration.allowedGroupCiphers.clear();
        wifiConfiguration.allowedKeyManagement.clear();
        wifiConfiguration.allowedPairwiseCiphers.clear();
        wifiConfiguration.allowedProtocols.clear();

        if (isConnectWifi) { //wifi连接
            wifiConfiguration.SSID = ("\"" + ssid + "\"");
//            WifiConfiguration existEonfiguration = isExsits(ssid);
//            if (existEonfiguration != null) {
//                wifiManager.removeNetwork(existEonfiguration.networkId); //从列表中删除指定的网络配置网络
//            }
            if (encryptType == SECURITY_NONE) { //没有密码
                wifiConfiguration.hiddenSSID = isHiddenSSID;
                wifiConfiguration.wepKeys[0] = "";
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfiguration.wepTxKeyIndex = 0;
            } else if (encryptType == SECURITY_WEP) { //简单密码
                wifiConfiguration.hiddenSSID = isHiddenSSID;
                wifiConfiguration.wepKeys[0] = ("\"" + passawrd + "\"");
                wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfiguration.wepTxKeyIndex = 0;
            } else if (encryptType == SECURITY_WPA_PSK) { //wpa加密
                wifiConfiguration.preSharedKey = ("\"" + passawrd + "\"");
//                wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfiguration.hiddenSSID = isHiddenSSID;
                wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            }
        } else {//"ap" wifi热点
            wifiConfiguration.SSID = ssid;
            wifiConfiguration.allowedAuthAlgorithms.set(1);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfiguration.wepTxKeyIndex = 0;
            if (encryptType == SECURITY_NONE) {  //没有密码
                wifiConfiguration.wepKeys[0] = "";
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfiguration.wepTxKeyIndex = 0;
            } else if (encryptType == SECURITY_WEP) { //简单密码
                wifiConfiguration.hiddenSSID = true;//网络上不广播ssid
                wifiConfiguration.wepKeys[0] = passawrd;
            } else if (encryptType == SECURITY_WPA_PSK) {//wpa加密
                wifiConfiguration.preSharedKey = passawrd;
                wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            }
        }
        return wifiConfiguration;
    }

    /**
     * 获取热点名
     **/
    public String getApSSID() {
        try {
            Method localMethod = this.wifiManager.getClass().getDeclaredMethod("getWifiApConfiguration", new Class[0]);
            if (localMethod == null) return null;
            Object localObject1 = localMethod.invoke(this.wifiManager, new Object[0]);
            if (localObject1 == null) return null;
            WifiConfiguration localWifiConfiguration = (WifiConfiguration) localObject1;
            if (localWifiConfiguration.SSID != null) return localWifiConfiguration.SSID;
            Field localField1 = WifiConfiguration.class.getDeclaredField("mWifiApProfile");
            if (localField1 == null) return null;
            localField1.setAccessible(true);
            Object localObject2 = localField1.get(localWifiConfiguration);
            localField1.setAccessible(false);
            if (localObject2 == null) return null;
            Field localField2 = localObject2.getClass().getDeclaredField("SSID");
            localField2.setAccessible(true);
            Object localObject3 = localField2.get(localObject2);
            if (localObject3 == null) return null;
            localField2.setAccessible(false);
            String str = (String) localObject3;
            return str;
        } catch (Exception localException) {
        }
        return null;
    }

    public String getCurrentSSID() {
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();

        if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            String ssid = info.getSSID();
            return ssid;
        }
        return "unknown";
    }

    /**
     * 获取wifi名
     **/
    public String getBSSID() {
        if (this.wifiInfo == null)
            return "NULL";
        return this.wifiInfo.getBSSID();
    }

    /**
     * 得到配置好的网络
     **/
    public List<WifiConfiguration> getConfiguration() {
        return this.wifiConfigurations;
    }

    /**
     * 获取ip地址
     **/
    public int getIPAddress() {
        return (wifiInfo == null) ? 0 : wifiInfo.getIpAddress();
    }

    /**
     * 获取物理地址(Mac)
     **/
    public String getMacAddress() {
        return (wifiInfo == null) ? "NULL" : wifiInfo.getMacAddress();
    }

    /**
     * 获取网络id
     **/
    public int getNetworkId() {
        return (wifiInfo == null) ? 0 : wifiInfo.getNetworkId();
    }

    /**
     * 获取热点创建状态
     **/
    public int getWifiApState() {
        try {
            int i = ((Integer) this.wifiManager.getClass()
                    .getMethod("getWifiApState", new Class[0])
                    .invoke(this.wifiManager, new Object[0])).intValue();
            return i;
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return 4;   //未知wifi网卡状态
    }

    /**
     * 获取wifi连接信息
     **/
    public WifiInfo getWifiInfo() {
        return this.wifiManager.getConnectionInfo();
    }

    /**
     * 得到网络列表
     **/
    public List<ScanResult> getWifiList() {
        return scanResults;
    }

    /**
     * 查看扫描结果
     **/
    public StringBuilder lookUpScan() {
        StringBuilder localStringBuilder = new StringBuilder();
        for (int i = 0; i < scanResults.size(); i++) {
            localStringBuilder.append("Index_" + Integer.valueOf(i + 1).toString() + ":");
            //将ScanResult信息转换成一个字符串包
            //其中把包括：BSSID、SSID、capabilities、frequency、level
            localStringBuilder.append((scanResults.get(i)).toString());
            localStringBuilder.append("\n");
        }
        return localStringBuilder;
    }

    /**
     * 设置wifi搜索结果
     **/
    public List<ScanResult> updateWifiList() {
        this.scanResults = this.wifiManager.getScanResults();
        return scanResults;
    }

    /**
     * 开始搜索wifi
     **/
    public void startScan() {
        this.wifiManager.startScan();
    }


    public static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_WPA_PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    public static int getSecurity(ScanResult scanResult) {
        if (!TextUtils.isEmpty(scanResult.SSID)) {
            String capabilities = scanResult.capabilities;

            if (!TextUtils.isEmpty(capabilities)) {
                if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                    return SECURITY_WPA_PSK;
                } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                    return SECURITY_WEP;
                } else {
                    return SECURITY_NONE;
                }
            }
        }
        return -1;
    }

    public static String formatSSID(String ssid) {
        if (ssid == null) {
            return null;
        }

        if (ssid.charAt(0) == '"' && ssid.charAt(ssid.length() - 1) == '"') {
            return ssid;
        }

        return String.format("\"%s\"", ssid);
    }
}
