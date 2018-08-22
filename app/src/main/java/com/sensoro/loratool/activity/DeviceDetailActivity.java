package com.sensoro.loratool.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.DeviceDetailACRecylerAdaper;
import com.sensoro.loratool.base.BaseActivity;
import com.sensoro.loratool.imainview.IDeviceDetailAcView;
import com.sensoro.loratool.presenter.DeviceDetailAcPresenter;
import com.sensoro.loratool.utils.DateUtil;
import com.sensoro.loratool.widget.BatteryView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class DeviceDetailActivity extends BaseActivity<IDeviceDetailAcView,DeviceDetailAcPresenter> 
        implements IDeviceDetailAcView ,View.OnClickListener{

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
    private int battery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
    }

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_device_detail);
        initView();
        mPresenter.initData(this);
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
        mBatteryView = (BatteryView)findViewById(R.id.ac_device_detail_battery_view);
        mImvConfig = (ImageView)findViewById(R.id.ac_device_detail_imv_config);
        mTvStateTime = (TextView) findViewById(R.id.ac_device_detail_tv_state_time);
        
        mImvConfig.setOnClickListener(this);
        LayoutInflater inflater = LayoutInflater.from(this);
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
        ImageView closeIV = (ImageView) mBottomPopupView.findViewById(R.id.menu_iv_close);
        closeIV.setVisibility(View.GONE);
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

    }

    @Override
    protected void onStart() {
        super.onStart();
        mBatteryView.setBattery(battery);

    }

    @Override
    protected DeviceDetailAcPresenter createPresenter() {
        return new DeviceDetailAcPresenter();
    }


    @Override
    public void initWidget(DeviceInfo deviceInfo) {
//        mTvName.setText(deviceInfo.);
        if(deviceInfo.getRssi()!=0){
            mTvNear.setVisibility(View.VISIBLE);
        }else{
            mTvNear.setVisibility(View.GONE);
        }
        mTvVersion.setText(String.format(Locale.CHINA,"V %s",deviceInfo.getFirmwareVersion()));
        mTvLocation.setText(deviceInfo.getName());
        initTvState(deviceInfo.getNormalStatus(),deviceInfo.getLastUpTime());
        battery = deviceInfo.getBattery();
//        mBatteryView.setBattery(battery);
        mTvElectricQuantity.setText(String.format(Locale.CHINA,"%d%%", battery));
        initTvTest(deviceInfo);
        initRcContent(deviceInfo);

    }

    private void initTvTest(DeviceInfo deviceInfo) {
        String tags = getIntent().getStringExtra("tags");
        if (tags!=null&&tags.length()>0) {
            mTvTest.setText(tags);
        }else{
            mTvTest.setVisibility(View.GONE);
        }

    }

    private void initRcContent(DeviceInfo deviceInfo) {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        ArrayList<String> valueList = mPresenter.initRCValueList(deviceInfo);
        ArrayList<String> keyList = mPresenter.initRCKeyList();
        DeviceDetailACRecylerAdaper mAdapter = new DeviceDetailACRecylerAdaper(this);
        mAdapter.setKeyList(keyList);
        mAdapter.setValueList(valueList);
        mRcContent.setLayoutManager(gridLayoutManager);
        mRcContent.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL));
        mRcContent.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mRcContent.setAdapter(mAdapter);
    }


    private void initTvState(int normalStatus,long lastUpTime) {
        String defStr = getString(R.string.unknow);
        Drawable drawable ;
        switch (normalStatus) {
            case 0:
                drawable = getResources().getDrawable(R.drawable.shape_oval);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(getResources().getColor(R.color.status_normal), PorterDuff.Mode
                        .MULTIPLY);

                defStr = getString(R.string.status_normal);
                break;
            case 1:
                drawable = getResources().getDrawable(R.drawable.shape_status_fault);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(getResources().getColor(R.color.status_fault), PorterDuff.Mode
                        .MULTIPLY);
                defStr = getString(R.string.status_fault);
                break;
            case 2:
                drawable = getResources().getDrawable(R.drawable.shape_status_fault);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(getResources().getColor(R.color.status_serious), PorterDuff
                        .Mode.MULTIPLY);
                defStr = getString(R.string.status_serious);
                break;
            case 3:
                drawable = getResources().getDrawable(R.drawable.shape_status_timeout);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(getResources().getColor(R.color.status_timeout), PorterDuff
                        .Mode.MULTIPLY);
                defStr = getString(R.string.status_timeout);
                break;
            case -1:
                drawable = getResources().getDrawable(R.drawable.shape_status_inactive);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(getResources().getColor(R.color.status_inactive), PorterDuff
                        .Mode.MULTIPLY);
                defStr = getString(R.string.status_inactive);
                break;
            case 4:
                drawable = getResources().getDrawable(R.drawable.shape_status_offline);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(getResources().getColor(R.color.status_offline), PorterDuff
                        .Mode.MULTIPLY);
                defStr = getString(R.string.status_offline);
            default:
                drawable = getResources().getDrawable(R.drawable.shape_status_inactive);
                drawable.setBounds(0, 0, drawable != null ? drawable.getMinimumWidth() : 0,
                        drawable.getMinimumHeight());
                drawable.setColorFilter(getResources().getColor(R.color.status_inactive), PorterDuff
                        .Mode.MULTIPLY);
                break;
        }

        mTvState.setCompoundDrawables(drawable, null, null, null);
        mTvState.setText(defStr);
        mTvStateTime.setText(DateUtil.getDateDiffWithFormat(this,lastUpTime,"MM-DD"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ac_device_detail_imv_config:
                if (mBottomPopupWindow.isShowing()) {
                    mImvConfig.setImageResource(R.drawable.menu_btn_up);
                    mBottomPopupWindow.dismiss();
                }else{
                    mImvConfig.setImageResource(R.drawable.menu_btn_down);
                    mBottomPopupWindow.showAtLocation(mImvConfig, Gravity.TOP,0,0);
                }
                break;
            case R.id.menu_iv_config:
                break;
            case R.id.menu_iv_cloud:
                break;
            case R.id.menu_iv_upgrade:
                break;
            case R.id.menu_iv_signal:
                break;
            case R.id.menu_iv_clear:
                break;

        }
    }
}
