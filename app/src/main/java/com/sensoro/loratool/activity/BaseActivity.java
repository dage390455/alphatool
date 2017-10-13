package com.sensoro.loratool.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.sensoro.loratool.R;
import com.sensoro.loratool.widget.StatusBarCompat;

import java.lang.reflect.Method;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by fangping on 2016/7/21.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.actionbar_bg));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("", "BaseActivity onActivityResult===>");
    }

    public void resetRootLayout() {
        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.layout_root);
        FrameLayout.LayoutParams rootParams = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        rootParams.topMargin = StatusBarCompat.getStatusBarHeight(this);
        rootLayout.setLayoutParams(rootParams);
    }

    protected abstract int getLayoutResId();
    /**
     * 判断是否存在虚拟按键
     * @return
     */
    public boolean checkDeviceHasNavigationBar() {
        boolean hasNavigationBar = false;
        Resources rs = getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;
    }

}
