package com.sensoro.loratool.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sensoro.libbleserver.ble.callback.SensoroConnectionCallback;
import com.sensoro.libbleserver.ble.callback.SensoroWriteCallback;
import com.sensoro.libbleserver.ble.connection.SensoroStationConnection;
import com.sensoro.libbleserver.ble.entity.BLEDevice;
import com.sensoro.libbleserver.ble.entity.SensoroStation;
import com.sensoro.libbleserver.ble.entity.SensoroStationConfiguration;
import com.sensoro.loratool.R;
import com.sensoro.loratool.fragment.SettingsInputDialogFragment;
import com.sensoro.loratool.fragment.SettingsSingleChoiceItemsFragment;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.event.OnPositiveButtonClickListener;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by sensoro on 16/10/11.
 */

public class AdvanceSettingStationActivity extends BaseActivity implements Constants, OnPositiveButtonClickListener,
        SensoroConnectionCallback, SensoroWriteCallback, View.OnClickListener {


    private SensoroStationConnection sensoroConnection;
    private SensoroStation sensoroStation;
    private ProgressDialog progressDialog;

    /**
     * 单通道基站DR
     */
    private final String[] ITEM_SGL_DR_STR = {"SF12 / 125 kHz", "SF11 / 125 kHz", "SF10 / 125 kHz", "SF9 / 125 kHz",
            "SF8 / 125 kHz", "SF7 / 125 kHz"};


    @BindView(R.id.station_cloud_rl_netid)
    RelativeLayout netIdRelativeLayout;
    @BindView(R.id.station_cloud_rl_address)
    RelativeLayout cloudAddressRelativeLayout;
    @BindView(R.id.station_cloud_rl_port)
    RelativeLayout cloudPortRelativeLayout;
    @BindView(R.id.station_cloud_rl_key)
    RelativeLayout keyRelativeLayout;

    @BindView(R.id.station_cloud_tv_netid)
    TextView netIdTextView;
    @BindView(R.id.station_cloud_tv_address)
    TextView cloudAddressTextView;
    @BindView(R.id.station_cloud_tv_port)
    TextView cloudPortTextView;
    @BindView(R.id.station_cloud_tv_key)
    TextView keyTextView;
    //
    @BindView(R.id.settings_station_tv_sgl_gr)
    TextView sgl_drTextView;
    @BindView(R.id.settings_station_tv_sgl_freq)
    TextView sgl_freqTextView;

    @BindView(R.id.settings_station_rl_sgl_dr)
    RelativeLayout sgl_drLayout;
    @BindView(R.id.settings_station_rl_sgl_freq)
    RelativeLayout sgl_freqLayout;
    private SettingsSingleChoiceItemsFragment dialogFragment;
    private String stationType;
    private float sgl_freq_f;
    private int sgl_dr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onPageStart("基站私有云配置V1.1");
        initData();
    }

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

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_advcance_station;
    }

    private void refresh() {
        try {
            setContentView(R.layout.activity_advcance_station);
            ButterKnife.bind(this);
            resetRootLayout();
            if (!TextUtils.isEmpty(stationType) && "scgateway".equals(stationType)) {
                sgl_drLayout.setOnClickListener(this);
                sgl_freqLayout.setOnClickListener(this);
                int sgl_dr = sensoroStation.getSgl_dr();
                this.sgl_dr = sgl_dr;
                int sgl_freq = sensoroStation.getSgl_freq();
                sgl_drLayout.setVisibility(VISIBLE);
                sgl_drTextView.setText(ITEM_SGL_DR_STR[sgl_dr]);
                sgl_freqLayout.setVisibility(VISIBLE);
                sgl_freq_f = sgl_freq / 1000000f;
                sgl_freqTextView.setText(sgl_freq_f + " MHz");
            } else {
                sgl_drLayout.setVisibility(GONE);
                sgl_freqLayout.setVisibility(GONE);
            }


            netIdTextView.setText(sensoroStation.getNetid());
            cloudAddressTextView.setText(sensoroStation.getCloudaddress());
            cloudPortTextView.setText(sensoroStation.getCloudport());
            keyTextView.setText(sensoroStation.getKey());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(AdvanceSettingStationActivity.this, R.string.connect_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, R.string.connect_success, Toast.LENGTH_SHORT).show();
    }

    private void initData() {

        sensoroStation = this.getIntent().getParcelableExtra(Constants.EXTRA_NAME_STATION);
        stationType = this.getIntent().getStringExtra(Constants.EXTRA_NAME_STATION_TYPE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                AdvanceSettingStationActivity.this.finish();
            }
        });
        connectStation(sensoroStation);

    }

    public void connectStation(SensoroStation station) {
        try {
            progressDialog.setMessage(getString(R.string.connecting));
            progressDialog.show();
            sensoroConnection = new SensoroStationConnection(this, station);
            sensoroConnection.connect(sensoroStation.getPwd(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @OnClick(R.id.station_cloud_iv_back)
    public void back() {
        this.finish();
    }

    @OnClick(R.id.station_cloud_tv_save)
    public void save() {
        try {
            progressDialog.show();
            progressDialog.setMessage(getString(R.string.saving));
            SensoroStationConfiguration.Builder builder = new SensoroStationConfiguration.Builder();
            builder.setNetId(netIdTextView.getText().toString());
            builder.setCloudAddress(cloudAddressTextView.getText().toString());
            builder.setCloudPort(cloudPortTextView.getText().toString());
            builder.setKey(keyTextView.getText().toString());
            if (!TextUtils.isEmpty(stationType) && "scgateway".equals(stationType)) {
                builder.setSgl_dr(sgl_dr);
                builder.setSgl_freq((int) (sgl_freq_f * 1000000));
            }
            SensoroStationConfiguration sensoroStationConfiguration = builder.build();
            sensoroConnection.writeStationAdvanceConfiguration(sensoroStationConfiguration, this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectedSuccess(final BLEDevice bleDevice, int cmd) {
        String sn = sensoroStation.getSn();
        String firmwareVersion = sensoroStation.getFirmwareVersion();
        sensoroStation = (SensoroStation) bleDevice;
        sensoroStation.setFirmwareVersion(firmwareVersion);
        sensoroStation.setSn(sn);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                refresh();
            }
        });
    }

    @Override
    public void onConnectedFailure(int errorCode) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(AdvanceSettingStationActivity.this, R.string.connect_failed, Toast.LENGTH_SHORT).show();
                AdvanceSettingStationActivity.this.finish();
            }
        });
    }

    @Override
    public void onDisconnected() {

    }


    @Override
    public void onWriteSuccess(Object o, int cmd) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                AdvanceSettingStationActivity.this.finish();
                Toast.makeText(AdvanceSettingStationActivity.this, R.string.save_succ, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onWriteFailure(int errorCode, final int cmd) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(AdvanceSettingStationActivity.this, R.string.save_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.station_cloud_rl_netid)
    public void doNetId() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(netIdTextView.getText()
                .toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_STATION_NETID);
    }

    @OnClick(R.id.station_cloud_rl_address)
    public void doCloudAddress() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(cloudAddressTextView
                .getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_STATION_CLOUD_ADDRESS);
    }

    @OnClick(R.id.station_cloud_rl_port)
    public void doCloudPort() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(cloudPortTextView
                .getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_STATION_CLOUD_PORT);
    }

    @OnClick(R.id.station_cloud_rl_key)
    public void doKey() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(keyTextView.getText()
                .toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_STATION_KEY);
    }


    @Override
    public void onPositiveButtonClick(String tag, Bundle bundle) {
        if (tag.equals(SETTINGS_STATION_NETID)) {
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
            netIdTextView.setText(text);
        } else if (tag.equals(SETTINGS_STATION_CLOUD_ADDRESS)) {
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
            cloudAddressTextView.setText(text);
        } else if (tag.equals(SETTINGS_STATION_CLOUD_PORT)) {
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
            cloudPortTextView.setText(text);
        } else if (tag.equals(SETTINGS_STATION_KEY)) {
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
            keyTextView.setText(text);
        } else if (tag.equals(SETTINGS_SGL_DR)) {
            //单通道基站DR选择
            sgl_dr = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            sgl_drTextView.setText(ITEM_SGL_DR_STR[sgl_dr]);
            sensoroStation.setSgl_dr(sgl_dr);
        } else if (tag.equals(SETTINGS_SGL_FREQ)) {
            String text = bundle.getString(SettingsInputDialogFragment.INPUT);
            float v;
            try {
                v = Float.parseFloat(text);
                sgl_freqTextView.setText(v + "MHz");
                sgl_freq_f = v;
            } catch (Exception e) {
                Toast.makeText(this, "请输入正确的数字格式", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensoroConnection != null) {
            sensoroConnection.disconnect();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_station_rl_sgl_dr:
                //单通道基站DR
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(ITEM_SGL_DR_STR, sgl_dr);
                dialogFragment.show(getFragmentManager(), SETTINGS_SGL_DR);
                break;
            case R.id.settings_station_rl_sgl_freq:
                //单通道基站频点
//                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(encryptItems, getEncryptIndex
// (encrypt));
//                dialogFragment.show(getFragmentManager(), SETTINGS_ENCRYPT);
                SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(sgl_freq_f + "");
                dialogFragment.show(getFragmentManager(), SETTINGS_SGL_FREQ);
                break;
        }

    }
}
