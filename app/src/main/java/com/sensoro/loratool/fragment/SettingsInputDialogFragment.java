package com.sensoro.loratool.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.SettingDeviceActivity;
import com.sensoro.loratool.activity.SettingStationActivity;
import com.sensoro.loratool.event.OnPositiveButtonClickListener;

/**
 * Created by Sensoro on 15/8/5.
 */
public class SettingsInputDialogFragment extends SettingsBaseDialogFragment {

    public static final String INPUT = "INPUT";

    private View dialogView;
    private EditText inputEditText;
    private OnPositiveButtonClickListener listener;

    public static SettingsInputDialogFragment newInstance(String input) {
        SettingsInputDialogFragment settingsInputDialogFragment = new SettingsInputDialogFragment();
        Bundle args = new Bundle();
        args.putString(INPUT, input);
        settingsInputDialogFragment.setArguments(args);
        return settingsInputDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String input = getArguments().getString(INPUT);
        inputEditText.setText(input);
        if (input != null) {
            Spannable spanText = (Spannable) inputEditText.getText();
            Selection.setSelection(spanText, input.length());
        }
        int title = 0;
        if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE1)) {
            title = R.string.settings_uid;
            inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE2)) {
            title = R.string.settings_url;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_MEASURED_RSSI)) {
            title = R.string.settings_mrssi;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_LORA_INT)) {
            title = R.string.setting_lora_int;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_LORA_TXP)) {
            title = R.string.setting_lora_txp;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE1_URL)) {
            title = R.string.settings_url;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE1_UID)) {
            title = R.string.settings_uid;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE1_EID)) {
            title = R.string.settings_eid;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE2_URL)) {
            title = R.string.settings_url;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE2_UID)) {
            title = R.string.settings_uid;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE2_EID)) {
            title = R.string.settings_eid;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE3_URL)) {
            title = R.string.settings_url;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE3_UID)) {
            title = R.string.settings_uid;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE3_EID)) {
            title = R.string.settings_eid;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE4_URL)) {
            title = R.string.settings_url;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE4_UID)) {
            title = R.string.settings_uid;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_EDDYSTONE4_EID)) {
            title = R.string.settings_eid;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_CUSTOM_PACKAGE1)) {
            title = R.string.setting_custom_package1;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_CUSTOM_PACKAGE2)) {
            title = R.string.setting_custom_package2;
        } else if (getTag().equals(SettingDeviceActivity.SETTINGS_CUSTOM_PACKAGE3)) {
            title = R.string.setting_custom_package3;
        } else if (getTag().equals(SettingStationActivity.SETTINGS_IP_ADDRESS)) {
            title = R.string.setting_text_ip_address;
        } else if (getTag().equals(SettingStationActivity.SETTINGS_ROUTER)) {
            title = R.string.setting_text_router;
        } else if (getTag().equals(SettingStationActivity.SETTINGS_SUBNET_MASK)) {
            title = R.string.setting_text_subnet_mask;
        } else if (getTag().equals(SettingStationActivity.SETTINGS_DNS)) {
            title = R.string.setting_text_dns;
        } else if (getTag().equals(SettingStationActivity.SETTINGS_SEC_DNS)) {
            title = R.string.setting_text_second_dns;
        } else if (getTag().equals(SettingStationActivity.SETTINGS_NAME)) {
            title = R.string.name;
        } else if (getTag().equals(SettingStationActivity.SETTINGS_PASSWORD)) {
            title = R.string.password;
        } else {
            title = R.string.settings;
        }

        return create(title);
    }

    public Dialog create(final int title) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str = inputEditText.getText().toString();
                        Bundle bundle = new Bundle();
                        bundle.putString(INPUT, str);
                        if (listener != null) {
                            listener.onPositiveButtonClick(getTag(), bundle);
                        }

                        onPositiveButtonClickListener.onPositiveButtonClick(getTag(), bundle);

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }
                ).setCancelable(false)
                .create();
        return alertDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        dialogView = inflater.inflate(R.layout.dialog_settings_input_v4, null);

        inputEditText = (EditText) dialogView.findViewById(R.id.settings_input_v4_et_input);
    }

    public void setOnPositiveClickListener(OnPositiveButtonClickListener listener) {
        this.listener = listener;
    }
}
