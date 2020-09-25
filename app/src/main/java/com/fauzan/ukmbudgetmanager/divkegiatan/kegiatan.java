package com.fauzan.ukmbudgetmanager.divkegiatan;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class kegiatan {
    @SerializedName("id_kegiatan")
    @Expose
    private int id_kegiatan;

    @SerializedName("nama_kegiatan")
    @Expose
    private String nama_kegiatan;

    @SerializedName("tgl_kegiatan")
    @Expose
    private String tgl_kegiatan;


    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("message")
    @Expose
    private String message;

    public int getId_kegiatan() {
        return id_kegiatan;
    }

    public void setId_kegiatan(int id_kegiatan) {
        this.id_kegiatan = id_kegiatan;
    }

    public String getNama_kegiatan() {
        return nama_kegiatan;
    }

    public void setNama_kegiatan(String nama_kegiatan) {
        this.nama_kegiatan = nama_kegiatan;
    }

    public String getTgl_kegiatan() {
        return tgl_kegiatan;
    }

    public void setTgl_kegiatan(String tgl_kegiatan) {
        this.tgl_kegiatan = tgl_kegiatan;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
