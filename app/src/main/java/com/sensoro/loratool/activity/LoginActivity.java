package com.sensoro.loratool.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.sensoro.lora.setting.server.LoRaSettingServerImpl;
import com.sensoro.lora.setting.server.bean.LoginRsp;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.store.DeviceDataDao;
import com.sensoro.loratool.store.PreferencesHelper;
import com.sensoro.loratool.utils.AESUtil;
import com.sensoro.loratool.utils.PermissionUtils;
import com.sensoro.loratool.utils.PermissionsResultObserve;
import com.sensoro.loratool.utils.Utils;
import com.sensoro.loratool.widget.StatusBarCompat;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity implements Constants, PermissionsResultObserve {
    @BindView(R.id.login_btn)
    Button login_btn;
    @BindView(R.id.login_email)
    EditText login_email;
    @BindView(R.id.login_pwd)
    EditText login_pwd;
    @BindView(R.id.alpha_img)
    ImageView alphaImage;
    ProgressDialog progressDialog = null;
    LoRaSettingApplication app;
    private static final int MY_REQUEST_PERMISSION_CODE = 0x14;
    private static final ArrayList<String> FORCE_REQUIRE_PERMISSIONS = new ArrayList<String>() {
        {
            add(Manifest.permission.INTERNET);
            add(Manifest.permission.READ_EXTERNAL_STORAGE);
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            add(Manifest.permission.ACCESS_FINE_LOCATION);
            add(Manifest.permission.ACCESS_COARSE_LOCATION);
            add(Manifest.permission.CAMERA);
        }
    };
    private PermissionUtils mPermissionUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.transparent));
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        app = (LoRaSettingApplication) getApplication();
        progressDialog = new ProgressDialog(this);
        mPermissionUtils = new PermissionUtils(this);
        mPermissionUtils.registerObserver(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPermissionUtils.requestPermission(FORCE_REQUIRE_PERMISSIONS, true, MY_REQUEST_PERMISSION_CODE);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_login;
    }

