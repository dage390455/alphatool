package com.sensoro.lora.setting.server.bean;

/**
 * Created by tangrisheng on 2016/5/5.
 * login
 */
public class LoginReq {

    public LoginReq() {
    }

    public LoginReq(String email, String password) {
        this.email = email;
        this.password = password;
    }

    protected String email;
    protected String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
