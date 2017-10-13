package com.sensoro.lora.setting.server.bean;

import android.os.Parcelable;

import java.util.List;

/**
 * Created by tangrisheng on 2016/5/5.
 * Login Response
 */
public class LoginRsp extends ResponseBase {
    protected String id;
    protected String email;
    protected String name;
    protected String owner;
    protected String cellphone;
    protected boolean showNavigation;
    protected boolean buyStation;
    protected boolean twofactorauth;
    protected String avatar;
    protected Role roles;
    protected List<String> permission;
    protected Settings settings;
    protected int level;
    protected String expires;
    protected String sessionId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public boolean isShowNavigation() {
        return showNavigation;
    }

    public void setShowNavigation(boolean showNavigation) {
        this.showNavigation = showNavigation;
    }

    public boolean isBuyStation() {
        return buyStation;
    }

    public void setBuyStation(boolean buyStation) {
        this.buyStation = buyStation;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Role getRoles() {
        return roles;
    }

    public void setRoles(Role roles) {
        this.roles = roles;
    }

    public List<String> getPermission() {
        return permission;
    }

    public void setPermission(List<String> permission) {
        this.permission = permission;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public String getExpires() {
        return expires;
    }

    public LoginRsp setExpires(String expires) {
        this.expires = expires;
        return this;
    }

    public boolean isTwofactorauth() {
        return twofactorauth;
    }

    public LoginRsp setTwofactorauth(boolean twofactorauth) {
        this.twofactorauth = twofactorauth;
        return this;
    }

    @Override
    public String toString() {
        return "LoginRsp{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", cellphone='" + cellphone + '\'' +
                ", showNavigation=" + showNavigation +
                ", buyStation=" + buyStation +
                ", avatar='" + avatar + '\'' +
                ", roles=" + roles +
                ", permission=" + permission +
                ", level=" + level +
                ", sessionId='" + sessionId + '\'' +
                ", expires='" + expires + '\'' +
                ", twofactorauth='" + twofactorauth+ '\'' +
                '}';
    }

}
