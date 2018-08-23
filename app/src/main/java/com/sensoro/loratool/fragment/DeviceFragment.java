package com.sensoro.loratool.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sensoro.lora.setting.server.ILoRaSettingServer;
import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.lora.setting.server.bean.DeviceInfoListRsp;
import com.sensoro.loratool.LoRaSettingApplication;
import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.AdvanceSettingDeviceActivity;
import com.sensoro.loratool.activity.AdvanceSettingMultiDeviceActivity;
import com.sensoro.loratool.activity.SearchDeviceActivity;
import com.sensoro.loratool.activity.SettingDeviceActivity;
import com.sensoro.loratool.activity.SettingModuleActivity;
import com.sensoro.loratool.activity.SettingMultiDeviceActivity;
import com.sensoro.loratool.activity.SettingMultiModuleActivity;
import com.sensoro.loratool.activity.SignalDetectionActivity;
import com.sensoro.loratool.activity.UpgradeFirmwareListActivity;
import com.sensoro.loratool.adapter.DeviceInfoAdapter;
import com.sensoro.libbleserver.ble.SensoroDevice;
import com.sensoro.libbleserver.ble.SensoroSensorTest;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.store.DeviceDataDao;
import com.sensoro.loratool.utils.Utils;
import com.sensoro.loratool.widget.SensoroEditText;
import com.sensoro.loratool.widget.SensoroPopupView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 * Created by sensoro on 16/8/18.
 */

