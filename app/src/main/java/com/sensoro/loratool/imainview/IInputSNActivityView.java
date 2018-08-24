package com.sensoro.loratool.imainview;

import android.app.Activity;
import android.content.Intent;

import com.sensoro.loratool.iwidget.IProgressDialog;
import com.sensoro.loratool.iwidget.IToast;

public interface IInputSNActivityView extends IToast,IProgressDialog{
    void startAc(Intent intent);
}
