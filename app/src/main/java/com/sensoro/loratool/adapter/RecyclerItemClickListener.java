package com.sensoro.loratool.adapter;

import com.sensoro.loratool.model.SettingDeviceModel;

public interface RecyclerItemClickListener {
    void onItemClick(SettingDeviceModel model,int position);
}
