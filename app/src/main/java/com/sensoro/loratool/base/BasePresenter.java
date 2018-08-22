package com.sensoro.loratool.base;

import android.content.Context;

import com.sensoro.lora.setting.server.bean.DeviceInfo;
import com.sensoro.loratool.iwidget.IOnDestroy;

import java.lang.ref.WeakReference;

/**
 * @author DDONG
 * @date 2018/2/4 0004
 */

public abstract class BasePresenter<V> implements IOnDestroy {
    private WeakReference<V> mViewRef;

    /**
     * 这里采用弱引用
     *
     * @param view
     */
    public void attachView(V view) {
        mViewRef = new WeakReference<>(view);
    }

    /**
     * 获取当前的View
     *
     * @return
     */
    protected V getView() {
        V v = mViewRef.get();
        return v;
    }

    /**
     * 查看View是否存在
     *
     * @return
     */
    public boolean isAttachedView() {
        return mViewRef != null && mViewRef.get() != null;
    }

    /**
     * 解除View
     */
    public void detachView() {
        if (mViewRef != null) {
            mViewRef.clear();
            mViewRef = null;
        }
    }

    public abstract void initData(Context context);


}
