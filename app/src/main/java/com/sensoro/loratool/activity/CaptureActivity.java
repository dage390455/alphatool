package com.sensoro.loratool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sensoro.loratool.R;
import com.sensoro.loratool.base.BaseActivity;
import com.sensoro.loratool.imainview.ICaptureActivityView;
import com.sensoro.loratool.presenter.CaptureActivityPresenter;
import com.sensoro.loratool.widget.AlphaToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;


public class CaptureActivity extends BaseActivity<ICaptureActivityView, CaptureActivityPresenter> implements
        QRCodeView.Delegate, ICaptureActivityView, CompoundButton.OnCheckedChangeListener, TextView.OnEditorActionListener {

    @BindView(R.id.ac_capture_imv_close)
    ImageView acCaptureImvClose;
    @BindView(R.id.ac_capture_scan_view)
    ZXingView acCaptureScanView;
    @BindView(R.id.ac_capture_et_input_sn)
    EditText acCaptureEtInputSn;
    @BindView(R.id.ac_capture_switch_is_multi)
    SwitchCompat acCaptureSwitchIsMulti;
    @BindView(R.id.ac_capture_imv_flash)
    ImageView acCaptureImvFlash;
    private boolean isFlashOn;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_capture_upgrade);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);

    }

    @Override
    protected void onStart() {
        super.onStart();
        startScan();
    }

    @Override
    protected void onStop() {
        stopScan();
//        AlphaToast.INSTANCE.cancelToast();
        super.onStop();


    }

    @Override
    protected CaptureActivityPresenter createPresenter() {
        return new CaptureActivityPresenter();
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        mPresenter.processScanResult(result);
    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }

    @Override
    public void startScan() {
        acCaptureScanView.startSpotAndShowRect();
    }

    @Override
    public void stopScan() {
        acCaptureScanView.stopCamera();
    }

    @OnClick({R.id.ac_capture_imv_flash, R.id.ac_capture_imv_close})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ac_capture_imv_flash:
                acCaptureScanView.getCameraPreview().surfaceCreated(null);
                if (isFlashOn) {
                    acCaptureScanView.closeFlashlight();
                    acCaptureImvFlash.setImageResource(R.drawable.zxing_flash_off);
                } else {
                    acCaptureScanView.openFlashlight();
                    acCaptureImvFlash.setImageResource(R.drawable.zxing_flash_on);
                }
                isFlashOn = !isFlashOn;
                break;
            case R.id.ac_capture_imv_close:
                mPresenter.imvClose();
                break;
        }
    }


    private void initView() {
        acCaptureScanView.setDelegate(this);
        acCaptureScanView.getScanBoxView().setOnlyDecodeScanBoxArea(true);
        acCaptureScanView.getCameraPreview().setAutoFocusFailureDelay(0);
        isFlashOn = false;
        acCaptureSwitchIsMulti.setOnCheckedChangeListener(this);
        acCaptureEtInputSn.setOnEditorActionListener(this);
    }


    @Override
    public void toastShort(String msg) {
//        Toast.makeText(mActivity.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        AlphaToast.INSTANCE.makeText(mActivity,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {
//        Toast.makeText(mActivity.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        AlphaToast.INSTANCE.makeText(mActivity.getApplicationContext(),msg,Toast.LENGTH_LONG).show();

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mPresenter.setSwitchBtnState(isChecked);
    }

    @Override
    public void startAC(Intent intent) {

    }

    @Override
    public void finishAc() {

    }

    @Override
    public void startACForResult(Intent intent, int requestCode) {

    }

    @Override
    public void setIntentResult(int resultCode) {

    }

    @Override
    public void setIntentResult(int resultCode, Intent data) {
        setResult(resultCode, data);
        finish();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        mPresenter.processEditResult(v);
        return false;
    }

}
