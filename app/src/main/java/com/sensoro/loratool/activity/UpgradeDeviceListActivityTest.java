package com.sensoro.loratool.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.sensoro.libbleserver.ble.BLEDevice;
import com.sensoro.libbleserver.ble.SensoroConnectionCallback;
import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.libbleserver.ble.SensoroDeviceConnection;
import com.sensoro.libbleserver.ble.SensoroDirectWriteDfuCallBack;
import com.sensoro.libbleserver.ble.SensoroWriteCallback;
import com.sensoro.lora.setting.server.bean.ResponseBase;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.UpgradeDeviceAdapter;
import com.sensoro.loratool.base.BaseActivity;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.imainview.IUpgradeDeviceListActivityView;
import com.sensoro.loratool.presenter.UpgradeDeviceListActivityPresenter;
import com.sensoro.loratool.service.DfuService;
import com.sensoro.loratool.store.DeviceDataDao;
import com.sensoro.loratool.utils.DownloadUtil;
import com.sensoro.loratool.utils.LogUtils;
import com.sensoro.loratool.utils.ProgressUtils;
import com.sensoro.loratool.utils.RcItemTouchHelperCallback;
import com.sensoro.loratool.widget.AlphaToast;
import com.sensoro.loratool.widget.RecycleViewDivider;
import com.sensoro.loratool.widget.RecycleViewItemClickListener;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;


/**
 * Created by sensoro on 18/1/8.
 */

public class UpgradeDeviceListActivityTest extends BaseActivity<IUpgradeDeviceListActivityView, UpgradeDeviceListActivityPresenter> implements
        Constants, IUpgradeDeviceListActivityView, RecycleViewItemClickListener{
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 101;
    @BindView(R.id.upgrade_device_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.upgrade_device_add)
    ImageView addIv;
    @BindView(R.id.upgrade_device_start)
    Button mStartButton;
    private RecycleViewDivider mDivider = null;
    private UpgradeDeviceAdapter mUpgradeDeviceAdapter = null;
    private ProgressUtils progressDialog;

    private Handler mHandler;



    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_upgrade_device);
        ButterKnife.bind(this);
        init();
        MobclickAgent.onPageStart("设备升级");
        mPresenter.initData(mActivity);
    }

    private void init() {
        mHandler = new Handler();
        progressDialog = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());;

        initRc();

    }

    private void initRc() {
        mUpgradeDeviceAdapter = new UpgradeDeviceAdapter(this, this);
        RcItemTouchHelperCallback rcItemTouchHelperCallback = new RcItemTouchHelperCallback(mUpgradeDeviceAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(rcItemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mUpgradeDeviceAdapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.y1);
        mDivider = new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, spacingInPixels, R.color.station_item_more_line, false);
        mRecyclerView.addItemDecoration(mDivider);
    }



    @OnClick(R.id.settings_upgrade_device_back)
    public void back() {
        this.finish();
    }

    @OnClick(R.id.upgrade_device_add)
    public void add() {
        if (requireCameraPermission()) {
            Intent intent = new Intent(this, CaptureActivity.class);
            intent.putExtra(ZXING_REQUEST_CODE, ZXING_REQUEST_CODE_SCAN_BEACON);
            intent.putParcelableArrayListExtra(EXTRA_NAME_DEVICE_LIST, mUpgradeDeviceAdapter.getData());
            startActivityForResult(intent, ZXING_REQUEST_CODE_SCAN_BEACON);
        } else {
            Toast.makeText(this, R.string.tips_open_ble_service, Toast.LENGTH_SHORT).show();
        }

    }


    @OnClick(R.id.upgrade_device_add_nearby)
    public void addNearBy() {
        mPresenter.addNearBy();
    }

    @OnClick(R.id.upgrade_device_start)
    public void start() {
        if (!mPresenter.isStartUpgrade) {
            showProgressDialog();
            mPresenter.isStartUpgrade = true;
            mPresenter.requestDownLoadZip();
        }
    }

    private boolean requireCameraPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {


            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ZXING_REQUEST_CODE_RESULT) {

            ArrayList<SensoroDevice> sensoroDeviceArrayList = data.getParcelableArrayListExtra(EXTRA_NAME_DEVICE_LIST);
            System.out.println("UpgradeDeviceList.size=>" + sensoroDeviceArrayList.size());
            mUpgradeDeviceAdapter.getData().clear();
            mUpgradeDeviceAdapter.getData().addAll(sensoroDeviceArrayList);
            mUpgradeDeviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        DfuServiceListenerHelper.registerProgressListener(this, mPresenter);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        DfuServiceListenerHelper.unregisterProgressListener(this, mPresenter);
    }

    @Override
    protected UpgradeDeviceListActivityPresenter createPresenter() {
        return new UpgradeDeviceListActivityPresenter();
    }

    @Override
    public void onItemClick(View view, int position) {

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.destroyProgress();
        }

    }


    @Override
    public void setAddIvVisible(boolean isVisible) {
        addIv.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateRcData(ArrayList<SensoroDevice> targetDeviceList) {
        mUpgradeDeviceAdapter.setData(targetDeviceList);
        mUpgradeDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public ArrayList<SensoroDevice> getAdapterData() {
        return mUpgradeDeviceAdapter.getData();
    }

    @Override
    public void updateRc(int deviceIndex, @StringRes int string) {
        mUpgradeDeviceAdapter.getData(deviceIndex).setDfuInfo(getString(string));
        mUpgradeDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateRcProgressChanged(int deviceIndex,int percent) {
        mUpgradeDeviceAdapter.getData(deviceIndex).setDfuProgress(percent);
        mUpgradeDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateRcPercentAndTip(int deviceIndex,int percent,String dfuInfo) {
        mUpgradeDeviceAdapter.getData(deviceIndex).setDfuProgress(percent);
        mUpgradeDeviceAdapter.getData(deviceIndex).setDfuInfo(dfuInfo);
        mUpgradeDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public SensoroDevice getItemData(int deviceIndex) {
        return mUpgradeDeviceAdapter.getData(deviceIndex);
    }

    @Override
    public void setStartButtonBg(@DrawableRes int drawableId) {
        mStartButton.setBackground(getResources().getDrawable(drawableId));
    }

    @Override
    public void notigyAdapter() {
        mUpgradeDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void showProgressDialog() {
        progressDialog.showProgress();
    }

    @Override
    public void dismissProgressDialog() {
        progressDialog.dismissProgress();
    }

    @Override
    public void toastShort(String msg) {
        AlphaToast.INSTANCE.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {

    }
}
