package com.sensoro.loratool.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.SettingDeviceActivity;


/**
 * Created by Sensoro on 15/8/5.
 */
public class SettingsMajorMinorDialogFragment extends SettingsBaseDialogFragment {

    public static final String VALUE = "value";

    private View dialogView;
    private EditText hexEditText;
    private EditText decEditText;

    private int majorMinorValue;

    public static SettingsMajorMinorDialogFragment newInstance(int value) {
        SettingsMajorMinorDialogFragment settingsMajorMinorDialogFragment = new SettingsMajorMinorDialogFragment();
        Bundle args = new Bundle();
        args.putInt(VALUE, value);
        settingsMajorMinorDialogFragment.setArguments(args);
        return settingsMajorMinorDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int value = getArguments().getInt(VALUE);

        String majorMinorHex = String.format("%04X", value);
        String majorMinorDec = String.valueOf(value);
        hexEditText.setText(majorMinorHex);
        Spannable spanText = (Spannable) hexEditText.getText();
        Selection.setSelection(spanText, majorMinorHex.length());
        decEditText.setText(majorMinorDec);
        spanText = (Spannable) decEditText.getText();
        Selection.setSelection(spanText, majorMinorDec.length());
        hexEditText.addTextChangedListener(hexTextWatcher);
        decEditText.addTextChangedListener(decimcalTextWatcher);

        int title = 0;
        if (SettingsMajorMinorDialogFragment.this.getTag().equals(SettingDeviceActivity.SETTINGS_MAJOR)) {
            title = R.string.setting_major;
        } else if (SettingsMajorMinorDialogFragment.this.getTag().equals(SettingDeviceActivity.SETTINGS_MINOR)) {
            title = R.string.setting_minor;
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        bundle.putInt(VALUE, Integer.valueOf(decEditText.getText().toString()));
                        onPositiveButtonClickListener.onPositiveButtonClick(SettingsMajorMinorDialogFragment.this.getTag(), bundle);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        dialogView = inflater.inflate(R.layout.dialog_settings_major_minor_v4, null);

        hexEditText = (EditText) dialogView.findViewById(R.id.settings_v4_et_major_minor_hex);
        decEditText = (EditText) dialogView.findViewById(R.id.settings_v4_et_major_minor_dec);
    }

    private TextWatcher hexTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            decEditText.removeTextChangedListener(decimcalTextWatcher);
            try {
                majorMinorValue = Integer.parseInt(s.toString(), 16);
            } catch (Exception e) {
                if (s.toString().equals("")) {
                    decEditText.setText("");
                    decEditText.setSelection(0);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.data_invalid), Toast.LENGTH_SHORT).show();
                    hexEditText.setText("");
                    hexEditText.setSelection(0);
                }
                decEditText.addTextChangedListener(decimcalTextWatcher);
                return;
            }

            if (majorMinorValue > 65535 || majorMinorValue < 0) {
                Toast.makeText(getActivity(), getString(R.string.data_invalid), Toast.LENGTH_SHORT).show();
                hexEditText.setText("");
                hexEditText.setSelection(0);
                decEditText.addTextChangedListener(decimcalTextWatcher);
                return;
            }

            if (s.toString().equals("")) {
                decEditText.setText("");
                decEditText.setSelection(0);
            } else {
                decEditText.setText("" + majorMinorValue);
                decEditText.setSelection(("" + majorMinorValue).length());
            }
            decEditText.addTextChangedListener(decimcalTextWatcher);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher decimcalTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            hexEditText.removeTextChangedListener(hexTextWatcher);
            try {
                majorMinorValue = Integer.parseInt(s.toString(), 10);
            } catch (Exception e) {
                if (s.toString().equals("")) {
                    hexEditText.setText("");
                    hexEditText.setSelection(0);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.data_invalid), Toast.LENGTH_SHORT).show();
                    decEditText.setText("");
                    decEditText.setSelection(0);
                }
                hexEditText.addTextChangedListener(hexTextWatcher);
                return;
            }

            if (majorMinorValue > 65535 || majorMinorValue < 0) {
                Toast.makeText(getActivity(), getString(R.string.data_invalid), Toast.LENGTH_SHORT).show();
                decEditText.setText("");
                decEditText.setSelection(0);
                hexEditText.addTextChangedListener(hexTextWatcher);
                return;
            }

            if (s.toString().equals("")) {
                hexEditText.setText("");
                hexEditText.setSelection(0);
            } else {
                String text = Integer.toHexString(majorMinorValue) + "";
                hexEditText.setText(text);
                hexEditText.setSelection(text.length());
            }
            hexEditText.addTextChangedListener(hexTextWatcher);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
