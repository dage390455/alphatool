package com.sensoro.loratool.model;

public class SearchKeywords {

    public String keyword;
    public long value;
    public long time;
    public int type;

    public SearchKeywords(String keyword, long value, long time, int type) {
        this.keyword = keyword;
        this.value = value;
        this.time = time;
        this.type = type;
    }
}
