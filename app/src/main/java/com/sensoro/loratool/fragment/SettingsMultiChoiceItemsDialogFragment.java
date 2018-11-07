package com.sensoro.loratool.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.sensoro.loratool.R;
import com.sensoro.loratool.activity.SettingDeviceActivity;
import com.sensoro.loratool.model.ChannelData;
import com.sensoro.loratool.utils.DialogFragmentUtils;

import java.util.ArrayList;


/**
 * Created by Sensoro on 15/8/5.
 */
public class SettingsMultiChoiceItemsDialogFragment extends SettingsBaseDialogFragment {

    private static ArrayList<ChannelData> selectedChannelList;
    public static String RESULT = "result";

    public static SettingsMultiChoiceItemsDialogFragment newInstance(ArrayList<ChannelData> channelDatas) {
        SettingsMultiChoiceItemsDialogFragment settingsSingleChoiceItemsFragment = new SettingsMultiChoiceItemsDialogFragment();
        selectedChannelList =  channelDatas;
        return settingsSingleChoiceItemsFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            String[] items = new String[selectedChannelList.size()];
            boolean[] itemStatus = new boolean[selectedChannelList.size()];
            for (int i = 0; i < selectedChannelList.size(); i++) {
                ChannelData channelData = selectedChannelList.get(i);
                items[i] = "通道" + channelData.getIndex();
                itemStatus[i] = channelData.isOpen();

            }
                Dialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle("设置")
                        .setMultiChoiceItems(items, itemStatus, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                selectedChannelList.get(i).setOpen(b);
                                System.out.println("i=>"+i);
                                System.out.println("b=>"+b);
                            }
                        })
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(RESULT, selectedChannelList);
                                onPositiveButtonClickListener.onPositiveButtonClick(SettingsMultiChoiceItemsDialogFragment.this.getTag(), bundle);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create();
                return dialog;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        DialogFragmentUtils.fitListView(getDialog(),getActivity());
    }

}
