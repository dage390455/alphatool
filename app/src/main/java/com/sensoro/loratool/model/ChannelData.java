package com.sensoro.loratool.model;

import java.io.Serializable;

/**
 * Created by sensoro on 18/1/25.
 */

public class ChannelData implements Serializable {
    private int index;
    private boolean isOpen;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
