package com.sensoro.loratool.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.FilterDeviceInfoAdapter;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.model.FilterData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.sensoro.loratool.constant.Constants.DEVICE_HARDWARE_TYPE;

/**
 * Created by sensoro on 17/3/22.
 */

public class FilterDeviceActivity extends BaseActivity {

    private ExpandableListView mMenuListView;
    private TextView saveTextView;
    private FilterDeviceInfoAdapter filterDeviceInfoAdapter;
    private Map<String, List<FilterData>> dataSet = new HashMap<>();
    private String[] parentList = new String[6];
    List<FilterData> firmwareList = new ArrayList<>();
    List<FilterData> hardwareList = new ArrayList<>();
    List<FilterData> bandList = new ArrayList<>();
    List<FilterData> signalList = new ArrayList<>();
    List<FilterData> nearList = new ArrayList<>();
    List<FilterData> enableList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        initWidget();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_filter;
    }

    public void initWidget() {
        saveTextView = (TextView) findViewById(R.id.settings_filter_tv_save);
        saveTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                saveData();
                FilterDeviceActivity.this.setResult(Constants.RESULT_FILTER);
                FilterDeviceActivity.this.finish();
                return true;
            }
        });
        resetRootLayout();
        mMenuListView = (ExpandableListView) findViewById(R.id.settings_filter_menu);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCE_FILTER, Context.MODE_PRIVATE);
        HashSet<String> enableFilterSet = (HashSet) sharedPreferences.getStringSet("device_enable_filter", null);
        HashSet<String> firmwareSet = (HashSet) sharedPreferences.getStringSet("device_firmware", null);
        HashSet<String> hardwareSet = (HashSet) sharedPreferences.getStringSet("device_hardware", null);
        HashSet<String> bandSet = (HashSet) sharedPreferences.getStringSet("device_band", null);
        HashSet<String> signalSet = (HashSet) sharedPreferences.getStringSet("device_signal", null);
        HashSet<String> nearSet = (HashSet) sharedPreferences.getStringSet("device_near", null);
        String[] firmwareArray = getResources().getStringArray(R.array.filter_device_firmware_array);
        //添加支持的类型
        String[] hardwareArray = getResources().getStringArray(R.array.filter_device_hardware_array);
        String[] bandArray = getResources().getStringArray(R.array.filter_device_band_array);
        //添加支持的硬件类型
        String hardwareSp = getSharedPreferences(Constants.PREFERENCE_DEVICE_TYPES, Context.MODE_PRIVATE).getString(Constants.PREFERENCE_KEY_DEVICE_TYPE, null);
        String hardwareNameSp = getSharedPreferences(Constants.PREFERENCE_DEVICE_TYPES, Context.MODE_PRIVATE).getString(Constants.PREFERENCE_KEY_DEVICE_TYPE_NAME, null);
        String[] hardwareValueArray;
        if (!TextUtils.isEmpty(hardwareSp) && !TextUtils.isEmpty(hardwareNameSp)) {
            String[] split = hardwareSp.split(",");
            String[] nameSplit = hardwareNameSp.split(",");
            if (split.length > 0) {
                hardwareValueArray = split;
                hardwareArray = nameSplit;
            }else{
                hardwareValueArray = DEVICE_HARDWARE_TYPE;
            }
        }else{
            hardwareValueArray = DEVICE_HARDWARE_TYPE;
        }
        String[] nearArray = getResources().getStringArray(R.array.filter_near_array);
        String[] enableArray = getResources().getStringArray(R.array.filter_enable);
        if (enableFilterSet != null) {
            for (int i = 0; i < enableArray.length; i++) {
                String enable = enableArray[i];
                FilterData filterData = new FilterData();
                filterData.setId(i);
                filterData.setName(enable);
                if (enableFilterSet.contains(String.valueOf(i))) {
                    filterData.setSelected(true);
                } else {
                    filterData.setSelected(false);
                }
                enableList.add(filterData);
            }
        } else {
            for (int i = 0; i < enableArray.length; i++) {
                String enable = enableArray[i];
                FilterData filterData = new FilterData();
                filterData.setId(i);
                filterData.setName(enable);
                if (i == 0) {
                    filterData.setSelected(true);
                } else {
                    filterData.setSelected(false);
                }
                enableList.add(filterData);
            }
        }
        if (nearSet != null) {
            for (int i = 0; i < nearArray.length; i++) {
                String near = nearArray[i];
                FilterData filterData = new FilterData();
                filterData.setId(i);
                filterData.setName(near);
                if (nearSet.contains(String.valueOf(i))) {
                    filterData.setSelected(true);
                } else {
                    filterData.setSelected(false);
                }
                nearList.add(filterData);
            }
        } else {
            for (int i = 0; i < nearArray.length; i++) {
                String near = nearArray[i];
                FilterData filterData = new FilterData();
                filterData.setId(i);
                filterData.setName(near);
                if (i == 0) {
                    filterData.setSelected(true);
                } else {
                    filterData.setSelected(false);
                }
                nearList.add(filterData);
            }
        }
        if (firmwareSet != null) {
            for (int i = 0; i < firmwareArray.length; i++) {
                String firmware = firmwareArray[i];
                FilterData filterData = new FilterData();
                filterData.setId(i);
                filterData.setName(firmware);
                if (firmwareSet.contains(firmware)) {
                    filterData.setSelected(true);
                } else {
                    filterData.setSelected(false);
                }
                firmwareList.add(filterData);
            }

        } else {
            for (int i = 0; i < firmwareArray.length; i++) {
                String firmware = firmwareArray[i];
                FilterData filterData = new FilterData(i, firmware, true);
                firmwareList.add(filterData);
            }
        }

        if (hardwareSet != null) {
            for (int i = 0; i < hardwareArray.length; i++) {
                String hardware = hardwareArray[i];
                String hardwareValue = hardwareValueArray[i];
                FilterData filterData = new FilterData();
                filterData.setId(i);
                filterData.setName(hardware);
                filterData.setType(hardwareValue);
                if (hardwareSet.contains(hardwareValue)) {
                    filterData.setSelected(true);
                } else {
                    filterData.setSelected(false);
                }
                hardwareList.add(filterData);
            }
        } else {
            for (int i = 0; i < hardwareArray.length; i++) {
                String hardware = hardwareArray[i];
                FilterData filterData = new FilterData(i, hardware, hardwareValueArray[i], true);
                hardwareList.add(filterData);
            }
        }

        String[] signalArray = getResources().getStringArray(R.array.filter_signal_array);
        if (signalSet != null) {

            for (int i = 0; i < signalArray.length; i++) {
                String signal = signalArray[i];
                FilterData filterData = new FilterData();
                filterData.setId(i);
                filterData.setName(signal);
                if (signalSet.contains(signal)) {
                    filterData.setSelected(true);
                } else {
                    filterData.setSelected(false);
                }
                signalList.add(filterData);
            }
        } else {
            for (int i = 0; i < signalArray.length; i++) {
                String signal = signalArray[i];
                FilterData filterData = new FilterData();
                filterData.setId(i);
                filterData.setName(signal);
                if (signal.equals("-100")) {
                    filterData.setSelected(true);
                } else {
                    filterData.setSelected(false);
                }
                signalList.add(filterData);
            }
        }


        if (bandSet != null) {
            for (int i = 0; i < bandArray.length; i++) {
                String band = bandArray[i];
                FilterData filterData = new FilterData();
                filterData.setId(i);
                filterData.setName(band);
                if (bandSet.contains(band)) {
                    filterData.setSelected(true);
                } else {
                    filterData.setSelected(false);
                }
                bandList.add(filterData);
            }
        } else {
            for (int i = 0; i < bandArray.length; i++) {
                String band = bandArray[i];
                FilterData filterData = new FilterData(i, band, null, true);
                bandList.add(filterData);
            }
        }
        parentList[0] = getString(R.string.filter_switch);
        parentList[1] = getString(R.string.nearby);
        parentList[2] = getString(R.string.firmware_version);
        parentList[3] = getString(R.string.hardware_version);
        parentList[4] = getString(R.string.band);
        parentList[5] = getString(R.string.signal_strength);
        dataSet.put(parentList[0], enableList);
        dataSet.put(parentList[1], nearList);
        dataSet.put(parentList[2], firmwareList);
        dataSet.put(parentList[3], hardwareList);
        dataSet.put(parentList[4], bandList);
        dataSet.put(parentList[5], signalList);
        filterDeviceInfoAdapter = new FilterDeviceInfoAdapter(this, dataSet, parentList);
        mMenuListView.setAdapter(filterDeviceInfoAdapter);
        mMenuListView.setGroupIndicator(null);
        mMenuListView.setChildDivider(getResources().getDrawable(R.drawable.shape_line));

        for (int i = 0; i < filterDeviceInfoAdapter.getGroupCount(); i++) {
            mMenuListView.expandGroup(i);
        }
        mMenuListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // TODO Auto-generated method stub
                return true;
            }
        });


    }

    public void saveData() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCE_FILTER, Context
                    .MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("device_enable_filter");
            editor.remove("device_signal");
            editor.remove("device_firmware");
            editor.remove("device_hardware");
            editor.remove("device_near");
            editor.remove("device_band");
            HashSet setEnable = new HashSet();
            for (int i = 0; i < enableList.size(); i++) {
                FilterData filterData = enableList.get(i);
                if (filterData.isSelected()) {
                    setEnable.add(String.valueOf(filterData.getId()));
                }
            }
            HashSet setNear = new HashSet();
            for (int i = 0; i < nearList.size(); i++) {
                FilterData filterData = nearList.get(i);
                if (filterData.isSelected()) {
                    setNear.add(String.valueOf(filterData.getId()));
                }
            }
            HashSet setSignal = new HashSet();
            for (int i = 0; i < signalList.size(); i++) {
                FilterData filterData = signalList.get(i);
                if (filterData.isSelected()) {
                    setSignal.add(filterData.getName());
                }
            }

            HashSet setFirmware = new HashSet();
            for (int i = 0; i < firmwareList.size(); i++) {
                FilterData filterData = firmwareList.get(i);
                if (filterData.isSelected()) {
                    setFirmware.add(filterData.getName());
                }
            }

            HashSet setHardware = new HashSet();
            for (int i = 0; i < hardwareList.size(); i++) {
                FilterData filterData = hardwareList.get(i);
                if (filterData.isSelected()) {
                    setHardware.add(filterData.getType());
                }
            }


            HashSet setBand = new HashSet();
            for (int i = 0; i < bandList.size(); i++) {
                FilterData filterData = bandList.get(i);
                if (filterData.isSelected()) {
                    setBand.add(filterData.getName());
                }
            }
            editor.putStringSet("device_enable_filter", setEnable);
            editor.putStringSet("device_signal", setSignal);
            editor.putStringSet("device_firmware", setFirmware);
            editor.putStringSet("device_hardware", setHardware);
            editor.putStringSet("device_band", setBand);
            editor.putStringSet("device_near", setNear);

            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
