package com.sensoro.loratool.utils;

public interface PermissionsResultObserve {
    void onPermissionGranted();

    void onPermissionDenied();
}
