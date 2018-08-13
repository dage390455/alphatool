package com.sensoro.loratool;

import android.content.Intent;
import android.os.Bundle;

import com.sensoro.loratool.base.BaseActivity;
import com.sensoro.loratool.imainview.IMainActivityView;
import com.sensoro.loratool.presenter.MainActivityPresenter;

public class MVPSimpleActivity extends BaseActivity<IMainActivityView, MainActivityPresenter> implements
        IMainActivityView {
    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        //TODO 初始化UI等
        //
        mPrestener.initData(mActivity);
    }

    @Override
    protected MainActivityPresenter createPresenter() {
        return new MainActivityPresenter();
    }

    @Override
    public void startAC(Intent intent) {

    }

    @Override
    public void finishAc() {

    }

    @Override
    public void startACForResult(Intent intent, int requestCode) {

    }

    @Override
    public void setIntentResult(int resultCode) {

    }

    @Override
    public void setIntentResult(int resultCode, Intent data) {

    }

    @Override
    public void showProgressDialog() {

    }

    @Override
    public void dismissProgressDialog() {

    }

    @Override
    public void toastShort(String msg) {

    }

    @Override
    public void toastLong(String msg) {

    }
}
