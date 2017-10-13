package com.sensoro.loratool.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sensoro.loratool.R;

/**
 * Created by sensoro on 17/3/9.
 */

public class SensoroProgressDialog extends AlertDialog {

    private Context mContext;
    private ProgressBar mFirstProgress;
    private ProgressBar mSecondProgress;
    private ProgressBar mLoadingProgress;
    private TextView mTitleTextView;
    private TextView mFirstProgressTitle;
    private TextView mSecondProgressTitle;

    public SensoroProgressDialog(Context context) {
        super(context);
    }

    public SensoroProgressDialog(Context context, int theme) {
        super(context, theme);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);
        init();
    }

    public void init() {
        setCanceledOnTouchOutside(false);
        mFirstProgress = (ProgressBar) findViewById(R.id.upgrade_total_device);
        mSecondProgress = (ProgressBar) findViewById(R.id.upgrade_device_progress);
        mLoadingProgress = (ProgressBar) findViewById(R.id.upgrade_loading);
        mTitleTextView = (TextView) findViewById(R.id.upgrade_title);
        mFirstProgressTitle = (TextView) findViewById(R.id.upgrade_tv_total);
        mSecondProgressTitle = (TextView) findViewById(R.id.upgrade_tv_progress);
    }

    public ProgressBar getFirstProgress() {
        return mFirstProgress;
    }

    public ProgressBar getSecondProgress() {
        return mSecondProgress;
    }

    public ProgressBar getLoadingProgress() {
        return mLoadingProgress;
    }

    public TextView getTitle(){
        return mTitleTextView;
    }

    public TextView getFirstProgressTitle(){
        return mFirstProgressTitle;
    }

    public TextView getSecondProgressTitle(){
        return mSecondProgressTitle;
    }
}
