package com.fauzan.ukmbudgetmanager.divaset;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class aset {
    @SerializedName("id_aset")
    @Expose
    private int id_aset;

    @SerializedName("nama_aset")
    @Expose
    private String nama_aset;

    @SerializedName("harga_aset")
    @Expose
    private int harga_aset;

    @SerializedName("tgl_aset")
    @Expose
    private String tgl_aset;

    @SerializedName("bukti")
    @Expose
    private String bukti;

    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("message")
    @Expose
    private String message;


    public int getId_aset() {
        return id_aset;
    }

    public void setId_aset(int id_aset) {
        this.id_aset = id_aset;
    }

    public String getNama_aset() {
        return nama_aset;
    }

    public void setNama_aset(String nama_aset) {
        this.nama_aset = nama_aset;
    }

    public int getHarga_aset() {
        return harga_aset;
    }

    public void setHarga_aset(int harga_aset) {
        this.harga_aset = harga_aset;
    }

    public String getTgl_aset() {
        return tgl_aset;
    }

    public void setTgl_aset(String tgl_aset) {
        this.tgl_aset = tgl_aset;
    }

    public String getBukti() {
        return bukti;
    }

    public void setBukti(String bukti) {
        this.bukti = bukti;
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
