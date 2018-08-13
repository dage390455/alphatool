package com.sensoro.loratool.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sensoro.loratool.iwidget.IOnFragmentStart;
import com.sensoro.loratool.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by DDONG on 2017/7/12 0012.
 */

public abstract class BaseFragment<V, P extends BasePresenter<V>> extends Fragment implements IOnFragmentStart {
    protected P mPresenter;
    protected View mRootView;
    protected Unbinder unbinder;
    protected BaseFragment mRootFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(initRootViewId(), container, false);
        }
        mPresenter = createPresenter();
        mPresenter.attachView((V) this);
        V view = mPresenter.getView();
        if (view instanceof BaseFragment) {
            mRootFragment = (BaseFragment) view;
        } else {
            LogUtils.loge(this, "当前View转换异常！");
            mRootFragment = this;
        }
        unbinder = ButterKnife.bind(mPresenter.getView(), mRootView);
        LogUtils.logd("onCreateView");
        return mRootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 页面埋点
        MobclickAgent.onResume(mRootFragment.getActivity());
    }

    /**
     * fragment onStart
     */
    @Override
    public void onStart() {
        super.onStart();
        try {
            if (mPresenter != null && getUserVisibleHint()) {
                MobclickAgent.onPageStart(mRootFragment.getClass().getSimpleName());
                onFragmentStart();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * fragment onStop
     */
    @Override
    public void onStop() {
        super.onStop();
        try {
            if (mPresenter != null && getUserVisibleHint()) {
                MobclickAgent.onPageEnd(mRootFragment.getClass().getSimpleName());
                onFragmentStop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * fragment 选中
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        try {
            if (mPresenter != null) {
                if (isVisibleToUser) {
                    MobclickAgent.onPageStart(mRootFragment.getClass().getSimpleName());
                    onFragmentStart();
                } else {
                    MobclickAgent.onPageEnd(mRootFragment.getClass().getSimpleName());
                    onFragmentStop();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        // 页面埋点
        MobclickAgent.onPause(mRootFragment.getActivity());
    }

    protected abstract void initData(Context activity);

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtils.logd("onActivityCreated");
        initData(mRootFragment.getActivity());
    }

    protected abstract int initRootViewId();

    protected abstract P createPresenter();

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        if (mPresenter != null) {
            mPresenter.onDestroy();
            mPresenter.detachView();
            mPresenter = null;
        }
        if (mRootFragment != null) {
            mRootFragment = null;
        }
        super.onDestroyView();
    }
}
