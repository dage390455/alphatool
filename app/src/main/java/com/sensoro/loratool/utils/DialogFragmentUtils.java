package com.sensoro.loratool.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ListView;

public class DialogFragmentUtils {
    /**
     * 适配item数量过多时，button不显示的bug
     */
    public static void fitListView(Dialog dialog, final Activity activity) {
        if (dialog==null) {
            return;
        }

        AlertDialog alertDialog ;
        if (dialog instanceof AlertDialog) {
            alertDialog = (AlertDialog) dialog;
        }else{
            return;
        }

        final ListView listView = alertDialog.getListView();
        if (listView == null) {
            return;
        }
        listView.post(new Runnable() {
            @Override
            public void run() {
                DisplayMetrics dm = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

                int height = listView.getHeight();
                int h = (int) (dm.heightPixels * 0.6);
                if(height>h){
                    ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
                    layoutParams.height = h;
                    listView.setLayoutParams(layoutParams);
                }

            }
        });

    }
}
