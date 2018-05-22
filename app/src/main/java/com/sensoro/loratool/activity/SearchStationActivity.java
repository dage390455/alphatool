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

import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.SearchHistoryAdapter;
import com.sensoro.loratool.adapter.StationInfoAdapter;
import com.sensoro.loratool.ble.BLEDevice;
import com.sensoro.loratool.ble.SensoroStation;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.widget.RecycleViewItemClickListener;
import com.sensoro.loratool.widget.SensoroLinearLayoutManager;
import com.sensoro.loratool.widget.SpacesItemDecoration;
import com.sensoro.station.communication.bean.StationInfo;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

/**
 * Created by sensoro on 17/7/11.
 */

public class SearchStationActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, Constants, TextView.OnEditorActionListener, TextWatcher, LoRaSettingApplication.SensoroDeviceListener {
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

    private PopupWindow mPopupWindow;
    private View mPopupView;
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
    private StationInfoAdapter mStationInfoAdapter;

    private StationInfo targetStationInfo;
    private SensoroStation targetSensoroStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        MobclickAgent.onPageStart("基站检索");
        loRaSettingApplication = (LoRaSettingApplication) this.getApplication();
        progressDialog = new ProgressDialog(this);
        mPref = getSharedPreferences(PREFERENCE_STATION_HISTORY, Activity.MODE_PRIVATE);
        mEditor = mPref.edit();
        mHistoryKeywords = new ArrayList<String>();
        mClearKeywordIv.setOnClickListener(this);
        mKeywordEt.setOnEditorActionListener(this);
        mKeywordEt.addTextChangedListener(this);
        mKeywordEt.requestFocus();
        mSearchDeviceLv.setOnItemClickListener(this);
        mCancelTv.setOnClickListener(this);
        mClearBtn.setOnClickListener(this);
        mStationInfoAdapter = new StationInfoAdapter(this);
        mSearchDeviceLv.setAdapter(mStationInfoAdapter);
        mStationInfoAdapter.appendData(loRaSettingApplication.getStationInfoList());
        loRaSettingApplication.registersSensoroDeviceListener(this);
        initPopupWindow();
        initSearchHistory();
    }


    private void initPopupWindow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mPopupView = inflater.inflate(R.layout.menu_bottom_view, null);
        configImageView = (ImageView) mPopupView.findViewById(R.id.menu_iv_config);
        cloudImageView = (ImageView) mPopupView.findViewById(R.id.menu_iv_cloud);
        upgradeImageView = (ImageView) mPopupView.findViewById(R.id.menu_iv_upgrade);
        signalImageView = (ImageView) mPopupView.findViewById(R.id.menu_iv_signal);
        clearImageView = (ImageView) mPopupView.findViewById(R.id.menu_iv_clear);
        configLayout = (LinearLayout) mPopupView.findViewById(R.id.menu_ll_config);
        cloudLayout = (LinearLayout) mPopupView.findViewById(R.id.menu_ll_cloud);
        upgradeLayout = (LinearLayout) mPopupView.findViewById(R.id.menu_ll_upgrade);
        signalLayout = (LinearLayout) mPopupView.findViewById(R.id.menu_ll_signal);
        clearLayout = (LinearLayout) mPopupView.findViewById(R.id.menu_ll_clear);
        configImageView.setOnClickListener(this);
        cloudImageView.setOnClickListener(this);
        mPopupView.setFocusableInTouchMode(true);
        upgradeImageView.setVisibility(GONE);
        signalImageView.setVisibility(GONE);
        clearImageView.setVisibility(GONE);
        upgradeLayout.setVisibility(GONE);
        clearLayout.setVisibility(GONE);
        signalLayout.setVisibility(GONE);
        if (!Constants.permission[0]) {
            configImageView.setVisibility(GONE);
        }
        if (!Constants.permission[2]) {
            cloudImageView.setVisibility(GONE);
        }
        mPopupWindow = new PopupWindow(mPopupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setAnimationStyle(R.style.menuAnimationFade);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        ImageView closeIV = (ImageView) mPopupView.findViewById(R.id.menu_iv_close);
        closeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });
        mPopupView.setAlpha(0.8f);
        //TODO
