package com.sensoro.loratool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sensoro.loratool.service.PollingService;

/**
 * Created by sensoro on 16/9/5.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        Intent i = new Intent(context, PollingService.class);
//        if (Build.VERSION.SDK_INT >= 26) {
//            context.startForegroundService(i);
//        } else {
            context.startService(i);
//        }

    }
}
