package com.sensoro.loratool.activity;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;
import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.fragment.SettingsInputDialogFragment;
import com.sensoro.loratool.activity.fragment.SettingsSingleChoiceItemsFragment;
import com.sensoro.loratool.ble.BLEDevice;
import com.sensoro.loratool.ble.SensoroConnectionCallback;
import com.sensoro.loratool.ble.SensoroStation;
import com.sensoro.loratool.ble.SensoroStationConfiguration;
import com.sensoro.loratool.ble.SensoroStationConnection;
import com.sensoro.loratool.ble.SensoroWriteCallback;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.event.OnPositiveButtonClickListener;
import com.sensoro.loratool.proto.ProtoStationMsgV2;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by sensoro on 16/8/11.
 */

public class SettingStationActivity extends BaseActivity implements Constants, OnPositiveButtonClickListener, View
        .OnClickListener, SensoroWriteCallback, SensoroConnectionCallback {
    private static final String TAG = SettingStationActivity.class.getSimpleName();
    @BindView(R.id.settings_station_rl_access_mode)
    RelativeLayout accessModeLayout;
    @BindView(R.id.settings_station_rl_ip_assignment)
    RelativeLayout assignmentLayout;
    @BindView(R.id.settings_station_rl_ip_address)
    RelativeLayout addressLayout;
    @BindView(R.id.settings_station_rl_router)
    RelativeLayout routerLayout;
    @BindView(R.id.settings_station_rl_dns)
    RelativeLayout dnsLayout;
    @BindView(R.id.settings_station_rl_sec_dns)
    RelativeLayout secDnsLayout;
    @BindView(R.id.settings_station_rl_name)
    RelativeLayout nameLayout;
    @BindView(R.id.settings_station_rl_password)
    RelativeLayout passwordLayout;
    @BindView(R.id.settings_station_rl_subnet)
    RelativeLayout subnetMaskLayout;
    @BindView(R.id.settings_station_rl_encrypt)
    RelativeLayout encryptLayout;
    @BindView(R.id.settings_station_ll_static)
    LinearLayout staticLayout;
    @BindView(R.id.settings_station_ll_wifi)
    LinearLayout wifiLayout;
    @BindView(R.id.settings_station_tv_access_mode)
    TextView accessModeTextView;
    @BindView(R.id.settings_station_tv_ip_assignment)
    TextView assignmentTextView;
    @BindView(R.id.settings_station_tv_ip_address)
    TextView addressTextView;
    @BindView(R.id.settings_station_tv_subnet)
    TextView maskTextView;
    @BindView(R.id.settings_station_tv_router)
    TextView routerTextView;
    @BindView(R.id.settings_station_tv_dns)
    TextView dnsTextView;
    @BindView(R.id.settings_station_tv_sec_dns)
    TextView secDnsTextView;
    @BindView(R.id.settings_station_tv_name)
    TextView nameTextView;
    @BindView(R.id.settings_station_tv_password)
    TextView passwordTextView;
    @BindView(R.id.settings_station_tv_encrypt)
    TextView encryptTextView;

    @BindView(R.id.image_ip_assignment)
    ImageView assignmentImageView;
    @BindView(R.id.image_ip_address)
    ImageView addressImageView;
    @BindView(R.id.image_subnet)
    ImageView maskImageView;
    @BindView(R.id.image_router)
    ImageView routerImageView;
    @BindView(R.id.image_dns)
    ImageView dnsImageView;
    @BindView(R.id.image_sec_dns)
    ImageView secDnsImageView;
    @BindView(R.id.station_iv_back)
    ImageView backView;
    @BindView(R.id.station_tv_save)
    TextView saveView;
    ProgressDialog progressDialog;

    private String[] assignmentItems;
    private String[] accessModeItems;
    private String[] encryptItems;
    private String encrypt;

    private SensoroStationConnection sensoroConnection;
    private SensoroStation sensoroStation;
    private int accessMode;
    private int assignment;
    private String stationType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        connectStation(sensoroStation);
        MobclickAgent.onPageStart("基站配置");
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
        return R.layout.activity_setting_station;
    }

    private void initData() {
        sensoroStation = this.getIntent().getParcelableExtra(Constants.EXTRA_NAME_STATION);
        stationType = this.getIntent().getStringExtra(Constants.EXTRA_NAME_STATION_TYPE);
        if (stationType.equals("station")) {
            accessModeItems = getResources().getStringArray(R.array.station_access_mode_array);
        } else {
            accessModeItems = getResources().getStringArray(R.array.gateway_access_mode_array);
        }
        assignmentItems = getResources().getStringArray(R.array.assignment_array);
        encryptItems = getResources().getStringArray(R.array.encrypt_array);
    }

    private void registerUiEvent() {
        setContentView(R.layout.activity_setting_station);
        ButterKnife.bind(this);
        resetRootLayout();
        accessModeLayout.setOnClickListener(this);
        assignmentLayout.setOnClickListener(this);
        addressLayout.setOnClickListener(this);
        routerLayout.setOnClickListener(this);
        dnsLayout.setOnClickListener(this);
        secDnsLayout.setOnClickListener(this);
        nameLayout.setOnClickListener(this);
        passwordLayout.setOnClickListener(this);
        subnetMaskLayout.setOnClickListener(this);
        wifiLayout.setVisibility(GONE);
        encryptLayout.setOnClickListener(this);
        backView.setOnClickListener(this);
        saveView.setOnClickListener(this);
//        sgl_drLayout.setVisibility(GONE);
//        sgl_freqLayout.setVisibility(GONE);

    }

    private void showArrowImageView() {
        addressImageView.setVisibility(VISIBLE);
        routerImageView.setVisibility(VISIBLE);
        maskImageView.setVisibility(VISIBLE);
        dnsImageView.setVisibility(VISIBLE);
        secDnsImageView.setVisibility(VISIBLE);
        addressLayout.setClickable(true);
        routerLayout.setClickable(true);
        subnetMaskLayout.setClickable(true);
        dnsLayout.setClickable(true);
        secDnsLayout.setClickable(true);
    }

    private void dismissArrowImageView() {
        addressImageView.setVisibility(GONE);
        routerImageView.setVisibility(GONE);
        maskImageView.setVisibility(GONE);
        dnsImageView.setVisibility(GONE);
        secDnsImageView.setVisibility(GONE);
        addressLayout.setClickable(false);
        routerLayout.setClickable(false);
        subnetMaskLayout.setClickable(false);
        dnsLayout.setClickable(false);
        secDnsLayout.setClickable(false);
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                SettingStationActivity.this.finish();
            }
        });
    }

    public void refreshUI() {
        if (accessMode == ProtoStationMsgV2.NwkAccessMode.NWK_MODE_WIFI_VALUE && stationType.equals("gateway")) {
            wifiLayout.setVisibility(VISIBLE);
            assignmentLayout.setClickable(false);
            assignmentImageView.setVisibility(GONE);
        } else {
            wifiLayout.setVisibility(GONE);
            assignmentLayout.setClickable(true);
            assignmentImageView.setVisibility(VISIBLE);
        }
        if (accessMode == ProtoStationMsgV2.NwkAccessMode.NWK_MODE_CELLULAR_VALUE) {
            assignmentLayout.setVisibility(GONE);
            dismissArrowImageView();
        } else {
            assignmentLayout.setVisibility(VISIBLE);
            showArrowImageView();
        }
        if (assignment == ProtoStationMsgV2.IPAllocationMode.IP_ALLOC_DHCP_VALUE) {
            dismissArrowImageView();
        } else {
            showArrowImageView();
        }
    }

    private void refresh() {
        try {
            String[] assignmentArray = this.getResources().getStringArray(R.array.assignment_array);
            accessMode = sensoroStation.getAccessMode();
            if (accessMode == ProtoStationMsgV2.NwkAccessMode.NWK_MODE_CELLULAR_VALUE && stationType.equals
                    ("station")) {
                accessMode = 1;
            }
            assignmentTextView.setText(assignmentArray[sensoroStation.getAllocationMode()]);
            addressTextView.setText(sensoroStation.getIp());
            routerTextView.setText(sensoroStation.getGateway());
            dnsTextView.setText(sensoroStation.getPdns());
            secDnsTextView.setText(sensoroStation.getAdns());
            nameTextView.setText(sensoroStation.getSid());
            passwordTextView.setText(sensoroStation.getPwd());
            maskTextView.setText(sensoroStation.getMask());
            encrypt = sensoroStation.getEncrpt();
            encryptTextView.setText(encrypt);

//        if (sensoroStation.getAccessMode() == ProtoStationMsgV2.NwkAccessMode.NWK_MODE_WIFI_VALUE) {
//            wifiLayout.setVisibility(VISIBLE);
//        } else {
//            wifiLayout.setVisibility(View.GONE);
//        }
            if (stationType.equals("station")) {
                String[] accessModeArray = this.getResources().getStringArray(R.array.station_access_mode_array);
                if (sensoroStation.getAccessMode() == ProtoStationMsgV2.NwkAccessMode.NWK_MODE_CELLULAR_VALUE) {
                    accessModeTextView.setText(accessModeArray[1]);
//                assignmentLayout.setVisibility(GONE);
                } else {
//                assignmentLayout.setVisibility(VISIBLE);
                    accessModeTextView.setText(accessModeArray[0]);
                }
//            if (sensoroStation.getAllocationMode() == ProtoStationMsgV2.IPAllocationMode.IP_ALLOC_DHCP_VALUE) {
//                staticLayout.setVisibility(GONE);
//            } else {
//                staticLayout.setVisibility(VISIBLE);
//            }
            }  else {
                String[] accessModeArray = this.getResources().getStringArray(R.array.gateway_access_mode_array);
                accessModeTextView.setText(accessModeArray[sensoroStation.getAccessMode()]);
//            if (sensoroStation.getAllocationMode() == ProtoStationMsgV2.IPAllocationMode.IP_ALLOC_DHCP_VALUE ||
// sensoroStation.getAccessMode() == ProtoStationMsgV2.NwkAccessMode.NWK_MODE_WIFI_VALUE) {
//                staticLayout.setVisibility(GONE);
//            } else {
//                staticLayout.setVisibility(VISIBLE);
//            }
            }
            refreshUI();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.connect_failed), Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, R.string.connect_success, Toast.LENGTH_SHORT).show();

    }

    public int getEncryptIndex(String encrypt) {
        if (encrypt.equals("WPA-PSK")) {
            return 1;
        } else if (encrypt.equals("WPA2-PSK")) {
            return 2;
        } else {
            return 0;
        }
    }

    public void connectStation(SensoroStation station) {
        try {
            initProgressDialog();
            progressDialog.setMessage(this.getResources().getString(R.string.connecting));
            progressDialog.show();
            sensoroConnection = new SensoroStationConnection(this, station);
            sensoroConnection.connect(sensoroStation.getPwd(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPositiveButtonClick(String tag, Bundle bundle) {
        if (tag.equals(SETTINGS_IP_ADDRESS)) {
            String ip = bundle.getString(SettingsInputDialogFragment.INPUT);
            addressTextView.setText(ip);
            sensoroStation.setIp(ip);
        } else if (tag.equals(SETTINGS_ACCESS_MODE)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            accessMode = index;
            if (stationType.equals("station")) {
                if (index == 1) {
                    accessMode = ProtoStationMsgV2.NwkAccessMode.NWK_MODE_CELLULAR_VALUE;
                }
            }
            sensoroStation.setAccessMode(accessMode);
            accessModeTextView.setText(accessModeItems[index]);

        } else if (tag.equals(SETTINGS_IP_ASSIGNMENT)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            assignment = index;
            sensoroStation.setAllocationMode(index);
            assignmentTextView.setText(assignmentItems[index]);
        } else if (tag.equals(SETTINGS_ROUTER)) {
            String ip = bundle.getString(SettingsInputDialogFragment.INPUT);
            routerTextView.setText(ip);
            sensoroStation.setIp(ip);
        } else if (tag.equals(SETTINGS_SUBNET_MASK)) {
            String mask = bundle.getString(SettingsInputDialogFragment.INPUT);
            maskTextView.setText(mask);
            sensoroStation.setMask(mask);
        } else if (tag.equals(SETTINGS_DNS)) {
            String pdns = bundle.getString(SettingsInputDialogFragment.INPUT);
            dnsTextView.setText(pdns);
            sensoroStation.setPdns(pdns);
        } else if (tag.equals(SETTINGS_SEC_DNS)) {
            String adns = bundle.getString(SettingsInputDialogFragment.INPUT);
            dnsTextView.setText(adns);
            sensoroStation.setAdns(adns);
        } else if (tag.equals(SETTINGS_NAME)) {
            String name = bundle.getString(SettingsInputDialogFragment.INPUT);
            nameTextView.setText(name);
            sensoroStation.setSid(name);
        } else if (tag.equals(SETTINGS_PASSWORD)) {
            String pwd = bundle.getString(SettingsInputDialogFragment.INPUT);
            passwordTextView.setText(pwd);
            sensoroStation.setPwd(pwd);
        } else if (tag.equals(SETTINGS_ENCRYPT)) {
            int index = bundle.getInt(SettingsSingleChoiceItemsFragment.INDEX);
            encrypt = encryptItems[index];
            encryptTextView.setText(encrypt);
            sensoroStation.setEncrpt(encrypt);
            if (index == 0) {
                passwordLayout.setVisibility(GONE);
            } else {
                passwordLayout.setVisibility(VISIBLE);
            }
        }
        refreshUI();
    }

    @Override
    public void onClick(View view) {
        if (sensoroStation == null)
            return;
        DialogFragment dialogFragment = null;
        switch (view.getId()) {
            case R.id.station_iv_back:
                sensoroConnection.disconnect();
                finish();
                break;
            case R.id.settings_station_rl_ip_assignment:
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(assignmentItems, sensoroStation
                        .getAllocationMode());
                dialogFragment.show(getFragmentManager(), SETTINGS_IP_ASSIGNMENT);
                break;
            case R.id.settings_station_rl_access_mode:
                int access_mode = sensoroStation.getAccessMode();
                if (access_mode == ProtoStationMsgV2.NwkAccessMode.NWK_MODE_CELLULAR_VALUE && stationType.equals
                        ("station")) {
                    access_mode = 1;
                }
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(accessModeItems, access_mode);
                dialogFragment.show(getFragmentManager(), SETTINGS_ACCESS_MODE);
                break;
            case R.id.settings_station_rl_ip_address:
                if (accessMode != ProtoStationMsgV2.NwkAccessMode.NWK_MODE_CELLULAR_VALUE) {
                    dialogFragment = SettingsInputDialogFragment.newInstance(sensoroStation.getIp());
                    dialogFragment.show(getFragmentManager(), SETTINGS_IP_ADDRESS);
                }
                break;
            case R.id.settings_station_rl_router:
                if (accessMode != ProtoStationMsgV2.NwkAccessMode.NWK_MODE_CELLULAR_VALUE) {
                    dialogFragment = SettingsInputDialogFragment.newInstance(sensoroStation.getGateway());
                    dialogFragment.show(getFragmentManager(), SETTINGS_ROUTER);
                }
                break;
            case R.id.settings_station_rl_subnet:
                if (accessMode != ProtoStationMsgV2.NwkAccessMode.NWK_MODE_CELLULAR_VALUE) {
                    dialogFragment = SettingsInputDialogFragment.newInstance(sensoroStation.getMask());
                    dialogFragment.show(getFragmentManager(), SETTINGS_SUBNET_MASK);
                }
                break;
            case R.id.settings_station_rl_dns:
                if (accessMode != ProtoStationMsgV2.NwkAccessMode.NWK_MODE_CELLULAR_VALUE) {
                    dialogFragment = SettingsInputDialogFragment.newInstance(sensoroStation.getPdns());
                    dialogFragment.show(getFragmentManager(), SETTINGS_DNS);
                }
                break;
            case R.id.settings_station_rl_sec_dns:
                if (accessMode != ProtoStationMsgV2.NwkAccessMode.NWK_MODE_CELLULAR_VALUE) {
                    dialogFragment = SettingsInputDialogFragment.newInstance(sensoroStation.getAdns());
                    dialogFragment.show(getFragmentManager(), SETTINGS_SEC_DNS);
                }
                break;
            case R.id.settings_station_rl_name:
                dialogFragment = SettingsInputDialogFragment.newInstance(sensoroStation.getSid());
                dialogFragment.show(getFragmentManager(), SETTINGS_NAME);
                break;
            case R.id.settings_station_rl_password:
                dialogFragment = SettingsInputDialogFragment.newInstance(sensoroStation.getPwd());
                dialogFragment.show(getFragmentManager(), SETTINGS_PASSWORD);
                break;
            case R.id.settings_station_rl_encrypt:
                dialogFragment = SettingsSingleChoiceItemsFragment.newInstance(encryptItems, getEncryptIndex(encrypt));
                dialogFragment.show(getFragmentManager(), SETTINGS_ENCRYPT);
                break;
            case R.id.station_tv_save:
                try {
                    saveStationConfiguration();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    private void saveStationConfiguration() throws InvalidProtocolBufferException {
        progressDialog.show();
        progressDialog.setMessage(getString(R.string.saving));
        SensoroStationConfiguration.Builder builder = new SensoroStationConfiguration.Builder();
        builder.setSid(sensoroStation.getSid());
        builder.setPassword(sensoroStation.getPwd());
        builder.setAllocationMode(sensoroStation.getAllocationMode());
        builder.setAdns(sensoroStation.getAdns());
        builder.setEncrpt(sensoroStation.getEncrpt());

        builder.setFirmwareVersion(sensoroStation.getFirmwareVersion());
        builder.setHardwareModelName(sensoroStation.getHardwareVersion());
        builder.setPdns(sensoroStation.getPdns());
        builder.setAccessMode(sensoroStation.getAccessMode());
        builder.setMask(sensoroStation.getMask());
        builder.setRouter(sensoroStation.getGateway());
        builder.setIp(sensoroStation.getIp());
        SensoroStationConfiguration sensoroStationConfiguration = builder.build();
        sensoroConnection.writeStationConfiguration(sensoroStationConfiguration, this);
    }

    @OnClick(R.id.station_iv_back)
    public void back() {
        this.finish();
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
                registerUiEvent();
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
                Toast.makeText(SettingStationActivity.this, R.string.connect_failed, Toast.LENGTH_SHORT).show();
                SettingStationActivity.this.finish();
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
                SettingStationActivity.this.finish();
                Toast.makeText(SettingStationActivity.this, R.string.save_succ, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onWriteFailure(int errorCode, final int cmd) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Toast.makeText(SettingStationActivity.this, R.string.save_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (sensoroConnection != null) {
            sensoroConnection.disconnect();
        }
    }

}
