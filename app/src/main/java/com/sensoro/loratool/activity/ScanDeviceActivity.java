package com.sensoro.loratool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.base.BaseActivity;
import com.sensoro.loratool.imainview.IScanDeviceAcView;
import com.sensoro.loratool.presenter.ScanDeviceAcPresenter;
import com.sensoro.loratool.utils.ProgressUtils;
import com.sensoro.loratool.widget.StatusBarCompat;

import cn.bingoogolapple.qrcode.core.QRCodeView;

public class ScanDeviceActivity extends BaseActivity<IScanDeviceAcView,ScanDeviceAcPresenter> implements IScanDeviceAcView,View.OnClickListener
,QRCodeView.Delegate  {

    private ImageView mFlashImageView;
    private ImageView mManualImageView;
    private QRCodeView mQRCodeView;
    private ProgressUtils mProgressUtils;
    private boolean isFlashOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarCompat.setStatusBarColor(this,getResources().getColor(R.color.actionbar_bg));
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_scan_device);
        initView();
        mPresenter.initData(this);
    }


    @Override
    protected ScanDeviceAcPresenter createPresenter() {
        return new ScanDeviceAcPresenter();
    }


    @Override
    protected void onStart() {
        super.onStart();
        startScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }

    @Override
    protected void onDestroy() {
       mQRCodeView.onDestroy();
       mProgressUtils.destroyProgress();
       ((LoRaSettingApplication)getApplication()).scanDeviceView = null;
       super.onDestroy();
    }


    @Override
    public void showProgressDialog() {
        mProgressUtils.showProgress();
    }

    @Override
    public void dismissProgressDialog() {
        mProgressUtils.dismissProgress();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ac_scan_capture_iv_flash:
                mQRCodeView.getCameraPreview().surfaceCreated(null);
                if (isFlashOn) {
                    mQRCodeView.closeFlashlight();
                } else {
                    mQRCodeView.openFlashlight();
                }
                isFlashOn = !isFlashOn;
                break;
            case R.id.ac_scan_capture_iv_manual:
                Intent intent = new Intent(this, InputSNActivity.class);
                startAC(intent);
                break;
        }

    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        mPresenter.processResult(result);
    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }

    private void initView() {
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(this).build());
        mFlashImageView = (ImageView) findViewById(R.id.ac_scan_capture_iv_flash);
        mManualImageView = (ImageView) findViewById(R.id.ac_scan_capture_iv_manual);
        mFlashImageView.setOnClickListener(this);
        mManualImageView.setOnClickListener(this);
        mQRCodeView = (QRCodeView) findViewById(R.id.ac_scan_scan_view);
        mQRCodeView.setDelegate(this);
        mQRCodeView.getScanBoxView().setOnlyDecodeScanBoxArea(true);
        mQRCodeView.getCameraPreview().setAutoFocusFailureDelay(0);

        ((LoRaSettingApplication)getApplication()).scanDeviceView = this;
    }


    @Override
    public void setFlashLightState(boolean isOn) {
        mFlashImageView.setBackgroundResource(isOn ? R.drawable.zxing_flash_on : R.drawable.zxing_flash_off);
    }

    @Override
    public void shortToast(int message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startAC(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void finishAc() {
        finish();
    }

    @Override
    public void startScan() {
        mQRCodeView.startSpotDelay(1000);
        mQRCodeView.startSpotAndShowRect();
    }

    @Override
    public void stopScan() {
        mQRCodeView.stopCamera();
    }
}
