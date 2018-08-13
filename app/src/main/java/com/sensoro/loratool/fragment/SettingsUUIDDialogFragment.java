package com.sensoro.loratool.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Selection;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.sensoro.loratool.R;


/**
 * Created by Sensoro on 15/8/5.
 */
public class SettingsUUIDDialogFragment extends SettingsBaseDialogFragment implements View.OnClickListener {

    public static final String UUID = "uuid";

    private View dialogView;
    private EditText uuidEditText;
    private RelativeLayout sensoroUUIDRelativeLayout;
    private RelativeLayout airlocateUUIDRelativeLayout;
    private RelativeLayout estimoteUUIDRelativeLayout;
    private RelativeLayout wxUUIDRelativeLayout;

    public static SettingsUUIDDialogFragment newInstance(String uuid) {
        SettingsUUIDDialogFragment settingsUUIDDialogFragment = new SettingsUUIDDialogFragment();
        Bundle args = new Bundle();
        args.putString(UUID, uuid);
        settingsUUIDDialogFragment.setArguments(args);
        return settingsUUIDDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getArguments().getString(UUID) != null) {
            String uuid = getArguments().getString(UUID).toUpperCase();
            StringBuilder uuidString = new StringBuilder(uuid);
            uuidString.insert(8, "-");
            uuidString.insert(13, "-");
            uuidString.insert(18, "-");
            uuidString.insert(23, "-");
            uuidEditText.setText(uuidString);
            Spannable spanText =  uuidEditText.getText();
            Selection.setSelection(spanText, uuid.length());
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.setting_uuid)
                .setView(dialogView)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        String uuidText = uuidEditText.getText().toString();

                        bundle.putString(UUID, uuidText.replaceAll("-", ""));
                        onPositiveButtonClickListener.onPositiveButtonClick(SettingsUUIDDialogFragment.this.getTag(), bundle);
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
        dialogView = inflater.inflate(R.layout.dialog_settings_uuid_v4, null);

        uuidEditText = (EditText) dialogView.findViewById(R.id.settings_v4_et_uuid);
        sensoroUUIDRelativeLayout = (RelativeLayout) dialogView.findViewById(R.id.settings_v4_rl_sensoro_uuid);
        sensoroUUIDRelativeLayout.setOnClickListener(this);
        estimoteUUIDRelativeLayout = (RelativeLayout) dialogView.findViewById(R.id.settings_v4_rl_estimote_uuid);
        estimoteUUIDRelativeLayout.setOnClickListener(this);
        airlocateUUIDRelativeLayout = (RelativeLayout) dialogView.findViewById(R.id.settings_v4_rl_airlocate_uuid);
        airlocateUUIDRelativeLayout.setOnClickListener(this);
        wxUUIDRelativeLayout = (RelativeLayout) dialogView.findViewById(R.id.settings_v4_rl_wx_uuid);
        wxUUIDRelativeLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_v4_rl_sensoro_uuid:
                uuidEditText.setText(R.string.sensoro_uuid);
                break;
            case R.id.settings_v4_rl_estimote_uuid:
                uuidEditText.setText(R.string.estimote_uuid);
                break;
            case R.id.settings_v4_rl_airlocate_uuid:
                uuidEditText.setText(R.string.airlocate_uuid);
                break;
            case R.id.settings_v4_rl_wx_uuid:
                uuidEditText.setText(R.string.wx_uuid);
                break;
            default:
                break;
        }
    }
}
