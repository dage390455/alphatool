package com.sensoro.loratool.store;

import android.content.Context;
import android.content.SharedPreferences;

import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.utils.AESUtil;

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

}
