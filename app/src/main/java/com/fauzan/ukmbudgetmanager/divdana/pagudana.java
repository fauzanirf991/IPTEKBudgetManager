package com.fauzan.ukmbudgetmanager.divdana;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class pagudana {
    @SerializedName("id_pagudana")
    @Expose
    private int id_pagudana;

    @SerializedName("jumlahdana")
    @Expose
    private int jumlahdana;

    @SerializedName("tgl_diterima")
    @Expose
    private String tgl_diterima;

    @SerializedName("bukti")
    @Expose
    private String bukti;

    @SerializedName("value")
    @Expose
    private String value;

    @SerializedName("message")
    @Expose
    private String message;

    public int getId_pagudana(){
        return id_pagudana;
    }
    public void setId_pagudana(int id_pagudana){
        this.id_pagudana=id_pagudana;
    }

    public int getJumlahdana() {
        return jumlahdana;
    }

    public void setJumlahdana(int jumlahdana) {
        this.jumlahdana = jumlahdana;
    }

    public String getTgl_diterima() {
        return tgl_diterima;
    }

    public void setTgl_diterima(String tgl_diterima) {
        this.tgl_diterima = tgl_diterima;
    }

    public String getBukti() {
        return bukti;
    }

    public void setBukti(String bukti) {
        this.bukti = bukti;
    }
    public String getValue(){
        return value;
    }
    public void setValue(String value){
        this.value = value;
    }

    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }

}
