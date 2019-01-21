package com.sensoro.loratool.widget;

import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sensoro.loratool.R;

public class SettingEnterDialogUtils {
    private final TextInputEditText mEt;
    private final TextView mTvCancel;
    private final TextView mTvConfirm;
    private final TextView mTvTitle;
    private final TextInputLayout mInputLayout;
    public SettingEnterUtilsClickListener listener;
    private CustomCornerDialog mDialog;
    private Activity mActivity;
    private Float min;
    private Float max;
    private String errMsg;
    private String hint;

    public SettingEnterDialogUtils(Activity activity) {
        this(activity,activity.getString(R.string.please_enter_thresold),null,-1,-1,false);
    }

    public SettingEnterDialogUtils(Activity activity,String hint,float max,float min) {
        this(activity,hint,null,max,min,false);

    }

    public SettingEnterDialogUtils(Activity activity,String hint) {
        this(activity,hint,null,-1,-1,false);

    }

    public SettingEnterDialogUtils(Activity activity,String hint,String errMsg,float max,float min) {
        this(activity,hint,errMsg,max,min,false);

    }

    public SettingEnterDialogUtils(Activity activity,String hint,String errMsg,float max,float min,boolean cancelable) {
        mActivity = activity;
        initMsg(null, hint,errMsg,max,min);
        View view = View.inflate(activity, R.layout.item_setting_input_dialog, null);
        mTvTitle = view.findViewById(R.id.item_setting_input_tv_title);
        mEt = view.findViewById(R.id.item_setting_input_et);
        mInputLayout = view.findViewById(R.id.item_setting_input_layout);
        mTvCancel = view.findViewById(R.id.item_setting_tv_cancel);
        mTvConfirm = view.findViewById(R.id.item_setting_tv_confirm);
        mDialog = new CustomCornerDialog(activity, R.style.CustomCornerDialogStyle, view);
        mDialog.setCancelable(cancelable);

        mInputLayout.setHint(this.hint);
        mEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkNumber(s);

            }
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (listener != null) {
                    listener.onCancelClick();
                }
            }
        });

        mTvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Double aFloat = Double.valueOf(mEt.getText().toString());
                    if(max == -1 && min == -1){
                        mInputLayout.setErrorEnabled(false);
                        mDialog.dismiss();
                        if (listener != null) {
                            listener.onConfirmClick(aFloat);
                        }
                        return;
                    }
                    if(aFloat<min ||aFloat>max){
                        if (TextUtils.isEmpty(errMsg)) {
                            mInputLayout.setError(mActivity.getString(R.string.beyond_value_rang));
                        }else{
                            mInputLayout.setError(errMsg);
                        }
                        Toast.makeText(mActivity.getApplicationContext(),mActivity.getString(R.string.please_enter_correct_value),Toast.LENGTH_SHORT).show();

                    }else{
                        mInputLayout.setErrorEnabled(false);
                        mDialog.dismiss();
                        if (listener != null) {
                            listener.onConfirmClick(aFloat);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mEt.setError(mActivity.getString(R.string.number_format_error));
                    Toast.makeText(mActivity.getApplicationContext(),mActivity.getString(R.string.please_enter_correct_value),Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void initMsg(String content, String hint, String errMsg, float max, float min) {
        this.hint = hint;
        this.errMsg = errMsg;
        this.max = max;
        this.min = min;
        if (mEt != null) {
            mEt.setText(content);
            mEt.setSelection(content.length());
        }
        if (mInputLayout != null) {
            mInputLayout.setHint(hint);
        }
    }

    private void checkNumber(Editable s) {
        if(max == -1 && min == -1){
            return;
        }
        if (TextUtils.isEmpty(s.toString())) {
            mInputLayout.setErrorEnabled(false);
            return;
        }
        try {
            Float aFloat = Float.valueOf(s.toString());
            if(aFloat<min ||aFloat>max){
                if (TextUtils.isEmpty(errMsg)) {
                    mInputLayout.setError(mActivity.getString(R.string.beyond_value_rang));
                }else{
                    mInputLayout.setError(errMsg);
                }
            }else{
                mInputLayout.setErrorEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mInputLayout.setError(mActivity.getString(R.string.number_format_error));
        }
    }

    public boolean isShowing() {
        if (mDialog != null) {
            return mDialog.isShowing();
        }
        return false;
    }

    public void setTipTitleText(String text) {
        mTvTitle.setText(text);
    }

    public void setTipEditText(String text) {
        mEt.setText(text);
    }

    public void setTipCacnleText(String text, @ColorInt int color) {
        mTvCancel.setText(text);
        mTvCancel.setTextColor(color);
    }

    public void setTipConfirmText(String text, @ColorInt int color) {
        mTvConfirm.setText(text);
        mTvConfirm.setTextColor(color);
    }

    public void show(String content, String hint, String errMsg, float max, float min, SettingEnterUtilsClickListener listener) {
        if (mDialog != null) {
            initMsg(content,hint,errMsg,max,min);
            this.listener = listener;
            mDialog.show();
        }
    }

    public void show(String content, String hint, SettingEnterUtilsClickListener listener) {
        if (mDialog != null) {
            initMsg(content,hint,null,-1,-1);
            this.listener = listener;
            mDialog.show();
        }
    }


    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    public void destroy() {
        if (mDialog != null) {
            mDialog.cancel();
            mDialog = null;
        }
    }

    public void setTipDialogUtilsClickListener(SettingEnterUtilsClickListener listener) {
        this.listener = listener;
    }

    public void setTipConfirmVisible(boolean isVisible) {
        mTvConfirm.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }


    public interface SettingEnterUtilsClickListener {
        void onCancelClick();

        void onConfirmClick(double value);
    }
}
