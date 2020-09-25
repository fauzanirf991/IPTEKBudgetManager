package com.fauzan.ukmbudgetmanager.divlaporan;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class sisaDana {
    @SerializedName("sisa_dana")
    @Expose
    private int sisa_dana;

    public int getSisa_dana() {
        return sisa_dana;
    }

    public void setSisa_dana(int sisa_dana) {
        this.sisa_dana = sisa_dana;
    }
}
