package com.sensoro.lora.setting.server.bean;

import java.util.List;

/**
 * Created by sensoro on 16/9/22.
 */

public class Settings {
    private List<String> permissionSet;

    public List<String> getPermissionSet() {
        return permissionSet;
    }

    public void setPermissionSet(List<String> permissionSet) {
        this.permissionSet = permissionSet;
    }
}
