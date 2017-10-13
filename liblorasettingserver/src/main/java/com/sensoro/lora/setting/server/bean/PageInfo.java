package com.sensoro.lora.setting.server.bean;

/**
 * Created by fangping on 2016/7/22.
 */

public class PageInfo  {
    private int count;
    private int current_page;
    private int total_count;


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCurrent_page() {
        return current_page;
    }

    public void setCurrent_page(int current_page) {
        this.current_page = current_page;
    }

    public int getTotal_count() {
        return total_count;
    }

    public void setTotal_count(int total_count) {
        this.total_count = total_count;
    }
}
