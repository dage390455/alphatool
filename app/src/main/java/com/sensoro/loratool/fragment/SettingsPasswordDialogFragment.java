package com.sensoro.loratool.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.sensoro.loratool.R;


/**
 * Created by Sensoro on 15/8/5.
 */
public class SettingsPasswordDialogFragment extends SettingsBaseDialogFragment {

    public static final String PASSWORD = "PASSWORD";

    private AlertDialog passwordDialog;
    private View dialogView;
    private EditText passwordEditText;
    private EditText passwordConfirmEditText;

    private String password;

    public static SettingsPasswordDialogFragment newInstance() {
        SettingsPasswordDialogFragment settingsMajorMinorDialogFragment = new SettingsPasswordDialogFragment();
        return settingsMajorMinorDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        passwordDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.setting_password)
                .setView(dialogView)
                .setPositiveButton(getString(R.string.confirm), null)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        return passwordDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkEnterPassword()) {
                    password = passwordEditText.getText().toString();
                    Bundle bundle = new Bundle();
                    bundle.putString(PASSWORD, password);
                    onPositiveButtonClickListener.onPositiveButtonClick(getTag(), bundle);
                    dismiss();
                }
            }
        });
    }

    private boolean checkEnterPassword() {
        String enterPassword = passwordEditText.getText().toString();
        String confirmPassword = passwordConfirmEditText.getText().toString();
        if (TextUtils.isEmpty(enterPassword)) {
            passwordEditText.requestFocus();
            passwordEditText.setError(getString(R.string.password_not_be_null));
        } else if (TextUtils.isEmpty(confirmPassword)) {
            passwordConfirmEditText.requestFocus();
            passwordConfirmEditText.setError(getString(R.string.password_not_be_null));
        } else {
            if (!enterPassword.equals(confirmPassword)) {
                passwordConfirmEditText.requestFocus();
                passwordConfirmEditText.setError(getString(R.string.password_not_same));
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        dialogView = inflater.inflate(R.layout.dialog_settings_password_v4, null);

        passwordEditText = (EditText) dialogView.findViewById(R.id.settings_v4_et_password);
        passwordConfirmEditText = (EditText) dialogView.findViewById(R.id.settings_v4_et_password_confirm);
    }
}
