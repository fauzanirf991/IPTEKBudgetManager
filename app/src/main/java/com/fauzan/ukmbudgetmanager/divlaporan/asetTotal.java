package com.fauzan.ukmbudgetmanager.divlaporan;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class asetTotal {
    @SerializedName("total_aset")
    @Expose
    private int total_aset;

    public int getTotal_aset() {
        return total_aset;
    }

    public void setTotal_aset(int total_aset) {
        this.total_aset = total_aset;
    }
}
