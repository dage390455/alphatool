package com.sensoro.loratool.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.SettingDeviceActivity;
import com.sensoro.loratool.constant.Constants;
import com.sensoro.loratool.model.ChannelData;
import com.sensoro.loratool.utils.DialogFragmentUtils;

import java.util.ArrayList;

import static com.sensoro.loratool.constant.Constants.SETTINGS_DEVICE_RL_ACREL_CURR_HIGH_TYPE;


/**
 * Created by Sensoro on 15/8/5.
 */
public class SettingsMultiChoiceItemsFragment extends SettingsBaseDialogFragment {

    public static final String ITEMS = "ITEMS";
    public static final String ITEM = "ITEM";
    public static final String INDEX = "INDEX";

    private View dialogView;
    private TextView levelTipsTextView;
    private TextView tipsDetailTextView;

    private ArrayList<ChannelData> selectedChannelList;

    public static SettingsMultiChoiceItemsFragment newInstance( ArrayList<ChannelData> channelDataArrayList) {
        SettingsMultiChoiceItemsFragment settingsSingleChoiceItemsFragment = new SettingsMultiChoiceItemsFragment();
        Bundle args = new Bundle();
        args.putSerializable(INDEX, channelDataArrayList);
        settingsSingleChoiceItemsFragment.setArguments(args);
        return settingsSingleChoiceItemsFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            ArrayList<ChannelData> index = (ArrayList)getArguments().getSerializable(INDEX);
            selectedChannelList = index;
            String[] items = new String[index.size()];
            int title = getTitleId();
            boolean b_array[] = new boolean[index.size()];
            for (int i = 0; i < index.size(); i++) {
                items[i] = getString(R.string.setting_text_channel)+index.get(i).getIndex();
                if (index.get(i).isOpen()) {
                    b_array[i] = true;
                } else {
                    b_array[i] = false;
                }
            }
            if (title != R.string.prevent_squatters && title != R.string.tag_setting) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(title)
                        .setView(dialogView)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(INDEX, selectedChannelList);
                                onPositiveButtonClickListener.onPositiveButtonClick(SettingsMultiChoiceItemsFragment.this.getTag(), bundle);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                builder.setMultiChoiceItems(items, b_array, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        System.out.println("b=>"+b);
                        System.out.println("i=>"+i);
                        selectedChannelList.get(i).setOpen(b);
                    }
                });

                return builder.create();
            } else {
                Dialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(title)
                        .setView(dialogView)
                        .setMultiChoiceItems(items, b_array, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                selectedChannelList.get(i).setOpen(b);
                                System.out.println("i=>"+i);
                                System.out.println("b=>"+b);
                            }
                        })
                        .setNeutralButton(R.string.management, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(INDEX, selectedChannelList);
                                onPositiveButtonClickListener.onPositiveButtonClick(SettingsMultiChoiceItemsFragment.this.getTag(), bundle);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create();
                return dialog;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setPowerView() {
        tipsDetailTextView.setText(R.string.setting_power_tips);
    }

    private void setBroadcastKeyView(int index) {
        tipsDetailTextView.setText(R.string.setting_key_reminder);
    }

    private void setAdvIntervalView(int index) {
        tipsDetailTextView.setText(R.string.setting_interval_tips);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        dialogView = inflater.inflate(R.layout.dialog_settings_tips_v4, null);
        levelTipsTextView = (TextView) dialogView.findViewById(R.id.settings_tips_v4_tv_level_tips);
        tipsDetailTextView = (TextView) dialogView.findViewById(R.id.settings_tips_v4_tv_tips_detail);
    }

    @Override
    public void onStart() {
        super.onStart();
        DialogFragmentUtils.fitListView(getDialog(),getActivity());
    }

    public int getTitleId() {
        String tag = getTag();
        switch (tag){
            case SettingDeviceActivity.SETTINGS_BLE_POWER:
                return R.string.setting_power;
            case SettingDeviceActivity.SETTINGS_ADV_INTERVAL:
                return R.string.setting_rate;
            case SettingDeviceActivity.SETTINGS_BROADCAST_KEY:
                return R.string.prevent_squatters;
            case SettingDeviceActivity.SETTINGS_TEMP_INTERVAL:
                return R.string.setting_temp;
            case SettingDeviceActivity.SETTINGS_LIGHT_INTERVAL:
                return R.string.setting_brightness;
            case SettingDeviceActivity.SETTINGS_ACCELER:
                return R.string.setting_speed;
            case SettingDeviceActivity.SETTINGS_TLM_INTERVAL:
                return R.string.settings_tlm_interval;
            case SettingDeviceActivity.SETTINGS_HUMIDITY_INTERVAL:
                return R.string.setting_humidity;
            case SettingDeviceActivity.SETTINGS_BLE_TURNON_TIME:
                return R.string.setting_ble_turnon_time;
            case SettingDeviceActivity.SETTINGS_BLE_TURNOFF_TIME:
                return R.string.setting_ble_turnoff_time;
            case SettingDeviceActivity.SETTINGS_LORA_TXP:
                return R.string.setting_lora_txp;
            case SettingDeviceActivity.SETTINGS_DEVICE_RL_ACREL_CONNECT_SW:
            case SettingDeviceActivity.SETTINGS_DEVICE_RL_ACREL_CH_ENABLE:
            case SettingDeviceActivity.SETTINGS_DEVICE_RL_ACREL_VAL_HIGH_TYPE:
            case SettingDeviceActivity.SETTINGS_DEVICE_RL_ACREL_VAL_Low_TYPE:
            case SettingDeviceActivity.SETTINGS_DEVICE_RL_ACREL_CURR_HIGH_TYPE:
                return R.string.tag_setting;



        }
        return R.string.settings;
    }
}
