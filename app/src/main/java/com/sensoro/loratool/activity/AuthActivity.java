package com.sensoro.loratool.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.sensoro.lora.setting.server.bean.DeviceTyps;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.store.PreferencesHelper;
import com.sensoro.loratool.utils.AESUtil;
import com.sensoro.loratool.utils.Utils;
import com.sensoro.loratool.widget.NumberKeyboardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by sensoro on 17/6/30.
 */

public class AuthActivity extends AppCompatActivity implements Constants, NumberKeyboardView.OnNumberClickListener {

    @BindView(R.id.auth_keyboard)
    NumberKeyboardView mNumberKeyBoard;
    @BindView(R.id.auth_btn1)
    Button button1;
    @BindView(R.id.auth_btn2)
    Button button2;
    @BindView(R.id.auth_btn3)
    Button button3;
    @BindView(R.id.auth_btn4)
    Button button4;
    @BindView(R.id.auth_btn5)
    Button button5;
    @BindView(R.id.auth_btn6)
    Button button6;
    @BindView(R.id.auth_forward)
    ImageButton forwardBtn;
    private int textCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
        mNumberKeyBoard.setOnNumberClickListener(this);

    }

    @Override
    public void onNumberReturn(String number) {
        if (textCount < 6) {
//            authText.append(number);
            switch (textCount) {
                case 0:
                    button1.setText(number);
                    break;
                case 1:
                    button2.setText(number);
                    break;
                case 2:
                    button3.setText(number);
                    break;
                case 3:
                    button4.setText(number);
                    break;
                case 4:
                    button5.setText(number);
                    break;
                case 5:
                    button6.setText(number);
                    break;
            }
            textCount++;
        }
    }


    @Override
    public void onNumberDelete() {
        if (textCount > 0) {
            switch (textCount) {
                case 1:
                    button1.setText("");
                    break;
                case 2:
                    button2.setText("");
                    break;
                case 3:
                    button3.setText("");
                    break;
                case 4:
                    button4.setText("");
                    break;
                case 5:
                    button5.setText("");
                    break;
                case 6:
                    button6.setText("");
                    break;
            }

            textCount--;

        }
    }

    @OnClick(R.id.auth_iv_close)
    public void close() {
        this.finish();
    }

    @OnClick(R.id.auth_forward)
    public void forward() {
        if (textCount == 6) {
            String code = button1.getText().toString() + button2.getText().toString() + button3.getText().toString() + button4.getText().toString() + button5.getText().toString() + button6.getText().toString();
            doSecondAuth(code);
        } else {
            Toast.makeText(this, R.string.tips_auth_length_error, Toast.LENGTH_SHORT).show();
        }
    }

    //146424209
    private void saveLoginData() {
        String name = this.getIntent().getStringExtra("name");
        String pwd = this.getIntent().getStringExtra("pwd");
        String expires = this.getIntent().getStringExtra("expires");
        String sessionId = this.getIntent().getStringExtra("sessionId");
        String servername = this.getIntent().getStringExtra("servername");
        String aes_pwd = AESUtil.encode(pwd);
        SharedPreferences sp = getSharedPreferences(PREFERENCE_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_SERVER_NAME, servername);
        editor.putString(PREFERENCE_KEY_NAME, name);
        editor.putString(PREFERENCE_KEY_PWD, aes_pwd);
        editor.putString(PREFERENCE_KEY_EXPIRES, expires);
        editor.putString(PREFERENCE_KEY_SESSION_ID, sessionId);
        editor.commit();
    }

    private void doSecondAuth(String pinCode) {
        final LoRaSettingApplication app = (LoRaSettingApplication) getApplication();
        final String name = this.getIntent().getStringExtra("name");
        final String servername = this.getIntent().getStringExtra("servername");
        app.loRaSettingServer.secondAuth(pinCode, new Response.Listener<ResponseBase>() {
            @Override
            public void onResponse(ResponseBase response) {
                if (response.getErr_code() == 0) {
                    getAccountDevicesType();

                    Utils.checkBleStatus(getApplicationContext());
                    Intent intent = new Intent();
                    intent.setClass(AuthActivity.this, MainActivity.class);
                    intent.putExtra("name", servername);
                    startActivity(intent);
                    saveLoginData();
                    AuthActivity.this.finish();
                } else {
                    Toast.makeText(app, R.string.tips_auth_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                byte data[] = error.networkResponse.data;
                if (data != null) {
                    String errorMsg = new String(data);
                    try {
                        JSONObject jsonObject = new JSONObject(errorMsg);
                        String jsonMsg = jsonObject.getString("err_msg");
                        Toast.makeText(AuthActivity.this, jsonMsg, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(AuthActivity.this, R.string.tips_network_error, Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void getAccountDevicesType() {
        final LoRaSettingApplication app = (LoRaSettingApplication) getApplication();
        app.loRaSettingServer.getDeviceTypes(
                new Response.Listener<DeviceTyps>() {
                    @Override
                    public void onResponse(DeviceTyps response) {
                        if (response != null && response.getData() != null) {
                            DeviceTyps.DataBean data = response.getData();
                            List<String> devices = data.getDevices();
                            if (devices != null && devices.size() > 0) {
                                PreferencesHelper.getInstance().saveDeviceTypes(AuthActivity.this, devices);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int i = 1;
                        String s = new String(error.networkResponse.data);
                        int statusCode = error.networkResponse.statusCode;

                    }
                });
    }

}
