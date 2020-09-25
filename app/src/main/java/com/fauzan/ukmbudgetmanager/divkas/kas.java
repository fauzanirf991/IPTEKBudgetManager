package com.fauzan.ukmbudgetmanager.divkas;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class kas {
    @SerializedName("id_kas")
    @Expose
    private int id_kas;

    @SerializedName("nama_pembayar")
    @Expose
    private String nama_pembayar;

    @SerializedName("jumlah_bayar")
    @Expose
    private int jumlah_bayar;

    @SerializedName("tgl_bayar")
    @Expose
    private String tgl_bayar;

    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("message")
    @Expose
    private String message;

    public int getId_kas() {
        return id_kas;
    }

    public void setId_kas(int id_kas) {
        this.id_kas = id_kas;
    }

    public String getNama_pembayar() {
        return nama_pembayar;
    }

    public void setNama_pembayar(String nama_pembayar) {
        this.nama_pembayar = nama_pembayar;
    }

    public int getJumlah_bayar() {
        return jumlah_bayar;
    }

    public void setJumlah_bayar(int jumlah_bayar) {
        this.jumlah_bayar = jumlah_bayar;
    }

    public String getTgl_bayar() {
        return tgl_bayar;
    }

    public void setTgl_bayar(String tgl_bayar) {
        this.tgl_bayar = tgl_bayar;
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
