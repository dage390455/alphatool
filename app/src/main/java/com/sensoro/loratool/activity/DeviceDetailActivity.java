package com.sensoro.loratool.activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.DeviceDetailACRecycleAdapter;
import com.sensoro.loratool.base.BaseActivity;
import com.sensoro.loratool.imainview.IDeviceDetailAcView;
import com.sensoro.loratool.presenter.DeviceDetailAcPresenter;
import com.sensoro.loratool.widget.BatteryView;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class DeviceDetailActivity extends BaseActivity<IDeviceDetailAcView, DeviceDetailAcPresenter>
        implements IDeviceDetailAcView, View.OnClickListener {

    private RecyclerView mRcContent;
    private TextView mTvReportTime;
    private TextView mTvElectricQuantity;
    private TextView mTvLocation;
    private TextView mTvName;
    private TextView mTvNear;
    private TextView mTvState;
    private TextView mTvTest;
    private TextView mTvVersion;
    private BatteryView mBatteryView;
    private ImageView mImvConfig;
    private View mBottomPopupView;
    private ImageView configImageView;
    private ImageView cloudImageView;
    private ImageView upgradeImageView;
    private ImageView signalImageView;
    private ImageView clearImageView;
    private LinearLayout configLayout;
    private LinearLayout clearLayout;
    private LinearLayout upgradeLayout;
    private LinearLayout signalLayout;
    private LinearLayout cloudLayout;
    private PopupWindow mBottomPopupWindow;
    private TextView mTvStateTime;
    //
    private DeviceDetailACRecycleAdapter mAdapter;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_device_detail);
        initView();
        mPresenter.initData(mActivity);
    }

    private void initView() {
        mRcContent = (RecyclerView) findViewById(R.id.ac_device_detail_recycler_content);
        mTvReportTime = (TextView) findViewById(R.id.ac_device_detail_tv_data_report_time);
        mTvElectricQuantity = (TextView) findViewById(R.id.ac_device_detail_tv_electric_quantity);
        mTvLocation = (TextView) findViewById(R.id.ac_device_detail_tv_location);
        mTvName = (TextView) findViewById(R.id.ac_device_detail_tv_name);
        mTvNear = (TextView) findViewById(R.id.ac_device_detail_tv_near);
        mTvState = (TextView) findViewById(R.id.ac_device_detail_tv_state);
        mTvTest = (TextView) findViewById(R.id.ac_device_detail_tv_test);
        mTvVersion = (TextView) findViewById(R.id.ac_device_detail_tv_version);
        mBatteryView = (BatteryView) findViewById(R.id.ac_device_detail_battery_view);
        mImvConfig = (ImageView) findViewById(R.id.ac_device_detail_imv_config);
        mTvStateTime = (TextView) findViewById(R.id.ac_device_detail_tv_state_time);

        mImvConfig.setOnClickListener(this);
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        mBottomPopupView = inflater.inflate(R.layout.menu_bottom_view, null);
        configImageView = (ImageView) mBottomPopupView.findViewById(R.id.menu_iv_config);
        cloudImageView = (ImageView) mBottomPopupView.findViewById(R.id.menu_iv_cloud);
        upgradeImageView = (ImageView) mBottomPopupView.findViewById(R.id.menu_iv_upgrade);
        signalImageView = (ImageView) mBottomPopupView.findViewById(R.id.menu_iv_signal);
        clearImageView = (ImageView) mBottomPopupView.findViewById(R.id.menu_iv_clear);
        configLayout = (LinearLayout) mBottomPopupView.findViewById(R.id.menu_ll_config);
        clearLayout = (LinearLayout) mBottomPopupView.findViewById(R.id.menu_ll_clear);
        upgradeLayout = (LinearLayout) mBottomPopupView.findViewById(R.id.menu_ll_upgrade);
        signalLayout = (LinearLayout) mBottomPopupView.findViewById(R.id.menu_ll_signal);
        cloudLayout = (LinearLayout) mBottomPopupView.findViewById(R.id.menu_ll_cloud);
        View line = mBottomPopupView.findViewById(R.id.menu_line);
        line.setVisibility(View.GONE);
        ImageView closeIV = (ImageView) mBottomPopupView.findViewById(R.id.menu_iv_close);
        closeIV.setVisibility(View.VISIBLE);
        closeIV.setImageResource(R.drawable.menu_btn_down);
        closeIV.setOnClickListener(this);
        clearLayout.setVisibility(View.GONE);
        configImageView.setOnClickListener(this);
        cloudImageView.setOnClickListener(this);
        upgradeImageView.setOnClickListener(this);
        signalImageView.setOnClickListener(this);
        clearImageView.setOnClickListener(this);
        mBottomPopupView.setFocusableInTouchMode(true);
        mBottomPopupWindow = new PopupWindow(mBottomPopupView, LinearLayout.LayoutParams.MATCH_PARENT,
                WRAP_CONTENT, true);
        mBottomPopupWindow.setAnimationStyle(R.style.menuAnimationFade);
        mBottomPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mBottomPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mImvConfig.setVisibility(View.VISIBLE);
            }
        });
        mAdapter = new DeviceDetailACRecycleAdapter(mActivity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.setBatteryLevel();
    }

    @Override
    protected DeviceDetailAcPresenter createPresenter() {
        return new DeviceDetailAcPresenter();
    }


    @Override
    public void startAc(Intent intent) {
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (mBottomPopupWindow != null) {
            mBottomPopupWindow.dismiss();
            mBottomPopupWindow = null;
        }
        super.onDestroy();

    }

    @Override
    public void showShortToast(String msg) {
        Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setTvNameVisible(boolean isVisible) {
        mTvName.setVisibility(isVisible ? VISIBLE : GONE);
    }

    @Override
    public void setTvNameContent(String nameContent) {
        mTvName.setText(nameContent);
    }

    @Override
    public void setTvNearVisible(boolean isVisible) {
        mTvNear.setVisibility(isVisible ? VISIBLE : GONE);
    }

    @Override
    public void setTvVersionContent(String versionContent) {
        mTvVersion.setText(versionContent);
    }

    @Override
    public void setTvLocationContent(String locationContent) {
        mTvLocation.setText(locationContent);
    }

    @Override
    public void setTvStateCompoundDrawables(Drawable drawable) {
        mTvState.setCompoundDrawables(drawable, null, null, null);
    }

    @Override
    public void setTvStateContent(String stateContent) {
        mTvState.setText(stateContent);
    }

    @Override
    public void setTvStateTime(String stateTime) {
        mTvStateTime.setText(stateTime);
    }

    @Override
    public void setTvElectricQuantityContent(String electricQuantityContent) {
        mTvElectricQuantity.setText(electricQuantityContent);
    }

    @Override
    public void setTvTestContent(String testContent) {
        mTvTest.setText(testContent);
    }

    @Override
    public void setTvTestVisible(boolean isVisible) {
        mTvTest.setVisibility(isVisible ? VISIBLE : GONE);
    }

    @Override
    public void setRcKeyList(ArrayList<String> keyList) {
        mAdapter.setKeyList(keyList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateRcValueList(ArrayList<String> valueList) {
        mAdapter.setValueList(valueList);

    }

    @Override
    public void setRcAdapter() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 2) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mRcContent.setLayoutManager(gridLayoutManager);
        mRcContent.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.HORIZONTAL));
        mRcContent.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
        mRcContent.setAdapter(mAdapter);
    }

    @Override
    public void setTvReportTimeContent(String reportTimeContent) {
        mTvReportTime.setText(reportTimeContent);
    }

    @Override
    public void showPopSettingItem(boolean[] itemVisible) {
        configLayout.setVisibility(itemVisible[0] ? VISIBLE : GONE);
        cloudLayout.setVisibility(itemVisible[0] ? VISIBLE : GONE);
        upgradeLayout.setVisibility(itemVisible[0] ? VISIBLE : GONE);
        signalLayout.setVisibility(itemVisible[0] ? VISIBLE : GONE);
        mBottomPopupWindow.showAtLocation(mImvConfig, Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void setBatteryLevel(int level) {
        mBatteryView.setBattery(level);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ac_device_detail_imv_config:
                mImvConfig.setVisibility(View.INVISIBLE);
                mPresenter.showPopupWindow();
                break;
            case R.id.menu_iv_config:
                mPresenter.config();
                mBottomPopupWindow.dismiss();
                break;
            case R.id.menu_iv_cloud:
                mPresenter.cloud();
                mBottomPopupWindow.dismiss();
                break;
            case R.id.menu_iv_upgrade:
                mPresenter.upgrade();
                break;
            case R.id.menu_iv_signal:
                mPresenter.signal();
                break;
            case R.id.menu_iv_clear:
                break;
            case R.id.menu_iv_close:
                mBottomPopupWindow.dismiss();
                break;

        }
    }
}
