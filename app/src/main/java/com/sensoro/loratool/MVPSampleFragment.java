package com.sensoro.loratool;

import android.content.Context;
import android.content.Intent;

import com.sensoro.loratool.base.BaseFragment;
import com.sensoro.loratool.imainview.IMainActivityView;
import com.sensoro.loratool.presenter.MainActivityPresenter;

public class MVPSampleFragment extends BaseFragment<IMainActivityView,MainActivityPresenter> implements IMainActivityView {
    @Override
    protected void initData(Context activity) {
        //TODO 初始化UI等
        //
        mPresenter.initData(activity);
    }

    @Override
    protected int initRootViewId() {
        //TODO resID
        return 0;
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
    public void onFragmentStart() {

    }

    @Override
    public void onFragmentStop() {

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
