package com.sensoro.loratool.fragment;

import android.app.Activity;
import android.app.DialogFragment;

import com.sensoro.loratool.event.OnPositiveButtonClickListener;

/**
 * Created by Sensoro on 15/8/5.
 */
public class SettingsBaseDialogFragment extends DialogFragment {
    OnPositiveButtonClickListener onPositiveButtonClickListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            onPositiveButtonClickListener = (OnPositiveButtonClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnPositiveButtonClickListener");
        }
    }
}
