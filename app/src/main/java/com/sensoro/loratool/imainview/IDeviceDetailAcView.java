package com.sensoro.loratool.imainview;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public interface IDeviceDetailAcView {
    void startAc(Intent intent);

    void showShortToast(String msg);

    void setTvNameVisible(boolean isVisible);

    void setTvNameContent(String nameContent);

    void setTvNearVisible(boolean isVisible);

    void setTvVersionContent(String versionContent);

    void setTvLocationContent(String locationContent);

    void setTvStateCompoundDrawables(Drawable drawable);

    void setTvStateContent(String stateContent);

    void setTvStateTime(String stateTime);

    void setTvElectricQuantityContent(String electricQuantityContent);

    void setTvTestContent(String testContent);

    void setTvTestVisible(boolean isVisible);

    void setRcKeyList(ArrayList<String> keyList);

    void updateRcValueList(ArrayList<String> valueList);

    void setRcAdapter();

    void setTvReportTimeContent(String reportTimeContent);

    void showPopSettingItem(boolean[] itemVisible);

    void setBatteryLevel(int level);
}
