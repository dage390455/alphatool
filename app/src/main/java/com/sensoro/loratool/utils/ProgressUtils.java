package com.sensoro.loratool.utils;

import android.app.Activity;
import android.app.ProgressDialog;


import com.sensoro.loratool.R;

import java.lang.ref.WeakReference;

public class ProgressUtils {
    private Builder builder;

    public ProgressUtils(Builder builder) {
        this.builder = builder;
    }

    public void showProgress() {
        ProgressDialog progressDialog = builder.getProgressDialog();
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

    public void dismissProgress() {
        ProgressDialog progressDialog = builder.getProgressDialog();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

    }

    public void destroyProgress() {
        builder.destroyProgressDialog();
    }

    public static final class Builder {
        private ProgressDialog progressDialog;
        private WeakReference<Activity> activity;
        private String message;

        public Builder(Activity ac) {
            activity = new WeakReference<>(ac);
            message = activity.get().getString(R.string.loading);
        }

        public Builder build() {
            progressDialog = new ProgressDialog(activity.get());
            progressDialog.setMessage(message);
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        private ProgressDialog getProgressDialog() {
            if (progressDialog != null) {
                return progressDialog;
            }
            return null;
        }

        private void destroyProgressDialog() {
            if (progressDialog != null) {
                progressDialog.cancel();
                progressDialog = null;
            }
            if (activity != null) {
                activity.clear();
                activity = null;
            }
        }
    }
}