//    private void requirePermission() {
//
//        // Here, thisActivity is the current activity
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
//
//
//            } else {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{
//                                Manifest.permission.ACCESS_COARSE_LOCATION},
//                        100);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private long[] mHits = new long[7]; // 数组长度代表点击次数

    @OnClick(R.id.alpha_img)
    public void showSwitchApi() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - 1200)) {
            showPasswordDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionUtils.onRequestPermissionsResult(MY_REQUEST_PERMISSION_CODE, requestCode, permissions, grantResults,
                FORCE_REQUIRE_PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPermissionUtils.onActivityResult(requestCode, resultCode, data, MY_REQUEST_PERMISSION_CODE);
    }

    private void showPasswordDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.dialog_input, null);
        builder.setView(view);
        builder.setCancelable(true);
        final EditText input_edt = (EditText) view
                .findViewById(R.id.dialog_edit);//输入内容
        Button btn_cancel = (Button) view
                .findViewById(R.id.btn_cancel);//取消按钮
        Button btn_confirm = (Button) view
                .findViewById(R.id.btn_confirm);//确定按钮
        //取消或确定按钮监听事件处理
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = input_edt.getText().toString();
                if (str.equals("SENSORO")) {
                    switchApi();
                } else {
                    Toast.makeText(app, "密码错误！", Toast.LENGTH_SHORT).show();
                }
                dialog.cancel();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
    }

    private int scope_selectedIndex = 0;

    private void switchApi() {
        final String[] urlArr = new String[]{"IOT", "MOCHA"};

        SharedPreferences sp = getSharedPreferences(PREFERENCE_SCOPE, Context.MODE_PRIVATE);
        String url = sp.getString(PREFERENCE_KEY_URL, null);
        if (url != null) {
            LoRaSettingServerImpl.SCOPE = url;
            if (url.equals(LoRaSettingServerImpl.SCOPE_MOCHA)) {
                scope_selectedIndex = 1;
            }
        }
        Dialog alertDialog = new AlertDialog.Builder(this).
                setTitle("环境切换")
                .setSingleChoiceItems(urlArr, scope_selectedIndex, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        scope_selectedIndex = which;
                    }
                }).setPositiveButton("确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (scope_selectedIndex == 0) {
                            login_btn.setBackground(getResources().getDrawable(R.drawable.shape_button));
                            LoRaSettingServerImpl.SCOPE = LoRaSettingServerImpl.SCOPE_IOT;
                        } else {
                            login_btn.setBackground(getResources().getDrawable(R.drawable.shape_button_mocha));
                            LoRaSettingServerImpl.SCOPE = LoRaSettingServerImpl.SCOPE_MOCHA;
                        }
                        PreferencesHelper.getInstance().saveScopeData(app, LoRaSettingServerImpl.SCOPE);
                        Toast.makeText(LoginActivity.this, urlArr[scope_selectedIndex], Toast.LENGTH_SHORT).show();
                    }
                }).
                        setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                            }
                        }).
                        create();
        alertDialog.show();
    }


    private void readLoginData() {
        SharedPreferences sp = getSharedPreferences(PREFERENCE_LOGIN, Context.MODE_PRIVATE);
        String name = sp.getString(PREFERENCE_KEY_NAME, null);
        String pwd = sp.getString(PREFERENCE_KEY_PWD, null);
        if (name != null) {
            String aes_pwd = AESUtil.decode(pwd);
            if (name != null) {
                login_email.setText(name);
            }
            if (pwd != null) {
                login_pwd.setText(aes_pwd);
            }
        }
        SharedPreferences spScope = getSharedPreferences(PREFERENCE_SCOPE, Context.MODE_PRIVATE);
        String url = spScope.getString(PREFERENCE_KEY_URL, null);
        if (url != null) {
            if (url.equals(LoRaSettingServerImpl.SCOPE_MOCHA)) {
                login_btn.setBackground(getResources().getDrawable(R.drawable.shape_button_mocha));
            } else {
                login_btn.setBackground(getResources().getDrawable(R.drawable.shape_button));
            }
        }


    }

    private void showAuthDialog(final LoginRsp response) {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra("servername", response.getName());
        intent.putExtra("name", login_email.getText().toString());
        intent.putExtra("pwd", login_pwd.getText().toString());
        intent.putExtra("expires", response.getExpires());
        intent.putExtra("sessionId", response.getSessionId());
        startActivity(intent);
    }

    @OnClick(R.id.login_btn)
    public void doLogin() {
        DeviceDataDao.clearWithNotExistInServer();
        progressDialog.setMessage(getResources().getString(R.string.tips_logining));
        progressDialog.show();
        final String username = login_email.getText().toString().trim();
        final String pwd = login_pwd.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, R.string.tips_username_empty, Toast.LENGTH_SHORT).show();
            progressDialog.cancel();
            return;
        }
        if (pwd.isEmpty()) {
            Toast.makeText(this, R.string.tips_login_pwd_empty, Toast.LENGTH_SHORT).show();
            progressDialog.cancel();
            return;
        }

        app.loRaSettingServer.login(username, pwd, new Response.Listener<LoginRsp>() {
            @Override
            public void onResponse(LoginRsp response) {
                // go to station list.
//                Settings settings = response.getSettings();
                progressDialog.cancel();
                List<String> permission = response.getPermission();
                for (int i = 0; i < Constants.permission.length; i++) {
                    Constants.permission[i] = false;
                }
                for (int i = 0; i < permission.size(); i++) {
                    switch (permission.get(i)) {
                        case "sCfgByBle":
                            Constants.permission[0] = true;
                            break;
                        case "sCfgByWifi":
                            Constants.permission[1] = true;
                            break;
                        case "sCfgToPrivateCloud":
                            Constants.permission[2] = true;
                            break;
                        case "sUpgrade":
                            Constants.permission[3] = true;
                            break;
                        case "dCfgByBle":
                            Constants.permission[4] = true;
                            break;
                        case "dCfgToPrivateCloud":
                            Constants.permission[5] = true;
                            break;
                        case "dUpgrade":
                            Constants.permission[6] = true;
                            break;
                    }
                }
                PreferencesHelper.getInstance().savePermissionData(app);

                if (response.isTwofactorauth()) {
                    showAuthDialog(response);
                } else {
                    Utils.checkBleStatus(getApplicationContext());
                    PreferencesHelper.getInstance().saveLoginData(app, response.getName(), username, pwd, response
                            .getExpires(), response.getSessionId());
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    intent.putExtra("name", response.getName());
                    startActivity(intent);
                    finish();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.cancel();
                if (error.networkResponse != null) {
                    try {
                        byte data[] = error.networkResponse.data;
                        if (data != null) {
                            String dataString = new String(data);
                            JSONObject error_msg = new JSONObject(dataString);
                            if (error_msg.getInt("err_code") == 902) {
                                Toast.makeText(LoginActivity.this, R.string.tips_user_info_error, Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.tips_network_error, Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, R.string.tips_network_error, Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        mPermissionUtils.unregisterObserver(this);
        super.onDestroy();
    }

    @Override
    public void onPermissionGranted() {
        readLoginData();
        Log.d("loginAc", "onPermissionGranted: 权限获取完毕 ");
    }

    @Override
    public void onPermissionDenied() {

    }
}
