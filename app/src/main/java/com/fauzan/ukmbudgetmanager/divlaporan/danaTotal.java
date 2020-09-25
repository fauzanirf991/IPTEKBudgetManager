package com.fauzan.ukmbudgetmanager.divlaporan;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class danaTotal {
    @SerializedName("jumlahdana")
    @Expose
    private int jumlahdana;

    public int getJumlahdana() {
        return jumlahdana;
    }

    public void setJumlahdana(int jumlahdana) {
        this.jumlahdana = jumlahdana;
    }
}
