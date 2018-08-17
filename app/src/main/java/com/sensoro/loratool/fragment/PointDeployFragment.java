package com.sensoro.loratool.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.MainActivity;
import com.sensoro.loratool.base.BaseFragment;
import com.sensoro.loratool.imainview.IPointDeployFragmentView;
import com.sensoro.loratool.presenter.PointDeployFragmentPresenter;
import com.sensoro.loratool.utils.ProgressUtils;


import cn.bingoogolapple.qrcode.core.QRCodeView;

import static com.sensoro.loratool.fragment.DeviceFragment.INPUT;

/**
 * Created by sensoro on 17/7/24.
 */

public class PointDeployFragment extends BaseFragment<IPointDeployFragmentView,
        PointDeployFragmentPresenter>
        implements IPointDeployFragmentView, View.OnClickListener, QRCodeView.Delegate {
    private static final String TAG = PointDeployFragment.class.getSimpleName();

    private QRCodeView mQRCodeView;
    private ImageView mMenuImageView;
    private ImageView flashImageView;
    private ImageView manualImageView;

    private ProgressUtils mProgressUtils;
    private boolean isFlashOn = false;

    public static PointDeployFragment newInstance(String input) {
        PointDeployFragment pointDeployFragment = new PointDeployFragment();
        Bundle args = new Bundle();
        args.putString(INPUT, input);
        pointDeployFragment.setArguments(args);
        return pointDeployFragment;
    }


    private void initView() {
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mRootFragment.getActivity()).build());
        flashImageView = (ImageView) mRootView.findViewById(R.id.zxing_capture_iv_flash);
        manualImageView = (ImageView) mRootView.findViewById(R.id.zxing_capture_iv_manual);
        mMenuImageView = (ImageView) mRootView.findViewById(R.id.deploy_iv_menu_list);
        mMenuImageView.setColorFilter(getResources().getColor(R.color.white));
        mMenuImageView.setOnClickListener(this);
        flashImageView.setOnClickListener(this);
        manualImageView.setOnClickListener(this);
        mQRCodeView = mRootView.findViewById(R.id.scan_view);
        mQRCodeView.setDelegate(this);
        mQRCodeView.getScanBoxView().setOnlyDecodeScanBoxArea(true);
        mQRCodeView.getCameraPreview().setAutoFocusFailureDelay(0);
    }

    @Override
    public void onDestroyView() {
        mProgressUtils.destroyProgress();
        mQRCodeView.onDestroy();
        super.onDestroyView();
    }

//    public void hiddenRootView() {
//        if (mRootView != null) {
//            mRootView.setVisibility(View.GONE);
//        }
//    }
//
//    public void showRootView() {
//        try {
//            mRootView.setVisibility(View.VISIBLE);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    protected void initData(Context activity) {
        mPresenter.initData(activity);
        initView();
    }


    @Override
    protected int initRootViewId() {
        return R.layout.fragment_point_deploy;
    }

    @Override
    protected PointDeployFragmentPresenter createPresenter() {
        return new PointDeployFragmentPresenter();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zxing_capture_iv_flash:
                //改变mSurfaceCreated变量强制开启闪光灯 针对1.2.4
                mQRCodeView.getCameraPreview().surfaceCreated(null);
                if (isFlashOn) {
                    mQRCodeView.closeFlashlight();
                } else {
                    mQRCodeView.openFlashlight();
                }
                isFlashOn = !isFlashOn;
                break;
            case R.id.zxing_capture_iv_manual:
                mPresenter.openSNTextAc();
                break;
            case R.id.deploy_iv_menu_list:
//                ((MainActivity) getActivity()).openMenu();
                break;
            default:
                break;
        }
    }


    @Override
    public void setFlashLightState(boolean isOn) {
        flashImageView.setBackgroundResource(isOn ? R.drawable.zxing_flash_on : R.drawable.zxing_flash_off);
    }

    @Override
    public void startScan() {
        mQRCodeView.startCamera();
        mQRCodeView.startSpotDelay(1000);
        mQRCodeView.showScanRect();
        mQRCodeView.startSpotAndShowRect();
        mQRCodeView.startSpot();
    }

    @Override
    public void stopScan() {
        mQRCodeView.stopCamera();
    }


    @Override
    public void toastShort(String msg) {
//        SensoroToast.makeText(mRootFragment.getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {

    }


    @Override
    public void startAC(Intent intent) {
        mRootFragment.getActivity().startActivity(intent);
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
    public void onScanQRCodeSuccess(String result) {
        mPresenter.processResult(result);
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }

    @Override
    public void onFragmentStart() {
        startScan();
    }

    @Override
    public void onFragmentStop() {
        stopScan();
    }
}
