package com.sensoro.loratool.iwidget;

import android.content.Intent;

public interface IActivityIntent {
    void startAC(Intent intent);

    void finishAc();

    void startACForResult(Intent intent, int requestCode);

    void setIntentResult(int resultCode);

    void setIntentResult(int resultCode, Intent data);
}
