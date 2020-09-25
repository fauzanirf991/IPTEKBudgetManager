package com.fauzan.ukmbudgetmanager.divlaporan;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ttlPengeluaran {
    @SerializedName("ttl_pengeluaran")
    @Expose
    private int ttl_pengeluaran;

    public int getTtl_pengeluaran() {
        return ttl_pengeluaran;
    }

    public void setTtl_pengeluaran(int ttl_pengeluaran) {
        this.ttl_pengeluaran = ttl_pengeluaran;
    }
}