//        if (Constants.permission[4]) {
//            configLayout.setVisibility(View.GONE);
//        }
//        if (Constants.permission[5]) {
//            cloudLayout.setVisibility(View.GONE);
//        }
//        if (Constants.permission[6]) {
//            upgradeLayout.setVisibility(View.GONE);
//        }
        if (!Constants.permission[0]) {
            configLayout.setVisibility(GONE);
        } else {
            configLayout.setVisibility(View.VISIBLE);
        }
        if (!Constants.permission[2]) {
            cloudLayout.setVisibility(GONE);
        } else {
            cloudLayout.setVisibility(View.VISIBLE);
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
//                progressDialog.setMessage(getString(R.string.tips_loading_device_data));
//                progressDialog.show();
                mKeywordEt.clearFocus();
                dismissInputMethodManager(view);
                filterStationList(mKeywordEt.getText().toString());
            }
        });
        mSearchHistoryRv.setAdapter(mSearchHistoryAdapter);
        mSearchHistoryAdapter.notifyDataSetChanged();
    }

    public void dismissInputMethodManager(View view) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);//从控件所在的窗口中隐藏
    }

    private void changePopupWindowState() {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        } else {
            mPopupWindow.showAtLocation(mSearchDeviceLv, Gravity.BOTTOM, 0, 0);
        }
    }

    public void save() {
        String text = mKeywordEt.getText().toString();
        String oldText = mPref.getString(PREFERENCE_KEY_HISTORY_KEYWORD, "");
        if (!TextUtils.isEmpty(text) && !mHistoryKeywords.contains(text)) {
            mEditor.putString(PREFERENCE_KEY_HISTORY_KEYWORD, text + "," + oldText);
            mEditor.commit();
            mHistoryKeywords.add(0, text);
        }
    }

    public void cleanHistory() {
        mEditor.clear();
        mHistoryKeywords.clear();
        mEditor.commit();
        mSearchHistoryAdapter.notifyDataSetChanged();
        mSearchHistoryLayout.setVisibility(View.GONE);
    }

    private void filterStationList(String filter) {

        ArrayList<StationInfo> originStationList = new ArrayList<>();
        originStationList.addAll(mStationInfoAdapter.getCacheData().values());
        ArrayList<StationInfo> deleteStationList = new ArrayList<>();
        for (StationInfo stationInfo : originStationList) {
            if (!stationInfo.getSys().getSn().contains(filter.toUpperCase())) {
                deleteStationList.add(stationInfo);
            }
        }

        for (StationInfo stationInfo : deleteStationList) {
            if (containsTag(stationInfo.getTags(), filter) || stationInfo.getName().contains(filter)) {

            } else {
                originStationList.remove(stationInfo);
            }
        }
        mStationInfoAdapter.clear();
        mStationInfoAdapter.appendSearchData(originStationList);
    }

    private boolean containsTag(List<String> tags, String filter) {
        for (String tag : tags) {
            if (tag.contains(filter)) {
                return true;
            }
        }
        return false;
    }


    public void cloud() {
        Intent intent = new Intent(this, AdvanceSettingStationActivity.class);
        intent.putExtra(Constants.EXTRA_NAME_STATION, targetSensoroStation);
        startActivity(intent);
    }

    public void config() {
        Intent intent = new Intent(this, SettingStationActivity.class);
        intent.putExtra(Constants.EXTRA_NAME_STATION, targetSensoroStation);
        intent.putExtra(Constants.EXTRA_NAME_STATION_TYPE, targetStationInfo.getDeviceType());
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
                break;
            case R.id.menu_iv_cloud:
                cloud();
                mPopupWindow.dismiss();
                break;
            case R.id.menu_iv_config:
                config();
                mPopupWindow.dismiss();
                break;
            default:
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
        mSearchHistoryLayout.setVisibility(View.GONE);
        mSearchDeviceLayout.setVisibility(View.VISIBLE);
        mClearKeywordIv.setVisibility(View.VISIBLE);
        filterStationList(s.toString());
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
            mKeywordEt.clearFocus();
            dismissInputMethodManager(v);
            filterStationList(mKeywordEt.getText().toString());
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        targetStationInfo = mStationInfoAdapter.getItem(position);

        if (targetStationInfo != null && mStationInfoAdapter.getNearByStationMap().containsKey(targetStationInfo.getSys().getSn())) {
            targetSensoroStation = mStationInfoAdapter.getNearByStationMap().get(targetStationInfo.getSys().getSn());
            targetSensoroStation.setPwd(targetStationInfo.getStation_pwd());
            changePopupWindowState();
        } else {
            Toast.makeText(this, R.string.tips_closeto_station, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNewDevice(final BLEDevice bleDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (bleDevice.getType()) {
                    case BLEDevice.TYPE_STATION:
                        mStationInfoAdapter.refreshNew((SensoroStation) bleDevice, true);
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
                    case BLEDevice.TYPE_STATION:
                        mStationInfoAdapter.refreshGone((SensoroStation) bleDevice, true);
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
                        case BLEDevice.TYPE_STATION:
                            mStationInfoAdapter.refreshNew((SensoroStation) bleDevice, true);
                            break;
                    }
                }
            }
        });

    }
}
