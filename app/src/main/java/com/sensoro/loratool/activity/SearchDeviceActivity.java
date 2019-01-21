package com.sensoro.loratool.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.sensoro.lora.setting.server.ILoRaSettingServer;
import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.lora.setting.server.bean.DeviceInfoListRsp;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.DeviceInfoAdapter;
import com.sensoro.loratool.adapter.SearchHistoryAdapter;
import com.sensoro.libbleserver.ble.BLEDevice;
import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.libbleserver.ble.SensoroSensor;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.widget.RecycleViewItemClickListener;
import com.sensoro.loratool.widget.SensoroLinearLayoutManager;
import com.sensoro.loratool.widget.SpacesItemDecoration;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by sensoro on 17/7/11.
 */

public class SearchDeviceActivity extends AppCompatActivity implements View.OnClickListener, AdapterView
        .OnItemClickListener, Constants, TextView.OnEditorActionListener, TextWatcher, LoRaSettingApplication
        .SensoroDeviceListener {
    @BindView(R.id.tab_bar_keyword_et)
    EditText mKeywordEt;
    @BindView(R.id.tab_bar_cancel_tv)
    TextView mCancelTv;
    @BindView(R.id.clear_keyword_iv)
    ImageView mClearKeywordIv;
    @BindView(R.id.search_history_ll)
    LinearLayout mSearchHistoryLayout;
    @BindView(R.id.search_device_ll)
    LinearLayout mSearchDeviceLayout;
    @BindView(R.id.clear_history_btn)
    Button mClearBtn;
    @BindView(R.id.search_history_rv)
    RecyclerView mSearchHistoryRv;
    @BindView(R.id.search_device_list)
    ListView mSearchDeviceLv;

    private PopupWindow mBottomPopupWindow;
    private View mBottomPopupView;
    private ImageView configImageView;
    private ImageView cloudImageView;
    private ImageView upgradeImageView;
    private ImageView signalImageView;
    private ImageView clearImageView;
    private LinearLayout configLayout;
    private LinearLayout cloudLayout;
    private LinearLayout upgradeLayout;
    private LinearLayout signalLayout;
    private LinearLayout clearLayout;

    private SharedPreferences mPref;
    private Editor mEditor;
    private List<String> mHistoryKeywords;
    private LoRaSettingApplication loRaSettingApplication;
    private ProgressDialog progressDialog;
    private SearchHistoryAdapter mSearchHistoryAdapter;
    private DeviceInfoAdapter mDeviceInfoAdapter;

    private DeviceInfo selectedDeviceInfo;
    private SensoroDevice mTargetDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        MobclickAgent.onPageStart("设备检索");
        loRaSettingApplication = (LoRaSettingApplication) this.getApplication();
        progressDialog = new ProgressDialog(this);
        mPref = getSharedPreferences(PREFERENCE_DEVICE_HISTORY, Activity.MODE_PRIVATE);
        mEditor = mPref.edit();
        mHistoryKeywords = new ArrayList<String>();
        mClearKeywordIv.setOnClickListener(this);
        mKeywordEt.setOnEditorActionListener(this);
        mKeywordEt.addTextChangedListener(this);
        mKeywordEt.requestFocus();

        mSearchDeviceLv.setOnItemClickListener(this);
        mCancelTv.setOnClickListener(this);
        mClearBtn.setOnClickListener(this);
        mDeviceInfoAdapter = new DeviceInfoAdapter(this);
        mSearchDeviceLv.setAdapter(mDeviceInfoAdapter);
        loRaSettingApplication.registersSensoroDeviceListener(this);
        initPopupWindow();
        initSearchHistory();
    }


    private void initPopupWindow() {
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
        closeIV.setOnClickListener(this);
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

        mBottomPopupView.setAlpha(0.8f);
        if (!Constants.permission[4]) {
            configLayout.setVisibility(View.GONE);
        }
        if (!Constants.permission[5]) {
            cloudLayout.setVisibility(View.GONE);
        }
        if (!Constants.permission[6]) {
            upgradeLayout.setVisibility(View.GONE);
        }

    }

    public void initSearchHistory() {
        String history = mPref.getString(PREFERENCE_KEY_HISTORY_KEYWORD, "");
        if (!TextUtils.isEmpty(history)) {
            List<String> list = new ArrayList<String>();
            for (Object o : history.split(",")) {
                list.add((String) o);
            }
            mHistoryKeywords = list;
        }
        if (mHistoryKeywords.size() > 0) {
            mSearchHistoryLayout.setVisibility(View.VISIBLE);
        } else {
            mSearchHistoryLayout.setVisibility(View.GONE);
        }
        SensoroLinearLayoutManager layoutManager = new SensoroLinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mSearchHistoryRv.setLayoutManager(layoutManager);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.x20);
        mSearchHistoryRv.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        mSearchHistoryAdapter = new SearchHistoryAdapter(this, mHistoryKeywords, new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mKeywordEt.setText(mHistoryKeywords.get(position));
                mSearchHistoryLayout.setVisibility(View.GONE);
                mSearchDeviceLayout.setVisibility(View.VISIBLE);
                mClearKeywordIv.setVisibility(View.VISIBLE);
                progressDialog.setMessage(getString(R.string.tips_loading_device_data));
                progressDialog.show();
                mKeywordEt.clearFocus();
                dismissInputMethodManager(view);
                requestDeviceListWithSearch(mKeywordEt.getText().toString());
                save();
            }
        });
        mSearchHistoryRv.setAdapter(mSearchHistoryAdapter);
        mSearchHistoryAdapter.notifyDataSetChanged();
    }

    public void dismissInputMethodManager(View view) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);//从控件所在的窗口中隐藏
    }

    public void save() {
        String text = mKeywordEt.getText().toString();
        String oldText = mPref.getString(PREFERENCE_KEY_HISTORY_KEYWORD, "");
        if (!TextUtils.isEmpty(text)) {
            if (mHistoryKeywords.contains(text)) {
                List<String> list = new ArrayList<String>();
                for (String o : oldText.split(",")) {
                    if (!o.equalsIgnoreCase(text)) {
                        list.add(o);
                    }
                }
                list.add(0, text);
                mHistoryKeywords = list;
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < list.size(); i++) {
                    if (i == (list.size() - 1)) {
                        stringBuffer.append(list.get(i));
                    } else {
                        stringBuffer.append(list.get(i) + ",");
                    }
                }
                mEditor.putString(PREFERENCE_KEY_HISTORY_KEYWORD, stringBuffer.toString());
                mEditor.commit();
            } else {
                mEditor.putString(PREFERENCE_KEY_HISTORY_KEYWORD, text + "," + oldText);
                mEditor.commit();
                mHistoryKeywords.add(0, text);
            }
        }
    }

    private void showBottomPopupWindow() {
        if (mBottomPopupWindow.isShowing()) {
            mBottomPopupWindow.dismiss();
        } else {
            int clear_layout_width = getResources().getDimensionPixelSize(R.dimen.x200);
            int clear_layout_height = getResources().getDimensionPixelSize(R.dimen.y200);
            if (!isDeviceNearBy()) {
                configLayout.setVisibility(GONE);
                cloudLayout.setVisibility(GONE);
                upgradeLayout.setVisibility(GONE);
                signalLayout.setVisibility(GONE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(clear_layout_width,
                        clear_layout_height);
                params.gravity = Gravity.CENTER;
                clearImageView.setLayoutParams(params);
                clearLayout.setVisibility(VISIBLE);

            } else {
                configLayout.setVisibility(VISIBLE);
                cloudLayout.setVisibility(VISIBLE);
                upgradeLayout.setVisibility(VISIBLE);
                signalLayout.setVisibility(VISIBLE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(clear_layout_width,
                        clear_layout_height);
                params.gravity = Gravity.CENTER;
                clearImageView.setLayoutParams(params);
            }
            clearLayout.setVisibility(GONE);
            //Tracker 1.0
            //sensor 1.1
            //module 1.1
            Log.e("hcs",":信号测试::"+selectedDeviceInfo.getBand()+"  "+selectedDeviceInfo.getDeviceType());
            if (selectedDeviceInfo.isCanSignal()) {
                signalLayout.setVisibility(VISIBLE);
            } else {
                signalLayout.setVisibility(GONE);
            }
            if (!Constants.permission[4]) {
                configLayout.setVisibility(View.GONE);
            }
            if (!Constants.permission[5]) {
                cloudLayout.setVisibility(View.GONE);
            }
            if (!Constants.permission[6]) {
                upgradeLayout.setVisibility(View.GONE);
            }

            if (isNotSupportBasicConfig(selectedDeviceInfo)) {
                configLayout.setVisibility(View.GONE);
            }
            if (isSupportCloudConfig(selectedDeviceInfo)) {
                cloudLayout.setVisibility(View.GONE);
            }
            mBottomPopupWindow.showAtLocation(mSearchDeviceLv, Gravity.BOTTOM, 0, 0);
        }
    }

    private boolean isNotSupportBasicConfig(DeviceInfo deviceInfo) {
        return deviceInfo.getDeviceType().equals("op_chip");
    }

    private boolean isSupportCloudConfig(DeviceInfo deviceInfo) {
        return deviceInfo.getDeviceType().equals("op_chip");
    }

    public boolean isDeviceNearBy() {
        return mDeviceInfoAdapter.getNearByDeviceMap().containsKey(selectedDeviceInfo.getSn());
    }

    public void cleanHistory() {
        mEditor.clear();
        mHistoryKeywords.clear();
        mEditor.commit();
        mSearchHistoryAdapter.notifyDataSetChanged();
        mSearchHistoryLayout.setVisibility(View.GONE);
    }

    private void requestDeviceListWithSearch(String text) {
        mDeviceInfoAdapter.clear();
        ILoRaSettingServer loRaSettingServer = loRaSettingApplication.loRaSettingServer;
        loRaSettingServer.deviceList(text, "sn", new Response.Listener<DeviceInfoListRsp>() {
            @Override
            public void onResponse(final DeviceInfoListRsp response) {
                ArrayList searchList = (ArrayList) response.getData().getItems();
                mDeviceInfoAdapter.appendSearchData(searchList);
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.tips_network_error, Toast.LENGTH_SHORT).show();
            }
        });

        loRaSettingServer.deviceList(text, "name", new Response.Listener<DeviceInfoListRsp>() {
            @Override
            public void onResponse(final DeviceInfoListRsp response) {
                ArrayList<DeviceInfo> tempArrayList = (ArrayList) response.getData().getItems();
                mDeviceInfoAdapter.appendSearchData(tempArrayList);
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.tips_network_error, Toast.LENGTH_SHORT).show();
            }
        });

        loRaSettingServer.deviceList(text, "tag", new Response.Listener<DeviceInfoListRsp>() {
            @Override
            public void onResponse(final DeviceInfoListRsp response) {
                ArrayList<DeviceInfo> tempArrayList = (ArrayList) response.getData().getItems();

                mDeviceInfoAdapter.appendSearchData(tempArrayList);
                progressDialog.dismiss();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.tips_network_error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void config() {
        Intent intent = new Intent();
        if (selectedDeviceInfo.toSensoroDeviceType() == DeviceInfo.TYPE_MODULE) {
            intent.setClass(this, SettingModuleActivity.class);
        } else {
            intent.setClass(this, SettingDeviceActivity.class);
        }
        intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, selectedDeviceInfo.getDeviceType());
        intent.putExtra(Constants.EXTRA_NAME_BAND, selectedDeviceInfo.getBand());
        intent.putExtra(Constants.EXTRA_NAME_DEVICE, mTargetDevice);
        startActivity(intent);
    }


    private void cloud() {
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_NAME_DEVICE, mTargetDevice);
        intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, selectedDeviceInfo.getDeviceType());
        intent.putExtra(Constants.EXTRA_NAME_BAND, selectedDeviceInfo.getBand());
        intent.putExtra(EXTRA_NAME_DEVICE_INFO, selectedDeviceInfo);
        intent.setClass(this, AdvanceSettingDeviceActivity.class);
        startActivity(intent);
    }

    private void upgrade() {
        Intent intent = new Intent(this, UpgradeFirmwareListActivity.class);
        intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, selectedDeviceInfo.getDeviceType());
        intent.putExtra(Constants.EXTRA_NAME_BAND, selectedDeviceInfo.getBand());
        intent.putExtra(Constants.EXTRA_NAME_DEVICE_HARDWARE_VERSION, selectedDeviceInfo.getHardwareVersion());
        intent.putExtra(Constants.EXTRA_NAME_DEVICE_FIRMWARE_VERSION, mTargetDevice.getFirmwareVersion());
        ArrayList<SensoroDevice> tempArrayList = new ArrayList<>();
        tempArrayList.add(mTargetDevice);
        intent.putParcelableArrayListExtra(Constants.EXTRA_NAME_DEVICE_LIST, tempArrayList);
        this.startActivity(intent);
    }

    private void signal() {
        Intent intent = new Intent(this, SignalDetectionActivity.class);
        intent.putExtra(Constants.EXTRA_NAME_DEVICE, mTargetDevice);
        intent.putExtra(Constants.EXTRA_NAME_BAND, selectedDeviceInfo.getBand());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loRaSettingApplication.unRegistersSensoroDeviceListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear_history_btn:
                cleanHistory();
                break;
            case R.id.tab_bar_cancel_tv:
                finish();
                break;
            case R.id.clear_keyword_iv:
                mKeywordEt.setText("");
                mClearKeywordIv.setVisibility(View.GONE);
                mSearchHistoryLayout.setVisibility(VISIBLE);
                mSearchHistoryAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_iv_cloud:
                cloud();
                mBottomPopupWindow.dismiss();
                break;
            case R.id.menu_iv_config:
                config();
                mBottomPopupWindow.dismiss();
                break;
            case R.id.menu_iv_signal:
                signal();
                mBottomPopupWindow.dismiss();
                break;
            case R.id.menu_iv_upgrade:
                upgrade();
                mBottomPopupWindow.dismiss();
                break;
            default:
                mBottomPopupWindow.dismiss();
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mDeviceInfoAdapter.appendData(mDeviceInfoAdapter.getFilterData());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            save();
            mSearchHistoryLayout.setVisibility(View.GONE);
            mSearchDeviceLayout.setVisibility(View.VISIBLE);
            mClearKeywordIv.setVisibility(View.VISIBLE);
            progressDialog.setMessage(getString(R.string.tips_loading_device_data));
            progressDialog.show();
            mKeywordEt.clearFocus();
            dismissInputMethodManager(v);
            requestDeviceListWithSearch(mKeywordEt.getText().toString());
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedDeviceInfo = mDeviceInfoAdapter.getItem(position);
        if (selectedDeviceInfo != null) {
            if (mDeviceInfoAdapter.isNearBy(selectedDeviceInfo.getSn())) {
                mTargetDevice = mDeviceInfoAdapter.getNearByDeviceMap().get(selectedDeviceInfo.getSn());
                mTargetDevice.setPassword(selectedDeviceInfo.getPassword());
                mTargetDevice.setBand(selectedDeviceInfo.getBand());
                mTargetDevice.setHardwareVersion(selectedDeviceInfo.getDeviceType());
                mTargetDevice.setFirmwareVersion(selectedDeviceInfo.getFirmwareVersion());
                showBottomPopupWindow();
            } else {
                Toast.makeText(getApplicationContext(), R.string.tips_closeto_device, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onNewDevice(final BLEDevice bleDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (bleDevice.getType()) {
                    case BLEDevice.TYPE_DEVICE:
                        mDeviceInfoAdapter.refreshNew((SensoroDevice) bleDevice, true);
                        break;
                    case BLEDevice.TYPE_SENSOR:
                        mDeviceInfoAdapter.refreshSensorNew((SensoroSensor) bleDevice);
                        break;
                }
            }
        });

    }

    @Override
    public void onGoneDevice(final BLEDevice bleDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (bleDevice.getType()) {

                    case BLEDevice.TYPE_DEVICE:
                        mDeviceInfoAdapter.refreshGone((SensoroDevice) bleDevice, true);
                        break;
                    case BLEDevice.TYPE_SENSOR:
                        mDeviceInfoAdapter.refreshSensorGone((SensoroSensor) bleDevice);
                        break;
                }
            }
        });


    }

    @Override
    public void onUpdateDevices(final ArrayList<BLEDevice> deviceList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < deviceList.size(); i++) {
                    BLEDevice bleDevice = deviceList.get(i);
                    switch (bleDevice.getType()) {
                        case BLEDevice.TYPE_SENSOR:
                            SensoroSensor sensoroSensor = (SensoroSensor) bleDevice;
                            mDeviceInfoAdapter.refreshSensor(sensoroSensor);
                            break;
                        case BLEDevice.TYPE_DEVICE:
                            mDeviceInfoAdapter.refreshNew((SensoroDevice) bleDevice, true);
                            break;
                    }
                }
            }
        });


    }
}
