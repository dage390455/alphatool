package com.sensoro.loratool.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.fragment.SettingsInputDialogFragment;
import com.sensoro.loratool.ble.BLEDevice;
import com.sensoro.loratool.ble.SensoroConnectionCallback;
import com.sensoro.loratool.ble.SensoroStation;
import com.sensoro.loratool.ble.SensoroStationConfiguration;
import com.sensoro.loratool.ble.SensoroStationConnection;
import com.sensoro.loratool.ble.SensoroWriteCallback;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.event.OnPositiveButtonClickListener;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sensoro on 16/10/11.
 */

public class AdvanceSettingStationActivity extends BaseActivity implements Constants, OnPositiveButtonClickListener, SensoroConnectionCallback, SensoroWriteCallback {


    private SensoroStationConnection sensoroConnection;
    private SensoroStation sensoroStation;
    private ProgressDialog progressDialog;

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
            SensoroStationConfiguration sensoroStationConfiguration = builder.build();
            sensoroConnection.writeStationAdvanceConfiguration(sensoroStationConfiguration, this);
        }catch (Exception e) {
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
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(netIdTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_STATION_NETID);
    }

    @OnClick(R.id.station_cloud_rl_address)
    public void doCloudAddress() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(cloudAddressTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_STATION_CLOUD_ADDRESS);
    }

    @OnClick(R.id.station_cloud_rl_port)
    public void doCloudPort() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(cloudPortTextView.getText().toString());
        dialogFragment.show(getFragmentManager(), SETTINGS_STATION_CLOUD_PORT);
    }

    @OnClick(R.id.station_cloud_rl_key)
    public void doKey() {
        SettingsInputDialogFragment dialogFragment = SettingsInputDialogFragment.newInstance(keyTextView.getText().toString());
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensoroConnection != null) {
            sensoroConnection.disconnect();
        }
    }
}