public class DeviceFragment extends Fragment implements Callable, AdapterView.OnItemClickListener, View
        .OnClickListener, Constants {
    public static final String INPUT = "INPUT";
    public static final int DOWN = 0;
    public static final int UP = 1;
    public static final int MODEL_SINGLE = 0;
    public static final int MODEL_MULTI = 1;
    public static final int MODEL_SAME = 2; // 固件版本，硬件，设备类型，频段
    private Context mContext;
    private LoRaSettingApplication loRaSettingApplication;
    private DeviceInfoAdapter mDeviceInfoAdapter = null;
    private SensoroDevice mTargetDevice = null;
    private ConcurrentHashMap<String, DeviceInfo> mTargetDeviceInfoMap = new ConcurrentHashMap<>();
    private PullToRefreshListView mPtrListView;
    private ProgressDialog progressDialog;
    private PopupWindow mBottomPopupWindow;
    private View mBottomPopupView;
    private ImageView configImageView;
    private ImageView cloudImageView;
    private ImageView upgradeImageView;
    private ImageView signalImageView;
    private ImageView clearImageView;
    private ImageView addImageView;
    private LinearLayout configLayout;
    private LinearLayout cloudLayout;
    private LinearLayout upgradeLayout;
    private LinearLayout signalLayout;
    private LinearLayout clearLayout;
    private LinearLayout shadowLayout;

    private DeviceInfo selectedDeviceInfo = null;
    private SensoroPopupView mSensoroPopupView;
    private FutureTask<Object> futureTask;
    private int cur_page = 1;
    private int cur_model = MODEL_SINGLE;
    private boolean isRunBackground = true;
    public boolean isCurrent = true;
    private ExecutorService executorService;

    public static DeviceFragment newInstance(String input) {
        DeviceFragment deviceFragment = new DeviceFragment();
        Bundle args = new Bundle();
        args.putString(INPUT, input);
        deviceFragment.setArguments(args);
        return deviceFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onPageStart("设备列表");
        futureTask = new FutureTask<Object>(this);

        executorService = Executors.newCachedThreadPool();
        new Thread(futureTask).start();

    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this.getContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this.getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        init(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunBackground = false;
        if (futureTask != null) {
            futureTask.cancel(true);
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private void init(View view) {
        loRaSettingApplication = (LoRaSettingApplication) this.getActivity().getApplication();
        progressDialog = new ProgressDialog(mContext);
        mPtrListView = (PullToRefreshListView) view.findViewById(R.id.device_ptr_list);
        ViewGroup searchLayout = (ViewGroup) LayoutInflater.from(this.getActivity()).inflate(R.layout.layout_search,
                null);
        SensoroEditText mSearchEditText = (SensoroEditText) searchLayout.findViewById(R.id.et_head_search);
        mSearchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent testIntent = new Intent(getActivity(), SearchDeviceActivity.class);
                startActivity(testIntent);
            }
        });
        mPtrListView.getRefreshableView().addHeaderView(searchLayout, null, true);
        mPtrListView.getRefreshableView().setHeaderDividersEnabled(false);
        mDeviceInfoAdapter = new DeviceInfoAdapter(mContext);
        mPtrListView.setOnItemClickListener(this);
        mPtrListView.setAdapter(mDeviceInfoAdapter);

        mPtrListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestDeviceList(DOWN);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestDeviceList(UP);
            }
        });
        mPtrListView.setMode(PullToRefreshBase.Mode.BOTH);
        requestDeviceList(DOWN);
        initPopupWindow(view);
        addImageView = (ImageView) view.findViewById(R.id.device_iv_add);
        addImageView.setOnClickListener(this);
        addImageView.setVisibility(GONE);
        shadowLayout = (LinearLayout) view.findViewById(R.id.device_list_shadow);
    }

    private void initPopupWindow(View view) {
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
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
        mSensoroPopupView = (SensoroPopupView) view.findViewById(R.id.device_model_popup);
        mSensoroPopupView.registerListener(new SensoroPopupView.SensoroPopupViewListener() {
            @Override
            public void onCallBack(int index) {
                cur_model = index;
                if (index == MODEL_SINGLE) {
                    clear();
                }
            }

            @Override
            public void onDismissAnimationEnd() {
            }

            @Override
            public void onDismissAnimationStart() {
                shadowLayout.setVisibility(GONE);
            }
        });

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

    public void doModelChoose() {
        showTopPopupWindow();
    }

    public void request() {
        loRaSettingApplication.loRaSettingServer.stopAllRequest();
        progressDialog.setMessage(getResources().getString(R.string.tips_loading_device_data));
        progressDialog.show();
        requestDeviceList(DOWN);
    }

    private void requestDeviceList(int direction) {
        switch (direction) {
            case DOWN:
                mPtrListView.getLoadingLayoutProxy().setPullLabel(mContext.getString(R.string.pulldown_loading));
                ILoRaSettingServer loRaSettingServer = loRaSettingApplication.loRaSettingServer;
                loRaSettingServer.deviceList(1, new Response.Listener<DeviceInfoListRsp>() {
                    @Override
                    public void onResponse(final DeviceInfoListRsp response) {
                        progressDialog.dismiss();
                        mPtrListView.onRefreshComplete();
                        mDeviceInfoAdapter.clearCache();
                        ArrayList tempList = (ArrayList) response.getData().getItems();
                        loRaSettingApplication.getDeviceInfoList().clear();
                        loRaSettingApplication.getDeviceInfoList().addAll(tempList);
                        if (tempList.size() == 0) {
                            Toast.makeText(mContext, R.string.tips_no_device, Toast.LENGTH_SHORT).show();
                        } else {
                            mDeviceInfoAdapter.appendData(tempList);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        error.printStackTrace();
                        Toast.makeText(mContext, R.string.tips_network_error, Toast.LENGTH_SHORT).show();
                    }
                });
                cur_page = 1;
                break;
            case UP:
                cur_page++;
                mPtrListView.getLoadingLayoutProxy().setPullLabel(mContext.getString(R.string.pullup_loading));
                ILoRaSettingServer loRaServer = loRaSettingApplication.loRaSettingServer;
                loRaServer.deviceList(cur_page, new Response.Listener<DeviceInfoListRsp>() {
                    @Override
                    public void onResponse(final DeviceInfoListRsp response) {
                        mPtrListView.onRefreshComplete();
                        ArrayList tempList = (ArrayList) response.getData().getItems();
                        if (tempList.size() == 0) {
                            Toast.makeText(mContext, R.string.tips_no_device, Toast.LENGTH_SHORT).show();
                            cur_page--;
                        } else {
                            mDeviceInfoAdapter.appendData(tempList);
                            loRaSettingApplication.getDeviceInfoList().addAll(tempList);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        cur_page--;
                        Toast.makeText(mContext, R.string.tips_network_error, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            default:
                break;
        }
    }

    private void requestDeviceListWithBackground() {
        StringBuffer snString = new StringBuffer();
        HashMap<String, String> dataMap = DeviceDataDao.getSnMapWithNotExistInServer();
        final HashMap<String, String> storeMap = new HashMap<>();
        int j = 0;
        for (String key : mDeviceInfoAdapter.getNearByDeviceMap().keySet()) {
            String sn = mDeviceInfoAdapter.getNearByDeviceMap().get(key).getSn();
            if (!mDeviceInfoAdapter.getCacheData().containsKey(sn) && !dataMap.containsKey(sn)) {
                if (j == mDeviceInfoAdapter.getNearByDeviceMap().size() - 1 || mDeviceInfoAdapter.getNearByDeviceMap
                        ().size() == 1) {
                    snString.append(sn);
                } else {
                    snString.append(sn + ",");
                }
                storeMap.put(sn, sn);
            }
            j++;
        }

        if (snString.length() > 0) {
            String temp_char = snString.substring(snString.length() - 1);
            if (temp_char.equals(",")) {
                snString.replace(snString.length() - 1, snString.length(), "");
            }
            loRaSettingApplication.loRaSettingServer.deviceAll(snString.toString(), new Response
                    .Listener<DeviceInfoListRsp>() {
                @Override
                public void onResponse(final DeviceInfoListRsp response) {
                    ArrayList<DeviceInfo> infoArrayList = (ArrayList) response.getData().getItems();
                    if (infoArrayList.size() != 0) {
                        mDeviceInfoAdapter.appendDataWithPosition(infoArrayList);
                    }
                    addSnWithNotExistInServer(storeMap, infoArrayList);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
        }
    }

    public void addSnWithNotExistInServer(final HashMap<String, String> map, final ArrayList<DeviceInfo> list) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < list.size(); i++) {
                    String sn = list.get(i).getSn();
                    if (map.containsKey(sn)) {
                        map.remove(sn);
                    }
                }
                for (String v : map.values()) {
                    DeviceDataDao.addDeviceWithNotExistInServer(v);
                }
            }
        });

    }

    public void showTopPopupWindow() {
        shadowLayout.setVisibility(VISIBLE);
        shadowLayout.setAlpha(0.5f);
        shadowLayout.setBackgroundColor(getResources().getColor(R.color.item_text_color));
        mSensoroPopupView.show();
    }

    private void showBottomPopupWindow() {
        if (mBottomPopupWindow.isShowing()) {
            mBottomPopupWindow.dismiss();
        } else {
            int clear_layout_width = mContext.getResources().getDimensionPixelSize(R.dimen.x200);
            int clear_layout_height = mContext.getResources().getDimensionPixelSize(R.dimen.y200);
            if (!isAllDeviceNearBy()) {
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
            if (cur_model == MODEL_MULTI || cur_model == MODEL_SAME) {
                clearLayout.setVisibility(VISIBLE);
                signalLayout.setVisibility(GONE);
            } else {
                clearLayout.setVisibility(GONE);
                if (selectedDeviceInfo.isCanSignal()) {
                    signalLayout.setVisibility(VISIBLE);
                } else {
                    signalLayout.setVisibility(GONE);
                }

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
            if (isNotSupportCloudConfig(selectedDeviceInfo)) {
                cloudLayout.setVisibility(View.GONE);
            }
            mBottomPopupWindow.showAtLocation(mPtrListView, Gravity.BOTTOM, 0, 0);
        }
    }

    public void refreshNew(SensoroDevice sensoroDevice) {
        if (mDeviceInfoAdapter != null) {
            mDeviceInfoAdapter.refreshNew(sensoroDevice, false);
        }
    }
    public void refreshGone(SensoroDevice sensoroDevice) {
        if (mDeviceInfoAdapter != null) {
            mDeviceInfoAdapter.refreshGone(sensoroDevice, false);
        }
    }

    public void refreshSensorGone(SensoroSensorTest sensoroSensor) {
        if (mDeviceInfoAdapter != null) {
            mDeviceInfoAdapter.refreshSensorGone(sensoroSensor);
        }
    }

    public void refreshSensorNew(SensoroSensorTest sensoroSensor) {
        if (mDeviceInfoAdapter != null) {
            mDeviceInfoAdapter.refreshSensorNew(sensoroSensor);
        }
    }

    public void refreshSensor(SensoroSensorTest sensoroSensor) {
        if (mDeviceInfoAdapter != null) {
            mDeviceInfoAdapter.refreshSensor(sensoroSensor);
        }
    }


    private boolean isNotSupportBasicConfig(DeviceInfo deviceInfo) {
        return deviceInfo.getDeviceType().equals("op_chip");
    }

    private boolean isNotSupportCloudConfig(DeviceInfo deviceInfo) {
        return deviceInfo.getDeviceType().equals("op_chip");
    }

    private boolean isSupportDevice() {
        String deviceType = null;
        String band = null;
        String firmware = null;
        boolean isSame = false;

        for (String key : mTargetDeviceInfoMap.keySet()) {
            DeviceInfo deviceInfo = mTargetDeviceInfoMap.get(key);
            if (deviceType == null || deviceInfo.getDeviceType().equals(deviceType)) {
                isSame = true;
            } else {
                Toast.makeText(mContext, R.string.tips_same_hardware, Toast.LENGTH_SHORT).show();
                return false;
            }
            deviceType = deviceInfo.getDeviceType();
        }

        for (String key : mTargetDeviceInfoMap.keySet()) {
            DeviceInfo deviceInfo = mTargetDeviceInfoMap.get(key);
            if (firmware == null || deviceInfo.getFirmwareVersion().equals(firmware)) {
                isSame = true;
            } else {
                Toast.makeText(mContext, R.string.tips_same_firmware, Toast.LENGTH_SHORT).show();
                return false;
            }
            firmware = deviceInfo.getFirmwareVersion();
        }
        for (String key : mTargetDeviceInfoMap.keySet()) {
            DeviceInfo deviceInfo = mTargetDeviceInfoMap.get(key);
            if (band == null || deviceInfo.getBand().equals(band)) {
                isSame = true;
            } else {
                Toast.makeText(mContext, R.string.tips_same_band, Toast.LENGTH_SHORT).show();
                return false;
            }
            band = deviceInfo.getBand();
        }
        return isSame;
    }


    public boolean isAllDeviceNearBy() {
        for (String key : mTargetDeviceInfoMap.keySet()) {
            if (!mDeviceInfoAdapter.getNearByDeviceMap().containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    private void config() {
        Intent intent = new Intent();
        if (selectedDeviceInfo.toSensoroDeviceType() == DeviceInfo.TYPE_MODULE) {
            intent.setClass(mContext, SettingModuleActivity.class);
        } else {
            intent.setClass(mContext, SettingDeviceActivity.class);
        }
        intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, selectedDeviceInfo.getDeviceType());
        intent.putExtra(Constants.EXTRA_NAME_BAND, selectedDeviceInfo.getBand());
        intent.putExtra(Constants.EXTRA_NAME_DEVICE, mTargetDevice);
        startActivity(intent);
    }

    private void config(ConcurrentHashMap<String, SensoroDevice> sensoroDeviceMap) {
        ArrayList<SensoroDevice> sensoroDeviceArrayList = new ArrayList<>(sensoroDeviceMap.values());
        SensoroDevice sensoroDevice = sensoroDeviceArrayList.get(0);
        DeviceInfo deviceInfo = mTargetDeviceInfoMap.get(sensoroDevice.getSn());
        if (sensoroDeviceMap.size() == 1) {
            Intent intent = new Intent();
            if (deviceInfo.toSensoroDeviceType() == DeviceInfo.TYPE_MODULE) {
                intent.setClass(mContext, SettingModuleActivity.class);
            } else {
                intent.setClass(mContext, SettingDeviceActivity.class);
            }
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, selectedDeviceInfo.getDeviceType());
            intent.putExtra(Constants.EXTRA_NAME_BAND, selectedDeviceInfo.getBand());
            intent.putExtra(Constants.EXTRA_NAME_DEVICE, sensoroDevice);
            startActivity(intent);
        } else if (sensoroDeviceMap.size() > 1) {
            Intent intent = new Intent();
            if (deviceInfo.toSensoroDeviceType() == DeviceInfo.TYPE_MODULE) {
                intent.setClass(mContext, SettingMultiModuleActivity.class);
            } else {
                intent.setClass(mContext, SettingMultiDeviceActivity.class);
            }
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, selectedDeviceInfo.getDeviceType());
            intent.putExtra(Constants.EXTRA_NAME_BAND, selectedDeviceInfo.getBand());
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_LIST, sensoroDeviceArrayList);
            startActivity(intent);

        } else {
            Toast.makeText(mContext, R.string.tips_closeto_device, Toast.LENGTH_SHORT).show();
        }
    }

    private void cloud() {
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_NAME_DEVICE, mTargetDevice);
        intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, selectedDeviceInfo.getDeviceType());
        intent.putExtra(Constants.EXTRA_NAME_BAND, selectedDeviceInfo.getBand());
        intent.putExtra(EXTRA_NAME_DEVICE_INFO, selectedDeviceInfo);
        intent.setClass(mContext, AdvanceSettingDeviceActivity.class);
        startActivity(intent);
    }

    private void cloud(ConcurrentHashMap<String, SensoroDevice> sensoroDeviceMap) {
        if (sensoroDeviceMap.size() == 1) {
            SensoroDevice sensoroDevice = sensoroDeviceMap.get(sensoroDeviceMap.keySet().iterator().next());
            Intent intent = new Intent(mContext, AdvanceSettingDeviceActivity.class);
            intent.putExtra(Constants.EXTRA_NAME_DEVICE, sensoroDevice);
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, selectedDeviceInfo.getDeviceType());
            intent.putExtra(Constants.EXTRA_NAME_BAND, selectedDeviceInfo.getBand());
            intent.putExtra(EXTRA_NAME_DEVICE_INFO, selectedDeviceInfo);
            startActivity(intent);
        } else if (sensoroDeviceMap.size() > 1) {
            Intent intent = new Intent(mContext, AdvanceSettingMultiDeviceActivity.class);
            ArrayList<SensoroDevice> sensoroDeviceArrayList = new ArrayList<>(sensoroDeviceMap.values());
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_LIST, sensoroDeviceArrayList);
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, selectedDeviceInfo.getDeviceType());
            intent.putExtra(Constants.EXTRA_NAME_BAND, selectedDeviceInfo.getBand());
            intent.putExtra(EXTRA_NAME_DEVICE_INFO, selectedDeviceInfo);
            startActivity(intent);
        } else {
            Toast.makeText(mContext, R.string.tips_closeto_device, Toast.LENGTH_SHORT).show();
        }

    }

    private void upgrade() {
        Intent intent = new Intent(mContext, UpgradeFirmwareListActivity.class);
        intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, selectedDeviceInfo.getDeviceType());
        intent.putExtra(Constants.EXTRA_NAME_BAND, selectedDeviceInfo.getBand());
        intent.putExtra(Constants.EXTRA_NAME_DEVICE_HARDWARE_VERSION, selectedDeviceInfo.getDeviceType());
        intent.putExtra(Constants.EXTRA_NAME_DEVICE_FIRMWARE_VERSION, mTargetDevice.getFirmwareVersion());
        ArrayList<SensoroDevice> tempArrayList = new ArrayList<>();
        tempArrayList.add(mTargetDevice);
        intent.putParcelableArrayListExtra(Constants.EXTRA_NAME_DEVICE_LIST, tempArrayList);
        this.startActivity(intent);
    }

    private void upgrade(ConcurrentHashMap<String, SensoroDevice> sensoroDeviceMap) {
        if (sensoroDeviceMap.size() == 0) {
            Toast.makeText(mContext, R.string.tips_closeto_device, Toast.LENGTH_SHORT).show();
        } else {
            ArrayList<SensoroDevice> sensoroDeviceArrayList = new ArrayList<>(sensoroDeviceMap.values());
            SensoroDevice tempDevice = sensoroDeviceArrayList.get(0);
            DeviceInfo deviceInfo = mTargetDeviceInfoMap.get(tempDevice.getSn());
            Intent intent = new Intent(mContext, UpgradeFirmwareListActivity.class);
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_TYPE, deviceInfo.getDeviceType());
            intent.putExtra(Constants.EXTRA_NAME_BAND, deviceInfo.getBand());
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_HARDWARE_VERSION, deviceInfo.getHardwareVersion());
            intent.putExtra(Constants.EXTRA_NAME_DEVICE_FIRMWARE_VERSION, tempDevice.getFirmwareVersion());
            intent.putParcelableArrayListExtra(Constants.EXTRA_NAME_DEVICE_LIST, sensoroDeviceArrayList);
            this.startActivity(intent);
        }

    }

    private void signal() {
        Intent intent = new Intent(mContext, SignalDetectionActivity.class);
        intent.putExtra(Constants.EXTRA_NAME_DEVICE, mTargetDevice);
        intent.putExtra(Constants.EXTRA_NAME_BAND, selectedDeviceInfo.getBand());
        startActivity(intent);
    }

    private void clear() {
        for (String key : mDeviceInfoAdapter.getCacheData().keySet()) {
            DeviceInfo deviceInfo = mDeviceInfoAdapter.getCacheData().get(key);
            deviceInfo.setSelected(false);
        }
        mDeviceInfoAdapter.notifyDataSetChanged();
        mTargetDeviceInfoMap.clear();
        addImageView.setVisibility(View.GONE);
    }

    public void doMultiEvent(View v) {
        switch (v.getId()) {
            case R.id.menu_iv_clear:
                clear();
                mBottomPopupWindow.dismiss();
                break;
            case R.id.device_iv_add:
                showBottomPopupWindow();
                break;
            case R.id.menu_iv_close:
                mBottomPopupWindow.dismiss();
                break;
            default:
                if (isSupportDevice()) {
                    ConcurrentHashMap<String, SensoroDevice> tempMap = new ConcurrentHashMap<>();
                    for (String key : mTargetDeviceInfoMap.keySet()) {
                        DeviceInfo deviceInfo = mTargetDeviceInfoMap.get(key);
                        if (mDeviceInfoAdapter.getNearByDeviceMap().containsKey(key)) {
                            SensoroDevice sensoroDevice = mDeviceInfoAdapter.getNearByDeviceMap().get(key);
                            sensoroDevice.setPassword(deviceInfo.getPassword());
                            sensoroDevice.setFirmwareVersion(deviceInfo.getFirmwareVersion());
                            sensoroDevice.setBand(deviceInfo.getBand());
                            sensoroDevice.setHardwareVersion(deviceInfo.getDeviceType());
                            tempMap.put(key, sensoroDevice);
                        }
                    }
                    switch (v.getId()) {
                        case R.id.menu_iv_cloud:
                            cloud(tempMap);
                            mBottomPopupWindow.dismiss();
                            break;
                        case R.id.menu_iv_config:
                            config(tempMap);
                            mBottomPopupWindow.dismiss();
                            break;
                        case R.id.menu_iv_signal:
                            mBottomPopupWindow.dismiss();
                            break;
                        case R.id.menu_iv_upgrade:
                            upgrade(tempMap);
                            mBottomPopupWindow.dismiss();
                            break;
                        default:
                            mBottomPopupWindow.dismiss();
                            break;
                    }
                }
                break;
        }
    }

    public void doSameEvent(View v) {
        switch (v.getId()) {
            case R.id.menu_iv_clear:
                clear();
                mBottomPopupWindow.dismiss();
                break;
            case R.id.device_iv_add:
                showBottomPopupWindow();
                break;
            case R.id.menu_iv_close:
                mBottomPopupWindow.dismiss();
                break;
            default:
                if (isSupportDevice()) {
                    ConcurrentHashMap<String, SensoroDevice> tempMap = new ConcurrentHashMap<>();
                    for (String key : mTargetDeviceInfoMap.keySet()) {
                        DeviceInfo deviceInfo = mTargetDeviceInfoMap.get(key);
                        if (mDeviceInfoAdapter.getNearByDeviceMap().containsKey(key)) {
                            SensoroDevice sensoroDevice = mDeviceInfoAdapter.getNearByDeviceMap().get(key);
                            sensoroDevice.setPassword(deviceInfo.getPassword());
                            sensoroDevice.setFirmwareVersion(deviceInfo.getFirmwareVersion());
                            tempMap.put(key, sensoroDevice);
                        }
                    }
                    switch (v.getId()) {
                        case R.id.menu_iv_cloud:
                            cloud(tempMap);
                            mBottomPopupWindow.dismiss();
                            break;
                        case R.id.menu_iv_config:
                            config(tempMap);
                            mBottomPopupWindow.dismiss();
                            break;
                        case R.id.menu_iv_signal:
                            mBottomPopupWindow.dismiss();
                            break;
                        case R.id.menu_iv_upgrade:
                            upgrade(tempMap);
                            mBottomPopupWindow.dismiss();
                            break;
                    }
                }
                break;
        }
    }

    public void doSingleEvent(View v) {
        switch (v.getId()) {
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
            case R.id.device_iv_add:
                showBottomPopupWindow();
                break;
            case R.id.menu_iv_close:
                mBottomPopupWindow.dismiss();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (cur_model) {
            case MODEL_MULTI:
                doMultiEvent(v);
                break;
            case MODEL_SAME:
                doSameEvent(v);
                break;
            default:
                doSingleEvent(v);
                break;
        }
    }

    public void doSingle(int position) {
        if (position >= 0) {
            selectedDeviceInfo = mDeviceInfoAdapter.getItem(position);
            if (mDeviceInfoAdapter.isNearBy(selectedDeviceInfo.getSn())) {
                mTargetDevice = mDeviceInfoAdapter.getNearByDeviceMap().get(selectedDeviceInfo.getSn());
                mTargetDevice.setPassword(selectedDeviceInfo.getPassword());
                mTargetDevice.setBand(selectedDeviceInfo.getBand());
                mTargetDevice.setFirmwareVersion(selectedDeviceInfo.getFirmwareVersion());
                mTargetDevice.setHardwareVersion(selectedDeviceInfo.getDeviceType());
                showBottomPopupWindow();
            } else {
                Toast.makeText(mContext, R.string.tips_closeto_device, Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void doMulti(int position) {
        if (position >= 0) {
            mBottomPopupWindow.dismiss();
            selectedDeviceInfo = mDeviceInfoAdapter.getItem(position);
            if (mDeviceInfoAdapter.isNearBy(selectedDeviceInfo.getSn())) {
                if (selectedDeviceInfo.isSelected()) {
                    mTargetDeviceInfoMap.remove(selectedDeviceInfo.getSn());
                    selectedDeviceInfo.setSelected(false);
                } else {
                    mTargetDeviceInfoMap.put(selectedDeviceInfo.getSn(), selectedDeviceInfo);
                    selectedDeviceInfo.setSelected(true);
                }
                mDeviceInfoAdapter.notifyDataSetChanged();
                if (mTargetDeviceInfoMap.size() > 0) {
                    addImageView.setVisibility(VISIBLE);
                } else {
                    addImageView.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(mContext, R.string.tips_closeto_device, Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void doSame(int position) {
        if (position >= 0) {
            mBottomPopupWindow.dismiss();
            selectedDeviceInfo = mDeviceInfoAdapter.getItem(position);
            if (mDeviceInfoAdapter.isNearBy(selectedDeviceInfo.getSn())) {
                mTargetDeviceInfoMap.clear();
                if (selectedDeviceInfo.isSelected()) {
                    selectedDeviceInfo.setSelected(false);
                } else {
                    List<DeviceInfo> data = mDeviceInfoAdapter.getFilterData();
                    for (int i = 0; i < data.size(); i++) {
                        DeviceInfo tempDeviceInfo = data.get(i);
                        if (tempDeviceInfo.getDeviceType().equals(selectedDeviceInfo.getDeviceType()) &&
                                tempDeviceInfo.getFirmwareVersion().equals(selectedDeviceInfo.getFirmwareVersion()) &&
                                tempDeviceInfo.getBand().equals(selectedDeviceInfo.getBand())
                                ) {
                            tempDeviceInfo.setSelected(true);
                            mTargetDeviceInfoMap.put(tempDeviceInfo.getSn(), tempDeviceInfo);
                        } else {
                            tempDeviceInfo.setSelected(false);
                        }
                    }
                }
                mDeviceInfoAdapter.notifyDataSetChanged();
                if (mTargetDeviceInfoMap.size() > 0) {
                    addImageView.setVisibility(VISIBLE);
                } else {
                    addImageView.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(mContext, R.string.tips_closeto_device, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (shadowLayout.getVisibility() == VISIBLE) {
            mSensoroPopupView.dismiss();
        } else {
            switch (cur_model) {
                case MODEL_SINGLE:
                    doSingle(position - 2);
                    break;
                case MODEL_MULTI:
                    doMulti(position - 2);
                    break;
                case MODEL_SAME:
                    doSame(position - 2);
                    break;
            }
        }

    }

    private void loadFilterData() {
        mDeviceInfoAdapter.filter();
    }


    public void filterDevice() {
        loadFilterData();
    }


    @Override
    public Object call() throws Exception {
        while (isRunBackground) {
            if (Utils.isWifi(mContext)) {
                Thread.sleep(5000);
            } else {
                Thread.sleep(30000);
            }
            if (isCurrent) {
                requestDeviceListWithBackground();
            }
        }
        return null;
    }
}
