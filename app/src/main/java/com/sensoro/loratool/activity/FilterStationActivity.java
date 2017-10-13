package com.sensoro.loratool.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.sensoro.loratool.R;
import com.sensoro.loratool.adapter.FilterStationInfoAdapter;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.model.FilterData;
import com.sensoro.loratool.widget.StatusBarCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by sensoro on 17/3/22.
 */

public class FilterStationActivity extends BaseActivity {

    public static final String STATION_HARDWARE_TYPE[] = {"station", "gateway"};
    private Map<String, List<FilterData>> dataSet = new HashMap<>();
    private ExpandableListView mMenuListView;
    private FilterStationInfoAdapter filterInfoAdapter;
    private String[] parentList = new String[5];
    private TextView saveTextView;

    List<FilterData> firmwareList = new ArrayList<>();
    List<FilterData> hardwareList = new ArrayList<>();
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
                FilterStationActivity.this.setResult(Constants.RESULT_FILTER);
                FilterStationActivity.this.finish();
                return true;
            }
        });
        resetRootLayout();
        mMenuListView = (ExpandableListView) findViewById(R.id.settings_filter_menu);
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCE_FILTER, Context.MODE_PRIVATE);

        String[] firmwareArray = getResources().getStringArray(R.array.filter_station_firmware_array);
        String[] hardwareArray = getResources().getStringArray(R.array.filter_station_hardware_array);
        String[] hardwareValueArray = STATION_HARDWARE_TYPE;
        HashSet<String> enableFilterSet = (HashSet) sharedPreferences.getStringSet("station_enable_filter", null);
        HashSet<String> firmwareSet = (HashSet) sharedPreferences.getStringSet("station_firmware", null);
        HashSet<String> hardwareSet = (HashSet) sharedPreferences.getStringSet("station_hardware", null);
        HashSet<String> signalSet = (HashSet) sharedPreferences.getStringSet("station_signal", null);
        HashSet<String> nearSet = (HashSet) sharedPreferences.getStringSet("station_near", null);
        String[] enableArray = getResources().getStringArray(R.array.filter_enable);
        if (enableFilterSet != null) {
            for (int i = 0 ; i < enableArray.length; i ++) {
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


        String[] nearArray = getResources().getStringArray(R.array.filter_near_array);

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
        parentList[0] = getString(R.string.filter_switch);
        parentList[1] = getString(R.string.nearby);
        parentList[2] = getString(R.string.firmware_version);
        parentList[3] = getString(R.string.hardware_version);
        parentList[4] = getString(R.string.signal_strength);
        dataSet.put(parentList[0], enableList);
        dataSet.put(parentList[1], nearList);
        dataSet.put(parentList[2], firmwareList);
        dataSet.put(parentList[3], hardwareList);
        dataSet.put(parentList[4], signalList);
        filterInfoAdapter = new FilterStationInfoAdapter(this, dataSet, parentList);
        mMenuListView.setAdapter(filterInfoAdapter);
        mMenuListView.setGroupIndicator(null);
        mMenuListView.setChildDivider(getResources().getDrawable(R.drawable.shape_line));
        for (int i = 0; i < filterInfoAdapter.getGroupCount(); i++) {
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
            SharedPreferences sharedPreferences = getSharedPreferences(Constants.PREFERENCE_FILTER, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("station_enable_filter");
            editor.remove("station_signal");
            editor.remove("station_firmware");
            editor.remove("station_hardware");
            editor.remove("station_near");
            HashSet setEnable = new HashSet();
            for (int i = 0; i < enableList.size(); i++) {
                FilterData filterData = enableList.get(i);
                if (filterData.isSelected()) {
                    setEnable.add(String.valueOf(filterData.getId()));
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

            HashSet setNear = new HashSet();
            for (int i = 0; i < nearList.size(); i++) {
                FilterData filterData = nearList.get(i);
                if (filterData.isSelected()) {
                    setNear.add(String.valueOf(filterData.getId()));
                }
            }
            editor.putStringSet("station_enable_filter", setEnable);
            editor.putStringSet("station_signal", setSignal);
            editor.putStringSet("station_firmware", setFirmware);
            editor.putStringSet("station_hardware", setHardware);
            editor.putStringSet("station_near", setNear);

            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
