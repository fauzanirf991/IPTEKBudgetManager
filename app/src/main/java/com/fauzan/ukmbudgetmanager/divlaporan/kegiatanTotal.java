package com.fauzan.ukmbudgetmanager.divlaporan;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class kegiatanTotal {
    @SerializedName("total_kegiatan")
    @Expose
    private int total_kegiatan;

    public int getTotal_kegiatan() {
        return total_kegiatan;
    }

    public void setTotal_kegiatan(int total_kegiatan) {
        this.total_kegiatan = total_kegiatan;
    }
}
