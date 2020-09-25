package com.fauzan.ukmbudgetmanager.divpengeluaran;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class eventneeds {
    @SerializedName("id_pengeluaran")
    @Expose
    private int id_pengeluaran;

    @SerializedName("nama_pengeluaran")
    @Expose
    private String nama_pengeluaran;

    @SerializedName("jumlah_item")
    @Expose
    private int jumlah_item;

    @SerializedName("harga_satuan")
    @Expose
    private int harga_satuan;

    @SerializedName("tgl_pengeluaran")
    @Expose
    private String tgl_pengeluaran;

    @SerializedName("bukti")
    @Expose
    private String bukti;

    @SerializedName("id_kegiatan")
    @Expose
    private int id_kegiatan;

    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("message")
    @Expose
    private String message;

    public int getId_pengeluaran() {
        return id_pengeluaran;
    }

    public void setId_pengeluaran(int id_pengeluaran) {
        this.id_pengeluaran = id_pengeluaran;
    }

    public String getNama_pengeluaran() {
        return nama_pengeluaran;
    }

    public void setNama_pengeluaran(String nama_pengeluaran) {
        this.nama_pengeluaran = nama_pengeluaran;
    }

    public int getJumlah_item() {
        return jumlah_item;
    }

    public void setJumlah_item(int jumlah_item) {
        this.jumlah_item = jumlah_item;
    }

    public int getHarga_satuan() {
        return harga_satuan;
    }

    public void setHarga_satuan(int harga_satuan) {
        this.harga_satuan = harga_satuan;
    }

    public String getTgl_pengeluaran() {
        return tgl_pengeluaran;
    }

    public void setTgl_pengeluaran(String tgl_pengeluaran) {
        this.tgl_pengeluaran = tgl_pengeluaran;
    }

    public String getBukti() {
        return bukti;
    }

    public void setBukti(String bukti) {
        this.bukti = bukti;
    }

    public int getId_kegiatan() {
        return id_kegiatan;
    }

    public void setId_kegiatan(int id_kegiatan) {
        this.id_kegiatan = id_kegiatan;
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
