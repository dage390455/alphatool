package com.sensoro.loratool.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.sensoro.lora.setting.server.LoRaSettingServerImpl;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.store.LoraDbHelper;
import com.umeng.analytics.MobclickAgent;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class SplashActivity extends BaseActivity implements Constants {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        SharedPreferences sp = getSharedPreferences(PREFERENCE_LOGIN, Context.MODE_PRIVATE);
        String expires = sp.getString(PREFERENCE_KEY_EXPIRES, null);
        String sessionId = sp.getString(PREFERENCE_KEY_SESSION_ID, null);
        String serverName = sp.getString(PREFERENCE_KEY_SERVER_NAME, null);
        if (expires != null && sessionId != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            try {
                long expiresTime = formatter.parse(expires).getTime();
                long currentTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
                if (currentTime > expiresTime) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    initPermissionData();
                    LoRaSettingApplication application = (LoRaSettingApplication) getApplication();
                    application.loRaSettingServer.setSessionId(sessionId);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("name", serverName);
                    startActivity(intent);
                }
//                startActivity(new Intent(this,CaptureActivity.class));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        this.finish();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_splash;
    }

    public void init() {
        initSeverUrl();
        initUMSDK();
        LoraDbHelper.instance.clearTable(LoraDbHelper.TABLE_DEVICE_OUT);
    }

    public void initPermissionData() {
        SharedPreferences sp = getSharedPreferences(PREFERENCE_LOGIN, Context.MODE_PRIVATE);
        Constants.permission[0] = sp.getBoolean(PREFERENCE_KEY_PERMISSION_0, false);
        Constants.permission[1] = sp.getBoolean(PREFERENCE_KEY_PERMISSION_1, false);
        Constants.permission[2] = sp.getBoolean(PREFERENCE_KEY_PERMISSION_2, false);
        Constants.permission[3] = sp.getBoolean(PREFERENCE_KEY_PERMISSION_3, false);
        Constants.permission[4] = sp.getBoolean(PREFERENCE_KEY_PERMISSION_4, false);
        Constants.permission[5] = sp.getBoolean(PREFERENCE_KEY_PERMISSION_5, false);
        Constants.permission[6] = sp.getBoolean(PREFERENCE_KEY_PERMISSION_6, false);
    }

    private void initUMSDK() {
        MobclickAgent.onPageStart("登录");
    }

    private void initSeverUrl() {
        try {
            ApplicationInfo appInfo = this.getPackageManager()
                    .getApplicationInfo(getPackageName(),
                            PackageManager.GET_META_DATA);
            String msg = appInfo.metaData.getString("InstallChannel");
            if (msg.equals("Mocha")) {
                LoRaSettingServerImpl.SCOPE = LoRaSettingServerImpl.SCOPE_MOCHA;
            } else {
                LoRaSettingServerImpl.SCOPE = LoRaSettingServerImpl.SCOPE_IOT;
            }
            SharedPreferences sp = getSharedPreferences(PREFERENCE_SCOPE, Context.MODE_PRIVATE);
            String url = sp.getString(PREFERENCE_KEY_URL, null);
            if (url != null) {
                LoRaSettingServerImpl.SCOPE = url;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }


}
