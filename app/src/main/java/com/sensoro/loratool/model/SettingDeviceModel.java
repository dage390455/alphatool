package com.sensoro.loratool.model;

public class SettingDeviceModel {
    public String name;
    public String content;
    public boolean isArrow = true;
    public boolean isDivider = true;
    public String title;
    public int cmd;
    public String hint;
    public String errMsg;
    //判断范围 包括min max两个值
    public Float max;
    public Float min;
    public Object tag;

    // 1 普通的带有箭头的布局 2.title布局，只展示，不可点击
    public int viewType = 1;
    // 1.输入数值对话框 2.电表命令
    public int eventType;
    public boolean canClick = true;


    public SettingDeviceModel() {

    }

    public SettingDeviceModel(String name, String content,int eventType) {
        this.name = name;
        this.content = content;
        this.eventType = eventType;
    }

    public SettingDeviceModel(String name, String content, boolean isArrow,int eventType) {
        this(name,content,eventType);
        this.isArrow = isArrow;
    }

    public SettingDeviceModel(String name, String content, int eventType,boolean isArrow,boolean isDivider) {
        this(name,content,isArrow,eventType);
        this.isDivider = isDivider;

    }



}
