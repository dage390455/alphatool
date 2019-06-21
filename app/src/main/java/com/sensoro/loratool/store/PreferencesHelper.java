package com.sensoro.loratool.store;

import android.content.Context;
import android.content.SharedPreferences;

import com.sensoro.loratool.R;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.utils.AESUtil;

import java.util.List;

/**
 * Created by sensoro on 17/7/4.
 */

public class PreferencesHelper implements Constants{

    private volatile static PreferencesHelper instance;

    private PreferencesHelper() {

    }

    public static PreferencesHelper getInstance() {
        if (instance == null) {
            synchronized (PreferencesHelper.class) {
                if (instance == null) {
                    instance = new PreferencesHelper();
                }
            }
        }
        return instance;
    }


    public void saveLoginData(Context context,String servername, String username, String pwd, String expires, String sessionId) {
        String aes_pwd = AESUtil.encode(pwd);
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_SERVER_NAME, servername);
        editor.putString(PREFERENCE_KEY_NAME, username);
        editor.putString(PREFERENCE_KEY_PWD, aes_pwd);
        editor.putString(PREFERENCE_KEY_EXPIRES, expires);
        editor.putString(PREFERENCE_KEY_SESSION_ID, sessionId);
        editor.commit();
    }

    public void saveScopeData(Context context, String url) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_SCOPE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_URL, url);
        editor.commit();
    }

    public void savePermissionData(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREFERENCE_KEY_PERMISSION_0, Constants.permission[0]);
        editor.putBoolean(PREFERENCE_KEY_PERMISSION_1, Constants.permission[1]);
        editor.putBoolean(PREFERENCE_KEY_PERMISSION_2, Constants.permission[2]);
        editor.putBoolean(PREFERENCE_KEY_PERMISSION_3, Constants.permission[3]);
        editor.putBoolean(PREFERENCE_KEY_PERMISSION_4, Constants.permission[4]);
        editor.putBoolean(PREFERENCE_KEY_PERMISSION_5, Constants.permission[5]);
        editor.putBoolean(PREFERENCE_KEY_PERMISSION_6, Constants.permission[6]);
        editor.commit();
    }

    public void saveDeviceTypes(Context context, List<String> devices) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_DEVICE_TYPES, Context.MODE_PRIVATE);
        String[] hardwareArray = context.getResources().getStringArray(R.array.filter_device_hardware_array);
        SharedPreferences.Editor edit = sp.edit();
        StringBuilder sb = new StringBuilder("##,");
        StringBuilder nameSb = new StringBuilder("通用,");

        for (String device : devices) {
            String temp = null;
            for (int i = 0; i < Constants.DEVICE_HARDWARE_TYPE.length; i++) {
                if (Constants.DEVICE_HARDWARE_TYPE[i].contains(device)) {
                    temp = hardwareArray[i];
                    if(device.equals("t1")){
                        temp+="(T1型)";
                    }
                    break;
                }
            }
            if (temp == null) {
                nameSb.append(device).append("(未支持设备),");
            }else{
                nameSb.append(temp).append(",");
            }
            sb.append(device).append(",");
        }

        edit.putString(PREFERENCE_KEY_DEVICE_TYPE,sb.toString());
        edit.putString(PREFERENCE_KEY_DEVICE_TYPE_NAME,nameSb.toString());
        edit.apply();

    }
//    public List<Boolean>
}
