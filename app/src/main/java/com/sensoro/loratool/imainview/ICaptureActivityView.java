package com.sensoro.loratool.imainview;

import com.sensoro.loratool.iwidget.IActivityIntent;
import com.sensoro.loratool.iwidget.IToast;

public interface ICaptureActivityView extends IToast,IActivityIntent{
    void startScan();
    void stopScan();
}
