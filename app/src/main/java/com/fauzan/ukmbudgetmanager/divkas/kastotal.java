package com.fauzan.ukmbudgetmanager.divkas;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class kastotal {
    @SerializedName("total_kas")
    @Expose
    private int total_kas;

    public int getTotal_kas() {
        return total_kas;
    }

    public void setTotal_kas(int total_kas) {
        this.total_kas = total_kas;
    }
}
