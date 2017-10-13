package com.sensoro.loratool.utils;

import android.net.wifi.WifiInfo;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by tangrisheng on 2016/5/19.
 * IP Util
 */
public class IPUtil {

    private static final String TAG = IPUtil.class.getSimpleName();

    public static String getWifiIP(WifiInfo wifiInfo) {
        if (wifiInfo == null) {
            return null;
        }
        int ipAddress = wifiInfo.getIpAddress();

        return intToIp(ipAddress);
    }

    public static String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    public static String getGPRSIP() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }

        return null;
    }

    public static String getGateWayIP(String localIP) {
        if (localIP == null) {
            return null;
        }
        String[] strings = localIP.split("\\.");
        if (strings.length != 4) {
            // ip invalid
            return null;
        }
        strings[3] = "1";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(strings[0]);
        stringBuilder.append(".");
        stringBuilder.append(strings[1]);
        stringBuilder.append(".");
        stringBuilder.append(strings[2]);
        stringBuilder.append(".");
        stringBuilder.append(strings[3]);

        return stringBuilder.toString();
    }
}
