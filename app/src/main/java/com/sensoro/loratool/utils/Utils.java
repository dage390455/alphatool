package com.sensoro.loratool.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.sensoro.loratool.R;
import com.sensoro.loratool.constant.Constants;

import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Utils {

    public static byte[] getSignature(byte[] data, String baseKey) {
        Mac shaMac;
        byte[] secretBytes = baseKey.getBytes();
        byte[] signatureBytes = null;
        try {
            shaMac = Mac.getInstance(Constants.ENCODE);
            SecretKey secretKey = new SecretKeySpec(secretBytes,
                    Constants.ENCODE);
            shaMac.init(secretKey);
            signatureBytes = shaMac.doFinal(data);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return signatureBytes;
    }

    public static void checkBleStatus(Context context) {
        if (!BluetoothManager.isBluetoothEnabled()) {
            Toast.makeText(context, R.string.tips_open_ble, Toast.LENGTH_SHORT).show();
            BluetoothManager.turnOnBluetooth();
        }
    }

    public static boolean checkMajorMinorLegel(String majorMinor) {
        if (TextUtils.isEmpty(majorMinor)) {
            return false;
        } else if (majorMinor.length() > 4) {
            return false;
        } else {
            try {
                int majorMinorValue = Integer.parseInt(majorMinor, 16);
                if (majorMinorValue >= 0 || majorMinorValue <= 65535) {
                    return true;
                } else {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    public static String getDecodeJSONStr(String s) {
        StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String SHA1(String inStr) {
        MessageDigest md = null;
        String outStr = null;
        try {
            md = MessageDigest.getInstance("SHA-1");     //选择SHA-1，也可以选择MD5
            byte[] digest = md.digest(inStr.getBytes());       //返回的是byet[]，要转化为String存储比较方便
            outStr = new String(digest);
        } catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        }
        return outStr;
    }

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    public static byte[] base64Decode(String s) {
        return Base64.decode(s, Base64.DEFAULT);
    }

    public static String base64Encode(byte[] b) {
        return Base64.encodeToString(b, Base64.DEFAULT).trim();
    }

    public static String base64Encode(String hexString) {
        return base64Encode(toByteArray(hexString));
    }

    /**
     * to hex string ({0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88} -> "1122334455667788")
     *
     * @param bytes
     * @return
     */
    public static String toHexString(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int c = bytes[i] & 0xFF;
            chars[i * 2] = HEX[c >>> 4];
            chars[i * 2 + 1] = HEX[c & 0x0F];
        }
        return new String(chars).toLowerCase();
    }

    public static byte[] toByteArray(String hexString) {
        if (hexString == null || hexString.length() == 0) {
            return null;
        }

        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    public static void setEnabledViews(boolean enabled, View... views) {
        if (views == null || views.length == 0) {
            return;
        }
        for (View v : views) {
            v.setEnabled(enabled);
        }
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static double getGPSDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        double EARTH_RADIUS = 6378.137;
        s = s * EARTH_RADIUS;
//        s = Math.round(s * 10000) / 10000;
        return s;
    }

    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }


    /**
     * make true current connect service is wifi
     *
     * @param mContext
     * @return
     */
    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
            return 75;
        }
    }

    public static int getTitleBarHeight(Activity activity) {
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        // 获取标题栏高度
        Window window = activity.getWindow();
        int contentViewTop = activity.getWindow()
                .findViewById(Window.ID_ANDROID_CONTENT).getTop();
        // statusBarHeight是上面所求的状态栏的高度
        int  titleBarHeight = contentViewTop - statusBarHeight;
        return  titleBarHeight;
    }
}
