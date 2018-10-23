package com.sensoro.loratool.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.sensoro.lora.setting.server.bean.UpgradeInfo;
import com.sensoro.lora.setting.server.bean.UpgradeListRsp;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.UpgradeInfoAdapter;
import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.widget.RecycleViewDivider;
import com.sensoro.loratool.widget.RecycleViewItemClickListener;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sensoro on 16/9/12.
 */

public class UpgradeFirmwareListActivity extends BaseActivity implements Constants, RecycleViewItemClickListener, View.OnClickListener {

    public static final String EXTERN_DIRECTORY_NAME = "sensoro_dfu";
    @BindView(R.id.upgrade_firmware_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.upgrade_firmware_refresh)
    ImageView refreshIv;
    @BindView(R.id.upgrade_firmware_shadow)
    LinearLayout mShadowLayout;
    private RecycleViewDivider mDivider = null;
    private UpgradeInfoAdapter mUpgradeInfoAdapter = null;
    private ProgressDialog progressDialog = null;

    private ArrayList<SensoroDevice> targetDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_firmware);
        ButterKnife.bind(this);
        init();
        MobclickAgent.onPageStart("设备固件升级");
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_upgrade_firmware;
    }

    private void init() {
        targetDeviceList = getIntent().getParcelableArrayListExtra(EXTRA_NAME_DEVICE_LIST);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        int size = targetDeviceList.size();
        if (size > 0) {
            Collections.sort(targetDeviceList, new Comparator<SensoroDevice>() {
                @Override
                public int compare(SensoroDevice lhs, SensoroDevice rhs) {
                    return lhs.getFirmwareVersion().compareTo(rhs.getFirmwareVersion());
                }
            });
            mUpgradeInfoAdapter = new UpgradeInfoAdapter(this, targetDeviceList.get(0), this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setAdapter(mUpgradeInfoAdapter);
            int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.y1);
            mDivider = new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, spacingInPixels, R.color.station_item_more_line, false);
            mRecyclerView.addItemDecoration(mDivider);
            requestUpgradeInfo();
        } else {
            this.finish();
            Toast.makeText(this, R.string.target_error, Toast.LENGTH_SHORT).show();
        }

    }


    @OnClick(R.id.settings_upgrade_firmware_back)
    public void back() {
        this.finish();
    }

    @OnClick(R.id.upgrade_firmware_refresh)
    public void refresh() {
        requestUpgradeInfo();
    }

    private void requestUpgradeInfo() {
        progressDialog.show();
        LoRaSettingApplication application = (LoRaSettingApplication) getApplication();
        String deviceType = this.getIntent().getStringExtra(Constants.EXTRA_NAME_DEVICE_TYPE);
        String band = this.getIntent().getStringExtra(Constants.EXTRA_NAME_BAND);
        String hv = this.getIntent().getStringExtra(Constants.EXTRA_NAME_DEVICE_HARDWARE_VERSION);
        String fv = this.getIntent().getStringExtra(Constants.EXTRA_NAME_DEVICE_FIRMWARE_VERSION);
        application.loRaSettingServer.deviceUpgradeList(1, deviceType, band, hv, fv, new Response.Listener<UpgradeListRsp>() {
            @Override
            public void onResponse(UpgradeListRsp response) {
                ArrayList<UpgradeInfo> list = (ArrayList) response.getData().getItems();
                progressDialog.dismiss();
                if (list != null) {
                    if (list.size() == 0) {
                        Toast.makeText(UpgradeFirmwareListActivity.this, R.string.tips_no_upgrade, Toast.LENGTH_SHORT).show();
                    }
                    mUpgradeInfoAdapter.setData(list);
                    mUpgradeInfoAdapter.notifyDataSetChanged();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
            }
        });
    }

    private void showPopup(final String url) {
        String[] mItems = {getString(R.string.upgrade), getString(R.string.advance), getString(R.string.cancel)};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(mItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent(UpgradeFirmwareListActivity.this, UpgradeDeviceListActivityTest.class);
                        intent.putExtra(EXTRA_NAME_DEVICE_LIST, targetDeviceList);
                        intent.putExtra(EXTRA_URL, url);
                        intent.putExtra(EXTRA_UPGRADE_INDEX, 0);
                        intent.putExtra(EXTRA_NAME_DEVICE_HARDWARE_VERSION, getIntent().getStringExtra(EXTRA_NAME_DEVICE_HARDWARE_VERSION));
                        intent.putExtra(EXTRA_NAME_BAND, getIntent().getStringExtra(EXTRA_NAME_BAND));
                        intent.putExtra(EXTRA_NAME_DEVICE_FIRMWARE_VERSION, getIntent().getStringExtra(EXTRA_NAME_DEVICE_FIRMWARE_VERSION));
                        startActivity(intent);
                        break;
                    case 1:
                        Intent advanceIntent = new Intent(UpgradeFirmwareListActivity.this, UpgradeDeviceListActivityTest.class);
                        advanceIntent.putExtra(EXTRA_NAME_DEVICE_LIST, targetDeviceList);
                        advanceIntent.putExtra(EXTRA_URL, url);
                        advanceIntent.putExtra(EXTRA_UPGRADE_INDEX, 1);
                        advanceIntent.putExtra(EXTRA_NAME_DEVICE_HARDWARE_VERSION, getIntent().getStringExtra(EXTRA_NAME_DEVICE_HARDWARE_VERSION));
                        advanceIntent.putExtra(EXTRA_NAME_BAND, getIntent().getStringExtra(EXTRA_NAME_BAND));
                        advanceIntent.putExtra(EXTRA_NAME_DEVICE_FIRMWARE_VERSION, getIntent().getStringExtra(EXTRA_NAME_DEVICE_FIRMWARE_VERSION));
                        startActivity(advanceIntent);
                        break;
                    case 2:
                        break;
                }
            }
        });
        builder.create();
        builder.show();
    }

    private void startDeviceActivity() {

    }


    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onItemClick(View view, int position) {
        UpgradeInfo upgradeInfo = mUpgradeInfoAdapter.getData(position);
        showPopup(upgradeInfo.getUrl());

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.upgrade_ll_upgrade:
                startDeviceActivity();
                break;
            case R.id.upgrade_ll_advance:
                startDeviceActivity();
                break;
            case R.id.upgrade_ll_cancel:
                break;
        }
    }
}
