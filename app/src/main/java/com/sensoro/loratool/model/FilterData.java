package com.sensoro.loratool.model;

/**
 * Created by sensoro on 17/3/27.
 */

public class FilterData {
    private int id;
    private String name;
    private String type;
    private boolean isSelected;

    public FilterData() {

    }

    public FilterData(int id, String name,boolean isSelected) {
        this.id = id;
        this.name = name;
        this.isSelected = isSelected;
    }

    public FilterData(int id, String name,String type, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.isSelected = isSelected;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
